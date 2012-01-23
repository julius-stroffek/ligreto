package net.ligreto.executor.layouts;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface;
import net.ligreto.builders.BuilderInterface.CellFormat;
import net.ligreto.builders.BuilderInterface.HeaderType;

public class InterlacedJoinLayout extends JoinLayout {

	public InterlacedJoinLayout(BuilderInterface reportBuilder, LigretoParameters ligretoParameters) {
		super(reportBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws SQLException, IOException {
		reportBuilder.nextRow();
		reportBuilder.dumpHeaderColumn(0, "# of Diffs", HeaderType.TOP);
		reportBuilder.setColumnPosition(1, 1, null);
		reportBuilder.dumpJoinOnHeader(rs1, on1, null);
		reportBuilder.setColumnPosition(onLength + 1, 2, null);
		reportBuilder.dumpOtherHeader(rs1, on1, excl1, dataSourceDesc1);
		reportBuilder.setColumnPosition(onLength + 2, 2, null);
		reportBuilder.dumpOtherHeader(rs2, on2, excl2, dataSourceDesc2);				
	}

	@Override
	public void dumpRow(int rowDiffs, int[] cmpArray, JoinResultType resultType) throws SQLException, IOException {
		reportBuilder.nextRow();
		switch (resultType) {
		case LEFT:
			reportBuilder.setHighlightArray(higherArray);
			reportBuilder.dumpColumn(0, rowDiffs, CellFormat.UNCHANGED, rowDiffs > 0);
			reportBuilder.setColumnPosition(1, 1, null);
			reportBuilder.dumpJoinOnColumns(rs1, on1);
			reportBuilder.setColumnPosition(onLength + 1, 2, lowerArray);
			reportBuilder.dumpOtherColumns(rs1, on1, excl1);
			reportBuilder.setColumnPosition(onLength + 2, 2, higherArray);
			for (int i=0; i < rsColCount - onLength; i++) {
				reportBuilder.dumpColumn(
					2*i, ligretoParameters.getMissingString(),
					CellFormat.UNCHANGED, true
				);
			}
			break;
		case RIGHT:
			reportBuilder.setHighlightArray(lowerArray);
			reportBuilder.dumpColumn(0, rowDiffs, CellFormat.UNCHANGED, rowDiffs > 0);
			reportBuilder.setColumnPosition(1, 1, null);
			reportBuilder.dumpJoinOnColumns(rs2, on2);
			reportBuilder.setColumnPosition(onLength + 1, 2, higherArray);							
			for (int i=0; i < rsColCount - onLength; i++) {
				reportBuilder.dumpColumn(
					2*i, ligretoParameters.getMissingString(),
					CellFormat.UNCHANGED, true
				);
			}
			reportBuilder.setColumnPosition(onLength + 2, 2, higherArray);
			reportBuilder.dumpOtherColumns(rs2, on2, excl2);
			break;
		case INNER:
			reportBuilder.dumpColumn(0, rowDiffs, CellFormat.UNCHANGED, rowDiffs > 0);
			reportBuilder.setColumnPosition(1, 1, null);
			reportBuilder.dumpJoinOnColumns(rs1, on1);
			reportBuilder.setColumnPosition(onLength + 1, 2, cmpArray);
			reportBuilder.dumpOtherColumns(rs1, on1, excl1);
			reportBuilder.setColumnPosition(onLength + 2, 2, cmpArray);
			reportBuilder.dumpOtherColumns(rs2, on2, excl2);
			break;
		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}
	}

}
