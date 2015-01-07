package net.ligreto.junit.tests.func.owndata;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	DataFormatTest.class,
	HighlightTest.class,
	KeyLayoutTest.class,
	NumericTypesTest.class,
	SqlReportTest.class,
	SortingTest.class,
	StyleDedupTest.class,
	XlsxJdbcTest.class
})
public class AllTests {
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDown() throws Exception {
	}	
}
