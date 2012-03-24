package net.ligreto.executor.layouts;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.builders.TargetInterface;
import net.ligreto.exceptions.DataException;

public class InterlacedJoinLayout extends JoinLayout {

	public InterlacedJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws DataException, IOException {
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "# of Diffs", OutputStyle.TOP_HEADER);
		targetBuilder.shiftPosition(1, 1);
		
		for (int i = 0; i < keyColumns.length; i++) {
			targetBuilder.dumpCell(i, getColumnName(keyColumns[i]), OutputStyle.TOP_HEADER);
		}
		targetBuilder.shiftPosition(keyColumns.length, 2);
		
		for (int i = 0; i < resultColumns.length; i++) {
			targetBuilder.dumpCell(i, dp1.getColumnName(resultColumns[i]), OutputStyle.TOP_HEADER);
		}
		targetBuilder.shiftPosition(1, 2);

		for (int i = 0; i < resultColumns.length; i++) {
			targetBuilder.dumpCell(i, dp2.getColumnName(resultColumns[i]), OutputStyle.TOP_HEADER);
		}
	}

	@Override
	public void dumpRow(int rowDiffs, int[] cmpArray, JoinResultType resultType) throws DataException, IOException {
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, rowDiffs, OutputFormat.DEFAULT, rowDiffs > 0 ? OutputStyle.HIGHLIGHTED : OutputStyle.DEFAULT);
		targetBuilder.shiftPosition(1);
		switch (resultType) {
		case LEFT:
			for (int i = 0; i < keyColumns.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(keyColumns[i]), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(keyColumns.length, 2);
			
			for (int i = 0; i < resultColumns.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(resultColumns[i]), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(1, 2);

			for (int i = 0; i < resultColumns.length; i++) {
				targetBuilder.dumpCell(i, ligretoParameters.getMissingString(), OutputStyle.HIGHLIGHTED);
			}
			break;
			
		case RIGHT:
			for (int i = 0; i < keyColumns.length; i++) {
				targetBuilder.dumpCell(i, dp2.getObject(keyColumns[i]), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(keyColumns.length, 2);
			
			for (int i = 0; i < resultColumns.length; i++) {
				targetBuilder.dumpCell(i, ligretoParameters.getMissingString(), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(1, 2);

			for (int i = 0; i < resultColumns.length; i++) {
				targetBuilder.dumpCell(i, dp2.getObject(resultColumns[i]), OutputStyle.HIGHLIGHTED);
			}
			break;
			
		case INNER:
			for (int i = 0; i < keyColumns.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(keyColumns[i]), OutputStyle.DEFAULT);
			}
			targetBuilder.shiftPosition(keyColumns.length, 2);
			
			for (int i = 0; i < resultColumns.length; i++) {
				OutputStyle style = cmpArray[resultColumns[i]] != 0 ? OutputStyle.HIGHLIGHTED : OutputStyle.DEFAULT;
				targetBuilder.dumpCell(i, dp1.getObject(resultColumns[i]), style);
			}
			targetBuilder.shiftPosition(1, 2);

			for (int i = 0; i < resultColumns.length; i++) {
				OutputStyle style = cmpArray[resultColumns[i]] != 0 ? OutputStyle.HIGHLIGHTED : OutputStyle.DEFAULT;
				targetBuilder.dumpCell(i, dp2.getObject(resultColumns[i]), style);
			}
			break;
			
		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}
	}
}
