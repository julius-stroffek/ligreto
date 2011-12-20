package net.ligreto.executor.layouts;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

import net.ligreto.Database;
import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface;
import net.ligreto.builders.BuilderInterface.CellFormat;
import net.ligreto.builders.BuilderInterface.HeaderType;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.util.MiscUtils;
import net.ligreto.util.JdbcUtils;

public class DetailedJoinLayout extends JoinLayout {

	public static final int OUTPUT_COLUMN_COUNT = 7;
	
	public DetailedJoinLayout(BuilderInterface reportBuilder, LigretoParameters ligretoParameters) {
		super(reportBuilder, ligretoParameters);
	}

	public void setColumnCount(int columnCount) {
		super.setColumnCount(columnCount);
		lowerArray = new int[OUTPUT_COLUMN_COUNT];
		higherArray = new int[OUTPUT_COLUMN_COUNT];
		for (int i=0; i < lowerArray.length; i++) {
			lowerArray[i] = -1;
			higherArray[i] = 1;
		}
	}

	@Override
	public void dumpHeader() throws SQLException, DataSourceNotDefinedException, IOException {
		reportBuilder.nextRow();
		reportBuilder.setHeaderColumn(0, "Column Name", HeaderType.TOP);
		reportBuilder.setColumnPosition(1, 1, null);
		reportBuilder.dumpJoinOnHeader(rs1, on1);
		reportBuilder.setColumnPosition(onLength + 1, 1, null);
		
		String dSrc0 =  joinNode.getSqlQueries().get(0).getDataSource();
		String dSrc1 =  joinNode.getSqlQueries().get(1).getDataSource();		
		reportBuilder.setHeaderColumn(0, Database.getInstance().getDataSourceNode(dSrc0).getDescription(), HeaderType.TOP);
		reportBuilder.setHeaderColumn(1, Database.getInstance().getDataSourceNode(dSrc1).getDescription(), HeaderType.TOP);
		
		reportBuilder.setHeaderColumn(2, "Difference", HeaderType.TOP);
		reportBuilder.setHeaderColumn(3, "Relative", HeaderType.TOP);
	}

