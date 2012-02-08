/**
 * 
 */
package net.ligreto.junit.tests.perf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import net.ligreto.executor.LigretoExecutor;
import net.ligreto.junit.tests.TestUtils;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.LigretoNode;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Julius Stroffek
 *
 */
public class PtpPerformance {

	/** The number of rows to be tested. */
	public static final long rowCount = 1000000;
	
	/** The number of rows inserted before commit while preparing the test data. */
	public static final long commitInterval = 10000;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Connection cnn = DriverManager.getConnection("jdbc:derby:db1");
		cnn.setAutoCommit(true);
		Statement stm = cnn.createStatement();
		try {
			stm.execute("drop table ptp_perf_table");
		} catch (SQLException e) {
			// do nothing
		}
		stm.execute("create table ptp_perf_table (Id int, stamp timestamp, first_name varchar(32), last_name varchar(32), age int)");
		PreparedStatement pstm = cnn.prepareStatement("insert into ptp_perf_table values (?, ?, ?, ?, ?)");
		cnn.setAutoCommit(false);
		long startStamp = System.currentTimeMillis();
		for (long l=0; l < rowCount; l++) {
			pstm.setLong(1, l);
			pstm.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			pstm.setString(3, "FirstName" + l);
			pstm.setString(4, "LastName" + l);
			pstm.setInt(5, (int) (l % 120));
			pstm.execute();
			if (l % commitInterval == 0)
				cnn.commit();
		}
		cnn.commit();
		long endStamp = System.currentTimeMillis();
		TestUtils.storePerfResults("insert (commit 1)", rowCount, endStamp - startStamp);
		pstm.close();
		stm.close();
		cnn.close();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() throws Exception {
		LigretoNode ligreto = Parser.parse("ptpperf.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		long startStamp = System.currentTimeMillis();
		executor.execute();
		long endStamp = System.currentTimeMillis();
		TestUtils.storePerfResults("transfer (commit 1)", rowCount, endStamp - startStamp);
	}

}
