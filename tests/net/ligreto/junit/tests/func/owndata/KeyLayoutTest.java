/**
 * 
 */
package net.ligreto.junit.tests.func.owndata;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import net.ligreto.exceptions.DuplicateKeyValuesException;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.LigretoExecutor;
import net.ligreto.junit.util.TestUtil;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.LigretoNode;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author Julius Stroffek
 *
 */
public class KeyLayoutTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Prepare the properties with 'create=true'
		Properties createProperties = new Properties();
		createProperties.setProperty("create", "true");
		// Get the database connections (create DBs if they do not exist)
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Connection cnn1 = DriverManager.getConnection("jdbc:derby:db1", createProperties);
		Connection cnn2 = DriverManager.getConnection("jdbc:derby:db2", createProperties);
		cnn1.setAutoCommit(true);
		cnn2.setAutoCommit(true);
		Statement stm1 = cnn1.createStatement();
		Statement stm2 = cnn2.createStatement();
		try {
			stm1.execute("drop table key_table1");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm2.execute("drop table key_table2");
		} catch (SQLException e) {
			// do nothing
		}
		stm1.execute("create table key_table1 (Key_Column int, Key2_T1 int)");
		stm2.execute("create table key_table2 (Key_Column int, Key2_T2 int)");

		stm1.execute("insert into key_table1 values (1,2)");
		stm2.execute("insert into key_table2 values (1,2 )");
		stm1.execute("insert into key_table1 values (2,3)");
		stm2.execute("insert into key_table2 values (2,3)");
		stm1.execute("insert into key_table1 values (3,4)");
		stm2.execute("insert into key_table2 values (4,4)");
		stm1.execute("insert into key_table1 values (5,5)");
		stm2.execute("insert into key_table2 values (5,5)");
		stm1.execute("insert into key_table1 values (6,7)");
		stm2.execute("insert into key_table2 values (7,8)");
		stm1.execute("insert into key_table1 values (8,8)");
		stm2.execute("insert into key_table2 values (8,9)");
		stm1.execute("insert into key_table1 values (9,11)");
		stm2.execute("insert into key_table2 values (9,11)");

		cnn1.close();
		cnn2.close();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testKeyLayout() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		TestUtil.testReport("keyreport", false);
	}
	
	@Test
	public void testNoKeyDuplicatesReport() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("nokeyreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		
		boolean exceptionThrown = false;
		try {
			executor.execute();
		} catch (LigretoException e) {
			Throwable c1 = e.getCause();
			Throwable c2 = c1.getCause();
			
			// Check that we got the right exception with the proper cause
			if (c2 instanceof DuplicateKeyValuesException) {
				exceptionThrown = true;
			} else {
				throw e;
			}
		}
		Assert.assertTrue(exceptionThrown);
	}
}
