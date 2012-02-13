package net.ligreto.junit.tests.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.xml.sax.SAXException;

import net.ligreto.ResultStatus;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.LigretoExecutor;
import net.ligreto.junit.util.XSSFWorkbookComparator;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.LigretoNode;

/**
 * This class provides various testing utility functions.
 * 
 * @author Julius Stroffek
 *
 */
public class TestUtil {
	
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
	public static void compareReport(String reportName) throws FileNotFoundException, IOException {
		Assert.assertTrue(new XSSFWorkbookComparator(
				new XSSFWorkbook(new FileInputStream(reportName + ".xlsx")),
				new XSSFWorkbook(new FileInputStream("desired/" + reportName + ".xlsx"))
		).areSame());						
	}
	
	/**
	 * This method will generate a report file based on the testing convention. It will check the acceptance
	 * in the result status structure. It will finally compare the generated file with the file in
	 * the 'desired' report directory.
	 * 
	 * @param reportName
	 * @param accepted
	 * @throws SAXException
	 * @throws IOException
	 * @throws LigretoException
	 */
	public static void testReport(String reportName, boolean accepted) throws SAXException, IOException, LigretoException {
		ResultStatus resultStatus = generateReport(reportName);
		Assert.assertTrue(resultStatus.isAccepted() == accepted);
		compareReport(reportName);
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
