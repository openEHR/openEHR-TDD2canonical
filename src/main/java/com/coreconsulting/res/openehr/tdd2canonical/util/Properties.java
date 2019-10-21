package com.coreconsulting.res.openehr.tdd2canonical.util;

import lombok.extern.log4j.Log4j2;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class reads properties from a XML file and serves them for resource introspection (i.e. resource paths).
 * The default properties file is "./src/main/resources/properties.xml", but it can be overridden through the system
 * property "properties".
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public class Properties {

    /**
     * The folder where TDS instances are cached.
     */
    public static String CACHE_FOLDER = "CACHE_FOLDER";
    /**
     * The folder where the TDS files shipped with the application are stored.
     */
    public static String TEMPLATE_FOLDER = "TEMPLATE_FOLDER";

    /**
     * Static reference to the properties object.
     */
    private static java.util.Properties properties;

    /**
     * Retrieves a named property mapped from the property file (either default or overriden).
     *
     * @param key name of the property which value is to be retrieved
     * @return value of the the property
     */
    public static String getProperty(String key) {
        log.trace("getProperty({})", () -> key);
        // Initializes the properties object once, when needed
        if (properties == null) {
            properties = new java.util.Properties();
            try {
                // If a custom properties file has been assigned, use it
                String path = System.getProperty("properties");
                if (path == null) {
                    // Otherwise use the default one
                    path = "./src/main/resources/properties.xml";
                }
                properties.loadFromXML(new FileInputStream(path));
            } catch (IOException e) {
                log.error("failed to read properties file", e);
            }
        }
        return properties.getProperty(key);
    }

}
