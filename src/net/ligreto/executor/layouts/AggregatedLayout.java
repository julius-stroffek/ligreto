package net.ligreto.executor.layouts;

import java.io.IOException;
import java.sql.SQLException;

import net.ligreto.Database;
import net.ligreto.LigretoParameters;
import net.ligreto.builders.BuilderInterface;
import net.ligreto.builders.BuilderInterface.HeaderType;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.exceptions.LigretoException;

public class AggregatedLayout extends JoinLayout {

	DetailedJoinLayout detailedLayout;
	
	public AggregatedLayout(BuilderInterface reportBuilder, LigretoParameters ligretoParameters) {
		super(reportBuilder, ligretoParameters);
		detailedLayout = new DetailedJoinLayout(reportBuilder, ligretoParameters);
	}
	
	public void setGroupByColumns(int[] groupBy) {
		super.setGroupByColumns(groupBy);
		detailedLayout.setOnColumns(groupBy, groupBy);
	}
	
	@Override
	public void dumpHeader() throws SQLException, DataSourceNotDefinedException, IOException {
		reportBuilder.nextRow();
		reportBuilder.setHeaderColumn(0, "Field Name", HeaderType.TOP);
		reportBuilder.setColumnPosition(1, 1, null);
		reportBuilder.dumpJoinOnHeader(rs1, groupBy);
		reportBuilder.setColumnPosition(groupByLength + 1, 1, null);
		
		String dSrc0 =  joinNode.getSqlQueries().get(0).getDataSource();
		String dSrc1 =  joinNode.getSqlQueries().get(1).getDataSource();		
		reportBuilder.setHeaderColumn(0, Database.getInstance().getDataSourceNode(dSrc0).getDescription(), HeaderType.TOP);
		reportBuilder.setHeaderColumn(1, Database.getInstance().getDataSourceNode(dSrc1).getDescription(), HeaderType.TOP);
		
		reportBuilder.setHeaderColumn(2, "Difference", HeaderType.TOP);
		reportBuilder.setHeaderColumn(3, "Relative", HeaderType.TOP);
	}

	@Override
	public void dumpRow(int[] highlightArray, JoinResultType resultType) throws SQLException, LigretoException, IOException {
		// TODO Auto-generated method stub

	}

}
