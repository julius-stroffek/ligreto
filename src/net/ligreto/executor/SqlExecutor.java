package net.ligreto.executor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.Database;
import net.ligreto.builders.ReportBuilder;
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
	protected ReportBuilder reportBuilder;
	
	/** The list of column indices to be excluded. */
	protected int[] excl = new int[0];
	
	@Override
	public boolean prepareProcessing(SqlNode sqlNode, ResultSet rs) throws Exception {
		// Go to the next SQL query if we do not have the target defined
		if (sqlNode.getTarget() == null)
			return false;
		
		String[] exclStr = sqlNode.getExcludeColumns();
		if (exclStr != null && exclStr.length > 0) {
			excl = new int[exclStr.length];
			for (int i=0; i < exclStr.length; i++) {
				excl[i] = MiscUtils.findColumnIndex(rs, exclStr[i]);
				log.info("Excluding column \"" + exclStr[i] + "\" from sql query which has the index: " + excl[i]);
			}
		}
		
		reportBuilder.setTarget(sqlNode.getTarget(), sqlNode.isAppend());
		if (sqlNode.getHeader()) {
			reportBuilder.dumpHeader(rs, excl);
		}
		return true;
	}
	
	@Override
	public void processResultSetRow(ResultSet rs) throws Exception {
		reportBuilder.nextRow();
		reportBuilder.setColumnPosition(0);
		ResultSetMetaData rsmd = rs.getMetaData();				
		for (int i=1, c=0; i <= rsmd.getColumnCount(); i++) {
			if (!MiscUtils.arrayContains(excl, i)) {
				reportBuilder.setColumn(c++, rs, i);
			}
		}
	}

	@Override
	public void execute() throws LigretoException {
		// Do nothing if there is nothing to process.
		if (sqlNodes == null)
			return;
		
		for (SqlNode sqlNode : sqlNodes) {
			try {
				Connection cnn = null;
				Statement stm = null;
				ResultSet rs = null;
				try {
					cnn = Database.getInstance().getConnection(sqlNode.getDataSource());
					String qry = sqlNode.getQuery().toString();
					stm = cnn.createStatement();
					log.info("Executing the SQL query on \"" + sqlNode.getDataSource() + "\" data source:");
					log.info(qry);
					try {
						rs = stm.executeQuery(qry);
						if (callBack != null) {
							if (callBack.prepareProcessing(sqlNode, rs)) {
								while (rs.next()) {
									callBack.processResultSetRow(rs);
								}
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
					Database.close(cnn, stm, rs);
				}
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
	public ReportBuilder getReportBuilder() {
		return reportBuilder;
	}

	/**
	 * @param reportBuilder the reportBuilder to set
	 */
	public void setReportBuilder(ReportBuilder reportBuilder) {
		this.reportBuilder = reportBuilder;
	}

}
