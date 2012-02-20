package net.ligreto.data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import net.ligreto.exceptions.DataException;

public abstract class DataProvider {
	
	public abstract boolean next() throws DataException;

	public abstract Boolean getBoolean(int index) throws DataException;

	public abstract Integer getInteger(int index) throws DataException;
	
	public abstract Long getLong(int index) throws DataException;

	public abstract Double getDouble(int index) throws DataException;

	public abstract Timestamp getTimestamp(int index) throws DataException;

	public abstract BigDecimal getBigDecimal(int index) throws DataException;
	
	public abstract Object getObject(int index) throws DataException;

	public abstract String getString(int index) throws DataException;
	
	public abstract int getColumnType(int index) throws DataException;
	
	public abstract String getColumnLabel(int index) throws DataException;
	
	public abstract String getColumnName(int index) throws DataException;
	
	public abstract int getColumnCount() throws DataException;
	
	/**
	 * @param index the index of the column in the data provider
	 * @return the original index including the excluded columns
	 * @throws DataException
	 */
	public abstract int getOriginalIndex(int index) throws DataException;
	
	/**
	 * 
	 * @param originalIndex the original index including the excluded columns
	 * @return the index of the column in the data provider
	 * @throws DataException
	 */
	public abstract int getIndex(int originalIndex) throws DataException;

	public abstract boolean wasNull() throws DataException;

	public abstract boolean isNumeric(int index) throws DataException;

	public abstract Time getTime(int index) throws DataException;

	public abstract Date getDate(int index) throws DataException;
}
