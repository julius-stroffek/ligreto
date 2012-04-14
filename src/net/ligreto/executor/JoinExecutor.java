package net.ligreto.executor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.Database;
import net.ligreto.ResultStatus;
import net.ligreto.builders.BuilderInterface;
import net.ligreto.builders.TargetInterface;
import net.ligreto.data.Field;
import net.ligreto.data.DataProvider;
import net.ligreto.data.ResultSetDataProvider;
import net.ligreto.data.SortingDataProvider;
import net.ligreto.data.SqlExecutionThread;
import net.ligreto.exceptions.CollationException;
import net.ligreto.exceptions.DuplicateKeyValuesException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.exceptions.UnimplementedMethodException;
import net.ligreto.executor.layouts.JoinLayout;
import net.ligreto.executor.layouts.JoinLayout.JoinResultType;
import net.ligreto.parser.nodes.JoinNode;
import net.ligreto.parser.nodes.JoinNode.DuplicatesStrategy;
import net.ligreto.parser.nodes.JoinNode.SortingStrategy;
import net.ligreto.parser.nodes.LayoutNode;
import net.ligreto.parser.nodes.SqlNode;
import net.ligreto.parser.nodes.Node.Attitude;
import net.ligreto.util.MiscUtils;
import net.ligreto.util.LigretoComparator;

/**
 * Provides execution code for join like comparison.
 * 
 * @author Julius Stroffek
 *
 */
public class JoinExecutor extends Executor implements JoinResultCallBack {

	/** The collation error message used in multiple places. */
	private static final String collationError = "The order of rows received from database does not match the used locale; set locale attribute in the <report> or <join> nodes; data source: %s; target: %s";

	/** The collation error message used in multiple places. */
	private static final String duplicateJoinColumnsError = "The rows received are duplicate in columns specified as 'on' columns; use 'on' columns that are unique; data source: %s; target: %s";
	
	/** The logger instance for the class. */
	private static Log log = LogFactory.getLog(JoinExecutor.class);

	/** Iterable object holding the join nodes to be processed. */
	protected Iterable<JoinNode> joinNodes;
	
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

