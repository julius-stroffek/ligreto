/**
 * 
 */
package net.ligreto.exceptions;

/**
 * @author Julius Stroffek
 *
 */
public class AssertionException extends RuntimeException {

	/**
	 * The default serial version ID.
	 */
	private static final long serialVersionUID = 1L;

	public AssertionException() {
	}

	public AssertionException(String message) {
		super(message);
	}

	public AssertionException(Throwable cause) {
		super(cause);
	}

	public AssertionException(String message, Throwable cause) {
		super(message, cause);
	}

}
