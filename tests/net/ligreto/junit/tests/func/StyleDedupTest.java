package net.ligreto.junit.tests.func;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import net.ligreto.exceptions.LigretoException;
import net.ligreto.executor.LigretoExecutor;
import net.ligreto.parser.Parser;
import net.ligreto.parser.nodes.LigretoNode;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;


public class StyleDedupTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Connection cnn = DriverManager.getConnection("jdbc:derby:db1");
		cnn.setAutoCommit(true);
		Statement stm = cnn.createStatement();
		try {
			stm.execute("drop table style_test_t1");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm.execute("drop table style_test_t2");
		} catch (SQLException e) {
			// do nothing
		}
		stm.execute("create table style_test_t1 (Id int, column_1 varchar(32), column_2 varchar(32), column_3 varchar(32), age int)");
		stm.execute("create table style_test_t2 (Id int, column_1 varchar(32), column_2 varchar(32), column_3 varchar(32), age int)");
		
		PreparedStatement pStm1 = cnn.prepareStatement("insert into style_test_t1 values (?, ?, ?, ?, ?)");
		PreparedStatement pStm2 = cnn.prepareStatement("insert into style_test_t2 values (?, ?, ?, ?, ?)");
		
		cnn.setAutoCommit(false);
		for (int i=0; i < 1400; i++) {
			pStm1.setInt(1, i);
			pStm1.setString(2, "Tab1Col2Row" + i);
			pStm1.setString(3, "Tab1Col2Row" + i);
			pStm1.setString(4, "Tab1Col2Row" + i);
			pStm1.setInt(5, i % 100);
			pStm1.execute();

			pStm2.setInt(1, i);
			pStm2.setString(2, "Tab2Col2Row" + i);
			pStm2.setString(3, "Tab2Col2Row" + i);
			pStm2.setString(4, "Tab2Col2Row" + i);
			pStm2.setInt(5, i % 100);
			pStm2.execute();
		}
		cnn.commit();

		cnn.close();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() {
		System.setProperty("excel97", "yes");
	}
	
	@After
	public void tearDown() {
		System.clearProperty("excel97");
	}
	
	@Test
	public void testStyleDedup() throws SAXException, IOException, ClassNotFoundException, SQLException, LigretoException {
		LigretoNode ligreto = Parser.parse("stylededupreport.xml");
		LigretoExecutor executor = new LigretoExecutor(ligreto);
		executor.execute();
	}

}
