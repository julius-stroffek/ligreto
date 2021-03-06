package net.ligreto.executor;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.ligreto.Database;
import net.ligreto.ResultStatus;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.ddl.DataTypeDialect;
import net.ligreto.parser.nodes.LigretoNode;
import net.ligreto.parser.nodes.PtpNode;
import net.ligreto.parser.nodes.SqlNode;
import net.ligreto.parser.nodes.TargetNode;
import net.ligreto.parser.nodes.TransferNode;
import net.ligreto.util.DataProviderUtils;
import net.ligreto.util.MiscUtils;
import net.pcal.sqlsheet.XlsResultSet;

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
	
	/** The data type dialect to use for DDL statements. */
	protected DataTypeDialect dataTypeDialect;
	
	/** The excel date format. */
	protected final DateFormat excelDateFormat;
	
	@Override
	public ResultStatus execute() throws LigretoException {
		ResultStatus result = new ResultStatus();
		for(PtpNode ptpNode: ptpNodes) {
			result.merge(executePTP(ptpNode));
		}
		return result;
	}

	/**
	 * The constructor used to construct the executor instance.
	 * 
	 * @param ligretoNode
	 */
	public PtpExecutor(LigretoNode ligretoNode) {
		excelDateFormat = new SimpleDateFormat(ligretoNode.getLigretoParameters().getXlsxJdbcDateFormat());
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
			throw new LigretoException("Error processing the PTP transfer: " + ptpNode.getName(), e);
		}
		result.info(log, "PTP");
		return result;
	}

	protected ResultStatus transferData(TransferNode transferNode) throws LigretoException, ParseException {
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
				case QUERY:
					log.info("Executing the SQL query on \"" + sqlNode.getDataSource() + "\" data source:");
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
					throw new LigretoException("Unsupported query type on \"" + sqlNode.getDataSource() + "\" data source:" + sqlNode.getQueryType().toString().toLowerCase());
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
			throw new LigretoException(msg, e);
		} catch (ClassNotFoundException e) {
			String msg = "Database driver not found for data source: " + sqlNode.getDataSource();
			throw new LigretoException(msg, e);			
		}
		return result;
	}

	protected void transferRow(ResultSet rs) throws SQLException, LigretoException, ParseException {
		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i=1; i <= rsmd.getColumnCount(); i++) {
			int columnType = rsmd.getColumnType(i);
			switch (columnType) {
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
				if (rs instanceof XlsResultSet) {
					String dateString = rs.getString(i);
					if (MiscUtils.isNotEmpty(dateString)) {
						Date dateValue = new Date(excelDateFormat.parse(dateString).getTime());
						insertStmt.setDate(i, dateValue);
					} else {
						insertStmt.setDate(i, null);						
					}
				} else {
					insertStmt.setDate(i, rs.getDate(i));
				}
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
				throw new LigretoException("Unsupported data type: " + DataProviderUtils.getJdbcTypeName(columnType));
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
			if (targetNode.getTransferNode().isQuoteColumnNames()) {
				sb.append('"');
				sb.append(rsmd.getColumnName(i));
				sb.append('"');
			} else {
				sb.append(rsmd.getColumnName(i));
			}
			sb.append(" ");
			sb.append(dataTypeDialect.getTypeDeclaration(rsmd, i));
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
				if (targetNode.getTransferNode().isQuoteColumnNames()) {
					sb.append('"');
					sb.append(rsmd.getColumnName(i));					
					sb.append('"');
				} else {
					sb.append(rsmd.getColumnName(i));
				}
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
			dataTypeDialect = DataTypeDialect.getInstance(tgtCnn);

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
