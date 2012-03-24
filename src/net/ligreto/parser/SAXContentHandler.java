/**
 * 
 */
package net.ligreto.parser;

import java.util.Stack;

import net.ligreto.exceptions.InvalidFormatException;
import net.ligreto.exceptions.InvalidValueException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.exceptions.ParserException;
import net.ligreto.exceptions.ReportException;

import net.ligreto.parser.nodes.DataSourceNode;
import net.ligreto.parser.nodes.JoinNode;
import net.ligreto.parser.nodes.LayoutNode;
import net.ligreto.parser.nodes.LayoutNode.LayoutType;
import net.ligreto.parser.nodes.LigretoNode;
import net.ligreto.parser.nodes.LimitNode;
import net.ligreto.parser.nodes.PtpNode;
import net.ligreto.parser.nodes.PostprocessNode;
import net.ligreto.parser.nodes.PreprocessNode;
import net.ligreto.parser.nodes.ReportNode;
import net.ligreto.parser.nodes.ResultNode;
import net.ligreto.parser.nodes.RowLimitNode;
import net.ligreto.parser.nodes.SqlNode;
import net.ligreto.parser.nodes.TargetNode;
import net.ligreto.parser.nodes.TransferNode;
import net.ligreto.util.Pair;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Object types being parsed by the parser. */
enum ObjectType {
	NONE, LIGRETO, DATA_SOURCE, INIT, INIT_SQL, QUERY, REPORT, DATA, TEMPLATE, SQL, JOIN, LAYOUT, RESULT, JOIN_SQL, PARAM, PTP, PTP_PREPROCESS, PTP_PREPROCESS_SQL, PTP_TRANSFER, PTP_TRANSFER_SQL, PTP_POSTPROCESS, PTP_POSTPROCESS_SQL
};

/**
 * @author Julius Stroffek
 * 
 */
public class SAXContentHandler implements ContentHandler, DTDHandler, ErrorHandler {

	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(SAXContentHandler.class);

	/** The configuration where the results are stored. */
	protected LigretoNode ligretoNode;

	/** The report configuration where the actual report configuration data are stored to. */
	protected ReportNode reportNode;

	/** The parsed data source object. */
	protected DataSourceNode dataSource;

	/** The stack of object types being parsed. */
	protected Stack<ObjectType> objectStack = new Stack<ObjectType>();

	/** The name, query pair. */
	protected Pair<String, StringBuilder> query;

	/** The SQL query. */
	protected SqlNode sql;

	/** The join node. */
	protected JoinNode join;
	
	/** The join layout. */
	protected LayoutNode layout;
	
	/** The result node. */
	protected ResultNode result;
	
	/** The row limit for the result. */
	protected RowLimitNode rowLimit;
	
	/** The limit for the result. */
	protected LimitNode limit;

	/** The Pre-Process/Transfer/Post-Process node - PTP */
	protected PtpNode ptpNode;

	/** The Pre-Processing of PTP transfer */
	protected PreprocessNode ptpPreprocess;

	/** The Post-Processing of PTP transfer */
	protected PostprocessNode ptpPostprocess;

	/** The Transfer of PTP transfer */
	protected TransferNode ptpTransfer;

	/** The name of the parameter being parsed. */
	protected String paramName;

	/** The value of the parameter being parsed. */
	protected StringBuilder paramValue;

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
	
