package net.ligreto.junit.tests.func.largedata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Properties;

import net.ligreto.builders.ExcelStreamReportBuilder;
import net.ligreto.junit.util.TestUtil;

import org.junit.Assert;
import org.junit.Test;

public class PrepareTestData {
	/** The number of rows to be tested. */
	public static final long rowCount = 10000;
	
	/** The number of rows to be tested. */
	public static final long commitInterval = 1000;

	/**
	 * @throws java.lang.Exception
	 */
	@Test
	public void prepareTestData() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Properties properties = new Properties();
		properties.setProperty("create", "true");
		Connection cnn = DriverManager.getConnection("jdbc:derby:db1", properties);
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
}
