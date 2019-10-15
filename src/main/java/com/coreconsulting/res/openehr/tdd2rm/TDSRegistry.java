package com.coreconsulting.res.openehr.tdd2rm;

import com.coreconsulting.res.openehr.tdd2rm.util.FST;
import com.coreconsulting.res.openehr.tdd2rm.util.Properties;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class TDSRegistry {

    static protected Map<String, TDS> templateCache = new HashMap<>();

    public static TDS fromTDSLocation(String tdsLocation) {
        log.trace("fromTDSLocation({})", () -> tdsLocation);
        try {
            TDS tds = new TDS(new URI(tdsLocation));
            TDS cached = fromCache(tds.getTemplateId());
            if (cached == null) {
                new Thread(() -> tds.loadNodeCache()).start();
                return tds;
            } else
                return cached;
        } catch (URISyntaxException e) {
            log.warn("malformed URI to retrieve the TDS through HTTP(S)", e);
            return null;
        }
    }

    public static TDS fromTemplateId(String templateId) {
        log.trace("fromTemplateId({})", () -> templateId);
        TDS tds = fromCache(templateId);
        if (tds == null) {
            File xsd =
                    new File(Properties.getProperty(Properties.TEMPLATE_FOLDER) + "/" + Properties.getProperty(templateId));
            tds = new TDS(xsd);
            tds.loadNodeCache();
        }
        return tds;
    }

    protected static TDS fromCache(String templateId) {
        File cache = new File(Properties.getProperty(Properties.CACHE_FOLDER) + "/" + templateId);
        try {
            FileInputStream stream = new FileInputStream(cache);
            TDS tds = (TDS) FST.read(stream);
            log.info("loaded TDS with @template_id={} from cache", () -> tds.getTemplateId());
            stream.close();
            return tds;
        } catch (FileNotFoundException e) {
            log.info("failed to load TDS from cache", e);
            return null;
        } catch (IOException e) {
            log.warn("failed to close input stream for cache", e);
            return null;
        }
    }

}
