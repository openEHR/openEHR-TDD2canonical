package com.coreconsulting.res.openehr.tdd2canonical;

import com.coreconsulting.res.openehr.tdd2canonical.util.Serializer;
import com.coreconsulting.res.openehr.tdd2canonical.util.Properties;
import com.coreconsulting.res.openehr.tdd2canonical.util.XML;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements functionality related to TDS handling, such as deserializing it from a XML instance, preloading
 * the XPath expressions relevant to {@link TDD} transformation and resolving the @template_id.
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public class TDS extends XML implements Serializable {

    /**
     * Cache of relevant XPath expressions preloaded in this TDS
     */
    protected Map<String, String> nodeCache = new HashMap<>();
    /**
     * {@literal @template_id} attribute. Parsed from the TDS.
     */
    protected String templateId;

    /**
     * Default constructor. Not used, but kept for serialization purposes.
     */
    public TDS() {
        super();
    }

    /**
     * Creates a TDS from a {@link File} object, expected to be a XML document available on the local filesystem.
     *
     * @param file XML document available on the local filesystem
     */
    public TDS(File file) {
        super(file);
        log.trace("TDS({})", () -> file.getAbsolutePath());
    }

    /**
     * Creates a TDS from a {@link String} object, expected to be an in-memory textual representation of the XML
     * document.
     *
     * @param string textual representation of the XML document
     */
    public TDS(String string) {
        super(string);
        log.trace("TDS({})", () -> string);
    }

    /**
     * Creates a TDS from a {@link URI}, expected to be a reachable remote instance of the XML document.
     *
     * @param uri reachable URI to retrieve the XML document
     */
    public TDS(URI uri) {
        super(uri);
        log.trace("TDS({})", () -> uri);
    }

    /**
     * Preloads all relevant XPath expressions from the TDS into {@link #nodeCache}, which acts as an in-memory
     * cache, greatly optimizing node introspection/lookup during {@link TDD} transformation.
     */
    public void loadNodeCache() {
        log.trace("loadNodeCache({})", () -> "");
        // Preload the paths starting from the root node
        Element composition = (Element) getXPathAsNodeList("/schema[1]/element[1]").item(0);
        loadElementPaths(composition, new StringBuilder("/schema[1]/element[1]"), new StringBuilder("/schema[1]/element[1]"));
        try {
            // Serialize the node cache into disk so we don't have to preload it again after GC/restarting
            File cache = new File(Properties.getProperty(Properties.CACHE_FOLDER) + "/" + getTemplateId());
            FileOutputStream stream = new FileOutputStream(cache);
            Serializer.write(stream, this);
            log.info("loaded TDS with @template_id={} into cache", () -> getTemplateId());
        } catch (FileNotFoundException e) {
            log.error("failed to write TDS into cache", e);
        }

    }

    /**
     * Load relevant XPath expressions from an element into {@link #nodeCache}, which acts as an in-memory cache,
     * greatly optimizing node introspection/lookup during {@link TDD} transformation.
     * The algorithm is a postorder tree traversal, so it recursively traverses all descendants using the same path
     * building scheme as the TDD transformation (see {@link TDD#transformNode(Node, StringBuilder)}, so it loads any
     * relevant attributes (@archetype_node_id, @type, @valueType) for every element that can be transformed.
     *
     * @param element current element being traversed
     * @param nodeXPath generic XPath for the current node in the TDS
     * @param xsdXPath XPath for the current node used during TDD transformation to look up a definition in the TDS
     */
    public void loadElementPaths(Element element, StringBuilder nodeXPath, StringBuilder xsdXPath) {
        log.trace("loadElementPaths({}, {}, {}, {})", () -> getTemplateId(), () -> element.getNodeName() +
                "[@name='" + element.getAttribute("name") + "']", () -> nodeXPath, () -> xsdXPath);

        // Postorder tree traversal
        List<Element> children = getChildElements(element);
        for (int i = 0; i < children.size(); i++) {
            Element child = children.get(i);
            // Due to the TDS being more complex, we maintain a dedicated/more generic recursive path tracking
            StringBuilder childNodeXPath = new StringBuilder(nodeXPath).append("/*[" + (i + 1) + "]");
            StringBuilder childXsdXPath;
            if (child.getNodeName().equals("xs:element") == true) {
                // If we reach an element definition, we append it to the path to be used during TDD transformation
                childXsdXPath =
                        new StringBuilder("(").append(xsdXPath).append("//element[@name='").append(child.getAttribute(
                                "name")).append("'])[1]");
            } else {
                // Otherwise, non-element nodes in the TDS don't affect the path
                childXsdXPath = new StringBuilder(xsdXPath);
            }
            loadElementPaths(child, childNodeXPath, childXsdXPath);
        }

        // Non-element nodes are not LOCATABLE, so no need to cache
        if (element.getNodeName().equals("xs:element") == false)
            return;

        log.debug("loading TDS element with @name={} into cache", () -> element.getAttribute("name"));
        // Cache @archetype_node_id
        String nodeIdKey = xsdXPath + "/complexType[1]/attribute[@name='archetype_node_id'][1]/@fixed";
        String nodeId = getXPathAsString(nodeIdKey);
        nodeCache.put(nodeIdKey, nodeId);
        log.trace("cached key={}, value={}", () -> nodeIdKey, () -> nodeId);
        // Cache @type
        String typeKey = xsdXPath + "/complexType[1]/attribute[@name='type'][1]/@fixed";
        String type = getXPathAsString(typeKey);
        nodeCache.put(typeKey, type);
        log.trace("cached key={}, value={}", () -> typeKey, () -> type);
        // Cache @valueType
        String valueTypeKey = xsdXPath + "/complexType[1]/attribute[@name='valueType'][1]/@fixed";
        String valueType = getXPathAsString(valueTypeKey);
        nodeCache.put(valueTypeKey, valueType);
        log.trace("cached key={}, value={}", () -> valueTypeKey, () -> valueType);
    }

    /**
     * Runs a XPath expression on the TDS. Attempts to get the result from the {@link #nodeCache}, in which it should
     * be preloaded, but evaluates it with a performance warning otherwise.
     *
     * @param xpath XPath expression to be evaluated
     * @return the result of the XPath evaluation, either from the {@link #nodeCache} or evaluted online
     */
    public String getCachedXPathAsString(String xpath) {
        log.trace("getCachedXpathAsString({})", () -> xpath);
        if (nodeCache == null || nodeCache.containsKey(xpath) == false) {
            log.warn("failed to lookup {} from the node cache, running XPath online with degraded performance");
            return getXPathAsString(xpath);
        }
        String value = nodeCache.get(xpath);
        return value;
    }

    /**
     * Parses the @template_id from the TDS attribute definition. Once parsed, it is stored as a field for future use.
     *
     * @return {@literal @template_id} from the TDD root element
     */
    public String getTemplateId() {
        log.trace("getTemplateId({})", () -> "");
        if (templateId == null) {
            templateId = getXPathAsString("/schema[1]/element[1]/complexType[1]/attribute[@name='template_id" +
                    "'][1]/@fixed");
            log.debug("parsed template_id={} from the TDS", () -> templateId);
        }
        return templateId;
    }

}
