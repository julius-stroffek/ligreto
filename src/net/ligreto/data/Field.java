package net.ligreto.data;

import net.ligreto.exceptions.DataException;
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
public class Field implements Comparable<Object> {

	/** The column type that correspond to java.sql.Types definitions. */
	protected int columnType;
	
	/** The column value. */
	protected Object columnValue;
	
	/** Indicates whether this field is of a numeric type. */
	protected boolean numeric;

	/**
	 * Creates the instance from the result set. 
	 * @throws DataException 
	 */
	public Field(DataProvider dp, int index) throws DataException {
		columnType = dp.getColumnType(index);
		columnValue = dp.getObject(index);
		numeric = dp.isNumeric(index);
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
		if (obj == null) {
			return LigretoComparator.getInstance().compareNulls(false, true);
		}
		if (!(obj instanceof Field))
			throw new IllegalArgumentException("Could not compare Column against other objects.");
		
		Field f = (Field) obj;
		try {
			return LigretoComparator.getInstance().compare(this, f);
		} catch (DataException e) {
			throw new IllegalArgumentException(e);
		}
	}
}