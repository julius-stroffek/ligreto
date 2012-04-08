package net.ligreto.data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import net.ligreto.exceptions.DataException;

/**
 * This is the class defining the interface to various data providers.
 * 
 * It is expected that that data provided identifies the rows that have duplicates in the columns
 * specified as key. These rows with duplicate values in key columns could be identified
 * by {@link DataProvider#hasDuplicateKey} method and should be thus processed separately.
 * <pre>
 * DataProvider dp = new DataProvider(...);
 * while (dp.next()) {
 *   while (dp.isValid() && dp.hasDuplicateKey) {
 *   	// here process the rows having duplicate values
 *   	// in the key columns
 *   }
 *   // here process the rows having unique values
 *   // in key columns
 * }
 * </pre>
 * Key columns should not have duplicates from their definition but the key columns specified
 * are only what we expect to be the key. The duplicates are an error that should be processed
 * in a separate way.
 * @author Julius Stroffek
 *
 */
public abstract class DataProvider {

	/** The data source caption used in user output. */
	protected String caption;
	
	/** The constructor could be used only by the subclasses. */
	protected DataProvider() {
	}
	
	/** Move to the next available row. */
	public abstract boolean next() throws DataException;

	/**
	 * Returns the boolean value of the specified column of the current row.
	 * 
	 * @param index the column index
	 * @return the boolean value of the specified column in current row
	 * @throws DataException if any data access error occurred
	 */
	public abstract Boolean getBoolean(int index) throws DataException;

	/**
	 * Returns the integer value of the specified column of the current row.
	 * 
	 * @param index The column index.
	 * @return the integer value of the specified column in current row
	 * @throws DataException if any data access error occurred
	 */
	public abstract Integer getInteger(int index) throws DataException;
	
	/**
	 * Returns the long value of the specified column of the current row.
	 * 
	 * @param index the column index.
	 * @return the long value of the specified column in current row
	 * @throws DataException if any data access error occurred
	 */
	public abstract Long getLong(int index) throws DataException;

	/**
	 * Returns the double value of the specified column of the current row.
	 * 
	 * @param index the column index
	 * @return the double value of the specified column in current row
	 * @throws DataException if any data access error occurred
	 */
	public abstract Double getDouble(int index) throws DataException;

	/**
	 * Returns the time stamp value of the specified column of the current row.
	 * 
	 * @param index the column index
	 * @return the time stamp value of the specified column in current row
	 * @throws DataException if any data access error occurred
	 */
	public abstract Timestamp getTimestamp(int index) throws DataException;

	/**
	 * Returns the big decimal value of the specified column of the current row.
	 * 
	 * @param index the column index.
	 * @return the big decimal value of the specified column in current row
	 * @throws DataException if any data access error occurred
	 */
	public abstract BigDecimal getBigDecimal(int index) throws DataException;
	
	/**
	 * Returns the object value of the specified column of the current row.
	 * 
	 * @param index the column index.
	 * @return the object value of the specified column in current row
	 * @throws DataException if any data access error occurred
	 */
	public abstract Object getObject(int index) throws DataException;

	/**
	 * Returns the string value of the specified column of the current row.
	 * 
	 * @param index the column index.
	 * @return the string value of the specified column in current row
	 * @throws DataException if any data access error occurred
	 */
	public abstract String getString(int index) throws DataException;
	
	/**
	 * Returns the SQL type of the specified column.
	 * 
	 * @param index the column index
	 * @return the SQL data type of the specified column
	 * @throws DataException if any data access error occurred
	 */
	public abstract int getColumnType(int index) throws DataException;
	
	/**
	 * Returns the label for the specified column.
	 * 
	 * @param index the column index.
	 * @return the label of the specified column
	 * @throws DataException if any data access error occurred
	 */
	public abstract String getColumnLabel(int index) throws DataException;
	
	/**
	 * Returns the name for the specified column.
	 * 
	 * @param index the column index.
	 * @return the name of the specified column
	 * @throws DataException if any data access error occurred
	 */
	public abstract String getColumnName(int index) throws DataException;
	
	/**
	 * Returns the number of columns of the data provider. All the rows that are
	 * returned have the same number of columns.
	 * 
	 * @return the number of columns.
	 * @throws DataException if any data access error occurred.
	 */
	public abstract int getColumnCount() throws DataException;
	
