package net.ligreto.config.nodes;

import java.util.ArrayList;
import java.util.List;

import net.ligreto.exceptions.ReportException;

/**
 * @author Julius Stroffek
 *
 */
public class ReportNode extends Node {
	public enum ReportType {EXCEL, TEX, XML};
	
	protected String name;
	protected String template;
	protected String output;
	protected ReportType reportType;
	protected List<SqlNode> sqlQueries = new ArrayList<SqlNode>();
	protected List<JoinNode> joins = new ArrayList<JoinNode>();
	
	public ReportNode(LigretoNode ligretoNode, String reportName, String reportType) throws ReportException {
		super(ligretoNode);
		name = reportName;
		if ("excel".equalsIgnoreCase(reportType)) {
			this.reportType = ReportType.EXCEL;
		} else if ("tex".equalsIgnoreCase(reportType)) {
				this.reportType = ReportType.TEX;
		} else if ("xml".equalsIgnoreCase(reportType)) {
			this.reportType = ReportType.XML;
		} else {
			throw new ReportException("Unknown report type: \"" + reportType + "\".");
		}
	}
	
	public void addSql(SqlNode sql) {
		sqlQueries.add(sql);
	}
	
	public void addJoin(JoinNode joinNode) {
		joins.add(joinNode);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the template
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}

	/**
	 * @return the reportType
	 */
	public ReportType getReportType() {
		return reportType;
	}

	/**
	 * @param reportType the reportType to set
	 */
	public void setReportType(ReportType reportType) {
		this.reportType = reportType;
	}

	public Iterable<SqlNode> sqlQueries() {
		return sqlQueries;
	}

	public Iterable<JoinNode> joins() {
		return joins;
	}

	/**
	 * @return the output
	 */
	public String getOutput() {
		return output;
	}

	/**
	 * @param output the output to set
	 */
	public void setOutput(String output) {
		this.output = output;
	}
}
