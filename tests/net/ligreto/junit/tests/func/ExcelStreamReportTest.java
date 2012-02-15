package net.ligreto.junit.tests.func;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import net.ligreto.builders.ExcelStreamReportBuilder;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.LigretoExecutor;
import net.ligreto.junit.util.TestUtil;
import net.ligreto.junit.util.XSSFWorkbookComparator;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.LigretoNode;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;


public class ExcelStreamReportTest {
	/** The number of rows to be tested. */
	public static final long rowCount = 10000;
	
	/** The number of rows to be tested. */
	public static final long commitInterval = 1000;

	@BeforeClass
	public static void setUp() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Connection cnn = DriverManager.getConnection("jdbc:derby:db1");
		cnn.setAutoCommit(true);
		Statement stm = cnn.createStatement();
		try {
			stm.execute("drop table large_table");
		} catch (SQLException e) {
			// do nothing
		}
		stm.execute("create table large_table (Id int, stamp timestamp, first_name varchar(32), last_name varchar(32), age int)");
		PreparedStatement pstm = cnn.prepareStatement("insert into large_table values (?, ?, ?, ?, ?)");
		cnn.setAutoCommit(false);
		
		// The number of rows processed here have to be greater than the number of rows kept in memory
		Assert.assertTrue(rowCount > ExcelStreamReportBuilder.FLUSH_ROW_INTERVAL);

		long startStamp = System.currentTimeMillis();
		for (long l=0; l < rowCount; l++) {
			pstm.setLong(1, l);
			
			@SuppressWarnings("deprecation")
			Timestamp stamp = new Timestamp(111, 11, 20, 11, (int)l%24, (int)l%60, (int)l%60);
			
			pstm.setTimestamp(2, stamp);
			pstm.setString(3, "FirstName" + l);
			pstm.setString(4, "LastName" + l);
			pstm.setInt(5, (int) (l % 120));
			pstm.execute();
			if (l % commitInterval == 0)
				cnn.commit();
		}
		cnn.commit();
		long endStamp = System.currentTimeMillis();
		TestUtil.logPerfResults("insert (commit 1)", rowCount, endStamp - startStamp);
		pstm.close();
		stm.close();
		cnn.close();
	}

	@AfterClass
	public static void tearDown() throws Exception {
	}

	@Test
	public void testExcelStreamReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("excelstreamreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		executor.execute();
		Assert.assertTrue(new XSSFWorkbookComparator(
				new XSSFWorkbook(new FileInputStream("excelstreamreport.xlsx")),
				new XSSFWorkbook(new FileInputStream("desired/excelstreamreport.xlsx"))
		).areSame());
	}
}
