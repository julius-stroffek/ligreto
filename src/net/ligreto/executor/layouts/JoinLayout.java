package net.ligreto.executor.layouts;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.ligreto.Database;
import net.ligreto.LigretoParameters;
import net.ligreto.ResultStatus;
import net.ligreto.builders.TargetInterface;
import net.ligreto.exceptions.DataSourceNotDefinedException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.parser.nodes.JoinNode;
import net.ligreto.parser.nodes.LayoutNode;
import net.ligreto.parser.nodes.LayoutNode.LayoutType;

/**
 * Provides the interface for implementing various join layouts that could be used
 * to build various reports using the specified report builder class.
 * 
 * @author Julius Stroffek
 * 
 */
public abstract class JoinLayout {
	
	/** Indicates the type of the result to be processed. */
	public enum JoinResultType {LEFT, RIGHT, INNER};
	
	/** The report target used for report generation of the layout. */
	protected TargetInterface targetBuilder;
	
	/** The parser join node of the processed join. */
	protected JoinNode joinNode = null;
	
	/** The parser layout node of the layout to create. */
	protected LayoutNode layoutNode = null;
	
	/** The object holding the join result status. */
	protected ResultStatus resultStatus = null;
	
	/** The column indices of the columns to be equal from the first result set. */
	protected int[] on1 = null;
	
	/** The column indices of the columns to be equal from the second result set. */
	protected int[] on2 = null;
	
	/** The column indices of the columns to be excluded from the comparison in the first result set. */
	protected int[] excl1 = null;
	
	/** The column indices of the columns to be excluded from the comparison in the second result set. */
	protected int[] excl2 = null;
	
	/** The columns which should be used for aggregated result. */
	protected int[] groupBy = null;
	
	/** The first result set. */
	protected ResultSet rs1 = null;
	
	/** The second result set. */
	protected ResultSet rs2 = null;
	
	/** The length of the on1 and on2 arrays which have to be the same. */
	protected int onLength = -1;
	
	/** The length of {@code groupBy} array. */
	protected int groupByLength = 0;
	
	/** The number of columns that will get processed from the both first and second result sets. */
	protected int rsColCount = -1;
	
	/** The arrays showing all the elements from the first result set to be lower. */
	protected int[] lowerArray = null;
	
	/** The arrays showing all the elements from the second result set to be higher. */
	protected int[] higherArray = null;
	
	/** The global ligreto parameters. */
	protected LigretoParameters ligretoParameters;
	
	/** The description of 1st data source. */
	protected String dataSourceDesc1 = null;
	
	/** The description of 2nd data source. */
	protected String dataSourceDesc2 = null;
	
	/** Constructs the layout having the specified report builder. */
	protected JoinLayout(TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		this.targetBuilder = targetBuilder;
		this.ligretoParameters = ligretoParameters;
	}
	
	/**
	 * Constructs the layout of the specified type.
	 * 
	 * @param layoutType The type of the layout to create.
	 * @param reportBuilder The report builder used for report generation.
	 * @param ligretoParameters The global ligreto parameters to use.
	 * @return The created {@code JoinLayout} instance.
	 */
	public static JoinLayout createInstance(LayoutType layoutType, TargetInterface targetBuilder, LigretoParameters ligretoParameters) {
		switch (layoutType) {
		case NORMAL:
			return new NormalJoinLayout(targetBuilder, ligretoParameters);
		case INTERLACED:
			return new InterlacedJoinLayout(targetBuilder, ligretoParameters);
		case DETAILED:
			return new DetailedJoinLayout(targetBuilder, ligretoParameters);
		case AGGREGATED:
			return new AggregatedLayout(targetBuilder, ligretoParameters);
		case KEY:
			return new KeyJoinLayout(targetBuilder, ligretoParameters);
		default:
			throw new IllegalArgumentException("Unexpected value of JoinLayoutType.");
		}
	}

	/** Dumps the join result header. 
	 * @throws SQLException 
	 * @throws DataSourceNotDefinedException 
	 * @throws IOException */
	public abstract void dumpHeader() throws SQLException, DataSourceNotDefinedException, IOException;
	
