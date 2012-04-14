package net.ligreto.data;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.sql.Date;

import net.ligreto.exceptions.DataException;

/**
 * The data provider class encapsulating the {@link java.sql.ResultSet} object into {@code DataProvider}.
 * 
 * @author Julius Stroffek
 *
 */
public class ResultSetDataProvider extends DataProvider {
	
	/** The result set used to obtain the data. */
	protected ResultSet resultSet;
	
	/** Indicates whether last fetched field was null. */
	protected boolean wasNull = false;
		
	/** The column SQL types. */
	protected int[] columnTypes = null;
	
	/** The current row. */
	protected DataProviderRow currentRow = null;
	
	/** The next row coming after the call to {@link #next}. */
	protected DataProviderRow nextRow = null;
	
	/** Indicates whether the current row has duplicate key values with the previous row. */
	protected boolean hasDuplicateKey = false;
	
	/** Indicates whether the next row has duplicate key values with the current row. */
	protected boolean nextHasDuplicateKey = false;
	
	/** The index within the set of rows with the same key column values. */
	protected int indexInDuplicates = 0;
	
	/**
	 * Constructs the data provider based on the specified result set.
	 * 
	 * @param resultSet the result set to be used to obtain the data
	 * @param keyColumns the indices of key columns
	 * @throws DataException if any data access error occurred
	 * @throws SQLException if there was an error in the result set calls
	 */
	public ResultSetDataProvider(ResultSet resultSet, int[] keyColumns) throws DataException, SQLException {
		super(resultSet.getMetaData().getColumnCount(), keyColumns, null);
		this.resultSet = resultSet;
		
		ResultSetMetaData rsmd = resultSet.getMetaData();
		
		columnTypes = new int[rsmd.getColumnCount()];
		for (int i=0; i < rsmd.getColumnCount(); i++) {
			columnTypes[i] = rsmd.getColumnType(i+1);
		}
		resultSet.next();
		nextRow = new DataProviderRow(columnTypes, resultSet, keyColumns);
		hasDuplicateKey = false;
		nextHasDuplicateKey = false;
	}

	/**
	 * Constructs the data provider based on the specified result set.
	 * 
	 * @param resultSet the result set to be used to obtain the data
	 * @param keyColumns the indices of key columns
	 * @param excludeColumns the indices of columns to be excluded
	 * @throws DataException if any data access error occurred
	 * @throws SQLException if there was an error in the result set calls
	 */
	public ResultSetDataProvider(ResultSet resultSet, int[] keyColumns, int[] excludeColumns) throws SQLException, DataException {
		super(resultSet.getMetaData().getColumnCount(), keyColumns, excludeColumns);
		this.resultSet = resultSet;

		ResultSetMetaData rsmd = resultSet.getMetaData();
		
		
		// We need to get the SQL types for columns
		columnTypes = new int[originalIndices.length];
		for (int i=0; i < originalIndices.length; i++) {
			columnTypes[i] = rsmd.getColumnType(originalIndices[i]);
		}

		if (resultSet.next()) {
			nextRow = new DataProviderRow(columnTypes, resultSet, originalIndices, keyIndices);
		} else {
			nextRow = null;
		}
		hasDuplicateKey = false;
		nextHasDuplicateKey = false;
	}

