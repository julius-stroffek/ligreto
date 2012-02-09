/**
 * 
 */
package net.ligreto.junit.tests.func;


import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import net.ligreto.exceptions.CollationException;
import net.ligreto.exceptions.DuplicateKeyValuesException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.LigretoExecutor;
import net.ligreto.junit.util.XSSFWorkbookComparator;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.LigretoNode;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
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
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Connection cnn1 = DriverManager.getConnection("jdbc:derby:db1");
		Connection cnn2 = DriverManager.getConnection("jdbc:derby:db2");
		Connection cnn3 = DriverManager.getConnection("jdbc:derby:db3");
		cnn1.setAutoCommit(true);
		cnn2.setAutoCommit(true);
		Statement stm1 = cnn1.createStatement();
		Statement stm2 = cnn2.createStatement();
		Statement stm3 = cnn3.createStatement();
		try {
			stm1.execute("drop table join_table1");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm1.execute("drop table multi_join1");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm2.execute("drop table join_table2");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm2.execute("drop table multi_join2");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm3.execute("drop table coll_table");
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
		
		stm1.execute("create table multi_join1 (Id int, Id2 int, Id3 varchar(32), first_name varchar(32), last_name varchar(32), age int)");
		stm2.execute("create table multi_join2 (Id int, Id2 int, Id3 varchar(32), first_name varchar(32), last_name varchar(32), age int)");
		stm1.execute("insert into multi_join1 values (1, 11, '12', '1Martin1', '1Velky1', 11)");
		stm2.execute("insert into multi_join2 values (1, 11, '12', '2Bruce1', '2Abone1', 21)");
		stm1.execute("insert into multi_join1 values (2, null, '12', 'middle1', 'null', 11)");
		stm2.execute("insert into multi_join2 values (2, 11, '12', 'middle1', 'null', 21)");
		stm1.execute("insert into multi_join1 values (3, 11, '12', 'middle2', 'null', 11)");
		stm2.execute("insert into multi_join2 values (3, null, '12', 'middle2', 'null', 21)");
		stm1.execute("insert into multi_join1 values (4, 11, null, 'last1', 'null', 11)");
		stm2.execute("insert into multi_join2 values (4, 11, '12', 'last1', 'null', 21)");
		stm1.execute("insert into multi_join1 values (5, 11, '12', 'last2', 'null', 11)");
		stm2.execute("insert into multi_join2 values (5, 11, null, 'last2', 'null', 21)");
		stm1.execute("insert into multi_join1 values (6, null, null, 'match1', 'null', 11)");
		stm2.execute("insert into multi_join2 values (6, null, null, 'match1', 'null', 21)");
		stm1.execute("insert into multi_join1 values (7, 11, null, 'match2', 'null', 11)");
		stm2.execute("insert into multi_join2 values (7, 11, null, 'match2', 'null', 21)");

		stm3.execute("create table coll_table (Id varchar(32), first_name varchar(32), last_name varchar(32), age int)");
		stm3.execute("insert into coll_table values ('abcd', '1Martin1', '1Velky1', 11)");
		stm3.execute("insert into coll_table values ('bcde', '1Martin2', '1Velky2', 12)");
		stm3.execute("insert into coll_table values ('cdef', '1Martin3', '1Velky3', 13)");
		stm3.execute("insert into coll_table values ('defg', '1Martin4', '1Velky4', 14)");
		stm3.execute("insert into coll_table values ('efgh', '1Martin5', '1Velky5', 15)");
		stm3.execute("insert into coll_table values ('fghc', '1Martin6', '1Velky6', 16)");
		stm3.execute("insert into coll_table values ('ghch', '1Martin7', '1Velky7', 17)");
		stm3.execute("insert into coll_table values ('hchi', '1Martin8', '1Velky8', 18)");
		stm3.execute("insert into coll_table values ('chij', '1Martin9', '1Velky9', 19)");

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
	public void testJoinReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("joinreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		
		executor.execute();
		
		Assert.assertTrue(new XSSFWorkbookComparator(
				new XSSFWorkbook(new FileInputStream("joinreport.xlsx")),
				new XSSFWorkbook(new FileInputStream("desired/joinreport.xlsx"))
		).areSame());
		Assert.assertTrue(new XSSFWorkbookComparator(
				new XSSFWorkbook(new FileInputStream("streamjoinreport.xlsx")),
				new XSSFWorkbook(new FileInputStream("desired/streamjoinreport.xlsx"))
		).areSame());
	}
	
	@Test
	public void testDetailedJoinReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("detailedreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		
		executor.execute();
		
		Assert.assertTrue(new XSSFWorkbookComparator(
				new XSSFWorkbook(new FileInputStream("detailedreport.xlsx")),
				new XSSFWorkbook(new FileInputStream("desired/detailedreport.xlsx"))
		).areSame());
		Assert.assertTrue(new XSSFWorkbookComparator(
				new XSSFWorkbook(new FileInputStream("streamdetailedreport.xlsx")),
				new XSSFWorkbook(new FileInputStream("desired/streamdetailedreport.xlsx"))
		).areSame());
	}
	
	@Test
	public void testDuplicateJoinColumns() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("duplicatejoincolumnsreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		
		boolean exceptionThrown = false;
		try {
			executor.execute();
		} catch (LigretoException e) {
			Throwable c1 = e.getCause();
			Throwable c2 = c1.getCause();
			
			// Check that we got the right exception with the proper cause
			if (c2 instanceof DuplicateKeyValuesException) {
				exceptionThrown = true;
			} else {
				throw e;
			}
		}
		Assert.assertTrue(exceptionThrown);
	}
	
	@Test
	public void testWrongCollation() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("wrongcollationreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		
		boolean exceptionThrown = false;
		try {
			executor.execute();
		} catch (LigretoException e) {
			Throwable c1 = e.getCause();
			Throwable c2 = c1.getCause();
			
			// Check that we got the right exception with the proper cause
			if (c2 instanceof CollationException) {
				exceptionThrown = true;
			} else {
				throw e;
			}
		}
		Assert.assertTrue(exceptionThrown);
	}
	
	@Test
	public void testWrongCollationDump() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("wrongcollationdumpreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		
		executor.execute();
		
		Assert.assertTrue(new XSSFWorkbookComparator(
				new XSSFWorkbook(new FileInputStream("wrongcollationdumpreport.xlsx")),
				new XSSFWorkbook(new FileInputStream("desired/wrongcollationdumpreport.xlsx"))
		).areSame());
	}
}
