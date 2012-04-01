package net.ligreto.executor;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.ResultStatus;
import net.ligreto.data.ColumnAggregationResult;
import net.ligreto.exceptions.DataException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.layouts.JoinLayout;
import net.ligreto.parser.nodes.LimitNode;
import net.ligreto.parser.nodes.ResultNode;
import net.ligreto.parser.nodes.RowLimitNode;
import net.ligreto.util.MiscUtils;

public class ResultExecutor extends Executor {

	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(ResultExecutor.class);

	protected ResultNode resultNode;
	protected JoinLayout joinLayout;
	protected ResultStatus resultStatus;
	
	public ResultExecutor(ResultNode resultNode, JoinLayout joinLayout) {
		this.resultNode = resultNode;
		this.joinLayout = joinLayout;
	}

	private String compareLimit(double value, Double limit) {
		String msg;
		if (value <= limit) {
			msg = "SUCCESS";
		} else {
			resultStatus.setAccepted(false);
			msg = "FAILURE";
		}
		return msg;
	}
	
	private String compareLimit(long value, Long limit) {
		String msg;
		if (value <= limit) {
			msg = "SUCCESS";
		} else {
			resultStatus.setAccepted(false);
			msg = "FAILURE";
		}
		return msg;
	}
	
	private void processRowLimitNode(RowLimitNode rowLimitNode) {
		Long totalRowCountLimit = rowLimitNode.getTotalRows();
		Long absoluteDifferenceLimit = rowLimitNode.getAbsoluteDifference();
		Double relativeDifferenceLimit = rowLimitNode.getRelativeDifference();
		Long absoluteNonMatchedLimit = rowLimitNode.getAbsoluteNonMatched();
		Double relativeNonMatchedLimit = rowLimitNode.getRelativeNonMatched();
		Long absoluteEqualLimit = rowLimitNode.getAbsoluteEqual();
		Double relativeEqualLimit = rowLimitNode.getRelativeEqual();
		Long absoluteMatchedLimit = rowLimitNode.getAbsoluteMatched();
		Double relativeMatchedLimit = rowLimitNode.getRelativeMatched();

		int differentRowCount = joinLayout.getDifferentRowCount();
		int totalRowCount = joinLayout.getTotalRowCount();
		int matchingRowCount = joinLayout.getMatchingRowCount();
		
		int absoluteDifference = differentRowCount;
		double relativeDifference = absoluteDifference / (double)totalRowCount;
		int absoluteEqual = totalRowCount - differentRowCount;
		double relativeEqual = absoluteEqual / (double)totalRowCount;
		int absoluteMatched = matchingRowCount;
		double relativeMatched = absoluteMatched / (double)totalRowCount;
		int absoluteNonMatched = totalRowCount - matchingRowCount;
		double relativeNonMatched = absoluteNonMatched / (double)totalRowCount;
		
		if (totalRowCountLimit != null) {
			String statusMsg = compareLimit(totalRowCount, totalRowCountLimit);
			String msg = String.format("%s: Total Row Count: %d; Limit: %d", statusMsg, totalRowCount, totalRowCountLimit);
			log.info(msg);
		} else {
			String msg = String.format("%s: Total Row Count: %d", "SKIPPED", totalRowCount);
			log.info(msg);
		}

		if (absoluteDifferenceLimit != null) {
			String statusMsg = compareLimit(absoluteDifference, absoluteDifferenceLimit);
			String msg = String.format("%s: Absolute Count of Different Rows: %d; Limit: %d", statusMsg, absoluteDifference, absoluteDifferenceLimit);
			log.info(msg);
		} else {
			String msg = String.format("%s: Absolute Count of Different Rows: %d", "SKIPPED", absoluteDifference);
			log.info(msg);
		}

		if (relativeDifferenceLimit != null) {
			String statusMsg = compareLimit(relativeDifference, relativeDifferenceLimit);
			String msg = String.format("%s: Relative Count of Different Rows: %f; Limit: %f", statusMsg, relativeDifference, relativeDifferenceLimit);
			log.info(msg);
		} else {
			String msg = String.format("%s: Relative Count of Different Rows: %f", "SKIPPED", relativeDifference);
			log.info(msg);
		}

		if (absoluteNonMatchedLimit != null) {
			String statusMsg = compareLimit(absoluteNonMatched, absoluteNonMatchedLimit);
			String msg = String.format("%s: Absolute Count of Non Matched Rows: %d; Limit: %d", statusMsg, absoluteNonMatched, absoluteNonMatchedLimit);
			log.info(msg);
		} else {
			String msg = String.format("%s: Absolute Count of Non Matched Rows: %d", "SKIPPED", absoluteNonMatched);
			log.info(msg);
		}
		
		if (relativeNonMatchedLimit != null) {
			String statusMsg = compareLimit(relativeNonMatched, relativeNonMatchedLimit);
			String msg = String.format("%s: Relative Count of Non Matched Rows: %f; Limit: %f", statusMsg, relativeNonMatched, relativeNonMatchedLimit);
			log.info(msg);
		} else {
			String msg = String.format("%s: Relative Count of Non Matched Rows: %f", "SKIPPED", relativeNonMatched);
			log.info(msg);
		}

		if (absoluteEqualLimit != null) {
			String statusMsg = compareLimit(absoluteEqual, absoluteEqualLimit);
			String msg = String.format("%s: Absolute Count of Equal Rows: %d; Limit: %d", statusMsg, absoluteEqual, absoluteEqualLimit);
			log.info(msg);
		} else {
			String msg = String.format("%s: Absolute Count of Equal Rows: %d", "SKIPPED", absoluteEqual);
			log.info(msg);
		}
		
		if (relativeEqualLimit != null) {
			String statusMsg = compareLimit(relativeEqual, relativeEqualLimit);
			String msg = String.format("%s: Relative Count of Equal Rows: %f; Limit: %f", statusMsg, relativeEqual, relativeEqualLimit);
			log.info(msg);
		} else {
			String msg = String.format("%s: Relative Count of Equal Rows: %f", "SKIPPED", relativeEqual);
			log.info(msg);
		}

		if (absoluteMatchedLimit != null) {
			String statusMsg = compareLimit(absoluteMatched, absoluteMatchedLimit);
			String msg = String.format("%s: Absolute Count of Matched Rows: %d; Limit: %d", statusMsg, absoluteMatched, absoluteMatchedLimit);
			log.info(msg);
		} else {
			String msg = String.format("%s: Absolute Count of Matched Rows: %d", "SKIPPED", absoluteMatched);
			log.info(msg);
		}
		
		if (relativeMatchedLimit != null) {
			String statusMsg = compareLimit(relativeMatched, relativeMatchedLimit);
			String msg = String.format("%s: Relative Count of Matched Rows: %f; Limit: %f", statusMsg, relativeMatched, relativeMatchedLimit);
			log.info(msg);
		} else {
			String msg = String.format("%s: Relative Count of Matched Rows: %f", "SKIPPED", relativeMatched);
			log.info(msg);
		}
	}
	
