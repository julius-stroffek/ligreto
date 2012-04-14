package net.ligreto.executor.layouts;

import java.io.IOException;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.builders.TargetInterface;
import net.ligreto.data.DataProvider;
import net.ligreto.exceptions.DataException;
import net.ligreto.exceptions.LigretoException;

public class DuplicatesJoinLayout extends JoinLayout {

	public DuplicatesJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws IOException, LigretoException {
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "Data Source", OutputStyle.TOP_HEADER);
		targetBuilder.shiftPosition(1);
		for (int i=0; i < dp1.getKeyIndices().length; i++) {
			targetBuilder.dumpCell(i, getKeyColumnName(i), OutputStyle.TOP_HEADER);
		}
		targetBuilder.shiftPosition(dp1.getKeyIndices().length);
		
		for (int i=0; i < comparedColumns.length; i++) {
			targetBuilder.dumpCell(i, getColumnName(comparedColumns[i]), OutputStyle.TOP_HEADER);
		}
		targetBuilder.shiftPosition(comparedColumns.length);
		
		for (int i=0; i < ignoredColumns.length; i++) {
			targetBuilder.dumpCell(i, getColumnName(ignoredColumns[i]), OutputStyle.TOP_HEADER_DISABLED);
		}
	}

	@Override
	public void dumpRow(int rowDiffs, int[] cmpArray, JoinResultType resultType) throws DataException, IOException {
	}
	
	@Override
	public void dumpDuplicate(int dataSourceIndex) throws IOException, LigretoException {
		// Call the parent function first
		super.dumpDuplicate(dataSourceIndex);
		
		DataProvider dp = null;
		switch (dataSourceIndex) {
		case 0:
			dp = dp1;
			break;
		case 1:
			dp = dp2;
			break;
		default:
			throw new RuntimeException("Unexpectidly reaching unreachable code.");
		}
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, dp.getCaption(), OutputFormat.DEFAULT);
		targetBuilder.shiftPosition(1);
		
		int[] keyIndices = dp.getKeyIndices();
		for (int i = 0; i < keyIndices.length; i++) {
			targetBuilder.dumpCell(i, dp.getObject(keyIndices[i]), OutputStyle.DEFAULT);
		}
		targetBuilder.shiftPosition(keyIndices.length);							
		
		for (int i = 0; i < comparedColumns.length; i++) {
			targetBuilder.dumpCell(i, dp.getObject(comparedColumns[i]), OutputStyle.DEFAULT);
		}	
		targetBuilder.shiftPosition(comparedColumns.length);							
		
		for (int i = 0; i < ignoredColumns.length; i++) {
			targetBuilder.dumpCell(i, dp.getObject(ignoredColumns[i]), OutputStyle.DISABLED);
		}	
	}
}
