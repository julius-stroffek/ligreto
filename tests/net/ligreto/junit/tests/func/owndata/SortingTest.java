package net.ligreto.junit.tests.func.owndata;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.junit.util.TestUtil;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SortingTest {
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Prepare the properties with 'create=true'
		Properties createProperties = new Properties();
		createProperties.setProperty("create", "true");
		// Get the database connections (create DBs if they do not exist)
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Connection cnn1 = DriverManager.getConnection("jdbc:derby:db1", createProperties);
		Connection cnn2 = DriverManager.getConnection("jdbc:derby:db2", createProperties);
		cnn1.setAutoCommit(true);
		cnn2.setAutoCommit(true);
		Statement stm1 = cnn1.createStatement();
		Statement stm2 = cnn2.createStatement();
		try {
			stm1.execute("drop table sort_table1");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm2.execute("drop table sort_table2");
		} catch (SQLException e) {
			// do nothing
		}
		stm1.execute("create table sort_table1 (Key1 varchar(32), Key2 varchar(32), name varchar(32), value int)");
		stm2.execute("create table sort_table2 (Key1 varchar(32), Key2 varchar(32), name varchar(32), value int)");

		stm1.execute("insert into sort_table1 values ('PSEUDORC', '04041973', 'Klient First', 10)");
		stm1.execute("insert into sort_table1 values ('PSEUDORC', '04.04.1973', 'Klient First', 11)");
		stm1.execute("insert into sort_table1 values ('PSEUDORC', '04041973/9999', 'Klient First', 12)");

		stm2.execute("insert into sort_table2 values ('PSEUDORC', '04041973/9999', 'Klient Second', 20)");
		stm2.execute("insert into sort_table2 values ('PSEUDORC', '04.04.1973', 'Klient Second', 21)");
		stm2.execute("insert into sort_table2 values ('PSEUDORC', '04041973', 'Klient Second', 22)");

		cnn1.close();
		cnn2.close();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testSorting() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("sortingreport", false);
	}
}
