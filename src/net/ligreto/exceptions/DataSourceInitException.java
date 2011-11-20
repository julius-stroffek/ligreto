package net.ligreto.exceptions;

public class DataSourceInitException extends DataSourceException {

	/**
	 * The default serial version ID.
	 */
	private static final long serialVersionUID = 1L;

	public DataSourceInitException() {
	}

	public DataSourceInitException(String message) {
		super(message);
	}

	public DataSourceInitException(Throwable cause) {
		super(cause);
	}

	public DataSourceInitException(String message, Throwable cause) {
		super(message, cause);
	}

}
