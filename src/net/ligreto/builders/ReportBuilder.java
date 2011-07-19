package net.ligreto.builders;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.ligreto.config.nodes.*;
import net.ligreto.exceptions.UnimplementedMethodException;

public abstract class ReportBuilder {
	ReportNode.ReportType reportType;
	protected String template;
	protected String output;
	
	protected ReportBuilder() {
	}
	
	public static ReportBuilder createInstance(ReportNode.ReportType reportType) {
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
		return builder;
	}

	public void setColumn(int i, ResultSet rs) throws SQLException {
		setColumn(i, rs.getObject(i));
	}

	public abstract void setTemplate(String template);
	public abstract void setTarget(String target);
	public abstract void nextRow();
	public abstract void setColumnPosition(int column);
	public abstract void setColumnPosition(int column, int step);
	public abstract void setColumn(int i, Object o);
	public abstract void writeOutput();

	public void setOutput(String theOutput) {
		output = theOutput;
	}
}
