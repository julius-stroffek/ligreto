package net.ligreto.executor;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.Database;
import net.ligreto.ResultStatus;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.parser.nodes.PtpNode;
import net.ligreto.parser.nodes.SqlNode;
import net.ligreto.parser.nodes.TargetNode;
import net.ligreto.parser.nodes.TransferNode;

public class PtpExecutor extends Executor {

	/** The logger instance for the class. */
	private Log log = LogFactory.getLog(PtpExecutor.class);
	
	/** Iterable object holding the PTP nodes to be processed. */ 
	Iterable<PtpNode> ptpNodes;
	
	/** The generated "insert" query for the transfer process according the select result set. */
	protected String insertQry;
	
	/** The generated "create table" query for the target according the select result set. */
	protected String createQry;
	
	/** The prepared statement for the insertion part of the data transfer process. */
	protected PreparedStatement insertStmt;
	
	/** The connection to the target data source. */
	protected Connection tgtCnn;
	
	@Override
	public ResultStatus execute() throws LigretoException {
		ResultStatus result = new ResultStatus();
		for(PtpNode ptpNode: ptpNodes) {
			result.merge(executePTP(ptpNode));
		}
		return result;
	}

	/**
	 * Executes the Pre-process/Transfer/Post-process for the specified PTP node.
	 * 
	 * @param ptpNode The node to be processed.
	 * @throws LigretoException On any failure with the chained exception.
	 */
	protected ResultStatus executePTP(PtpNode ptpNode) throws LigretoException {
		ResultStatus result = new ResultStatus();
		try {
			// Do pre-processing
			SqlExecutor sqlExecutor = new SqlExecutor();
			if (ptpNode.getPreprocessNode() != null) {
				log.info("Running transfer pre-processing.");
				sqlExecutor.setSqlNodes(ptpNode.getPreprocessNode().sqlQueries());
				sqlExecutor.execute();
			}
			
			// Do the transfer of data
			for (TransferNode transferNode : ptpNode.transferNodes()) {
				log.info("Running transfer process.");
				result.merge(transferData(transferNode));
			}
			
			// Do post-processing
			if (ptpNode.getPostprocessNode() != null) {
				log.info("Running transfer post-processing.");
				sqlExecutor.setSqlNodes(ptpNode.getPostprocessNode().sqlQueries());
				sqlExecutor.execute();
			}
		} catch (Exception e) {
			log.error("Error processing the PTP transfer.", e);
			throw new LigretoException("Error processing the PTP transfer: " + ptpNode.getName(), e);
		}
		result.info(log, "PTP");
		return result;
	}

	protected ResultStatus transferData(TransferNode transferNode) throws LigretoException {
		ResultStatus result = new ResultStatus();
		SqlNode sqlNode = transferNode.getSqlNode();
		TargetNode targetNode = transferNode.getTargetNode();
		try {
			Connection cnn = null;
			Statement stm = null;
			CallableStatement cstm = null;
			ResultSet rs = null;
			try {
				cnn = Database.getInstance().getConnection(sqlNode.getDataSource());
				String qry = sqlNode.getQuery().toString();
				stm = cnn.createStatement();
				
				switch (sqlNode.getQueryType()) {
				case STATEMENT:
					log.info("Executing the SQL statement on \"" + sqlNode.getDataSource() + "\" data source:");
					log.info(qry);
					stm = cnn.createStatement();
					rs = stm.executeQuery(qry);
					break;
				case CALL:
					log.info("Executing the SQL callable statement on \"" + sqlNode.getDataSource() + "\" data source:");
					log.info(qry);
					cstm = cnn.prepareCall(qry);
					rs = cstm.executeQuery();
					break;
				default:
					throw new LigretoException("Unknown query type.");
				}
				
				prepareTarget(targetNode, rs);
				
				if (targetNode.getCommitInterval() > 1) {
					log.info("Using commit interval: " + targetNode.getCommitInterval());
					tgtCnn.setAutoCommit(false);
					int insCnt = 0;
					while (rs.next()) {
						result.addRow();
						transferRow(rs);
						if (++insCnt == targetNode.getCommitInterval()) {
							tgtCnn.commit();
							insCnt = 0;
						}
					}
					tgtCnn.commit();
				} else {
					tgtCnn.setAutoCommit(true);
					while (rs.next()) {
						result.addRow();
						transferRow(rs);
					}
				}
			} finally {
				Database.close(cnn, stm, cstm, rs);
			}
			result.info(log, "TRANSFER");
		} catch (SQLException e) {
			String msg = "Database error on data source: " + sqlNode.getDataSource();
			log.error(msg, e);
			throw new LigretoException(msg, e);
		} catch (ClassNotFoundException e) {
			String msg = "Database driver not found for data source: " + sqlNode.getDataSource();
			log.error(msg, e);
			throw new LigretoException(msg, e);			
		}
		return result;
	}

