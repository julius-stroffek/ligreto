package net.ligreto.junit.tests.func.smalldata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.junit.Test;

public class PrepareTestData {

	/** The number of rows to be tested. */
	public static final long rowCount = 1000;

	/** The number of rows to be tested. */
	public static final long commitInterval = 6;

	/**
	 * @throws java.lang.Exception
	 */
	@Test
	public void prepareTestData() throws Exception {
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		Properties properties = new Properties();
		properties.setProperty("create", "true");
		Connection cnn1 = DriverManager.getConnection("jdbc:derby:db1", properties);
		Connection cnn2 = DriverManager.getConnection("jdbc:derby:db2", properties);
		Connection cnn3 = DriverManager.getConnection("jdbc:derby:db3", properties);
		cnn1.setAutoCommit(true);
		cnn2.setAutoCommit(true);
		Statement stm1 = cnn1.createStatement();
		Statement stm2 = cnn2.createStatement();
		Statement stm3 = cnn3.createStatement();
		try {
			stm1.execute("drop table join_table1");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm1.execute("drop table multi_join1");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm2.execute("drop table join_table2");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm2.execute("drop table multi_join2");
		} catch (SQLException e) {
			// do nothing
		}
		try {
			stm3.execute("drop table coll_table");
		} catch (SQLException e) {
			// do nothing
		}
		stm1.execute("create table join_table1 (Id int, first_name varchar(32), last_name varchar(32), age int)");
		stm2.execute("create table join_table2 (Id int, first_name varchar(32), last_name varchar(32), age int)");
		stm1.execute("insert into join_table1 values (1, '1Martin1', '1Velky1', 11)");
		stm2.execute("insert into join_table2 values (1, '2Bruce1', '2Abone1', 21)");
		stm1.execute("insert into join_table1 values (2, '1Martin2', '1Velky2', 12)");
		stm2.execute("insert into join_table2 values (2, '2Bruce2', '2Abone2', 22)");
		stm1.execute("insert into join_table1 values (3, '1Martin3', '1Velky3', 13)");
		stm2.execute("insert into join_table2 values (4, '2Bruce4', '2Abone4', 24)");
		stm1.execute("insert into join_table1 values (5, '1Martin5', '1Velky5', 15)");
		stm2.execute("insert into join_table2 values (6, '2Bruce6', '2Abone6', 26)");
		stm1.execute("insert into join_table1 values (7, 'Martin7', 'Velky7', 77)");
		stm2.execute("insert into join_table2 values (7, 'Martin7', 'Velky7', 77)");
		stm1.execute("insert into join_table1 values (8, 'Bruce8', 'Abone8', 15)");
		stm2.execute("insert into join_table2 values (8, 'Bruce8', 'Abone8', 88)");
		
		stm1.execute("create table multi_join1 (Id int, Id2 int, Id3 varchar(32), first_name varchar(32), last_name varchar(32), age int)");
		stm2.execute("create table multi_join2 (Id int, Id2 int, Id3 varchar(32), first_name varchar(32), last_name varchar(32), age int)");
		stm1.execute("insert into multi_join1 values (1, 11, '12', '1Martin1', '1Velky1', 11)");
		stm2.execute("insert into multi_join2 values (1, 11, '12', '2Bruce1', '2Abone1', 21)");
		stm1.execute("insert into multi_join1 values (2, null, '12', 'middle1', 'null', 11)");
		stm2.execute("insert into multi_join2 values (2, 11, '12', 'middle1', 'null', 21)");
		stm1.execute("insert into multi_join1 values (3, 11, '12', 'middle2', 'null', 11)");
		stm2.execute("insert into multi_join2 values (3, null, '12', 'middle2', 'null', 21)");
		stm1.execute("insert into multi_join1 values (4, 11, null, 'last1', 'null', 11)");
		stm2.execute("insert into multi_join2 values (4, 11, '12', 'last1', 'null', 21)");
		stm1.execute("insert into multi_join1 values (5, 11, '12', 'last2', 'null', 11)");
		stm2.execute("insert into multi_join2 values (5, 11, null, 'last2', 'null', 21)");
		stm1.execute("insert into multi_join1 values (6, null, null, 'match1', 'null', 11)");
		stm2.execute("insert into multi_join2 values (6, null, null, 'match1', 'null', 21)");
		stm1.execute("insert into multi_join1 values (7, 11, null, 'match2', 'null', 11)");
		stm2.execute("insert into multi_join2 values (7, 11, null, 'match2', 'null', 21)");

		stm3.execute("create table coll_table (Id varchar(32), first_name varchar(32), last_name varchar(32), age int)");
		stm3.execute("insert into coll_table values ('abcd', '1Martin1', '1Velky1', 11)");
		stm3.execute("insert into coll_table values ('bcde', '1Martin2', '1Velky2', 12)");
		stm3.execute("insert into coll_table values ('cdef', '1Martin3', '1Velky3', 13)");
		stm3.execute("insert into coll_table values ('defg', '1Martin4', '1Velky4', 14)");
		stm3.execute("insert into coll_table values ('efgh', '1Martin5', '1Velky5', 15)");
		stm3.execute("insert into coll_table values ('fghc', '1Martin6', '1Velky6', 16)");
		stm3.execute("insert into coll_table values ('ghch', '1Martin7', '1Velky7', 17)");
		stm3.execute("insert into coll_table values ('hchi', '1Martin8', '1Velky8', 18)");
		stm3.execute("insert into coll_table values ('chij', '1Martin9', '1Velky9', 19)");

		cnn1.close();
		cnn2.close();
	}
}
