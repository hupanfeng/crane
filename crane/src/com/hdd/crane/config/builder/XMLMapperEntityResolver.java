package com.hdd.crane.config.builder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hdd.crane.io.Resources;

public class XMLMapperEntityResolver implements EntityResolver {

    private static final Map<String, String> doctypeMap = new HashMap<String, String>();

    private static final String CRANE_CONFIG_DOCTYPE = "-//hdd.com//DTD Config 1.0//EN".toUpperCase(Locale.ENGLISH);
    private static final String CRANE_CONFIG_URL = "http://hdd.com/dtd/crane-config.dtd".toUpperCase(Locale.ENGLISH);

    private static final String CRANE_CONFIG_DTD = "com/hdd/crane/config/builder/crane-config.dtd";

    static {
        doctypeMap.put(CRANE_CONFIG_URL, CRANE_CONFIG_DTD);
        doctypeMap.put(CRANE_CONFIG_DOCTYPE, CRANE_CONFIG_DTD);
    }

    /**
     * Converts a public DTD into a local one
     * 
     * @param publicId
     *            Unused but required by EntityResolver interface
     * 
     * @param systemId
     *            The DTD that is being requested
     * 
     * @return The InputSource for the DTD
     * 
     * @throws org.xml.sax.SAXException
     *             If anything goes wrong
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {

        if (publicId != null) {
            publicId = publicId.toUpperCase(Locale.ENGLISH);
        }
        if (systemId != null) {
            systemId = systemId.toUpperCase(Locale.ENGLISH);
        }

        InputSource source = null;
        try {
            String path = doctypeMap.get(publicId);
            source = getInputSource(path, source);
            if (source == null) {
                path = doctypeMap.get(systemId);
                source = getInputSource(path, source);
            }
        } catch (Exception e) {
            throw new SAXException(e.toString());
        }
        return source;
    }

    private InputSource getInputSource(String path, InputSource source) {
        if (path != null) {
            InputStream in;
            try {
                in = Resources.getResourceAsStream(path);
                source = new InputSource(in);
            } catch (IOException e) {
                // ignore, null is ok
            }
        }
        return source;
    }

}