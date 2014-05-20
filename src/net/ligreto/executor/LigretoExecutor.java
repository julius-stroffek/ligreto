/**
 * 
 */
package net.ligreto.executor;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.Database;
import net.ligreto.ResultStatus;
import net.ligreto.builders.BuilderInterface;
import net.ligreto.builders.ReportBuilder;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.parser.nodes.EmailNode;
import net.ligreto.parser.nodes.JoinNode;
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
	
	@Override
	public ResultStatus execute() throws LigretoException {
		ResultStatus result = new ResultStatus();
		Database.getInstance(ligretoNode);
		result.merge(executeParams());
		result.merge(executePTPs());
		result.merge(executeReports());
		result.info(log, "LIGRETO");
		return result;
	}
	
	public ResultStatus executeParams() throws LigretoException {
		ParamExecutor paramExecutor = new ParamExecutor();
		paramExecutor.setParamNodes(ligretoNode.params());
		return paramExecutor.execute();
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

			// Prepare the SQL executor
			SqlExecutor sqlExecutor = new SqlExecutor();
			sqlExecutor.setReportBuilder(reportBuilder);
			sqlExecutor.setCallBack(sqlExecutor);
		
			// Prepare the join / comparison / reconciliation executor
			JoinExecutor joinExecutor = new JoinExecutor();
			joinExecutor.setReportBuilder(reportBuilder);
			joinExecutor.setCallBack(joinExecutor);

			// Prepare the iterators over sql and join nodes
			Iterator<SqlNode> sqlIterator = reportNode.sqlQueries().iterator();
			Iterator<JoinNode> joinIterator = reportNode.joins().iterator();
			
			// We will use merge algorithm to execute the processing
			// of SQL and JOIN nodes in the order as they were defined
			// in the configuration
			SqlNode sqlNode = sqlIterator.hasNext() ? sqlIterator.next() : null;
			JoinNode joinNode = joinIterator.hasNext() ? joinIterator.next() : null;
			while (sqlNode != null || joinNode != null) {
				boolean processJoin = false;
				if (sqlNode == null) {
					processJoin = true;
				} else if (joinNode == null) {
					processJoin = false;
				} else {
					assert(sqlNode.getOrderNumber() != joinNode.getOrderNumber());
					if (joinNode.getOrderNumber() < sqlNode.getOrderNumber()) {
						processJoin = true;
					} else {
						processJoin = false;
					}
				}
				if (processJoin) {
					ResultStatus joinResult = joinExecutor.execute(joinNode);
					result.merge(joinResult);

					joinNode = joinIterator.hasNext() ? joinIterator.next() : null;
				} else {
					ResultStatus sqlResult = sqlExecutor.execute(sqlNode);
					result.merge(sqlResult);

					sqlNode = sqlIterator.hasNext() ? sqlIterator.next() : null;
				}
			}		
			reportBuilder.writeOutput();
			
			// Here we will process the post build actions
			EmailExecutor emailExecutor = new EmailExecutor();
			for (EmailNode email : reportNode.emails()) {
				File reportFile = reportBuilder.getOutputFile();
				emailExecutor.execute(email, result, reportFile);
			}
		} catch (Exception e) {
			throw new LigretoException("Error processing the report: " + reportNode.getName(), e);
		}
		result.info(log, "REPORT");
		return result;
	}
}
