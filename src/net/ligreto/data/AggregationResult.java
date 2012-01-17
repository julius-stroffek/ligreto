package net.ligreto.data;

/**
 * Holds the aggregated comparison result across multiple rows and columns.
 * 
 * @author Julius Stroffek
 * 
 */
public class AggregationResult {

	/** The number of columns that are stored in this result object. */
	protected int columnCount = 0;
	
	/** The array of result for individual columns. */
	protected ColumnAggregationResult[] columnResult = null;
	
	/** Constructs the empty result object. */
	public AggregationResult(int columnCount) {
		this.columnCount = columnCount;
		this.columnResult = new ColumnAggregationResult[columnCount];
	}
	
	/**
	 * @return The number of columns this objects stores the result for.
	 */
	public int getColumnCount() {
		return columnCount;
	}
	
	/**
	 * @param i Column index.
	 * @return The column result object.
	 */
	public ColumnAggregationResult getColumnResult(int i) {
		return columnResult[i];
	}
	
	/**
	 * @param i Column index.
	 * @param result The column result object to set
	 */
	public void setColumnResult(int i, ColumnAggregationResult result) {
		columnResult[i] = result;
	}
	
	/**
	 * Merges the specified result object into this result.
	 * 
	 * @param other The result object to be merged.
	 */
	public void merge(AggregationResult other) {
		if (columnCount != other.columnCount) {
			throw new IllegalArgumentException(
				"The provided result to be merged has different column count; column count: "
					+ columnCount + "; other column count: " + other.columnCount
			);
		}
		for (int i=0; i < columnCount; i++) {
			columnResult[i].merge(other.columnResult[i]);
		}
	}
}
