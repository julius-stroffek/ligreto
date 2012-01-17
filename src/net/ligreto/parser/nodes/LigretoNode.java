/**
 * 
 */
package net.ligreto.parser.nodes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.ligreto.LigretoParameters;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.util.Parameters;

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
	protected HashMap<String, Void> lockedParams = new HashMap<String, Void>();
	protected List<ReportNode> reportNodes = new LinkedList<ReportNode>();
	protected List<PtpNode> ptpNodes = new LinkedList<PtpNode>();
	protected LigretoParameters ligretoParameters = new LigretoParameters();

	public LigretoNode() {
		super(null);
	}

	public void addDataSource(DataSourceNode dataSource) {
		dataSourceMap.put(dataSource.getName(), dataSource);
	}

	public void addQuery(String name, String query) {
		queryMap.put(name, query);
	}

	public void addParam(String name, String value) throws LigretoException {
		if (!lockedParams.containsKey(name)) {
			if (name.startsWith("ligreto.")) {
				ligretoParameters.setParameter(name, value);
				paramMap.put(name, value);
			} else {
				paramMap.put(name, value);
			}
		}
	}
	
	public void addLockedParam(String name, String value) throws LigretoException {
		addParam(name, value);
		lockedParams.put(name, null);
	}
	
	public String getParam(String name) throws LigretoException {
		return getParam(name, null);
	}
	
	public String getParam(String name, String defaultValue) throws LigretoException {
		if (name.startsWith("ligreto.")) {
			return ligretoParameters.getParameter(name);
		} else {
			String value = paramMap.get(name);
			if (value != null) {
				return value;
			}
			return defaultValue;
		}
	}
		
	public void addReport(ReportNode reportNode) {
		reportNodes.add(reportNode);
	}
	
	public void addPTP(PtpNode ptpNode) {
		ptpNodes.add(ptpNode);
	}
	
	public String substituteParams(String string) {
		return Parameters.substituteParams(paramMap, string);
	}

	public Properties substitueParams(Properties properties) {
		return Parameters.substituteParams(paramMap, properties);
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
	
	public String getQuery(String name) throws LigretoException {
		String query = queryMap.get(name);
		if (query == null) {
			throw new LigretoException("The referenced query was not defined: " + name);
		}
		return substituteParams(query);
	}

	public LigretoParameters getLigretoParameters() {
		return ligretoParameters;
	}
}
