package net.ligreto;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.ligreto.exceptions.LigretoException;

/**
 * The  class holding all the global ligreto parameters.
 * 
 * @author Julius Stroffek
 *
 */
public class LigretoParameters {
	
	/**
	 * The default constructor
	 */
	public LigretoParameters() {
	}
		
	/** The string shown instead of the null value. */
	protected String nullString = "<null>";
	
	/** The string shown instead of the missing record. */
	protected String missingString = "<missing>";
	
	/** The string displayed in case of NaN value. */
	protected String nanString = "<NaN>";
	
	/** The collator implementation to be used. */
	protected String collatorClass = null;
	
	/** The collation name to be used. */
	protected String collationName = null;
	
	/** Indicates whether the null values should be collated first. Otherwise they are collated last. */
	protected boolean nullsFirst = true;
	
	/** The number of rows to fetch at once from the server in the result set. */
	protected int fetchSize = 1000;
	
	/** Indicates whether we should use strict type checking. */
	protected boolean strictTypes = true;
	
	/** The number of rows to be used as layout limit if not otherwise specified for the layout. */
	protected int layoutLimit = 0;
	
	/**
	 * The number of rows to be used as layout limit if not otherwise specified for the layout.
	 * This value applies only to layout with {@code diffs == true} and has a precedence
	 * before the {@link layoutLimit}.
	 */
	protected int layoutDifferenceLimit = 32000;
	
	/** Specifies the format for integer numbers in excel spread sheet. */
	protected String excelIntegerFormat = null;

	/** Specifies the format for float numbers in excel spread sheet. */
	protected String excelFloatFormat = null;
	
	/** Specifies the format for date type in excel spread sheet. */
	protected String excelDateFormat = "yyyy-mm-dd";
	
	/** Specifies the format for time stamp type in excel spread sheet. */
	protected String excelTimestampFormat = "yyyy-mm-dd hh:mm:ss";	

	/** Specifies the format for time type in excel spread sheet. */
	protected String excelTimeFormat = "hh:mm:ss";	
	
	/** Specifies the format for big decimal type in excel spread sheet. */
	protected String excelBigDecimalFormat = null;
	
	/** Specifies the format for string type in excel spread sheet. */
	protected String excelStringFormat = null;
	
	/** Specifies the format for date type for xlsx jdbc driver (using sqlsheet project). */
	protected String xlsJdbcDateFormat = "mm/dd/yyyy";
	
	/** E-mail address of the sender of outgoing e-mail. */
	protected String smtpEmailFrom;
	
	/** SMTP host name to send outgoing e-mails. */
	protected String smtpHost;
	
	/** SMTP port to be used to send outgoing e-mails. */
	protected String smtpPort;
	
	/** SMTP user name for outgoing e-mails authentication. */
	protected String smtpUser;
	
	/** SMTP password for outgoing e-mails authentication. */
	protected String smtpPasswd;
	
	/** Indicates whether to use SSL in SMTP. */
	protected String smtpSsl;

	/** Timestamp format for the ligreto.timestamp parameter. */
	protected String timestampFormat;
	
	/** Time format for the ligreto.time parameter. */
	protected String timeFormat;
	
	/** Date format for the ligreto.date parameter. */
	protected String dateFormat;
	
	/** Always contains actual timestamp. */
	protected String timestamp;
	
	/** Always contains actual time. */
	protected String time;
	
	/** Always contains actual date. */
	protected String date;
	
	public String getNullString() {
		return nullString;
	}

	public void setNullString(String nullString) {
		this.nullString = nullString;
	}

	public void setNullStringAsString(String nullString) {
		this.nullString = nullString;
	}

	public String getMissingString() {
		return missingString;
	}

	public String getMissingStringAsString() {
		return missingString;
	}

	public void setMissingString(String missingString) {
		this.missingString = missingString;
	}

	public String getNanString() {
		return nanString;
	}

	public String getNanStringAsString() {
		return nanString;
	}

	public void setNanString(String nanString) {
		this.nanString = nanString;
	}

	public void setNanStringAsString(String nanString) {
		this.nanString = nanString;
	}

	public String getCollatorClass() {
		return collatorClass;
	}

	public String getCollatorClassAsString() {
		return collatorClass;
	}

	public void setCollatorClass(String collatorClass) {
		this.collatorClass = collatorClass;
	}

	public String getCollationName() {
		return collationName;
	}

	public String getCollationNameAsString() {
		return collationName;
	}

	public void setCollationName(String collationName) {
		this.collationName = collationName;
	}

