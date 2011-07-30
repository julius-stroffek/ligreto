package net.ligreto.config.nodes;

public class TransferNode extends Node {

	protected TargetNode targetNode;
	protected SqlNode sqlNode;
	
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

}
