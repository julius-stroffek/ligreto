package net.ligreto.junit.tests;

import net.ligreto.junit.util.TestUtil;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	net.ligreto.junit.tests.unit.AllTests.class,
	net.ligreto.junit.tests.func.AllTests.class,
	net.ligreto.junit.tests.perf.AllTests.class
})
public class AllTests {
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
		TestUtil.createDBs();
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDown() throws Exception {
	}	
}
