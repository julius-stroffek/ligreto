package net.ligreto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.ligreto.exceptions.DataSourceException;
import net.ligreto.exceptions.DataSourceInitException;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.parser.nodes.DataSourceNode;
import net.ligreto.parser.nodes.LigretoNode;
import net.ligreto.parser.nodes.SqlNode;

public class Database {
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
			Statement stm = cnn.createStatement();
			for (SqlNode sqlNode : node.sqlQueries()) {
				stm.execute(sqlNode.getQuery());
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
}
