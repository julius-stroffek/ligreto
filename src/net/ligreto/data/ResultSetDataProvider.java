package net.ligreto.data;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import net.ligreto.exceptions.DataException;

public class ResultSetDataProvider extends DataProvider {
	
	protected ResultSet resultSet;
	
	public ResultSetDataProvider(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	@Override
	public ResultSetMetaData getMetaData() throws DataException {
		try {
			return new ResultSetMetaData(resultSet.getMetaData());
		} catch (SQLException e) {
			throw new DataException(e);
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
	public boolean getBoolean(int index) throws DataException {
		try {
			return resultSet.getBoolean(index);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public long getLong(int index) throws DataException {
		try {
			return resultSet.getLong(index);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public double getDouble(int index) throws DataException {
		try {
			return resultSet.getLong(index);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public Timestamp getTimestamp(int index) throws DataException {
		try {
			return resultSet.getTimestamp(index);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public BigDecimal getBigDecimal(int index) throws DataException {
		try {
			return resultSet.getBigDecimal(index);
		} catch (SQLException e) {
			throw new DataException(e);
		}
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
	public String getString(int index) throws DataException {
		try {
			return resultSet.getString(index);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Override
	public Object getObject(int index) throws DataException {
		try {
			return resultSet.getObject(index);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
}
