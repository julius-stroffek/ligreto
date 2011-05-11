/**
 * 
 */
package net.ligreto.config;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Julius Stroffek
 *
 */
public class SAXContentHandler implements ContentHandler, DTDHandler, ErrorHandler {

	/** The configuration where the results are stored. */
	ReportConfig reportConfig;
	
	/** Constructs the report configuration content handler. */
	public SAXContentHandler(ReportConfig reportConfig) {
		this.reportConfig = reportConfig;
	}
	
	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDocumentLocator(Locator arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void skippedEntity(String arg0) throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		throw e;		
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		throw e;		
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		throw e;		
	}

	@Override
	public void notationDecl(String arg0, String arg1, String arg2)
			throws SAXException {
		// TODO Auto-generated method stub		
	}

	@Override
	public void unparsedEntityDecl(String arg0, String arg1, String arg2,
			String arg3) throws SAXException {
		// TODO Auto-generated method stub		
	}

}
