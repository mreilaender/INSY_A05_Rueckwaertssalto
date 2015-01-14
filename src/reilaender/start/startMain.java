package reilaender.start;

import java.sql.SQLException;

import reilaender.connector.Connector;
import reilaender.parser.Parser;

public class startMain {

	public static void main(String[] args) {
		Parser parser = new Parser();
			parser.fillOptions();
			parser.createParser(args);
			parser.retrieveArgs();
			
		Connector connector = new Connector(parser);
			try {
				connector.connect();
			} catch (SQLException e) {
				// TODO
				System.err.println("TODO Unhandled Exception in " + new startMain().getClass().toString());
				e.printStackTrace();
			}
	}
}