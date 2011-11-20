package net.ligreto.exceptions;

public class DuplicateJoinColumnsException extends LigretoException {

	/**
	 * The default serial version ID.
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateJoinColumnsException() {
	}

	public DuplicateJoinColumnsException(String message) {
		super(message);
	}

	public DuplicateJoinColumnsException(Throwable cause) {
		super(cause);
	}

	public DuplicateJoinColumnsException(String message, Throwable cause) {
		super(message, cause);
	}

}
