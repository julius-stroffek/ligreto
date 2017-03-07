package net.ligreto;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.exceptions.DataSourceException;
import net.ligreto.exceptions.DataSourceInitException;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.parser.nodes.DataSourceNode;
import net.ligreto.parser.nodes.LigretoNode;
import net.ligreto.parser.nodes.SqlNode;

/**
 * The class providing the interface to database connections. There is only one instance of every database available for
 * the execution thread. There is a static map holding all the references
 * 
 * @author Julius Stroffek
 *
 */
public class Database {
	
	/**
	 * The class allowing the ad-hoc resolution of connections for the database.
	 * 
	 * @author Julius Stroffek
	 *
	 */
	public static interface ConnectionResolver {
		/**
		 * Returns the connection of a given name.
		 * 
		 * @param name the name of the connection to be returned
		 * @return
		 * @throws DataSourceException
		 * @throws ClassNotFoundException
		 * @throws SQLException
		 */
		public Connection getConnection(String name) throws DataSourceException, ClassNotFoundException, SQLException;
		
		/**
		 * Provides access to the description of the datasource.
		 * 
		 * @param name the data source name
		 * @return
		 * @throws DataSourceNotDefinedException
		 */
		public DataSourceNode getDataSourceNode(String name) throws DataSourceNotDefinedException;
	}
	
	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(Database.class);

	protected static Hashtable<Long, Database> instanceMap = new Hashtable<Long, Database>();
	
	public static Database getInstance() {
		synchronized (Database.class) {
			Database instance = instanceMap.get(Thread.currentThread().getId());
			if (instance == null) {
				instance = new Database();
				instanceMap.put(Thread.currentThread().getId(), instance);
			}
			
			return instance;
		}
	}

	public static Database getInstance(LigretoNode aLigretoNode) {
		Database instance = getInstance();
		instance.setLigretoNode(aLigretoNode);
		return instance;
	}

	/**
	 * Releases the current thread local instance.
	 */
	public static void dropInstance() {
		synchronized (Database.class) {
			instanceMap.remove(Thread.currentThread().getId());
		}
	}
	
	LigretoNode ligretoNode = null;
	
	public Database() {
	}
	
	@Override
	public void finalize() {
		synchronized (Database.class) {
			instanceMap.remove(Thread.currentThread().getId());
		}
	}

	public void setLigretoNode(LigretoNode aLigretoNode) {
		ligretoNode = aLigretoNode;
	}
	
	public DataSourceNode getDataSourceNode(String name) throws DataSourceNotDefinedException {
		DataSourceNode node = null;
		if (ligretoNode != null && ligretoNode.getConnectionResolver() != null) {
			try {
				node = ligretoNode.getConnectionResolver().getDataSourceNode(name);
				if (node != null) {
					return node;
				}
			} catch (DataSourceNotDefinedException e) {
				// Just ignore it
			}
		}
		node = ligretoNode.getDataSourceNode(name);
		if (node == null) {
			throw new DataSourceNotDefinedException("Data source \"" + name + "\" was not defined.");
		}
		return node;
	}
	
	public Connection getConnection(String name) throws DataSourceException, ClassNotFoundException, SQLException {
		DataSourceNode node = null;
		Connection cnn = null;
		String sourceDecription = null;
		if (ligretoNode != null && ligretoNode.getConnectionResolver() != null) {
			try {
				node = ligretoNode.getConnectionResolver().getDataSourceNode(name);
				cnn = ligretoNode.getConnectionResolver().getConnection(name);
				sourceDecription = node.getDescription() + " (" + node.getName() + ")";
			} catch (DataSourceNotDefinedException e) {
				// Just ignore it as we will try more options
			}
		}
		
		if (cnn == null || node == null) {
			node = ligretoNode.getDataSourceNode(name);
			if (node == null) {
				throw new DataSourceNotDefinedException("Data source \"" + name + "\" was not defined.");
			}
	
			sourceDecription = node.getDescription() + " (" + node.getName() + ")";
			String uri = ligretoNode.substituteParams(node.getUri());
			log.info("Connecting to \"" + sourceDecription + "\" data source with uri: " + uri);
			
			Class.forName(ligretoNode.substituteParams(node.getDriverClass()));
			
			try {
				Properties params = ligretoNode.substitueParams(node.getParameters());
				cnn = DriverManager.getConnection(uri, params);
			} catch (SQLException e) {
				throw new DataSourceException("Could not connect to data source: " + sourceDecription, e);
			}
		}
		
		// Initialize the connection with the given SQL queries
		try {
			Statement stm = null;
			CallableStatement cstm = null;
			for (SqlNode sqlNode : node.sqlQueries()) {
				try {
					switch (sqlNode.getQueryType()) {
					case STATEMENT:
						log.info("Executing the SQL statement on \"" + sourceDecription + "\" data source:");
						log.info(sqlNode.getQuery());
						stm = cnn.createStatement();
						stm.execute(sqlNode.getQuery());
						break;
					case QUERY:
						log.info("Executing the SQL query on \"" + sourceDecription + "\" data source:");
						log.info(sqlNode.getQuery());
						stm = cnn.createStatement();
						stm.executeQuery(sqlNode.getQuery());
						break;
					case CALL:
						log.info("Executing the SQL callable statement on \"" + sourceDecription + "\" data source:");
						log.info(sqlNode.getQuery());
						cstm = cnn.prepareCall(sqlNode.getQuery());
						cstm.execute();
						break;
					default:
						throw new DataSourceInitException("Unknown query type.");
					}
				} catch (SQLException e) {
					switch (sqlNode.getExceptions()) {
					case IGNORE:
						break;
					case DUMP:
						log.error("Exception while executing query", e);
						break;
					case FAIL:
						throw e;
					}
				} finally {
					if (stm != null)
						stm.close();
					if (cstm != null)
						cstm.close();
				}
			}
		} catch (Exception e) {
			throw new DataSourceInitException("Failed to initialize the connection by custom SQL statements.", e);
		}
		
		log.info("Connected.");
		return cnn;
	}

	public static void close(Connection cnn, Statement stm, ResultSet rs) throws SQLException {
		if (rs != null)
			rs.close();
		if (stm != null)
			stm.close();
		if (cnn != null)
			cnn.close();
	}
	
	public static void close(Connection cnn, Statement stm, CallableStatement cstm, ResultSet rs) throws SQLException {
		if (rs != null)
			rs.close();
		if (stm != null)
			stm.close();
		if (cstm != null)
			cstm.close();
		if (cnn != null)
			cnn.close();
	}
}
