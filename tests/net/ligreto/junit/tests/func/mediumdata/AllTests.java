package net.ligreto.junit.tests.func.mediumdata;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	PrepareTestData.class,
	AggregatedLayoutTest.class,
	AnalyticalLayoutTest.class,
	LayoutTest.class,
	ResultRowLimitTest.class,
	ResultColumnLimitTest.class,
	SummaryLayoutTest.class
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
