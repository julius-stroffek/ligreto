package net.ligreto.exceptions;

public class DuplicateKeyColumnsException extends LigretoException {

	/**
	 * The default serial version ID.
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateKeyColumnsException() {
	}

	public DuplicateKeyColumnsException(String message) {
		super(message);
	}

	public DuplicateKeyColumnsException(Throwable cause) {
		super(cause);
	}

	public DuplicateKeyColumnsException(String message, Throwable cause) {
		super(message, cause);
	}

}
