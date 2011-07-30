/**
 * 
 */
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
		Connection cnn1 = DriverManager.getConnection("jdbc:derby:db1");
		Connection cnn2 = DriverManager.getConnection("jdbc:derby:db2");
		cnn1.setAutoCommit(true);
		cnn2.setAutoCommit(true);
		Statement stm1 = cnn1.createStatement();
		Statement stm2 = cnn2.createStatement();
		try {
			stm1.execute("drop table join_table1");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm2.execute("drop table join_table2");
		} catch (SQLException e) {
			// do nothing
		}
		stm1.execute("create table join_table1 (Id int, first_name varchar(32), last_name varchar(32), age int)");
		stm2.execute("create table join_table2 (Id int, first_name varchar(32), last_name varchar(32), age int)");
		stm1.execute("insert into join_table1 values (1, '1Martin1', '1Velky1', 11)");
		stm2.execute("insert into join_table2 values (1, '2Bruce1', '2Abone1', 21)");
		stm1.execute("insert into join_table1 values (2, '1Martin2', '1Velky2', 12)");
		stm2.execute("insert into join_table2 values (2, '2Bruce2', '2Abone2', 22)");
		stm1.execute("insert into join_table1 values (3, '1Martin3', '1Velky3', 13)");
		stm2.execute("insert into join_table2 values (4, '2Bruce4', '2Abone4', 24)");
		stm1.execute("insert into join_table1 values (5, '1Martin5', '1Velky5', 15)");
		stm2.execute("insert into join_table2 values (6, '2Bruce6', '2Abone6', 26)");
		stm1.execute("insert into join_table1 values (7, 'Martin7', 'Velky7', 77)");
		stm2.execute("insert into join_table2 values (7, 'Martin7', 'Velky7', 77)");
		stm1.execute("insert into join_table1 values (8, 'Bruce8', 'Abone8', 15)");
		stm2.execute("insert into join_table2 values (8, 'Bruce8', 'Abone8', 88)");
		cnn1.close();
		cnn2.close();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testJoinReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("joinreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		executor.executeReports();
	}
}
