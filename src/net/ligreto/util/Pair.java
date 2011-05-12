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
