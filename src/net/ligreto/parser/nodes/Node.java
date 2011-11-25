package net.ligreto.parser.nodes;

public class Node {	
	protected LigretoNode ligretoNode;
	
	/** The actions that should be taken in certain situations. */
	public enum Attitude {IGNORE, DUMP, FAIL};
	
	protected Node(LigretoNode aLigretoNode) {
		ligretoNode = aLigretoNode;
	}
	
	public LigretoNode getLigretoNode() {
		return ligretoNode;
	}
	
	public void setLigretoNode(LigretoNode ligretoNode) {
		this.ligretoNode = ligretoNode;
	}
}
