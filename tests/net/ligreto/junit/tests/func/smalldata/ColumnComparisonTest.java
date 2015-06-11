package net.ligreto.junit.tests.func.smalldata;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.LigretoExecutor;
import net.ligreto.junit.util.TestUtil;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.LigretoNode;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public class ColumnComparisonTest {
	@Test
	public void testColumnComparison() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("columnscomparisonreport", true);
	}

	@Test
	public void testColumnComparisonHtml() throws SAXException, IOException, LigretoException {
		TestUtil.testHtmlReport("columnscomparisonhtmlreport", true);
	}

	@Test
	public void testColumnComparisonFailure() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("columnscomparisonfailure.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);

		boolean exceptionThrown = false;
		try {
			executor.execute();
		} catch (LigretoException e) {
			Throwable c1 = e.getCause();
			Throwable c2 = c1.getCause();
			
			// Check that we got the right exception with the proper cause
			if (c2 instanceof LigretoException) {
				exceptionThrown = true;
			} else {
				throw e;
			}
		}
		Assert.assertTrue(exceptionThrown);
	}
}
