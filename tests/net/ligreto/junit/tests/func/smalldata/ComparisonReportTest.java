/**
 * 
 */
package net.ligreto.junit.tests.func.smalldata;


import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.exceptions.CollationException;
import net.ligreto.exceptions.DuplicateKeyValuesException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.LigretoExecutor;
import net.ligreto.junit.util.TestUtil;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.LigretoNode;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Julius Stroffek
 *
 */
public class ComparisonReportTest {
	@Test
	public void testComparisionReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("joinreport", false);
	}

	@Test
	public void testStreamComparisionReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("streamjoinreport", false);
	}
	
	@Test
	public void testSheetOrderReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("sheetorderreport", false);
	}

	@Test
	public void testDuplicateKeyColumnsInComparison() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("duplicatejoincolumnsreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		
		boolean exceptionThrown = false;
		try {
			executor.execute();
		} catch (LigretoException e) {
			Throwable c1 = e.getCause();
			Throwable c2 = c1.getCause();
			
			// Check that we got the right exception with the proper cause
			if (c2 instanceof DuplicateKeyValuesException) {
				exceptionThrown = true;
			} else {
				throw e;
			}
		}
		Assert.assertTrue(exceptionThrown);
	}
	
	@Test
	public void testWrongCollationInComparision() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("wrongcollationreport.xml");
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
	public void testWrongCollationInComparisonDump() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("wrongcollationdumpreport", true);
	}
}
