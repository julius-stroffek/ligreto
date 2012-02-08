package net.ligreto.exceptions;

public class ParserException extends LigretoException {
	
	/**
	 * The default serial version ID.
	 */
	private static final long serialVersionUID = 1L;

	public ParserException() {
	}

	public ParserException(String message) {
		super(message);
	}

	public ParserException(Throwable cause) {
		super(cause);
	}

	public ParserException(String message, Throwable cause) {
		super(message, cause);
	}
}
