package net.ligreto.junit.tests;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.LigretoExecutor;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.LigretoNode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;


public class PtpReportTest {
	@Before
	public void setUp() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Connection cnn = DriverManager.getConnection("jdbc:derby:db1");
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
		
		// Just a temporary workaround for the table to exist
		Connection cnn2 = DriverManager.getConnection("jdbc:derby:db2");
		cnn2.setAutoCommit(true);
		Statement stm2 = cnn2.createStatement();
		try {
			stm2.execute("drop table telt_table");
		} catch (SQLException e) {
			// do nothing
		}
		stm2.execute("create table telt_table (Id int, first_name varchar(32), last_name varchar(32), age int)");
		stm2.close();
		cnn2.close();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPtpReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("ptpreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		executor.execute();
	}

}
