/**
 * 
 */
package net.ligreto.parser.nodes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * This class encapsulates the main <ligreto> node in the report configuration file.
 * 
 * @author Julius Stroffek
 *
 */
public class LigretoNode extends Node {
	protected HashMap<String, DataSourceNode> dataSourceMap = new HashMap<String, DataSourceNode>();
	protected HashMap<String, String> queryMap = new HashMap<String, String>();
	protected HashMap<String, String> paramMap = new HashMap<String, String>();
	protected List<ReportNode> reportNodes = new LinkedList<ReportNode>();
	protected List<PtpNode> ptpNodes = new LinkedList<PtpNode>();

	public LigretoNode() {
		super(null);
	}

	public void addDataSource(DataSourceNode dataSource) {
		dataSourceMap.put(dataSource.getName(), dataSource);
	}

	public void addQuery(String name, String query) {
		queryMap.put(name, query);
	}

	public void addParam(String name, String value) {
		paramMap.put(name, value);
	}
	
	public void addReport(ReportNode reportNode) {
		reportNodes.add(reportNode);
	}
	
	public void addPTP(PtpNode ptpNode) {
		ptpNodes.add(ptpNode);
	}
	
	public String substituteParams(String string) {
		String oResult;
		String result = new String(string);
		do {
			oResult = result;
			for (String name : paramMap.keySet()) {
				result = result.replaceAll(
					"\\u0024\\u007B" + name + "\\u007D",
					paramMap.get(name).replaceAll("\\$", "\\\\\\$")
				);
			}
		} while (!result.equals(oResult));
		return result;
	}
	
	public Iterable<ReportNode> reports() {
		return reportNodes;
	}
	
	public Iterable<PtpNode> ptps() {
		return ptpNodes;
	}
	
	public DataSourceNode getDataSourceNode(String name) {
		return dataSourceMap.get(name);
	}
	
	public String getQuery(String name) {
		return substituteParams(queryMap.get(name));
	}
}
