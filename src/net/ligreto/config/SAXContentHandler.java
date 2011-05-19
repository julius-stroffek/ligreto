/**
 * 
 */
package net.ligreto.config;

import java.util.Stack;

import net.ligreto.config.data.DataSourceConfig;
import net.ligreto.config.data.JoinNode;
import net.ligreto.config.data.ReportConfig;
import net.ligreto.config.data.SqlNode;
import net.ligreto.exceptions.AssertionException;

import net.ligreto.util.Pair;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Object types being parsed by the parser. */
enum ObjectType {NONE, LIGRETO, DATA_SOURCE, QUERY, REPORT, DATA, TEMPLATE, SQL, JOIN, JOIN_SQL};

/**
 * @author Julius Stroffek
 *
 */
public class SAXContentHandler implements ContentHandler, DTDHandler, ErrorHandler {

	/** The configuration where the results are stored. */
	FileConfig fileConfig;
	
	/** The report configuration where the actual report configuration data are stored to. */
	ReportConfig reportConfig;
	
	/** The parsed data source object. */
	DataSourceConfig dataSource;
	
	/** The stack of object types being parsed. */
	Stack<ObjectType> objectStack = new Stack<ObjectType>();
	
	/** The name, query pair. */
	Pair<String, StringBuilder> query;
	
	/** The SQL query. */
	SqlNode sql;
	
	/** The join node. */
	JoinNode join;
	
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
		case SQL:
		case JOIN_SQL:
			sql.getQuery().append(chars, start, start + length);
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
			query = null;
			break;
		case REPORT:
			fileConfig.addReport(reportConfig);
			reportConfig = null;
			break;
		case SQL:
			reportConfig.addSql(sql);
			sql = null;
			break;
		case JOIN_SQL:
			join.addSql(sql);
			sql = null;
			break;
		case JOIN:
			reportConfig.addJoin(join);
			join = null;
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
		int entryStackDepth = objectStack.size();
		switch (objectStack.empty() ? ObjectType.NONE : objectStack.peek()) {
		case LIGRETO:
			if ("param".equals(localName)) {
				objectStack.push(ObjectType.NONE);
				fileConfig.addParam(atts.getValue("name"), atts.getValue("value"));
			} else if ("report".equals(localName)) {
				objectStack.push(ObjectType.REPORT);
				reportConfig = new ReportConfig();
			} else {
				objectStack.push(ObjectType.NONE);
			}
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
			if ("template".equals(localName)) {
				objectStack.push(ObjectType.NONE);
				reportConfig.setTemplate(atts.getValue("file"));
			} else	if ("data".equals(localName)) {
				objectStack.push(ObjectType.DATA);
			} else {
				objectStack.push(ObjectType.NONE);
			}
			break;
		case DATA:
			if ("sql".equals(localName)) {
				objectStack.push(ObjectType.SQL);
				sql = new SqlNode();
				if (atts.getValue("data-source") != null) {
					sql.setDataSource(atts.getValue("data-source"));
				}
				if (atts.getValue("name") != null) {
					sql.setQueryName(atts.getValue("name"));
				}
				if (atts.getValue("target") != null) {
					sql.setTarget(atts.getValue("target"));
				}
			} else if ("join".equals(localName)) {
				objectStack.push(ObjectType.JOIN);
				join = new JoinNode();
			}
			break;
		case JOIN:
			if ("sql".equals(localName)) {
				objectStack.push(ObjectType.JOIN_SQL);
				sql = new SqlNode();
				if (atts.getValue("data-source") != null) {
					sql.setDataSource(atts.getValue("data-source"));
				}
				if (atts.getValue("name") != null) {
					sql.setQueryName(atts.getValue("name"));
				}
				if (atts.getValue("on") != null) {
					sql.setOn(atts.getValue("on"));
				}
			}
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
		if (objectStack.size() != entryStackDepth + 1) {
			throw new AssertionException("Fatal error in parser: The parsed node was not added into the object stack.");
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
