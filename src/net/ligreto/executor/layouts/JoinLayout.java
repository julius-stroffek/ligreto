package net.ligreto.executor.layouts;

import java.io.IOException;
import java.util.HashMap;

import net.ligreto.LigretoParameters;
import net.ligreto.ResultStatus;
import net.ligreto.builders.TargetInterface;
import net.ligreto.data.AggregationResult;
import net.ligreto.data.Column;
import net.ligreto.data.ColumnAggregationResult;
import net.ligreto.data.DataProvider;
import net.ligreto.exceptions.DataException;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.ResultExecutor;
import net.ligreto.parser.nodes.JoinNode;
import net.ligreto.parser.nodes.LayoutNode;
import net.ligreto.parser.nodes.LayoutNode.LayoutType;
import net.ligreto.util.Assert;

/**
 * Provides the interface for implementing various join layouts that could be used
 * to build various reports using the specified report builder class.
 * 
 * @author Julius Stroffek
 * 
 */
public abstract class JoinLayout {
	
	/** Indicates the type of the result to be processed. */
	public enum JoinResultType {LEFT, RIGHT, INNER};
	
	/** The report target used for report generation of the layout. */
	protected TargetInterface targetBuilder;
	
	/** The parser join node of the processed join. */
	protected JoinNode joinNode = null;
	
	/** The parser layout node of the layout to create. */
	protected LayoutNode layoutNode = null;
	
	/** The object holding the join result status. */
	protected ResultStatus resultStatus = null;
	
	/** The results aggregated across all the rows. */
	protected AggregationResult aggregatedResult;
	
	/** The results calculated for the current row. */
	protected AggregationResult currentResult;
		
	/** The column indices of the columns to be equal from both data providers. */
	protected int[] keyColumns = null;
			
	/** The columns which should be used for aggregated result. */
	protected int[] groupByColumns = null;
	
	/** The first data provider. */
	protected DataProvider dp1 = null;
	
	/** The second data provider. */
	protected DataProvider dp2 = null;
			
	/** The global ligreto parameters. */
	protected LigretoParameters ligretoParameters;
	
	/** The number of rows that are equal. */
	protected int equalRowCount = 0;
	
	/** The number of rows that matched on the specified key. */
	protected int matchingRowCount = 0;
	
	/** The number of rows that differs in any of the compared columns (including non-matched rows). */
	protected int differentRowCount = 0;
	
	/** The total number of rows. */
	protected int totalRowCount = 0;
	
	/** The number of rows on 1st data source. */
	protected int rowCountSrc1 = 0;
	
	/** The number of rows that have not matched from the 1st data source. */
	protected int nonMatchingRowsSrc1 = 0;
	
	/** The number of rows on 2nd data source. */
	protected int rowCountSrc2 = 0;
	
	/** The number of rows that have not matched from the 2nd data source. */
	protected int nonMatchingRowsSrc2 = 0;
	
	/** The number of column differences across all the rows. */
	protected int columnDifferences = 0;
	
	/** Stores whether the initialization was done. */
	private boolean startCalled = false;
	
	/** The number of rows processed/dumped by this layout object. */
	protected int dumpedRawCount = 0;
	
	/** The maximal number of rows to be processed by this layout. */ 
	protected Integer dumpedRawCountLimit = null;

	/** The array of columns that are compared. */
	protected int[] comparedColumns;
	
	/** Column metrics related fields. */
	protected HashMap<Integer, Void> noResultColumns = new HashMap<Integer, Void>();
	int[] resultColumns = null;
	int[] xmlToResult1 = null;
	int[] xmlToResult2 = null;

