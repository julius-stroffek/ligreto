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
import net.ligreto.util.Assert;

public class ResultSetDataProvider extends DataProvider {
	
	protected ResultSet resultSet;
	protected int[] originalIndices;
	protected int[] dataProviderIndices;
	
	public ResultSetDataProvider(ResultSet resultSet) throws SQLException {
		this.resultSet = resultSet;
		ResultSetMetaData rsmd = resultSet.getMetaData();
		originalIndices = new int[rsmd.getColumnCount()];
		dataProviderIndices  = new int[rsmd.getColumnCount()];
		for (int i=0; i < rsmd.getColumnCount(); i++) {
			originalIndices[i] = i+1;
			dataProviderIndices[i] = i+1;
		}
	}

	public ResultSetDataProvider(ResultSet resultSet, int[] excludeColumns) throws SQLException {
		this.resultSet = resultSet;
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
		for (int i=1, r=0; i <= rsmd.getColumnCount(); i++) {
			if (columnExcluded[i-1]) {
				dataProviderIndices[i-1] = -1;
			} else {
				dataProviderIndices[i-1] = r+1;
				originalIndices[r] = i;
				r++;
			}
		}
	}

	@Override
	public boolean next() throws DataException {
		try {
			return resultSet.next();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public Boolean getBoolean(int index) throws DataException {
		Assert.assertTrue(index > 0 && index <= originalIndices.length);
		try {
			Boolean result = resultSet.getBoolean(originalIndices[index-1]);
			if (resultSet.wasNull())
				return null;
			else
				return result;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public Integer getInteger(int index) throws DataException {
		Assert.assertTrue(index > 0 && index <= originalIndices.length);
		try {
			Integer result = resultSet.getInt(originalIndices[index-1]);
			if (resultSet.wasNull())
				return null;
			else
				return result;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public Long getLong(int index) throws DataException {
		Assert.assertTrue(index > 0 && index <= originalIndices.length);
		try {
			Long result = resultSet.getLong(originalIndices[index-1]);
			if (resultSet.wasNull())
				return null;
			else
				return result;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public Double getDouble(int index) throws DataException {
		Assert.assertTrue(index > 0 && index <= originalIndices.length);
		try {
			Double result = resultSet.getDouble(originalIndices[index-1]);
			if (resultSet.wasNull())
				return null;
			else
				return result;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public Timestamp getTimestamp(int index) throws DataException {
		Assert.assertTrue(index > 0 && index <= originalIndices.length);
		try {
			Timestamp result = resultSet.getTimestamp(originalIndices[index-1]);
			if (resultSet.wasNull())
				return null;
			else
				return result;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public BigDecimal getBigDecimal(int index) throws DataException {
		Assert.assertTrue(index > 0 && index <= originalIndices.length);
		try {
			BigDecimal result = resultSet.getBigDecimal(originalIndices[index-1]);
			if (resultSet.wasNull())
				return null;
			else
				return result;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public String getString(int index) throws DataException {
		Assert.assertTrue(index > 0 && index <= originalIndices.length);
		try {
			String result = resultSet.getString(originalIndices[index-1]);
			if (resultSet.wasNull())
				return null;
			else
				return result;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public Time getTime(int index) throws DataException {
		Assert.assertTrue(index > 0 && index <= originalIndices.length);
		Time result;
		try {
			result = resultSet.getTime(originalIndices[index-1]);
			if (resultSet.wasNull())
				return null;
			else
				return result;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public Date getDate(int index) throws DataException {
		Assert.assertTrue(index > 0 && index <= originalIndices.length);
		Date result;
		try {
			result = resultSet.getDate(originalIndices[index-1]);
			if (resultSet.wasNull())
				return null;
			else
				return result;
		} catch (SQLException e) {
			throw new DataException(e);
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
		Assert.assertTrue(index > 0 && index <= originalIndices.length);
		try {
			return resultSet.getMetaData().getColumnType(originalIndices[index-1]);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public String getColumnLabel(int index) throws DataException {
		Assert.assertTrue(index > 0 && index <= originalIndices.length);
		try {
			return resultSet.getMetaData().getColumnLabel(originalIndices[index-1]);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public String getColumnName(int index) throws DataException {
		Assert.assertTrue(index > 0 && index <= originalIndices.length);
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
		Assert.assertTrue(index > 0 && index <= originalIndices.length);
		return originalIndices[index-1];
	}

	@Override
	public int getIndex(int originalIndex) throws DataException {
		try {
			Assert.assertTrue(originalIndex > 0 && originalIndex <= resultSet.getMetaData().getColumnCount());
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return dataProviderIndices[originalIndex-1];
	}

	@Override
	public boolean wasNull() throws DataException {
		try {
			return resultSet.wasNull();
		} catch (SQLException e) {
			throw new DataException(e);
		}
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
	public boolean isActive() throws DataException {
		// TODO Auto-generated method stub
		try {
			return !resultSet.isBeforeFirst() && !resultSet.isAfterLast();
		} catch (SQLException e) {
			throw new DataException("Error on data source.", e);
		}
	}

	@Override
	public boolean hasDuplicateKey() throws DataException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getIndexInDuplicates() throws DataException {
		// TODO Auto-generated method stub
		return 0;
	}
}
