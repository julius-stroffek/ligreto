package net.ligreto.data;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.Database;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.parser.nodes.SqlNode;

public class SqlExecutionThread extends Thread {
	
	/** The logger instance for the class. */
	private static Log log = LogFactory.getLog(SqlExecutionThread.class);

	protected Connection cnn = null;
	protected Statement stm = null;
	protected CallableStatement cstm = null;
	protected String dataSource = null;
	protected String query = null;
	protected SqlNode.QueryType queryType = null;
	protected Throwable throwable = null;
	protected ResultSet resultSet = null;
	
	protected SqlExecutionThread() {
		super();
	}
	
	public static SqlExecutionThread executeQuery(String dataSource, String query, SqlNode.QueryType queryType) {
		SqlExecutionThread instance = new SqlExecutionThread();
		instance.dataSource = dataSource;
		instance.query = query;
		instance.queryType = queryType;
		instance.start();
		return instance;
	}

	@Override
	public void run() {
		try {
			cnn = Database.getInstance().getConnection(dataSource);
			switch (queryType) {
			case STATEMENT:
				log.info("Executing the SQL statement on \"" + dataSource + "\" data source:");
				log.info(query);
				stm = cnn.createStatement();
				resultSet = stm.executeQuery(query);
				break;
			case CALL:
				log.info("Executing the SQL callable statement on \"" + dataSource + "\" data source:");
				log.info(query);
				cstm = cnn.prepareCall(query);
				resultSet = cstm.executeQuery();
				break;
			default:
				throw new LigretoException("Unknown query type.");
			}
		} catch (Throwable t) {
			throwable = t;
		}
	}
	
	public Throwable getThrowable() {
		return throwable;
	}
	
	public ResultSet getResultSet() {
		return resultSet;
	}
	
	public void cleanup() throws SQLException, LigretoException {
		switch (queryType) {
		case STATEMENT:
			Database.close(cnn, stm, resultSet);
			break;
		case CALL:
			Database.close(cnn, cstm, resultSet);
			break;
		default:
			throw new LigretoException("Unknown query type.");
		}
	}

	public void throwExceptions() throws LigretoException {
		if (throwable != null) {
			throw new LigretoException("Error while executing query on \"" + dataSource + "\".", throwable);
		}
	}
}
