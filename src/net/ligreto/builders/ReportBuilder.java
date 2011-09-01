package net.ligreto.builders;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.ligreto.exceptions.InvalidTargetException;
import net.ligreto.exceptions.UnimplementedMethodException;
import net.ligreto.parser.nodes.*;
import net.ligreto.util.MiscUtils;

/**
 * This class defines the interface between the various report executors
 * and the target report format. The main type of the output report format
 * is excel spreadsheet. However, any type of the output report format could
 * be used if the below interface could be implemented.
 * 
 * This is the overall overview on what methods should be called during
 * life-cycle of this class in pseudo-code as called by the executors.
 * 
 * <pre>
 *   setTemplate("Template.xls");
 *   setOutput("Output.xls");
 *   
 *   for (tgt : targets) {
 *   	setTarget("A target ID string"); // For spreadsheet - e.g. "B:2"
 *   	for (row : rows) {
 *   		nextRow();
 *   		// Dump the i-th column from the ResultSet
 *   		setColumn(i, rs); 
 *   		// Could dump more columns here
 *   		setColumnPosition(10);
 *   		// Could dump more columns here to shifted location
 *   	}
 *   }
 *   
 * </pre>
 *
 * The report builder could be automatically highlighting the differences
 * or based on the other reason by invoking <code>setHighlightArray</code>
 * before the call to any <code>setCell</code> methods.
 *  
 * @author Julius Stroffek
 *
 */
public abstract class ReportBuilder {
	
	/** The string representation of NULL values. */
	public static final String NULL="";
	
	/** 
	 * The type of the report to be generated. This corresponds to the proper type
	 *  of the <code>ReportBuilder</code> class instance.
	 */
	protected ReportNode.ReportType reportType;
	
	/** The template file name. */
	protected String template;
	
	/** The output file name. */
	protected String output;
	
	/** The parent node from the XML file for reference to other objects. */
	protected LigretoNode ligretoNode;
	
	/** The number of columns to shift right after the call to <code>setColumn</code>. */
	protected int columnStep = 1;
	
	/**
	 * The base row which is defined by the call to <code>setTarget</code>.
	 *
	 * The numbering starts from 0.
	 */
	protected int baseRow = 0;
	
	/**
	 * The base column which is defined by the call to <code>setTarget</code>.
	 * 
	 * The numbering starts from 0.
	 */
	protected int baseCol = 0;
	
	/**
	 * The actual row number where the output is being produced.
	 * <p>
	 * Initially this starts one row before the base row as this
	 * will get shifted by the call to <code>nextRow</code>
	 * </p>
	 */
	protected int actRow = baseRow-1;
	
	/**
	 * The actual column number where the output is produced. The exact column number
	 * where the function <code>setColumn</code> stores the content is produced by taking
	 * this <code>actCol</code> value and adding the value of the column number argument
	 * specified in the <code>setColumn</code> method call.
	 */
	protected int actCol = baseCol;
	
	/**
	 * The array defining which columns should be highlighted by <code>setColumn</code>.
	 */
	protected int[] highlightArray = null;
	
	/** Indicates whether the columns should be highlighted. */
	protected boolean highlight;
	
	/** Specifies the highlight text color for highlighted cells. */
	protected short[] rgbHlColor;
	
	/** Nobody except child classes could create the instance. */
	protected ReportBuilder() {
	}
	
	/**
	 * This method will create the instance of the right child class
	 * corresponding to the report type specified.
	 * 
	 * @param ligretoNode The reference for the parent node.
	 * @param reportType The desired report type to be created.
	 * @return The instance of the child class of <code>ReportBuilder</code>
	 *         corresponding to the desired report type.
	 */
	public static ReportBuilder createInstance(LigretoNode ligretoNode, ReportNode.ReportType reportType) {
		ReportBuilder builder;
		switch (reportType) {
		case EXCEL:
			builder = new ExcelReportBuilder();
			break;
		case TEX:
			throw new UnimplementedMethodException();
		case XML:
			throw new UnimplementedMethodException();
		default:
			throw new UnimplementedMethodException();			
		}
		builder.reportType = reportType;
		builder.ligretoNode = ligretoNode;
		return builder;
	}
	
	/**
	 * This method will return the highlight color for the specified column number
	 * based on the array content defined by <code>setHighlightArray</code> method
	 * call.
	 * 
	 * @param i The column number.
	 * @return The corresponding highlight color or null.
	 */
	protected short[] getHlColor(int i) {
		int cmp = highlightArray != null ? highlightArray[i] : 0;
		if (cmp != 0 && highlight) {
			return rgbHlColor;
		}
		return null;
	}

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
	public void setColumn(int i, ResultSet rs, int rsi) throws SQLException {
		Object o = rs.getObject(rsi);
		if (rs.wasNull()) {
			setColumn(columnStep*i, NULL, getHlColor(i));
		} else {
			setColumn(columnStep*i, o, getHlColor(i));
		}
	}

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
	public void setColumn(int i, ResultSet rs) throws SQLException {
		setColumn(i-1, rs, i);
	}