	/** Constructs the layout having the specified report builder. */
	protected JoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		this.targetBuilder = targetBuilder;
		this.ligretoParameters = ligretoParameters;
	}
	
	/**
	 * Constructs the layout of the specified type.
	 * 
	 * @param layoutType The type of the layout to create.
	 * @param ligretoParameters The global ligreto parameters to use.
	 * @return The created {@code JoinLayout} instance.
	 */
	public static JoinLayout createInstance(LayoutType layoutType, TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		switch (layoutType) {
		case NORMAL:
			return new NormalJoinLayout(targetBuilder, ligretoParameters);
		case INTERLACED:
			return new InterlacedJoinLayout(targetBuilder, ligretoParameters);
		case DETAILED:
			return new DetailedJoinLayout(targetBuilder, ligretoParameters);
		case AGGREGATED:
			return new AggregatedJoinLayout(targetBuilder, ligretoParameters);
		case KEY:
			return new KeyJoinLayout(targetBuilder, ligretoParameters);
		case SUMMARY:
			return new SummaryJoinLayout(targetBuilder, ligretoParameters);
		case ANALYTICAL:
			return new AnalyticalJoinLayout(targetBuilder, ligretoParameters);
		case DUPLICATES:
			return new DuplicatesJoinLayout(targetBuilder, ligretoParameters);
		default:
			throw new IllegalArgumentException("Unexpected value of JoinLayoutType.");
		}
	}

	/** Dumps the join result header. 
	 * @throws SQLException 
	 * @throws DataSourceNotDefinedException 
	 * @throws IOException 
	 * @throws DataException */
	public abstract void dumpHeader() throws DataSourceNotDefinedException, IOException, DataException;
	
	/**
	 * This function converts the specified column index from XML into the index
	 * of the result structure. The result structure does not contain the columns
	 * that were specified in exclude attribute.
	 * 
	 * @param index Index of the column as specified in "on" attribute of the join. 
	 * @return the index of the column in the result structure; -1 of the index column is not in the result
	 * @throws LigretoException 
	 */
	public int translateToResultColumn(int index) throws LigretoException {
		Assert.assertTrue(startCalled);

		int rc1 = dp1.getIndex(index);
		int rc2 = dp2.getIndex(index);
		
		if (rc1 == -1 || rc2 == -1)
			throw new LigretoException("Column index \"" + index + "\" matches the excluded column.");
		
		rc1 = xmlToResult1[rc1-1];
		rc2 = xmlToResult2[rc2-1];
		
		if (rc1 == -1 && rc2 == -1)
			return -1;

		if (rc1 != rc2) {
			throw new LigretoException("Column index \"" + index + "\" does not matches the same columns in the result due to some columns being excluded.");
		}
		
		return rc1;
	}
	
	/**
	 * Function will provide the name of the specified result column.
	 * 
	 * @param i the index of the result column
	 * @return the name of the i-th result column
	 * @throws SQLException
	 */
	public String getResultColumnName(int i) throws DataException {
		Assert.assertTrue(startCalled);

		if (i < 0 || i >= resultColumns.length)
			throw new IllegalArgumentException("Result column index out of range: " + i);

		return getColumnName(resultColumns[i]);
	}
	
	/**
	 * Function will provide the name of the specified result column.
	 * 
	 * @param i the index of the column
	 * @return the name of the i-th column
	 * @throws SQLException
	 */
	public String getColumnName(int i) throws DataException {
		Assert.assertTrue(startCalled);

		if (i <= 0 || i > getColumnCount())
			throw new IllegalArgumentException("Column index out of range: " + i);

		String colName = dp1.getColumnName(i);
		String col2Name = dp1.getColumnName(i);
		if (! colName.equalsIgnoreCase(col2Name)) {
			colName = colName + " / " + col2Name;
		}

		return colName;
	}
	
	/**
	 * @param index index of the result column
	 * @return the aggregated result of the column data
	 */
	public ColumnAggregationResult getColumnAggregationResult(int index) {
		return aggregatedResult.getColumnResult(index);
	}
	
	/**
	 * This function is called to calculate the column result metrics.
	 * 
	 * @param resultType the type of the result being processed
	 * @return the metrics calculated for the current row produced as output
	 * @throws SQLException 
	 */
	protected AggregationResult calculateColumnMetrics(JoinResultType resultType) throws DataException {		
		// Create the object holding the result metrics
		AggregationResult result = new AggregationResult(resultColumns.length);
		
		// Loop through all the columns to be in the result
		for (int i = 0; i < resultColumns.length; i++) {
			
			// Get the indices of result columns into the result sets
			int r = resultColumns[i];
			
			Column columnValue1, columnValue2;
			switch (resultType) {
			case LEFT:
				columnValue1 = new Column(dp1, r);
				result.setColumnResult(i, new ColumnAggregationResult(columnValue1, null));
				break;
			case RIGHT:
				columnValue2 = new Column(dp2, r);
				result.setColumnResult(i, new ColumnAggregationResult(null, columnValue2));
				break;
			case INNER:
				columnValue1 = new Column(dp1, r);
				columnValue2 = new Column(dp2, r);
				result.setColumnResult(i, new ColumnAggregationResult(columnValue1, columnValue2));
				break;
			default:
				throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
			}
		}
		return result;
	}
	
	/**
	 * Will dump the result row from the corresponding result sets. The method will also
	 * call the ResultSet.next() method on the result sets where the row was processed.
	 * The method will highlight some of the columns based on the array specified
	 * 
	 * @param rowDiffs   The number of differences encountered in the current row.
	 * @param highlightArray Determines which columns should be highlighted.
	 * @param resultType Determines whether to dump the row from the first,
	 *                   second or both result sets.
	 * @throws SQLException 
	 * @throws LigretoException 
	 * @throws IOException
	 * 
	 * @return Whether the presented row was part of the join type (full, inner, etc.) in this layout
	 */
	public boolean processRow(int rowDiffs, int[] highlightArray, JoinResultType resultType) throws LigretoException, IOException {
		
		// Check for proper initialization
		Assert.assertTrue(startCalled);
		
		// We will not accept more rows if we are over limit
		if (isOverLimit()) {
			return false;
		}
		
		/*
		 * First switch on the type of join and then decide whether the specified
		 * result type should be part of the join result.
		 */
		switch (layoutNode.getJoinType()) {
		case FULL:
			switch (resultType) {
			case LEFT:
				rowCountSrc1++;
				nonMatchingRowsSrc1++;
				break;
			case RIGHT:
				rowCountSrc2++;
				nonMatchingRowsSrc2++;
				break;
			case INNER:
				rowCountSrc1++;
				rowCountSrc2++;
				matchingRowCount++;
				break;
			default:
				return false;
			}
			break;
		case COMPLEMENT:
			switch (resultType) {
			case LEFT:
				rowCountSrc1++;
				nonMatchingRowsSrc1++;
				break;
			case RIGHT:
				rowCountSrc2++;
				nonMatchingRowsSrc2++;
				break;
			default:
				return false;
			}
			break;
		case LEFT:
			switch (resultType) {
			case LEFT:
				rowCountSrc1++;
				nonMatchingRowsSrc1++;
				break;
			case INNER:
				rowCountSrc1++;
				rowCountSrc2++;
				matchingRowCount++;
				break;
			default:
				return false;
			}
			break;
		case LEFT_COMPLEMENT:
			if (resultType != JoinResultType.LEFT) {
				return false;
			}
			rowCountSrc1++;
			nonMatchingRowsSrc1++;
			break;
		case RIGHT:
			switch (resultType) {
			case RIGHT:
				rowCountSrc2++;
				nonMatchingRowsSrc2++;
				break;
			case INNER:
				rowCountSrc1++;
				rowCountSrc2++;
				matchingRowCount++;
				break;
			default:
				return false;
			}
			break;
		case RIGHT_COMPLEMENT:
			if (resultType != JoinResultType.RIGHT) {
				return false;
			}
			rowCountSrc2++;
			nonMatchingRowsSrc2++;
			break;
		case INNER:
			if (resultType != JoinResultType.INNER) {
				return false;
			}
			rowCountSrc1++;
			rowCountSrc2++;
			matchingRowCount++;
			break;
		default:
			throw new IllegalArgumentException("Unexpected value of JoinType.");
		}
		totalRowCount++;
		columnDifferences += rowDiffs;
		if (resultType != JoinResultType.INNER || rowDiffs > 0) {
			differentRowCount++;
		} else {
			equalRowCount++;
		}
		
		// Here we now that we produce the row that is part of the result.
		// We will therefore do some more aggregations here.
		currentResult = calculateColumnMetrics(resultType);
		
		if (aggregatedResult != null) {
			aggregatedResult.merge(currentResult);
		} else {
			aggregatedResult = currentResult;
		}
		
		// And finally we will dump the row after the current result
		// and aggregation results are available.
		// But we will not dump differences if they were not requested.
		if (!layoutNode.getDiffs() || resultType != JoinResultType.INNER || rowDiffs > 0) {
			dumpedRawCount++;
			dumpRow(rowDiffs, highlightArray, resultType);
		}
		
		return true;
	}

	/**
	 * @return true if the limit number of rows was reached and no more raws are
	 *         dumped in later processing.
	 */
	public boolean isOverLimit() {
		return dumpedRawCountLimit != null && dumpedRawCount >= dumpedRawCountLimit;
	}
	
	/**
	 * Will dump the result row from the corresponding result sets. The method will also
	 * call the ResultSet.next() method on the result sets where the row was processed.
	 * 
	 * @param rowDiffs   The number of differences encountered in the current row.
	 * @param resultType Determines whether to dump the row from the first,
	 *                   second or both result sets.
	 * @throws SQLException 
	 * @throws LigretoException 
	 * @throws IOException 
	 */
	public boolean processRow(int rowDiffs, JoinResultType resultType) throws LigretoException, IOException {
		return processRow(rowDiffs, null, resultType);
	}

	/**
	 * Will dump the result row from the corresponding result sets. The method will also
	 * call the ResultSet.next() method on the result sets where the row was processed.
	 * The method will highlight some of the columns based on the array specified
	 * 
	 * @param rowDiffs   The number of differences encountered in the current row.
	 * @param highlightArray Determines which columns should be highlighted.
	 * @param resultType Determines whether to dump the row from the first,
	 *                   second or both result sets.
	 * @throws SQLException 
	 * @throws LigretoException 
	 * @throws IOException 
	 */
	public abstract void dumpRow(int rowDiffs, int[] highlightArray, JoinResultType resultType) throws LigretoException, IOException;
	
	/**
	 * Will dump the row which was identified as having duplicate key.
	 * 
	 * @param dataSourceIndex The index of data source that the duplicate belongs to.
	 * @throws IOException 
	 * @throws SQLException
	 */
	public void dumpDuplicate(int dataSourceIndex) throws DataException, IOException {
	}
	
	/**
	 * Will dump the result row from the corresponding result sets. The method will also
	 * call the ResultSet.next() method on the result sets where the row was processed.
	 * 
	 * @param rowDiffs   The number of differences encountered in the current row.
	 * @param resultType Determines whether to dump the row from the first,
	 *                   second or both result sets.
	 * @throws SQLException 
	 * @throws LigretoException 
	 * @throws IOException 
	 */
	public void dumpRow(int rowDiffs, JoinResultType resultType) throws LigretoException, IOException {
		dumpRow(rowDiffs, null, resultType);
	}

	/**
	 * @return the layoutNode
	 */
	public LayoutNode getLayoutNode() {
		return layoutNode;
	}

	/**
	 * @param layoutNode
	 * 				The layout node to set.
	 */
	public void setLayoutNode(LayoutNode layoutNode) {
		this.layoutNode = layoutNode;
		dumpedRawCountLimit = layoutNode.getLimit();
	}

	/**
	 * @param joinNode
	 * 				The join node to set.
	 * @throws DataSourceNotDefinedException 
	 */
	public void setJoinNode(JoinNode joinNode) throws DataSourceNotDefinedException {
		this.joinNode = joinNode;
	}

	/**
	 * @return the resultStatus
	 */
	public ResultStatus getResultStatus() {
		return resultStatus;
	}

	/**
	 * @param resultStatus
	 * 				The result status to set.
	 */
	public void setResultStatus(ResultStatus resultStatus) {
		this.resultStatus = resultStatus;
	}
	
	/**
	 * @param key The join columns for both data providers.
	 */
	public void setKeyColumns(int[] keyColumns) {
		this.keyColumns = keyColumns;
	}
	
	/**
	 * @return the comparedColumns
	 */
	public int[] getComparedColumns() {
		return comparedColumns;
	}

	/**
	 * @param comparedColumns the comparedColumns to set
	 */
	public void setComparedColumns(int[] comparedColumns) {
		this.comparedColumns = comparedColumns;
	}

	/**
	 * @param groupBy
	 * 			The group by columns to set.
	 */
	public void setGroupByColumns(int[] groupByClumns) {
		this.groupByColumns = groupByClumns;
	}
	
	/**
	 * @param dp1 The first data provider to set.
	 * @param dp2 The second data provider to set.
	 */
	public void setDataProviders(DataProvider dp1, DataProvider dp2) {
		this.dp1 = dp1;
		this.dp2 = dp2;
	}
	
	/**
	 * @param ligretoParameters the ligretoParameters to set
	 */
	public void setLigretoParameters(LigretoParameters ligretoParameters) {
		this.ligretoParameters = ligretoParameters;
	}
	
	/**
	 * @return The number of columns being processed.
	 * @throws DataException 
	 */
	public int getColumnCount() throws DataException {
		Assert.assertTrue(dp1.getColumnCount() == dp2.getColumnCount());
		return dp1.getColumnCount();
	}
	
	/**
	 * @return the matchingRowCount
	 */
	public int getMatchingRowCount() {
		return matchingRowCount;
	}

	/**
	 * @return the nonMatchingRowsSrc1
	 */
	public int getNonMatchingRowsSrc1() {
		return nonMatchingRowsSrc1;
	}

	/**
	 * @return the nonMatchingRowsSrc2
	 */
	public int getNonMatchingRowsSrc2() {
		return nonMatchingRowsSrc2;
	}

	/**
	 * @return the differentRowCount
	 */
	public int getDifferentRowCount() {
		return differentRowCount;
	}

	/**
	 * @return the totalRowCount
	 */
	public int getTotalRowCount() {
		return totalRowCount;
	}

	/**
	 * @return the columnDifferences
	 */
	public int getColumnDifferences() {
		return columnDifferences;
	}

	/**
	 * The method executed before providing any data to the layout object.
	 * @throws SQLException 
	 * @throws LigretoException 
	 */
	public void start() throws LigretoException {
		startCalled = true;

		for (int i=0; i < keyColumns.length; i++) {
			noResultColumns.put(keyColumns[i], null);
		}

		int resultCount = getColumnCount() - noResultColumns.size();
				
		resultColumns = new int[resultCount];
		
		// Store the information about the result column's indices
		for (int i=0, i1=1; i < resultCount; i++, i1++) {
			while (noResultColumns.containsKey(i1))
				i1++;
			resultColumns[i] = i1;
		}
		
		xmlToResult1 = new int[getColumnCount()];
		xmlToResult2 = new int[getColumnCount()];
		// Do the XML index to result translation array
		for (int i=0; i < getColumnCount(); i++) {
			xmlToResult1[i] = -1;
			xmlToResult2[i] = -1;
			for (int j=0; j < resultCount; j++) {
				if (resultColumns[j] == i + 1) {
					xmlToResult1[i] = j;
					xmlToResult2[i] = j;
				}
			}
		}
	}

	/**
	 * The method executed after providing all data to the layout object.
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws LigretoException
	 * @return the result status 
	 */
	public ResultStatus finish() throws IOException, LigretoException {
		Assert.assertTrue(startCalled);
		targetBuilder.finish();
		ResultExecutor executor = new ResultExecutor(layoutNode.getResultNode(), this);
		resultStatus = executor.execute();
		return resultStatus;
	}

}
