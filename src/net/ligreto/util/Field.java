package net.ligreto.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * This class is used to copy the column values from the database result
 * set and store them for later usage. It is mainly used for finding
 * duplicate entries with the same join columns and aggregated result
 * calculation.
 * 
 * @author Julius Stroffek
 *
 */
public class Field implements Comparable<Object> {

	/** The column type that correspond to java.sql.Types definitions. */
	public int columnType;
	
	/** The column value. */
	public Object columnValue;

	/**
	 * Creates the instance from the result set. 
	 * @throws SQLException
	 */
	public Field(ResultSet rs, int index) throws SQLException {
		columnType = rs.getMetaData().getColumnType(index);
		switch (columnType) {
		case Types.BOOLEAN:
			columnValue = new Boolean(rs.getBoolean(index));
			break;
		case Types.BIGINT:
		case Types.INTEGER:
			columnValue = new Long(rs.getLong(index));
			break;
		case Types.DOUBLE:
		case Types.FLOAT:
			columnValue = new Double(rs.getDouble(index));
			break;
		case Types.DATE:
		case Types.TIMESTAMP:
		case Types.TIME:
			columnValue = new Timestamp(rs.getTimestamp(index).getTime());
			break;
		case Types.DECIMAL:
		case Types.NUMERIC:
			BigDecimal bd = rs.getBigDecimal(index);
			columnValue = new BigDecimal(bd.unscaledValue(), bd.scale());
			break;
		default:
			columnType = Types.VARCHAR;
			String tmpValue = rs.getString(index);
			if (tmpValue != null)
				columnValue = new String(tmpValue);
			else
				columnValue = null;
			break;			
		}
		if (rs.wasNull())
			columnValue = null;
	}

	/**
	 * Function required for effective hashing.
	 */
	public int hashCode() {
		return columnValue != null ? columnValue.hashCode() : 0;
	}

	/**
	 * Function required for effective hashing.
	 */
	public boolean equals(Object o) {
		if (o instanceof Field) {
			Field f = (Field) o;
			if (columnType != f.columnType)
				return false;
			if (columnValue == null && f.columnValue == null)
				return true;
			if (columnValue == null || f.columnValue == null)
				return false;
			return columnValue.equals(f.columnValue);
		} else {
			return false;
		}
	}

	/**
	 * @return the columnType
	 */
	public int getColumnType() {
		return columnType;
	}

	/**
	 * @param columnType the columnType to set
	 */
	public void setColumnType(int columnType) {
		this.columnType = columnType;
	}

	/**
	 * @return the columnValue
	 */
	public Object getColumnValue() {
		return columnValue;
	}

	/**
	 * @param columnValue the columnValue to set
	 */
	public void setColumnValue(Object columnValue) {
		this.columnValue = columnValue;
	}

	@Override
	public int compareTo(Object obj) {
		if (!(obj instanceof Field))
			throw new IllegalArgumentException("Could not compare Field against other objects.");
		
		Field f = (Field) obj;
		
		Object o1 = getColumnValue();
		Object o2 = f.getColumnValue();
		
		// deal with null values first
		if (o1 == null && o2 == null)
			return 0;
		if (o1 == null)
			return -1;
		if (o2 == null)
			return 1;
		
		// Compare strings if classes do not match
		if (o1.getClass() != o2.getClass()) {
			return o1.toString().compareTo(o2.toString());
		}
		
		@SuppressWarnings("rawtypes")
		Comparable comp1 = (Comparable) o1;
		@SuppressWarnings("unchecked")
		int result = comp1.compareTo(o2);
		return result;
	}
}