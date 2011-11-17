package net.ligreto.parser.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class holds the configuration related to data source.
 * 
 * @author Julius Stroffek
 *
 */
public class DataSourceNode extends Node {
	
	protected String name;
	protected String driverClass;
	protected String uri;
	protected Properties parameters;
	protected List<SqlNode> sqlQueries = new ArrayList<SqlNode>();
	
	/**
	 * Creates the instance of DataSourceNode class.
	 */
	public DataSourceNode(LigretoNode ligretoNode, String name) {
		super(ligretoNode);
		this.name = name;
		parameters = new Properties();
	}

	/**
	 * @return the driverClass
	 */
	public String getDriverClass() {
		return driverClass;
	}

	/**
	 * @param driverClass the driverClass to set
	 */
	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	/**
	 * @param name The parameter name
	 * @return The value of the specified parameter
	 */
	public String getParameter(String name) {
		return parameters.getProperty(name);
	}
	
	/**
	 * @return The {@link Properties} object of all the parameters
	 */
	public Properties getParameters() {
		return parameters;
	}
	
	/**
	 * This function will assign the specified value to parameters used to get the connection.
	 * 
	 * @param name The parameter name
	 * @param value The parameter value
	 */
	public void setParameter(String name, String value) {
		parameters.setProperty(name, value);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return The string showing the basic attributes of the connection.
	 */	 
	public String toString() {
		return "Data Source: name=\"" + name + "\"; driver=\"" + driverClass + "\"; uri=\"" + uri + "\"";
	}
	
	public void addSql(SqlNode sql) {
		sqlQueries.add(sql);
	}
	
	public Iterable<SqlNode> sqlQueries() {
		return sqlQueries;
	}
}
