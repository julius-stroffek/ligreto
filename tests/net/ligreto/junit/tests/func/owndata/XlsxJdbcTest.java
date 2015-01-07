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

public class XlsxJdbcTest {
	@BeforeClass
	public static void setUp() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Properties props = new Properties();
		props.setProperty("create", "true");
		Connection cnn = DriverManager.getConnection("jdbc:derby:db4ptp", props);
		cnn.setAutoCommit(true);
		Statement stm = cnn.createStatement();
		try {
			stm.execute("drop table ptp_table");
		} catch (SQLException e) {
			// do nothing
		}
		stm.execute("create table ptp_table (Id int, first_name varchar(32), last_name varchar(32), age int)");
		stm.execute("insert into ptp_table values (1, '2Spring', '3Big', 12)");
		stm.execute("insert into ptp_table values (2, '4Summer', '5Brother', 23)");
		stm.execute("insert into ptp_table values (3, '6Autumn', '7Winter', 34)");
		stm.close();
		cnn.close();
	}

	@AfterClass
	public static void tearDown() throws Exception {
	}

	@Test
	public void testXlsxJdbc() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("xlsxjdbc");
	}
	
	@Test
	public void testXlsxJdbcPtp() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("xlsxjdbcptp");
	}	
}
