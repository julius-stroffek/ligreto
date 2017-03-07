/**
 * 
 */
package net.ligreto.parser.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.Database.ConnectionResolver;
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

	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(LigretoNode.class);

	protected Map<String, DataSourceNode> dataSourceMap = new HashMap<String, DataSourceNode>();
	protected Map<String, String> queryMap = new HashMap<String, String>(128);
	protected Map<String, String> paramMap = new HashMap<String, String>(256);
	protected Map<String, Void> lockedParams = new HashMap<String, Void>(256);
	protected List<ParamNode> paramNodes = new LinkedList<ParamNode>();
	protected List<SqlNode> sqlQueries = new ArrayList<SqlNode>();
	protected List<ReportNode> reportNodes = new LinkedList<ReportNode>();
	protected List<PtpNode> ptpNodes = new LinkedList<PtpNode>();
	protected LigretoParameters ligretoParameters = new LigretoParameters();
	protected ConnectionResolver connectionResolver = null;

	public LigretoNode() {
		super(null);
	}

	public ConnectionResolver getConnectionResolver() {
		return connectionResolver;
	}

	public void setConnectionResolver(ConnectionResolver connectionResolver) {
		this.connectionResolver = connectionResolver;
	}

	public void addDataSource(DataSourceNode dataSource) {
		dataSourceMap.put(dataSource.getName(), dataSource);
	}

	public void addQuery(String name, String query) {
		queryMap.put(name, query);
	}

	public void addParam(ParamNode paramNode) {
		paramNodes.add(paramNode);
	}
	
	public void addParam(String name, String value) throws LigretoException {
		if (!lockedParams.containsKey(name)) {
			if (name.startsWith("ligreto.")) {
				log.info("Setting up ligreto parameter: \"" + name + "\"");
				log.debug("Parameter value: \"" + value + "\"");
				ligretoParameters.setParameter(name, value);
				paramMap.put(name, value);
			} else {
				log.info("Setting up parameter: \"" + name + "\"");
				log.debug("Parameter value: \"" + value + "\"");
				paramMap.put(name, value);
			}
		} else {
			log.info("Skipping ligreto parameter assignment: \"" + name + "\"");
			log.debug("Requested parameter value: \"" + value + "\"");
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
	
	public Iterable<ParamNode> params() {
		return paramNodes;
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

	public void addSql(SqlNode sql) {
		sqlQueries.add(sql);
	}
	
	public List<SqlNode> sqlQueries() {
		return sqlQueries;
	}
}
