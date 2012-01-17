/**
 * 
 */
package net.ligreto.parser.nodes;

import java.util.ArrayList;
import java.util.List;

import net.ligreto.exceptions.InvalidFormatException;
import net.ligreto.exceptions.InvalidValueException;
import net.ligreto.util.MiscUtils;

/**
 * @author Julius Stroffek
 *
 */
public class JoinNode extends Node {
	public enum JoinType {FULL, LEFT, RIGHT, INNER};
	public enum JoinLayoutType {NORMAL, INTERLACED, DETAILED, AGGREGATED};
	
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
	protected String groupBy;
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
	 * @param joinLayoutType the joinLayout to set as string
	 */
	public void setJoinLayoutType(String joinLayoutType) {
		if ("normal".equals(joinLayoutType))
			this.joinLayoutType = JoinLayoutType.NORMAL;
		else if ("interlaced".equals(joinLayoutType))
			this.joinLayoutType = JoinLayoutType.INTERLACED;
		else if ("detailed".equals(joinLayoutType))
			this.joinLayoutType = JoinLayoutType.DETAILED;			
		else if ("aggregated".equals(joinLayoutType))
			this.joinLayoutType = JoinLayoutType.AGGREGATED;			
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
	 * @param on the comma separated list of column indices to be used for join condition
	 */
	public void setOn(String on) {
		this.on = on;
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
	 * @return the array of column indices to be used for join condition
	 */
	public int[] getOn() {
		if (on == null)
			return null;
		String[] sValues = ligretoNode.substituteParams(on).split(",");
		int values[] = new int[sValues.length];
		for (int i=0; i < values.length; i++) {
			values[i] = Integer.parseInt(sValues[i]);
		}
		return values;
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
