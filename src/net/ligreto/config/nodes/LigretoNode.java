/**
 * 
 */
package net.ligreto.config.nodes;

import java.util.HashMap;

/**
 * This class encapsulates the main <ligreto> node in the report configuration file.
 * 
 * @author Julius Stroffek
 *
 */
public class LigretoNode {
	protected HashMap<String, DataSourceNode> dataSourceMap = new HashMap<String, DataSourceNode>();
	protected HashMap<String, String> queryMap = new HashMap<String, String>();
	protected HashMap<String, String> paramMap = new HashMap<String, String>();
	protected HashMap<Integer, ReportNode> reportMap = new HashMap<Integer, ReportNode>();

	public LigretoNode() {
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
		reportMap.put(reportMap.size(), reportNode);
	}
	
	public String paramsSubstitue(String string) {
		String result = new String(string);
		for (String name : paramMap.keySet()) {
			result = result.replaceFirst("%%"+name, paramMap.get(name));
		}
		return result;
	}
}
