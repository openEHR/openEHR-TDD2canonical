package com.coreconsulting.res.openehr.tdd2rm;

import com.coreconsulting.res.openehr.tdd2rm.util.FST;
import com.coreconsulting.res.openehr.tdd2rm.util.Properties;
import com.coreconsulting.res.openehr.tdd2rm.util.XML;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Element;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class TDS extends XML implements Serializable {

    protected Map<String, String> nodeCache = new HashMap<>();
    protected String templateId;

    public TDS() {
        super();
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

    public void loadNodeCache() {
        log.trace("loadNodeCache({})", () -> "");
        Element composition = (Element) getXPathAsNodeList("/schema[1]/element[1]").item(0);
        loadElementPaths(composition, new StringBuilder("/schema[1]/element[1]"), new StringBuilder("/schema[1]/element[1]"));
        try {
            File cache = new File(Properties.getProperty(Properties.CACHE_FOLDER) + "/" + getTemplateId());
            FileOutputStream stream = new FileOutputStream(cache);
            FST.write(stream, this);
            log.info("loaded TDS with @template_id={} into cache", () -> getTemplateId());
        } catch (FileNotFoundException e) {
            log.error("failed to write TDS into cache", e);
        }

    }

    public void loadElementPaths(Element element, StringBuilder nodeXPath, StringBuilder xsdXPath) {
        log.trace("loadElementPaths({}, {}, {}, {})", () -> getTemplateId(), () -> element.getNodeName() +
                "[@name='" + element.getAttribute("name") + "']", () -> nodeXPath, () -> xsdXPath);

        List<Element> children = getChildElements(element);
        for (int i = 0; i < children.size(); i++) {
            Element child = children.get(i);
            StringBuilder childNodeXPath = new StringBuilder(nodeXPath).append("/*[" + (i + 1) + "]");
            StringBuilder childXsdXPath;
            if (child.getNodeName().equals("xs:element") == true)
                childXsdXPath =
                        new StringBuilder("(").append(xsdXPath).append("//element[@name='").append(child.getAttribute(
                                "name")).append("'])[1]");
            else
                childXsdXPath = new StringBuilder(xsdXPath);
            loadElementPaths(child, childNodeXPath, childXsdXPath);
        }

        if (element.getNodeName().equals("xs:element") == false)
            return;

        log.debug("loading TDS element with @name={} into cache", () -> element.getAttribute("name"));
        String nodeIdKey = xsdXPath + "/complexType[1]/attribute[@name='archetype_node_id'][1]/@fixed";
        String nodeId = getXPathAsString(nodeIdKey);
        nodeCache.put(nodeIdKey, nodeId);
        log.trace("cached key={}, value={}", () -> nodeIdKey, () -> nodeId);
        String typeKey = xsdXPath + "/complexType[1]/attribute[@name='type'][1]/@fixed";
        String type = getXPathAsString(typeKey);
        nodeCache.put(typeKey, type);
        log.trace("cached key={}, value={}", () -> typeKey, () -> type);
        String valueTypeKey = xsdXPath + "/complexType[1]/attribute[@name='valueType'][1]/@fixed";
        String valueType = getXPathAsString(valueTypeKey);
        nodeCache.put(valueTypeKey, valueType);
        log.trace("cached key={}, value={}", () -> valueTypeKey, () -> valueType);
    }

    public String getCachedXPathAsString(String xpath) {
        log.trace("getCachedXpathAsString({})", () -> xpath);
        if (nodeCache == null || nodeCache.containsKey(xpath) == false) {
            log.warn("failed to lookup {} from the node cache, running XPath online with degraded performance");
            return getXPathAsString(xpath);
        }
        String value = nodeCache.get(xpath);
        return value;
    }

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
