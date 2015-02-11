package reilaender.connector;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.DatabaseMetaData;

import reilaender.parser.Parser;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class Connector {
	private Connection con;
	private Statement st;
	private MysqlDataSource ds;
	private Parser parser;
	/**
	 * Contains every single table. It is just empty if u didn't call refresh() before.
	 */
	private ArrayList<String> tables;
	/**
	 * Contains every single table with his PK, FK and attributes. It is just empty if u didn't call refresh() before.
	 */
	private HashMap<String, ArrayList<String>> table_keys;
	/**
	 * 
	 * @param parser It just works with my Parser class! It is needed to connect to the Database
	 */
	public Connector(Parser parser) {
		this.parser = parser;
		tables = new ArrayList<>();
		table_keys = new HashMap<>();
	}
	/**
	 * It <u>ONLY</u> works if you called the constructor with <u>MY</u> Parser class. 
	 * First Method to be called. Connects to a Database using a Parsers static attributes.
	 * @throws SQLException
	 */
	public void connect() throws SQLException {
		ds = new MysqlDataSource();

		if(parser.getConsole() == null) {
			//this is just needed on my computer
			if(System.getProperty("user.name").toLowerCase().equals("manuel")) {
				System.err.println("Username is manuel");
				parser.PASSWORD = "manuel";
			}
		}
		ds.setServerName(parser.IP);
		ds.setUser(parser.USERNAME);
		ds.setPassword(parser.PASSWORD);
		ds.setDatabaseName(parser.DATABASE);

		con = ds.getConnection();
		st = con.createStatement();
	}
	/**
	 * Executes a query
	 * @param query Query to be executed
	 * @return ResultSet of the query
	 * @throws SQLException Exception will be thrown if the connection wasn't successfully created.
	 */
	public ResultSet executeQuery(String query) throws SQLException {
		return st.executeQuery(query);
	}
	/**
	 * Closes all Streams (Statement, Connection)
	 */
	public void closeStreams() {
		try {
			st.close();
		} catch (SQLException e) {
			System.err.println("Statement has already been closed");
		}
		try {
			con.close();
		} catch (SQLException e) {
			System.err.println("Connection has already been closed");
		}
	}
	/**
	 * Saves foreign and primary keys, foreign key references (table names) in a HashMap< String1, ArrayList< String2> in which 
	 * <li>String1 stands for the Tablename</li>
	 * <li>ArrayList is a List of all Attributes marked as PK (Primary Key) and/or FK (Foreign Key) like this: &lt;&lt;PK&gt;&gt;, &lt;&lt;FK&gt;&gt;</li>
	 */
	//TODO Code-Optimizing
	//Some Statements are multiple in this method
	public void refresh() {
		try {
			DatabaseMetaData tmp = getCon().getMetaData();					//tmp gets all the meta-data from the db you are connected with
																			//tmp.getImportedKeys(con.getCatalog(), null, null);
			ResultSet tmprs = tmp.getTables(null, null, null, null);		//seperate the metadata into specific tables, which are implemented in the specific database
			while(tmprs.next()) {											//walking step-by-step through the tables
				String table_name = tmprs.getString(3);						//getString() returns specific metadata => parameter 3 is for the tablename
				ResultSet tmppk = tmp.getPrimaryKeys(null, null, table_name),					//Now 3 ResultSet Objects got declared and initialized. tmppk=> Primary Key
						tmpfk = tmp.getImportedKeys(con.getCatalog(), null, table_name),		//tmpfk => Foreign Keys (Keys, which are bond to other tables)
						tmpat = tmp.getColumns(null, null, table_name, null);					//attributes 

				ArrayList<String> all_Keys = new ArrayList<>();									//for each table, a own ArrayList is being used
				while(tmppk.next()) {															//walking through the PK's
					boolean isMul = true;
					while(tmpfk.next()) {														//walking through the FK's
						String name = tmpfk.getString("FKCOLUMN_NAME");							//Here we get all the column-names, where a FK is implemented
						if(tmppk.getString(4).equals(name)) {									//If actual PK is also a FK
							isMul = false;	
							String fk_table = tmpfk.getString("FKTABLE_NAME");			
							all_Keys.add("<<PK>><<FK>>" + fk_table + "." + name);				//Marking the data with both PK and FK
						}
					}
					if(isMul)
						
						all_Keys.add("<<PK>>" + tmppk.getString(4));							//if it is just a PK
				}

				tmpfk = tmp.getImportedKeys(con.getCatalog(), null, table_name);				//getting all the FK's from the speific table
				while(tmpfk.next()) {
					String fk_name = tmpfk.getString("FKCOLUMN_NAME"),
							fk_table = tmpfk.getString("FKTABLE_NAME");
					boolean isJustFK = true;
					for(int i = 0;i < all_Keys.size(); ++i) {
						if(all_Keys.get(i).contains(fk_name)) {									//if the specific key contains FKCOLUMN_NAME, it's aleady marked from
							isJustFK = false;													//the upper mechanism as PK&FK, so it can't be declared twice as FK
							break;
						}
					}
					if(isJustFK)
						all_Keys.add("<<FK>>" + fk_table + "." + fk_name);						//an ordinary FK
				}

				while(tmpat.next()) {
					String key_name = tmpat.getString("COLUMN_NAME");
					boolean isIncl = true;
					for(int i = 0;i < all_Keys.size(); ++i) {
						if(all_Keys.get(i).contains(key_name)) {
							isIncl = false;
							break;
						}
					}
					if(isIncl)
						all_Keys.add(key_name);
				}

				table_keys.put(table_name, all_Keys);
				tables.add(table_name);
			}

		} catch (SQLException e) {
			System.err.println("Connection closed (" + e.getMessage() + "). Restart Program");
		}
		

	}
	/**
	 * BEFORE you call this method, you may call refresh()-method.<br/>
	 * Prints all Tables and its PK, FK and Attributes to a given PrintWriter and flushes him.
	 * @param pw PrintWriter to be written to
	 */
	public void printAttr(PrintWriter pw) {
		for(int i = 0;i < tables.size(); ++i) {
			String table_name = tables.get(i);
			pw.print(table_name + "(");
			ArrayList<String> tmp = table_keys.get(table_name);
			for(int j = 0; j < tmp.size()-1; ++j)
				pw.print(tmp.get(j) + ", ");
			pw.println(tmp.get(tmp.size()-1) + ")");
		}
		pw.flush();
	}
	public Connection getCon() {
		return con;
	}
	public Statement getSt() {
		return st;
	}
}