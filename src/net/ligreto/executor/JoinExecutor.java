package net.ligreto.executor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.Database;
import net.ligreto.builders.ReportBuilder;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.exceptions.UnimplementedMethodException;
import net.ligreto.parser.nodes.JoinNode;
import net.ligreto.parser.nodes.SqlNode;
import net.ligreto.util.MiscUtils;
import net.ligreto.util.ResultSetComparator;

public class JoinExecutor extends Executor implements JoinResultCallBack {

	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(JoinExecutor.class);

	/** Iterable object holding the join nodes to be processed. */
	Iterable<JoinNode> joinNodes;
	
	/** The callback object which handles the processing of each row returned. */
	protected JoinResultCallBack callBack;
	
	/** The <code>ReportBuilder</code> object used to process the results. */
	protected ReportBuilder reportBuilder;

	@Override
	public boolean prepareProcessing(JoinNode joinNode, ResultSet rs1, ResultSet rs2)
			throws Exception {
		return true;
	}

	@Override
	public void processLeftResult(JoinNode joinNode, ResultSet rs1) throws Exception {
		throw new UnimplementedMethodException("Callback implementation is not done for join execution");
	}

	@Override
	public void processRightResult(JoinNode joinNode, ResultSet rs2) throws Exception {
		throw new UnimplementedMethodException("Callback implementation is not done for join execution");
	}

	@Override
	public void processJoinResult(JoinNode joinNode, ResultSet rs1, ResultSet rs2)
			throws Exception {
		throw new UnimplementedMethodException("Callback implementation is not done for join execution");
	}

	@Override
	public void execute() throws LigretoException {
		try {
			for (JoinNode joinNode : joinNodes) {
				executeJoin(joinNode);
			}
		} catch (Exception e) {
			LigretoException ne = new LigretoException("Could not process the join.");
			ne.initCause(e);
			throw ne;

		}
	}

