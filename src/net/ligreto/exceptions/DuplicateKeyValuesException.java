package net.ligreto.exceptions;

public class DuplicateKeyValuesException extends LigretoException {

	/**
	 * The default serial version ID.
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateKeyValuesException() {
	}

	public DuplicateKeyValuesException(String message) {
		super(message);
	}

	public DuplicateKeyValuesException(Throwable cause) {
		super(cause);
	}

	public DuplicateKeyValuesException(String message, Throwable cause) {
		super(message, cause);
	}

}
