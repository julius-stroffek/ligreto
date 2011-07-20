package net.ligreto.builders;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.ligreto.config.nodes.*;
import net.ligreto.exceptions.InvalidTargetExpection;
import net.ligreto.exceptions.UnimplementedMethodException;

public abstract class ReportBuilder {
	protected ReportNode.ReportType reportType;
	protected String template;
	protected String output;
	protected LigretoNode ligretoNode;
	
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

	public void setColumn(int i, ResultSet rs) throws SQLException {
		setColumn(i-1, rs.getObject(i));
	}

	public void setTemplate(String template) {
		this.template = ligretoNode.substituteParams(template);
	}
	
	public void setOutput(String output) {
		this.output = ligretoNode.substituteParams(output);
	}
	
	public abstract void nextRow();
	public abstract void setColumnPosition(int column);
	public abstract void setColumnPosition(int column, int step);
	public abstract void setColumn(int i, Object o);
	public abstract void setTarget(String target) throws InvalidTargetExpection;
	public abstract void start() throws IOException;
	public abstract void writeOutput() throws IOException;

	public void dumpHeader(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i=1; i <= rsmd.getColumnCount(); i++) {
			setColumn(i-1, rsmd.getColumnLabel(i));
		}
	}
}
