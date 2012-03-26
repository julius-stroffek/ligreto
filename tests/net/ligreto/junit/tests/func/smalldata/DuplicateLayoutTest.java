/**
 * 
 */
package net.ligreto.junit.tests.func.smalldata;


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
public class DuplicateLayoutTest {
	@Test
	public void testDuplicateLayout() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("duplicatelayoutreport", true);
	}
}
