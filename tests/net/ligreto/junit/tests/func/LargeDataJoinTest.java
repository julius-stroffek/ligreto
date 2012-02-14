/**
 * 
 */
package net.ligreto.junit.tests.func;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.junit.tests.TestUtils;
import net.ligreto.junit.tests.util.TestUtil;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Julius Stroffek
 *
 */
public class LargeDataJoinTest {

	/** The number of rows to be tested. */
	public static final long rowCount = 1000;
	
	/** The number of rows to be tested. */
	public static final long commitInterval = 6;

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
			stm1.execute("drop table aggregation_table1");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm2.execute("drop table aggregation_table2");
		} catch (SQLException e) {
			// do nothing
		}
		stm1.execute("create table aggregation_table1 (Id int, stamp timestamp, party_type int, name varchar(32), age int)");
		stm2.execute("create table aggregation_table2 (Id int, stamp timestamp, party_type int, name varchar(32), age int)");
		PreparedStatement pstm1 = cnn1.prepareStatement("insert into aggregation_table1 values (?, ?, ?, ?, ?)");
		PreparedStatement pstm2 = cnn2.prepareStatement("insert into aggregation_table2 values (?, ?, ?, ?, ?)");
		cnn1.setAutoCommit(false);
		cnn2.setAutoCommit(false);
		
		long startStamp = System.currentTimeMillis();
		for (long l=0; l < rowCount; l++) {
			
			if (l % 31 != 0) {
				// Insert data into 1st table
				pstm1.setLong(1, l);
			
				@SuppressWarnings("deprecation")
				Timestamp stamp1 = new Timestamp(111, 11, 20, 11, (int)l%24, (int)l%60, (int)l%60);
			
				pstm1.setTimestamp(2, stamp1);
				pstm1.setInt(3, (int)(l % 5));
				pstm1.setString(4, "LastName" + l);
				pstm1.setInt(5, (int) (l % 120));
				pstm1.execute();
			}
			if (l % commitInterval == 0) {
				cnn1.commit();
			}
			
			if (l % 133 != 0) {
				// Insert data into 2nd table
				pstm2.setLong(1, l);		
			
				@SuppressWarnings("deprecation")
				Timestamp stamp2 = new Timestamp(111, 11, 20, 11, (int)l%24, (int)l%60, (int)l%60);
			
				pstm2.setTimestamp(2, stamp2);
				pstm2.setInt(3, (int)(l % 7));
				pstm2.setString(4, "LastName" + l);
				pstm2.setInt(5, (int) (l % 120));
				pstm2.execute();
			}
			if (l % commitInterval == 0) {
				cnn2.commit();
			}
		}
		cnn1.commit();
		cnn2.commit();
		long endStamp = System.currentTimeMillis();
		TestUtils.logPerfResults("insert (commit 1)", rowCount, endStamp - startStamp);
		pstm1.close();
		pstm2.close();
		stm1.close();
		stm2.close();
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
	public void testAggregatedReport() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("aggregatedreport", true);
	}
	
	@Test
	public void testSummaryReport() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("summaryreport", true);
	}

	@Test
	public void testMultipleLayoutsReport() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("multilayoutreport", true);
	}
	
	@Test
	public void testResultRowLimits() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("result-row-rdc-succ-report", true);		
		TestUtil.testReport("result-row-rdc-fail-report", false);		
		TestUtil.testReport("result-row-adc-succ-report", true);		
		TestUtil.testReport("result-row-adc-fail-report", false);		
		TestUtil.testReport("result-row-rnmc-succ-report", true);		
		TestUtil.testReport("result-row-rnmc-fail-report", false);		
		TestUtil.testReport("result-row-anmc-succ-report", true);		
		TestUtil.testReport("result-row-anmc-fail-report", false);		
	}
}
