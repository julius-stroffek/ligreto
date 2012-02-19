package net.ligreto.builders;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.CellFormat;
import net.ligreto.builders.BuilderInterface.HeaderType;
import net.ligreto.data.DataProvider;
import net.ligreto.exceptions.DataException;
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
public abstract class ReportTarget implements TargetInterface {
	
	/** 
	 * The report builder that created this target
	 */
	protected ReportBuilder reportBuilder;
	
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
	
	/** The global ligreto parameters. */
	protected LigretoParameters ligretoParameters;
	
	/** Nobody except child classes could create the instance. */
	protected ReportTarget(ReportBuilder reportBuilder) {
		this.reportBuilder = reportBuilder;
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
	 * This is the function used to set the actual row for the dump.
	 */
	void setActRow(int actRow) {
		this.actRow = actRow;
	}

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setColumn(int, java.sql.ResultSet, int)
	 */
	@Override
	public void dumpColumn(int i, DataProvider dp, int dpi) throws DataException {
		Object o = dp.getObject(dpi);
		if (dp.wasNull() || o == null) {
			o = ligretoParameters.getNullString();
		}
		dumpColumn(columnStep*i, o, getHlColor(i), CellFormat.UNCHANGED);
	}

	@Override
	public void dumpColumn(int i, DataProvider dp) throws DataException {
		dumpColumn(i-1, dp, i);
	}
	
	@Override
	public void nextRow() throws IOException {
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
	public void dumpColumn(int i, Object o, CellFormat cellFormat) {
		dumpColumn(i, o, getHlColor(i), cellFormat);
	}
	
	@Override
	public void dumpColumn(int i, Object o, CellFormat cellFormat, boolean highlight) {
		dumpColumn(i, o, highlight && this.highlight ? rgbHlColor : null, cellFormat);
	}

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setColumn(int, java.lang.Object, short[])
	 */
	@Override
	public abstract void dumpColumn(int i, Object o, short[] rgb, CellFormat cellFormat);
	
	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setHeaderColumn(int, java.lang.Object, net.ligreto.builders.ReportBuilder.HeaderType)
	 */
	@Override
	public void dumpHeaderColumn(int i, Object o, HeaderType headerType) {
		switch (headerType) {
		case TOP:
			dumpColumn(i, o, getHlColor(i), CellFormat.UNCHANGED);
			break;
		case ROW:
			dumpColumn(i, o, getHlColor(i), CellFormat.UNCHANGED);
			break;
		default:
			throw new RuntimeException("Unexpected value of HeaderType enumeration.");
		}
	}
	
	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#dumpHeader(java.sql.ResultSet, int[])
	 */
	@Override
	public void dumpHeader(DataProvider dp, int[] excl) throws DataException, IOException {
		nextRow();
		for (int i=1, c=0; i <= dp.getColumnCount(); i++) {
			if (!MiscUtils.arrayContains(excl, i)) {
				dumpHeaderColumn(columnStep*c++, dp.getColumnLabel(i), HeaderType.TOP);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#dumpJoinOnHeader(java.sql.ResultSet, int[])
	 */
	@Override
	public void dumpJoinOnHeader(DataProvider dp, int[] on, String dataSourceDesc) throws DataException {
		for (int i=0; i < on.length; i++) {
			dumpHeaderColumn(
				columnStep*i,
				dataSourceDesc != null
					? dp.getColumnLabel(on[i]) + " (" + dataSourceDesc + ")"
					: dp.getColumnLabel(on[i]),
				HeaderType.TOP
			);
		}
	}

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#dumpOtherHeader(java.sql.ResultSet, int[], int[])
	 */
	@Override
	public void dumpOtherHeader(DataProvider dp, int[] on, int[] excl, String dataSourceDesc) throws DataException {
		int rsLength = dp.getColumnCount();
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
				dumpHeaderColumn(
					columnStep*idx,
					dataSourceDesc != null
						? dp.getColumnLabel(i+1) + " (" + dataSourceDesc + ")"
						: dp.getColumnLabel(i+1),
					HeaderType.TOP
				);
				idx++;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setJoinOnColumns(java.sql.ResultSet, int[])
	 */
	@Override
	public void dumpJoinOnColumns(DataProvider dp, int[] on) throws DataException {
		for (int i=0; i < on.length; i++) {
			dumpColumn(i, dp, on[i]);
		}
	}

	/* (non-Javadoc)
	 * @see net.ligreto.builders.BuilderInterface#setOtherColumns(java.sql.ResultSet, int[], int[])
	 */
	@Override
	public void dumpOtherColumns(DataProvider dp, int[] on, int[] excl) throws DataException {
		int rsLength = dp.getColumnCount();
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
				dumpColumn(idx, dp, i+1);
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

	@Override
	public void setLigretoParameters(LigretoParameters ligretoParameters) {
		this.ligretoParameters = ligretoParameters;
	}
}
