package net.ligreto.config.data;

import java.util.HashMap;

/**
 * This class holds the configuration related to data source.
 * 
 * @author Julius Stroffek
 *
 */
public class DataSourceConfig {
	
	private String name;
	private String driverClass;
	private String uri;
	private HashMap<String,String> parameters;
	
	/**
	 * Creates the instance of DataSourceConfig class.
	 */
	public DataSourceConfig(String name) {
		this.name = name;
		parameters = new HashMap<String,String>();
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
		return parameters.get(name);
	}
	
	/**
	 * This function will assign the specified value to parameters used to get the connection.
	 * 
	 * @param name The parameter name
	 * @param value The parameter value
	 */
	public void setParameter(String name, String value) {
		parameters.put(name, value);
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
}
