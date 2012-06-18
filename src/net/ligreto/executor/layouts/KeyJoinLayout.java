package net.ligreto.executor.layouts;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.builders.TargetInterface;
import net.ligreto.exceptions.LigretoException;

public class KeyJoinLayout extends JoinLayout {

	public KeyJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws IOException, LigretoException {
		targetBuilder.nextRow();
		
		int[] keyIndices = dp1.getKeyIndices();
		for (int i = 0; i < keyIndices.length; i++) {
			targetBuilder.dumpCell(i, dp1.getColumnName(keyIndices[i]) + " (" + dp1.getCaption() + ")", OutputStyle.TOP_HEADER);
		}
		targetBuilder.shiftPosition(keyIndices.length);

		keyIndices = dp2.getKeyIndices();
		for (int i = 0; i < keyIndices.length; i++) {
			targetBuilder.dumpCell(i, dp2.getColumnName(keyIndices[i]) + " (" + dp2.getCaption() + ")", OutputStyle.TOP_HEADER);
		}
	}

	@Override
	public void dumpRow(int rowDiffs, boolean[] cmpArray, JoinResultType resultType) throws IOException, LigretoException {		
		targetBuilder.nextRow();
		int[] keyIndices;
		switch (resultType) {
		case LEFT:
			keyIndices = dp1.getKeyIndices();
			for (int i = 0; i < keyIndices.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(keyIndices[i]), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(keyIndices.length);

			keyIndices = dp2.getKeyIndices();
			for (int i = 0; i < keyIndices.length; i++) {
				targetBuilder.dumpCell(i, ligretoParameters.getMissingString(), OutputStyle.HIGHLIGHTED);
			}
			break;
			
		case RIGHT:
			keyIndices = dp1.getKeyIndices();
			for (int i = 0; i < keyIndices.length; i++) {
				targetBuilder.dumpCell(i, ligretoParameters.getMissingString(), OutputStyle.HIGHLIGHTED);
			}
			targetBuilder.shiftPosition(keyIndices.length);

			keyIndices = dp2.getKeyIndices();
			for (int i = 0; i < keyIndices.length; i++) {
				targetBuilder.dumpCell(i, dp2.getObject(keyIndices[i]), OutputStyle.HIGHLIGHTED);
			}
			break;
			
		case INNER:
			keyIndices = dp1.getKeyIndices();
			for (int i = 0; i < keyIndices.length; i++) {
				targetBuilder.dumpCell(i, dp1.getObject(keyIndices[i]), OutputStyle.DEFAULT);
			}
			targetBuilder.shiftPosition(keyIndices.length);

			keyIndices = dp2.getKeyIndices();
			for (int i = 0; i < keyIndices.length; i++) {
				targetBuilder.dumpCell(i, dp2.getObject(keyIndices[i]), OutputStyle.DEFAULT);
			}
			break;

		default:
			throw new IllegalArgumentException("Unexpected value of JoinResultType enumeration");
		}
	}

}
