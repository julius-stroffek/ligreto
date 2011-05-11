/**
 * 
 */
package net.ligreto.config;

import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class encapsulates all the report configuration read from the report file.
 * 
 * @author Julius Stroffek
 *
 */
public class ReportConfig {
	
	protected static final String PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
	protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";
	protected static final String NAMESPACE_PREFIXES_FEATURE_ID = "http://xml.org/sax/features/namespace-prefixes";
	protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";
	protected static final String XINCLUDE_FEATURE_ID = "http://apache.org/xml/features/xinclude";
	
	protected ReportConfig() {
	}

	public static ReportConfig parse(String systemId) throws SAXException, IOException {
		ReportConfig reportConfig = new ReportConfig();
		SAXContentHandler handler = new SAXContentHandler(reportConfig);
		
		XMLReader parser = XMLReaderFactory.createXMLReader(PARSER_NAME);
		parser.setFeature(NAMESPACES_FEATURE_ID, true);
		parser.setFeature(NAMESPACE_PREFIXES_FEATURE_ID, true);
		parser.setFeature(VALIDATION_FEATURE_ID, true);
		parser.setFeature(XINCLUDE_FEATURE_ID, true);
		parser.setContentHandler(handler);
		parser.setErrorHandler(handler);
		parser.setDTDHandler(handler);		
		parser.parse(new InputSource(systemId));
		
		return reportConfig;
	}
}
