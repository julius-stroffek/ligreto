/**
 * 
 */
package net.ligreto;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.ligreto.builders.ReportBuilder;
import net.ligreto.config.nodes.JoinNode;
import net.ligreto.config.nodes.LigretoNode;
import net.ligreto.config.nodes.ReportNode;
import net.ligreto.config.nodes.SqlNode;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.exceptions.UnimplementedMethodException;
import net.ligreto.util.ResultSetComparator;

/**
 * @author Julius Stroffek
 *
 */
public class LigretoExecutor {
	LigretoNode ligretoNode;
	
	public LigretoExecutor(LigretoNode aLigretoNode) {
		ligretoNode = aLigretoNode;
	}
	
	public void executeReports() throws ClassNotFoundException, SQLException, IOException, LigretoException {
		Database.getInstance(ligretoNode);
		for (ReportNode reportNode : ligretoNode.reports()) {
			executeReport(reportNode);
		}
	}
	
	public void executeReport(ReportNode reportNode) throws ClassNotFoundException, SQLException, IOException, LigretoException {
		ReportBuilder reportBuilder = ReportBuilder.createInstance(ligretoNode, reportNode.getReportType());
		reportBuilder.setTemplate(reportNode.getTemplate());
		reportBuilder.setOutput(reportNode.getOutput());
		reportBuilder.start();
		for (SqlNode sqlQuery : reportNode.sqlQueries()) {
			reportBuilder.setTarget(sqlQuery.getTarget());
			Connection cnn = null;
			Statement stm = null;
			ResultSet rs = null;
			try {
				cnn = Database.getInstance().getConnection(sqlQuery.getDataSource());
				String qry = sqlQuery.getQuery().toString();
				stm = cnn.createStatement();
				rs = stm.executeQuery(qry);
				if (sqlQuery.getHeader()) {
					reportBuilder.dumpHeader(rs);
				}
				while (rs.next()) {
					reportBuilder.nextRow();
					reportBuilder.setColumnPosition(0);
					ResultSetMetaData rsmd = rs.getMetaData();				
					for (int i=1; i <= rsmd.getColumnCount(); i++)
						reportBuilder.setColumn(i, rs);
				}
			} finally {
				Database.close(cnn, stm, rs);
			}
		}
		for (JoinNode join : reportNode.joins()) {
			reportBuilder.setTarget(join.getTarget());
			JoinNode.JoinType joinType = join.getJoinType();
			List<SqlNode> sqlQueries = join.getSqlQueries();
			
			if (sqlQueries.size() < 2)
				throw new LigretoException("There have to be two queries defined for a join.");
			
			if (sqlQueries.size() > 2)
				throw new UnimplementedMethodException("Join of more than 2 tables is not yet implemented");
			
			Connection cnn1 = null, cnn2 = null;
			Statement stm1 = null, stm2 = null;
			ResultSet rs1 = null, rs2 = null;
			try {
				cnn1 = Database.getInstance().getConnection(sqlQueries.get(0).getDataSource());
				cnn2 = Database.getInstance().getConnection(sqlQueries.get(1).getDataSource());
			
				String qry1 = sqlQueries.get(0).getQuery().toString();
				String qry2 = sqlQueries.get(1).getQuery().toString();			
				stm1 = cnn1.createStatement();
				stm2 = cnn2.createStatement();
				rs1 = stm1.executeQuery(qry1);
				rs2 = stm2.executeQuery(qry2);
				ResultSetMetaData rsmd1 = rs1.getMetaData();
				ResultSetMetaData rsmd2 = rs2.getMetaData();
				int on1[] = sqlQueries.get(0).getOn();
				int on2[] = sqlQueries.get(1).getOn();
				
				if (on1 == null)
					on1 = join.getOn();
				if (on2 == null)
					on2 = join.getOn();
				
				if (on1 == null || on2 == null)
					throw new LigretoException("The \"on\" attribute have to be present in <join> node or all <sql> children.");
				
				// Do certain sanity checks here
				if (on1.length != on2.length)
					throw new LigretoException("All queries in the join have to have the same number of \"on\" columns.");
				
				for (int i=0; i < on1.length; i++) {
					if (on1[i] > rsmd1.getColumnCount())
						throw new LigretoException("Index in \"on\" clause of 1st query is out of the range. It is \""
								+ on1[i] + "\" and should be \"" + rsmd1.getColumnCount() + "\" the largest.");
				}
			
				for (int i=0; i < on2.length; i++) {
					if (on2[i] > rsmd2.getColumnCount())
						throw new LigretoException("Index in \"on\" clause of 2nd query is out of the range. It is \""
								+ on2[i] + "\" and should be \"" + rsmd2.getColumnCount() + "\" the largest.");
				}

				// Things are all right, so we will continue...
				int onLength = on1.length;
				int rs1Length = rs1.getMetaData().getColumnCount();
				
				// Dump the header row if requested
				if (join.getHeader()) {
					reportBuilder.nextRow();
					reportBuilder.dumpJoinOnHeader(rs1, on1);
					if (join.getInterlaced()) {
						reportBuilder.setColumnPosition(onLength, 2);
					} else  {
						reportBuilder.setColumnPosition(onLength, 1);							
					}
					reportBuilder.dumpOtherHeader(rs1, on1);
					if (join.getInterlaced()) {
						reportBuilder.setColumnPosition(onLength+1, 2);
					} else {
						reportBuilder.setColumnPosition(rs1Length, 1);
					}
					reportBuilder.dumpOtherHeader(rs2, on2);
				}
				
				boolean hasNext1 = rs1.next();
				boolean hasNext2 = rs2.next();
				while (hasNext1 && hasNext2) {
					int cResult = ResultSetComparator.compare(rs1, on1, rs2, on2); 
					switch (cResult) {
					case -1:
						if (joinType == JoinNode.JoinType.LEFT || joinType == JoinNode.JoinType.FULL) {
							reportBuilder.nextRow();
							reportBuilder.setJoinOnColumns(rs1, on1);
							if (join.getInterlaced()) {
								reportBuilder.setColumnPosition(onLength, 2);
							} else  {
								reportBuilder.setColumnPosition(onLength, 1);							
							}
							reportBuilder.setOtherColumns(rs1, on1);
						}
						hasNext1 = rs1.next();
						break;
					case 0:
						// We will break if we are supposed to produce only differences
						// and there are no differences present.
						if (!join.getDiffs() || ResultSetComparator.compareOthers(rs1, on1, rs2, on2) != 0) {
							reportBuilder.nextRow();
							reportBuilder.setJoinOnColumns(rs1, on1);
							if (join.getInterlaced()) {
								reportBuilder.setColumnPosition(onLength, 2);
							} else  {
								reportBuilder.setColumnPosition(onLength, 1);							
							}
							reportBuilder.setOtherColumns(rs1, on1);
							if (join.getInterlaced()) {
								reportBuilder.setColumnPosition(onLength+1, 2);
							} else {
								reportBuilder.setColumnPosition(rs1Length, 1);
							}
							reportBuilder.setOtherColumns(rs2, on2);
						}
						hasNext1 = rs1.next();
						hasNext2 = rs2.next();
						break;
					case 1:
						if (joinType == JoinNode.JoinType.RIGHT || joinType == JoinNode.JoinType.FULL) {
							reportBuilder.nextRow();							
							reportBuilder.setJoinOnColumns(rs2, on2);
							if (join.getInterlaced()) {
								reportBuilder.setColumnPosition(onLength+1, 2);
							} else  {
								reportBuilder.setColumnPosition(rs1Length, 1);							
							}
							reportBuilder.setOtherColumns(rs2, on2);
						}
						hasNext2 = rs2.next();
						break;
					}				
				}
				if (joinType == JoinNode.JoinType.LEFT || joinType == JoinNode.JoinType.FULL) {
					while (hasNext1) {
						reportBuilder.nextRow();
						reportBuilder.setJoinOnColumns(rs1, on1);
						if (join.getInterlaced()) {
							reportBuilder.setColumnPosition(onLength, 2);
						} else  {
							reportBuilder.setColumnPosition(onLength, 1);							
						}
						reportBuilder.setOtherColumns(rs1, on1);
						hasNext1 = rs1.next();
					}
				}
				if (joinType == JoinNode.JoinType.RIGHT || joinType == JoinNode.JoinType.FULL) {
					while (hasNext2) {
						reportBuilder.nextRow();
						reportBuilder.setJoinOnColumns(rs2, on2);
						if (join.getInterlaced()) {
							reportBuilder.setColumnPosition(onLength+1, 2);
						} else  {
							reportBuilder.setColumnPosition(rs1Length, 1);							
						}
						reportBuilder.setOtherColumns(rs2, on2);
						hasNext2 = rs2.next();
					}
				}
			} finally {
				Database.close(cnn1, stm1, rs1);
				Database.close(cnn2, stm2, rs2);
			}
			
		}
		reportBuilder.writeOutput();
	}
}
