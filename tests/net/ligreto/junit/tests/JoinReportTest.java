/**
 * 
 */
package net.ligreto.junit.tests;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import net.ligreto.LigretoExecutor;
import net.ligreto.config.Parser;
import net.ligreto.config.nodes.LigretoNode;
import net.ligreto.exceptions.LigretoException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Julius Stroffek
 *
 */
public class JoinReportTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Properties properties = new Properties();
		properties.setProperty("create", "true");
		Connection cnn = DriverManager.getConnection("jdbc:derby:TestRun/test", properties);
		cnn.setAutoCommit(true);
		Statement stm = cnn.createStatement();
		try {
			stm.execute("drop table join_table1");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm.execute("drop table join_table2");
		} catch (SQLException e) {
			// do nothing
		}
		stm.execute("create table join_table1 (Id int, first_name varchar(32), last_name varchar(32), age int)");
		stm.execute("create table join_table2 (Id int, first_name varchar(32), last_name varchar(32), age int)");
		stm.execute("insert into join_table1 values (1, '1Martin1', '1Velky1', 11)");
		stm.execute("insert into join_table2 values (1, '2Bruce1', '2Abone1', 21)");
		stm.execute("insert into join_table1 values (2, '1Martin2', '1Velky2', 12)");
		stm.execute("insert into join_table2 values (2, '2Bruce2', '2Abone2', 22)");
		stm.execute("insert into join_table1 values (3, '1Martin3', '1Velky3', 13)");
		stm.execute("insert into join_table2 values (4, '2Bruce4', '2Abone4', 24)");
		stm.execute("insert into join_table1 values (5, '1Martin5', '1Velky5', 15)");
		stm.execute("insert into join_table2 values (6, '2Bruce6', '2Abone6', 26)");
		cnn.close();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testJoinReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse(ClassLoader.getSystemResource("data/joinreport.xml").toString());
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		executor.executeReports();
	}
}
