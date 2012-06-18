package net.ligreto.executor.layouts;

import java.io.IOException;
import java.math.BigDecimal;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.builders.TargetInterface;
import net.ligreto.exceptions.DataException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.util.DataProviderUtils;

public class DetailedJoinLayout extends JoinLayout {

	public static final int OUTPUT_COLUMN_COUNT = 7;
	
	public DetailedJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws IOException, LigretoException {
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "Column Name", OutputStyle.TOP_HEADER);
		targetBuilder.shiftPosition(1);
		
		for (int i = 0; i < dp1.getKeyIndices().length; i++) {
			targetBuilder.dumpCell(i, getKeyColumnName(i), OutputStyle.TOP_HEADER);
		}
		targetBuilder.shiftPosition(dp1.getKeyIndices().length);
		
		targetBuilder.dumpCell(0, dp1.getCaption(), OutputStyle.TOP_HEADER);
		targetBuilder.dumpCell(1, dp2.getCaption(), OutputStyle.TOP_HEADER);
		
		targetBuilder.dumpCell(2, "Difference", OutputStyle.TOP_HEADER);
		targetBuilder.dumpCell(3, "Relative", OutputStyle.TOP_HEADER);
	}

	protected void dumpLeft(int index, OutputStyle style) throws IOException, LigretoException {
		if (style != OutputStyle.DISABLED) {
			style = OutputStyle.HIGHLIGHTED;
		}
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, getColumnName(index), style == OutputStyle.DISABLED ? OutputStyle.ROW_HEADER_DISABLED : OutputStyle.ROW_HEADER);
		targetBuilder.shiftPosition(1);

		int[] keyIndices = dp1.getKeyIndices();
		for (int j = 0; j < keyIndices.length; j++) {
			targetBuilder.dumpCell(j, dp1.getObject(keyIndices[j]), style);
		}
		targetBuilder.shiftPosition(keyIndices.length);
		
		targetBuilder.dumpCell(0, dp1.getObject(index), style);
		targetBuilder.dumpCell(1, ligretoParameters.getMissingString(), style);
		
		if (DataProviderUtils.getNumericObject(dp1, index) != null) {
			targetBuilder.dumpCell(2, dp1.getObject(index), style);
			targetBuilder.dumpCell(3, 1.00, OutputFormat.PERCENTAGE_3_DECIMAL_DIGITS, style);
		} else {
			targetBuilder.dumpCell(2, "yes", OutputFormat.DEFAULT, style);
		}	
	}
	
	protected void dumpRight(int index, OutputStyle style) throws IOException, LigretoException {
		if (style != OutputStyle.DISABLED) {
			style = OutputStyle.HIGHLIGHTED;
		}
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, getColumnName(index), style == OutputStyle.DISABLED ? OutputStyle.ROW_HEADER_DISABLED : OutputStyle.ROW_HEADER);
		targetBuilder.shiftPosition(1);

		int[] keyIndices = dp2.getKeyIndices();
		for (int j = 0; j < keyIndices.length; j++) {
			targetBuilder.dumpCell(j, dp2.getObject(keyIndices[j]), style);
		}
		targetBuilder.shiftPosition(keyIndices.length);
		
		targetBuilder.dumpCell(0, ligretoParameters.getMissingString(), style);
		targetBuilder.dumpCell(1, dp2.getObject(index), style);
		
		if (DataProviderUtils.getNumericObject(dp2, index) != null) {
			targetBuilder.dumpCell(2, dp2.getObject(index), style);
			targetBuilder.dumpCell(3, 1.00, OutputFormat.PERCENTAGE_3_DECIMAL_DIGITS, style);
		} else {
			targetBuilder.dumpCell(2, "yes", OutputFormat.DEFAULT, style);
		}		
	}
	
	protected void dumpInner(int index, OutputStyle style) throws IOException, LigretoException {
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, getColumnName(index), style == OutputStyle.DISABLED ? OutputStyle.ROW_HEADER_DISABLED
				: OutputStyle.ROW_HEADER);
		targetBuilder.shiftPosition(1);

		OutputStyle keyStyle = style == OutputStyle.DISABLED ? OutputStyle.DISABLED : OutputStyle.DEFAULT;
		int[] keyIndices = dp1.getKeyIndices();
		for (int j = 0; j < keyIndices.length; j++) {
			targetBuilder.dumpCell(j, dp1.getObject(keyIndices[j]), keyStyle);
		}
		targetBuilder.shiftPosition(keyIndices.length);

		targetBuilder.dumpCell(0, dp1.getObject(index), style);
		targetBuilder.dumpCell(1, dp2.getObject(index), style);

		targetBuilder.dumpCell(2, calculateDifference(index, index), style);
		targetBuilder.dumpCell(3, calculateRelativeDifference(index, index), OutputFormat.PERCENTAGE_3_DECIMAL_DIGITS, style);
	}
	
	@Override
	public void dumpRow(int rowDiffs, boolean[] cmpArray, JoinResultType resultType) throws LigretoException, IOException {		
		// Loop through all the compared columns
		for (int i = 0; i < comparedColumns.length; i++) {
			switch (resultType) {
			case LEFT:
				dumpLeft(comparedColumns[i], OutputStyle.HIGHLIGHTED);
				break;
				
			case RIGHT:
				dumpRight(comparedColumns[i], OutputStyle.HIGHLIGHTED);
				break;

			case INNER:
				if (!layoutNode.getDiffs() || !cmpArray[i]) {
					dumpInner(comparedColumns[i], cmpArray[i] ? OutputStyle.DEFAULT : OutputStyle.HIGHLIGHTED);
				}
				break;
				
			default:
				throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
			}
		}
		// Loop through all the ignored columns
		for (int i = 0; i < ignoredColumns.length; i++) {
			switch (resultType) {
			case LEFT:
				dumpLeft(ignoredColumns[i], OutputStyle.DISABLED);
				break;
				
			case RIGHT:
				dumpRight(ignoredColumns[i], OutputStyle.DISABLED);
				break;

			case INNER:
				dumpInner(ignoredColumns[i], OutputStyle.DISABLED);
				break;
				
			default:
				throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
			}
		}
	}

	private double calculateDifference(long value1, long value2) {
		return Math.abs(value1 - value2);
	}

	private double calculateDifference(double value1, double value2) {
		return Math.abs(value1 - value2);
	}

	private double calculateDifference(BigDecimal value1, BigDecimal value2) {
		return value1.subtract(value2).abs().doubleValue();
	}

	private double calculateRelativeDifference(long value1, long value2) {
		double divisor = (double)Math.abs(value1);
		if (divisor != 0)
			return Math.abs(value1 - value2)/divisor;
		else
			return 0;
	}

	private double calculateRelativeDifference(double value1, double value2) {
		double divisor = Math.abs(value1);
		if (divisor != 0)
			return Math.abs(value1 - value2)/divisor;
		else
			return 0;
	}

	private double calculateRelativeDifference(BigDecimal value1, BigDecimal value2) {
		try {
			return	value1.subtract(value2).abs().divide(
						value1.abs(),
						20,	BigDecimal.ROUND_HALF_UP
					).doubleValue();
		} catch (ArithmeticException e) {
			return 0;
		}
	}

	private Object calculateDifference(int i1, int i2) throws LigretoException {
		Object columnValue1 = DataProviderUtils.getNumericObject(dp1, i1);
		Object columnValue2 = DataProviderUtils.getNumericObject(dp2, i2);
		
		if (columnValue1 instanceof String)
			columnValue1 = null;
		if (columnValue2 instanceof String)
			columnValue2 = null;
			
		// If one of the values is not number, report just 'yes'/'no'
		if (columnValue1 == null || columnValue2 == null) {
			String str1 = dp1.getString(i1);
			String str2 = dp2.getString(i2);
			if (str1 == null && str2 == null)
				return "no";
			else if (str1 != null)
				return str1.equals(str2) ? "no" : "yes";
			else
				return str2.equals(str1) ? "no" : "yes";
		}
		
		if (columnValue1 instanceof Integer) {
			columnValue1 = new Long((Integer)columnValue1);
		}
		if (columnValue2 instanceof Integer) {
			columnValue2 = new Long((Integer)columnValue2);
		}
		
		if (columnValue1 instanceof Long && columnValue2 instanceof Long) {
			double diff = calculateDifference((Long)columnValue1, (Long)columnValue2);
			return diff != 0 ? new Double(diff) : "no";
		} else if (columnValue1 instanceof Double && columnValue2 instanceof Double) {
			double diff = calculateDifference((Double)columnValue1, (Double)columnValue2);
			return diff != 0 ? new Double(diff) : "no";
		} else if (columnValue1 instanceof BigDecimal && columnValue2 instanceof BigDecimal) {
			double diff = calculateDifference((BigDecimal)columnValue1, (BigDecimal)columnValue2);
			return diff != 0 ? new Double(diff) : "no";
		} else if (columnValue1 instanceof BigDecimal) {
			double diff;
			if (columnValue2 instanceof Long) {
				diff = calculateDifference((BigDecimal)columnValue1, new BigDecimal((Long)columnValue2));
			} else {
				diff = calculateDifference((BigDecimal)columnValue1, new BigDecimal((Double)columnValue2));			
			}
			return diff != 0 ? new Double(diff) : "no";
		} else if (columnValue2 instanceof BigDecimal) {
			double diff;
			if (columnValue1 instanceof Long) {
				diff = calculateDifference(new BigDecimal((Long)columnValue1), (BigDecimal)columnValue2);
			} else {
				diff = calculateDifference(new BigDecimal((Double)columnValue1), (BigDecimal)columnValue2);
			}
			return diff != 0 ? new Double(diff) : "no";
		} else if (columnValue1 instanceof Double) {
			double diff = calculateDifference((Double)columnValue1, ((Long)columnValue2).doubleValue());
			return diff != 0 ? new Double(diff) : "no";
		} else if (columnValue2 instanceof Double) {
			double diff = calculateDifference(((Long)columnValue1).doubleValue(), (Double)columnValue2);
			return diff != 0 ? new Double(diff) : "no";
		} else {
			throw new RuntimeException("Executing unreachable code.");
		}
	}

	private Object calculateRelativeDifference(int i1, int i2) throws DataException {
		Object columnValue1 = DataProviderUtils.getNumericObject(dp1, i1);
		Object columnValue2 = DataProviderUtils.getNumericObject(dp2, i2);
		
		// If one of the values is not number, report just empty string
		if (columnValue1 == null || columnValue2 == null) {
			return "";
		}
		
		if (columnValue1 instanceof Integer) {
			columnValue1 = new Long((Integer)columnValue1);
		}
		if (columnValue2 instanceof Integer) {
			columnValue2 = new Long((Integer)columnValue2);
		}
		
		double diff;
		if (columnValue1 instanceof Long && columnValue2 instanceof Long) {
			diff = calculateRelativeDifference((Long)columnValue1, (Long)columnValue2);
		} else if (columnValue1 instanceof Double && columnValue2 instanceof Double) {
			diff = calculateRelativeDifference((Double)columnValue1, (Double)columnValue2);
		} else if (columnValue1 instanceof BigDecimal && columnValue2 instanceof BigDecimal) {
			diff = calculateRelativeDifference((BigDecimal)columnValue1, (BigDecimal)columnValue2);
		} else if (columnValue1 instanceof BigDecimal) {
			if (columnValue2 instanceof Long) {
				diff = calculateRelativeDifference((BigDecimal)columnValue1, new BigDecimal((Long)columnValue2));
			} else {
				diff = calculateRelativeDifference((BigDecimal)columnValue1, new BigDecimal((Double)columnValue2));			
			}
		} else if (columnValue2 instanceof BigDecimal) {
			if (columnValue1 instanceof Long) {
				diff = calculateRelativeDifference(new BigDecimal((Long)columnValue1), (BigDecimal)columnValue2);
			} else {
				diff = calculateRelativeDifference(new BigDecimal((Double)columnValue1), (BigDecimal)columnValue2);
			}
		} else if (columnValue1 instanceof Double) {
			diff = calculateRelativeDifference((Double)columnValue1, ((Long)columnValue2).doubleValue());
		} else if (columnValue2 instanceof Double) {
			diff = calculateRelativeDifference(((Long)columnValue1).doubleValue(), (Double)columnValue2);
		} else {
			throw new RuntimeException("Executing unreachable code.");
		}
		
		// Provide number only for non-zero values
		if (diff != 0) {
			return diff;
		} else {
			return "";
		}
	}
}
