/**
 * 
 */
package net.ligreto.config;

import java.io.IOException;
import java.util.HashMap;

import net.ligreto.config.data.DataSourceConfig;
import net.ligreto.config.data.ReportConfig;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class encapsulates all the report configuration read from the report file.
 * 
 * @author Julius Stroffek
 *
 */
public class FileConfig {
	
	
	protected static final String PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
	protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";
	protected static final String NAMESPACE_PREFIXES_FEATURE_ID = "http://xml.org/sax/features/namespace-prefixes";
	protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";
	protected static final String XINCLUDE_FEATURE_ID = "http://apache.org/xml/features/xinclude";
	
	protected HashMap<String, DataSourceConfig> dataSourceMap = new HashMap<String, DataSourceConfig>();
	protected HashMap<String, String> queryMap = new HashMap<String, String>();
	protected HashMap<String, String> paramMap = new HashMap<String, String>();
	protected HashMap<Integer, ReportConfig> reportMap = new HashMap<Integer, ReportConfig>();

	protected FileConfig() {
	}

	public static FileConfig parse(String systemId) throws SAXException, IOException {
		FileConfig fileConfig = new FileConfig();
		SAXContentHandler handler = new SAXContentHandler(fileConfig);
		
		XMLReader parser = XMLReaderFactory.createXMLReader(PARSER_NAME);
		parser.setFeature(NAMESPACES_FEATURE_ID, true);
		parser.setFeature(NAMESPACE_PREFIXES_FEATURE_ID, true);
		parser.setFeature(VALIDATION_FEATURE_ID, true);
		parser.setFeature(XINCLUDE_FEATURE_ID, true);
		parser.setContentHandler(handler);
		parser.setErrorHandler(handler);
		parser.setDTDHandler(handler);		
		parser.parse(new InputSource(systemId));
		
		return fileConfig;
	}
	
	public void addDataSource(DataSourceConfig dataSource) {
		dataSourceMap.put(dataSource.getName(), dataSource);
	}

	public void addQuery(String name, String query) {
		queryMap.put(name, query);
	}

	public void addParam(String name, String query) {
		paramMap.put(name, query);
	}
	
	public void addReport(ReportConfig reportConfig) {
		reportMap.put(reportMap.size(), reportConfig);
	}
	
	public String paramsSubstitue(String string) {
		String result = new String(string);
		for (String name : paramMap.keySet()) {
			result = result.replaceFirst("%%"+name, paramMap.get(name));
		}
		return result;
	}
}
