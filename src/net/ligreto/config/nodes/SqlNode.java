/**
 * 
 */
package net.ligreto.config.nodes;

/**
 * @author Julius Stroffek
 *
 */
public class SqlNode {
	protected StringBuilder query = new StringBuilder();
	protected String queryName;
	protected String dataSource;
	protected String target;
	protected int[] on;
	
	/** Constructs SQL node. */
	public SqlNode() {
	}
	
	/**
	 * @return the name
	 */
	public String getQueryName() {
		return queryName;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}
	
	/**
	 * @return the content
	 */
	public StringBuilder getQuery() {
		return query;
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

}
