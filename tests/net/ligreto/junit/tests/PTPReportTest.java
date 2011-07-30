package net.ligreto.junit.tests;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import net.ligreto.LigretoExecutor;
import net.ligreto.config.Parser;
import net.ligreto.config.nodes.LigretoNode;
import net.ligreto.exceptions.LigretoException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;


public class PTPReportTest {
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
		stm.execute("insert into test_table values (1, 'Martin', 'Velky', 52)");
		stm.execute("insert into test_table values (1, 'Bruce', 'Abone', 13)");
		cnn.close();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSqlReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("ptpreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		executor.execute();
	}

}