package net.ligreto.data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import net.ligreto.exceptions.DataException;
import net.ligreto.exceptions.DataTypeMismatchException;
import net.ligreto.util.LigretoComparator;
import net.pcal.sqlsheet.XlsResultSet;

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
	
	/**
	 * The data provider where the row was fetched from. It is null if the data
	 * were not fetched from data provider.
	 */
	protected DataProvider dataProvider = null;
	
	/**
	 * The result set where the data were fetched from. It is null if the data
	 * were not fetched from result set.
	 */
	protected ResultSet resultSet = null;
	
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
	 * @param keyColumns the indices of key columns in {@code columns} array
	 * @throws DataException if any data access error occurred
	 */
	public DataProviderRow(int[] columnTypes, DataProvider dataProvider, int[] keyColumns) throws DataException {
		this.dataProvider = dataProvider;
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
	 * @param keyColumns the indices of key columns in {@code columns} array
	 * @throws DataException if any data access error occurred
	 */
	public DataProviderRow(int[] columnTypes, ResultSet resultSet, int[] keyColumns) throws DataException {
		this.resultSet = resultSet;
		this.columnTypes = columnTypes;
		this.keyColumns = keyColumns;

		try {
			columnValues = new Object[resultSet.getMetaData().getColumnCount()];
			for (int i = 0; i < columnValues.length; i++) {
				columnValues[i] = getObject(resultSet, i + 1, columnTypes[i]);
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
	 * @param keyColumns the indices of key columns in {@code columns} array
	 * @throws DataException if any data access error occurred
	 */
	public DataProviderRow(int[] columnTypes, DataProvider dataProvider, int[] columns, int[] keyColumns) throws DataException {
		this.dataProvider = dataProvider;
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
		this.resultSet = resultSet;
		this.columnTypes = columnTypes;
		this.keyColumns = keyColumns;
		assert(columnTypes.length == columns.length);
		
		try {
			columnValues = new Object[columns.length];
			for (int i = 0; i < columnValues.length; i++) {
				columnValues[i] = getObject(resultSet, columns[i], columnTypes[i]);
			}
		} catch (SQLException e) {
			throw new DataException("Failed to get the data.", e);
		}
	}

	/**
	 * This method will get the appropriate object type from the specified result
	 * set based on the column SQL type.
	 * 
	 * @param rs result set
	 * @param index the column index
	 * @return the column value
	 * @throws SQLException in case of JDBC errors
	 */
	public Object getObject(ResultSet rs, int index, int sqlType) throws SQLException {
		Object columnValue;
		switch (sqlType) {
		case Types.BOOLEAN:
			columnValue = rs.getBoolean(index);
			break;
		case Types.BIGINT:
			columnValue = rs.getLong(index);
			break;
		case Types.INTEGER:
			columnValue = rs.getInt(index);
			break;
		case Types.DOUBLE:
		case Types.FLOAT:
			columnValue = rs.getDouble(index);
			break;
		case Types.DATE:
			if (rs instanceof XlsResultSet) {
				columnValue = rs.getString(index);
			} else {
				Date date = rs.getDate(index);
				if (date != null) {
					columnValue = new Date(date.getTime());
				} else {
					columnValue = null;
				}
			}
			break;
		case Types.TIMESTAMP:
			Timestamp ts = rs.getTimestamp(index);
			if (ts != null) {
				columnValue = new Timestamp(ts.getTime());
			} else {
				columnValue = null;
			}
			break;
		case Types.TIME:
			Time time = rs.getTime(index);
			if (time != null) {
				columnValue = new Time(time.getTime());
			} else {
				columnValue = null;
			}
			break;
		case Types.DECIMAL:
		case Types.NUMERIC:
			BigDecimal bd = rs.getBigDecimal(index);
			if (bd != null) {
				columnValue = new BigDecimal(bd.unscaledValue(), bd.scale());
			} else {
				columnValue = null;
			}

			break;
		default:
			String tmpValue = rs.getString(index);
			if (tmpValue != null)
				columnValue = new String(tmpValue);
			else
				columnValue = null;
			break;
		}
		if (rs instanceof XlsResultSet) {
			// The call of wasNull is not yet implemented in SQLSheet XLS driver
		} else if (rs.wasNull()) {
			columnValue = null;
		}
		return columnValue;
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
				try {
				result = LigretoComparator.getInstance().compare(columnTypes[keyColumns[i] - 1],
						columnValues[keyColumns[i] - 1], other.columnTypes[other.keyColumns[i] - 1],
						other.columnValues[other.keyColumns[i] - 1]);
				} catch (DataTypeMismatchException e) {
					int i1 = keyColumns[i], i2 = keyColumns[i];
					String cn1 = null, cn2 = null;
					
					// get additional details about first columns
					if (dataProvider != null) {
						i1 = dataProvider.getOriginalIndex(i1);
					} else if (resultSet != null) {
						try {
							cn1 = resultSet.getMetaData().getColumnName(i1);
						} catch (SQLException se) {
							throw new RuntimeException(e);
						}
					}

					// get additional details about second column
					if (other.dataProvider != null) {
						i2 = other.dataProvider.getOriginalIndex(i2);
					} else if (other.resultSet != null) {
						try {
							cn2 = other.resultSet.getMetaData().getColumnName(i2);
						} catch (SQLException se) {
							throw new RuntimeException(e);
						}
					}

					// Provide the additional details about the location
					e.setColumnIndices(i1, i2);
					e.setColumnNames(cn1, cn2);

					// Re-throw the exception with more details
					throw new IllegalArgumentException(e);
				}
				if (result != 0)
					break;
			}
		} catch (DataException e) {
			throw new IllegalArgumentException(e);
		}
		
		return result;
	}
}