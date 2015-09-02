/**
 * 
 */
package net.ligreto.parser.nodes;

import net.ligreto.exceptions.InvalidValueException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.util.MiscUtils;

/**
 * @author Julius Stroffek
 *
 */
public class SqlNode extends Node {
	protected StringBuilder query = new StringBuilder();
	protected String queryName;
	protected String dataSource;
	protected String target;
	protected boolean header;
	protected boolean append;
	protected boolean result;
	protected String key;
	protected String columns;
	protected String exclude;
	protected Attitude exceptions = Attitude.FAIL;
	protected QueryType queryType = QueryType.STATEMENT;
	protected int orderNumber;
	
	/** The type of the query which determines the way it is executed. */
	public enum QueryType {STATEMENT, CALL, QUERY};
	
	/** Constructs SQL node. */
	public SqlNode(LigretoNode ligretoNode) {
		super(ligretoNode);
	}
	
	/**
	 * @return the name
	 */
	public String getQueryName() {
		return queryName;
	}
	
	/**
	 * @param queryName the query name to set
	 */
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}
	
	/**
	 * @return the content
	 */
	public StringBuilder getQueryBuilder() {
		return query;
	}
	
	/**
	 * @return The query with all the substitutions already done.
	 * @throws LigretoException 
	 */
	public String getQuery() throws LigretoException {
		String result = queryName != null ? ligretoNode.getQuery(queryName) : "";
		result += query.toString();		
		return ligretoNode.substituteParams(result);
	}
	
	/**
	 * @return the dataSource
	 */
	public String getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
	
	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}
	
	/**
	 * @return the header
	 */
	public boolean getHeader() {
		return header;
	}
	
	/**
	 * @param header the header to set
	 */
	public void setHeader(boolean header) {
		this.header = header;
	}

	/**
	 * @param header the header to set
	 * @throws InvalidValueException 
	 */
	public void setHeader(String header) throws InvalidValueException {
		this.header = MiscUtils.parseBoolean(header);
	}

	/**
	 * @return the append
	 */
	public boolean isAppend() {
		return append;
	}

	/**
	 * @param append the append to set
	 */
	public void setAppend(boolean append) {
		this.append = append;
	}

	public void setAppend(String append) throws InvalidValueException {
		this.append = MiscUtils.parseBoolean(append);
	}

	public boolean getResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public void setResult(String result) throws InvalidValueException {
		if (result != null) {
			this.result = MiscUtils.parseBoolean(result);
		} else {
			this.result = false;
		}
	}

	/**
	 * 
	 * @param key the comma separated list of column indices to be used for join condition
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the array of column indices to be used for join condition
	 */
	public int[] getKey() {
		if (key == null)
			return null;
		String[] skey = ligretoNode.substituteParams(key).split(",");
		int ikey[] = new int[skey.length];
		for (int i=0; i < ikey.length; i++) {
			ikey[i] = Integer.parseInt(skey[i].trim());
		}
		return ikey;
	}

	/**
	 * @return the columns
	 */
	public int[] getColumns() {
		if (columns == null)
			return null;
		String[] scols = ligretoNode.substituteParams(columns).split(",");
		int icols[] = new int[scols.length];
		for (int i=0; i < icols.length; i++) {
			icols[i] = Integer.parseInt(scols[i].trim());
		}
		return icols;
	}

	/**
	 * @param columns the columns to set
	 */
	public void setColumns(String columns) {
		this.columns = columns;
	}

	/**
	 * @return the exclude
	 */
	public String getExclude() {
		return exclude;
	}
	
	/**
	 * @param exclude the comma separated list of column names to be ignored in comparison & output
	 */
	public void setExclude(String exclude) {
		this.exclude = exclude;
	}
	
	/** @return the split column names to be ignored. */
	public String[] getExcludeColumns() {
		String[] retValue;
		if (exclude != null) {
			retValue = ligretoNode.substituteParams(exclude).split(",");
			for (int i=0; i < retValue.length; i++)
				retValue[i] = retValue[i].trim();
			return retValue;
		} else {
			return null;
		}
	}

	public Attitude getExceptions() {
		return exceptions;
	}

	public void setExceptions(Attitude exceptions) {
		this.exceptions = exceptions;
	}
	
	public void setExceptions(String exceptions) {
		if ("ignore".equals(exceptions)) {
			this.exceptions = Attitude.IGNORE;
		} else if ("dump".equals(exceptions)) {
			this.exceptions = Attitude.DUMP;
		} else if ("fail".equals(exceptions)) {
			this.exceptions = Attitude.FAIL;
		} else {
			throw new IllegalArgumentException("Wrong value specified as attitude in case of SQL errors: " + exceptions);
		}
	}

	public QueryType getQueryType() {
		return queryType;
	}

	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}

	public void setQueryType(String queryType) {
		if ("statement".equals(queryType)) {
			this.queryType = QueryType.STATEMENT;			
		} else if ("query".equals(queryType)) {
			this.queryType = QueryType.QUERY;
		} else if ("call".equals(queryType)) {
			this.queryType = QueryType.CALL;
		} else {
			throw new IllegalArgumentException("Wrong value specified as query type: " + queryType);
		}
	}

	/**
	 * @return the orderNumber
	 */
	public int getOrderNumber() {
		return orderNumber;
	}

	/**
	 * @param orderNumber the orderNumber to set
	 */
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}
}
