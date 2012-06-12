/**
 * 
 */
package net.ligreto.junit.tests.orcl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import net.ligreto.exceptions.CollationException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.LigretoExecutor;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.LigretoNode;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Julius Stroffek
 *
 */
public class OraCollatorTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Properties properties = new Properties();
		properties.setProperty("create", "true");
		properties.setProperty("territory", "en_US");		
		properties.setProperty("collation", "TERRITORY_BASED");		
		Connection cnn1 = DriverManager.getConnection("jdbc:derby:dbo1", properties);
		Connection cnn2 = DriverManager.getConnection("jdbc:derby:dbo2", properties);
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
		stm1.execute("create table join_table1 (Id char(6), first_name varchar(32), last_name varchar(32), age int)");
		stm2.execute("create table join_table2 (Id char(6), first_name varchar(32), last_name varchar(32), age int)");
		stm1.execute("insert into join_table1 values ('ABCD', '1Martin1', '1Velky1', 11)");
		stm2.execute("insert into join_table2 values ('ABCD', '2Bruce1', '2Abone1', 21)");
		stm1.execute("insert into join_table1 values ('A01D', '1Martin2', '1Velky2', 12)");
		stm2.execute("insert into join_table2 values ('A01D', '2Bruce2', '2Abone2', 22)");
		stm1.execute("insert into join_table1 values ('ACCD', '1Martin3', '1Velky3', 13)");
		stm2.execute("insert into join_table2 values ('ACCD', '2Bruce4', '2Abone4', 24)");
		stm1.execute("insert into join_table1 values ('A_CD', '1Martin5', '1Velky5', 15)");
		stm2.execute("insert into join_table2 values ('A_CD', '2Bruce6', '2Abone6', 26)");
		stm1.execute("insert into join_table1 values ('A CD', 'Martin7', 'Velky7', 77)");
		stm2.execute("insert into join_table2 values ('A CD', 'Martin7', 'Velky7', 77)");
		stm1.execute("insert into join_table1 values ('A0CD', 'Bruce8', 'Abone8', 15)");
		stm2.execute("insert into join_table2 values ('A0CD', 'Bruce8', 'Abone8', 88)");

		cnn1.close();
		cnn2.close();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Test the comparison that does not work for GENERIC_M collation.
	 * 
	 * @param sortingRule the sorting rule to use, e.g. BINARY, GENERIC_M, CZECH
	 */
	public void testComparator(String sortingRule) {
		oracle.i18n.text.OraCollator collator = oracle.i18n.text.OraCollator.getInstance(sortingRule);
		
		int result1a = collator.compare("04.04.1973", "04041973/9999");
		int result1b = collator.compare("04041973/9999", "04.04.1973");
		Assert.assertTrue(result1a != 0 && result1b != 0);
		Assert.assertTrue(result1a != result1b);
		
		int result2a = collator.compare("04041973", "04041973/9999");
		int result2b = collator.compare("04041973/9999", "04041973");
		Assert.assertTrue(result2a != 0 && result2b != 0);
		Assert.assertTrue(result2a != result2b);
		
		int result3a = collator.compare("04.04.1973", "04041973");
		int result3b = collator.compare("04041973", "04.04.1973");
		Assert.assertTrue(result3a != 0 && result3b != 0);
		Assert.assertTrue(result3a != result3b);
	}
	
	@Test
	public void testComparator() {
		testComparator("BINARY");
		testComparator("GENERIC_M");
	}
	
	@Test
	public void testOraCollator() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("oracollatorreport.xml");
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
	public void testOraCollatorLocale() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("oracollatorlocalereport.xml");
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
}
