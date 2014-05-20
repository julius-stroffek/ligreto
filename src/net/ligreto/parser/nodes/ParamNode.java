/**
 * 
 */
package net.ligreto.parser.nodes;

import net.ligreto.exceptions.LigretoException;

/**
 * @author Julius Stroffek
 *
 */
public class ParamNode extends Node {
	protected StringBuilder query = new StringBuilder();
	protected String paramName;
	protected String queryName;
	protected String dataSource;
	protected Attitude exceptions = Attitude.FAIL;
	protected QueryType queryType = QueryType.STATEMENT;
	protected int orderNumber;
	
	/** The type of the query which determines the way it is executed. */
	public enum QueryType {STATEMENT, CALL};
	
	/** Constructs SQL node. */
	public ParamNode(LigretoNode ligretoNode) {
		super(ligretoNode);
	}
	
	/**
	 * @return the paramName
	 * @link #paramName
	 */
	public String getParamName() {
		return paramName;
	}

	/**
	 * @param paramName the paramName to set
	 * @link #paramName
	 */
	public void setParamName(String paramName) {
		this.paramName = paramName;
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
