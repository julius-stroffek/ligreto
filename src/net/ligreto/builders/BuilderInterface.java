package net.ligreto.builders;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.ligreto.LigretoParameters;
import net.ligreto.exceptions.InvalidTargetException;
import net.ligreto.exceptions.LigretoException;

public interface BuilderInterface {
	
	/** The types of the headers. */
	public enum HeaderType {TOP, ROW}
	
	/** Keep the enumeration of the possible cell formatting changes. */
	public enum CellFormat {
		UNCHANGED,
		PERCENTAGE_NO_DECIMAL_DIGITS,
		PERCENTAGE_2_DECIMAL_DIGITS,
		PERCENTAGE_3_DECIMAL_DIGITS
	};
	
	/** The string representation of NULL values. */
	public static final String NULL = "";
	
	/**
	 * This method will set the value in the output report at the i-th position
	 * and taking the rsi-th position value from the specified result set.
	 * 
	 * @param i The report output column index.
	 * @param rs The result set where to get the data.
	 * @param rsi The result set index number where to get the value from.
	 * @throws SQLException in case of database related problems.
	 * 
	 * <p>
	 * The first output column index is 0.
	 * </p><p>
	 * The first result set column index is 1.
	 * </p>
	 */
	public abstract void setColumn(int i, ResultSet rs, int rsi) throws SQLException;

	/**
	 * This method will set the value in the output report at the i-th position
	 * and taking the i-th position value from the specified result set.
	 * 
	 * @param i The report output column index.
	 * @param rs The result set where to get the data.
	 * @throws SQLException in case of database related problems.
	 * 
	 * <p>
	 * The first output column index is 1.
	 * </p>
	 */
	public abstract void setColumn(int i, ResultSet rs) throws SQLException;

	/** Set up the template file. */
	public abstract void setTemplate(String template);

	/** Set up the output file name. */
	public abstract void setOutput(String output);

	/** 
	 * Move the output processing to the next row. The <code>highlightArray</code>
	 * is replaced with null. The row number is increased and the actual column is
	 * set to the base column value.
	 * @throws IOException 
	 */
	public abstract void nextRow() throws IOException;

	/** Shift the actual column position by the specified number of columns. */
	public abstract void setColumnPosition(int column);

	/**
	 * This method sets up the array that determines the highlighting of the actually processed row.
	 * 
	 * @param highlightArray
	 * 
	 * <p>
	 * The highlight array will be erased by the call to <code>nextRow</code> method.
	 * </p>
	 */
	public abstract void setHighlightArray(int[] highlightArray);

	/**
	 * This method sets the column position for <code>setColumn</code> function. The position
	 * have to be specified relatively to the <code>baseColumn</code> position.
	 * 
	 * @param column The relative position to <code>baseColumn</code>.
	 * @param step The number of cells to be skipped between column <code>i</code> and <code>i+1</code>. 
	 * @param highlightArray The array which determines the highlighting of the columns in the row
	 *                       actually processed.
	 */
	public abstract void setColumnPosition(int column, int step, int[] highlightArray);

	/**
	 * Store the specified object into the result column of the current row.
	 * 
	 * @param i The column index relative to <code>actColumn</code> position.
	 * @param o The object which value should be stored.
	 * @param cellFormat The change in the cell formatting.
	 * 
	 * <p>
	 * The method will automatically determine the highlight color to be used.
	 * </p>
	 */
	public abstract void setColumn(int i, Object o, CellFormat cellFormat);
	
	/**
	 * Store the specified object into the result column of the current row.
	 * 
	 * @param i The column index relative to <code>actColumn</code> position.
	 * @param o The object which value should be stored.
	 * @param cellFormat The change in the cell formatting.
	 * @param highlight Indicates whether to highlight the cell content.
	 */
	public abstract void setColumn(int i, Object o, CellFormat cellFormat, boolean highlight);

	/**
	 * Sets up the report type specific options. 
	 *
	 * @throws LigretoException
	 */
	public abstract void setOptions(Iterable<String> options) throws LigretoException;

	/**
	 * Store the specified object into the result column of the current row.
	 * 
	 * @param i The column index relative to <code>actColumn</code> position.
	 * @param o The object which value should be stored.
	 * @param rgb The text color to use.
	 */
	public abstract void setColumn(int i, Object o, short[] rgb, CellFormat cellFormat);

	/**
	 * Store the specified object into the result column of the header row.
	 * 
	 * @param i The column index relative to <code>actColumn</code> position.
	 * @param o The object which value should be stored.
	 */
	public abstract void setHeaderColumn(int i, Object o, HeaderType headerType);

	/**
	 * This method will setup the 
	 * @param target The target location to set.
	 * @param append Indicates whether the data should be appended.
	 * @throws InvalidTargetException
	 */
	public abstract void setTarget(String target, boolean append) throws InvalidTargetException;

	public abstract void start() throws IOException, LigretoException;

	public abstract void writeOutput() throws IOException;

	public abstract void dumpHeader(ResultSet rs, int[] excl) throws SQLException, IOException;

	public abstract void dumpJoinOnHeader(ResultSet rs, int[] on) throws SQLException;

	public abstract void dumpOtherHeader(ResultSet rs, int[] on, int[] excl) throws SQLException;

	public abstract void setJoinOnColumns(ResultSet rs, int[] on) throws SQLException;

	public abstract void setOtherColumns(ResultSet rs, int[] on, int[] excl) throws SQLException;

	/**
	 * Sets the difference highlighting option
	 * 
	 * @param highlight
	 */
	public abstract void setHighlight(boolean highlight);

	/**
	 * Sets the difference highlighting color
	 * 
	 * @param rgbHlColor The RGB color to set
	 */
	public abstract void setHlColor(short[] rgbHlColor);
	
	/**
	 * Sets up the global ligreto parameters object.
	 * @param ligretoParameters the parameter object to set.
	 */
	public void setLigretoParameters(LigretoParameters ligretoParameters);
}