package net.ligreto.executor.layouts;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface;
import net.ligreto.builders.BuilderInterface.CellFormat;

public class InterlacedJoinLayout extends JoinLayout {

	public InterlacedJoinLayout(BuilderInterface reportBuilder, LigretoParameters ligretoParameters) {
		super(reportBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws SQLException, IOException {
		reportBuilder.nextRow();
		reportBuilder.dumpJoinOnHeader(rs1, on1);
		reportBuilder.setColumnPosition(onLength, 2, null);
		reportBuilder.dumpOtherHeader(rs1, on1, excl1);
		reportBuilder.setColumnPosition(onLength+1, 2, null);
		reportBuilder.dumpOtherHeader(rs2, on2, excl2);				
	}

	@Override
	public void dumpRow(int[] cmpArray, JoinResultType resultType) throws SQLException, IOException {
		reportBuilder.nextRow();
		switch (resultType) {
		case LEFT:
			reportBuilder.setHighlightArray(higherArray);
			reportBuilder.setJoinOnColumns(rs1, on1);
			reportBuilder.setColumnPosition(onLength, 2, lowerArray);
			reportBuilder.setOtherColumns(rs1, on1, excl1);
			reportBuilder.setColumnPosition(onLength+1, 2, higherArray);
			for (int i=0; i < rsColCount - onLength; i++) {
				reportBuilder.setColumn(
					2*i, ligretoParameters.getMissingString(),
					CellFormat.UNCHANGED, true
				);
			}
			break;
		case RIGHT:
			reportBuilder.setHighlightArray(lowerArray);
			reportBuilder.setJoinOnColumns(rs2, on2);
			reportBuilder.setColumnPosition(onLength, 2, higherArray);							
			for (int i=0; i < rsColCount - onLength; i++) {
				reportBuilder.setColumn(
					2*i, ligretoParameters.getMissingString(),
					CellFormat.UNCHANGED, true
				);
			}
			reportBuilder.setColumnPosition(onLength+1, 2, higherArray);
			reportBuilder.setOtherColumns(rs2, on2, excl2);
			break;
		case INNER:
			reportBuilder.setJoinOnColumns(rs1, on1);
			reportBuilder.setColumnPosition(onLength, 2, cmpArray);
			reportBuilder.setOtherColumns(rs1, on1, excl1);
			reportBuilder.setColumnPosition(onLength+1, 2, cmpArray);
			reportBuilder.setOtherColumns(rs2, on2, excl2);
			break;
		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}
	}

}
