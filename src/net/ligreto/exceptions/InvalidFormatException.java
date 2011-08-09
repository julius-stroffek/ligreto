package net.ligreto.exceptions;

/**
 * This is the exception thrown when invalid format was specified in various occasions.
 * 
 * @author Julius Stroffek
 *
 */
public class InvalidFormatException extends Exception {

	/**
	 * Auto-generated stuff
	 */
	private static final long serialVersionUID = 1L;

	public InvalidFormatException() {
	}

	public InvalidFormatException(String message) {
		super(message);
	}

	public InvalidFormatException(Throwable cause) {
		super(cause);
	}

	public InvalidFormatException(String message, Throwable cause) {
		super(message, cause);
	}

}
