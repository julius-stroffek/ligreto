package net.ligreto.junit.tests.orcl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	OraCollatorTest.class
})
public class AllTests {
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Properties properties = new Properties();
		properties.setProperty("create", "true");
		Connection cnn = DriverManager.getConnection("jdbc:derby:db1", properties);
		cnn.close();
		cnn = DriverManager.getConnection("jdbc:derby:db2", properties);
		cnn.close();
		cnn = DriverManager.getConnection("jdbc:derby:db3", properties);
		cnn.close();
		/*
		cnn = DriverManager.getConnection("jdbc:derby:db4", properties);
		cnn.close();
		*/
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDown() throws Exception {
	}	
}
