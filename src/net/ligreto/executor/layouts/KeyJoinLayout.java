package net.ligreto.executor.layouts;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.builders.TargetInterface;
import net.ligreto.exceptions.DataException;

public class KeyJoinLayout extends JoinLayout {

	public KeyJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws DataException, IOException {
		targetBuilder.nextRow();
		
		for (int i = 0; i < keyColumns.length; i++) {
			targetBuilder.dumpCell(i, dp1.getColumnName(keyColumns[i]) + " (" + dp1.getCaption() + ")", OutputStyle.TOP_HEADER);
		}
		targetBuilder.shiftPosition(keyColumns.length);

		for (int i = 0; i < keyColumns.length; i++) {
			targetBuilder.dumpCell(i, dp2.getColumnName(keyColumns[i]) + " (" + dp2.getCaption() + ")", OutputStyle.TOP_HEADER);
		}
	}

	@Override
	public void dumpRow(int rowDiffs, int[] cmpArray, JoinResultType resultType) throws DataException, IOException {		
		targetBuilder.nextRow();
		switch (resultType) {
		case LEFT:
			for (int i = 0; i < keyColumns.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(keyColumns[i]), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(keyColumns.length);

			for (int i = 0; i < keyColumns.length; i++) {
				targetBuilder.dumpCell(i, ligretoParameters.getMissingString(), OutputStyle.HIGHLIGHTED);
			}
			break;
			
		case RIGHT:
			for (int i = 0; i < keyColumns.length; i++) {
				targetBuilder.dumpCell(i, ligretoParameters.getMissingString(), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(keyColumns.length);

			for (int i = 0; i < keyColumns.length; i++) {
				targetBuilder.dumpCell(i, dp2.getObject(keyColumns[i]), OutputStyle.HIGHLIGHTED);
			}
			break;
			
		case INNER:
			for (int i = 0; i < keyColumns.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(keyColumns[i]), OutputStyle.DEFAULT);
			}
			targetBuilder.shiftPosition(keyColumns.length);

			for (int i = 0; i < keyColumns.length; i++) {
				targetBuilder.dumpCell(i, dp2.getObject(keyColumns[i]), OutputStyle.DEFAULT);
			}
			break;

		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}
	}

}
