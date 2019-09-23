package com.coreconsulting.res.openehr;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TDS extends XML {

    static protected Map<String, TDS> cache = new HashMap<String, TDS>();

    static {
        loadCache();
    }

    public TDS(File file) {
        super(file);
    }

    public TDS(String string) {
        super(string);
    }

    public TDS(URI uri) {
        super(uri);
    }

    public static TDS fromTDSLocation(String tdsLocation) {
        try {
            return new TDS(new URI(tdsLocation));
        } catch (URISyntaxException e) {
            log.warn("malformed URI to retrieve the TDS through HTTP(S)");
            return null;
        }
    }

    public static TDS fromTemplateId(String templateId) {
        return cache.get(templateId);
    }

    public static void loadCache() {
        File folder = new File("./src/main/resources/templates");
        for (File template : folder.listFiles()) {
            TDS tds = new TDS(template);
            String templateId = tds.getTemplateId();
            cache.put(templateId, tds);
            log.debug("loaded schema " + template.getName() + " with template_id=\"" + templateId + "\" into cache");
        }
    }

    public String getTemplateId() {
        String templateId = getXPathAsString("//attribute[@name='template_id'][1]/@fixed");
        log.debug("parsed template_id=\"" + templateId + "\" from the TDS");
        return templateId;
    }

}
