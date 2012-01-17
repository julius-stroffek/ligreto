package net.ligreto.parser.nodes;

import net.ligreto.exceptions.InvalidValueException;
import net.ligreto.util.MiscUtils;

public class TransferNode extends Node {

	protected TargetNode targetNode;
	protected SqlNode sqlNode;
	protected boolean result;
	
	public TransferNode(LigretoNode aLigretoNode) {
		super(aLigretoNode);
	}

	/**
	 * @return the targetNode
	 */
	public TargetNode getTargetNode() {
		return targetNode;
	}

	/**
	 * @param targetNode the targetNode to set
	 */
	public void setTargetNode(TargetNode targetNode) {
		this.targetNode = targetNode;
	}

	/**
	 * @return the sqlNode
	 */
	public SqlNode getSqlNode() {
		return sqlNode;
	}

	/**
	 * @param sqlNode the sqlNode to set
	 */
	public void setSqlNode(SqlNode sqlNode) {
		this.sqlNode = sqlNode;
	}
	
	/**
	 * @return the result
	 */
	public boolean getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(boolean result) {
		this.result = result;
	}

	/**
	 * @param result the result to set parsed to boolean
	 * @throws InvalidValueException 
	 */
	public void setResult(String result) throws InvalidValueException {
		if (result != null) {
			this.result = MiscUtils.parseBoolean(result);
		} else {
			this.result = false;
		}
	}
}
