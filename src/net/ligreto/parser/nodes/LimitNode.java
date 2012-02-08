package net.ligreto.parser.nodes;

import net.ligreto.util.MiscUtils;

public class LimitNode extends Node {
	
	protected String columns = null;
	protected Double relativeDifference = null;
	protected Double absoluteDifference = null;
	protected Double relativeCount = null;
	protected Double absoluteCount = null;

	public LimitNode(LigretoNode aLigretoNode) {
		super(aLigretoNode);
	}

	/**
	 * @return the columns
	 */
	public int[] getColumns() {
		if (columns == null) {
			return new int[0];
		}
		String[] sValues = ligretoNode.substituteParams(columns).split(",");
		int values[] = new int[sValues.length];
		for (int i=0; i < values.length; i++) {
			values[i] = Integer.parseInt(sValues[i]);
		}
		return values;
	}

	/**
	 * @param columns the columns to set
	 */
	public void setColumns(String columns) {
		this.columns = columns;
	}

	/**
	 * @return the relativeDifference
	 */
	public Double getRelativeDifference() {
		return relativeDifference;
	}

	/**
	 * @param relativeDifference the relativeDifference to set
	 */
	public void setRelativeDifference(Double relativeDifference) {
		this.relativeDifference = relativeDifference;
	}

	/**
	 * @param relativeDifference the relativeDifference to set
	 */
	public void setRelativeDifference(String relativeDifference) {
		this.relativeDifference = MiscUtils.parseDoublePercentage(relativeDifference);
	}

	/**
	 * @return the absoluteDifference
	 */
	public Double getAbsoluteDifference() {
		return absoluteDifference;
	}

	/**
	 * @param absoluteDifference the absoluteDifference to set
	 */
	public void setAbsoluteDifference(Double absoluteDifference) {
		this.absoluteDifference = absoluteDifference;
	}

	/**
	 * @param absoluteDifference the absoluteDifference to set
	 */
	public void setAbsoluteDifference(String absoluteDifference) {
		this.absoluteDifference = MiscUtils.parseDouble(absoluteDifference);
	}

	/**
	 * @return the relativeCount
	 */
	public Double getRelativeCount() {
		return relativeCount;
	}

	/**
	 * @param relativeCount the relativeCount to set
	 */
	public void setRelativeCount(Double relativeCount) {
		this.relativeCount = relativeCount;
	}

	/**
	 * @param relativeCount the relativeCount to set
	 */
	public void setRelativeCount(String relativeCount) {
		this.relativeCount = MiscUtils.parseDoublePercentage(relativeCount);
	}

	/**
	 * @return the absoluteCount
	 */
	public Double getAbsoluteCount() {
		return absoluteCount;
	}

	/**
	 * @param absoluteCount the absoluteCount to set
	 */
	public void setAbsoluteCount(Double absoluteCount) {
		this.absoluteCount = absoluteCount;
	}

	/**
	 * @param absoluteCount the absoluteCount to set
	 */
	public void setAbsoluteCount(String absoluteCount) {
		this.absoluteCount = MiscUtils.parseDouble(absoluteCount);
	}

}
