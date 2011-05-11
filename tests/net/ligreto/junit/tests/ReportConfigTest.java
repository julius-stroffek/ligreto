/**
 * 
 */
package net.ligreto.junit.tests;

import static org.junit.Assert.*;

import java.io.IOException;

import net.ligreto.config.ReportConfig;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author julo
 *
 */
public class ReportConfigTest {

	/**
	 * Test method for {@link net.ligreto.config.ReportConfig#parse(java.io.File)}.
	 * @throws IOException 
	 * @throws SAXException 
	 */
	@Test
	public void testParse() throws SAXException, IOException {
		ReportConfig.parse(ClassLoader.getSystemResource("data/sample.xml").toString());
	}

}
