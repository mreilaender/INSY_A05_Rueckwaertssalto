package reilaender.start;

import java.io.FileNotFoundException;
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
			connector.printAttr(new PrintWriter("Relationenmodell.txt"));
		} catch (SQLException e) {
			// TODO
			System.err.println(todo);
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO
			System.err.println(todo);
			e.printStackTrace();
		}
	}
}