	/** Set up the template file. */
	public void setTemplate(String template) {
		this.template = ligretoNode.substituteParams(template);
	}
	
	/** Set up the output file name. */
	public void setOutput(String output) {
		this.output = ligretoNode.substituteParams(output);
	}
	
	/** 
	 * Move the output processing to the next row. The <code>highlightArray</code>
	 * is replaced with null. The row number is increased and the actual column is
	 * set to the base column value.
	 */
	public void nextRow() {
		actRow++;
		actCol = baseCol;
		columnStep = 1;
		highlightArray = null;
	}
	
	/** Shift the actual column position by the specified number of columns. */
	public void setColumnPosition(int column) {
		actCol = baseCol + column;
	}

	/**
	 * This method sets up the array that determines the highlighting of the actually processed row.
	 * 
	 * @param highlightArray
	 * 
	 * <p>
	 * The highlight array will be erased by the call to <code>nextRow</code> method.
	 * </p>
	 */
	public void setHighlightArray(int[] highlightArray) {
		this.highlightArray = highlightArray;
	}
	
	/**
	 * This method sets the column position for <code>setColumn</code> function. The position
	 * have to be specified relatively to the <code>baseColumn</code> position.
	 * 
	 * @param column The relative position to <code>baseColumn</code>.
	 * @param step The number of cells to be skipped between column <code>i</code> and <code>i+1</code>. 
	 * @param highlightArray The array which determines the highlighting of the columns in the row
	 *                       actually processed.
	 */
	public void setColumnPosition(int column, int step, int[] highlightArray) {
		actCol = baseCol + column;
		columnStep = step;
		this.highlightArray = highlightArray;
	}

	/**
	 * Store the specified object into the result column of the current row.
	 * 
	 * @param i The column index relative to <code>actColumn</code> position.
	 * @param o The object which value should be stored.
	 * 
	 * <p>
	 * The method will automatically determine the highlight color to be used.
	 * </p>
	 */
	public void setColumn(int i, Object o) {
		setColumn(i, o, getHlColor(i));
	}

	/**
	 * Store the specified object into the result column of the current row.
	 * 
	 * @param i The column index relative to <code>actColumn</code> position.
	 * @param o The object which value should be stored.
	 */
	public abstract void setColumn(int i, Object o, short[] rgb);
	
	/**
	 * This method will setup the 
	 * @param target
	 * @param append
	 * @throws InvalidTargetException
	 */
	public abstract void setTarget(String target, boolean append) throws InvalidTargetException;
	public abstract void start() throws IOException;
	public abstract void writeOutput() throws IOException;

	public void dumpHeader(ResultSet rs, int[] excl) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		nextRow();
		for (int i=1, c=0; i <= rsmd.getColumnCount(); i++) {
			if (!MiscUtils.arrayContains(excl, i)) {
				setColumn(columnStep*c++, rsmd.getColumnLabel(i));
			}
		}
	}

	public void dumpJoinOnHeader(ResultSet rs, int[] on) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i=0; i < on.length; i++) {
			setColumn(columnStep*i, rsmd.getColumnLabel(on[i]));
		}
	}

	public void dumpOtherHeader(ResultSet rs, int[] on, int[] excl) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int rsLength = rsmd.getColumnCount();
		int idx = 0;
		for (int i=0; i < rsLength; i++) {
			boolean skip = false;
			if (MiscUtils.arrayContains(on, i+1)) {
				skip = true;
			}
			if (MiscUtils.arrayContains(excl, i+1)) {
				skip = true;
			}
			
			if (!skip) {
				setColumn(columnStep*idx, rsmd.getColumnLabel(i+1));
				idx++;
			}
		}
	}
	
	public void setJoinOnColumns(ResultSet rs, int[] on) throws SQLException {
		for (int i=0; i < on.length; i++) {
			setColumn(i, rs, on[i]);
		}
	}

	public void setOtherColumns(ResultSet rs, int[] on, int[] excl) throws SQLException {
		int rsLength = rs.getMetaData().getColumnCount();
		int idx = 0;
		for (int i=0; i < rsLength; i++) {
			boolean skip = false;
			if (MiscUtils.arrayContains(on, i+1)) {
				skip = true;
			}
			if (MiscUtils.arrayContains(excl, i+1)) {
				skip = true;
			}
			
			if (!skip) {
				setColumn(idx, rs, i+1);
				idx++;
			}
		}
	}

	/**
	 * Sets the difference highlighting option
	 * 
	 * @param highlight
	 */
	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}

	/**
	 * Sets the difference highlighting color
	 * 
	 * @param hlColor The color to set
	 */
	public void setHlColor(short[] rgbHlColor) {
		this.rgbHlColor = rgbHlColor;
	}
}
