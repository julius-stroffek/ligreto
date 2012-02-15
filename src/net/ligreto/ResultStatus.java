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

	/** The number of row that were different. */
	protected long differentRowCount = 0;
	
	/** The total number of row count that was transferred/reported. */
	protected long totalRowCount = 0;
	
	/** Indicates whether the result was accepted according the <result> node definition. */
	protected boolean accepted = true;
	
	/** Constructs the clean object. */
	public ResultStatus() {
	}

	/**
	 * The method will merge the results from the child (or other) operation.
	 * 
	 * @param other The result of the other operation to be merged.
	 */
	public void merge(ResultStatus other) {
		differentRowCount += other.differentRowCount;
		totalRowCount += other.totalRowCount;
		accepted &= other.accepted;
	}
	

	/**
	 * @return the differentRowCount
	 */
	public long getDifferentRowCount() {
		return differentRowCount;
	}

	/**
	 * @param differentRowCount the differentRowCount to set
	 */
	public void setDifferentRowCount(long differentRowCount) {
		this.differentRowCount = differentRowCount;
	}

	/**
	 * @return the totalRowCount
	 */
	public long getTotalRowCount() {
		return totalRowCount;
	}

	/**
	 * @param totalRowCount the totalRowCount to set
	 */
	public void setTotalRowCount(long totalRowCount) {
		this.totalRowCount = totalRowCount;
	}

	/**
	 * @return the accepted
	 */
	public boolean isAccepted() {
		return accepted;
	}

	/**
	 * @param accepted the accepted to set
	 */
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	/**
	 * Log the result information to the specified log.
	 * 
	 * @param log The log where the result status will be logged.
	 * @param typeString The type of the result - e.g. "JOIN", "LIGRETO", etc.
	 */
	public void info(Log log, String typeString) {
		log.info("[" + typeString + "] Different Row Count: " + differentRowCount);
		log.info("[" + typeString + "] Total Row Count: " + totalRowCount);
		if (accepted) {
			log.info("[" + typeString + "] ACCEPTED.");
		} else {
			log.info("[" + typeString + "] REJECTED.");			
		}
	}

	/**
	 * Add additional row the row to the total row count.
	 */
	public void addRow() {
		totalRowCount++;
	}
}
