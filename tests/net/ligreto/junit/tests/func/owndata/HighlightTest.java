package net.ligreto.junit.tests.func.owndata;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.junit.util.TestUtil;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Julius Stroffek
 *
 */
public class HighlightTest {
	
	/**
	 * Tests the highlighting functionality in case of columns to be missed for comparison.
	 * 
	 * @throws SAXException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws LigretoException
	 */
	@Test
	public void testKeyLayout() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("highlightreport", true);
	}
}
