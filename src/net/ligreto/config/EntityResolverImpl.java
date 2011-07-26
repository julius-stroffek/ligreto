package net.ligreto.config;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class EntityResolverImpl implements EntityResolver {

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		if ("ligreto.dtd".equals(publicId)) {
			EntityResolverImpl.class.getClassLoader();
			return new InputSource(ClassLoader.getSystemResourceAsStream("ligreto.dtd"));
		}
		return null;
	}

}
