package net.ligreto.executor.layouts;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.builders.TargetInterface;
import net.ligreto.exceptions.LigretoException;

public class NormalJoinLayout extends JoinLayout {

	public NormalJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws IOException, LigretoException {
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "# of Diffs", OutputStyle.TOP_HEADER);
		targetBuilder.shiftPosition(1, 1);
		
		for (int i = 0; i < keyColumns.length; i++) {
			targetBuilder.dumpCell(i, getOriginalColumnName(keyColumns[i]), OutputStyle.TOP_HEADER);
		}
		targetBuilder.shiftPosition(keyColumns.length);
		
		for (int i = 0; i < comparedColumns.length; i++) {
			targetBuilder.dumpCell(i, dp1.getColumnName(comparedColumns[i]) + " (" + dp1.getCaption() + ")", OutputStyle.TOP_HEADER);
		}
		targetBuilder.shiftPosition(comparedColumns.length);

		for (int i = 0; i < comparedColumns.length; i++) {
			targetBuilder.dumpCell(i, dp2.getColumnName(comparedColumns[i]) + " (" + dp2.getCaption() + ")", OutputStyle.TOP_HEADER);
		}
		targetBuilder.shiftPosition(comparedColumns.length);
		
		for (int i = 0; i < ignoredColumns.length; i++) {
			targetBuilder.dumpCell(i, dp1.getColumnName(ignoredColumns[i]) + " (" + dp1.getCaption() + ")", OutputStyle.TOP_HEADER_DISABLED);
		}
		targetBuilder.shiftPosition(ignoredColumns.length);

		for (int i = 0; i < ignoredColumns.length; i++) {
			targetBuilder.dumpCell(i, dp2.getColumnName(ignoredColumns[i]) + " (" + dp2.getCaption() + ")", OutputStyle.TOP_HEADER_DISABLED);
		}
	}

	@Override
	public void dumpRow(int rowDiffs, int[] cmpArray, JoinResultType resultType) throws IOException, LigretoException {
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, rowDiffs, OutputFormat.DEFAULT, rowDiffs > 0 ? OutputStyle.HIGHLIGHTED : OutputStyle.DEFAULT);
		targetBuilder.shiftPosition(1);
		switch (resultType) {
		case LEFT:
			for (int i = 0; i < keyColumns.length; i++) {
				targetBuilder.dumpCell(i, dp1.getOriginalObject(keyColumns[i]), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(keyColumns.length);
			
			for (int i = 0; i < comparedColumns.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(comparedColumns[i]), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(comparedColumns.length);

			for (int i = 0; i < comparedColumns.length; i++) {
				targetBuilder.dumpCell(i, ligretoParameters.getMissingString(), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(comparedColumns.length);

			for (int i = 0; i < ignoredColumns.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(ignoredColumns[i]), OutputStyle.DISABLED);
			}
			targetBuilder.shiftPosition(ignoredColumns.length);

			for (int i = 0; i < ignoredColumns.length; i++) {
				targetBuilder.dumpCell(i, ligretoParameters.getMissingString(), OutputStyle.DISABLED);
			}
			break;
			
		case RIGHT:
			for (int i = 0; i < keyColumns.length; i++) {
				targetBuilder.dumpCell(i, dp2.getObject(keyColumns[i]), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(keyColumns.length);
			
			for (int i = 0; i < comparedColumns.length; i++) {
				targetBuilder.dumpCell(i, ligretoParameters.getMissingString(), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(comparedColumns.length);

			for (int i = 0; i < comparedColumns.length; i++) {
				targetBuilder.dumpCell(i, dp2.getObject(comparedColumns[i]), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(comparedColumns.length);

			for (int i = 0; i < ignoredColumns.length; i++) {
				targetBuilder.dumpCell(i, ligretoParameters.getMissingString(), OutputStyle.DISABLED);
			}
			targetBuilder.shiftPosition(ignoredColumns.length);

			for (int i = 0; i < ignoredColumns.length; i++) {
				targetBuilder.dumpCell(i, dp2.getObject(ignoredColumns[i]), OutputStyle.DISABLED);
			}
			break;
			
		case INNER:
			for (int i = 0; i < keyColumns.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(keyColumns[i]), OutputStyle.DEFAULT);
			}
			targetBuilder.shiftPosition(keyColumns.length);
			
			for (int i = 0; i < comparedColumns.length; i++) {
				OutputStyle style = cmpArray[i] != 0 ? OutputStyle.HIGHLIGHTED : OutputStyle.DEFAULT;
				targetBuilder.dumpCell(i, dp1.getObject(comparedColumns[i]), style);
			}
			targetBuilder.shiftPosition(comparedColumns.length);

			for (int i = 0; i < comparedColumns.length; i++) {
				OutputStyle style = cmpArray[i] != 0 ? OutputStyle.HIGHLIGHTED : OutputStyle.DEFAULT;
				targetBuilder.dumpCell(i, dp2.getObject(comparedColumns[i]), style);
			}
			targetBuilder.shiftPosition(comparedColumns.length);

			for (int i = 0; i < ignoredColumns.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(ignoredColumns[i]), OutputStyle.DISABLED);
			}
			targetBuilder.shiftPosition(ignoredColumns.length);

			for (int i = 0; i < ignoredColumns.length; i++) {
				targetBuilder.dumpCell(i, dp2.getObject(ignoredColumns[i]), OutputStyle.DISABLED);
			}
			break;
			
		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}
	}

}
