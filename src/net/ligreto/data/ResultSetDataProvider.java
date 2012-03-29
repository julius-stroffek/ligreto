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

public class ResultSetDataProvider extends DataProvider {
	
	protected ResultSet resultSet;
	protected int[] originalIndices;
	protected int[] dataProviderIndices;
	
	protected boolean wasNull = false;
	protected int[] keyColumns = null;
	protected int[] columnTypes = null;
	protected DataProviderRow currentRow = null;
	protected DataProviderRow nextRow = null;
	protected boolean hasDuplicateKey = false;
	protected boolean nextHasDuplicateKey = false;
	protected int indexInDuplicates = 0;
	
	public ResultSetDataProvider(ResultSet resultSet, int[] keyColumns) throws DataException, SQLException {
		this.resultSet = resultSet;
		this.keyColumns = keyColumns;
		ResultSetMetaData rsmd = resultSet.getMetaData();
		originalIndices = new int[rsmd.getColumnCount()];
		dataProviderIndices  = new int[rsmd.getColumnCount()];
		columnTypes = new int[rsmd.getColumnCount()];
		for (int i=0; i < rsmd.getColumnCount(); i++) {
			originalIndices[i] = i+1;
			dataProviderIndices[i] = i+1;
			columnTypes[i] = rsmd.getColumnType(i+1);
		}
		resultSet.next();
		nextRow = new DataProviderRow(columnTypes, resultSet, keyColumns);
		hasDuplicateKey = false;
		nextHasDuplicateKey = false;
	}

	public ResultSetDataProvider(ResultSet resultSet, int[] keyColumns, int[] excludeColumns) throws SQLException, DataException {
		this.resultSet = resultSet;
		this.keyColumns = keyColumns;
		ResultSetMetaData rsmd = resultSet.getMetaData();
		
		boolean[] columnExcluded = new boolean[rsmd.getColumnCount()];
		for (int i=1; i <= rsmd.getColumnCount(); i++) {
			columnExcluded[i-1] = false;
		}
		
		int excludedCount = 0;
		if (excludeColumns != null) {
			for (int i=0; i < excludeColumns.length; i++) {
				if (excludeColumns[i] > 0 && excludeColumns[i] <= rsmd.getColumnCount()) {
					excludedCount++;
					columnExcluded[excludeColumns[i]-1] = true;
				}
			}
		}
		
		originalIndices = new int[rsmd.getColumnCount() - excludedCount];
		dataProviderIndices  = new int[rsmd.getColumnCount()];
		columnTypes = new int[rsmd.getColumnCount() - excludedCount];
		for (int i=1, r=0; i <= rsmd.getColumnCount(); i++) {
			if (columnExcluded[i-1]) {
				dataProviderIndices[i-1] = -1;
			} else {
				dataProviderIndices[i-1] = r+1;
				originalIndices[r] = i;
				columnTypes[r] = rsmd.getColumnType(i);
				r++;
			}
		}
		if (resultSet.next()) {
			nextRow = new DataProviderRow(columnTypes, resultSet, originalIndices, keyColumns);
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
				nextRow = new DataProviderRow(columnTypes, resultSet, originalIndices, keyColumns);
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
	public boolean isActive() {
		return currentRow != null;
	}

	@Override
	public boolean hasDuplicateKey() throws DataException {
		return hasDuplicateKey && nextHasDuplicateKey;
	}

	@Override
	public int getIndexInDuplicates() throws DataException {
		return indexInDuplicates;
	}
}
