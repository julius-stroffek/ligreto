package net.ligreto.data;

/**
 * Encapsulates the array of {@code Column} objects. It is required to be
 * a separate class due to efficient hashing to {@code HashMap}-s.
 * 
 * @author Julius Stroffek
 *
 */
public class Row implements Comparable<Object> {
	
	/** The array of field values. */
	protected Field[] fields = null;
	
	/** Creates the empty row with no fields. */
	public Row() {	
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		if (fields == null)
			return 0;	
		int hColCount = 2;
		if (hColCount > fields.length) {
			hColCount = fields.length;
		}
		int hash = 0;
		for (int i=0; i < hColCount; i++) {
			hash = prime*hash + fields[i].hashCode();
		}
		return hash;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Row) {
			Row f = (Row) o;
			if (fields == null && f.fields == null)
				return true;
			if (fields == null || f.fields == null)
				return false;
			if (fields.length != f.fields.length)
				return false;
			for (int i=0; i < fields.length; i++) {
				if (!fields[i].equals(f.fields[i])) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Return the fields of this row.
	 * 
	 * @return the fields of this row
	 */
	public Field[] getFields() {
		return fields;
	}

	/**
	 * Set the fields for this row.
	 * 
	 * @param fields the fields to set
	 */
	public void setFields(Field[] fields) {
		this.fields = fields;
	}

	@Override
	public int compareTo(Object obj) {
		if (!(obj instanceof Row))
			throw new IllegalArgumentException("Could not compare Row against other objects.");
		Row fObj = (Row) obj;
		int result = 0;
		for (int i=0; i < fields.length; i++) {
			result = fields[i].compareTo(fObj.getFields()[i]);
			if (result != 0)
				break;
		}
		return result;
	}
}
