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
import net.ligreto.ResultStatus;
import net.ligreto.builders.BuilderInterface;
import net.ligreto.builders.ReportBuilder;
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
	
	protected void executeSqlNodes(Iterable<SqlNode> sqls) throws ClassNotFoundException, SQLException, LigretoException {
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
	
	public ResultStatus execute() throws LigretoException {
		ResultStatus result = new ResultStatus();
		Database.getInstance(ligretoNode);
		result.merge(executePTPs());
		result.merge(executeReports());
		result.info(log, "LIGRETO");
		return result;
	}
	
	public ResultStatus executePTPs() throws LigretoException {
		PtpExecutor ptpExecutor = new PtpExecutor();
		ptpExecutor.setPtpNodes(ligretoNode.ptps());
		return ptpExecutor.execute();
	}
	
	public ResultStatus executeReports() throws LigretoException {
		ResultStatus result = new ResultStatus();
		for (ReportNode reportNode : ligretoNode.reports()) {
			result.merge(executeReport(reportNode));
		}
		return result;
	}
	
	public ResultStatus executeReport(ReportNode reportNode) throws LigretoException {
		ResultStatus result = new ResultStatus();
		try {
			BuilderInterface reportBuilder = ReportBuilder.createInstance(ligretoNode, reportNode.getReportType());
			reportBuilder.setLigretoParameters(reportNode.getLigretoNode().getLigretoParameters());
			reportBuilder.setTemplate(reportNode.getTemplate());
			reportBuilder.setOutputFileName(reportNode.getOutput());
			reportBuilder.setOptions(reportNode.getOptions());
			reportBuilder.start();
		
			SqlExecutor sqlExecutor = new SqlExecutor();
			sqlExecutor.setReportBuilder(reportBuilder);
			sqlExecutor.setSqlNodes(reportNode.sqlQueries());
			sqlExecutor.setCallBack(sqlExecutor);
			ResultStatus sqlResult = sqlExecutor.execute();
			result.merge(sqlResult);
		
			JoinExecutor joinExecutor = new JoinExecutor();
			joinExecutor.setReportBuilder(reportBuilder);
			joinExecutor.setJoinNodes(reportNode.joins());
			joinExecutor.setCallBack(joinExecutor);
			ResultStatus joinResult = joinExecutor.execute();
			result.merge(joinResult);
			
			reportBuilder.writeOutput();
		} catch (Exception e) {
			throw new LigretoException("Error creating the report: " + reportNode.getName(), e);
		}
		result.info(log, "REPORT");
		return result;
	}
}
