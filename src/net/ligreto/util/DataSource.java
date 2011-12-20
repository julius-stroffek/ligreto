package net.ligreto.util;

import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;

public abstract class DataSource {
	
	public abstract ResultSetMetaData getMetaData();

	public abstract boolean next();

	public abstract String getBoolean(int index);

	public abstract String getLong(int index);

	public abstract String getDouble(int index);

	public abstract Timestamp getTimestamp(int index);

	public abstract BigDecimal getBigDecimal(int index);
	
	public abstract Object getObject(int index);

	public abstract boolean wasNull();

	public abstract String getString(int index);
}
