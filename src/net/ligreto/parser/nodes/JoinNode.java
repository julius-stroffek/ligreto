/**
 * 
 */
package net.ligreto.parser.nodes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Julius Stroffek
 *
 */
public class JoinNode extends Node {
	public enum JoinType {FULL, LEFT, RIGHT, INNER};
	protected JoinType joinType = JoinType.FULL;
	protected String target;
	protected String hlColor;
	protected boolean diffs = false;
	protected boolean interlaced = false;
	protected boolean highlight = false;
	protected boolean header = false;
	protected boolean append = false;
	protected List<SqlNode> sqlQueries = new ArrayList<SqlNode>();
	protected int[] on;
	
	public JoinNode(LigretoNode ligretoNode) {
		super(ligretoNode);

	}
	public void addSql(SqlNode sql) {
		sqlQueries.add(sql);
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
	 * @param joinType the joinType to set
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
	public String getHlColor() {
		return hlColor;
	}

	/**
	 * @param hlColor the hlColor to set
	 */
	public void setHlColor(String hlColor) {
		this.hlColor = hlColor;
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
	 */
	public void setDiffs(String diffs) {
		this.diffs = Boolean.parseBoolean(diffs);
	}

	/**
	 * @return the interlaced
	 */
	public boolean getInterlaced() {
		return interlaced;
	}

	/**
	 * @param interlaced the interlaced to set
	 */
	public void setInterlaced(boolean interlaced) {
		this.interlaced = interlaced;
	}

	/**
	 * @param interlaced the interlaced to set
	 */
	public void setInterlaced(String interlaced) {
		this.interlaced = Boolean.parseBoolean(interlaced);
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
	 */
	public void setHighlight(String highlight) {
		this.highlight = Boolean.parseBoolean(highlight);
	}

	/**
	 * @return the sqlQueries
	 */
	public List<SqlNode> getSqlQueries() {
		return sqlQueries;
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
	 */
	public void setHeader(String header) {
		this.header = Boolean.parseBoolean(header);
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
	 */
	public void setAppend(String append) {
		this.append = Boolean.parseBoolean(append);
	}

	/**
	 * 
	 * @param on the comma separated list of column indices to be used for join condition
	 */
	public void setOn(String on) {
		String[] ons = on.split(",");
		this.on = new int[ons.length];
		for (int i=0; i < ons.length; i++) {
			this.on[i] = Integer.parseInt(ons[i]);
		}
	}

	/**
	 * @return the array of column indices to be used for join condition
	 */
	public int[] getOn() {
		return on;
	}
}
