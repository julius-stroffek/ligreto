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
import net.ligreto.parser.nodes.JoinNode.JoinLayoutType;
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

	protected String getAttributeValue(Attributes atts, String name) {
		String value = atts.getValue(name);
		if (value != null) {
			value = ligretoNode.substituteParams(value);
		}
		return value;
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
			join.setReportNode(reportNode);
			join = null;
			break;
		case PARAM:
			try {
				ligretoNode.addParam(paramName, paramValue.toString());
			} catch (LigretoException e) {
				throw new SAXException("Error parsing input file.", e);
			}
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
					paramName = getAttributeValue(atts, "name");
					if (getAttributeValue(atts, "value") != null) {
						paramValue.append(getAttributeValue(atts, "value"));
					}
				} else if ("report".equals(localName)) {
					objectStack.push(ObjectType.REPORT);
					try {
						reportNode = new ReportNode(ligretoNode, getAttributeValue(atts, "name"), getAttributeValue(atts, "type"));
						String options = getAttributeValue(atts, "options");
						if (options != null) {
							reportNode.setOptions(options);
						}
						reportNode.setLocale(getAttributeValue(atts, "locale"));
						reportNode.setResult(getAttributeValue(atts, "result"));
					} catch (ReportException e) {
						throw new SAXException(e);
					}
				} else if ("ptp".equals(localName)) {
					ptpNode = new PtpNode(ligretoNode);
					ptpNode.setName(getAttributeValue(atts, "name"));
					ligretoNode.addPTP(ptpNode);
					objectStack.push(ObjectType.PTP);
				} else {
					objectStack.push(ObjectType.NONE);
				}
				break;
			case DATA_SOURCE:
				if ("driver".equals(localName)) {
					dataSource.setDriverClass(getAttributeValue(atts, "value"));
					objectStack.push(ObjectType.NONE);
				} else if ("uri".equals(localName)) {
					dataSource.setUri(getAttributeValue(atts, "value"));
					objectStack.push(ObjectType.NONE);
				} else if ("param".equals(localName)) {
					dataSource.setParameter(getAttributeValue(atts, "name"), getAttributeValue(atts, "value"));
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
					if (getAttributeValue(atts, "data-source") != null) {
						sql.setDataSource(getAttributeValue(atts, "data-source"));
					}
					if (getAttributeValue(atts, "query") != null) {
						sql.setQueryName(getAttributeValue(atts, "query"));
					}
					if (getAttributeValue(atts, "exceptions") != null) {
						sql.setExceptions(getAttributeValue(atts, "exceptions"));
					}
					if (getAttributeValue(atts, "type") != null) {
						sql.setQueryType(getAttributeValue(atts, "type"));
					}
				}
				break;
			case REPORT:
				if ("template".equals(localName)) {
					objectStack.push(ObjectType.NONE);
					reportNode.setTemplate(getAttributeValue(atts, "file"));
				} else if ("output".equals(localName)) {
					objectStack.push(ObjectType.NONE);
					reportNode.setOutput(getAttributeValue(atts, "file"));
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
					if (getAttributeValue(atts, "data-source") != null) {
						sql.setDataSource(getAttributeValue(atts, "data-source"));
					}
					if (getAttributeValue(atts, "target") != null) {
						sql.setTarget(getAttributeValue(atts, "target"));
					}
					if (getAttributeValue(atts, "query") != null) {
						sql.setQueryName(getAttributeValue(atts, "query"));
					}
					if (getAttributeValue(atts, "header") != null) {
						sql.setHeader(getAttributeValue(atts, "header"));
					}
					if (getAttributeValue(atts, "append") != null) {
						sql.setAppend(getAttributeValue(atts, "append"));
					}
					if (getAttributeValue(atts, "exclude") != null) {
						sql.setExclude(getAttributeValue(atts, "exclude"));
					}
					if (getAttributeValue(atts, "exceptions") != null) {
						sql.setExceptions(getAttributeValue(atts, "exceptions"));
					}
					if (getAttributeValue(atts, "type") != null) {
						sql.setQueryType(getAttributeValue(atts, "type"));
					}
					sql.setResult(getAttributeValue(atts, "result"));
				} else if ("join".equals(localName)) {
					objectStack.push(ObjectType.JOIN);
					join = new JoinNode(ligretoNode);
					if (getAttributeValue(atts, "target") != null) {
						join.setTarget(getAttributeValue(atts, "target"));
					}
					if (getAttributeValue(atts, "type") != null) {
						join.setJoinType(getAttributeValue(atts, "type"));
					}
					if (getAttributeValue(atts, "diffs") != null) {
						join.setDiffs(getAttributeValue(atts, "diffs"));
					}
					if (getAttributeValue(atts, "layout") != null) {
						join.setJoinLayoutType(getAttributeValue(atts, "layout"));
					}
					if (getAttributeValue(atts, "interlaced") != null) {
						if (Boolean.parseBoolean(getAttributeValue(atts, "interlaced"))) {
							join.setJoinLayoutType(JoinLayoutType.INTERLACED);
						} else {
							join.setJoinLayoutType(JoinLayoutType.NORMAL);
						}
					}
					if (getAttributeValue(atts, "highlight") != null) {
						join.setHighlight(getAttributeValue(atts, "highlight"));
					}
					if (getAttributeValue(atts, "hlColor") != null) {
						join.setHlColor(getAttributeValue(atts, "hlColor"));
					}
					if (getAttributeValue(atts, "on") != null) {
						join.setOn(getAttributeValue(atts, "on"));
					}
					if (getAttributeValue(atts, "group-by") != null) {
						join.setGroupBy(getAttributeValue(atts, "group-by"));
					}
					if (getAttributeValue(atts, "exclude") != null) {
						join.setExclude(getAttributeValue(atts, "exclude"));
					}
					if (getAttributeValue(atts, "header") != null) {
						join.setHeader(getAttributeValue(atts, "header"));
					}
					if (getAttributeValue(atts, "append") != null) {
						join.setAppend(getAttributeValue(atts, "append"));
					}
					join.setLocale(getAttributeValue(atts, "locale"));
					if (getAttributeValue(atts, "collation") != null) {
						join.setCollation(getAttributeValue(atts, "collation"));
					}
					join.setResult(getAttributeValue(atts, "result"));
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
					if (getAttributeValue(atts, "data-source") != null) {
						sql.setDataSource(getAttributeValue(atts, "data-source"));
					}
					if (getAttributeValue(atts, "on") != null) {
						sql.setOn(getAttributeValue(atts, "on"));
					}
					if (getAttributeValue(atts, "exclude") != null) {
						sql.setExclude(getAttributeValue(atts, "exclude"));
					}
					if (getAttributeValue(atts, "query") != null) {
						sql.setQueryName(getAttributeValue(atts, "query"));
					}
					if (getAttributeValue(atts, "exceptions") != null) {
						sql.setExceptions(getAttributeValue(atts, "exceptions"));
					}
					if (getAttributeValue(atts, "type") != null) {
						sql.setQueryType(getAttributeValue(atts, "type"));
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
					ptpTransfer.setResult(getAttributeValue(atts, "result"));
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
					if (getAttributeValue(atts, "data-source") != null) {
						sql.setDataSource(getAttributeValue(atts, "data-source"));
					}
					if (getAttributeValue(atts, "query") != null) {
						sql.setQueryName(getAttributeValue(atts, "query"));
					}
					if (getAttributeValue(atts, "exceptions") != null) {
						sql.setExceptions(getAttributeValue(atts, "exceptions"));
					}
					if (getAttributeValue(atts, "type") != null) {
						sql.setQueryType(getAttributeValue(atts, "type"));
					}
				}
				break;
			case PTP_TRANSFER:
				if ("target".equals(localName)) {
					objectStack.push(ObjectType.NONE);
					TargetNode ptpTarget = new TargetNode(ligretoNode);
					if (getAttributeValue(atts, "table") != null) {
						ptpTarget.setTable(getAttributeValue(atts, "table"));
					}
					if (getAttributeValue(atts, "data-source") != null) {
						ptpTarget.setDataSource(getAttributeValue(atts, "data-source"));
					}
					if (getAttributeValue(atts, "create") != null) {
						ptpTarget.setCreate(getAttributeValue(atts, "create"));
					}
					if (getAttributeValue(atts, "recreate") != null) {
						ptpTarget.setRecreate(getAttributeValue(atts, "recreate"));
					}
					if (getAttributeValue(atts, "truncate") != null) {
						ptpTarget.setTruncate(getAttributeValue(atts, "truncate"));
					}
					if (getAttributeValue(atts, "commitInterval") != null) {
						ptpTarget.setCommitInterval(getAttributeValue(atts, "commitInterval"));
					}
					ptpTransfer.setTargetNode(ptpTarget);
				} else if ("sql".equals(localName)) {
					objectStack.push(ObjectType.PTP_TRANSFER_SQL);
					sql = new SqlNode(ligretoNode);
					if (getAttributeValue(atts, "data-source") != null) {
						sql.setDataSource(getAttributeValue(atts, "data-source"));
					}
					if (getAttributeValue(atts, "query") != null) {
						sql.setQueryName(getAttributeValue(atts, "query"));
					}
					if (getAttributeValue(atts, "exceptions") != null) {
						sql.setExceptions(getAttributeValue(atts, "exceptions"));
					}
					if (getAttributeValue(atts, "type") != null) {
						sql.setQueryType(getAttributeValue(atts, "type"));
					}
					ptpTransfer.setSqlNode(sql);
				}
				break;
			case PTP_POSTPROCESS:
				if ("sql".equals(localName)) {
					objectStack.push(ObjectType.PTP_POSTPROCESS_SQL);
					sql = new SqlNode(ligretoNode);
					if (getAttributeValue(atts, "data-source") != null) {
						sql.setDataSource(getAttributeValue(atts, "data-source"));
					}
					if (getAttributeValue(atts, "query") != null) {
						sql.setQueryName(getAttributeValue(atts, "query"));
					}
					if (getAttributeValue(atts, "exceptions") != null) {
						sql.setExceptions(getAttributeValue(atts, "exceptions"));
					}
					if (getAttributeValue(atts, "type") != null) {
						sql.setQueryType(getAttributeValue(atts, "type"));
					}
				}
				break;
			default:
				if ("query".equals(localName)) {
					query = new Pair<String, StringBuilder>(getAttributeValue(atts, "name"), new StringBuilder());
					objectStack.push(ObjectType.QUERY);
				} else if ("data-source".equals(localName)) {
					objectStack.push(ObjectType.DATA_SOURCE);
					dataSource = new DataSourceNode(ligretoNode, getAttributeValue(atts, "name"));
					dataSource.setDescription(getAttributeValue(atts, "desc"));
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
