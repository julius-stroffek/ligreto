/**
 * 
 */
package net.ligreto.junit.tests.func;

//import static org.junit.Assert.*;

import java.io.IOException;

import net.ligreto.parser.Parser;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Julius Stroffek
 *
 */
public class ParserTest {

	/**
	 * Test method for {@link net.ligreto.parser.nodes.Parser#parse(java.lang.String)}.
	 * @throws IOException 
	 * @throws SAXException 
	 */
	@Test
	public void testParse() throws SAXException, IOException {
		Parser.parse("sample.xml");
	}

}
