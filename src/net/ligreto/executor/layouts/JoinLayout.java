package net.ligreto.executor.layouts;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.ligreto.builders.BuilderInterface;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.parser.nodes.JoinNode;

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
	
	/** The report builder used for report generation of the layout. */
	protected BuilderInterface reportBuilder;
	
	/** The parser join node of the processed join. */
	protected JoinNode joinNode = null;
	
	/** The column indices of the columns to be equal from the first result set. */
	protected int[] on1 = null;

	/** The column indices of the columns to be equal from the second result set. */
	protected int[] on2 = null;
	
	/** The column indices of the columns to be excluded from the comparison in the first result set. */
	protected int[] excl1 = null;

	/** The column indices of the columns to be excluded from the comparison in the second result set. */
	protected int[] excl2 = null;
	
	/** The first result set. */
	protected ResultSet rs1 = null;
	
	/** The second result set. */
	protected ResultSet rs2 = null;
	
	/** The length of the on1 and on2 arrays which have to be the same. */
	protected int onLength = -1;
	
	/** The number of columns that will get processed from the both first and second result sets. */
	protected int rsColCount = -1;
	
	/** The arrays showing all the elements from the first result set to be lower. */
	protected int[] lowerArray = null;

	/** The arrays showing all the elements from the second result set to be higher. */
	protected int[] higherArray = null;
		
	/** Constructs the layout having the specified report builder. */
	protected JoinLayout(BuilderInterface reportBuilder) {
		this.reportBuilder = reportBuilder;
	}

	/** Dumps the join result header. 
	 * @throws SQLException */
	public abstract void dumpHeader() throws SQLException;
	
	/**
	 * Will dump the result row from the corresponding result sets. The method will also
	 * call the ResultSet.next() method on the result sets where the row was processed.
	 * The method will highlight some of the columns based on the array specified
	 * 
	 * @param highlightArray Determines which columns should be highlighted.
	 * @param resultType Determines whether to dump the row from the first,
	 *                   second or both result sets.
	 * @throws SQLException 
	 * @throws LigretoException 
	 */
	public abstract void dumpRow(int[] highlightArray, JoinResultType resultType) throws SQLException, LigretoException;
	
	/**
	 * Will dump the result row from the corresponding result sets. The method will also
	 * call the ResultSet.next() method on the result sets where the row was processed.
	 * 
	 * @param resultType Determines whether to dump the row from the first,
	 *                   second or both result sets.
	 * @throws SQLException 
	 * @throws LigretoException 
	 */
	public void dumpRow(JoinResultType resultType) throws SQLException, LigretoException {
		dumpRow(null, resultType);
	}

	public void setJoinNode(JoinNode joinNode) {
		this.joinNode = joinNode;
	}

	public void setOnColumns(int[] on1, int[] on2) {
		this.on1 = on1;
		this.on2 = on2;
		if (on1.length != on2.length)
			throw new IllegalArgumentException("The length of on1 and on2 arrays differs.");
		onLength = on1.length;
	}

	public void setExcludeColumns(int[] excl1, int[] excl2) {
		this.excl1 = excl1;
		this.excl2 = excl2;
	}

	public void setResultSets(ResultSet rs1, ResultSet rs2) {
		this.rs1 = rs1;
		this.rs2 = rs2;
	}
	
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
}
