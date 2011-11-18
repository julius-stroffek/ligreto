/**
 * 
 */
package net.ligreto.parser;

import java.util.Stack;

import net.ligreto.exceptions.AssertionException;
import net.ligreto.exceptions.InvalidFormatException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.exceptions.ReportException;

import net.ligreto.parser.nodes.DataSourceNode;
import net.ligreto.parser.nodes.JoinNode;
import net.ligreto.parser.nodes.LigretoNode;
import net.ligreto.parser.nodes.PtpNode;
import net.ligreto.parser.nodes.PostprocessNode;
import net.ligreto.parser.nodes.PreprocessNode;
import net.ligreto.parser.nodes.ReportNode;
import net.ligreto.parser.nodes.SqlNode;
import net.ligreto.parser.nodes.TargetNode;
import net.ligreto.parser.nodes.TransferNode;
import net.ligreto.util.Pair;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Object types being parsed by the parser. */
enum ObjectType {
	NONE, LIGRETO, DATA_SOURCE, INIT, INIT_SQL, QUERY, REPORT, DATA, TEMPLATE, SQL, JOIN, JOIN_SQL, PARAM, PTP, PTP_PREPROCESS, PTP_PREPROCESS_SQL, PTP_TRANSFER, PTP_TRANSFER_SQL, PTP_POSTPROCESS, PTP_POSTPROCESS_SQL
};

/**
 * @author Julius Stroffek
 * 
 */
public class SAXContentHandler implements ContentHandler, DTDHandler, ErrorHandler {

	/** The configuration where the results are stored. */
	LigretoNode ligretoNode;

	/**
	 * The report configuration where the actual report configuration data are
	 * stored to.
	 */
	ReportNode reportNode;

	/** The parsed data source object. */
	DataSourceNode dataSource;

	/** The stack of object types being parsed. */
	Stack<ObjectType> objectStack = new Stack<ObjectType>();

	/** The name, query pair. */
	Pair<String, StringBuilder> query;

	/** The SQL query. */
	SqlNode sql;

	/** The join node. */
	JoinNode join;

	/** The Pre-Process/Transfer/Post-Process node - PTP */
	PtpNode ptpNode;

	/** The Pre-Processing of PTP transfer */
	PreprocessNode ptpPreprocess;

	/** The Post-Processing of PTP transfer */
	PostprocessNode ptpPostprocess;

	/** The Transfer of PTP transfer */
	TransferNode ptpTransfer;

	/** The name of the parameter being parsed. */
	String paramName;

	/** The value of the parameter being parsed. */
	StringBuilder paramValue;

	/** Constructs the report configuration content handler. */
	public SAXContentHandler(LigretoNode ligretoNode) {
		this.ligretoNode = ligretoNode;
	}

