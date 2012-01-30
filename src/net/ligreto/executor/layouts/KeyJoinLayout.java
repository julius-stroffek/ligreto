package net.ligreto.executor.layouts;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.CellFormat;
import net.ligreto.builders.TargetInterface;

public class KeyJoinLayout extends JoinLayout {

	public KeyJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws SQLException, IOException {
		targetBuilder.nextRow();
		targetBuilder.dumpJoinOnHeader(rs1, on1, dataSourceDesc1);
		targetBuilder.setColumnPosition(onLength, 1, null);
		targetBuilder.dumpJoinOnHeader(rs2, on2, dataSourceDesc2);
	}

	@Override
	public void dumpRow(int rowDiffs, int[] cmpArray, JoinResultType resultType) throws SQLException, IOException {		
		targetBuilder.nextRow();
		switch (resultType) {
		case LEFT:
			targetBuilder.setHighlightArray(higherArray);
			targetBuilder.dumpJoinOnColumns(rs1, on1);
			targetBuilder.setColumnPosition(onLength, 1, null);
			for (int i=0; i < onLength; i++) {
				targetBuilder.dumpColumn(
					2*i, ligretoParameters.getMissingString(),
					CellFormat.UNCHANGED, true
				);
			}
			break;
		case RIGHT:
			for (int i=0; i < onLength; i++) {
				targetBuilder.dumpColumn(
					2*i, ligretoParameters.getMissingString(),
					CellFormat.UNCHANGED, true
				);
			}
			targetBuilder.setColumnPosition(onLength, 1, higherArray);
			targetBuilder.dumpJoinOnColumns(rs2, on2);
			break;
		case INNER:
			targetBuilder.dumpJoinOnColumns(rs1, on1);
			targetBuilder.setColumnPosition(onLength, 1, null);
			targetBuilder.dumpJoinOnColumns(rs2, on2);
			break;
		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}
	}

}
