/**
 * 
 */
package net.ligreto.config;

import java.util.Stack;

import net.ligreto.config.data.DataSourceConfig;
import net.ligreto.util.Pair;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Object types being parsed by the parser. */
enum ObjectType {NONE, LIGRETO, DATA_SOURCE, QUERY, REPORT, TEMPLATE, SQL, JOIN};

/**
 * @author Julius Stroffek
 *
 */
public class SAXContentHandler implements ContentHandler, DTDHandler, ErrorHandler {

	/** The configuration where the results are stored. */
	FileConfig fileConfig;
	
	/** The parsed data source object. */
	DataSourceConfig dataSource;
	
	/** The stack of object types being parsed. */
	Stack<ObjectType> objectStack = new Stack<ObjectType>();
	
	/** The name, query pair. */
	Pair<String, StringBuilder> query;
	
	/** Constructs the report configuration content handler. */
	public SAXContentHandler(FileConfig fileConfig) {
		this.fileConfig = fileConfig;
	}
	
	@Override
	public void characters(char[] chars, int start, int length) throws SAXException {
		switch (objectStack.empty() ? ObjectType.NONE : objectStack.peek()) {
		case QUERY:
			query.getSecond().append(chars, start, start + length);
			break;
		}
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		switch (objectStack.pop()) {
		case DATA_SOURCE:
				fileConfig.addDataSource(dataSource);
				break;
		case QUERY:
			fileConfig.addQuery(query.getFirst(), query.getSecond().toString());
			break;
		}
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
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		switch (objectStack.empty() ? ObjectType.NONE : objectStack.peek()) {
		case LIGRETO:
			if ("param".equals(localName))
				fileConfig.addParam(atts.getValue("name"), atts.getValue("value"));
			objectStack.push(ObjectType.NONE);
			break;
		case DATA_SOURCE:
			if ("driver".equals(localName)) {
				dataSource.setDriverClass(atts.getValue("value"));
			} else if ("uri".equals(localName)) {
				dataSource.setUri(atts.getValue("value"));
			} else if ("param".equals(localName)) {
				dataSource.setParameter(atts.getValue("name"), atts.getValue("value"));
			}
			objectStack.push(ObjectType.NONE);
			break;
		case REPORT:
			objectStack.push(ObjectType.NONE);
			break;
		default:
			if ("query".equals(localName)) {
				query = new Pair<String, StringBuilder>(atts.getValue("name"), new StringBuilder());
				objectStack.push(ObjectType.QUERY);
			} else if ("data-source".equals(localName)) {
				objectStack.push(ObjectType.DATA_SOURCE);
				dataSource = new DataSourceConfig(atts.getValue("name"));
			} else if ("ligreto".equals(localName)) {
				objectStack.push(ObjectType.LIGRETO);
				dataSource = new DataSourceConfig(atts.getValue("name"));
			} else {
				objectStack.push(ObjectType.NONE);
			}
			break;
		}
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
