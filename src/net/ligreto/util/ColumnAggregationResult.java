package net.ligreto.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;

/**
 * Holds the aggregated comparison result for one column across multiple rows.
 * 
 * @author Julius Stroffek
 * 
 */
public class ColumnAggregationResult {

	/** The number of differences encountered. */
	protected int differenceCount = 0;

	/** The absolute difference in value across all the rows. */
	protected double difference = 0;

	/** The sum of all the absolute values across all the rows. */
	protected double totalValue = 0;

	/**
	 * The relative difference calculated across all the rows. Negative
	 * value means that relative difference does not make sense.
	 */
	protected double relativeDifference = -1;
	
	/** The relative number of differences encountered. */
	protected double differenceRatio = 0;

	/** The number of rows this object holds the result for. */
	protected long rowCount = 0;

	/**
	 * Constructs the object holding zero in all the result values.
	 */
	public ColumnAggregationResult() {
	}
	
	/**
	 * Constructs the object representing the difference between the given objects.
	 */
	public ColumnAggregationResult(Object value1, Object value2) {
		rowCount = 1;
		if (value1 == null && value2 == null) {
			differenceCount = 0;
		} else if (value1 != null && value2 != null) {
			differenceCount = value1.equals(value2) ? 0 : 1;
			double dValue1 = getDoubleValue(value1);
			double dValue2 = getDoubleValue(value2);
			difference = Math.abs(dValue2 - dValue1);
			totalValue = Math.abs(dValue1);
			relativeDifference = Math.abs(difference / totalValue);
		} else if (value1 != null) {
			differenceCount = 1;
			relativeDifference = 1;
			difference = Math.abs(getDoubleValue(value1));
			totalValue = difference;
		} else if (value2 != null) {
			differenceCount = 1;
			relativeDifference = 1;
			difference = Math.abs(getDoubleValue(value2));
			totalValue = difference;
		}
		differenceRatio = differenceCount / (double)rowCount;
	}
	
	/**
	 * Constructs the object representing the difference between the given objects.
	 */
	public ColumnAggregationResult(ResultSetComparator rsComparator, ResultSet rs1, int i1, ResultSet rs2, int i2) {
	}

	/**
	 * Returns the double representation of the specified numeric object.
	 * 
	 * @param o The numeric object which should be converted to double.
	 * @return Double value of the numeric object or 0 if the given object is not a numeric type.
	 */
	protected double getDoubleValue(Object o) {
		if (o instanceof Integer) {
			Integer v = (Integer) o;
			return v.doubleValue();
		} else if (o instanceof Long) {
			Long v = (Long) o;
			return v.doubleValue();
		} else if (o instanceof Float) {
			Float v = (Float) o;
			return v.doubleValue();
		} else if (o instanceof Double) {
			Double v = (Double) o;
			return v.doubleValue();
		} else if (o instanceof BigInteger) {
			BigInteger v = (BigInteger) o;
			return v.doubleValue();
		} else if (o instanceof BigDecimal) {
			BigDecimal v = (BigDecimal) o;
			return v.doubleValue();
		} else {
			return 0;
		}
	}
	
	/**
	 * Merges the aggregated result information of two objects.
	 */
	public void merge(ColumnAggregationResult other) {
		differenceCount += other.differenceCount;
		rowCount += other.rowCount;
		difference += other.difference;
		totalValue += other.totalValue;
		relativeDifference = difference / (double)totalValue;
		differenceRatio = differenceCount / (double)rowCount;
	}
	
	/**
	 * @return the match
	 */
	public int getDifferenceCount() {
		return differenceCount;
	}

	/**
	 * @param differenceCount
	 *            the differenceCount to set
	 */
	public void setDifferenceCount(int differenceCount) {
		this.differenceCount = differenceCount;
	}

	/**
	 * @return the difference
	 */
	public double getDifference() {
		return difference;
	}

	/**
	 * @param difference
	 *            the difference to set
	 */
	public void setDifference(double difference) {
		this.difference = difference;
	}

	/**
	 * @return the totalValue
	 */
	public double getTotalValue() {
		return totalValue;
	}

	/**
	 * @param totalValue
	 *            the totalValue to set
	 */
	public void setTotalValue(double totalValue) {
		this.totalValue = totalValue;
	}

	/**
	 * @return the relativeDifference
	 */
	public double getRelativeDifference() {
		return relativeDifference;
	}

	/**
	 * @param relativeDifference
	 *            the relativeDifference to set
	 */
	public void setRelativeDifference(double relativeDifference) {
		this.relativeDifference = relativeDifference;
	}

	/**
	 * @return the differenceRatio
	 */
	public double getDifferenceRatio() {
		return differenceRatio;
	}

	/**
	 * @param differenceRatio the differenceRatio to set
	 */
	public void setDifferenceRatio(double differenceRatio) {
		this.differenceRatio = differenceRatio;
	}

	/**
	 * @return the rowCount
	 */
	public long getRowCount() {
		return rowCount;
	}

	/**
	 * @param rowCount
	 *            the rowCount to set
	 */
	public void setRowCount(long rowCount) {
		this.rowCount = rowCount;
	}
}