package net.ligreto.exceptions;

public class DataSourceException extends LigretoException {

	/**
	 * The default serial version ID.
	 */
	private static final long serialVersionUID = 1L;

	public DataSourceException() {
	}

	public DataSourceException(String message) {
		super(message);
	}

	public DataSourceException(Throwable cause) {
		super(cause);
	}

	public DataSourceException(String message, Throwable cause) {
		super(message, cause);
	}

}
