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

            transformNode(rm, root, 0, new StringBuilder("/schema[1]/element[1]"));
            log.debug("transformed the TDD into a COMPOSITION");

            log.debug("RM: " + toString(rm));

            return rm;
        } catch (ParserConfigurationException e) {
            log.error("error creating the DOM builder", e);
            throw new RuntimeException(e);
        }
    }

    public void transformNode(Document document, Node node, int siblingIndex, StringBuilder xsdXPath) {
        if (node.getNodeType() != Node.ELEMENT_NODE)
            return;
        log.debug("transforming node name=\"" + node.getNodeName() + "\"");

        String nodeId = getTDS().getXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='archetype_node_id']/@fixed");
        if (nodeId != null && nodeId.startsWith("openEHR-")) {
            log.debug("transforming archetype " + nodeId);
            String type = RegEx.getFirstMatch(nodeId,"openEHR\\-\\w+\\-([\\w]+)\\..*");
            if (type.equals("COMPOSITION")) {
                transformComposition(document, (Element) node, siblingIndex, xsdXPath);
            }
        }

        //TODO

        NodeList nodes = node.getChildNodes();
        log.debug("descending into " + nodes.getLength() + " children");
        for (int i = 0; i < nodes.getLength(); i++)
            transformNode(document, nodes.item(i), i,
                    xsdXPath.append("/complexType[1]/sequence[1]/element[" + (i + 1)  + "]"));
    }

    public void transformComposition(Document document, Element element, int siblingIndex, StringBuilder xsdXPath) {
        document.renameNode(element, null, "composition");
        transformLocatable(document, element, xsdXPath);

        NodeList children = element.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE)
                continue;
            else if (child.getNodeName().equals("context"))
                break;
            document.renameNode(child, null, "content");
        }
    }

    public void transformLocatable(Document document, Element element, StringBuilder xsdXPath) {
        String nodeId = getTDS().getXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='archetype_node_id']/@fixed");
        element.setAttribute("archetype_node_id", nodeId);

        NodeList children = element.getChildNodes();
        Node language = null;
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE)
                continue;
            else if (child.getNodeName().equals("language")) {
                language = child;
                break;
            }
        }
        Element archetypeDetails = document.createElement("archetype_details");
        element.insertBefore(archetypeDetails, language);
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