	@Override
	public void characters(char[] chars, int start, int length) throws SAXException {
		switch (objectStack.empty() ? ObjectType.NONE : objectStack.peek()) {
		case QUERY:
			query.getSecond().append(chars, start, length);
			break;
		case SQL:
		case JOIN_SQL:
		case INIT_SQL:
		case PTP_TRANSFER_SQL:
		case PTP_PREPROCESS_SQL:
		case PTP_POSTPROCESS_SQL:
			sql.getQueryBuilder().append(chars, start, length);
			break;
		case PARAM:
			paramValue.append(chars, start, length);
			break;
		}
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		switch (objectStack.pop()) {
		case DATA_SOURCE:
			ligretoNode.addDataSource(dataSource);
			break;
		case QUERY:
			ligretoNode.addQuery(query.getFirst(), query.getSecond().toString());
			query = null;
			break;
		case REPORT:
			ligretoNode.addReport(reportNode);
			reportNode = null;
			break;
		case SQL:
			reportNode.addSql(sql);
			sql = null;
			break;
		case JOIN_SQL:
			join.addSql(sql);
			sql = null;
			break;
		case INIT_SQL:
			dataSource.addSql(sql);
			sql = null;
			break;
		case PTP_PREPROCESS_SQL:
			ptpPreprocess.addSql(sql);
			break;
		case PTP_POSTPROCESS_SQL:
			ptpPostprocess.addSql(sql);
			break;
		case JOIN:
			reportNode.addJoin(join);
			join = null;
			break;
		case PARAM:
			ligretoNode.addParam(paramName, paramValue.toString());
			break;
		}
	}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
		// Auto-generated method stub
	}

	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
		// Auto-generated method stub
	}

	@Override
	public void processingInstruction(String arg0, String arg1) throws SAXException {
		// Auto-generated method stub
	}

	@Override
	public void setDocumentLocator(Locator arg0) {
		// Auto-generated method stub
	}

	@Override
	public void skippedEntity(String arg0) throws SAXException {
		// Auto-generated method stub
	}

	@Override
	public void startDocument() throws SAXException {
		// Auto-generated method stub
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		try {
			int entryStackDepth = objectStack.size();
			switch (objectStack.empty() ? ObjectType.NONE : objectStack.peek()) {
			case LIGRETO:
				if ("param".equals(localName)) {
					objectStack.push(ObjectType.PARAM);
					paramValue = new StringBuilder();
					paramName = atts.getValue("name");
					if (atts.getValue("value") != null) {
						paramValue.append(atts.getValue("value"));
					}
				} else if ("report".equals(localName)) {
					objectStack.push(ObjectType.REPORT);
					try {
						reportNode = new ReportNode(ligretoNode, atts.getValue("name"), atts.getValue("type"));
						String options = atts.getValue("options");
						if (options != null) {
							reportNode.setOptions(options);
						}
					} catch (ReportException e) {
						throw new SAXException(e);
					}
				} else if ("ptp".equals(localName)) {
					ptpNode = new PtpNode(ligretoNode);
					ptpNode.setName(atts.getValue("name"));
					ligretoNode.addPTP(ptpNode);
					objectStack.push(ObjectType.PTP);
				} else {
					objectStack.push(ObjectType.NONE);
				}
				break;
			case DATA_SOURCE:
				if ("driver".equals(localName)) {
					dataSource.setDriverClass(atts.getValue("value"));
					objectStack.push(ObjectType.NONE);
				} else if ("uri".equals(localName)) {
					dataSource.setUri(atts.getValue("value"));
					objectStack.push(ObjectType.NONE);
				} else if ("param".equals(localName)) {
					dataSource.setParameter(atts.getValue("name"), atts.getValue("value"));
					objectStack.push(ObjectType.NONE);
				} else if ("init".equals(localName)) {
					objectStack.push(ObjectType.INIT);
				} else {
					objectStack.push(ObjectType.NONE);
				}
				break;
			case INIT:
				if ("sql".equals(localName)) {
					objectStack.push(ObjectType.INIT_SQL);
					sql = new SqlNode(ligretoNode);
					if (atts.getValue("data-source") != null) {
						sql.setDataSource(atts.getValue("data-source"));
					}
					if (atts.getValue("query") != null) {
						sql.setQueryName(atts.getValue("query"));
					}
				}
				break;
			case REPORT:
				if ("template".equals(localName)) {
					objectStack.push(ObjectType.NONE);
					reportNode.setTemplate(atts.getValue("file"));
				} else if ("output".equals(localName)) {
					objectStack.push(ObjectType.NONE);
					reportNode.setOutput(atts.getValue("file"));
				} else if ("data".equals(localName)) {
					objectStack.push(ObjectType.DATA);
				} else {
					objectStack.push(ObjectType.NONE);
				}
				break;
			case DATA:
				if ("sql".equals(localName)) {
					objectStack.push(ObjectType.SQL);
					sql = new SqlNode(ligretoNode);
					if (atts.getValue("data-source") != null) {
						sql.setDataSource(atts.getValue("data-source"));
					}
					if (atts.getValue("target") != null) {
						sql.setTarget(atts.getValue("target"));
					}
					if (atts.getValue("query") != null) {
						sql.setQueryName(atts.getValue("query"));
					}
					if (atts.getValue("header") != null) {
						sql.setHeader(atts.getValue("header"));
					}
					if (atts.getValue("append") != null) {
						sql.setAppend(atts.getValue("append"));
					}
					if (atts.getValue("exclude") != null) {
						sql.setExclude(atts.getValue("exclude"));
					}
				} else if ("join".equals(localName)) {
					objectStack.push(ObjectType.JOIN);
					join = new JoinNode(ligretoNode);
					if (atts.getValue("target") != null) {
						join.setTarget(atts.getValue("target"));
					}
					if (atts.getValue("type") != null) {
						join.setJoinType(atts.getValue("type"));
					}
					if (atts.getValue("diffs") != null) {
						join.setDiffs(atts.getValue("diffs"));
					}
					if (atts.getValue("interlaced") != null) {
						join.setInterlaced(atts.getValue("interlaced"));
					}
					if (atts.getValue("highlight") != null) {
						join.setHighlight(atts.getValue("highlight"));
					}
					if (atts.getValue("hlColor") != null) {
						join.setHlColor(atts.getValue("hlColor"));
					}
					if (atts.getValue("on") != null) {
						join.setOn(atts.getValue("on"));
					}
					if (atts.getValue("exclude") != null) {
						join.setExclude(atts.getValue("exclude"));
					}
					if (atts.getValue("header") != null) {
						join.setHeader(atts.getValue("header"));
					}
					if (atts.getValue("append") != null) {
						join.setAppend(atts.getValue("append"));
					}
				}
				break;
			case JOIN:
				if ("sql".equals(localName)) {
					if (join.getSqlQueries().size() > 1
							&& (join.getJoinType() == JoinNode.JoinType.LEFT || join.getJoinType() == JoinNode.JoinType.RIGHT)) {
						throw new SAXException(new LigretoException(
								"Left or right join could have only two sql queries specified."));
					}
					objectStack.push(ObjectType.JOIN_SQL);
					sql = new SqlNode(ligretoNode);
					if (atts.getValue("data-source") != null) {
						sql.setDataSource(atts.getValue("data-source"));
					}
					if (atts.getValue("on") != null) {
						sql.setOn(atts.getValue("on"));
					}
					if (atts.getValue("exclude") != null) {
						sql.setExclude(atts.getValue("exclude"));
					}
					if (atts.getValue("query") != null) {
						sql.setQueryName(atts.getValue("query"));
					}
				}
				break;
			case PTP:
				if ("preprocess".equals(localName)) {
					objectStack.push(ObjectType.PTP_PREPROCESS);
					ptpPreprocess = new PreprocessNode(ligretoNode);
					ptpNode.setPreprocessNode(ptpPreprocess);
				} else if ("transfer".equals(localName)) {
					objectStack.push(ObjectType.PTP_TRANSFER);
					ptpTransfer = new TransferNode(ligretoNode);
					ptpNode.addTransferNode(ptpTransfer);
				} else if ("postprocess".equals(localName)) {
					objectStack.push(ObjectType.PTP_POSTPROCESS);
					ptpPostprocess = new PostprocessNode(ligretoNode);
					ptpNode.setPostprocessNode(ptpPostprocess);
				}
				break;
			case PTP_PREPROCESS:
				if ("sql".equals(localName)) {
					objectStack.push(ObjectType.PTP_PREPROCESS_SQL);
					sql = new SqlNode(ligretoNode);
					if (atts.getValue("data-source") != null) {
						sql.setDataSource(atts.getValue("data-source"));
					}
					if (atts.getValue("query") != null) {
						sql.setQueryName(atts.getValue("query"));
					}
				}
				break;
			case PTP_TRANSFER:
				if ("target".equals(localName)) {
					objectStack.push(ObjectType.NONE);
					TargetNode ptpTarget = new TargetNode(ligretoNode);
					if (atts.getValue("table") != null) {
						ptpTarget.setTable(atts.getValue("table"));
					}
					if (atts.getValue("data-source") != null) {
						ptpTarget.setDataSource(atts.getValue("data-source"));
					}
					if (atts.getValue("create") != null) {
						ptpTarget.setCreate(atts.getValue("create"));
					}
					if (atts.getValue("recreate") != null) {
						ptpTarget.setRecreate(atts.getValue("recreate"));
					}
					if (atts.getValue("truncate") != null) {
						ptpTarget.setTruncate(atts.getValue("truncate"));
					}
					if (atts.getValue("commitInterval") != null) {
						ptpTarget.setCommitInterval(atts.getValue("commitInterval"));
					}
					ptpTransfer.setTargetNode(ptpTarget);
				} else if ("sql".equals(localName)) {
					objectStack.push(ObjectType.PTP_TRANSFER_SQL);
					sql = new SqlNode(ligretoNode);
					if (atts.getValue("data-source") != null) {
						sql.setDataSource(atts.getValue("data-source"));
					}
					if (atts.getValue("query") != null) {
						sql.setQueryName(atts.getValue("query"));
					}
					ptpTransfer.setSqlNode(sql);
				}
				break;
			case PTP_POSTPROCESS:
				if ("sql".equals(localName)) {
					objectStack.push(ObjectType.PTP_POSTPROCESS_SQL);
					sql = new SqlNode(ligretoNode);
					if (atts.getValue("data-source") != null) {
						sql.setDataSource(atts.getValue("data-source"));
					}
					if (atts.getValue("query") != null) {
						sql.setQueryName(atts.getValue("query"));
					}
				}
				break;
			default:
				if ("query".equals(localName)) {
					query = new Pair<String, StringBuilder>(atts.getValue("name"), new StringBuilder());
					objectStack.push(ObjectType.QUERY);
				} else if ("data-source".equals(localName)) {
					objectStack.push(ObjectType.DATA_SOURCE);
					dataSource = new DataSourceNode(ligretoNode, atts.getValue("name"));
				} else if ("ligreto".equals(localName)) {
					objectStack.push(ObjectType.LIGRETO);
				} else {
					objectStack.push(ObjectType.NONE);
				}
				break;
			}
			if (objectStack.size() != entryStackDepth + 1) {
				throw new AssertionException("Fatal error in parser: The parsed node was not added into the object stack.");
			}
		} catch (InvalidFormatException e) {
			throw new SAXException("Error parsing input file.", e);
		}
	}

	@Override
	public void startPrefixMapping(String arg0, String arg1) throws SAXException {
		// Auto-generated method stub
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
	public void notationDecl(String arg0, String arg1, String arg2) throws SAXException {
	}

	@Override
	public void unparsedEntityDecl(String arg0, String arg1, String arg2, String arg3) throws SAXException {
	}

}
