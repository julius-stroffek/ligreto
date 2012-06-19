package net.ligreto.executor.ddl;

/**
 * 
 * @author Julius Stroffek
 *
 */
public class DerbyDataTypeDialect extends DataTypeDialect {

	/** The singleton instance. */
	protected static DerbyDataTypeDialect instance = new DerbyDataTypeDialect();
	
	/**
	 * @return the singleton instance
	 */
	public static DerbyDataTypeDialect getInstance() {
		return instance;
	}
	
	/**
	 * The default constructor
	 */
	public DerbyDataTypeDialect() {
	}

}
