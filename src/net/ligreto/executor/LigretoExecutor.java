/**
 * 
 */
package net.ligreto.executor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.Database;
import net.ligreto.builders.ReportBuilder;
import net.ligreto.exceptions.DataSourceException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.parser.nodes.LigretoNode;
import net.ligreto.parser.nodes.ReportNode;
import net.ligreto.parser.nodes.SqlNode;

/**
 * @author Julius Stroffek
 *
 */
public class LigretoExecutor extends Executor {
	
	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(LigretoExecutor.class);
	
	LigretoNode ligretoNode;
	
	public LigretoExecutor(LigretoNode aLigretoNode) {
		ligretoNode = aLigretoNode;
	}
	
	protected void executeSqlNodes(Iterable<SqlNode> sqls) throws DataSourceException, ClassNotFoundException, SQLException {
		for (SqlNode sqlQuery : sqls) {
			Connection cnn = null;
			Statement stm = null;
			ResultSet rs = null;
			try {
				cnn = Database.getInstance().getConnection(sqlQuery.getDataSource());
				String qry = sqlQuery.getQuery().toString();
				stm = cnn.createStatement();
				rs = stm.executeQuery(qry);
			} finally {
				Database.close(cnn, stm, rs);
			}
		}
	}
	
	public int execute() throws LigretoException {
		int result = 0;
		Database.getInstance(ligretoNode);
		result += executePTPs();
		result += executeReports();
		return result;
	}
	
	public int executePTPs() throws LigretoException {
		PtpExecutor ptpExecutor = new PtpExecutor();
		ptpExecutor.setPtpNodes(ligretoNode.ptps());
		return ptpExecutor.execute();
	}
	
	public int executeReports() throws LigretoException {
		int result = 0;
		for (ReportNode reportNode : ligretoNode.reports()) {
			result += executeReport(reportNode);
		}
		log.info("LIGRETO result row count: " + result);
		return result;
	}
	
	public int executeReport(ReportNode reportNode) throws LigretoException {
		int result = 0;
		try {
			ReportBuilder reportBuilder = ReportBuilder.createInstance(ligretoNode, reportNode.getReportType());
			reportBuilder.setTemplate(reportNode.getTemplate());
			reportBuilder.setOutput(reportNode.getOutput());
			reportBuilder.setOptions(reportNode.getOptions());
			reportBuilder.start();
		
			SqlExecutor sqlExecutor = new SqlExecutor();
			sqlExecutor.setReportBuilder(reportBuilder);
			sqlExecutor.setSqlNodes(reportNode.sqlQueries());
			sqlExecutor.setCallBack(sqlExecutor);
			int sqlResult = sqlExecutor.execute();
			log.info("REPORT/SQLs result row count: " + result);
			if (reportNode.getResult())
				result += sqlResult;
		
			JoinExecutor joinExecutor = new JoinExecutor();
			joinExecutor.setReportBuilder(reportBuilder);
			joinExecutor.setJoinNodes(reportNode.joins());
			joinExecutor.setCallBack(joinExecutor);
			int joinResult = joinExecutor.execute();
			log.info("REPORT/JOINs result row count: " + result);
			if (reportNode.getResult())
				result += joinResult;
			
			reportBuilder.writeOutput();
		} catch (Exception e) {
			throw new LigretoException("Error creating the report: " + reportNode.getName(), e);
		}
		log.info("REPORT result row count: " + result);
		return result;
	}
}
