package com.coreconsulting.res.openehr;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class TDS extends XML {

    static protected Map<String, TDS> cache = new HashMap<String, TDS>();

    static {
        loadCache();
    }

    public TDS(File file) {
        super(file);
        log.trace("TDS({})", () -> file.getAbsolutePath());
    }

    public TDS(String string) {
        super(string);
        log.trace("TDS({})", () -> string);
    }

    public TDS(URI uri) {
        super(uri);
        log.trace("TDS({})", () -> uri);
    }

    public static TDS fromTDSLocation(String tdsLocation) {
        log.trace("fromTDSLocation({})", () -> tdsLocation);
        try {
            TDS tds = new TDS(new URI(tdsLocation));
            return tds;
        } catch (URISyntaxException e) {
            log.warn("malformed URI to retrieve the TDS through HTTP(S)");
            return null;
        }
    }

    public static TDS fromTemplateId(String templateId) {
        log.trace("fromTemplateId({})", () -> templateId);
        TDS tds = cache.get(templateId);
        return tds;
    }

    public static void loadCache() {
        log.trace("loadCache({})", () -> "");
        File folder = new File("./src/main/resources/templates");
        for (File template : folder.listFiles()) {
            TDS tds = new TDS(template);
            String templateId = tds.getTemplateId();
            cache.put(templateId, tds);
            log.info("loaded schema {} with @template_id = {} into  cache", () -> template.getName(), () -> templateId);
        }
    }

    public String getTemplateId() {
        log.trace("getTemplateId({})", () -> "");
        String templateId = getXPathAsString("//attribute[@name='template_id'][1]/@fixed");
        return templateId;
    }

}
