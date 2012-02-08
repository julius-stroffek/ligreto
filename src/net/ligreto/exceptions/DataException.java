package net.ligreto.exceptions;

public class DataException extends LigretoException {
	
	/**
	 * The default serial version ID.
	 */
	private static final long serialVersionUID = 1L;

	public DataException() {
	}

	public DataException(String message) {
		super(message);
	}

	public DataException(Throwable cause) {
		super(cause);
	}

	public DataException(String message, Throwable cause) {
		super(message, cause);
	}
}
