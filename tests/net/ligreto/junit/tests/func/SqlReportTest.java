package net.ligreto.junit.tests.func;

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


public class SqlReportTest {
	@BeforeClass
	public static void setUp() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Connection cnn = DriverManager.getConnection("jdbc:derby:db1");
		cnn.setAutoCommit(true);
		Statement stm = cnn.createStatement();
		try {
			stm.execute("drop table test_table");
		} catch (SQLException e) {
			// do nothing
		}
		stm.execute("create table test_table (Id int, first_name varchar(32), last_name varchar(32), age int)");
		stm.execute("insert into test_table values (1, 'Martin', 'Velky', 52)");
		stm.execute("insert into test_table values (1, 'Bruce', 'Abone', 13)");
		cnn.close();
	}

	@AfterClass
	public static void tearDown() throws Exception {
	}

	@Test
	public void testSqlReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("sqlreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		executor.execute();
		Assert.assertTrue(new XSSFWorkbookComparator(
				new XSSFWorkbook(new FileInputStream("sqlreport.xlsx")),
				new XSSFWorkbook(new FileInputStream("desired/sqlreport.xlsx"))
		).areSame());
	}
}
