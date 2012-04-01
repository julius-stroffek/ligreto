package net.ligreto.parser.nodes;

import net.ligreto.util.MiscUtils;

public class RowLimitNode extends Node {

	protected Long totalRows = null;
	protected Long absoluteDifference = null;
	protected Double relativeDifference = null;
	protected Long absoluteNonMatched = null;
	protected Double relativeNonMatched = null;
	
	protected Long absoluteMatched = null;
	protected Double relativeMatched = null;
	protected Long absoluteEqual = null;
	protected Double relativeEqual = null;
	
	public RowLimitNode(LigretoNode aLigretoNode) {
		super(aLigretoNode);
	}

	/**
	 * @return the absoluteDifference
	 */
	public Long getAbsoluteDifference() {
		return absoluteDifference;
	}

	/**
	 * @param absoluteDifference the absoluteDifference to set
	 */
	public void setAbsoluteDifference(Long absoluteDifference) {
		this.absoluteDifference = absoluteDifference;
	}

	/**
	 * @param absoluteDifference the absoluteDifference to set
	 */
	public void setAbsoluteDifference(String absoluteDifference) {
		this.absoluteDifference = MiscUtils.parseLong(absoluteDifference);
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
	public Long getAbsoluteNonMatched() {
		return absoluteNonMatched;
	}

	/**
	 * @param absoluteNonMatched the absoluteNonMatched to set
	 */
	public void setAbsoluteNonMatched(Long absoluteNonMatched) {
		this.absoluteNonMatched = absoluteNonMatched;
	}

	/**
	 * @param absoluteNonMatched the absoluteNonMatched to set
	 */
	public void setAbsoluteNonMatched(String absoluteNonMatched) {
		this.absoluteNonMatched = MiscUtils.parseLong(absoluteNonMatched);
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

	/**
	 * @return the totalRows
	 */
	public Long getTotalRows() {
		return totalRows;
	}

	/**
	 * @param totalRows the totalRows to set
	 */
	public void setTotalRows(Long totalRows) {
		this.totalRows = totalRows;
	}

	/**
	 * @param totalRows the totalRows to set
	 */
	public void setTotalRows(String totalRows) {
		this.totalRows = MiscUtils.parseLong(totalRows);
	}

	/**
	 * @return the absoluteMatched
	 */
	public Long getAbsoluteMatched() {
		return absoluteMatched;
	}

	/**
	 * @param absoluteMatched the absoluteMatched to set
	 */
	public void setAbsoluteMatched(Long absoluteMatched) {
		this.absoluteMatched = absoluteMatched;
	}

	/**
	 * @param absoluteMatched the absoluteMatched to set
	 */
	public void setAbsoluteMatched(String absoluteMatched) {
		this.absoluteMatched = MiscUtils.parseLong(absoluteMatched);
	}

	/**
	 * @return the relativeMatched
	 */
	public Double getRelativeMatched() {
		return relativeMatched;
	}

	/**
	 * @param relativeMatched the relativeMatched to set
	 */
	public void setRelativeMatched(Double relativeMatched) {
		this.relativeMatched = relativeMatched;
	}

	/**
	 * @param relativeMatched the relativeMatched to set
	 */
	public void setRelativeMatched(String relativeMatched) {
		this.relativeMatched = MiscUtils.parseDoublePercentage(relativeMatched);
	}

	/**
	 * @return the absoluteEqual
	 */
	public Long getAbsoluteEqual() {
		return absoluteEqual;
	}

	/**
	 * @param absoluteEqual the absoluteEqual to set
	 */
	public void setAbsoluteEqual(Long absoluteEqual) {
		this.absoluteEqual = absoluteEqual;
	}

	/**
	 * @param absoluteEqual the absoluteEqual to set
	 */
	public void setAbsoluteEqual(String absoluteEqual) {
		this.absoluteEqual = MiscUtils.parseLong(absoluteEqual);
	}

	/**
	 * @return the relativeEqual
	 */
	public Double getRelativeEqual() {
		return relativeEqual;
	}

	/**
	 * @param relativeEqual the relativeEqual to set
	 */
	public void setRelativeEqual(Double relativeEqual) {
		this.relativeEqual = relativeEqual;
	}

	/**
	 * @param relativeEqual the relativeEqual to set
	 */
	public void setRelativeEqual(String relativeEqual) {
		this.relativeEqual = MiscUtils.parseDoublePercentage(relativeEqual);
	}
}
