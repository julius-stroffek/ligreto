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
	public enum SortingStrategy {INTERNAL, EXTERNAL};
	protected List<SqlNode> sqlQueries = new ArrayList<SqlNode>();
	protected List<LayoutNode> layouts = new ArrayList<LayoutNode>();
	protected String on;
	protected String exclude;
	protected String locale;
	protected SortingStrategy sortingStrategy = SortingStrategy.EXTERNAL;
	protected ReportNode reportNode;
	protected Attitude collation = Attitude.FAIL;
	
	public JoinNode(LigretoNode ligretoNode) {
		super(ligretoNode);
	}

	public void addSql(SqlNode sql) {
		sqlQueries.add(sql);
	}
	
	/**
	 * @return the sqlQueries
	 */
	public List<SqlNode> getSqlQueries() {
		return sqlQueries;
	}
	
	/**
	 * 
	 * @param on the comma separated list of column indices to be used for join condition
	 */
	public void setOn(String on) {
		this.on = on;
	}

	/**
	 * @return the array of column indices to be used for join condition
	 */
	public int[] getOn() {
		if (on == null) {
			return new int[0];
		}
		String[] sValues = ligretoNode.substituteParams(on).split(",");
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
	
	/**
	 * @return the sortingStrategy
	 */
	public SortingStrategy getSortingStrategy() {
		return sortingStrategy;
	}

	/**
	 * @param sortingStrategy the sortingStrategy to set
	 */
	public void setSortingStrategy(SortingStrategy sortingStrategy) {
		this.sortingStrategy = sortingStrategy;
	}

	/**
	 * @param sortingStrategy the sortingStrategy to set
	 */
	public void setSortingStrategy(String sortingStrategy) {
		if ("internal".equals(sortingStrategy)) {
			this.sortingStrategy = SortingStrategy.INTERNAL;
		} else if ("external".equals(sortingStrategy)) {
			this.sortingStrategy = SortingStrategy.EXTERNAL;			
		} else {
			throw new IllegalArgumentException("The sort could not be \"" + sortingStrategy + "\"");
		}
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

	public void addLayout(LayoutNode layout) {
		layouts.add(layout);
	}
	
	public List<LayoutNode> getLayouts() {
		return layouts;
	}
}
