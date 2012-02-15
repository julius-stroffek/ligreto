package net.ligreto.junit.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import net.ligreto.ResultStatus;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.LigretoExecutor;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.LigretoNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.xml.sax.SAXException;

/**
 * This class provides various common functions related to testing
 * that could be used across all the tests. For example database
 * creation, etc.
 * 
 * @author Julius Stroffek
 *
 */
public class TestUtil {

	/** The logger instance for the class. */
	private static Log log = LogFactory.getLog(TestUtil.class);

	/** This function will create the databases used across all the tests. 
	 * @throws ClassNotFoundException 
	 * @throws SQLException */
	public static void createDBs() throws ClassNotFoundException, SQLException {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Properties properties = new Properties();
		properties.setProperty("create", "true");
		Connection cnn = DriverManager.getConnection("jdbc:derby:db1", properties);
		cnn.close();
		cnn = DriverManager.getConnection("jdbc:derby:db2", properties);
		cnn.close();
	}
	
	/** Stores the performance testing results into the target DB for further reference. */
	public static void storePerfResults(String operation, long amount, long millis) {
		log.info("Operation: " + operation + " on " + amount + " records took " + millis/1000 + " seconds.");
	}
	
	/** Log the performance results into the log file. */
	public static void logPerfResults(String operation, long amount, long millis) {
		log.info("Operation: " + operation + " on " + amount + " records took " + millis/1000 + " seconds.");
	}
	
	/**
	 * This function will invoke the report generation according the convention in the tests.
	 * 
	 * @param reportName The name of the report without '.xml' and '.xlsx' extensions.
	 * @return the result status of the report generation
	 * @throws SAXException
	 * @throws IOException
	 * @throws LigretoException
	 */
	public static ResultStatus generateReport(String reportName) throws SAXException, IOException, LigretoException {
		LigretoNode ligreto = Parser.parse(reportName + ".xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		ResultStatus resultStatus = executor.execute();
		
		return resultStatus;
	}

	/**
	 * This file will compare the generated report with the same file in the 'desired' report directory.
	 * 
	 * @param reportName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void compareReport(String reportName, String desiredReportName) throws FileNotFoundException, IOException {
		Assert.assertTrue(new XSSFWorkbookComparator(
				new XSSFWorkbook(new FileInputStream(reportName + ".xlsx")),
				new XSSFWorkbook(new FileInputStream("desired/" + desiredReportName + ".xlsx"))
		).areSame());						
	}
	
	/**
	 * This method will generate a report file based on the testing convention. It will check the acceptance
	 * in the result status structure. It will finally compare the generated file with the file in
	 * the 'desired' report directory.
	 * 
	 * @param reportName
	 * @param desiredReportName
	 * @param accepted
	 * @throws SAXException
	 * @throws IOException
	 * @throws LigretoException
	 */
	public static void testReport(String reportName, String desiredReportName, boolean accepted) throws SAXException, IOException, LigretoException {
		ResultStatus resultStatus = generateReport(reportName);
		Assert.assertTrue(resultStatus.isAccepted() == accepted);
		compareReport(reportName, desiredReportName);
	}

	public static void testReport(String reportName, boolean accepted) throws SAXException, IOException, LigretoException {
		testReport(reportName, reportName, accepted);
	}

	/**
	 * This method will generate a report file based on the testing convention. It will check the acceptance
	 * in the result status structure (have to be accepted). It will finally compare the generated file with
	 * the file in the 'desired' report directory.
	 * 
	 * @param reportName
	 * @throws SAXException
	 * @throws IOException
	 * @throws LigretoException
	 */
	public static void testReport(String reportName) throws SAXException, IOException, LigretoException {
		testReport(reportName, true);
	}
}
