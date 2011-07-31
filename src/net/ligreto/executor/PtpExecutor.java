package net.ligreto.executor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import net.ligreto.Database;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.parser.nodes.PtpNode;
import net.ligreto.parser.nodes.SqlNode;
import net.ligreto.parser.nodes.TargetNode;

public class PtpExecutor extends Executor {

	/** Iterable object holding the PTP nodes to be processed. */ 
	Iterable<PtpNode> ptpNodes;
	
	protected String insertQry;
	protected PreparedStatement insertStmt;
	protected Connection tgtCnn;
	
	@Override
	public void execute() throws LigretoException {
		for(PtpNode ptpNode: ptpNodes) {
			executePTP(ptpNode);
		}
	}

	private void executePTP(PtpNode ptpNode) throws LigretoException {
		try {
			// Do pre-processing
			SqlExecutor sqlExecutor = new SqlExecutor();
			if (ptpNode.getPreprocessNode() != null) {
				sqlExecutor.setSqlNodes(ptpNode.getPreprocessNode().sqlQueries());
				sqlExecutor.execute();
			}
			
			// Do the transfer of data
			SqlNode sqlNode = ptpNode.getTransferNode().getSqlNode();
			TargetNode targetNode = ptpNode.getTransferNode().getTargetNode();
			transferData(sqlNode, targetNode);
			
			// Do post-processing
			if (ptpNode.getPostprocessNode() != null) {
				sqlExecutor.setSqlNodes(ptpNode.getPostprocessNode().sqlQueries());
				sqlExecutor.execute();
			}
		} catch (Exception e) {
			throw new LigretoException("Error processing the PTP transfer: " + ptpNode.getName(), e);
		}
		
	}

	private void transferData(SqlNode sqlNode, TargetNode targetNode) throws LigretoException {
		try {
			Connection cnn = null;
			Statement stm = null;
			ResultSet rs = null;
			try {
				cnn = Database.getInstance().getConnection(sqlNode.getDataSource());
				String qry = sqlNode.getQuery().toString();
				stm = cnn.createStatement();
				rs = stm.executeQuery(qry);
				
				prepareTarget(targetNode, rs);
				
				while (rs.next()) {
					transferRow(rs);
				}
			} finally {
				Database.close(cnn, stm, rs);
			}
		} catch (SQLException e) {
			throw new LigretoException("Database error on data source: " + sqlNode.getDataSource(), e);
		} catch (ClassNotFoundException e) {
			throw new LigretoException("Database driver not found for data source: " + sqlNode.getDataSource(), e);			
		}
	}

	private void transferRow(ResultSet rs) throws SQLException, LigretoException {
		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i=1; i <= rsmd.getColumnCount(); i++) {
			switch (rsmd.getColumnType(i)) {
			case Types.INTEGER:
				insertStmt.setInt(i, rs.getInt(i));
				break;
			case Types.VARCHAR:
				insertStmt.setString(i, rs.getString(i));
				break;
			default:
				throw new LigretoException("Unsupported data type.");
			}
		}
		insertStmt.execute();
	}

	private void prepareTarget(TargetNode targetNode, ResultSet rs) throws SQLException, ClassNotFoundException, LigretoException {
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			StringBuilder sb = new StringBuilder();

			// Build the insert query first
			sb.append("insert into ");
			sb.append(targetNode.getTable());
			sb.append(" (");
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				sb.append(rsmd.getColumnName(i));
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(") values (");
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				sb.append("?,");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
			insertQry = sb.toString();

			// Get the target connection
			tgtCnn = Database.getInstance().getConnection(targetNode.getDataSource());
			tgtCnn.setAutoCommit(true);

			if (targetNode.isCreate()) {
				System.err.println("Table creation is not yet implemented.");
			}
			if (targetNode.isTruncate()) {
				Statement stm = tgtCnn.createStatement();
				stm.execute("truncate table " + targetNode.getTable());
			}
			insertStmt = tgtCnn.prepareStatement(insertQry);
		} catch (SQLException e) {
			throw new LigretoException("Database error on data source: " + targetNode.getDataSource(), e);
		}
	}

	/**
	 * @return the ptpNodes
	 */
	public Iterable<PtpNode> getPtpNodes() {
		return ptpNodes;
	}

	/**
	 * @param ptpNodes the ptpNodes to set
	 */
	public void setPtpNodes(Iterable<PtpNode> ptpNodes) {
		this.ptpNodes = ptpNodes;
	}

}
