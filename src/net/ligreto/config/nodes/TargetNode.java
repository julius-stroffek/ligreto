package net.ligreto.config.nodes;

public class TargetNode extends Node {

	protected String table;
	protected String dataSource;
	protected boolean create = false;
	protected boolean truncate = false;
	
	public TargetNode(LigretoNode aLigretoNode) {
		super(aLigretoNode);
	}

	/**
	 * @return the table
	 */
	public String getTable() {
		return table;
	}

	/**
	 * @param table the table to set
	 */
	public void setTable(String table) {
		this.table = table;
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
	 * @return the create
	 */
	public boolean isCreate() {
		return create;
	}

	/**
	 * @param create the create to set
	 */
	public void setCreate(boolean create) {
		this.create = create;
	}

	/**
	 * @return the truncate
	 */
	public boolean isTruncate() {
		return truncate;
	}

	/**
	 * @param truncate the truncate to set
	 */
	public void setTruncate(boolean truncate) {
		this.truncate = truncate;
	}

	/**
	 * Parses the string value into <code>create</code> field
	 * @param create The string value to parse
	 */
	public void setCreate(String create) {
		this.create = Boolean.parseBoolean(create);	
	}

	/**
	 * Parses the string value into <code>truncate</code> field
	 * @param truncate The string value to parse
	 */
	public void setTruncate(String truncate) {
		this.truncate = Boolean.parseBoolean(truncate);	
	}

}
