package net.ligreto.executor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.Database;
import net.ligreto.ResultStatus;
import net.ligreto.builders.BuilderInterface;
import net.ligreto.exceptions.CollationException;
import net.ligreto.exceptions.DuplicateJoinColumnsException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.exceptions.UnimplementedMethodException;
import net.ligreto.executor.layouts.DetailedJoinLayout;
import net.ligreto.executor.layouts.InterlacedJoinLayout;
import net.ligreto.executor.layouts.JoinLayout;
import net.ligreto.executor.layouts.JoinLayout.JoinResultType;
import net.ligreto.executor.layouts.NormalJoinLayout;
import net.ligreto.parser.nodes.JoinNode;
import net.ligreto.parser.nodes.SqlNode;
import net.ligreto.parser.nodes.Node.Attitude;
import net.ligreto.util.MiscUtils;
import net.ligreto.util.ResultSetComparator;

public class JoinExecutor extends Executor implements JoinResultCallBack {

	/** The collation error message used in multiple places. */
	private static final String collationError = "The order of rows received from database does not match the used locale; set locale attribute in the <report> or <join> nodes; data source: %s; target: %s";

	/** The collation error message used in multiple places. */
	private static final String duplicateJoinColumnsError = "The rows received are duplicate in columns specified as 'on' columns; use 'on' columns that are unique; data source: %s; target: %s";
	
	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(JoinExecutor.class);

	/** Iterable object holding the join nodes to be processed. */
	Iterable<JoinNode> joinNodes;
	
	/** The callback object which handles the processing of each row returned. */
	protected JoinResultCallBack callBack;
	
	/** The <code>ReportBuilder</code> object used to process the results. */
	protected BuilderInterface reportBuilder;
	
	/** The locale to be used for join processing. */
	protected Locale locale;
	
	/** The collator to be used for join processing based on the specified locale. */
	protected Comparator<Object> comparator;

