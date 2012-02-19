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
import net.ligreto.builders.BuilderInterface;
import net.ligreto.builders.TargetInterface;
import net.ligreto.data.DataProvider;
import net.ligreto.data.ResultSetDataProvider;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.parser.nodes.SqlNode;
import net.ligreto.util.MiscUtils;

/**
 * @author Julius Stroffek
 *
 */
public class SqlExecutor extends Executor implements SqlResultCallBack {

	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(SqlExecutor.class);

	/** Iterable object holding the SQL nodes to be processed. */ 
	protected Iterable<SqlNode> sqlNodes;
	
	/** The callback object which handles the processing of each row returned. */
	protected SqlResultCallBack callBack;
	
	/** The <code>ReportBuilder</code> object used to process the results. */
	protected BuilderInterface reportBuilder;
	
	/** The target builder. */
	protected TargetInterface targetBuilder;
	
	/** The list of column indices to be excluded. */
	protected int[] excl = new int[0];
	
	@Override
	public boolean prepareProcessing(SqlNode sqlNode, DataProvider dp) throws Exception {
		// Go to the next SQL query if we do not have the target defined
		if (sqlNode.getTarget() == null)
			return false;
		
		String[] exclStr = sqlNode.getExcludeColumns();
		if (exclStr != null && exclStr.length > 0) {
			excl = new int[exclStr.length];
			for (int i=0; i < exclStr.length; i++) {
				excl[i] = MiscUtils.findColumnIndex(dp, exclStr[i]);
				log.info("Excluding column \"" + exclStr[i] + "\" from sql query which has the index: " + excl[i]);
			}
		}
		
		targetBuilder = reportBuilder.getTargetBuilder(sqlNode.getTarget(), sqlNode.isAppend());
		if (sqlNode.getHeader()) {
			targetBuilder.dumpHeader(dp, excl);
		}
		return true;
	}
	
	@Override
	public void processResultSetRow(DataProvider dp) throws Exception {
		targetBuilder.nextRow();
		targetBuilder.setColumnPosition(0);
		for (int i=1, c=0; i <= dp.getColumnCount(); i++) {
			if (!MiscUtils.arrayContains(excl, i)) {
				targetBuilder.dumpColumn(c++, dp, i);
			}
		}
	}

	@Override
	public ResultStatus execute() throws LigretoException {
		ResultStatus result = new ResultStatus();
		
		// Do nothing if there is nothing to process.
		if (sqlNodes == null)
			return result;
		
		for (SqlNode sqlNode : sqlNodes) {
			try {
				Connection cnn = null;
				Statement stm = null;
				CallableStatement cstm = null;
				ResultSet rs = null;
				try {
					cnn = Database.getInstance().getConnection(sqlNode.getDataSource());
					String qry = sqlNode.getQuery().toString();
					try {
						switch (sqlNode.getQueryType()) {
						case STATEMENT:
							log.info("Executing the SQL statement on \"" + sqlNode.getDataSource() + "\" data source:");
							log.info(qry);
							stm = cnn.createStatement();
							rs = stm.executeQuery(qry);
							break;
						case CALL:
							log.info("Executing the SQL callable statement on \"" + sqlNode.getDataSource() + "\" data source:");
							log.info(qry);
							cstm = cnn.prepareCall(qry);
							rs = cstm.executeQuery();
							break;
						default:
							throw new LigretoException("Unknown query type.");
						}
						if (callBack != null && rs != null) {
							DataProvider dp = new ResultSetDataProvider(rs, excl);
							if (callBack.prepareProcessing(sqlNode, dp)) {
								while (rs.next()) {
									result.addRow();
									callBack.processResultSetRow(dp);
								}
								callBack.finalizeProcessing();
							}
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
					}
				} finally {
					Database.close(cnn, stm, cstm, rs);
				}
				result.info(log, "SQL");
			} catch (SQLException e) {
				String msg = "Database error on data source: " + sqlNode.getQuery().toString();
				log.error(msg);
				throw new LigretoException(msg, e);
			} catch (ClassNotFoundException e) {
				String msg = "Database driver not found for data source: " + sqlNode.getDataSource();
				log.error(msg);
				throw new LigretoException(msg, e);
			} catch (Exception e) {
				String msg = "Error processing while processing SQL query: " + sqlNode.getQuery();
				log.error(msg);
				throw new LigretoException(msg, e);
			}
		}
		return result;
	}

	/**
	 * @return the sqlNodes
	 */
	public Iterable<SqlNode> getSqlNodes() {
		return sqlNodes;
	}

	/**
	 * @param sqlNodes the sqlNodes to set
	 */
	public void setSqlNodes(Iterable<SqlNode> sqlNodes) {
		this.sqlNodes = sqlNodes;
	}

	/**
	 * @return the callBack
	 */
	public SqlResultCallBack getCallBack() {
		return callBack;
	}

	/**
	 * @param callBack the callBack to set
	 */
	public void setCallBack(SqlResultCallBack callBack) {
		this.callBack = callBack;
	}

	/**
	 * @return the reportBuilder
	 */
	public BuilderInterface getReportBuilder() {
		return reportBuilder;
	}

	/**
	 * @param reportBuilder the reportBuilder to set
	 */
	public void setReportBuilder(BuilderInterface reportBuilder) {
		this.reportBuilder = reportBuilder;
	}

	@Override
	public void finalizeProcessing() throws Exception {
		targetBuilder.finish();
	}
}
