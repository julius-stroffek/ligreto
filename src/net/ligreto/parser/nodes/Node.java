package net.ligreto.parser.nodes;

public class Node {	
	protected LigretoNode ligretoNode;
	
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
