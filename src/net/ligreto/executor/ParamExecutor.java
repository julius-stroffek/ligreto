package net.ligreto.executor;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.Database;
import net.ligreto.ResultStatus;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.parser.nodes.ParamNode;

/**
 * @author Julius Stroffek
 *
 */
public class ParamExecutor extends Executor {

	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(ParamExecutor.class);

	/** Iterable object holding the SQL nodes to be processed. */ 
	protected Iterable<ParamNode> paramNodes;
	
	public ResultStatus execute(ParamNode paramNode) throws LigretoException {
		ResultStatus result = new ResultStatus();
		try {
			Connection cnn = null;
			Statement stm = null;
			CallableStatement cstm = null;
			ResultSet rs = null;
			try {
				cnn = Database.getInstance().getConnection(paramNode.getDataSource());
				String qry = paramNode.getQuery().toString();
				try {
					switch (paramNode.getQueryType()) {
					case STATEMENT:
						log.info("Executing the SQL statement on \"" + paramNode.getDataSource() + "\" data source:");
						log.info(qry);
						stm = cnn.createStatement();
						rs = stm.executeQuery(qry);
						break;
					case CALL:
						log.info("Executing the SQL callable statement on \"" + paramNode.getDataSource() + "\" data source:");
						log.info(qry);
						cstm = cnn.prepareCall(qry);
						rs = cstm.executeQuery();
						break;
					default:
						throw new LigretoException("Unknown query type.");
					}
					if (rs != null) {
						// Here we will store the result of the query into parameters
						if (rs.next()) {
							if (rs.getMetaData().getColumnCount() > 1) {
								throw new LigretoException("To many columns in the result for parameter value");
							}
							String value = rs.getString(1);
							paramNode.getLigretoNode().addParam(paramNode.getParamName(), value);
							if (rs.next()) {
								throw new LigretoException("Too many rows returned as parameter value.");								
							}
						} else {
							throw new LigretoException("No rows returned as parameter value.");
						}
					}
				} catch (SQLException e) {
					switch (paramNode.getExceptions()) {
					case IGNORE:
						break;
					case DUMP:
						log.error("Exception while executing query", e);
						break;
					case FAIL:
						throw e;
					}
				}
			} finally {
				Database.close(cnn, stm, cstm, rs);
			}
			result.info(log, "PARAM SQL");
		} catch (SQLException e) {
			String msg = "Database error on data source: " + paramNode.getDataSource();
			throw new LigretoException(msg, e);
		} catch (ClassNotFoundException e) {
			String msg = "Database driver not found for data source: " + paramNode.getDataSource();
			throw new LigretoException(msg, e);
		} catch (Exception e) {
			String msg = "Error processing SQL query in parameter: " + paramNode.getQuery();
			throw new LigretoException(msg, e);
		}
		return result;
	}
	
	@Override
	public ResultStatus execute() throws LigretoException {
		// Do nothing if there is nothing to process.
		if (paramNodes == null || !paramNodes.iterator().hasNext()) {
			return null;
		}
		
		log.info("Starting execution of parameter values.");
		for (ParamNode paramNode : paramNodes) {
			execute(paramNode);
		}
		log.info("Finished execution of parameter values.");
		return null;
	}

	/**
	 * @return the paramNodes
	 */
	public Iterable<ParamNode> getParamNodes() {
		return paramNodes;
	}

	/**
	 * @param paramNodes the paramNodes to set
	 */
	public void setParamNodes(Iterable<ParamNode> paramNodes) {
		this.paramNodes = paramNodes;
	}
}
