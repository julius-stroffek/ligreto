package net.ligreto.util;

/**
 * Encapsulates the array of {@code Field} objects. It is required to be
 * for effective hashing.
 * 
 * @author Julius Stroffek
 *
 */
public class Fields implements Comparable<Object> {
	
	/** The array of field values. */
	protected Field[] fields = null;
	
	@Override
	public int hashCode() {
		if (fields == null)
			return 0;	
		int hash = 0;
		for (int i=0; i < fields.length; i++) {
			hash += fields[i].hashCode();
		}
		return hash;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Fields) {
			Fields f = (Fields) o;
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
	 * @return the fields
	 */
	public Field[] getFields() {
		return fields;
	}

	/**
	 * @param fields the fields to set
	 */
	public void setFields(Field[] fields) {
		this.fields = fields;
	}

	@Override
	public int compareTo(Object obj) {
		if (!(obj instanceof Fields))
			throw new IllegalArgumentException("Could not compare Fields against other objects.");
		Fields fObj = (Fields) obj;
		int result = 0;
		for (int i=0; i < fields.length; i++) {
			result = fields[i].compareTo(fObj.getFields()[i]);
			if (result != 0)
				break;
		}
		return result;
	}
}
