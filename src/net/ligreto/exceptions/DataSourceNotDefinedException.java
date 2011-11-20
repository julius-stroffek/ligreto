package net.ligreto.exceptions;

public class DataSourceNotDefinedException extends DataSourceException {

	/**
	 * The default serial version ID.
	 */
	private static final long serialVersionUID = 1L;

	public DataSourceNotDefinedException() {
	}

	public DataSourceNotDefinedException(String message) {
		super(message);
	}

	public DataSourceNotDefinedException(Throwable cause) {
		super(cause);
	}

	public DataSourceNotDefinedException(String message, Throwable cause) {
		super(message, cause);
	}
}
