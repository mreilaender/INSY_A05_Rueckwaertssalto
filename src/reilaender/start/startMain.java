package reilaender.start;

import java.io.PrintWriter;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import reilaender.connector.Connector;
import reilaender.parser.Parser;

public class startMain {

	private static String todo = "(TODO) Unhandled Exception in " + new startMain().getClass().toString();
	private static DatabaseMetaData meta;
	public static void main(String[] args) {
		Parser parser = new Parser();
		parser.fillOptions();
		parser.createParser(args);
		parser.retrieveArgs();

		Connector connector = new Connector(parser);
		try {
			connector.connect();
			connector.refresh();
			connector.printAttr(new PrintWriter(System.out));
		} catch (SQLException e) {
			// TODO
			System.err.println(todo);
			e.printStackTrace();
		}
//		try {
//			meta = connector.getCon().getMetaData();
//			ResultSet tmp = connector.executeQuery("SHOW TABLES");
//			System.out.println("\n\n" + tmp.getString(1) + "\n\n\n");
//			while(tmp.next()) {
//				System.out.println("-- Table " + tmp.getString(1) + " --");
//				ResultSet metat = meta.getPrimaryKeys(null, null, tmp.getString(1));
//				
//				metat.next();
//				for(int j = 1;j < 10;++j) {
//					try {
//						System.out.print(metat.getString(j) + " ; ");
//					} catch(SQLException e) {
//						System.err.print("Out of Range ; ");
//					} catch(NullPointerException e) {
//						System.err.print("Null " + " ; ");
//					}
//				}
//				System.out.println();
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			//			System.err.println(todo);
//			e.printStackTrace();
//		}
	}
}