	private void processLimitNode(LimitNode limitNode, int columnIndex) throws LigretoException {
		Double absoluteCountLimit = limitNode.getAbsoluteCount();
		Double relativeCountLimit = limitNode.getRelativeCount();
		Double absoluteDifferenceLimit = limitNode.getAbsoluteDifference();
		Double relativeDifferenceLimit = limitNode.getRelativeDifference();
		int resultColumn = joinLayout.translateToResultColumn(columnIndex);
		if (resultColumn >= 0) {
			if (!MiscUtils.arrayContains(joinLayout.getIgnoredColumns(), columnIndex)) {
				log.warn("Column used in result is not included in the compared columns: " + columnIndex);
				log.warn("We still use this column for result determination.");
			}
			ColumnAggregationResult columnResult = joinLayout.getColumnAggregationResult(resultColumn);

			log.info("Checking \"" + joinLayout.getResultColumnName(resultColumn) + "\" column result:");
			
			if (absoluteCountLimit != null) {
				String statusMsg = compareLimit(columnResult.getDifferenceCount(), absoluteCountLimit);
				String msg = String.format("%s: Absolute Difference Count: %d; Limit: %d", statusMsg, columnResult.getDifferenceCount(), absoluteCountLimit.intValue());
				log.info(msg);
			} else {
				String msg = String.format("%s: Absolute Difference Count: %d", "SKIPPED", columnResult.getDifferenceCount());
				log.info(msg);
			}

			if (relativeCountLimit != null) {
				String statusMsg = compareLimit(columnResult.getDifferenceRatio(), relativeCountLimit);
				String msg = String.format("%s: Relative Difference Count: %f; Limit: %f", statusMsg, columnResult.getDifferenceRatio(), relativeCountLimit);
				log.info(msg);
			} else {
				String msg = String.format("%s: Relative Difference Count: %f", "SKIPPED", columnResult.getDifferenceRatio());
				log.info(msg);
			}

			if (absoluteDifferenceLimit != null) {
				String statusMsg = compareLimit(columnResult.getDifference(), absoluteDifferenceLimit);
				String msg = String.format("%s: Absolute Difference: %f; Limit: %f", statusMsg, columnResult.getDifference(), absoluteDifferenceLimit);
				log.info(msg);
			} else {
				String msg = String.format("%s: Absolute Difference: %f", "SKIPPED", columnResult.getDifference());
				log.info(msg);
			}

			if (relativeDifferenceLimit != null) {
				String statusMsg = compareLimit(columnResult.getRelativeDifference(), relativeDifferenceLimit);
				String msg = String.format("%s: Relative Difference: %f; Limit: %f", statusMsg, columnResult.getRelativeDifference(), relativeDifferenceLimit);
				log.info(msg);
			} else {
				String msg = String.format("%s: Relative Difference: %f", "SKIPPED", columnResult.getRelativeDifference());
				log.info(msg);
			}
		}
	}
	
	private void processLimitNode(LimitNode limitNode) throws LigretoException {
		if (limitNode.getColumns() != null) {
			for (int col : limitNode.getColumns()) {
				processLimitNode(limitNode, col);
			}
		} else {
			for (int col = 1; col <= joinLayout.getColumnCount(); col++) {
				processLimitNode(limitNode, col);
			}
		}
	}

	@Override
	public ResultStatus execute() throws LigretoException {
		resultStatus = new ResultStatus();
		resultStatus.setDifferentRowCount(joinLayout.getDifferentRowCount());
		resultStatus.setTotalRowCount(joinLayout.getTotalRowCount());
		resultStatus.setDuplicateKeyCount(joinLayout.getKeyDuplicatesSrc1() + joinLayout.getKeyDuplicatesSrc2());
		try {
			if (resultNode != null && resultNode.isEnabled()) {
				RowLimitNode rowLimitNode = resultNode.getRowLimitNode();
				if (rowLimitNode != null) {
					processRowLimitNode(rowLimitNode);
				}
				List<LimitNode> limitNodes = resultNode.getLimitNodes();
				if (limitNodes != null) {
					for (LimitNode limitNode : limitNodes) {
						processLimitNode(limitNode);
					}
				}
			} else if (joinLayout.getLayoutNode().getResult() && resultStatus.getDifferentRowCount() > 0) {
					resultStatus.setAccepted(false);
			}
		} catch (DataException e) {
			throw new LigretoException("Error checking the result of comparison.", e);
		}
		return resultStatus;
	}
}