	@Override
	public boolean next() throws DataException {
		try {
			hasDuplicateKey = nextHasDuplicateKey;
			currentRow = nextRow;
			if (hasDuplicateKey) {
				indexInDuplicates++;
			} else {
				indexInDuplicates = 0;
			}
			if (currentRow != null && resultSet.next()) {
				nextRow = new DataProviderRow(columnTypes, resultSet, originalIndices, keyIndices);
				nextHasDuplicateKey = currentRow.compareTo(nextRow) == 0;
			} else {
				nextRow = null;
				nextHasDuplicateKey = false;
			}
			return currentRow != null;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public Boolean getBoolean(int index) throws DataException {
		assert(index > 0 && index <= originalIndices.length);

		Object result = currentRow.columnValues[index-1];
		wasNull = result == null;
		if (wasNull) {
			return null;
		} else {
			return (Boolean) result;
		}
	}

	@Override
	public Integer getInteger(int index) throws DataException {
		assert(index > 0 && index <= originalIndices.length);

		Object result = currentRow.columnValues[index-1];
		wasNull = result == null;
		if (wasNull) {
			return null;
		} else {
			return (Integer) result;
		}
	}

	@Override
	public Long getLong(int index) throws DataException {
		assert(index > 0 && index <= originalIndices.length);

		Object result = currentRow.columnValues[index-1];
		wasNull = result == null;
		if (wasNull) {
			return null;
		} else {
			return (Long) result;
		}
	}

	@Override
	public Double getDouble(int index) throws DataException {
		assert(index > 0 && index <= originalIndices.length);

		Object result = currentRow.columnValues[index-1];
		wasNull = result == null;
		if (wasNull) {
			return null;
		} else {
			return (Double) result;
		}
	}

	@Override
	public Timestamp getTimestamp(int index) throws DataException {
		assert(index > 0 && index <= originalIndices.length);

		Object result = currentRow.columnValues[index-1];
		wasNull = result == null;
		if (wasNull) {
			return null;
		} else {
			return (Timestamp) result;
		}
	}

	@Override
	public BigDecimal getBigDecimal(int index) throws DataException {
		assert(index > 0 && index <= originalIndices.length);

		Object result = currentRow.columnValues[index-1];
		wasNull = result == null;
		if (wasNull) {
			return null;
		} else {
			return (BigDecimal) result;
		}
	}

	@Override
	public String getString(int index) throws DataException {
		assert(index > 0 && index <= originalIndices.length);

		Object result = currentRow.columnValues[index-1];
		wasNull = result == null;
		if (wasNull) {
			return null;
		} else {
			if (result instanceof String) {
				return (String) result;
			} else {
				return result.toString();
			}
		}
	}

	@Override
	public Time getTime(int index) throws DataException {
		assert(index > 0 && index <= originalIndices.length);

		Object result = currentRow.columnValues[index-1];
		wasNull = result == null;
		if (wasNull) {
			return null;
		} else {
			return (Time) result;
		}
	}

	@Override
	public Date getDate(int index) throws DataException {
		assert(index > 0 && index <= originalIndices.length);

		Object result = currentRow.columnValues[index-1];
		wasNull = result == null;
		if (wasNull) {
			return null;
		} else {
			return (Date) result;
		}
	}

	@Override
	public Object getObject(int index) throws DataException {	
		Object columnValue = null;
		switch (getColumnType(index)) {
		case Types.BOOLEAN:
			columnValue = getBoolean(index);
			break;
		case Types.BIGINT:
			columnValue = getLong(index);
			break;
		case Types.INTEGER:
			columnValue = getInteger(index);
			break;
		case Types.DOUBLE:
		case Types.FLOAT:
			columnValue = getDouble(index);
			break;
		case Types.DATE:
			Date date = getDate(index);
			if (date != null) {
				columnValue = new Date(date.getTime());
			} else {
				columnValue = null;
			}
			break;
		case Types.TIMESTAMP:
			Timestamp ts = getTimestamp(index);
			if (ts != null) {
				columnValue = new Timestamp(ts.getTime());
			} else {
				columnValue = null;
			}
			break;
		case Types.TIME:
			Time time = getTime(index);
			if (time != null) {
				columnValue = new Time(time.getTime());
			} else {
				columnValue = null;
			}
			break;
		case Types.DECIMAL:
		case Types.NUMERIC:
			BigDecimal bd = getBigDecimal(index);
			if (bd != null) {
				columnValue = new BigDecimal(bd.unscaledValue(), bd.scale());
			} else {
				columnValue = null;
			}

			break;
		default:
			String tmpValue = getString(index);
			if (tmpValue != null)
				columnValue = new String(tmpValue);
			else
				columnValue = null;
			break;
		}
		if (wasNull())
			columnValue = null;

		return columnValue;
	}

	@Override
	public int getColumnType(int index) throws DataException {
		assert(index > 0 && index <= originalIndices.length);
		return columnTypes[index-1];
	}

	@Override
	public String getColumnLabel(int index) throws DataException {
		assert(index > 0 && index <= originalIndices.length);
		try {
			return resultSet.getMetaData().getColumnLabel(originalIndices[index-1]);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public String getColumnName(int index) throws DataException {
		assert(index > 0 && index <= originalIndices.length);
		try {
			return resultSet.getMetaData().getColumnName(originalIndices[index-1]);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public int getColumnCount() throws DataException {
		return originalIndices.length;
	}

	@Override
	public int getOriginalIndex(int index) throws DataException {
		assert(index > 0 && index <= originalIndices.length);
		return originalIndices[index-1];
	}

	@Override
	public int getIndex(int originalIndex) throws DataException {
		try {
			assert(originalIndex > 0 && originalIndex <= resultSet.getMetaData().getColumnCount());
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return dataProviderIndices[originalIndex-1];
	}

	@Override
	public boolean wasNull() throws DataException {
		return wasNull;
	}

	@Override
	public boolean isNumeric(int index) throws DataException {
		switch (getColumnType(index)) {
		case Types.BIGINT:
		case Types.INTEGER:
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.DECIMAL:
		case Types.NUMERIC:
			return true;
		default:
			return false;			
		}
	}

	@Override
	public boolean isValid() {
		return currentRow != null;
	}

	@Override
	public boolean hasDuplicateKey() throws DataException {
		return hasDuplicateKey || nextHasDuplicateKey;
	}

	@Override
	public int getIndexInDuplicates() throws DataException {
		return indexInDuplicates;
	}
}
