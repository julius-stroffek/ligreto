package net.ligreto.executor.layouts;

import java.io.IOException;

import org.junit.Assert;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.builders.TargetInterface;
import net.ligreto.data.DataProvider;
import net.ligreto.exceptions.DataException;

public class DuplicatesJoinLayout extends JoinLayout {

	public DuplicatesJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws DataException, IOException {
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, "Data Source", OutputStyle.TOP_HEADER);
		targetBuilder.shiftPosition(1);
		for (int i=0; i < keyColumns.length; i++) {
			targetBuilder.dumpCell(i, getColumnName(keyColumns[i]), OutputStyle.TOP_HEADER);
		}
		for (int i=0; i < resultColumns.length; i++) {
			targetBuilder.dumpCell(i, getColumnName(resultColumns[i]), OutputStyle.TOP_HEADER);
		}
	}

	@Override
	public void dumpRow(int rowDiffs, int[] cmpArray, JoinResultType resultType) throws DataException, IOException {
	}
	
	@Override
	public void dumpDuplicate(int dataSourceIndex) throws DataException, IOException {
		DataProvider dp = null;
		switch (dataSourceIndex) {
		case 0:
			dp = dp1;
			break;
		case 1:
			dp = dp2;
			break;
		default:
			Assert.assertTrue(false);
			break;
		}
		targetBuilder.nextRow();
		targetBuilder.dumpCell(0, dp.getCaption(), OutputFormat.DEFAULT);
		targetBuilder.shiftPosition(1);
		for (int i = 0; i < keyColumns.length; i++) {
			targetBuilder.dumpCell(i, dp.getObject(keyColumns[i]), OutputStyle.DEFAULT);
		}
		targetBuilder.shiftPosition(keyColumns.length);							
		
		for (int i = 0; i < resultColumns.length; i++) {
			targetBuilder.dumpCell(i, dp.getObject(resultColumns[i]), OutputStyle.DEFAULT);
		}	
	}
}
