package net.ligreto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import net.ligreto.config.nodes.DataSourceNode;
import net.ligreto.config.nodes.LigretoNode;
import net.ligreto.exceptions.DataSourceNotDefinedException;

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
	
	public Connection getConnection(String name) throws DataSourceNotDefinedException, ClassNotFoundException, SQLException {
		DataSourceNode node = ligretoNode.getDataSourceNode(name);
		if (node == null) {
			throw new DataSourceNotDefinedException("Data source \"" + name + "\" was not defined.");
		}
		Class.forName(node.getDriverClass());
		return DriverManager.getConnection(node.getUri(), node.getParameters());
	}
}
