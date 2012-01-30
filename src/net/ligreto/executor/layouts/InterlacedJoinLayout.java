package net.ligreto.executor.layouts;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.CellFormat;
import net.ligreto.builders.BuilderInterface.HeaderType;
import net.ligreto.builders.TargetInterface;

public class InterlacedJoinLayout extends JoinLayout {

	public InterlacedJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws SQLException, IOException {
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "# of Diffs", HeaderType.TOP);
		targetBuilder.setColumnPosition(1, 1, null);
		targetBuilder.dumpJoinOnHeader(rs1, on1, null);
		targetBuilder.setColumnPosition(onLength + 1, 2, null);
		targetBuilder.dumpOtherHeader(rs1, on1, excl1, dataSourceDesc1);
		targetBuilder.setColumnPosition(onLength + 2, 2, null);
		targetBuilder.dumpOtherHeader(rs2, on2, excl2, dataSourceDesc2);				
	}

	@Override
	public void dumpRow(int rowDiffs, int[] cmpArray, JoinResultType resultType) throws SQLException, IOException {
		targetBuilder.nextRow();
		switch (resultType) {
		case LEFT:
			targetBuilder.setHighlightArray(higherArray);
			targetBuilder.dumpColumn(0, rowDiffs, CellFormat.UNCHANGED, rowDiffs > 0);
			targetBuilder.setColumnPosition(1, 1, null);
			targetBuilder.dumpJoinOnColumns(rs1, on1);
			targetBuilder.setColumnPosition(onLength + 1, 2, lowerArray);
			targetBuilder.dumpOtherColumns(rs1, on1, excl1);
			targetBuilder.setColumnPosition(onLength + 2, 2, higherArray);
			for (int i=0; i < rsColCount - onLength; i++) {
				targetBuilder.dumpColumn(
					2*i, ligretoParameters.getMissingString(),
					CellFormat.UNCHANGED, true
				);
			}
			break;
		case RIGHT:
			targetBuilder.setHighlightArray(lowerArray);
			targetBuilder.dumpColumn(0, rowDiffs, CellFormat.UNCHANGED, rowDiffs > 0);
			targetBuilder.setColumnPosition(1, 1, null);
			targetBuilder.dumpJoinOnColumns(rs2, on2);
			targetBuilder.setColumnPosition(onLength + 1, 2, higherArray);							
			for (int i=0; i < rsColCount - onLength; i++) {
				targetBuilder.dumpColumn(
					2*i, ligretoParameters.getMissingString(),
					CellFormat.UNCHANGED, true
				);
			}
			targetBuilder.setColumnPosition(onLength + 2, 2, higherArray);
			targetBuilder.dumpOtherColumns(rs2, on2, excl2);
			break;
		case INNER:
			targetBuilder.dumpColumn(0, rowDiffs, CellFormat.UNCHANGED, rowDiffs > 0);
			targetBuilder.setColumnPosition(1, 1, null);
			targetBuilder.dumpJoinOnColumns(rs1, on1);
			targetBuilder.setColumnPosition(onLength + 1, 2, cmpArray);
			targetBuilder.dumpOtherColumns(rs1, on1, excl1);
			targetBuilder.setColumnPosition(onLength + 2, 2, cmpArray);
			targetBuilder.dumpOtherColumns(rs2, on2, excl2);
			break;
		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}
	}

}
