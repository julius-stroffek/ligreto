package net.ligreto.parser.nodes;

import net.ligreto.exceptions.InvalidValueException;
import net.ligreto.util.MiscUtils;

public class Node {	
	protected boolean enabled = true;
	protected LigretoNode ligretoNode;
	
	/** The actions that should be taken in certain situations. */
	public enum Attitude {IGNORE, DUMP, FAIL};
	
	protected Node(LigretoNode aLigretoNode) {
		ligretoNode = aLigretoNode;
	}
	
	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @param enabled the enabled to set
	 * @throws InvalidValueException 
	 */
	public void setEnabled(String enabled) throws InvalidValueException {
		this.enabled = MiscUtils.parseBoolean(enabled);
	}
	
	/**
	 * @return the ligretoNode
	 */
	public LigretoNode getLigretoNode() {
		return ligretoNode;
	}

	/**
	 * @param ligretoNode the ligretoNode to set
	 */
	public void setLigretoNode(LigretoNode ligretoNode) {
		this.ligretoNode = ligretoNode;
	}

}
