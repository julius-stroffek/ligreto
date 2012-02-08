package net.ligreto.parser.nodes;

import net.ligreto.util.MiscUtils;

public class RowLimitNode extends Node {

	protected Double absoluteDifference = null;
	protected Double relativeDifference = null;
	protected Double absoluteNonMatched = null;
	protected Double relativeNonMatched = null;

	public RowLimitNode(LigretoNode aLigretoNode) {
		super(aLigretoNode);
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
	 * @return the absoluteNonMatched
	 */
	public Double getAbsoluteNonMatched() {
		return absoluteNonMatched;
	}

	/**
	 * @param absoluteNonMatched the absoluteNonMatched to set
	 */
	public void setAbsoluteNonMatched(Double absoluteNonMatched) {
		this.absoluteNonMatched = absoluteNonMatched;
	}

	/**
	 * @param absoluteNonMatched the absoluteNonMatched to set
	 */
	public void setAbsoluteNonMatched(String absoluteNonMatched) {
		this.absoluteNonMatched = MiscUtils.parseDouble(absoluteNonMatched);
	}

	/**
	 * @return the relativeNonMatched
	 */
	public Double getRelativeNonMatched() {
		return relativeNonMatched;
	}

	/**
	 * @param relativeNonMatched the relativeNonMatched to set
	 */
	public void setRelativeNonMatched(Double relativeNonMatched) {
		this.relativeNonMatched = relativeNonMatched;
	}
	
	/**
	 * @param relativeNonMatched the relativeNonMatched to set
	 */
	public void setRelativeNonMatched(String relativeNonMatched) {
		this.relativeNonMatched = MiscUtils.parseDoublePercentage(relativeNonMatched);
	}

}
