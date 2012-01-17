package net.ligreto.util;

import java.util.Map;
import java.util.Properties;

public class Parameters {
	public static String substituteParams(Map<String, String> parametersMap, String string) {
		String oResult;
		String result = new String(string);
		do {
			oResult = result;
			for (String name : parametersMap.keySet()) {
				result = result.replaceAll(
					"\\u0024\\u007B" + name + "\\u007D",
					parametersMap.get(name).replaceAll("\\$", "\\\\\\$")
				);
			}
		} while (!result.equals(oResult));
		return result;	
	}
	
	public static Properties substituteParams(Map<String, String> parametersMap, Properties properties) {
		Properties rValue = new Properties();
		for (Object key : properties.keySet()) {
			if (key instanceof String) {
				Object value = properties.get(key);
				if (value instanceof String) {
					rValue.put((String)key, substituteParams(parametersMap, (String)value));
				} else {
					throw new RuntimeException("Unexpected data type for property value: " + key);
				}
			} else {
				throw new RuntimeException("Unexpected data type for property key.");
			}
		}
		return rValue;
	}
}
