/**
 * 
 */
package net.ligreto.parser.nodes;

import net.ligreto.exceptions.InvalidFormatException;
import net.ligreto.exceptions.InvalidValueException;
import net.ligreto.util.MiscUtils;

/**
 * @author Julius Stroffek
 *
 */
public class LayoutNode extends Node {
	public enum JoinType {FULL, LEFT, RIGHT, INNER, COMPLEMENT, LEFT_COMPLEMENT, RIGHT_COMPLEMENT};
	public enum LayoutType {NORMAL, INTERLACED, DETAILED, AGGREGATED, KEY, SUMMARY};
	
	protected LayoutType layoutType = LayoutType.INTERLACED;
	protected JoinType joinType = JoinType.FULL;
	protected String target;
	protected short[] rgbHlColor;
	protected boolean diffs = false;
	protected boolean highlight = false;
	protected boolean header = false;
	protected boolean append = false;
	protected boolean result = true;
	protected String groupBy;
//	protected ReportNode reportNode;
	
	public LayoutNode(LigretoNode ligretoNode) {
		super(ligretoNode);
	}

	/**
	 * @return the joinLayoutType
	 */
	public LayoutType getType() {
		return layoutType;
	}

	/**
	 * @param layoutType the layoutType to set
	 */
	public void setType(LayoutType layoutType) {
		this.layoutType = layoutType;
	}

	/**
	 * @param layoutType the LayoutType to set as string
	 */
	public void setType(String layoutType) {
		if ("normal".equals(layoutType))
			this.layoutType = LayoutType.NORMAL;
		else if ("interlaced".equals(layoutType))
			this.layoutType = LayoutType.INTERLACED;
		else if ("detailed".equals(layoutType))
			this.layoutType = LayoutType.DETAILED;			
		else if ("aggregated".equals(layoutType))
			this.layoutType = LayoutType.AGGREGATED;			
		else if ("key".equals(layoutType))
			this.layoutType = LayoutType.KEY;			
		else if ("summary".equals(layoutType))
			this.layoutType = LayoutType.SUMMARY;			
		else
			throw new IllegalArgumentException("The join layout could not be \"" + layoutType + "\"");
	}
	
	/**
	 * @return the joinType
	 */
	public JoinType getJoinType() {
		return joinType;
	}

	/**
	 * @param joinType the joinType to set
	 */
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}

	/**
	 * @param joinType the joinType to set as string
	 */
	public void setJoinType(String joinType) {
		if ("full".equals(joinType))
			this.joinType = JoinType.FULL;
		else if ("left".equals(joinType))
			this.joinType = JoinType.LEFT;
		else if ("right".equals(joinType))
			this.joinType = JoinType.RIGHT;
		else if ("inner".equals(joinType))
			this.joinType = JoinType.INNER;
		else if ("complement".equals(joinType))
			this.joinType = JoinType.COMPLEMENT;
		else if ("left complement".equals(joinType))
			this.joinType = JoinType.LEFT_COMPLEMENT;
		else if ("right complement".equals(joinType))
			this.joinType = JoinType.RIGHT_COMPLEMENT;
		else
			throw new IllegalArgumentException("The join type could not be \"" + joinType + "\"");
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * @return the hlColor
	 */
	public short[] getHlColor() {
		return rgbHlColor;
	}

	/**
	 * @param rgbHlColor The RGB highlight color to set.
	 */
	public void setHlColor(short[] rgbHlColor) {
		this.rgbHlColor = rgbHlColor;
	}

	/**
	 * @param rgbHlColor The RGB highlight color to set.
	 * @throws InvalidFormatException 
	 */
	public void setHlColor(String rgbHlColor) throws InvalidFormatException {
		this.rgbHlColor = MiscUtils.parseRGB(rgbHlColor);
	}

	/**
	 * @return the diffs
	 */
	public boolean getDiffs() {
		return diffs;
	}

	/**
	 * @param diffs the diffs to set
	 */
	public void setDiffs(boolean diffs) {
		this.diffs = diffs;
	}

	/**
	 * @param diffs the diffs to set
	 * @throws InvalidValueException 
	 */
	public void setDiffs(String diffs) throws InvalidValueException {
		this.diffs = MiscUtils.parseBoolean(diffs);
	}

	/**
	 * @return the highlight
	 */
	public boolean getHighlight() {
		return highlight;
	}

	/**
	 * @param highlight the highlight to set
	 */
	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}

	/**
	 * @param highlight the highlight to set
	 * @throws InvalidValueException 
	 */
	public void setHighlight(String highlight) throws InvalidValueException {
		this.highlight = MiscUtils.parseBoolean(highlight);
	}

	/**
	 * @return the header
	 */
	public boolean getHeader() {
		return header;
	}
	
	/**
	 * @param header the header to set
	 */
	public void setHeader(boolean header) {
		this.header = header;
	}

	/**
	 * @param header the header to set
	 * @throws InvalidValueException 
	 */
	public void setHeader(String header) throws InvalidValueException {
		this.header = MiscUtils.parseBoolean(header);
	}

	/**
	 * @return the append
	 */
	public boolean isAppend() {
		return append;
	}
	/**
	 * @param append the append to set
	 */
	public void setAppend(boolean append) {
		this.append = append;
	}

	/**
	 * @param append the append to set which indicates whether the newly added
	 *        data should be appended to the target location.
	 * @throws InvalidValueException 
	 */
	public void setAppend(String append) throws InvalidValueException {
		this.append = MiscUtils.parseBoolean(append);
	}

	/**
	 * 
	 * @param groupBy the comma separated list of column indices to be used for aggregating results
	 */
	public void setGroupBy(String groupBy) {
		// If the list is empty, we will always store null
		if (groupBy != null) {
			this.groupBy = groupBy.trim();
		} else {
			this.groupBy = null;
		}
		if ("".equals(groupBy))
			this.groupBy = null;
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
	
	/**
	 * @return the array of column indices to be used for aggregation of results
	 */
	public int[] getGroupBy() {
		if (groupBy == null)
			return null;
		String[] sValues = ligretoNode.substituteParams(groupBy).split(",");
		int values[] = new int[sValues.length];
		for (int i=0; i < values.length; i++) {
			values[i] = Integer.parseInt(sValues[i]);
		}
		return values;
	}
		
}