	@Override
	public boolean prepareProcessing(JoinNode joinNode, ResultSet rs1, ResultSet rs2) throws Exception {
		throw new UnimplementedMethodException("Callback implementation is not done for join execution");
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
	public ResultStatus execute() throws LigretoException {
		ResultStatus result = new ResultStatus();
		try {
			for (JoinNode joinNode : joinNodes) {
				String localeName = joinNode.getLocale();
				if (localeName == null) {
					localeName = joinNode.getReportNode().getLocale();
				}
				if (localeName != null) {
					locale = new Locale(localeName);
				} else {
					locale = Locale.getDefault();
				}
				String collatorName = joinNode.getLigretoNode().getParam("ligreto.collatorClass");
				if ("oracle.i18n.text.OraCollator".equals(collatorName)) {
					Class<?> collatorClass = Class.forName(collatorName);
					
					Method method = collatorClass.getMethod("getInstance", String.class);
					String collationName = joinNode.getLigretoNode().getParam("ligreto.collationName").trim();
				
					if (collationName == null || "".equals(collationName)) {
						throw new LigretoException("Parameter ligreto.collationName must be specified.");
					}
					
					/* The local variable below is only due to @SuppressWarnings annotation. */
					@SuppressWarnings("unchecked")
					Comparator<Object> comparator = (Comparator<Object>)method.invoke(null, collationName);
					this.comparator = comparator;
					log.info("Using collator class: " + collatorClass + "; collation: " + collationName);
				} else if (collatorName == null) {
					Collator collator = Collator.getInstance(locale);
					collator.setDecomposition(Collator.FULL_DECOMPOSITION);
					comparator = collator;
				} else {
					throw new LigretoException("Unsupported collator: " + collatorName);
				}
				result.merge(executeJoin(joinNode));
			}
		} catch (Exception e) {
			throw new LigretoException("Could not process the join.", e);
		}
		return result;
	}

	protected void returnRowNormal() {
		
	}
	
	protected void returnRowInterlaced() {
		
	}
	
	protected void returnRowDetailed() {
		
	}
	
	protected ResultStatus executeJoin(JoinNode joinNode) throws SQLException, LigretoException, ClassNotFoundException, IOException {
		ResultStatus result = new ResultStatus();
		reportBuilder.setTarget(joinNode.getTarget(), joinNode.isAppend());
		JoinNode.JoinType joinType = joinNode.getJoinType();
		List<SqlNode> sqlQueries = joinNode.getSqlQueries();
		
		if (sqlQueries.size() < 2)
			throw new LigretoException("There have to be two queries defined for a join.");
		
		if (sqlQueries.size() > 2)
			throw new UnimplementedMethodException("Join of more than 2 tables is not yet implemented");
		
		Connection cnn1 = null, cnn2 = null;
		Statement stm1 = null, stm2 = null;
		CallableStatement cstm1 = null, cstm2 = null;
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
			
			switch (sqlQueries.get(0).getQueryType()) {
			case STATEMENT:
				log.info("Executing the SQL statement on \"" + sqlQueries.get(0).getDataSource() + "\" data source:");
				log.info(qry1);
				stm1 = cnn1.createStatement();
				rs1 = stm1.executeQuery(qry1.toString());
				break;
			case CALL:
				log.info("Executing the SQL callable statement on \"" + sqlQueries.get(0).getDataSource() + "\" data source:");
				log.info(qry1);
				cstm1 = cnn1.prepareCall(qry1.toString());
				rs1 = cstm1.executeQuery();
				break;
			default:
				throw new LigretoException("Unknown query type.");
			}
			
			switch (sqlQueries.get(1).getQueryType()) {
			case STATEMENT:
				log.info("Executing the SQL statement on \"" + sqlQueries.get(1).getDataSource() + "\" data source:");
				log.info(qry2);
				stm2 = cnn2.createStatement();
				rs2 = stm2.executeQuery(qry2.toString());
				break;
			case CALL:
				log.info("Executing the SQL callable statement on \"" + sqlQueries.get(1).getDataSource() + "\" data source:");
				log.info(qry2);
				cstm2 = cnn2.prepareCall(qry2.toString());
				rs2 = cstm2.executeQuery();
				break;
			default:
				throw new LigretoException("Unknown query type.");
			}
			
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
			String[] exclStr1 = sqlQueries.get(0).getExcludeColumns();
			String[] exclStr2 = sqlQueries.get(1).getExcludeColumns();
			
			if (exclStr1 == null)
				exclStr1 = joinNode.getExcludeColumns();
			
			if (exclStr2 == null)
				exclStr2 = joinNode.getExcludeColumns();
			
			// Initialize exclude arrays to empty arrays
			int[] excl1Tmp = new int[0];
			int[] excl2Tmp = new int[0];
			int excl1Count = 0, excl2Count = 0;
			
			// Convert the column names into numbers for sql query 1
			if (exclStr1 != null && exclStr1.length > 0) {
				excl1Tmp = new int[exclStr1.length];
				for (int i=0; i < exclStr1.length; i++) {
					excl1Tmp[i] = MiscUtils.findColumnIndex(rs1, exclStr1[i]);
					if (excl1Tmp[i] >= 0) {
						if (MiscUtils.arrayContains(on1, excl1Tmp[i])) {
							throw new LigretoException("Column listed in 'exclude' attribute cannot be used in 'on' clause:" + exclStr1[i]);
						}
						excl1Count++;
						log.info("Excluding column \"" + exclStr1[i] + "\" from 1st sql query which has the index: " + excl1Tmp[i]);
					} else {
						log.info("Column to be exculded \"" + exclStr1[i] + "\" from 1st sql query was not found in the result set.");												
					}
				}
			}
			
			// Convert the column names into numbers for sql query 2
			if (exclStr2 != null && exclStr2.length > 0) {
				excl2Tmp = new int[exclStr2.length];
				for (int i=0; i < exclStr2.length; i++) {
					excl2Tmp[i] = MiscUtils.findColumnIndex(rs2, exclStr2[i]);
					if (excl2Tmp[i] >= 0) {
						if (MiscUtils.arrayContains(on2, excl2Tmp[i])) {
							throw new LigretoException("Column listed in 'exclude' attribute cannot be used in 'on' clause:" + exclStr2[i]);
						}
						excl2Count++;
						log.info("Excluding column \"" + exclStr2[i] + "\" from 2nd sql query which has the index: " + excl2Tmp[i]);
					} else {
						log.info("Column to be exculded \"" + exclStr2[i] + "\" from 2nd sql query was not found in the result set.");						
					}
				}
			}
			
			int[] excl1 = new int[excl1Count];
			int[] excl2 = new int[excl2Count];
			
			for (int i=0, ti=0; i < excl1Tmp.length; i++) {
				if (excl1Tmp[i] >= 0) {
					excl1[ti] = excl1Tmp[i];
					ti++;
				}
			}
			for (int i=0, ti=0; i < excl2Tmp.length; i++) {
				if (excl2Tmp[i] >= 0) {
					excl2[ti] = excl2Tmp[i];
					ti++;
				}
			}
			
			int rs1ColCount = rs1.getMetaData().getColumnCount() - excl1.length;
			int rs2ColCount = rs2.getMetaData().getColumnCount() - excl2.length;
			
			if (rs1ColCount != rs2ColCount) {
				throw new LigretoException("Result set column counts differs: " + rs1ColCount + " and " + rs2ColCount);
			}
			
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
			
			/* Create the proper implementation of the join layout. */
			JoinLayout joinLayout;
			switch (joinNode.getJoinLayoutType()) {
			case NORMAL:
				joinLayout = new NormalJoinLayout(reportBuilder, joinNode.getLigretoNode().getLigretoParameters());
				break;
			case INTERLACED:
				joinLayout = new InterlacedJoinLayout(reportBuilder, joinNode.getLigretoNode().getLigretoParameters());
				break;
			case DETAILED:
				joinLayout = new DetailedJoinLayout(reportBuilder, joinNode.getLigretoNode().getLigretoParameters());
				break;
			default:
				throw new LigretoException("Unexpected value of JoinLayoutType.");
			}

			// Setup other parameters required for the join layout
			joinLayout.setJoinNode(joinNode);
			joinLayout.setResultStatus(result);
			joinLayout.setOnColumns(on1, on2);
			joinLayout.setExcludeColumns(excl1, excl2);
			joinLayout.setResultSets(rs1, rs2);
			joinLayout.setColumnCount(rs1ColCount);
			
			// Dump the header row if requested
			if (joinNode.getHeader()) {
				joinLayout.dumpHeader();
			}
			
			boolean hasNext1 = rs1.next();
			boolean hasNext2 = rs2.next();
			ResultSetComparator rsComparator = new ResultSetComparator(comparator);
			ResultSetComparator.Column[] pCol1 = null;
			ResultSetComparator.Column[] pCol2 = null;
			ResultSetComparator.Column[] col1 = null;
			ResultSetComparator.Column[] col2 = null;
			while (hasNext1 && hasNext2) {
				// Compare the subsequent rows in each result set and see whether they match
				// the collation we are using here for processing
				col1 = rsComparator.duplicate(rs1, on1);
				col2 = rsComparator.duplicate(rs2, on2);
				int dResult1 = pCol1 != null ? rsComparator.compare(pCol1, col1) : -1;
				int dResult2 = pCol2 != null ? rsComparator.compare(pCol2, col2) : -1;

				if (dResult1 == 0) {
					log.error("Duplicate entries found.");
					rsComparator.error(log, col1);
					throw new DuplicateJoinColumnsException(String.format(duplicateJoinColumnsError, joinNode.getSqlQueries().get(0).getDataSource(), joinNode.getTarget()));
				}
				if (dResult2 == 0) {
					log.error("Duplicate entries found.");
					rsComparator.error(log, col2);
					throw new DuplicateJoinColumnsException(String.format(duplicateJoinColumnsError, joinNode.getSqlQueries().get(1).getDataSource(), joinNode.getTarget()));
				}
				if (dResult1 > 0 && joinNode.getCollation() != Attitude.IGNORE) {
					log.error("Wrong collation found.");
					rsComparator.error(log, pCol1);
					rsComparator.error(log, col1);
					CollationException e = new CollationException(String.format(collationError, joinNode.getSqlQueries().get(0).getDataSource(), joinNode.getTarget()));
					switch (joinNode.getCollation()) {
					case DUMP:
						e.printStackTrace();
						break;
					case FAIL:
						throw e;
					}
				}
				if (dResult2 > 0 && joinNode.getCollation() != Attitude.IGNORE) {
					log.error("Wrong collation found.");
					rsComparator.error(log, pCol2);
					rsComparator.error(log, col2);
					CollationException e = new CollationException(String.format(collationError, joinNode.getSqlQueries().get(1).getDataSource(), joinNode.getTarget()));
					switch (joinNode.getCollation()) {
					case DUMP:
						e.printStackTrace();
						break;
					case FAIL:
						throw e;
					}
				}
				
				int cResult = rsComparator.compare(rs1, on1, rs2, on2);
				switch (cResult) {
				case -1:
					if (joinType == JoinNode.JoinType.LEFT || joinType == JoinNode.JoinType.FULL) {
						result.addRow(joinNode.getResult());
						joinLayout.dumpRow(JoinResultType.LEFT);	
					}
					hasNext1 = rs1.next();
					pCol1 = col1;
					break;
				case 0:
					// We will break if we are supposed to produce only differences
					// and there are no differences present.
					int[] cmpArray = rsComparator.compareOthers(rs1, on1, excl1, rs2, on2, excl2);
					
					if (!joinNode.getDiffs() || !MiscUtils.allZeros(cmpArray)) {
						result.addRow(joinNode.getResult());
						joinLayout.dumpRow(cmpArray, JoinResultType.INNER);
					}
					hasNext1 = rs1.next();
					hasNext2 = rs2.next();
					pCol1 = col1;
					pCol2 = col2;
					break;
				case 1:
					if (joinType == JoinNode.JoinType.RIGHT || joinType == JoinNode.JoinType.FULL) {
						result.addRow(joinNode.getResult());
						joinLayout.dumpRow(JoinResultType.RIGHT);							
					}
					pCol2 = col2;
					hasNext2 = rs2.next();
					break;
				}				
			}
			if (joinType == JoinNode.JoinType.LEFT || joinType == JoinNode.JoinType.FULL) {
				while (hasNext1) {
					// Compare the subsequent rows in each result set and see whether they match
					// the collation we are using here for processing
					col1 = rsComparator.duplicate(rs1, on1);
					int dResult1 = pCol1 != null ? rsComparator.compare(pCol1, col1) : -1;

					if (dResult1 == 0) {
						log.error("Duplicate entries found.");
						rsComparator.error(log, col1);
						throw new DuplicateJoinColumnsException(String.format(duplicateJoinColumnsError, joinNode.getSqlQueries().get(0).getDataSource(), joinNode.getTarget()));
					}
					if (dResult1 > 0 && joinNode.getCollation() != Attitude.IGNORE) {
						log.error("Wrong collation found.");
						rsComparator.error(log, pCol1);
						rsComparator.error(log, col1);
						CollationException e = new CollationException(String.format(collationError, joinNode.getSqlQueries().get(0).getDataSource(), joinNode.getTarget()));
						switch (joinNode.getCollation()) {
						case DUMP:
							e.printStackTrace();
							break;
						case FAIL:
							throw e;
						}
					}
					pCol1 = col1;

					result.addRow(joinNode.getResult());
					joinLayout.dumpRow(JoinResultType.LEFT);
					hasNext1 = rs1.next();
				}
			}
			if (joinType == JoinNode.JoinType.RIGHT || joinType == JoinNode.JoinType.FULL) {
				while (hasNext2) {
					// Compare the subsequent rows in each result set and see whether they match
					// the collation we are using here for processing
					col2 = rsComparator.duplicate(rs2, on2);
					int dResult2 = pCol2 != null ? rsComparator.compare(pCol2, col2) : -1;

					if (dResult2 == 0) {
						log.error("Duplicate entries found.");
						rsComparator.error(log, col2);
						throw new DuplicateJoinColumnsException(String.format(duplicateJoinColumnsError, joinNode.getSqlQueries().get(1).getDataSource(), joinNode.getTarget()));
					}
					if (dResult2 > 0 && joinNode.getCollation() != Attitude.IGNORE) {
						log.error("Wrong collation found.");
						rsComparator.error(log, pCol2);
						rsComparator.error(log, col2);
						CollationException e = new CollationException(String.format(collationError, joinNode.getSqlQueries().get(1).getDataSource(), joinNode.getTarget()));
						switch (joinNode.getCollation()) {
						case DUMP:
							e.printStackTrace();
							break;
						case FAIL:
							throw e;
						}
					}
					pCol2 = col2;
					
					result.addRow(joinNode.getResult());
					joinLayout.dumpRow(JoinResultType.RIGHT);
					hasNext2 = rs2.next();
				}
			}
		} finally {
			Database.close(cnn1, stm1, cstm1, rs1);
			Database.close(cnn2, stm2, cstm2, rs2);
		}
		result.info(log, "JOIN");
		return result;
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
	public BuilderInterface getReportBuilder() {
		return reportBuilder;
	}

	/**
	 * @param reportBuilder the reportBuilder to set
	 */
	public void setReportBuilder(BuilderInterface reportBuilder) {
		this.reportBuilder = reportBuilder;
	}
}