	@Override
	public void dumpRow(int[] cmpArray, JoinResultType resultType) throws SQLException, LigretoException, IOException {
		int rs1Length = rs1.getMetaData().getColumnCount();
		int rs2Length = rs2.getMetaData().getColumnCount();
		
		for (int i=0, i1=1, i2=1; i1 <= rs1Length && i2 <= rs2Length; i++, i1++, i2++) {
			// Find the next column in the first result set that
			// is not part of 'on' nor 'exclude' column list
			boolean col1Found = false;
			while (i1 <= rs1Length) {
				if (MiscUtils.arrayContains(on1, i1) || MiscUtils.arrayContains(excl1, i1)) {
					i1++;
				} else {
					col1Found = true;
					break;
				}
			}
			// Find the next column in the second result set that
			// is not part of 'on' nor 'exclude' column list
			boolean col2Found = false;
			while (i2 <= rs1Length) {
				if (MiscUtils.arrayContains(on2, i2) || MiscUtils.arrayContains(excl2, i2)) {
					i2++;
				} else {
					col2Found = true;
					break;
				}
			}
			if (col1Found && col2Found) {
				switch (resultType) {
				case LEFT:
					reportBuilder.nextRow();
					reportBuilder.setHeaderColumn(0, rs1.getMetaData().getColumnName(i1), HeaderType.ROW);
					reportBuilder.setHighlightArray(higherArray);
					reportBuilder.setColumnPosition(1);
					reportBuilder.setJoinOnColumns(rs1, on1);
					reportBuilder.setColumnPosition(onLength + 1);
					reportBuilder.setColumn(0, rs1, i1);
					reportBuilder.setColumn(1, ligretoParameters.getMissingString(), CellFormat.UNCHANGED, true);
					if (JdbcUtils.getNumericObject(rs1, i1) != null) {
						reportBuilder.setColumn(2, rs1, i1);
						reportBuilder.setColumn(3, 1.00, CellFormat.PERCENTAGE_3_DECIMAL_DIGITS);
					} else {
						reportBuilder.setColumn(2, "yes", CellFormat.UNCHANGED);
					}
					break;
				case RIGHT:
					reportBuilder.nextRow();
					reportBuilder.setHeaderColumn(0, rs2.getMetaData().getColumnName(i2), HeaderType.ROW);
					reportBuilder.setHighlightArray(lowerArray);
					reportBuilder.setColumnPosition(1);
					reportBuilder.setJoinOnColumns(rs2, on2);
					reportBuilder.setColumnPosition(onLength + 1);
					reportBuilder.setColumn(0, ligretoParameters.getMissingString(), CellFormat.UNCHANGED, true);
					reportBuilder.setColumn(1, rs2, i2);
					if (JdbcUtils.getNumericObject(rs2, i2) != null) {
						reportBuilder.setColumn(2, rs2, i2);
						reportBuilder.setColumn(3, 1.00, CellFormat.PERCENTAGE_3_DECIMAL_DIGITS);
					} else {
						reportBuilder.setColumn(2, "yes", CellFormat.UNCHANGED);
					}
					break;
				case INNER:
					if (!joinNode.getDiffs() || cmpArray[i] != 0) {
						String colName = rs1.getMetaData().getColumnName(i1);
						String col2Name = rs1.getMetaData().getColumnName(i2);
						if (! colName.equalsIgnoreCase(col2Name)) {
							colName = colName + " / " + col2Name;
						}
						reportBuilder.nextRow();
						reportBuilder.setHeaderColumn(0, colName, HeaderType.ROW);
						reportBuilder.setColumnPosition(1);
						reportBuilder.setJoinOnColumns(rs1, on1);
						reportBuilder.setColumnPosition(onLength + 1);
						if (cmpArray[i] < 0) {
							reportBuilder.setHighlightArray(lowerArray);
						} else if (cmpArray[i] > 0) {
							reportBuilder.setHighlightArray(higherArray);
						}
						reportBuilder.setColumn(0, rs1, i1);
						reportBuilder.setColumn(1, rs2, i2);
						reportBuilder.setColumn(2, calculateDifference(i1, i2), CellFormat.UNCHANGED);
						reportBuilder.setColumn(3, calculateRelativeDifference(i1, i2), CellFormat.PERCENTAGE_3_DECIMAL_DIGITS);

					}
					break;
				default:
					throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
				}
			} else if (col1Found || col2Found) {
				throw new RuntimeException("Internal inconsistency found.");
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

	private Object calculateDifference(int i1, int i2) throws SQLException, LigretoException {
		Object columnValue1 = JdbcUtils.getNumericObject(rs1, i1);
		Object columnValue2 = JdbcUtils.getNumericObject(rs2, i2);
		
		if (columnValue1 instanceof String)
			columnValue1 = null;
		if (columnValue2 instanceof String)
			columnValue2 = null;
			
		// If one of the values is not number, report just 'yes'/'no'
		if (columnValue1 == null || columnValue2 == null) {
			String str1 = rs1.getString(i1);
			String str2 = rs2.getString(i2);
			if (str1 == null && str2 == null)
				return "no";
			else if (str1 != null)
				return str1.equals(str2) ? "no" : "yes";
			else
				return str2.equals(str1) ? "no" : "yes";
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

	private Object calculateRelativeDifference(int i1, int i2) throws SQLException {
		Object columnValue1 = JdbcUtils.getNumericObject(rs1, i1);
		Object columnValue2 = JdbcUtils.getNumericObject(rs2, i2);
		
		// If one of the values is not number, report just empty string
		if (columnValue1 == null || columnValue2 == null) {
			return "";
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
			resultStatus.addRelativeDifference(diff);
			return diff;
		} else {
			return "";
		}
	}
}
