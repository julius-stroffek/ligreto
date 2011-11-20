package net.ligreto.exceptions;

public class CollationException extends LigretoException {

	/**
	 * The default serial version ID.
	 */
	private static final long serialVersionUID = 1L;

	public CollationException() {
	}

	public CollationException(String message) {
		super(message);
	}

	public CollationException(Throwable cause) {
		super(cause);
	}

	public CollationException(String message, Throwable cause) {
		super(message, cause);
	}

}
