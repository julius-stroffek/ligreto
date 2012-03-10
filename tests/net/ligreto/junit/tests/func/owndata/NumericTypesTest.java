/**
 * 
 */
package net.ligreto.junit.tests.func.owndata;


import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
public class NumericTypesTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Connection cnn1 = DriverManager.getConnection("jdbc:derby:db1");
		Connection cnn2 = DriverManager.getConnection("jdbc:derby:db2");
		cnn1.setAutoCommit(true);
		cnn2.setAutoCommit(true);
		Statement stm1 = cnn1.createStatement();
		Statement stm2 = cnn2.createStatement();
		try {
			stm1.execute("drop table num_table1");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm2.execute("drop table num_table2");
		} catch (SQLException e) {
			// do nothing
		}
		stm1.execute("create table num_table1 (Id int, etype char(1), first_name varchar(32), num_int int, num_float float, num_double double, num_decimal1 decimal(31,11), num_decimal2 decimal(31,0), num_decimal3 decimal(31,31))");
		stm2.execute("create table num_table2 (Id int, etype char(1), first_name varchar(32), num_int int, num_float float, num_double double, num_decimal1 decimal(31,11), num_decimal2 decimal(31,0), num_decimal3 decimal(31,31))");
		stm1.execute("insert into num_table1 values (1, 'M', 'Martin', 65537, 0.00012345, 0.0001234567, 12345678901234567890.12345678901, 1234567890123456789012345678901, 0.1234567890123456789012345678901)");
		stm2.execute("insert into num_table2 values (1, 'M', 'Martin', 65537, 0.00012345, 0.0001234567, 12345678901234567890.12345678901, 1234567890123456789012345678901, 0.1234567890123456789012345678901)");
		stm1.execute("insert into num_table1 values (2, 'Z', 'Zdenek', -65537, -0.00012345, -0.0001234567, -12345678901234567890.12345678901, -1234567890123456789012345678901, -0.1234567890123456789012345678901)");
		stm2.execute("insert into num_table2 values (2, 'Z', 'Zdenek', 65537, 0.00012345, 0.0001234567, 12345678901234567890.12345678901, 1234567890123456789012345678901, 0.1234567890123456789012345678901)");
		stm1.execute("insert into num_table1 values (3, 'M', 'Karel', 65537, 0.00012345, 0.0001234567, 12345678901234567890.12345678901, 1234567890123456789012345678901, 0.1234567890123456789012345678901)");
		stm2.execute("insert into num_table2 values (3, 'M', 'Karel', -65537, -0.00012345, -0.0001234567, -12345678901234567890.12345678901, -1234567890123456789012345678901, -0.1234567890123456789012345678901)");
		stm1.execute("insert into num_table1 values (4, 'Z', 'Miro', 65537, 0.00012345, 0.0001234567, 12345678901234567890.12345678901, 1234567890123456789012345678901, 0.1234567890123456789012345678901)");
		stm2.execute("insert into num_table2 values (5, 'M', 'Stefan', -65537, -0.00012345, -0.0001234567, -12345678901234567890.12345678901, -1234567890123456789012345678901, -0.1234567890123456789012345678901)");
		stm1.execute("insert into num_table1 values (6, 'M', 'Jarek', 65500, 0.000123, 0.0001234567, 1234567890123456789.12345678901, 1234567890123456789012345678901, 0.1234567890123456789012345678901)");
		stm2.execute("insert into num_table2 values (6, 'Z', 'Jarek', 65537, 0.00012345, 0.0002234567, 12345678901234567890.12345678901, 123456789023456789012345678901, 0.134567890123456789012345678901)");

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
	public void testNumericTypesInJoin() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("numericjoinreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		
		executor.execute();
				
		Assert.assertTrue(new XSSFWorkbookComparator(
				new XSSFWorkbook(new FileInputStream("numericjoinreport.xlsx")),
				new XSSFWorkbook(new FileInputStream("desired/numericjoinreport.xlsx"))
		).areSame());
	}
	
	@Test
	public void testNumericTypesInAggregatedJoin() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("numericaggregatedreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		
		executor.execute();
		
		Assert.assertTrue(new XSSFWorkbookComparator(
				new XSSFWorkbook(new FileInputStream("numericaggregatedreport.xlsx")),
				new XSSFWorkbook(new FileInputStream("desired/numericaggregatedreport.xlsx"))
		).areSame());
	}
}