	/**
	 * Will dump the result row from the corresponding result sets. The method will also
	 * call the ResultSet.next() method on the result sets where the row was processed.
	 * The method will highlight some of the columns based on the array specified
	 * 
	 * @param rowDiffs   The number of differences encountered in the current row.
	 * @param highlightArray Determines which columns should be highlighted.
	 * @param resultType Determines whether to dump the row from the first,
	 *                   second or both result sets.
	 * @throws SQLException 
	 * @throws LigretoException 
	 * @throws IOException 
	 */
	public abstract void dumpRow(int rowDiffs, int[] highlightArray, JoinResultType resultType) throws SQLException, LigretoException, IOException;
	
	/**
	 * Will dump the result row from the corresponding result sets. The method will also
	 * call the ResultSet.next() method on the result sets where the row was processed.
	 * 
	 * @param rowDiffs   The number of differences encountered in the current row.
	 * @param resultType Determines whether to dump the row from the first,
	 *                   second or both result sets.
	 * @throws SQLException 
	 * @throws LigretoException 
	 * @throws IOException 
	 */
	public void dumpRow(int rowDiffs, JoinResultType resultType) throws SQLException, LigretoException, IOException {
		dumpRow(rowDiffs, null, resultType);
	}

	/**
	 * @return the layoutNode
	 */
	public LayoutNode getLayoutNode() {
		return layoutNode;
	}

	/**
	 * @param joinNode
	 * 				The join node to set.
	 * @throws DataSourceNotDefinedException 
	 */
	public void setLayoutNode(LayoutNode layoutNode) {
		this.layoutNode = layoutNode;
	}

	/**
	 * @param joinNode
	 * 				The join node to set.
	 * @throws DataSourceNotDefinedException 
	 */
	public void setJoinNode(JoinNode joinNode) throws DataSourceNotDefinedException {
		this.joinNode = joinNode;
		if (joinNode != null) {
			String dSrc1 =  joinNode.getSqlQueries().get(0).getDataSource();
			String dSrc2 =  joinNode.getSqlQueries().get(1).getDataSource();		
			dataSourceDesc1 = Database.getInstance().getDataSourceNode(dSrc1).getDescription();
			dataSourceDesc2 = Database.getInstance().getDataSourceNode(dSrc2).getDescription();
		} else {
			dataSourceDesc1 = null;
			dataSourceDesc2 = null;
		}
	}

	/**
	 * @param resultStatus
	 * 				The result status to set.
	 */
	public void setResultStatus(ResultStatus resultStatus) {
		this.resultStatus = resultStatus;
	}

	/**
	 * @param on1 The join columns for first result set to set.
	 * @param on2 The join columns for second result set to set.
	 */
	public void setOnColumns(int[] on1, int[] on2) {
		this.on1 = on1;
		this.on2 = on2;
		if (on1.length != on2.length)
			throw new IllegalArgumentException("The length of on1 and on2 arrays differs.");
		onLength = on1.length;
	}
	
	/**
	 * @param groupBy
	 * 			The group by columns to set.
	 */
	public void setGroupByColumns(int[] groupBy) {
		this.groupBy = groupBy;
		if (groupBy != null)
			groupByLength = groupBy.length;
		else
			groupByLength = 0;
	}

	/**
	 * @param excl1 The exclude columns for the first result set to set.
	 * @param excl2 The exclude columns for the second result set to set.
	 */
	public void setExcludeColumns(int[] excl1, int[] excl2) {
		this.excl1 = excl1;
		this.excl2 = excl2;
	}

	/**
	 * @param rs1 The first result set to set.
	 * @param rs2 The second result set to set.
	 */
	public void setResultSets(ResultSet rs1, ResultSet rs2) {
		this.rs1 = rs1;
		this.rs2 = rs2;
	}
	
	/**
	 * @param ligretoParameters the ligretoParameters to set
	 */
	public void setLigretoParameters(LigretoParameters ligretoParameters) {
		this.ligretoParameters = ligretoParameters;
	}

	/**
	 * @param columnCount
	 * 				The column count to set.
	 */
	public void setColumnCount(int columnCount) {
		rsColCount = columnCount;	
		// Create the arrays to be used to highlight
		// differences for left, right and outer joins
		lowerArray = new int[rsColCount];
		higherArray = new int[lowerArray.length];
		for (int i=0; i < lowerArray.length; i++) {
			lowerArray[i] = -1;
			higherArray[i] = 1;
		}
	}

	/**
	 * The method executed before providing any data to the layout object.
	 * @throws SQLException 
	 * @throws LigretoException 
	 */
	public void start() throws SQLException, LigretoException {
	}

	/**
	 * The method executed after providing all data to the layout object.
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public void finish() throws IOException, SQLException {
		targetBuilder.finish();
	}

}
