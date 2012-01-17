package net.ligreto.data;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import net.ligreto.util.LigretoComparator;

/**
 * This class is used to copy the column values from the database result
 * set and store them for later usage. It is mainly used for finding
 * duplicate entries with the same join columns and aggregated result
 * calculation.
 * 
 * @author Julius Stroffek
 *
 */
public class Column implements Comparable<Object> {

	/** The column type that correspond to java.sql.Types definitions. */
	protected int columnType;
	
	/** The column value. */
	protected Object columnValue;
	
	/** Indicates whether this field is of a numeric type. */
	protected boolean numeric;

	/**
	 * Creates the instance from the result set. 
	 * @throws SQLException
	 */
	public Column(ResultSet rs, int index) throws SQLException {
		columnType = rs.getMetaData().getColumnType(index);
		numeric = false;
		switch (columnType) {
		case Types.BOOLEAN:
			columnValue = new Boolean(rs.getBoolean(index));
			break;
		case Types.BIGINT:
		case Types.INTEGER:
			columnValue = new Long(rs.getLong(index));
			numeric = true;
			break;
		case Types.DOUBLE:
		case Types.FLOAT:
			columnValue = new Double(rs.getDouble(index));
			numeric = true;
			break;
		case Types.DATE:
		case Types.TIMESTAMP:
		case Types.TIME:
			Timestamp ts = rs.getTimestamp(index);
			if (ts != null) {
				columnValue = new Timestamp(ts.getTime());
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
			numeric = true;
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
		if (o instanceof Column) {
			Column f = (Column) o;
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

	/**
	 * @return the numeric
	 */
	public boolean isNumeric() {
		return numeric;
	}

	/**
	 * @param numeric the numeric to set
	 */
	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
	}

	@Override
	public int compareTo(Object obj) {
		if (!(obj instanceof Column))
			throw new IllegalArgumentException("Could not compare Column against other objects.");
		
		Column f = (Column) obj;
		return LigretoComparator.getInstance().compare(this, f);
	}
}