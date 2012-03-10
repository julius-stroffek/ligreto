/**
 * 
 */
package net.ligreto.junit.tests.func.smalldata;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.junit.util.TestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Julius Stroffek
 *
 */
public class Excel97ReportTest {
	@Before
	public void setUp() {
		System.setProperty("excel97", "yes");
	}
	
	@After
	public void tearDown() {
		System.clearProperty("excel97");
	}
	
	@Test
	public void testExcel97Format() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("excel97report", true);
	}
}