	protected String getAttributeValueWithParams(Attributes atts, String name) {
		String value = atts.getValue(name);
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
		case LAYOUT:
			join.addLayout(layout);
			layout = null;
			break;
		case RESULT:
			layout.setResultNode(result);
			result = null;
			rowLimit = null;
			limit = null;
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
					if (getAttributeValueWithParams(atts, "value") != null) {
						paramValue.append(getAttributeValueWithParams(atts, "value"));
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
				} else if ("ligreto".equals(localName)) {
					objectStack.push(ObjectType.LIGRETO);
				} else {
					objectStack.push(ObjectType.NONE);
				}
				break;
			case DATA_SOURCE:
				if ("driver".equals(localName)) {
					dataSource.setDriverClass(getAttributeValueWithParams(atts, "value"));
					objectStack.push(ObjectType.NONE);
				} else if ("uri".equals(localName)) {
					dataSource.setUri(getAttributeValueWithParams(atts, "value"));
					objectStack.push(ObjectType.NONE);
				} else if ("param".equals(localName)) {
					dataSource.setParameter(getAttributeValue(atts, "name"), getAttributeValueWithParams(atts, "value"));
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
				} else if ("comparison".equals(localName)) {
					objectStack.push(ObjectType.JOIN);
					join = new JoinNode(ligretoNode);
					if (getAttributeValue(atts, "key") != null) {
						join.setKey(getAttributeValue(atts, "key"));
					}
					if (getAttributeValue(atts, "exclude") != null) {
						join.setExclude(getAttributeValue(atts, "exclude"));
					}
					if (getAttributeValue(atts, "columns") != null) {
						join.setColumns(getAttributeValue(atts, "columns"));
					}
					if (getAttributeValue(atts, "sort") != null) {
						join.setSortingStrategy(getAttributeValue(atts, "sort"));
					}
					join.setLocale(getAttributeValue(atts, "locale"));
					if (getAttributeValue(atts, "collation") != null) {
						join.setCollation(getAttributeValue(atts, "collation"));
					}
					if (getAttributeValue(atts, "duplicates") != null) {
						join.setDuplicates(getAttributeValue(atts, "duplicates"));
					}
				} else if ("join".equals(localName)) {
					log.warn("Use of <join> node is deprecated and might not work in the future releases.");
					log.warn("Please use <comparison> node instead.");
					objectStack.push(ObjectType.JOIN);
					join = new JoinNode(ligretoNode);
					if (getAttributeValue(atts, "on") != null) {
						join.setKey(getAttributeValue(atts, "on"));
					}
					if (getAttributeValue(atts, "exclude") != null) {
						join.setExclude(getAttributeValue(atts, "exclude"));
					}
					if (getAttributeValue(atts, "sort") != null) {
						join.setSortingStrategy(getAttributeValue(atts, "sort"));
					}
					join.setLocale(getAttributeValue(atts, "locale"));
					if (getAttributeValue(atts, "collation") != null) {
						join.setCollation(getAttributeValue(atts, "collation"));
					}
					if (getAttributeValue(atts, "duplicates") != null) {
						join.setDuplicates(getAttributeValue(atts, "duplicates"));
					}
					if (getAttributeValue(atts, "target") != null) {
						log.warn("Use of 'target' attribute in <join> node is deprecated and might not work in the future releases.");
						log.warn("Please use <layout> node instead.");
						layout = new LayoutNode(ligretoNode);
						if (getAttributeValue(atts, "target") != null) {
							layout.setTarget(getAttributeValue(atts, "target"));
						}
						if (getAttributeValue(atts, "type") != null) {
							layout.setJoinType(getAttributeValue(atts, "type"));
						}
						if (getAttributeValue(atts, "diffs") != null) {
							layout.setDiffs(getAttributeValue(atts, "diffs"));
						}
						if (getAttributeValue(atts, "layout") != null) {
							layout.setType(getAttributeValue(atts, "layout"));
						}
						if (getAttributeValue(atts, "interlaced") != null) {
							if (Boolean.parseBoolean(getAttributeValue(atts, "interlaced"))) {
								layout.setType(LayoutType.INTERLACED);
							} else {
								layout.setType(LayoutType.NORMAL);
							}
						}
						if (getAttributeValue(atts, "highlight") != null) {
							layout.setHighlight(getAttributeValue(atts, "highlight"));
						}
						if (getAttributeValue(atts, "hl-color") != null) {
							layout.setHlColor(getAttributeValue(atts, "hl-color"));
						}
						if (getAttributeValue(atts, "group-by") != null) {
							layout.setGroupBy(getAttributeValue(atts, "group-by"));
						}
						if (getAttributeValue(atts, "header") != null) {
							layout.setHeader(getAttributeValue(atts, "header"));
						}
						if (getAttributeValue(atts, "append") != null) {
							layout.setAppend(getAttributeValue(atts, "append"));
						}
						if (getAttributeValue(atts, "result") != null) {
							layout.setResult(getAttributeValue(atts, "result"));
						}
						join.addLayout(layout);
						layout = null;
					}
				}
				break;
			case JOIN:
				if ("sql".equals(localName)) {
					if (join.getSqlQueries().size() > 1) {
						throw new SAXException(new LigretoException(
								"There could be only two sql queries specified for the join."));
					}
					objectStack.push(ObjectType.JOIN_SQL);
					sql = new SqlNode(ligretoNode);
					if (getAttributeValue(atts, "data-source") != null) {
						sql.setDataSource(getAttributeValue(atts, "data-source"));
					}
					if (getAttributeValue(atts, "key") != null) {
						sql.setKey(getAttributeValue(atts, "key"));
					} else if (getAttributeValue(atts, "on") != null) {
						sql.setKey(getAttributeValue(atts, "on"));
					}
					if (getAttributeValue(atts, "columns") != null) {
						sql.setColumns(getAttributeValue(atts, "columns"));
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
				} else if ("layout".equals(localName)) {
					objectStack.push(ObjectType.LAYOUT);
					layout = new LayoutNode(ligretoNode);
					if (getAttributeValue(atts, "target") != null) {
						layout.setTarget(getAttributeValue(atts, "target"));
					}
					if (getAttributeValue(atts, "type") != null) {
						layout.setType(getAttributeValue(atts, "type"));
					}
					if (getAttributeValue(atts, "join") != null) {
						layout.setJoinType(getAttributeValue(atts, "join"));
					}
					if (getAttributeValue(atts, "limit") != null) {
						layout.setLimit(getAttributeValue(atts, "limit"));
					}
					if (getAttributeValue(atts, "diffs") != null) {
						layout.setDiffs(getAttributeValue(atts, "diffs"));
					}
					if (getAttributeValue(atts, "highlight") != null) {
						layout.setHighlight(getAttributeValue(atts, "highlight"));
					}
					if (getAttributeValue(atts, "hl-color") != null) {
						layout.setHlColor(getAttributeValue(atts, "hl-color"));
					}
					if (getAttributeValue(atts, "group-by") != null) {
						layout.setGroupBy(getAttributeValue(atts, "group-by"));
					}
					if (getAttributeValue(atts, "header") != null) {
						layout.setHeader(getAttributeValue(atts, "header"));
					}
					if (getAttributeValue(atts, "append") != null) {
						layout.setAppend(getAttributeValue(atts, "append"));
					}
					layout.setResult(getAttributeValue(atts, "result"));
				}
				break;
			case LAYOUT:
				if ("result".equals(localName)) {
					objectStack.push(ObjectType.RESULT);
					result = new ResultNode(ligretoNode);
					if (getAttributeValue(atts, "enabled") != null) {
						result.setEnabled(getAttributeValue(atts, "enabled"));
					}
				}
				break;
			case RESULT:
				objectStack.push(ObjectType.NONE);
				if ("row-limit".equals(localName)) {
					if (rowLimit != null) {
						throw new ParserException("Node <row-limit> could be specified only once in a <result> node.");
					}
					rowLimit = new RowLimitNode(ligretoNode);
					if (getAttributeValue(atts, "enabled") != null) {
						rowLimit.setEnabled(getAttributeValue(atts, "enabled"));
					}
					if (getAttributeValue(atts, "abs-diff-count") != null) {
						rowLimit.setAbsoluteDifference(getAttributeValue(atts, "abs-diff-count"));
					}
					if (getAttributeValue(atts, "rel-diff-count") != null) {
						rowLimit.setRelativeDifference(getAttributeValue(atts, "rel-diff-count"));
					}
					if (getAttributeValue(atts, "rel-non-matched-count") != null) {
						rowLimit.setRelativeNonMatched(getAttributeValue(atts, "rel-non-matched-count"));
					}
					if (getAttributeValue(atts, "abs-non-matched-count") != null) {
						rowLimit.setAbsoluteNonMatched(getAttributeValue(atts, "abs-non-matched-count"));
					}
					result.setRowLimitNode(rowLimit);
				} else if ("limit".equals(localName)) {
					limit = new LimitNode(ligretoNode);
					if (getAttributeValue(atts, "enabled") != null) {
						limit.setEnabled(getAttributeValue(atts, "enabled"));
					}
					if (getAttributeValue(atts, "columns") != null) {
						limit.setColumns(getAttributeValue(atts, "columns"));
					}
					if (getAttributeValue(atts, "rel-diff-value") != null) {
						limit.setRelativeDifference(getAttributeValue(atts, "rel-diff-value"));
					}
					if (getAttributeValue(atts, "abs-diff-value") != null) {
						limit.setAbsoluteDifference(getAttributeValue(atts, "abs-diff-value"));
					}
					if (getAttributeValue(atts, "rel-diff-count") != null) {
						limit.setRelativeCount(getAttributeValue(atts, "rel-diff-count"));
					}
					if (getAttributeValue(atts, "abs-diff-count") != null) {
						limit.setAbsoluteCount(getAttributeValue(atts, "abs-diff-count"));
					}
					result.addLimitNode(limit);
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
					log.debug("Parser error on node: \"" + localName + "\"; parser state: "
						+ (objectStack.empty() ? ObjectType.NONE : objectStack.peek())
					);
					log.debug("Parser state stack:");
					for (int i=0; i < objectStack.size(); i++) {
						log.debug("Depth: " + i + "; State: " + objectStack.get(objectStack.size() - 1 - i));
					}
					objectStack.push(ObjectType.NONE);
				}
				break;
			}
			if (objectStack.size() != entryStackDepth + 1 && log.isDebugEnabled()) {
				log.debug("Parser error on node: \"" + localName + "\"; parser state: "
						+ (objectStack.empty() ? ObjectType.NONE : objectStack.peek())
					);
				log.debug("Parser state stack:");
				for (int i=0; i < objectStack.size(); i++) {
					log.debug("Depth: " + i + "; State: " + objectStack.get(objectStack.size() - 1 - i));
				}
			}
		} catch (InvalidFormatException e) {
			throw new SAXException("Invalid format specified.", e);
		} catch (InvalidValueException e) {
			throw new SAXException("Wrong value specified.", e);
		} catch (ParserException e) {
			throw new SAXException("Error parsing file.", e);
		}
	}

	@Override
	public void startPrefixMapping(String arg0, String arg1) throws SAXException {
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		log.error("---");
		log.error(e.getMessage());
		log.error("Parser error occurred while processing");
		log.error("url: '" + e.getSystemId() + "'");
		log.error("line: " + e.getLineNumber() + "; column: " + e.getColumnNumber());
		throw e;
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		log.error("---");
		log.error(e.getMessage());
		log.error("Parser error occurred while processing");
		log.error("url: '" + e.getSystemId() + "'");
		log.error("line: " + e.getLineNumber() + "; column: " + e.getColumnNumber());
		throw e;
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		log.warn("---");
		log.warn(e.getMessage());
		log.warn("Parser warning occurred while processing");
		log.warn("url: '" + e.getSystemId() + "'");
		log.warn("line: " + e.getLineNumber() + "; column: " + e.getColumnNumber());
	}

	@Override
	public void notationDecl(String arg0, String arg1, String arg2) throws SAXException {
	}

	@Override
	public void unparsedEntityDecl(String arg0, String arg1, String arg2, String arg3) throws SAXException {
	}

}
