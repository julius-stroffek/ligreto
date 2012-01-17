package net.ligreto.data;

import java.math.BigDecimal;
import java.sql.Timestamp;

import net.ligreto.exceptions.DataException;

public abstract class DataProvider {
	
	public abstract ResultSetMetaData getMetaData() throws DataException;

	public abstract boolean next() throws DataException;

	public abstract boolean getBoolean(int index) throws DataException;

	public abstract long getLong(int index) throws DataException;

	public abstract double getDouble(int index) throws DataException;

	public abstract Timestamp getTimestamp(int index) throws DataException;

	public abstract BigDecimal getBigDecimal(int index) throws DataException;
	
	public abstract Object getObject(int index) throws DataException;

	public abstract boolean wasNull() throws DataException;

	public abstract String getString(int index) throws DataException;
}
