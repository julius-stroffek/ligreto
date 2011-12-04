package net.ligreto;

import org.apache.commons.logging.Log;

/**
 * The class representing the result status of ligreto operations. It contains the various
 * properties that are reported by individual operations.
 * 
 * @author Julius Stroffek
 *
 */
public class ResultStatus {

	/** The number of rows that will be reported as result row count. */
	protected long resultRowCount;
	
	/** The total number of row count that was transferred/reported. */
	protected long totalRowCount;
	
	/** The maximal relative difference that was seen during the operation. */
	protected double maximalRelativeDifference;
	
	/** Constructs the clean object. */
	public ResultStatus() {
		resultRowCount = 0;
		totalRowCount = 0;
		maximalRelativeDifference = 0;
	}

	/**
	 * The method will merge the results from the child (or other) operation.
	 * 
	 * @param child The result of the child/other operation.
	 */
	public void merge(ResultStatus other) {
		resultRowCount += other.resultRowCount;
		totalRowCount += other.totalRowCount;
		maximalRelativeDifference = Math.max(maximalRelativeDifference, other.maximalRelativeDifference);
	}
	
	/**
	 * The method will merge the results from the child (or other) operation.
	 * 
	 * @param child The result of the child/other operation.
	 * @param result Indicates whether the child/other operation should be treated as the result.
	 */
	public void merge(ResultStatus other, boolean result) {
		if (result)
			resultRowCount += other.resultRowCount;
		totalRowCount += other.totalRowCount;
		maximalRelativeDifference = Math.max(maximalRelativeDifference, other.maximalRelativeDifference);
	}
	
	/**
	 * Add additional row to the result reported.
	 * 
	 * @param result Indicates whether to increase also the result row count;
	 *               otherwise only total row count will be increased.
	 */
	public void addRow(boolean result) {
		totalRowCount++;
		if (result)
			resultRowCount++;
	}

	/**
	 * Add additional row to the result reported.
	 * 
	 * @param result Indicates whether to increase also the result row count;
	 *               otherwise only total row count will be increased.
	 * @param relativeDifference Maximal relative difference that was found.
	 */
	public void addRow(boolean result, double relativeDifference) {
		addRow(result);
		maximalRelativeDifference = Math.max(maximalRelativeDifference, relativeDifference);
	}

	/**
	 * Add additional value of the relative difference.
	 * 
	 * @param relativeDifference Next value of relative difference to add.
	 */
	public void addRelativeDifference(double relativeDifference) {
		maximalRelativeDifference = Math.max(maximalRelativeDifference, relativeDifference);
	}

	/**
	 * @return The number of rows that should be reported as the result.
	 */
	public long getResultRowCount() {
		return resultRowCount;
	}

	/**
	 * @return The total number of rows transferred/reported.
	 */
	public long getTotalRowCount() {
		return totalRowCount;
	}

	/**
	 * @return The maximal relative difference that was found.
	 */
	public double getMaximalRelativeDifference() {
		return maximalRelativeDifference;
	}

	/**
	 * Log the result information to the specified log.
	 * 
	 * @param log The log where the result status will be logged.
	 * @param typeString The type of the result - e.g. "JOIN", "LIGRETO", etc.
	 */
	public void info(Log log, String typeString) {
		log.info(typeString + " result row count: " + resultRowCount);
		log.info(typeString + " total row count: " + totalRowCount);
		String msg = String.format("%s maximal relative difference: %1.3f%%", typeString, 100*maximalRelativeDifference);
		log.info(msg);
	}
}
