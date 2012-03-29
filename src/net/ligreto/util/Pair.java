/**
 * 
 */
package net.ligreto.util;

/**
 * @author Julius Stroffek
 *
 */
public class Pair<A, B> {
	A first;
	B second;
	
	/** Constructs a Pair */
	public Pair(A a, B b) {
		this.first = a;
		this.second = b;
	}
	
	/**
	 * Calculates the hash code for the given object
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	/**
	 * Determines whether the specified object is equal to the current object.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Pair)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		Pair other = (Pair) obj;
		if (first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!first.equals(other.first)) {
			return false;
		}
		if (second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!second.equals(other.second)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the first
	 */
	public A getFirst() {
		return first;
	}
	
	/**
	 * @param first the first to set
	 */
	public void setFirst(A first) {
		this.first = first;
	}
	
	/**
	 * @return the second
	 */
	public B getSecond() {
		return second;
	}
	
	/**
	 * @param second the second to set
	 */
	public void setSecond(B second) {
		this.second = second;
	}
}
