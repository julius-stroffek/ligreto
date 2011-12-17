package net.ligreto.util;


public class Fields {
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
			return true; // TODO: FIX ME
		} else {
			return false;
		}
	}
}
