package net.ligreto.executor.layouts;

import java.io.IOException;

import org.junit.Assert;

import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface.OutputFormat;
import net.ligreto.builders.BuilderInterface.OutputStyle;
import net.ligreto.builders.TargetInterface;
import net.ligreto.data.DataProvider;
import net.ligreto.exceptions.DataException;
import net.ligreto.util.MiscUtils;

public class DuplicatesJoinLayout extends JoinLayout {

	public DuplicatesJoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		super(targetBuilder, ligretoParameters);
	}

	@Override
	public void dumpHeader() throws DataException, IOException {
		targetBuilder.nextRow();
		targetBuilder.dumpHeaderColumn(0, "Data Source", OutputStyle.TOP_HEADER);
		targetBuilder.setColumnPosition(1, 1, null);
		for (int i=0; i < onLength; i++) {
			String desc1 = dp1.getColumnLabel(on1[i]);
			String desc2 = dp2.getColumnLabel(on2[i]);
			if (desc1.toUpperCase().equals(desc2.toUpperCase())) {
				targetBuilder.dumpHeaderColumn(i, desc1, OutputStyle.TOP_HEADER);
			} else {
				targetBuilder.dumpHeaderColumn(i, desc1 + " / " + desc2, OutputStyle.TOP_HEADER);				
			}
		}
		for (int i=0; i < rsColCount; i++) {
			if (!MiscUtils.arrayContains(on1, i+1)) {
				String desc1 = dp1.getColumnLabel(i+1);
				String desc2 = dp2.getColumnLabel(i+1);
				if (desc1.toUpperCase().equals(desc2.toUpperCase())) {
					targetBuilder.dumpHeaderColumn(i, desc1, OutputStyle.TOP_HEADER);
				} else {
					targetBuilder.dumpHeaderColumn(i, desc1 + " / " + desc2, OutputStyle.TOP_HEADER);
				}
			}
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
		targetBuilder.dumpColumn(0, dp.getCaption(), OutputFormat.DEFAULT);
		targetBuilder.setColumnPosition(1, 1, null);
		targetBuilder.dumpJoinOnColumns(dp, on1);
		targetBuilder.setColumnPosition(onLength + 1, 1, null);							
		targetBuilder.dumpOtherColumns(dp, on1, null);		
	}
}
