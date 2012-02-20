package net.ligreto.builders;

import net.ligreto.exceptions.ReportException;

public class TargetException extends ReportException {

	/**
	 * The default serial version ID.
	 */
	private static final long serialVersionUID = 1L;

	public TargetException() {
	}

	public TargetException(String message) {
		super(message);
	}

	public TargetException(Throwable cause) {
		super(cause);
	}

	public TargetException(String message, Throwable cause) {
		super(message, cause);
	}

}
