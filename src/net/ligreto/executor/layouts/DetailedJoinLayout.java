package net.ligreto.executor.layouts;

import java.sql.SQLException;

import net.ligreto.builders.ReportBuilder;
import net.ligreto.builders.ReportBuilder.HeaderType;
import net.ligreto.util.MiscUtils;

public class DetailedJoinLayout extends JoinLayout {

	public DetailedJoinLayout(ReportBuilder reportBuilder) {
		super(reportBuilder);
	}

	@Override
	public void dumpHeader() throws SQLException {
		reportBuilder.nextRow();
		reportBuilder.setHeaderColumn(0, "Column Name", HeaderType.TOP);
		reportBuilder.setColumnPosition(1, 1, null);
		reportBuilder.dumpJoinOnHeader(rs1, on1);
		reportBuilder.setColumnPosition(onLength + 1, 1, null);
		reportBuilder.setHeaderColumn(0, joinNode.getSqlQueries().get(0).getDataSource(), HeaderType.TOP);
		reportBuilder.setHeaderColumn(1, joinNode.getSqlQueries().get(1).getDataSource(), HeaderType.TOP);
	}

	@Override
	public void dumpRow(int[] cmpArray, JoinResultType resultType) throws SQLException {
		int rs1Length = rs1.getMetaData().getColumnCount();
		int rs2Length = rs2.getMetaData().getColumnCount();
		
		for (int i=0, i1=0, i2=0; i1 < rs1Length && i2 < rs2Length; i++, i1++, i2++) {
			// Find the next column in the first result set that
			// is not part of 'on' nor 'exclude' column list
			boolean col1Found = false;
			while (i1 < rs1Length) {
				if (MiscUtils.arrayContains(on1, i1+1) || MiscUtils.arrayContains(excl1, i1 + 1)) {
					i1++;
				} else {
					col1Found = true;
					break;
				}
			}
			// Find the next column in the second result set that
			// is not part of 'on' nor 'exclude' column list
			boolean col2Found = false;
			while (i2 < rs1Length) {
				if (MiscUtils.arrayContains(on2, i2+1) || MiscUtils.arrayContains(excl2, i2 + 1)) {
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
					reportBuilder.setHeaderColumn(0, rs1.getMetaData().getColumnName(i1 + 1), HeaderType.ROW);
					reportBuilder.setHighlightArray(higherArray);
					reportBuilder.setColumnPosition(1);
					reportBuilder.setJoinOnColumns(rs1, on1);
					reportBuilder.setColumnPosition(onLength + 1);
					reportBuilder.setColumn(0, rs1, i1 + 1);
					break;
				case RIGHT:
					reportBuilder.nextRow();
					reportBuilder.setHeaderColumn(0, rs2.getMetaData().getColumnName(i2 + 1), HeaderType.ROW);
					reportBuilder.setHighlightArray(lowerArray);
					reportBuilder.setColumnPosition(1);
					reportBuilder.setJoinOnColumns(rs2, on2);
					reportBuilder.setColumnPosition(onLength + 1);
					reportBuilder.setColumn(1, rs2, i2 + 1);
					break;
				case INNER:
					if (!joinNode.getDiffs() || cmpArray[i] != 0) {
						String colName = rs1.getMetaData().getColumnName(i1 + 1);
						String col2Name = rs1.getMetaData().getColumnName(i2 + 1);
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
						reportBuilder.setColumn(0, rs1, i1 + 1);
						reportBuilder.setColumn(1, rs2, i2 + 1);
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
}
