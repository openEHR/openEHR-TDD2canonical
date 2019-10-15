package com.coreconsulting.res.openehr.tdd2rm.util;

import lombok.extern.log4j.Log4j2;

import java.io.FileInputStream;
import java.io.IOException;

@Log4j2
public class Properties {

    public static String CACHE_FOLDER = "CACHE_FOLDER";
    public static String TEMPLATE_FOLDER = "TEMPLATE_FOLDER";

    private static java.util.Properties properties;

    public static String getProperty(String key) {
        log.trace("getProperty({})", () -> key);
        if (properties == null) {
            properties = new java.util.Properties();
            try {
                String path = System.getProperty("properties");
                if (path == null)
                    path = "./src/main/resources/properties.xml";
                properties.loadFromXML(new FileInputStream(path));
            } catch (IOException e) {
                log.error("failed to read properties file", e);
            }
        }
        return properties.getProperty(key);
    }

}
