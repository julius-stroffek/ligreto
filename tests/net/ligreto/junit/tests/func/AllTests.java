package net.ligreto.junit.tests.func;

import net.ligreto.junit.tests.EnvironmentTest;
import net.ligreto.junit.util.TestUtil;
import net.ligreto.util.AssertionUtil;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	EnvironmentTest.class,
	net.ligreto.junit.tests.func.nodata.AllTests.class,
	net.ligreto.junit.tests.func.owndata.AllTests.class,
	net.ligreto.junit.tests.func.smalldata.AllTests.class,
	net.ligreto.junit.tests.func.mediumdata.AllTests.class,
	net.ligreto.junit.tests.func.largedata.AllTests.class
})
public class AllTests {
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
		AssertionUtil.enableAssertions();
		TestUtil.createDBs();
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDown() throws Exception {
	}	
}
