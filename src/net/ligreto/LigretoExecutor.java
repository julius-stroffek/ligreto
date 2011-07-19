/**
 * 
 */
package net.ligreto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import net.ligreto.builders.ReportBuilder;
import net.ligreto.config.nodes.JoinNode;
import net.ligreto.config.nodes.LigretoNode;
import net.ligreto.config.nodes.ReportNode;
import net.ligreto.config.nodes.SqlNode;
import net.ligreto.exceptions.DataSourceNotDefinedException;

/**
 * @author Julius Stroffek
 *
 */
public class LigretoExecutor {
	LigretoNode ligretoNode;
	
	public LigretoExecutor(LigretoNode aLigretoNode) {
		ligretoNode = aLigretoNode;
	}
	
	public void executeReports() throws DataSourceNotDefinedException, ClassNotFoundException, SQLException {
		Database.getInstance(ligretoNode);
		for (ReportNode reportNode : ligretoNode.reports()) {
			executeReport(reportNode);
		}
	}
	
	public void executeReport(ReportNode reportNode) throws DataSourceNotDefinedException, ClassNotFoundException, SQLException {
		ReportBuilder reportBuilder = ReportBuilder.createInstance(reportNode.getReportType());
		reportBuilder.setTemplate(reportNode.getTemplate());
		reportBuilder.setOutput(reportNode.getOutput());
		for (SqlNode sqlQuery : reportNode.sqlQueries()) {
			reportBuilder.setTarget(sqlQuery.getTarget());
			Connection cnn = Database.getInstance().getConnection(sqlQuery.getDataSource());
			String qry = sqlQuery.getQuery().toString();
			Statement stm = cnn.createStatement();
			ResultSet rs = stm.executeQuery(qry);
			while (rs.next()) {
				reportBuilder.nextRow();
				reportBuilder.setColumnPosition(0);
				ResultSetMetaData rsmd = rs.getMetaData();				
				for (int i=1; i <= rsmd.getColumnCount(); i++)
					reportBuilder.setColumn(i, rs);
			}
		}
		for (JoinNode joinQueries : reportNode.joins()) {
			
		}
		reportBuilder.writeOutput();
	}
}
