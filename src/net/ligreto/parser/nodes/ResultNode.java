package net.ligreto.parser.nodes;

import java.util.ArrayList;
import java.util.List;

import net.ligreto.exceptions.ParserException;

public class ResultNode extends Node {

	protected RowLimitNode rowLimitNode = null;
	protected List<LimitNode> limitNodes = new ArrayList<LimitNode>();
	
	public ResultNode(LigretoNode aLigretoNode) {
		super(aLigretoNode);
	}

	/**
	 * @return the rowLimitNode
	 */
	public RowLimitNode getRowLimitNode() {
		return rowLimitNode;
	}

	/**
	 * @param rowLimitNode the rowLimitNode to set
	 * @throws ParserException 
	 */
	public void setRowLimitNode(RowLimitNode rowLimitNode) throws ParserException {
		if (this.rowLimitNode != null) {
			throw new ParserException("Row limit could be specified only once in <result> node.");
		}
		this.rowLimitNode = rowLimitNode;
	}

	/**
	 * @return the limitNodes
	 */
	public List<LimitNode> getLimitNodes() {
		return limitNodes;
	}

	/**
	 * @param limitNode the limitNode to add to the limit nodes
	 */
	public void addLimitNode(LimitNode limitNode) {
		limitNodes.add(limitNode);
	}

}
