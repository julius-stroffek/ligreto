package net.ligreto.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.ligreto.exceptions.DataException;
import net.ligreto.util.LigretoComparator;

/**
 * This is the row of the data provider.
 * 
 * This class is mainly used to store the rows for sorting and to identify the rows
 * with duplicate key columns.
 * 
 * @author Julius Stroffek
 *
 */
public class DataProviderRow implements Comparable<DataProviderRow> {
	
	/** The SQL data types for the columns. */
	protected int[] columnTypes;
	
	/** The column values matching the SQL data types types. */
	protected Object[] columnValues;
	
	/** The array of column indices that form the key. The first column has index 1. */
	protected int[] keyColumns;
	
	/**
	 * Create the data provider row.
	 * 
	 * @param columnTypes the SQL data types
	 * @param columnValues the column values
	 * @param keyColumns the indices of key columns
	 */
	public DataProviderRow(int[] columnTypes, Object[] columnValues, int[] keyColumns) {
		this.columnTypes = columnTypes;
		this.columnValues = columnValues;
		this.keyColumns = keyColumns;
	}

	/**
	 * Creates the data provider row from the current row of the specified data provider.
	 * 
	 * @param columnTypes the SQL data types
	 * @param dataProvider the data provider to be used to fetch the column values
	 * @param keyColumns the indices of key columns
	 * @throws DataException if any data access error occurred
	 */
	public DataProviderRow(int[] columnTypes, DataProvider dataProvider, int[] keyColumns) throws DataException {
		this.columnTypes = columnTypes;
		this.keyColumns = keyColumns;

		columnValues = new Object[dataProvider.getColumnCount()];
		for (int i=0; i < columnValues.length; i++) {
			columnValues[i] = dataProvider.getObject(i+1);
		}
	}

	/**
	 * Creates the data provider row from the current row of the specified {@link java.sql.ResultSet} object.
	 * 
	 * @param columnTypes the SQL data types
	 * @param resultSet the result set used to fetch the columns values
	 * @param keyColumns the indices of key columns
	 * @throws DataException if any data access error occurred
	 */
	public DataProviderRow(int[] columnTypes, ResultSet resultSet, int[] keyColumns) throws DataException {
		this.columnTypes = columnTypes;
		this.keyColumns = keyColumns;

		try {
			columnValues = new Object[resultSet.getMetaData().getColumnCount()];
			for (int i = 0; i < columnValues.length; i++) {
				columnValues[i] = resultSet.getObject(i + 1);
			}
		} catch (SQLException e) {
			throw new DataException("Failed to get the data.", e);
		}
	}

	
	/**
	 * Creates the data provider row from the current row of the specified data provider.
	 * 
	 * @param columnTypes the SQL data types
	 * @param dataProvider the data provider to be used to fetch the column values
	 * @param columns the indices of columns to be copied
	 * @param keyColumns the indices of key columns
	 * @throws DataException if any data access error occurred
	 */
	public DataProviderRow(int[] columnTypes, DataProvider dataProvider, int[] columns, int[] keyColumns) throws DataException {
		this.columnTypes = columnTypes;
		this.keyColumns = keyColumns;
		assert(columnTypes.length == columns.length);
		
		columnValues = new Object[columns.length];
		for (int i=0; i < columnValues.length; i++) {
			columnValues[i] = dataProvider.getObject(columns[i]);
		}
	}

	/**
	 * Creates the data provider row from the current row of the specified {@link java.sql.ResultSet} object.
	 * 
	 * @param columnTypes the SQL data types
	 * @param resultSet the result set used to fetch the columns values
	 * @param columns the indices of columns to be copied
	 * @param keyColumns the indices of key columns
	 * @throws DataException if any data access error occurred
	 */
	public DataProviderRow(int[] columnTypes, ResultSet resultSet, int[] columns, int[] keyColumns) throws DataException {
		this.columnTypes = columnTypes;
		this.keyColumns = keyColumns;
		assert(columnTypes.length == columns.length);
		
		try {
			columnValues = new Object[columns.length];
			for (int i = 0; i < columnValues.length; i++) {
				columnValues[i] = resultSet.getObject(columns[i]);
			}
		} catch (SQLException e) {
			throw new DataException("Failed to get the data.", e);
		}
	}

	/**
	 * Compare the values of key columns only.
	 */
	@Override
	public int compareTo(DataProviderRow other) {
		int result = 0;

		assert(columnTypes.length == other.columnTypes.length);
		assert(keyColumns.length == other.keyColumns.length);
		try {
			for (int i = 0; i < keyColumns.length; i++) {
				result = LigretoComparator.getInstance().compare(columnTypes[keyColumns[i] - 1],
						columnValues[keyColumns[i] - 1], other.columnTypes[other.keyColumns[i] - 1],
						other.columnValues[other.keyColumns[i] - 1]);
				if (result != 0)
					break;
			}
		} catch (DataException e) {
			throw new IllegalArgumentException(e);
		}
		
		return result;
	}
}