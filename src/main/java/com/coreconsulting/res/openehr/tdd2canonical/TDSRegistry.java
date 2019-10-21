package com.coreconsulting.res.openehr.tdd2canonical;

import com.coreconsulting.res.openehr.tdd2canonical.util.Serializer;
import com.coreconsulting.res.openehr.tdd2canonical.util.Properties;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements functionality related to caching TDS instances, reading/writing from/into the cache,
 * obtaining remote objects and preloading paths as necessary.
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public class TDSRegistry {

    /**
     * Cache of TDS instances preloaded into the registry
     */
    static protected Map<String, TDS> templateCache = new HashMap<>();

    /**
     * Retrieves a TDS instance from a remote location, checks if the same @template_id has a cache and returns it.
     * Otherwise, it returns the a fresh instance and starts preloading the paths in a new thread. XPath evaluations
     * on the TDS might be executed online if they advance faster than the thread but they won't block the execution.
     *
     * @param tdsLocation reachable {@link URI} to retrieve the TDS
     * @return TDS instance from cache, if @template_id was previously cached, or TDS instance preloading the cache
     * in a separate thread
     */
    public static TDS fromTDSLocation(String tdsLocation) {
        log.trace("fromTDSLocation({})", () -> tdsLocation);
        try {
            // Obtain the TDS from the remote location and return it if the @template_id was previously cached
            TDS tds = new TDS(new URI(tdsLocation));
            TDS cached = fromCache(tds.getTemplateId());
            if (cached == null) {
                // Start preloading in a separate thread and return the fresh instance
                new Thread(() -> tds.loadNodeCache()).start();
                return tds;
            } else
                return cached;
        } catch (URISyntaxException e) {
            log.warn("malformed URI to retrieve the TDS through HTTP(S)", e);
            return null;
        }
    }

    /**
     * Retrieves a TDS instance based on the @template_id. If it was not cached before, it will attempt to read from
     * a local folder, by default /src/main/resources/templates, using a file name mapped from the @template_id.
     * Configuration (both the local folder and @template_id to file name mappings) can be overridden (see
     * {@link Properties}).
     * It can be assumed that templates shipped with the application can have a cold deploy, so preloading the paths
     * happen on the same thread.
     *
     * @param templateId the @template_id to look up for the TDS
     * @return the TDS instance, either from the cache or parsed from the local folder
     */
    public static TDS fromTemplateId(String templateId) {
        log.trace("fromTemplateId({})", () -> templateId);
        TDS tds = fromCache(templateId);
        if (tds == null) {
            // Obtain the TDS from the local folder, mapping the @template_id to a file name through properties
            File xsd =
                    new File(Properties.getProperty(Properties.TEMPLATE_FOLDER) + "/" + Properties.getProperty(templateId));
            tds = new TDS(xsd);
            tds.loadNodeCache();
        }
        return tds;
    }

    /**
     * Retrieves a TDS instance from the cache based on the @template_id.
     *
     * @param templateId the @template_id to look up for the TDS
     * @return the TDS instance, if it was previously cached, or null otherwise
     */
    protected static TDS fromCache(String templateId) {
        File cache = new File(Properties.getProperty(Properties.CACHE_FOLDER) + "/" + templateId);
        try {
            FileInputStream stream = new FileInputStream(cache);
            TDS tds = (TDS) Serializer.read(stream);
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
