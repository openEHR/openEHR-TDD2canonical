package com.coreconsulting.res.openehr;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.net.URI;
import java.util.List;

@Slf4j
public class TDD extends XML {

    protected TDS tds;
    protected String templateId;

    public TDD(File file) {
        super(file);
    }

    public TDD(String string) {
        super(string);
    }

    public TDD(URI uri) {
        super(uri);
    }

    public String getTDSLocation() {
        String schemaLocation = getXPathAsString("/*[1]/@schemaLocation");
        log.debug("parsed schemaLocation=\"" + schemaLocation + "\"");
        String tdsLocation = RegEx.getFirstMatch(schemaLocation, "http:\\/\\/schemas\\.oceanehr\\.com\\/templates ([^\\s]*)");
        log.debug("parsed TDS location=\"" + tdsLocation + "\"");
        return tdsLocation;
    }

    public String getTemplateId() {
        if (templateId == null) {
            templateId = getXPathAsString("/*[1]/@template_id");
            log.debug("parsed template_id=\"" + templateId + "\" from the TDD");
        }
        return templateId;
    }

    public TDS getTDS() {
        if (tds == null) {
            tds = TDS.fromTemplateId(getTemplateId());
            if (tds != null) {
                log.debug("loaded cached TDS for the TDD");
            } else {
                tds = TDS.fromTDSLocation(getTDSLocation());
                log.debug("loaded remote TDS for the TDD");
            }
        }
        return tds;
    }

    public Document toRM() {
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document rm = builder.newDocument();
            Node root = rm.importNode(xml.getDocumentElement(), true);
            rm.appendChild(root);
            log.debug("cloned the TDD object into a RM object");

            transformNode(rm, root, new StringBuilder("/schema[1]/element[1]"), 0);
            log.debug("transformed the TDD into a COMPOSITION");

            log.debug(XML.toString(rm));

            return rm;
        } catch (ParserConfigurationException e) {
            log.error("error creating the DOM builder", e);
            throw new RuntimeException(e);
        }
    }

    public void transformNode(Document document, Node node, StringBuilder xsdXPath, int depth) {
        if (depth++ > 2) return;
        String nodeId = getTDS().getXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='archetype_node_id" +
                "']/@fixed");
        log.debug("transforming element=\"" + node.getNodeName() + "\" with nodeId=" + nodeId + " (" + xsdXPath + ")");
        if (nodeId == null || nodeId.isEmpty())
            return;

        List<Element> children = XML.getChildElements(node);
        for (Element child : children)
            transformNode(document, child,
                    new StringBuilder(xsdXPath).append("//element[@name='" + child.getNodeName() + "'][1]"), depth);

        String type = getTDS().getXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='type']/@fixed");
        transformLocatable(document, (Element) node, nodeId, type);

        if (nodeId.startsWith("openEHR-")) {
            log.debug("transforming " + type + " " + nodeId);
            if (type.equals("COMPOSITION")) {
                transformComposition(document, (Element) node);
            }
        }
    }

    public void transformComposition(Document document, Element element) {
        log.debug("renaming " + element.getNodeName() + " to composition");
        document.renameNode(element, null, "composition");

        List<Element> children = XML.getChildElements(element);
        for (int i = children.size() - 1; i >= 0; i--) {
            Node child = children.get(i);
            if (child.getNodeName().equals("context"))
                break;
            log.debug("renaming " + child.getNodeName() + " to content");
            document.renameNode(child, null, "content");
        }
    }

    public void transformLocatable(Document document, Element element, String nodeId, String type) {
        log.debug("setting archetype_node_id=\"" + nodeId + "\" type=\"" + type + "\" for " + element.getNodeName());
        element.setAttribute("archetype_node_id", nodeId);
        element.setAttribute("xsi:type", type);

        List<Element> children = XML.getChildElements(element);
        Node reference = children.get(1);
        for (int i = children.size() - 1; i >= 0; i--) {
            Element child = children.get(i);
            if (child.getNodeName().equals("feeder_audit")) {
                reference = child;
                break;
            }
        }

        if (nodeId.startsWith("openEHR-")) {
            log.debug("adding archetype_details");
            Element archetypeDetails = document.createElement("archetype_details");
            element.insertBefore(archetypeDetails, reference);
            Element archetypeId = document.createElement("archetype_id");
            archetypeDetails.appendChild(archetypeId);
            archetypeId.setTextContent(nodeId);
            Element templateId = document.createElement("template_id");
            archetypeDetails.appendChild(templateId);
            templateId.setTextContent(getTemplateId());
            Element rmVersion = document.createElement("rm_version");
            archetypeDetails.appendChild(rmVersion);
            rmVersion.setTextContent("1.0.2");
        }
    }

}