	/**
	 * Returns the original index of the specified column.
	 * 
	 * Data provider can exclude columns that it gets from the underlying data source.
	 * This method returns the index used for
	 * the column in the underlying data source. If there are more data providers chained
	 * as data sources, this method will get called internally through the whole chain.
	 * The index returned will be the value that the inner most data provider returns.
	 * 
	 * @param index the index of the column in the data provider
	 * @return the original index including the excluded columns
	 * @throws DataException
	 */
	public abstract int getOriginalIndex(int index) throws DataException;
	
	/**
	 * This is the reverse operation to {@code getOriginalIndex} method.
	 * 
	 * @param originalIndex the original index including the excluded columns
	 * @return the index of the column in the data provider
	 * @throws DataException
	 * @see DataProvider#getOriginalIndex
	 */
	public abstract int getIndex(int originalIndex) throws DataException;

	public Object getOriginalObject(int originalIndex) throws DataException {
		int index = getIndex(originalIndex);
		if (index > 0) {
			return getObject(index);
		} else {
			throw new DataException(
				"The requested column was exceluded from the data: "
				+ originalIndex + "; data source: " + getCaption()
			);
		}
	}

	public String getOriginalColumnLabel(int originalIndex) throws DataException {
		int index = getIndex(originalIndex);
		if (index > 0) {
			return getColumnLabel(index);
		} else {
			throw new DataException(
				"The requested column was exceluded from the data: "
				+ originalIndex + "; data source: " + getCaption()
			);
		}
	}

	public String getOriginalColumnName(int originalIndex) throws DataException {
		int index = getIndex(originalIndex);
		if (index > 0) {
			return getColumnName(index);
		} else {
			throw new DataException(
				"The requested column was exceluded from the data: "
				+ originalIndex + "; data source: " + getCaption()
			);
		}
	}

	public int getOriginalColumnType(int originalIndex) throws DataException {
		int index = getIndex(originalIndex);
		if (index > 0) {
			return getColumnType(index);
		} else {
			throw new DataException(
				"The requested column was exceluded from the data: "
				+ originalIndex + "; data source: " + getCaption()
			);
		}
	}

	/**
	 * Indicates whether the current row is active and valid.
	 * 
	 * @return true if the current row is active and valid row
	 * @throws DataException if any error occurred
	 */
	public abstract boolean isValid() throws DataException;
	
	/**
	 * Indicates whether the current row has duplicate values in key columns.
	 * 
	 * @return true if the current row has duplicate values in key columns
	 * @throws DataException if there is problem accessing the data
	 */
	public abstract boolean hasDuplicateKey() throws DataException;
	
	/**
	 * The index within the set of rows having the same values in key columns.
	 * 
	 * @return the index within the set of rows with duplicate values in key columns
	 * @throws DataException if there is problem accessing the data
	 */
	public abstract int getIndexInDuplicates() throws DataException;
	
	/**
	 * Indicates whether the last column value retrieved was null.
	 * 
	 * @return true if the last column value retrieved was null
	 * @throws DataException if there is a problem accessing the data
	 */
	public abstract boolean wasNull() throws DataException;

	/**
	 * Indicates whether the specified column has numeric data type.
	 * 
	 * @param index the index of the column in the data provider
	 * @return true if the specified column has numeric data type
	 * @throws DataException if there is a problem accessing the data
	 */
	public abstract boolean isNumeric(int index) throws DataException;

	
	/**
	 * Returns the time value of the specified column of the current row.
	 * 
	 * @param index the column index.
	 * @return the time value of the specified column in current row
	 * @throws DataException if any data access error occurred
	 */
	public abstract Time getTime(int index) throws DataException;

	/**
	 * Returns the data value of the specified column of the current row.
	 * 
	 * @param index the column index.
	 * @return the date value of the specified column in current row
	 * @throws DataException if any data access error occurred
	 */
	public abstract Date getDate(int index) throws DataException;

	/**
	 * Returns the caption used to refer to this data source.
	 * 
	 * @return the caption of this data source
	 */
	public String getCaption() {
		return caption;
	}
	
	/**
	 * Set the caption for this data source.
	 * 
	 * @param caption the caption to be used to refer to this data source
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}
}
