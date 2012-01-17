package net.ligreto.parser.nodes;

import java.util.ArrayList;
import java.util.List;

import net.ligreto.exceptions.InvalidValueException;
import net.ligreto.exceptions.ReportException;
import net.ligreto.util.MiscUtils;

/**
 * @author Julius Stroffek
 *
 */
public class ReportNode extends Node {
	public enum ReportType {EXCEL, EXCELSTREAM, TEX, XML};
	
	protected String name;
	protected String template;
	protected String output;
	protected ReportType reportType;
	protected String locale;
	protected List<SqlNode> sqlQueries = new ArrayList<SqlNode>();
	protected List<JoinNode> joins = new ArrayList<JoinNode>();
	protected List<String> options = new ArrayList<String>();
	protected boolean result;
	
	public ReportNode(LigretoNode ligretoNode, String reportName, String reportType) throws ReportException {
		super(ligretoNode);
		name = reportName;
		if ("excel".equalsIgnoreCase(reportType)) {
			this.reportType = ReportType.EXCEL;
		} else if ("excel.stream".equalsIgnoreCase(reportType)) {
			this.reportType = ReportType.EXCELSTREAM;
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

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}
	
	public Iterable<String> getOptions() {
		return options;
	}
	
	public void setOptions(String optionString) {
		options = new ArrayList<String>();
		String[] opts = optionString.split(",");
		for (String o : opts) {
			options.add(o.trim());
		}
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	public boolean getResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public void setResult(String result) throws InvalidValueException {
		if (result != null) {
			this.result = MiscUtils.parseBoolean(result);
		} else {
			this.result = false;
		}
	}
}