	public boolean getNullsFirst() {
		return nullsFirst;
	}

	public String getNullsFirstAsString() {
		return Boolean.toString(nullsFirst);
	}

	public void setNullsFirst(String nullsFirst) {
		this.nullsFirst = Boolean.parseBoolean(nullsFirst);
	}

	public int getFetchSize() {
		return fetchSize;
	}

	public String getFetchSizeAsString() {
		return Integer.toString(fetchSize);
	}

	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	public void setFetchSize(String fetchSize) {
		this.fetchSize = Integer.parseInt(fetchSize);
	}

	public void setStrictTypes(String strictTypes) {
		this.strictTypes = Boolean.parseBoolean(strictTypes);
	}

	public boolean getStrictTypes() {
		return strictTypes;
	}

	public String getStrictTypesAsString() {
		return Boolean.toString(strictTypes);
	}

	public int getLayoutLimit() {
		return layoutLimit;
	}

	public String getLayoutLimitAsString() {
		return Integer.toString(layoutLimit);
	}

	public void setLayoutLimit(int layoutLimit) {
		this.layoutLimit = layoutLimit;
	}

	public void setLayoutLimit(String layoutLimit) {
		if (layoutLimit != null && !"".equals(layoutLimit.trim())) { 
			this.layoutLimit = Integer.parseInt(layoutLimit);
		} else {
			this.layoutLimit = 0;
		}
	}

	public int getLayoutDifferenceLimit() {
		return layoutDifferenceLimit;
	}

	public String getLayoutDifferenceLimitAsString() {
		return Integer.toString(layoutDifferenceLimit);
	}

	public void setLayoutDifferenceLimit(int layoutDifferenceLimit) {
		this.layoutDifferenceLimit = layoutDifferenceLimit;
	}

	public void setLayoutDifferenceLimit(String layoutDifferenceLimit) {
		if (layoutDifferenceLimit != null && !"".equals(layoutDifferenceLimit.trim())) { 
			this.layoutDifferenceLimit = Integer.parseInt(layoutDifferenceLimit);
		} else {
			this.layoutDifferenceLimit = 0;
		}
	}

	public String getExcelIntegerFormat() {
		return excelIntegerFormat;
	}

	public void setExcelIntegerFormat(String excelIntegerFormat) {
		this.excelIntegerFormat = excelIntegerFormat;
	}

	public void setExcelIntegerFormatAsString(String excelIntegerFormat) {
		this.excelIntegerFormat = excelIntegerFormat;
	}

	public String getExcelFloatFormat() {
		return excelFloatFormat;
	}

	public void setExcelFloatFormat(String excelFloatFormat) {
		this.excelFloatFormat = excelFloatFormat;
	}

	public void setExcelFloatFormatAsString(String excelFloatFormat) {
		this.excelFloatFormat = excelFloatFormat;
	}

	public String getExcelDateFormat() {
		return excelDateFormat;
	}

	public void setExcelDateFormat(String excelDateFormat) {
		this.excelDateFormat = excelDateFormat;
	}

	public void setExcelDateFormatAsString(String excelDateFormat) {
		this.excelDateFormat = excelDateFormat;
	}

	public String getExcelTimestampFormat() {
		return excelTimestampFormat;
	}

	public void setExcelTimestampFormat(String excelTimestampFormat) {
		this.excelTimestampFormat = excelTimestampFormat;
	}

	public void setExcelTimestampFormatAsString(String excelTimestampFormat) {
		this.excelTimestampFormat = excelTimestampFormat;
	}

	public String getExcelTimeFormat() {
		return excelTimeFormat;
	}

	public void setExcelTimeFormat(String excelTimeFormat) {
		this.excelTimeFormat = excelTimeFormat;
	}

	public void setExcelTimeFormatAsString(String excelTimeFormat) {
		this.excelTimeFormat = excelTimeFormat;
	}

	public String getExcelBigDecimalFormat() {
		return excelBigDecimalFormat;
	}

	public void setExcelBigDecimalFormat(String excelBigDecimalFormat) {
		this.excelBigDecimalFormat = excelBigDecimalFormat;
	}

	public void setExcelBigDecimalFormatAsString(String excelBigDecimalFormat) {
		this.excelBigDecimalFormat = excelBigDecimalFormat;
	}

	public String getExcelStringFormat() {
		return excelStringFormat;
	}

	public void setExcelStringFormat(String excelStringFormat) {
		this.excelStringFormat = excelStringFormat;
	}

	public void setExcelStringFormatAsString(String excelStringFormat) {
		this.excelStringFormat = excelStringFormat;
	}

