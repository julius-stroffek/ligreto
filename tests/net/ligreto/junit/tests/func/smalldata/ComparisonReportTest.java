/**
 * 
 */
package net.ligreto.junit.tests.func.smalldata;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import net.ligreto.Database.ConnectionResolver;
import net.ligreto.exceptions.CollationException;
import net.ligreto.exceptions.DataSourceException;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.exceptions.DuplicateKeyValuesException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.LigretoExecutor;
import net.ligreto.junit.util.TestUtil;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.DataSourceNode;
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
	public void testConnectionResolver() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		String reportName = "joinreport";
		LigretoNode ligreto = Parser.parse(reportName + ".xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		ligreto.setConnectionResolver(new ConnectionResolver() {

			@Override
			public Connection getConnection(String name) throws DataSourceException, ClassNotFoundException, SQLException {
				if ("Source1".equals(name)) {
					Connection cnn = DriverManager.getConnection("jdbc:derby:db1");
					return cnn;
				}
				throw new DataSourceNotDefinedException();
			}

			@Override
			public DataSourceNode getDataSourceNode(String name) throws DataSourceNotDefinedException {
				if ("Source1".equals(name)) {
					DataSourceNode node = new DataSourceNode(null, "Source1");
					return node;
				}
				throw new DataSourceNotDefinedException();
			}
			
		});
		executor.execute();
		TestUtil.compareReport(reportName, reportName);
	}
	
	@Test
	public void testWrongCollationInComparisonDump() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("wrongcollationdumpreport", true);
	}
}
