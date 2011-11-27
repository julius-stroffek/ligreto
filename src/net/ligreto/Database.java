package net.ligreto;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.exceptions.DataSourceException;
import net.ligreto.exceptions.DataSourceInitException;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.parser.nodes.DataSourceNode;
import net.ligreto.parser.nodes.LigretoNode;
import net.ligreto.parser.nodes.SqlNode;

public class Database {
	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(Database.class);

	protected static Database instance;
	
	public static Database getInstance() {
		if (instance == null) {
			instance = new Database();
		}
		return instance;
	}

	public static Database getInstance(LigretoNode aLigretoNode) {
		if (instance == null) {
			instance = new Database();
		}
		instance.setLigretoNode(aLigretoNode);
		return instance;
	}

	LigretoNode ligretoNode = null;
	
	public Database() {
	}

	public void setLigretoNode(LigretoNode aLigretoNode) {
		ligretoNode = aLigretoNode;
	}
	
	public Connection getConnection(String name) throws DataSourceException, ClassNotFoundException, SQLException {
		DataSourceNode node = ligretoNode.getDataSourceNode(name);
		if (node == null) {
			throw new DataSourceNotDefinedException("Data source \"" + name + "\" was not defined.");
		}
		Class.forName(node.getDriverClass());
		
		// Create the connection
		Connection cnn = DriverManager.getConnection(ligretoNode.substituteParams(node.getUri()), node.getParameters());
		
		// Initialize the connection with the given SQL queries
		try {
			Statement stm = null;
			CallableStatement cstm = null;
			for (SqlNode sqlNode : node.sqlQueries()) {
				try {
					switch (sqlNode.getQueryType()) {
					case STATEMENT:
						log.info("Executing the SQL statement on \"" + name + "\" data source:");
						log.info(sqlNode.getQuery());
						stm = cnn.createStatement();
						stm.executeQuery(sqlNode.getQuery());
						break;
					case CALL:
						log.info("Executing the SQL callable statement on \"" + name + "\" data source:");
						log.info(sqlNode.getQuery());
						cstm = cnn.prepareCall(sqlNode.getQuery());
						cstm.executeQuery();
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
		} catch (SQLException e) {
			throw new DataSourceInitException("Failed to initialize the connection by custom SQL statements.", e);
		}
		
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
