/**
 * 
 */
package net.ligreto.parser.nodes;

import java.util.ArrayList;
import java.util.List;

import net.ligreto.exceptions.InvalidFormatException;
import net.ligreto.util.MiscUtils;

/**
 * @author Julius Stroffek
 *
 */
public class JoinNode extends Node {
	public enum JoinType {FULL, LEFT, RIGHT, INNER};
	public enum JoinLayoutType {NORMAL, INTERLACED, DETAILED};
	
	protected JoinType joinType = JoinType.FULL;
	protected JoinLayoutType joinLayoutType = JoinLayoutType.INTERLACED;
	protected String target;
	protected short[] rgbHlColor;
	protected boolean diffs = false;
	protected boolean highlight = false;
	protected boolean header = false;
	protected boolean append = false;
	protected boolean result = true;
	protected List<SqlNode> sqlQueries = new ArrayList<SqlNode>();
	protected String on;
	protected String exclude;
	protected String locale;
	protected ReportNode reportNode;
	protected Attitude collation = Attitude.FAIL;
	
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
		else
			throw new IllegalArgumentException("The join type could not be \"" + joinType + "\"");
	}

	/**
	 * @return the joinLayoutType
	 */
	public JoinLayoutType getJoinLayoutType() {
		return joinLayoutType;
	}

	/**
	 * @param joinLayoutType the joinLayoutType to set
	 */
	public void setJoinLayoutType(JoinLayoutType joinLayoutType) {
		this.joinLayoutType = joinLayoutType;
	}

	/**
	 * @param joinLayout the joinLayout to set as string
	 */
	public void setJoinLayoutType(String joinLayoutType) {
		if ("normal".equals(joinLayoutType))
			this.joinLayoutType = JoinLayoutType.NORMAL;
		else if ("interlaced".equals(joinLayoutType))
			this.joinLayoutType = JoinLayoutType.INTERLACED;
		else if ("detailed".equals(joinLayoutType))
			this.joinLayoutType = JoinLayoutType.DETAILED;			
		else
			throw new IllegalArgumentException("The join layout could not be \"" + joinLayoutType + "\"");
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
	 * @param hlColor the hlColor to set
	 */
	public void setHlColor(short[] rgbHlColor) {
		this.rgbHlColor = rgbHlColor;
	}

	/**
	 * @param hlColor the hlColor to set
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
	 */
	public void setDiffs(String diffs) {
		this.diffs = Boolean.parseBoolean(diffs);
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
		this.on = on;
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
	 */
	public void setResult(String result) {
		if (result != null) {
			this.result = Boolean.parseBoolean(result);
		} else {
			this.result = false;
		}
	}

	/**
	 * @return the array of column indices to be used for join condition
	 */
	public int[] getOn() {
		if (on == null)
			return null;
		String[] ons = ligretoNode.substituteParams(on).split(",");
		int onn[] = new int[ons.length];
		for (int i=0; i < onn.length; i++) {
			onn[i] = Integer.parseInt(ons[i]);
		}
		return onn;
	}
	
	/**
	 * @return the exclude
	 */
	public String getExclude() {
		return exclude;
	}
	
	/**
	 * @param exclude the comma separated list of column names to be ignored in comparison & output
	 */
	public void setExclude(String exclude) {
		this.exclude = exclude;
	}
	
	/** @return the split column names to be ignored. */
	public String[] getExcludeColumns() {
		String[] result;
		if (exclude != null) {
			result = ligretoNode.substituteParams(exclude).split(",");
			int c = 0;
			for (int i=0; i < result.length; i++) {
				result[i] = result[i].trim();
				if (! "".equals(result[i])) {
					c++;
				}
			}
			String[] retValue = new String[c];
			int i2 = 0;
			for (int i=0; i < retValue.length; i++) {
				if (! "".equals(result[i])) {
					retValue[i2] = result[i];
					i2++;
				}
			}
			return retValue;
		} else {
			return null;
		}
	}
	
	public String getLocale() {
		return locale;
	}
	
	public void setLocale(String locale) {
		this.locale = locale;
	}

	public ReportNode getReportNode() {
		return reportNode;
	}

	public void setReportNode(ReportNode reportNode) {
		this.reportNode = reportNode;
	}
	
	public Attitude getCollation() {
		return collation;
	}

	public void setCollation(Attitude collation) {
		this.collation = collation;
	}
	
	public void setCollation(String collation) {
		if ("ignore".equals(collation)) {
			this.collation = Attitude.IGNORE;
		} else if ("dump".equals(collation)) {
			this.collation = Attitude.DUMP;
		} else if ("fail".equals(collation)) {
			this.collation = Attitude.FAIL;
		} else {
			throw new IllegalArgumentException("Wrong value specified as attitude in case of collation errors: " + collation);
		}
	}
}
