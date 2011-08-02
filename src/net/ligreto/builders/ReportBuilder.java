package net.ligreto.builders;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.ligreto.exceptions.InvalidTargetException;
import net.ligreto.exceptions.UnimplementedMethodException;
import net.ligreto.parser.nodes.*;

public abstract class ReportBuilder {
	public static final String NULL="";
	protected ReportNode.ReportType reportType;
	protected String template;
	protected String output;
	protected LigretoNode ligretoNode;
	protected int columnStep = 1;
	protected int baseRow = 0;
	protected int baseCol = 0;
	protected int actRow = baseRow-1;
	protected int actCol = baseCol;
	protected int[] cmpArray = null;
	protected boolean highlight;
	protected String hlColor;
	
	protected ReportBuilder() {
	}
	
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
	
	protected String getHlColor(int i) {
		int cmp = cmpArray != null ? cmpArray[i] : 0;
		if (cmp != 0 && highlight) {
			return hlColor;
		}
		return null;
	}

	public void setColumn(int i, ResultSet rs, int rsi) throws SQLException {
		Object o = rs.getObject(rsi);
		if (rs.wasNull()) {
			setColumn(columnStep*i, NULL, getHlColor(i));
		} else {
			setColumn(columnStep*i, o, getHlColor(i));
		}
	}

	public void setColumn(int i, ResultSet rs) throws SQLException {
		setColumn(i-1, rs, i);
	}

	public void setTemplate(String template) {
		this.template = ligretoNode.substituteParams(template);
	}
	
	public void setOutput(String output) {
		this.output = ligretoNode.substituteParams(output);
	}
	
	public void nextRow() {
		actRow++;
		actCol = baseCol;
		columnStep = 1;
		cmpArray = null;
	}
	
	public void setColumnPosition(int column) {
		actCol = baseCol + column;
	}

	public void setCmpArray(int[] cmpArray) {
		this.cmpArray = cmpArray;
	}
	
	public void setColumnPosition(int column, int step, int[] cmpArray) {
		actCol = baseCol + column;
		columnStep = step;
		this.cmpArray = cmpArray;
	}

	public void setColumn(int i, Object o) {
		setColumn(i, o, getHlColor(i));
	}

	// TODO Add support for other data types
	public abstract void setColumn(int i, Object o, String color);
	public abstract void setTarget(String target, boolean append) throws InvalidTargetException;
	public abstract void start() throws IOException;
	public abstract void writeOutput() throws IOException;

	public void dumpHeader(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		nextRow();
		for (int i=1; i <= rsmd.getColumnCount(); i++) {
			setColumn(columnStep*(i-1), rsmd.getColumnLabel(i));
		}
	}

	public void dumpJoinOnHeader(ResultSet rs, int[] on) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i=0; i < on.length; i++) {
			setColumn(columnStep*i, rsmd.getColumnLabel(on[i]));
		}
	}

	public void dumpOtherHeader(ResultSet rs, int[] on) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int rsLength = rsmd.getColumnCount();
		int idx = 0;
		for (int i=0; i < rsLength; i++) {
			boolean onPresent = false;
			for (int j=0; j < on.length; j++) {
				if (i+1 == on[j]) {
					onPresent = true;
					break;
				}
			}
			if (!onPresent) {
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

	public void setOtherColumns(ResultSet rs, int[] on) throws SQLException {
		int rsLength = rs.getMetaData().getColumnCount();
		int idx = 0;
		for (int i=0; i < rsLength; i++) {
			boolean onPresent = false;
			for (int j=0; j < on.length; j++) {
				if (i+1 == on[j]) {
					onPresent = true;
					break;
				}
			}
			if (!onPresent) {
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
	public void setHlColor(String hlColor) {
		this.hlColor = hlColor;
	}
}
