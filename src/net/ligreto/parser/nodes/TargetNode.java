package net.ligreto.parser.nodes;

import net.ligreto.exceptions.InvalidValueException;
import net.ligreto.util.MiscUtils;

public class TargetNode extends Node {

	protected final TransferNode transferNode;
	protected String table;
	protected String dataSource;
	protected boolean create = false;
	protected boolean recreate = false;
	protected boolean truncate = false;
	protected int commitInterval = 1;
	
	public TargetNode(LigretoNode aLigretoNode, TransferNode aTransferNode) {
		super(aLigretoNode);
		transferNode = aTransferNode;
	}

	/**
	 * @return the transferNode
	 */
	public TransferNode getTransferNode() {
		return transferNode;
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
	 * @return the recreate
	 */
	public boolean isRecreate() {
		return recreate;
	}

	/**
	 * @param recreate the recreate to set
	 */
	public void setRecreate(boolean recreate) {
		this.recreate = recreate;
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
	 * @throws InvalidValueException 
	 */
	public void setCreate(String create) throws InvalidValueException {
		this.create = MiscUtils.parseBoolean(create);	
	}

	/**
	 * Parses the string value into <code>truncate</code> field
	 * @param recreate The string value to parse
	 * @throws InvalidValueException 
	 */
	public void setRecreate(String recreate) throws InvalidValueException {
		this.recreate = MiscUtils.parseBoolean(recreate);
	}

	/**
	 * Parses the string value into <code>truncate</code> field
	 * @param truncate The string value to parse
	 * @throws InvalidValueException 
	 */
	public void setTruncate(String truncate) throws InvalidValueException {
		this.truncate = MiscUtils.parseBoolean(truncate);	
	}

	/**
	 * @return the commitInterval
	 */
	public int getCommitInterval() {
		return commitInterval;
	}

	/**
	 * @param commitInterval the commit interval to set
	 */
	public void setCommitInterval(int commitInterval) {
		this.commitInterval = commitInterval;
	}

	/**
	 * @param commitInterval the commit interval to parse and set
	 */
	public void setCommitInterval(String commitInterval) {
		this.commitInterval = Integer.parseInt(commitInterval);
	}

}
