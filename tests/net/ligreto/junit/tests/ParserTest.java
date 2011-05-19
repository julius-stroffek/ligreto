/**
 * 
 */
package net.ligreto.junit.tests;

//import static org.junit.Assert.*;

import java.io.IOException;

import net.ligreto.config.Parser;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Julius Stroffek
 *
 */
public class ParserTest {

	/**
	 * Test method for {@link net.ligreto.config.nodes.Parser#parse(java.lang.String)}.
	 * @throws IOException 
	 * @throws SAXException 
	 */
	@Test
	public void testParse() throws SAXException, IOException {
		Parser.parse(ClassLoader.getSystemResource("data/sample.xml").toString());
	}

}
