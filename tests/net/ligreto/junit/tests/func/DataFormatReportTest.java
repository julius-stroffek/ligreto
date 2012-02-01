package net.ligreto.junit.tests.func;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;

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


public class DataFormatReportTest {
	@BeforeClass
	public static void setUp() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Connection cnn = DriverManager.getConnection("jdbc:derby:db1");
		cnn.setAutoCommit(true);
		Statement stm = cnn.createStatement();
		try {
			stm.execute("drop table date_time_test");
		} catch (SQLException e) {
			// do nothing
		}
		stm.execute("create table data_test (Id int, full_stamp timestamp, date_only date, time_only time, num_int int, num_float float, num_decimal decimal(15,5))");
		PreparedStatement pstm = cnn.prepareStatement("insert into data_test values (?, ?, ?, ?, ?, ?, ?)");
		for (short s=0; s < 20; s++) {
			pstm.setShort(1, s);
			
			@SuppressWarnings("deprecation")
			Timestamp stamp = new Timestamp(111, 11, 20, 11, s, s, s);
			pstm.setTimestamp(2, stamp);
			
			@SuppressWarnings("deprecation")
			Date date = new Date(111, 10, s+1);
			pstm.setDate(3, date);
			
			@SuppressWarnings("deprecation")
			Time time = new Time(s,s,s);
			pstm.setTime(4, time);
			
			pstm.setInt(5, 2*s);
			pstm.setDouble(6, s*s);
			pstm.setBigDecimal(7, new BigDecimal(s*s+s));
			pstm.execute();
		}
		cnn.close();
	}

	@AfterClass
	public static void tearDown() throws Exception {
	}

	@Test
	public void testDataFormatReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("dataformatreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		executor.execute();
		Assert.assertTrue(new XSSFWorkbookComparator(
				new XSSFWorkbook(new FileInputStream("dataformatreport.xlsx")),
				new XSSFWorkbook(new FileInputStream("desired/dataformatreport.xlsx"))
		).areSame());
	}
	
	@Test
	public void testNoDataFormatReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("nodataformatreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		executor.execute();
		Assert.assertTrue(new XSSFWorkbookComparator(
				new XSSFWorkbook(new FileInputStream("nodataformatreport.xlsx")),
				new XSSFWorkbook(new FileInputStream("desired/nodataformatreport.xlsx"))
		).areSame());
	}
}
