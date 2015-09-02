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

/**
 * The class allowing to execute the SQL statement in the separate execution thread. This allows
 * the parallel execution of multiple queries on various data sources. The instances of this class
 * should be used as follows:
 * <pre>
 * SqlExecutionThread t1 = SqlExecutionThread.executeQuery(...);
 * SqlExecutionThread t2 = SqlExecutionThread.executeQuery(...);
 * try {
 *     t1.join();
 *     t2.join();
 * } catch (InterruptedException e) {
 *     throw new LigretoException("Execution interrupted.", e);
 * }
 * t1.throwExceptions();
 * t2.throwExceptions();
 * </pre>
 * 
 * @author Julius Stroffek
 *
 */
public class SqlExecutionThread extends Thread {
	
	/** The logger instance for the class. */
	private static Log log = LogFactory.getLog(SqlExecutionThread.class);

	/** The connection into database. */
	protected Connection cnn = null;
	
	/** The created statement on the connection. */
	protected Statement stm = null;
	
	/** The created callable statement on the connection. */
	protected CallableStatement cstm = null;
	
	/** The data source name where the query should be executed on. */
	protected String dataSource = null;
	
	/** The query string to be executed. */
	protected String query = null;
	
	/** The type of the query to be executed. */
	protected SqlNode.QueryType queryType = null;
	
	/** The number of rows to fetch at once from the server in the result set object. */
	protected int fetchSize = 1000;
	
	/** The throwable object that was caught during the thread execution. */
	protected Throwable throwable = null;
	
	/** The result set to be returned after execution. */
	protected ResultSet resultSet = null;
	
	/**
	 * Creates the instance.
	 * 
	 * Instances should be created only using static method {@link #executeQuery}.
	 */
	protected SqlExecutionThread() {
		super();
	}
	
	/**
	 * Creates the instance with the specified query to be executed the separate execution thread. The created thread is not started.
	 * 
	 * @param dataSource the data source name where the query should be executed
	 * @param query the query string to be executed
	 * @param queryType the type of the query
	 * @return the created SqlExecutionThread object
	 */
	public static SqlExecutionThread getInstance(String dataSource, String query, SqlNode.QueryType queryType) {
		SqlExecutionThread instance = new SqlExecutionThread();
		instance.dataSource = dataSource;
		instance.query = query;
		instance.queryType = queryType;
		return instance;
	}

	/**
	 * Execute the query. This method is called by {@link #start()}.
	 */
	@Override
	public void run() {
		try {
			cnn = Database.getInstance().getConnection(dataSource);
			switch (queryType) {
			case QUERY:
			case STATEMENT:
				log.info("Executing the SQL query/statement on \"" + dataSource + "\" data source:");
				log.info(query);
				stm = cnn.createStatement();
				stm.setFetchSize(fetchSize);
				resultSet = stm.executeQuery(query);
				break;
			case CALL:
				log.info("Executing the SQL callable statement on \"" + dataSource + "\" data source:");
				log.info(query);
				cstm = cnn.prepareCall(query);
				cstm.setFetchSize(fetchSize);
				resultSet = cstm.executeQuery();
				break;
			default:
				throw new LigretoException("Unknown query type.");
			}
		} catch (Throwable t) {
			throwable = t;
		}
	}
	
	/**
	 * @return the fetchSize
	 */
	public int getFetchSize() {
		return fetchSize;
	}

	/**
	 * @param fetchSize the fetchSize to set
	 */
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	/**
	 * Provides the access to the throwable object that was thrown during the thread execution.
	 * 
	 * @return the throwable caught during the thread execution.
	 */
	public Throwable getThrowable() {
		return throwable;
	}
	
	/**
	 * Provides the access to the result set.
	 * 
	 * @return the result set returned by the query execution.
	 */
	public ResultSet getResultSet() {
		return resultSet;
	}
	
	/**
	 * Final cleanup. It will close result set, statement, connection.
	 * 
	 * @throws SQLException if database related problems occurred
	 * @throws LigretoException if the query type is unknown
	 */
	public void cleanup() throws SQLException, LigretoException {
		switch (queryType) {
		case QUERY:
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

	/**
	 * This method will throw the exception in the current thread that was caught
	 * during the query execution.
	 * 
	 * @throws LigretoException if the throwable object was caught during the query execution
	 */
	public void throwExceptions() throws LigretoException {
		if (throwable != null) {
			throw new LigretoException("Error while executing query on \"" + dataSource + "\".", throwable);
		}
	}
}
