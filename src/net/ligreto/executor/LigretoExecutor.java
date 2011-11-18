/**
 * 
 */
package net.ligreto.executor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.ligreto.Database;
import net.ligreto.builders.ReportBuilder;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.parser.nodes.LigretoNode;
import net.ligreto.parser.nodes.ReportNode;
import net.ligreto.parser.nodes.SqlNode;

/**
 * @author Julius Stroffek
 *
 */
public class LigretoExecutor extends Executor {
	LigretoNode ligretoNode;
	
	public LigretoExecutor(LigretoNode aLigretoNode) {
		ligretoNode = aLigretoNode;
	}
	
	protected void executeSqlNodes(Iterable<SqlNode> sqls) throws DataSourceNotDefinedException, ClassNotFoundException, SQLException {
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
	
	public void execute() throws LigretoException {
		Database.getInstance(ligretoNode);
		executePTPs();
		executeReports();
	}
	
	public void executePTPs() throws LigretoException {
		PtpExecutor ptpExecutor = new PtpExecutor();
		ptpExecutor.setPtpNodes(ligretoNode.ptps());
		ptpExecutor.execute();
	}
	
	public void executeReports() throws LigretoException {	
		for (ReportNode reportNode : ligretoNode.reports()) {
			executeReport(reportNode);
		}
	}
	
	public void executeReport(ReportNode reportNode) throws LigretoException {
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
			sqlExecutor.execute();
		
			JoinExecutor joinExecutor = new JoinExecutor();
			joinExecutor.setReportBuilder(reportBuilder);
			joinExecutor.setJoinNodes(reportNode.joins());
			joinExecutor.setCallBack(joinExecutor);
			joinExecutor.execute();
			
			reportBuilder.writeOutput();
		} catch (Exception e) {
			throw new LigretoException("Error creating the report: " + reportNode.getName(), e);
		}
		
	}
}
