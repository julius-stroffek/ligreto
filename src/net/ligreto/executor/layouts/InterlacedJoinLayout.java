package net.ligreto.executor.layouts;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.builders.TargetInterface;
import net.ligreto.exceptions.DataException;
import net.ligreto.exceptions.LigretoException;

public class InterlacedJoinLayout extends JoinLayout {

	public InterlacedJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws IOException, LigretoException {
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "# of Diffs", OutputStyle.TOP_HEADER);
		targetBuilder.shiftPosition(1, 1);
		
		for (int i = 0; i < dp1.getKeyIndices().length; i++) {
			targetBuilder.dumpCell(i, getKeyColumnName(i), OutputStyle.TOP_HEADER);
		}
		targetBuilder.shiftPosition(dp1.getKeyIndices().length, 2);
		
		for (int i = 0; i < comparedColumns.length; i++) {
			targetBuilder.dumpCell(i, dp1.getColumnName(comparedColumns[i]) + " (" + dp1.getCaption() + ")", OutputStyle.TOP_HEADER);
		}
		targetBuilder.shiftPosition(1, 2);

		for (int i = 0; i < comparedColumns.length; i++) {
			targetBuilder.dumpCell(i, dp2.getColumnName(comparedColumns[i]) + " (" + dp2.getCaption() + ")", OutputStyle.TOP_HEADER);
		}
		targetBuilder.shiftPosition(2*comparedColumns.length - 1, 2);

		for (int i = 0; i < ignoredColumns.length; i++) {
			targetBuilder.dumpCell(i, dp1.getColumnName(ignoredColumns[i]) + " (" + dp1.getCaption() + ")", OutputStyle.TOP_HEADER_DISABLED);
		}
		targetBuilder.shiftPosition(1, 2);

		for (int i = 0; i < ignoredColumns.length; i++) {
			targetBuilder.dumpCell(i, dp2.getColumnName(ignoredColumns[i]) + " (" + dp2.getCaption() + ")", OutputStyle.TOP_HEADER_DISABLED);
		}
	}

	@Override
	public void dumpRow(int rowDiffs, boolean[] cmpArray, JoinResultType resultType) throws DataException, LigretoException, IOException {
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, rowDiffs, OutputFormat.DEFAULT, rowDiffs > 0 ? OutputStyle.HIGHLIGHTED : OutputStyle.DEFAULT);
		targetBuilder.shiftPosition(1);
		int[] keyIndices;
		switch (resultType) {
		case LEFT:
			keyIndices = dp1.getKeyIndices();
			for (int i = 0; i < keyIndices.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(keyIndices[i]), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(keyIndices.length, 2);
			
			for (int i = 0; i < comparedColumns.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(comparedColumns[i]), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(1, 2);

			for (int i = 0; i < comparedColumns.length; i++) {
				targetBuilder.dumpCell(i, ligretoParameters.getMissingString(), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(2*comparedColumns.length - 1, 2);
			
			for (int i = 0; i < ignoredColumns.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(ignoredColumns[i]), OutputStyle.DISABLED);
			}
			targetBuilder.shiftPosition(1, 2);

			for (int i = 0; i < ignoredColumns.length; i++) {
				targetBuilder.dumpCell(i, ligretoParameters.getMissingString(), OutputStyle.DISABLED);
			}
			break;
			
		case RIGHT:
			keyIndices = dp2.getKeyIndices();
			for (int i = 0; i < keyIndices.length; i++) {
				targetBuilder.dumpCell(i, dp2.getObject(keyIndices[i]), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(keyIndices.length, 2);
			
			for (int i = 0; i < comparedColumns.length; i++) {
				targetBuilder.dumpCell(i, ligretoParameters.getMissingString(), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(1, 2);

			for (int i = 0; i < comparedColumns.length; i++) {
				targetBuilder.dumpCell(i, dp2.getObject(comparedColumns[i]), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(2*comparedColumns.length - 1, 2);
			
			for (int i = 0; i < ignoredColumns.length; i++) {
				targetBuilder.dumpCell(i, ligretoParameters.getMissingString(), OutputStyle.DISABLED);
			}
			targetBuilder.shiftPosition(1, 2);

			for (int i = 0; i < ignoredColumns.length; i++) {
				targetBuilder.dumpCell(i, dp2.getObject(ignoredColumns[i]), OutputStyle.DISABLED);
			}
			break;
			
		case INNER:
			keyIndices = dp1.getKeyIndices();
			for (int i = 0; i < keyIndices.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(keyIndices[i]), OutputStyle.DEFAULT);
			}
			targetBuilder.shiftPosition(keyIndices.length, 2);
			
			for (int i = 0; i < comparedColumns.length; i++) {
				OutputStyle style = cmpArray[i] ? OutputStyle.DEFAULT : OutputStyle.HIGHLIGHTED;
				targetBuilder.dumpCell(i, dp1.getObject(comparedColumns[i]), style);
			}
			targetBuilder.shiftPosition(1, 2);

			for (int i = 0; i < comparedColumns.length; i++) {
				OutputStyle style = cmpArray[i] ? OutputStyle.DEFAULT : OutputStyle.HIGHLIGHTED;
				targetBuilder.dumpCell(i, dp2.getObject(comparedColumns[i]), style);
			}
			targetBuilder.shiftPosition(2*comparedColumns.length - 1, 2);
			
			for (int i = 0; i < ignoredColumns.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(ignoredColumns[i]), OutputStyle.DISABLED);
			}
			targetBuilder.shiftPosition(1, 2);

			for (int i = 0; i < ignoredColumns.length; i++) {
				targetBuilder.dumpCell(i, dp2.getObject(ignoredColumns[i]), OutputStyle.DISABLED);
			}
			break;
			
		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}
	}
}
