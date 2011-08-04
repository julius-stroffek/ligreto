package net.ligreto.junit.tests.func;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.LigretoExecutor;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.LigretoNode;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;


public class PtpReportTest {
	@BeforeClass
	public void setUpBeforeClass() throws Exception {
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
	}

	@AfterClass
	public void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testPtpReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("ptpreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		executor.execute();
	}

}
