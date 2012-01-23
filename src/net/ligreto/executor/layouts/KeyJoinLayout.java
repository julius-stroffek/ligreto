package net.ligreto.executor.layouts;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface;
import net.ligreto.builders.BuilderInterface.CellFormat;

public class KeyJoinLayout extends JoinLayout {

	public KeyJoinLayout(BuilderInterface reportBuilder, LigretoParameters ligretoParameters) {
		super(reportBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws SQLException, IOException {
		reportBuilder.nextRow();
		reportBuilder.dumpJoinOnHeader(rs1, on1, dataSourceDesc1);
		reportBuilder.setColumnPosition(onLength, 1, null);
		reportBuilder.dumpJoinOnHeader(rs2, on2, dataSourceDesc2);
	}

	@Override
	public void dumpRow(int rowDiffs, int[] cmpArray, JoinResultType resultType) throws SQLException, IOException {		
		reportBuilder.nextRow();
		switch (resultType) {
		case LEFT:
			reportBuilder.setHighlightArray(higherArray);
			reportBuilder.dumpJoinOnColumns(rs1, on1);
			reportBuilder.setColumnPosition(onLength, 1, null);
			for (int i=0; i < onLength; i++) {
				reportBuilder.dumpColumn(
					2*i, ligretoParameters.getMissingString(),
					CellFormat.UNCHANGED, true
				);
			}
			break;
		case RIGHT:
			for (int i=0; i < onLength; i++) {
				reportBuilder.dumpColumn(
					2*i, ligretoParameters.getMissingString(),
					CellFormat.UNCHANGED, true
				);
			}
			reportBuilder.setColumnPosition(onLength, 1, higherArray);
			reportBuilder.dumpJoinOnColumns(rs2, on2);
			break;
		case INNER:
			reportBuilder.dumpJoinOnColumns(rs1, on1);
			reportBuilder.setColumnPosition(onLength, 1, null);
			reportBuilder.dumpJoinOnColumns(rs2, on2);
			break;
		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}
	}

}
