package net.ligreto.data;

/**
 * Encapsulates the array of {@code Column} objects. It is required to be
 * for effective hashing.
 * 
 * @author Julius Stroffek
 *
 */
public class Row implements Comparable<Object> {
	
	/** The array of field values. */
	protected Column[] columns = null;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		if (columns == null)
			return 0;	
		int hColCount = 2;
		if (hColCount > columns.length) {
			hColCount = columns.length;
		}
		int hash = 0;
		for (int i=0; i < hColCount; i++) {
			hash = prime*hash + columns[i].hashCode();
		}
		return hash;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Row) {
			Row f = (Row) o;
			if (columns == null && f.columns == null)
				return true;
			if (columns == null || f.columns == null)
				return false;
			if (columns.length != f.columns.length)
				return false;
			for (int i=0; i < columns.length; i++) {
				if (!columns[i].equals(f.columns[i])) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return the columns
	 */
	public Column[] getFields() {
		return columns;
	}

	/**
	 * @param fields the fields to set
	 */
	public void setFields(Column[] fields) {
		this.columns = fields;
	}

	@Override
	public int compareTo(Object obj) {
		if (!(obj instanceof Row))
			throw new IllegalArgumentException("Could not compare Row against other objects.");
		Row fObj = (Row) obj;
		int result = 0;
		for (int i=0; i < columns.length; i++) {
			result = columns[i].compareTo(fObj.getFields()[i]);
			if (result != 0)
				break;
		}
		return result;
	}
}
