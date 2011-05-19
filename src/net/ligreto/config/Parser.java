/**
 * 
 */
package net.ligreto.config;

import java.io.IOException;

import net.ligreto.config.nodes.LigretoNode;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class defines the main configuration file entry processing.
 * 
 * @author Julius Stroffek
 *
 */
public class Parser {
	protected static final String PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
	protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";
	protected static final String NAMESPACE_PREFIXES_FEATURE_ID = "http://xml.org/sax/features/namespace-prefixes";
	protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";
	protected static final String XINCLUDE_FEATURE_ID = "http://apache.org/xml/features/xinclude";
	
	/**
	 * This function parses the specified file and creates the LigretoNode object holding the whole configuration.
	 */
	public static LigretoNode parse(String systemId) throws SAXException, IOException {
		LigretoNode ligretoNode = new LigretoNode();
		SAXContentHandler handler = new SAXContentHandler(ligretoNode);
		
		XMLReader parser = XMLReaderFactory.createXMLReader(PARSER_NAME);
		parser.setFeature(NAMESPACES_FEATURE_ID, true);
		parser.setFeature(NAMESPACE_PREFIXES_FEATURE_ID, true);
		parser.setFeature(VALIDATION_FEATURE_ID, true);
		parser.setFeature(XINCLUDE_FEATURE_ID, true);
		parser.setContentHandler(handler);
		parser.setErrorHandler(handler);
		parser.setDTDHandler(handler);		
		parser.parse(new InputSource(systemId));
		
		return ligretoNode;
	}
}