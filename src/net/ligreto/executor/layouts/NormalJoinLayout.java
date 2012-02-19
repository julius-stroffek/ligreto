package net.ligreto.executor.layouts;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.CellFormat;
import net.ligreto.builders.BuilderInterface.HeaderType;
import net.ligreto.builders.TargetInterface;
import net.ligreto.exceptions.DataException;

public class NormalJoinLayout extends JoinLayout {

	public NormalJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws DataException, IOException {
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "# of Diffs", HeaderType.TOP);
		targetBuilder.setColumnPosition(1, 1, null);
		targetBuilder.dumpJoinOnHeader(dp1, on1, null);
		targetBuilder.setColumnPosition(onLength + 1, 1, null);
		targetBuilder.dumpOtherHeader(dp1, on1, null, dataSourceDesc1);
		targetBuilder.setColumnPosition(rsColCount + 1, 1, null);
		targetBuilder.dumpOtherHeader(dp2, on2, null, dataSourceDesc2);		
	}

	@Override
	public void dumpRow(int rowDiffs, int[] cmpArray, JoinResultType resultType) throws DataException, IOException {
		targetBuilder.nextRow();
		switch (resultType) {
		case LEFT:
			targetBuilder.setHighlightArray(higherArray);
			targetBuilder.dumpColumn(0, rowDiffs, CellFormat.UNCHANGED, rowDiffs > 0);
			targetBuilder.setColumnPosition(1, 1, null);
			targetBuilder.dumpJoinOnColumns(dp1, on1);
			targetBuilder.setColumnPosition(onLength + 1, 1, lowerArray);							
			targetBuilder.dumpOtherColumns(dp1, on1, null);
			targetBuilder.setColumnPosition(rsColCount + 1, 1, higherArray);
			for (int i=0; i < rsColCount - onLength; i++) {
				targetBuilder.dumpColumn(
					i, ligretoParameters.getMissingString(),
					CellFormat.UNCHANGED, true
				);
			}
			break;
		case RIGHT:
			targetBuilder.setHighlightArray(lowerArray);
			targetBuilder.dumpColumn(0, rowDiffs, CellFormat.UNCHANGED, rowDiffs > 0);
			targetBuilder.setColumnPosition(1, 1, null);
			targetBuilder.dumpJoinOnColumns(dp2, on2);
			targetBuilder.setColumnPosition(onLength + 1, 1, higherArray);							
			for (int i=0; i < rsColCount - onLength; i++) {
				targetBuilder.dumpColumn(
					i, ligretoParameters.getMissingString(),
					CellFormat.UNCHANGED, true
				);
			}
			targetBuilder.setColumnPosition(rsColCount + 1, 1, higherArray);							
			targetBuilder.dumpOtherColumns(dp2, on2, null);
			break;
		case INNER:
			targetBuilder.dumpColumn(0, rowDiffs, CellFormat.UNCHANGED, rowDiffs > 0);
			targetBuilder.setColumnPosition(1, 1, null);
			targetBuilder.dumpJoinOnColumns(dp1, on1);
			targetBuilder.setColumnPosition(onLength + 1, 1, cmpArray);							
			targetBuilder.dumpOtherColumns(dp1, on1, null);			
			targetBuilder.setColumnPosition(rsColCount + 1, 1, cmpArray);
			targetBuilder.dumpOtherColumns(dp2, on2, null);
			break;
		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}
	}

}
