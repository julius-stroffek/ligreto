package net.ligreto.exceptions;

import net.ligreto.util.DataProviderUtils;

public class DataTypeMismatchException extends LigretoException {

	/**
	 * The default serial version ID
	 */
	private static final long serialVersionUID = 1L;

	protected Integer column1Index = null;
	protected Integer column2Index = null;
	protected String column1Name = null;
	protected String column2Name = null;
	protected String column1Type = null;
	protected String column2Type = null;

	public DataTypeMismatchException() {
	}

	public DataTypeMismatchException(String message) {
		super(message);
	}

	public DataTypeMismatchException(Throwable cause) {
		super(cause);
	}

	public DataTypeMismatchException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public DataTypeMismatchException(int col1, int col2) {
		column1Type = DataProviderUtils.getJdbcTypeName(col1);
		column2Type = DataProviderUtils.getJdbcTypeName(col2);
	}
	
	public void setColumnNames(String col1, String col2) {
		column1Name = col1;
		column2Name = col2;
	}
	
	public void setColumnIndices(int col1, int col2) {
		column1Index = col1;
		column2Index = col2;
	}
	
	public void setColumnTypes(String col1, String col2) {
		column1Type = col1;
		column2Type = col2;
	}
	
	public void setColumnTypes(int col1, int col2) {
		column1Type = DataProviderUtils.getJdbcTypeName(col1);
		column2Type = DataProviderUtils.getJdbcTypeName(col2);
	}
	
	@Override
	public String getMessage() {
		StringBuffer sb = new StringBuffer();
		String msg = super.getMessage();
		if (msg != null && !"".equals(msg)) {
			sb.append(super.getMessage());
			sb.append("; ");
		}
		sb.append("Data types differ;");
		sb.append(String.format(" 1st: %s [%d];", column1Type, column1Index));
		sb.append(String.format(" 2nd: %s [%d];", column2Type, column2Index));
		return sb.toString();
	}
}