	protected void transferRow(ResultSet rs) throws SQLException, LigretoException {
		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i=1; i <= rsmd.getColumnCount(); i++) {
			switch (rsmd.getColumnType(i)) {
			case Types.BIGINT:
				insertStmt.setLong(i, rs.getLong(i));
				break;
			case Types.BOOLEAN:
				insertStmt.setBoolean(i, rs.getBoolean(i));
				break;
			case Types.CHAR: 
				insertStmt.setString(i, rs.getString(i));
				break;
			case Types.DATE:
				insertStmt.setDate(i, rs.getDate(i));
				break;
			case Types.DECIMAL: 
				insertStmt.setBigDecimal(i, rs.getBigDecimal(i));
				break;
			case Types.DOUBLE: 
				insertStmt.setDouble(i, rs.getDouble(i));
				break;
			case Types.FLOAT: 
				insertStmt.setFloat(i, rs.getFloat(i));
				break;
			case Types.INTEGER: 
				insertStmt.setInt(i, rs.getInt(i));
				break;
			case Types.LONGNVARCHAR: 
				insertStmt.setString(i, rs.getString(i));
				break;
			case Types.LONGVARCHAR: 
				insertStmt.setString(i, rs.getString(i));
				break;
			case Types.NCHAR: 
				insertStmt.setString(i, rs.getString(i));
				break;
			case Types.NUMERIC: 
				insertStmt.setBigDecimal(i, rs.getBigDecimal(i));
				break;
			case Types.NVARCHAR: 
				insertStmt.setString(i, rs.getString(i));
				break;
			case Types.REAL:
				insertStmt.setFloat(i, rs.getFloat(i));
				break;
			case Types.SMALLINT: 
				insertStmt.setShort(i, rs.getShort(i));
				break;
			case Types.TIMESTAMP:
				insertStmt.setTimestamp(i, rs.getTimestamp(i));
				break;
			case Types.TINYINT:
				insertStmt.setShort(i, rs.getShort(i));
				break;
			case Types.VARCHAR:
				insertStmt.setString(i, rs.getString(i));
				break;
			case Types.ARRAY: 
			case Types.BINARY: 
			case Types.BIT: 
			case Types.BLOB: 
			case Types.CLOB: 
			case Types.DATALINK:
			case Types.DISTINCT: 
			case Types.JAVA_OBJECT:
			case Types.LONGVARBINARY: 
			case Types.NCLOB: 
			case Types.OTHER:
			case Types.REF: 
			case Types.ROWID: 
			case Types.SQLXML: 
			case Types.STRUCT: 
			case Types.TIME: 
			case Types.VARBINARY: 
			default:
				log.fatal("Unsupported data type.");
				throw new LigretoException("Unsupported data type.");
			}
		}
		insertStmt.execute();
	}

	protected void generateCreateTableQuery(TargetNode targetNode, ResultSet rs) throws SQLException, LigretoException {
		ResultSetMetaData rsmd = rs.getMetaData();
		StringBuilder sb = new StringBuilder();

		// Build the insert query first
		sb.append("create table ");
		sb.append(targetNode.getTable());
		sb.append(" (");
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			sb.append(rsmd.getColumnName(i));
			sb.append(" ");
			switch (rsmd.getColumnType(i)) {
			case Types.BIGINT:
				sb.append("long");
				break;
			case Types.BOOLEAN:
				sb.append("boolean");
				break;
			case Types.CHAR: 
				sb.append("char(");
				sb.append(rsmd.getPrecision(i));
				sb.append(")");
				break;
			case Types.DATE:
				sb.append("date");
				break;
			case Types.DECIMAL: 
				sb.append("decimal(");
				sb.append(rsmd.getPrecision(i) <= 31 ? rsmd.getPrecision(i) : 31);
				if (rsmd.getScale(i) >= 0) {
					sb.append(",");
					sb.append(rsmd.getScale(i));
				}
				sb.append(")");
				break;
			case Types.DOUBLE: 
				sb.append("double");
				break;
			case Types.FLOAT: 
				sb.append("float");
				break;
			case Types.INTEGER: 
				sb.append("int");
				break;
			case Types.LONGNVARCHAR: 
				sb.append("longnvarchar(");
				sb.append(rsmd.getPrecision(i));
				sb.append(")");
				break;
			case Types.LONGVARCHAR: 
				sb.append("longvarchar(");
				sb.append(rsmd.getPrecision(i));
				sb.append(")");
				break;
			case Types.NCHAR: 
				sb.append("nchar(");
				sb.append(rsmd.getPrecision(i));
				sb.append(")");
				break;
			case Types.NUMERIC: 
				sb.append("numeric(");
				sb.append(rsmd.getPrecision(i) <= 31 ? rsmd.getPrecision(i) : 31);
				if (rsmd.getScale(i) >= 0) {
					sb.append(",");
					sb.append(rsmd.getScale(i));
				}
				sb.append(")");
				break;
			case Types.NVARCHAR: 
				sb.append("nvarchar(");
				sb.append(rsmd.getPrecision(i));
				sb.append(")");
				break;
			case Types.REAL:
				sb.append("float");
				break;
			case Types.SMALLINT: 
				sb.append("short");
				break;
			case Types.TIMESTAMP:
				sb.append("timestamp");
				break;
			case Types.TINYINT:
				sb.append("short");
				break;
			case Types.VARCHAR:
				sb.append("varchar(");
				sb.append(rsmd.getPrecision(i));
				sb.append(")");
				break;
			case Types.ARRAY: 
			case Types.BINARY: 
			case Types.BIT: 
			case Types.BLOB: 
			case Types.CLOB: 
			case Types.DATALINK:
			case Types.DISTINCT: 
			case Types.JAVA_OBJECT:
			case Types.LONGVARBINARY: 
			case Types.NCLOB: 
			case Types.OTHER:
			case Types.REF: 
			case Types.ROWID: 
			case Types.SQLXML: 
			case Types.STRUCT: 
			case Types.TIME: 
			case Types.VARBINARY: 
			default:
				log.fatal("Unsupported data type.");
				throw new LigretoException("Unsupported data type.");
			}
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");
		createQry = sb.toString();
		log.debug("create table query generated from the query result:");
		log.debug(createQry);
	}

	/**
	 * This function will prepare the target and could do the following:
	 * <ul>
	 * <li>drop the table if it should be re-created</li>
	 * <li>create the table according the result set</li>
	 * <li>truncate the already existing table</li>
	 * <li>generate the insert statement for the transfer</li>
	 * </ul>
	 * 
	 * @param targetNode The description of the target from the parser.
	 * @param rs The result set of the source query.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws LigretoException
	 */
	protected void prepareTarget(TargetNode targetNode, ResultSet rs) throws SQLException, ClassNotFoundException, LigretoException {
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
			log.debug("insert into query generated from the query result:");
			log.debug(insertQry);
			

			// Get the target connection
			tgtCnn = Database.getInstance().getConnection(targetNode.getDataSource());
			tgtCnn.setAutoCommit(true);

			boolean tableExists = true;
			boolean createTable = targetNode.isCreate();
			Statement stm = tgtCnn.createStatement();
			
			try {
				stm.execute("select * from " + targetNode.getTable());
			} catch (SQLException e) {
				tableExists = false;
			}
			if (targetNode.isRecreate()) {
				try {
					log.info("Dropping the already existing table: " + targetNode.getTable());
					stm.execute("drop table " + targetNode.getTable());
				} catch (SQLException e) {
					// do nothing here as the table might not exist
				}
				createTable = true;
				tableExists = false;
			}
			if (createTable && !tableExists) {
				generateCreateTableQuery(targetNode, rs);
				log.info("Creating the table according the query result: " + targetNode.getTable());
				stm.execute(createQry);
			}
			if (targetNode.isTruncate()) {
				log.info("Truncating the table: " + targetNode.getTable());
				stm.execute("truncate table " + targetNode.getTable());
			}
			insertStmt = tgtCnn.prepareStatement(insertQry);
		} catch (SQLException e) {
			log.error("Database error on data source: " + targetNode.getDataSource(), e);
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
