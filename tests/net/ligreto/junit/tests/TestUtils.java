package net.ligreto.junit.tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides various common functions related to testing
 * that could be used across all the tests. For example database
 * creation, etc.
 * 
 * @author Julius Stroffek
 *
 */
public class TestUtils {

	/** The logger instance for the class. */
	private static Log log = LogFactory.getLog(TestUtils.class);

	/** This function will create the databases used across all the tests. 
	 * @throws ClassNotFoundException 
	 * @throws SQLException */
	public static void createDBs() throws ClassNotFoundException, SQLException {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Properties properties = new Properties();
		properties.setProperty("create", "true");
		Connection cnn = DriverManager.getConnection("jdbc:derby:db1", properties);
		cnn.close();
		cnn = DriverManager.getConnection("jdbc:derby:db2", properties);
		cnn.close();
	}
	
	/** Stores the performance testing results into the target DB for further reference. */
	public static void storePerfResults(String operation, long amount, long millis) {
		log.info("Operation: " + operation + " on " + amount + " records took " + millis/1000 + " seconds.");
	}
}
