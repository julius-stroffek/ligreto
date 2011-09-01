/**
 * 
 */
package net.ligreto.parser.nodes;

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
	protected int[] on;
	protected String[] exclude;
	
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
	 */
	public String getQuery() {
		String result = queryName != null ? ligretoNode.getQuery(queryName) : query.toString();
		String oResult;
		do {
			oResult = result;			
			result = ligretoNode.substituteParams(oResult);
		} while (!result.equals(oResult)); 
		
		return result;
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
	 */
	public void setHeader(String header) {
		this.header = Boolean.parseBoolean(header);
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

	/**
	 * @param append the append to set
	 */
	public void setAppend(String append) {
		this.append = Boolean.parseBoolean(append);
	}

	/**
	 * 
	 * @param on the comma separated list of column indices to be used for join condition
	 */
	public void setOn(String on) {
		String[] ons = on.split(",");
		this.on = new int[ons.length];
		for (int i=0; i < ons.length; i++) {
			this.on[i] = Integer.parseInt(ons[i]);
		}
	}

	/**
	 * @return the array of column indices to be used for join condition
	 */
	public int[] getOn() {
		return on;
	}

	/**
	 * @return the exclude
	 */
	public String[] getExclude() {
		return exclude;
	}
	
	/**
	 * @param exclude the comma separated list of column names to be ignored in comparison & output
	 */
	public void setExclude(String exclude) {
		this.exclude = exclude.split(",");
	}
	
}