	protected void executeJoin(JoinNode joinNode) throws SQLException, LigretoException, ClassNotFoundException {
		reportBuilder.setTarget(joinNode.getTarget(), joinNode.isAppend());
		JoinNode.JoinType joinType = joinNode.getJoinType();
		List<SqlNode> sqlQueries = joinNode.getSqlQueries();
		
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
		
			StringBuilder qry1 = new StringBuilder(sqlQueries.get(0).getQuery().toString());
			StringBuilder qry2 = new StringBuilder(sqlQueries.get(1).getQuery().toString());			
			stm1 = cnn1.createStatement();
			stm2 = cnn2.createStatement();
			
			int on1[] = sqlQueries.get(0).getOn();
			int on2[] = sqlQueries.get(1).getOn();
			if (on1 == null)
				on1 = joinNode.getOn();
			if (on2 == null)
				on2 = joinNode.getOn();
			
			if (on1 == null || on2 == null)
				throw new LigretoException("The \"on\" attribute have to be present in <join> node or all <sql> children.");
			
			// Do certain sanity checks here
			if (on1.length != on2.length)
				throw new LigretoException("All queries in the join have to have the same number of \"on\" columns.");
			
			// Things are all right, so we will continue...
			int onLength = on1.length;
			qry1.append(" order by ");
			qry2.append(" order by ");
			for (int i=0; i < on1.length; i++) {
				qry1.append(on1[i]);
				qry1.append(",");
				qry2.append(on2[i]);
				qry2.append(",");
			}
			qry1.deleteCharAt(qry1.length() - 1);
			qry2.deleteCharAt(qry2.length() - 1);
			
			log.info("Executing the SQL query on \"" + sqlQueries.get(0).getDataSource() + "\" data source:");
			log.info(qry1);
			rs1 = stm1.executeQuery(qry1.toString());
			
			log.info("Executing the SQL query on \"" + sqlQueries.get(1).getDataSource() + "\" data source:");
			log.info(qry2);
			rs2 = stm2.executeQuery(qry2.toString());
			
			ResultSetMetaData rsmd1 = rs1.getMetaData();
			ResultSetMetaData rsmd2 = rs2.getMetaData();
			
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

			// Process the exclude columns
			String[] exclStr1 = sqlQueries.get(0).getExclude();
			String[] exclStr2 = sqlQueries.get(1).getExclude();
			
			if (exclStr1 == null)
				exclStr1 = joinNode.getExclude();
			
			if (exclStr2 == null)
				exclStr2 = joinNode.getExclude();
			
			// Initialize exclude arrays to empty arrays
			int[] excl1 = new int[0];
			int[] excl2 = new int[0];
			
			// Convert the column names into numbers for sql query 1
			if (exclStr1 != null && exclStr1.length > 0) {
				excl1 = new int[exclStr1.length];
				for (int i=0; i < exclStr1.length; i++) {
					excl1[i] = MiscUtils.findColumnIndex(rs1, exclStr1[i]);
					if (MiscUtils.arrayContains(on1, excl1[i])) {
						throw new LigretoException("Column listed in 'exclude' attribute cannot be in used in 'on' clause:" + exclStr1[i]);
					}
				}
			}
			
			// Convert the column names into numbers for sql query 2
			if (exclStr2 != null && exclStr2.length > 0) {
				excl2 = new int[exclStr2.length];
				for (int i=0; i < exclStr2.length; i++) {
					excl2[i] = MiscUtils.findColumnIndex(rs2, exclStr2[i]);
					if (MiscUtils.arrayContains(on2, excl2[i])) {
						throw new LigretoException("Column listed in 'exclude' attribute cannot be in used in 'on' clause:" + exclStr2[i]);
					}
				}
			}
			
			int rs1ColCount = rs1.getMetaData().getColumnCount() - excl1.length;
			int rs2ColCount = rs2.getMetaData().getColumnCount() - excl2.length;
			
			// Create the arrays to be used to highlight
			// differences for left, right, outer joins
			int[] lowerArray = new int[rs1ColCount > rs2ColCount ? rs1ColCount : rs2ColCount];
			int[] higherArray = new int[lowerArray.length];
			for (int i=0; i < lowerArray.length; i++) {
				lowerArray[i] = -1;
				higherArray[i] = 1;
			}
			reportBuilder.setHighlight(joinNode.getHighlight());
			reportBuilder.setHlColor(joinNode.getHlColor());
			
			// Dump the header row if requested
			if (joinNode.getHeader()) {
				reportBuilder.nextRow();
				reportBuilder.dumpJoinOnHeader(rs1, on1);
				if (joinNode.getInterlaced()) {
					reportBuilder.setColumnPosition(onLength, 2, null);
				} else  {
					reportBuilder.setColumnPosition(onLength, 1, null);							
				}
				reportBuilder.dumpOtherHeader(rs1, on1, excl1);
				if (joinNode.getInterlaced()) {
					reportBuilder.setColumnPosition(onLength+1, 2, null);
				} else {
					reportBuilder.setColumnPosition(rs1ColCount, 1, null);
				}
				reportBuilder.dumpOtherHeader(rs2, on2, excl2);
			}
			
			boolean hasNext1 = rs1.next();
			boolean hasNext2 = rs2.next();
			while (hasNext1 && hasNext2) {
				int cResult = ResultSetComparator.compare(rs1, on1, rs2, on2); 
				switch (cResult) {
				case -1:
					if (joinType == JoinNode.JoinType.LEFT || joinType == JoinNode.JoinType.FULL) {
						reportBuilder.nextRow();
						reportBuilder.setHighlightArray(higherArray);
						reportBuilder.setJoinOnColumns(rs1, on1);
						if (joinNode.getInterlaced()) {
							reportBuilder.setColumnPosition(onLength, 2, lowerArray);
						} else  {
							reportBuilder.setColumnPosition(onLength, 1, lowerArray);							
						}
						reportBuilder.setOtherColumns(rs1, on1, excl1);
					}
					hasNext1 = rs1.next();
					break;
				case 0:
					// We will break if we are supposed to produce only differences
					// and there are no differences present.
					int[] cmpArray = ResultSetComparator.compareOthers(rs1, on1, excl1, rs2, on2, excl2);
					
					if (!joinNode.getDiffs() || !MiscUtils.allZeros(cmpArray)) {
						reportBuilder.nextRow();
						reportBuilder.setJoinOnColumns(rs1, on1);
						if (joinNode.getInterlaced()) {
							reportBuilder.setColumnPosition(onLength, 2, cmpArray);
						} else  {
							reportBuilder.setColumnPosition(onLength, 1, cmpArray);							
						}
						
						reportBuilder.setOtherColumns(rs1, on1, excl1);
						
						if (joinNode.getInterlaced()) {
							reportBuilder.setColumnPosition(onLength+1, 2, cmpArray);
						} else {
							reportBuilder.setColumnPosition(rs1ColCount, 1, cmpArray);
						}
						reportBuilder.setOtherColumns(rs2, on2, excl1);
					}
					hasNext1 = rs1.next();
					hasNext2 = rs2.next();
					break;
				case 1:
					if (joinType == JoinNode.JoinType.RIGHT || joinType == JoinNode.JoinType.FULL) {
						reportBuilder.nextRow();							
						reportBuilder.setHighlightArray(lowerArray);
						reportBuilder.setJoinOnColumns(rs2, on2);
						if (joinNode.getInterlaced()) {
							reportBuilder.setColumnPosition(onLength+1, 2, higherArray);
						} else  {
							reportBuilder.setColumnPosition(rs1ColCount, 1, higherArray);							
						}
						reportBuilder.setOtherColumns(rs2, on2, excl2);
					}
					hasNext2 = rs2.next();
					break;
				}				
			}
			if (joinType == JoinNode.JoinType.LEFT || joinType == JoinNode.JoinType.FULL) {
				while (hasNext1) {
					reportBuilder.nextRow();
					reportBuilder.setHighlightArray(higherArray);
					reportBuilder.setJoinOnColumns(rs1, on1);
					if (joinNode.getInterlaced()) {
						reportBuilder.setColumnPosition(onLength, 2, lowerArray);
					} else  {
						reportBuilder.setColumnPosition(onLength, 1, lowerArray);							
					}
					reportBuilder.setOtherColumns(rs1, on1, excl1);
					hasNext1 = rs1.next();
				}
			}
			if (joinType == JoinNode.JoinType.RIGHT || joinType == JoinNode.JoinType.FULL) {
				while (hasNext2) {
					reportBuilder.nextRow();
					reportBuilder.setHighlightArray(lowerArray);
					reportBuilder.setJoinOnColumns(rs2, on2);
					if (joinNode.getInterlaced()) {
						reportBuilder.setColumnPosition(onLength+1, 2, higherArray);
					} else  {
						reportBuilder.setColumnPosition(rs1ColCount, 1, higherArray);							
					}
					reportBuilder.setOtherColumns(rs2, on2, excl2);
					hasNext2 = rs2.next();
				}
			}
		} finally {
			Database.close(cnn1, stm1, rs1);
			Database.close(cnn2, stm2, rs2);
		}
	}

	/**
	 * @return the joinNodes
	 */
	public Iterable<JoinNode> getJoinNodes() {
		return joinNodes;
	}

	/**
	 * @param joinNodes the joinNodes to set
	 */
	public void setJoinNodes(Iterable<JoinNode> joinNodes) {
		this.joinNodes = joinNodes;
	}

	/**
	 * @return the callBack
	 */
	public JoinResultCallBack getCallBack() {
		return callBack;
	}

	/**
	 * @param callBack the callBack to set
	 */
	public void setCallBack(JoinResultCallBack callBack) {
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
