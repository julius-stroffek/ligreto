/**
 * 
 */
package net.ligreto.exceptions;

/**
 * @author Julius Stroffek
 *
 */
public class UnimplementedMethodException extends RuntimeException {

	/**
	 * The dummy version ID
	 */
	private static final long serialVersionUID = 1L;

	public UnimplementedMethodException() {
	}

	public UnimplementedMethodException(String message) {
		super(message);
	}

	public UnimplementedMethodException(Throwable cause) {
		super(cause);
	}

	public UnimplementedMethodException(String message, Throwable cause) {
		super(message, cause);
	}
}
