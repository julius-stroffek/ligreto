package net.ligreto.builders;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.ligreto.exceptions.InvalidTargetException;
import net.ligreto.exceptions.LigretoException;
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
 *   		// Could dump more columns here using setColumn method call
 *   		setColumnPosition(10);
 *   		// Could dump more columns here to shifted location using setColumn method call
 *   	}
 *   }
 *   
 * </pre>
 *
 * The report builder could automatically highlight the differences
 * or otherwise highlight certain column values as specified
 * by invoking <code>setHighlightArray</code> before the call
 * to any <code>setCell</code> methods.
 *  
 * @author Julius Stroffek
 *
 */
public abstract class ReportBuilder implements BuilderInterface {
	
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
	public static BuilderInterface createInstance(LigretoNode ligretoNode, ReportNode.ReportType reportType) {
		ReportBuilder builder;
		switch (reportType) {
		case EXCEL:
			builder = new ExcelReportBuilder();
			break;
		case EXCELSTREAM:
			builder = new ExcelStreamReportBuilder();
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

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setColumn(int, java.sql.ResultSet, int)
	 */
	@Override
	public void setColumn(int i, ResultSet rs, int rsi) throws SQLException {
		Object o = rs.getObject(rsi);
		if (rs.wasNull()) {
			setColumn(columnStep*i, NULL, getHlColor(i), CellFormat.UNCHANGED);
		} else {
			setColumn(columnStep*i, o, getHlColor(i), CellFormat.UNCHANGED);
		}
	}

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setColumn(int, java.sql.ResultSet)
	 */
	@Override
	public void setColumn(int i, ResultSet rs) throws SQLException {
		setColumn(i-1, rs, i);
	}

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setTemplate(java.lang.String)
	 */
	@Override
	public void setTemplate(String template) {
		if (template != null) {
			this.template = ligretoNode.substituteParams(template);
		} else {
			this.template = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setOutput(java.lang.String)
	 */
	@Override
	public void setOutput(String output) {
		this.output = ligretoNode.substituteParams(output);
	}
	
	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#nextRow()
	 */
	@Override
	public void nextRow() {
		actRow++;
		actCol = baseCol;
		columnStep = 1;
		highlightArray = null;
	}
	
	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setColumnPosition(int)
	 */
	@Override
	public void setColumnPosition(int column) {
		actCol = baseCol + column;
	}

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setHighlightArray(int[])
	 */
	@Override
	public void setHighlightArray(int[] highlightArray) {
		this.highlightArray = highlightArray;
	}
	
	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setColumnPosition(int, int, int[])
	 */
	@Override
	public void setColumnPosition(int column, int step, int[] highlightArray) {
		actCol = baseCol + column;
		columnStep = step;
		this.highlightArray = highlightArray;
	}

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setColumn(int, java.lang.Object)
	 */
	@Override
	public void setColumn(int i, Object o, CellFormat cellFormat) {
		setColumn(i, o, getHlColor(i), cellFormat);
	}

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setOptions(java.lang.Iterable)
	 */
	@Override
	public abstract void setOptions(Iterable<String> options) throws LigretoException;

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setColumn(int, java.lang.Object, short[])
	 */
	@Override
	public abstract void setColumn(int i, Object o, short[] rgb, CellFormat cellFormat);
	
	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setHeaderColumn(int, java.lang.Object, net.ligreto.builders.ReportBuilder.HeaderType)
	 */
	@Override
	public void setHeaderColumn(int i, Object o, HeaderType headerType) {
		switch (headerType) {
		case TOP:
			setColumn(i, o, getHlColor(i), CellFormat.UNCHANGED);
			break;
		case ROW:
			setColumn(i, o, getHlColor(i), CellFormat.UNCHANGED);
			break;
		default:
			throw new RuntimeException("Unexpected value of HeaderType enumeration.");
		}
	}
	
	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setTarget(java.lang.String, boolean)
	 */
	@Override
	public abstract void setTarget(String target, boolean append) throws InvalidTargetException;
	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#start()
	 */
	@Override
	public abstract void start() throws IOException, LigretoException;
	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#writeOutput()
	 */
	@Override
	public abstract void writeOutput() throws IOException;

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#dumpHeader(java.sql.ResultSet, int[])
	 */
	@Override
	public void dumpHeader(ResultSet rs, int[] excl) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		nextRow();
		for (int i=1, c=0; i <= rsmd.getColumnCount(); i++) {
			if (!MiscUtils.arrayContains(excl, i)) {
				setHeaderColumn(columnStep*c++, rsmd.getColumnLabel(i), HeaderType.TOP);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#dumpJoinOnHeader(java.sql.ResultSet, int[])
	 */
	@Override
	public void dumpJoinOnHeader(ResultSet rs, int[] on) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i=0; i < on.length; i++) {
			setHeaderColumn(columnStep*i, rsmd.getColumnLabel(on[i]), HeaderType.TOP);
		}
	}

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#dumpOtherHeader(java.sql.ResultSet, int[], int[])
	 */
	@Override
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
				setHeaderColumn(columnStep*idx, rsmd.getColumnLabel(i+1), HeaderType.TOP);
				idx++;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setJoinOnColumns(java.sql.ResultSet, int[])
	 */
	@Override
	public void setJoinOnColumns(ResultSet rs, int[] on) throws SQLException {
		for (int i=0; i < on.length; i++) {
			setColumn(i, rs, on[i]);
		}
	}

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setOtherColumns(java.sql.ResultSet, int[], int[])
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setHighlight(boolean)
	 */
	@Override
	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setHlColor(short[])
	 */
	@Override
	public void setHlColor(short[] rgbHlColor) {
		this.rgbHlColor = rgbHlColor;
	}
}