	public String getXlsJdbcDateFormat() {
		return xlsJdbcDateFormat;
	}

	public void setXlsJdbcDateFormat(String xlsJdbcDateFormat) {
		this.xlsJdbcDateFormat = xlsJdbcDateFormat;
	}

	public void setXlsJdbcDateFormatAsString(String xlsJdbcDateFormat) {
		this.xlsJdbcDateFormat = xlsJdbcDateFormat;
	}

	public String getSmtpEmailFrom() {
		return smtpEmailFrom;
	}

	public void setSmtpEmailFrom(String smtpEmailFrom) {
		this.smtpEmailFrom = smtpEmailFrom;
	}

	public void setSmtpEmailFromAsString(String smtpEmailFrom) {
		this.smtpEmailFrom = smtpEmailFrom;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public void setSmtpHostAsString(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public void setSmtpPortAsString(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public String getSmtpUser() {
		return smtpUser;
	}

	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}

	public void setSmtpUserAsString(String smtpUser) {
		this.smtpUser = smtpUser;
	}

	public String getSmtpPasswd() {
		return smtpPasswd;
	}

	public void setSmtpPasswd(String smtpPasswd) {
		this.smtpPasswd = smtpPasswd;
	}

	public void setSmtpPasswdAsString(String smtpPasswd) {
		this.smtpPasswd = smtpPasswd;
	}

	public String getSmtpSsl() {
		return smtpSsl;
	}

	public void setSmtpSsl(String smtpSsl) {
		this.smtpSsl = smtpSsl;
	}

	public void setSmtpSslAsString(String smtpSsl) {
		this.smtpSsl = smtpSsl;
	}

	public String getTimestampFormatAsString() {
		return timestampFormat;
	}

	public void setTimestampFormat(String timestampFormat) {
		this.timestampFormat = timestampFormat;
	}

	public String getTimeFormatAsString() {
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}

	public String getDateFormatAsString() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getTimestampAsString() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getTimeAsString() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDateAsString() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * Obtains the value of the specified parameter using the java reflection.
	 * 
	 * @param name The name of the ligreto parameter as 'ligreto.*'
	 * @return The parameter value
	 * @throws LigretoException In case of any error
	 */
	public String getParameter(String name) throws LigretoException {
		String fieldName = name.replaceFirst("^ligreto.", "");
		String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1) + "AsString";
		try {
			Method getter = LigretoParameters.class.getMethod(getterName);
			if (getter.getReturnType().isAssignableFrom(String.class)) {
				Object ret = getter.invoke(this);
				if (ret instanceof String) {
					return (String) ret;
				}
			} else {
				throw new LigretoException("Getter function return type is incorrect.");
			}
		} catch (SecurityException e) {
			throw new LigretoException("Security error in obtaining ligreto parameter: " + name, e);
		} catch (NoSuchMethodException e) {
			throw new LigretoException("Unknown ligreto parameter: " + name, e);
		} catch (IllegalArgumentException e) {
			throw new LigretoException("Error obtaining ligreto parameter: " + name, e);
		} catch (IllegalAccessException e) {
			throw new LigretoException("Error obtaining ligreto parameter: " + name, e);
		} catch (InvocationTargetException e) {
			throw new LigretoException("Error obtaining ligreto parameter: " + name, e);
		}
		return null;
	}

	/**
	 * Sets up the value of the specified parameter using the java reflection.
	 * 
	 * @param name The name of the ligreto parameter as 'ligreto.*'
	 * @param value The value of the parameter
	 * @throws LigretoException In case of any error
	 */
	public void setParameter(String name, String value) throws LigretoException {
		String fieldName = name.replaceFirst("^ligreto.", "");
		String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
		try {
			Method setter = LigretoParameters.class.getMethod(setterName, String.class);
			if (setter.getReturnType().equals(Void.TYPE)) {
				setter.invoke(this, value);
			} else {
				throw new LigretoException("Setter function return type is incorrect.");
			}
		} catch (SecurityException e) {
			throw new LigretoException("Security error in setting ligreto parameter: " + name, e);
		} catch (NoSuchMethodException e) {
			throw new LigretoException("Unknown ligreto parameter: " + name, e);
		} catch (IllegalArgumentException e) {
			throw new LigretoException("Error setting ligreto parameter: " + name, e);
		} catch (IllegalAccessException e) {
			throw new LigretoException("Error setting ligreto parameter: " + name, e);
		} catch (InvocationTargetException e) {
			throw new LigretoException("Error setting ligreto parameter: " + name, e);
		}
	}
}
