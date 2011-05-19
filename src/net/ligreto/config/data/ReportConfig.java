package net.ligreto.config.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Julius Stroffek
 *
 */
public class ReportConfig {
	enum ReportType {EXCEL, TEX, XML};
	
	protected String name;
	protected String template;
	protected ReportType reportType;
	protected List<SqlNode> sqlQueries = new ArrayList<SqlNode>();
	protected List<JoinNode> joins = new ArrayList<JoinNode>();
	
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

}