	protected ResultStatus executeJoin(JoinNode joinNode) throws SQLException, LigretoException, ClassNotFoundException, IOException {
		ResultStatus result = new ResultStatus();
		List<SqlNode> sqlQueries = joinNode.getSqlQueries();
		
		if (sqlQueries.size() < 2)
			throw new LigretoException("There have to be two queries defined for a join.");
		
		if (sqlQueries.size() > 2)
			throw new UnimplementedMethodException("Join of more than 2 tables is not yet implemented");
		
		SqlExecutionThread exec1 = null;
		SqlExecutionThread exec2 = null;
		ResultSet rs1 = null, rs2 = null;
		try {
			StringBuilder qry1 = new StringBuilder(sqlQueries.get(0).getQuery().toString());
			StringBuilder qry2 = new StringBuilder(sqlQueries.get(1).getQuery().toString());			
			
			int key[] = joinNode.getKey();
			if (key == null)
				throw new LigretoException("The \"key\" attribute have to be present in <comparison> node.");
			
			// Do certain sanity checks here

			
			if (key.length > 0 && joinNode.getSortingStrategy() == SortingStrategy.EXTERNAL) {
				// Things are all right, so we will continue...
				qry1.append(" order by ");
				qry2.append(" order by ");
				for (int i = 0; i < key.length; i++) {
					qry1.append(key[i]);
					qry1.append(",");
					qry2.append(key[i]);
					qry2.append(",");
				}
				qry1.deleteCharAt(qry1.length() - 1);
				qry2.deleteCharAt(qry2.length() - 1);
			}
			
			exec1 = SqlExecutionThread.executeQuery(sqlQueries.get(0).getDataSource(), qry1.toString(), sqlQueries.get(0).getQueryType());
			exec2 = SqlExecutionThread.executeQuery(sqlQueries.get(1).getDataSource(), qry2.toString(), sqlQueries.get(1).getQueryType());

			try {
				exec1.join();
				exec2.join();
			} catch (InterruptedException e) {
				throw new LigretoException("Execution interrupted.", e);
			}

			exec1.throwExceptions();
			exec2.throwExceptions();
			rs1 = exec1.getResultSet();
			rs2 = exec2.getResultSet();			
			
			ResultSetMetaData rsmd1 = rs1.getMetaData();
			ResultSetMetaData rsmd2 = rs2.getMetaData();
			
			for (int i=0; i < key.length; i++) {
				if (key[i] > rsmd1.getColumnCount())
					throw new LigretoException("Index in \"key\" is out of the range for 1st query. It is \""
							+ key[i] + "\" and should be \"" + rsmd1.getColumnCount() + "\" the largest.");
				if (key[i] > rsmd2.getColumnCount())
					throw new LigretoException("Index in \"key\" is out of the range for 2nd query. It is \""
							+ key[i] + "\" and should be \"" + rsmd1.getColumnCount() + "\" the largest.");
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
						if (MiscUtils.arrayContains(key, excl1Tmp[i])) {
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
						if (MiscUtils.arrayContains(key, excl2Tmp[i])) {
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
			
			// The comparator instance
			LigretoComparator rsComparator = LigretoComparator.getInstance(joinNode.getLigretoNode().getLigretoParameters());
			rsComparator.setComparator(comparator);
			
			DataProvider dp1 = new ResultSetDataProvider(rs1, key, excl1);
			DataProvider dp2 = new ResultSetDataProvider(rs2, key, excl2);
			String dSrc1 =  joinNode.getSqlQueries().get(0).getDataSource();
			String dSrc2 =  joinNode.getSqlQueries().get(1).getDataSource();		
			dp1.setCaption(Database.getInstance().getDataSourceNode(dSrc1).getDescription());		
			dp2.setCaption(Database.getInstance().getDataSourceNode(dSrc2).getDescription());		
			
			if (dp1.getColumnCount() != dp2.getColumnCount()) {
				throw new LigretoException("Result set column counts differs: " + dp1.getColumnCount() + " and " + dp2.getColumnCount());
			}
			
			if (joinNode.getSortingStrategy() == SortingStrategy.INTERNAL) {
				SortingDataProvider sdp1 = new SortingDataProvider(dp1, key);
				SortingDataProvider sdp2 = new SortingDataProvider(dp2, key);
				sdp1.prepareData();
				sdp2.prepareData();
				dp1 = sdp1;
				dp2 = sdp2;
			}
			
			// Get the list of columns to compare
			int[] columns = joinNode.getColumns();
			
			if (columns == null) {
				columns = new int[dp1.getColumnCount() - key.length];
				for (int i=0, i1=1; i < columns.length; i++, i1++) {
					while (MiscUtils.arrayContains(key, i1))
						i1++;
					
					assert(i1 <= dp1.getColumnCount());

					columns[i] = i1;
				}
			}
			
			// Create the arrays to be used to highlight
			// differences for left, right, outer joins
			int[] lowerArray = new int[dp1.getColumnCount() - key.length];
			int[] higherArray = new int[lowerArray.length];
			for (int i=0; i < lowerArray.length; i++) {
				lowerArray[i] = 0;
				higherArray[i] = 0;
			}
			for (int i=0; i < columns.length; i++) {
				int index1 = columns[i];
				for (int j=0; j < key.length; j++) {
					if (key[j] < columns[i]) {
						index1--;
					}
				}
				lowerArray[index1-1] = -1;
				higherArray[index1-1] = 1;
			}
			for (int i=0; i < columns.length; i++) {
				if (MiscUtils.arrayContains(key, columns[i])) {
					throw new LigretoException("The key columns could not be listed in columns for comparison; columns: " + columns[i]);
				}
			}
			
			// Calculate the number of columns to compare
			int otherColumnCount = dp1.getColumnCount() - key.length;
			
			String firstTarget = null;
			
			// Build all the layouts
			List<JoinLayout> layouts = new ArrayList<JoinLayout>();
			for (LayoutNode layoutNode : joinNode.getLayouts()) {
				// Remember the first target for error messages only
				if (firstTarget != null) {
					firstTarget = layoutNode.getTarget();
				}
				
				/* Dump the information about target output. */
				if (layoutNode.isAppend()) {
					log.info("The output will be appended to target: \"" + layoutNode.getTarget() + "\"");
				} else {
					log.info("The output will be written to target: \"" + layoutNode.getTarget() + "\"");
				}
				
				/* Create the report target object first. */
				TargetInterface targetBuilder = reportBuilder.getTargetBuilder(layoutNode.getTarget(), layoutNode.isAppend());
				targetBuilder.setLigretoParameters(joinNode.getLigretoNode().getLigretoParameters());
				targetBuilder.setHighlight(layoutNode.getHighlight());
				targetBuilder.setHighlightColor(layoutNode.getHlColor());

				/* Create the proper implementation of the join layout. */
				JoinLayout joinLayout = JoinLayout.createInstance(layoutNode.getType(), targetBuilder, joinNode.getLigretoNode().getLigretoParameters());

				// Setup other parameters required for the join layout
				joinLayout.setJoinNode(joinNode);
				joinLayout.setLayoutNode(layoutNode);
				joinLayout.setGroupByColumns(layoutNode.getGroupBy());
				joinLayout.setResultStatus(result);
				joinLayout.setDataProviders(dp1, dp2);
				joinLayout.setComparedColumns(columns);
				joinLayout.start();
				
				// Dump the header row if requested
				if (layoutNode.getHeader()) {
					joinLayout.dumpHeader();
				}
				layouts.add(joinLayout);
			}
			
			boolean hasNext1 = dp1.next();
			boolean hasNext2 = dp2.next();
			Field[] pCol1 = null;
			Field[] pCol2 = null;
			Field[] col1 = null;
			Field[] col2 = null;
			while (hasNext1 && hasNext2) {
				
				// First process the duplicates
				while (hasNext1 && dp1.hasDuplicateKey()) {
					if (joinNode.getDuplicates() == DuplicatesStrategy.FAIL) {
						throw new DuplicateKeyValuesException(String.format(duplicateJoinColumnsError, dp1.getCaption(), firstTarget));
					}
					for (JoinLayout joinLayout : layouts) {
						joinLayout.dumpDuplicate(0);
					}
					hasNext1 = dp1.next();
				}
				while (hasNext2 && dp2.hasDuplicateKey()) {
					if (joinNode.getDuplicates() == DuplicatesStrategy.FAIL) {
						throw new DuplicateKeyValuesException(String.format(duplicateJoinColumnsError, dp1.getCaption(), firstTarget));
					}
					for (JoinLayout joinLayout : layouts) {
						joinLayout.dumpDuplicate(1);
					}
					hasNext2 = dp2.next();
				}

				// Exit the processing if the processed duplicates
				// were at the end of the data set
				if (!hasNext1 || !hasNext2) {
					break;
				}
				
				// Compare the subsequent rows in each result set and see whether they match
				// the collation we are using here for processing
				col1 = LigretoComparator.duplicate(dp1, dp1.getKeyIndices());
				col2 = LigretoComparator.duplicate(dp2, dp2.getKeyIndices());
				int dResult1 = pCol1 != null ? rsComparator.compareAsDataSource(pCol1, col1) : -1;
				int dResult2 = pCol2 != null ? rsComparator.compareAsDataSource(pCol2, col2) : -1;

				assert(dResult1 != 0);
				assert(dResult2 != 0);
				if (dResult1 > 0 && joinNode.getCollation() != Attitude.IGNORE) {
					log.error("Wrong collation found.");
					rsComparator.error(log, pCol1);
					rsComparator.error(log, col1);
					CollationException e = new CollationException(String.format(collationError, joinNode.getSqlQueries().get(0).getDataSource(), firstTarget));
					switch (joinNode.getCollation()) {
					case DUMP:
						log.error("Wrong collation found", e);
						break;
					case FAIL:
						throw e;
					}
				}
				if (dResult2 > 0 && joinNode.getCollation() != Attitude.IGNORE) {
					log.error("Wrong collation found.");
					rsComparator.error(log, pCol2);
					rsComparator.error(log, col2);
					CollationException e = new CollationException(String.format(collationError, joinNode.getSqlQueries().get(1).getDataSource(), firstTarget));
					switch (joinNode.getCollation()) {
					case DUMP:
						log.error("Wrong collation found", e);
						break;
					case FAIL:
						throw e;
					}
				}
				
				int cResult = rsComparator.compareAsDataSource(dp1, dp1.getKeyIndices(), dp2, dp2.getKeyIndices());
				switch (cResult) {
				case -1:
					for (JoinLayout joinLayout : layouts) {
						joinLayout.processRow(otherColumnCount, lowerArray, JoinResultType.LEFT);
					}
					hasNext1 = dp1.next();
					pCol1 = col1;
					break;
				case 0:
					// We will break if we are supposed to produce only differences
					// and there are no differences present.
					int[] cmpArray = rsComparator.compareOthersAsDataSource(dp1, dp1.getKeyIndices(), columns, dp2, dp2.getKeyIndices(), columns);
					
					int rowDiffs = MiscUtils.countNonZeros(cmpArray);
					for (JoinLayout joinLayout : layouts) {
						joinLayout.processRow(rowDiffs, cmpArray, JoinResultType.INNER);
					}
					
					hasNext1 = dp1.next();
					hasNext2 = dp2.next();
					pCol1 = col1;
					pCol2 = col2;
					break;
				case 1:
					for (JoinLayout joinLayout : layouts) {
						joinLayout.processRow(otherColumnCount, higherArray, JoinResultType.RIGHT);
					}						
					pCol2 = col2;
					hasNext2 = dp2.next();
					break;
				}
				boolean stillProcessing = false;
				for (JoinLayout joinLayout : layouts) {
					if (!joinLayout.isOverLimit()) {
						stillProcessing = true;
						break;
					}
				}
				if (!stillProcessing) {
					hasNext1 = false;
					hasNext2 = false;
					break;
				}
			}
			while (hasNext1) {
				// Compare the subsequent rows in each result set and see whether they match
				// the collation we are using here for processing
				col1 = LigretoComparator.duplicate(dp1, dp1.getKeyIndices());
				int dResult1 = pCol1 != null ? rsComparator.compareAsDataSource(pCol1, col1) : -1;
				assert(dResult1 != 0);

				if (dResult1 > 0 && joinNode.getCollation() != Attitude.IGNORE) {
					log.error("Wrong collation found.");
					rsComparator.error(log, pCol1);
					rsComparator.error(log, col1);
					CollationException e = new CollationException(String.format(collationError, joinNode.getSqlQueries().get(0).getDataSource(), firstTarget));
					switch (joinNode.getCollation()) {
					case DUMP:
						log.error("Wrong collation found", e);
						break;
					case FAIL:
						throw e;
					}
				}
				pCol1 = col1;

				for (JoinLayout joinLayout : layouts) {
					joinLayout.processRow(otherColumnCount, lowerArray, JoinResultType.LEFT);
				}
				hasNext1 = dp1.next();
				boolean stillProcessing = false;
				for (JoinLayout joinLayout : layouts) {
					if (!joinLayout.isOverLimit()) {
						stillProcessing = true;
						break;
					}
				}
				if (!stillProcessing) {
					hasNext1 = false;
					hasNext2 = false;
					break;
				}
			}

			while (hasNext2) {
				// Compare the subsequent rows in each result set and see whether they match
				// the collation we are using here for processing
				col2 = LigretoComparator.duplicate(dp2, dp2.getKeyIndices());
				int dResult2 = pCol2 != null ? rsComparator.compareAsDataSource(pCol2, col2) : -1;
				assert(dResult2 != 0);

				if (dResult2 > 0 && joinNode.getCollation() != Attitude.IGNORE) {
					log.error("Wrong collation found.");
					rsComparator.error(log, pCol2);
					rsComparator.error(log, col2);
					CollationException e = new CollationException(String.format(collationError, joinNode.getSqlQueries().get(1).getDataSource(), firstTarget));
					switch (joinNode.getCollation()) {
					case DUMP:
						log.error("Wrong collation found", e);
						break;
					case FAIL:
						throw e;
					}
				}
				pCol2 = col2;
				
				for (JoinLayout joinLayout : layouts) {
					joinLayout.processRow(otherColumnCount, higherArray, JoinResultType.RIGHT);
				}
				hasNext2 = dp2.next();
				boolean stillProcessing = false;
				for (JoinLayout joinLayout : layouts) {
					if (!joinLayout.isOverLimit()) {
						stillProcessing = true;
						break;
					}
				}
				if (!stillProcessing) {
					hasNext1 = false;
					hasNext2 = false;
					break;
				}
			}

			result = new ResultStatus();
			for (JoinLayout joinLayout : layouts) {
				result.merge(joinLayout.finish());
			}
		}
		finally {
			if (exec1 != null) {
				exec1.cleanup();
			}
			if (exec2 != null) {
				exec2.cleanup();
			}
		}
		result.info(log, "JOIN COMPARISON");
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
