/**
 * 
 */
package net.ligreto.junit.tests.func.nodata;

//import static org.junit.Assert.*;

import java.io.IOException;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.junit.util.TestUtil;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Julius Stroffek
 *
 */
public class SqlQueryTest {

	/**
	 * Test method for SQL Query nodes in the main ligreto node.
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws LigretoException 
	 */
	@Test
	public void testParse() throws SAXException, IOException, LigretoException {
		TestUtil.testReport("sqlquerytest");
	}

}
