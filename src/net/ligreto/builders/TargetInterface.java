package net.ligreto.builders;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.CellFormat;
import net.ligreto.builders.BuilderInterface.HeaderType;
import net.ligreto.data.DataProvider;
import net.ligreto.exceptions.DataException;

public interface TargetInterface {
	
	/**
	 * This method will set the value in the output report at the i-th position
	 * and taking the rsi-th position value from the specified result set.
	 * 
	 * @param i The report output column index.
	 * @param dp The data provider where to get the data.
	 * @param dpi The data provider index number where to get the value from.
	 * @throws SQLException in case of database related problems.
	 * 
	 * <p>
	 * The first output column index is 0.
	 * </p><p>
	 * The first result set column index is 1.
	 * </p>
	 */
	public abstract void dumpColumn(int i, DataProvider dp, int rsi) throws DataException;

	/**
	 * This method will set the value in the output report at the i-th position
	 * and taking the i-th position value from the specified result set.
	 * 
	 * @param i The report output column index.
	 * @param dp The data provider where to get the data.
	 * @throws SQLException in case of database related problems.
	 * 
	 * <p>
	 * The first output column index is 1.
	 * </p>
	 */
	public abstract void dumpColumn(int i, DataProvider dp) throws DataException;

	/** 
	 * Move the output processing to the next row. The <code>highlightArray</code>
	 * is replaced with null. The row number is increased and the actual column is
	 * set to the base column value.
	 * @throws IOException 
	 */
	public abstract void nextRow() throws IOException;
	
	/** This method have to be called after all the target related data are dumped. 
	 * @throws IOException */
	public abstract void finish() throws IOException;

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
	public abstract void dumpColumn(int i, Object o, CellFormat cellFormat);
	
	/**
	 * Store the specified object into the result column of the current row.
	 * 
	 * @param i The column index relative to <code>actColumn</code> position.
	 * @param o The object which value should be stored.
	 * @param cellFormat The change in the cell formatting.
	 * @param highlight Indicates whether to highlight the cell content.
	 */
	public abstract void dumpColumn(int i, Object o, CellFormat cellFormat, boolean highlight);

	public abstract void dumpHeader(DataProvider dp, int[] excl) throws DataException, IOException;

	public abstract void dumpJoinOnHeader(DataProvider dp, int[] on, String dataSourceDesc) throws DataException;

	public abstract void dumpOtherHeader(DataProvider dp, int[] on, int[] excl, String dataSourceDesc) throws DataException;

	public abstract void dumpJoinOnColumns(DataProvider dp, int[] on) throws DataException;

	public abstract void dumpOtherColumns(DataProvider dp, int[] on, int[] excl) throws DataException;

	/**
	 * Store the specified object into the result column of the current row.
	 * 
	 * @param i The column index relative to <code>actColumn</code> position.
	 * @param o The object which value should be stored.
	 * @param rgb The text color to use.
	 */
	public abstract void dumpColumn(int i, Object o, short[] rgb, CellFormat cellFormat);

	/**
	 * Store the specified object into the result column of the header row.
	 * 
	 * @param i The column index relative to <code>actColumn</code> position.
	 * @param o The object which value should be stored.
	 */
	public abstract void dumpHeaderColumn(int i, Object o, HeaderType headerType);

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
	 * Sets up the ligreto parameters to be used.
	 * @param ligretoParameters
	 */
	public abstract void setLigretoParameters(LigretoParameters ligretoParameters);
}
