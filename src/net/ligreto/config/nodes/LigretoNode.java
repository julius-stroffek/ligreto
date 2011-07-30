/**
 * 
 */
package net.ligreto.config.nodes;

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
	protected List<PTPNode> ptpNodes = new LinkedList<PTPNode>();

	public LigretoNode() {
		super(null);
	}

	public void addDataSource(DataSourceNode dataSource) {
		dataSourceMap.put(dataSource.getName(), dataSource);
	}

	public void addQuery(String name, String query) {
		queryMap.put(name, query);
	}

	public void addParam(String name, String query) {
		paramMap.put(name, query);
	}
	
	public void addReport(ReportNode reportNode) {
		reportNodes.add(reportNode);
	}
	
	public void addPTP(PTPNode ptpNode) {
		ptpNodes.add(ptpNode);
	}
	
	public String substituteParams(String string) {
		String result = new String(string);
		for (String name : paramMap.keySet()) {
			result = result.replaceFirst("\\u0024\\u007B"+name+"\\u007D", paramMap.get(name));
		}
		return result;
	}
	
	public Iterable<ReportNode> reports() {
		return reportNodes;
	}
	
	public Iterable<PTPNode> ptps() {
		return ptpNodes;
	}
	
	public DataSourceNode getDataSourceNode(String name) {
		return dataSourceMap.get(name);
	}
	
	public String getQuery(String name) {
		return substituteParams(queryMap.get(name));
	}

}
