package com.coreconsulting.res.openehr;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
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
        log.debug("parsed schemaLocation=" + schemaLocation + "");
        String tdsLocation = RegEx.getFirstMatch(schemaLocation, "http:\\/\\/schemas\\.oceanehr\\.com\\/templates ([^\\s]*)");
        log.debug("parsed TDS location=" + tdsLocation + "");
        return tdsLocation;
    }

    public String getTemplateId() {
        if (templateId == null) {
            templateId = getXPathAsString("/*[1]/@template_id");
            log.debug("parsed template_id=" + templateId + " from the TDD");
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

    protected void insertNameAsFirstChild(Document document, Element parent, String text) {
        Node node = parent.getFirstChild();
        while (node instanceof Element == false)
            node = node.getNextSibling();
        insertNameBeforeElement(document, parent, (Element) node, text);
    }

    protected void insertNameBeforeElement(Document document, Element parent, Element sibling, String text) {
        log.debug("inserting name with a value of " + text + " within " + parent.getNodeName() + " before element " + sibling.getNodeName());
        Element name = document.createElement("name");
        parent.insertBefore(name, sibling);
        Element value = document.createElement("value");
        name.appendChild(value);
        value.setTextContent(text);
    }

    public Document toRM() {
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document rm = builder.newDocument();
            Node root = rm.importNode(xml.getDocumentElement(), true);
            rm.appendChild(root);
            log.debug("cloned the TDD object into a RM object");

            transformNode(rm, root, new StringBuilder("/schema[1]/element[1]"));
            log.debug("transformed the TDD into a COMPOSITION");

            log.debug(XML.toString(rm));

            return rm;
        } catch (ParserConfigurationException e) {
            log.error("error creating the DOM builder", e);
            throw new RuntimeException(e);
        }
    }

    protected void transformNode(Document document, Node node, StringBuilder xsdXPath) {
        String nodeId = getTDS().getXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='archetype_node_id" +
                "'][1]/@fixed");
        if (nodeId == null)
            return;

        List<Element> children = XML.getChildElements(node);
        for (Element child : children)
            transformNode(document, child,
                    new StringBuilder("(").append(xsdXPath).append("//element[@name='").append(child.getNodeName()).append("'])[1]"));

        String type = RegEx.getFirstMatch(nodeId, "openEHR\\-\\w+\\-([^\\.]+).*");
        if (type == null)
            type = getTDS().getXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='type'][1]/@fixed");

        log.debug("transforming node=" + node.getNodeName() + " with nodeId=" + nodeId + " and type=" + type + " " +
                "(" + xsdXPath + ")");

        transformLocatable(document, (Element) node, nodeId, type);

        if (type == null) {
            return;
        } else if (type.equals("ACTION")) {
            transformAction(document, (Element) node);
        } else if (type.equals("ACTIVITY")) {
            transformActivity(document, (Element) node);
        } else if (type.equals("ADMIN_ENTRY")) {
            transformAdminEntry(document, (Element) node);
        } else if (type.equals("CLUSTER")) {
            transformClusterOrSection(document, (Element) node);
        } else if (type.equals("COMPOSITION")) {
            transformComposition(document, (Element) node);
        } else if (type.equals("ELEMENT")) {
            transformElement(document, (Element) node, xsdXPath);
        } else if (type.equals("EVALUATION")) {
            transformEvaluation(document, (Element) node);
        } else if (type.equals("INSTRUCTION")) {
            transformInstruction(document, (Element) node);
        } else if (type.equals("INTERVAL_EVENT")) {
            transformIntervalEvent(document, (Element) node);
        } else if (type.equals("ITEM_TREE")) {
            transformItemTree(document, (Element) node);
        } else if (type.equals("OBSERVATION")) {
            transformObservation(document, (Element) node);
        } else if (type.equals("POINT_EVENT")) {
            transformPointEvent(document, (Element) node);
        } else if (type.equals("SECTION")) {
            transformClusterOrSection(document, (Element) node);
        } else {
            throw new RuntimeException("unsupported openEHR RM type " + type);
        }
    }

    protected void transformLocatable(Document document, Element element, String nodeId, String type) {
        log.debug("setting archetype_node_id=" + nodeId + " type=" + type + " for " + element.getNodeName());
        element.setAttribute("archetype_node_id", nodeId);
        if (type != null)
            element.setAttribute("xsi:type", type);

        if (nodeId.startsWith("openEHR-")) {
            log.debug("adding archetype_details");

            List<Element> children = XML.getChildElements(element);
            Node reference = children.get(1);
            for (int i = children.size() - 1; i >= 0; i--) {
                Element child = children.get(i);
                if (child.getNodeName().equals("feeder_audit")) {
                    reference = child;
                    break;
                }
            }

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

    protected void transformAction(Document document, Element element) {
        //TODO
    }

    protected void transformActivity(Document document, Element element) {
        //TODO
    }

    protected void transformAdminEntry(Document document, Element element) {
        //TODO
    }

    protected void transformClusterOrSection(Document document, Element element) {
        List<Element> children = XML.getChildElements(element);
        for (Element child : children) {
            String nodeName = child.getNodeName();
            if (nodeName.equals("name") || nodeName.equals("archetype_details"))
                continue;
            log.debug("renaming " + child.getNodeName() + " to items");
            document.renameNode(child, null, "items");
        }
    }

    protected void transformComposition(Document document, Element element) {
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

    protected void transformElement(Document document, Element element, StringBuilder xsdXPath) {
        String type = getTDS().getXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='valueType'][1]/@fixed");
        List<Element> children = XML.getChildElements(element);
        for (Element child : children) {
            if (child.getNodeName().equals("value")) {
                log.debug("setting ELEMENT valueType to " + type);
                child.setAttribute("type", type);
                break;
            }
        }
    }

    protected void transformEvaluation(Document document, Element element) {
        //TODO
    }

    protected void transformInstruction(Document document, Element element) {
        //TODO
    }

    protected void transformIntervalEvent(Document document, Element element) {
        //TODO
    }

    protected void transformItemTree(Document document, Element element) {
        List<Element> children = XML.getChildElements(element);
        if (children.size() > 0) {
            insertNameBeforeElement(document, element, children.get(0), "ITEM_TREE");
        }
        for (Element child : children) {
            log.debug("renaming " + child.getNodeName() + " to items");
            document.renameNode(child, null, "items");
        }
    }

    protected void transformObservation(Document document, Element element) {
        List<Element> children = XML.getChildElements(element);

        Element data = null;
        for (Element child : children) {
            if (child.getNodeName().equals("data")) {
                data = child;
                break;
            }
        }
        log.debug("setting OBSERVATION data type to HISTORY");
        data.setAttribute("type", "HISTORY");
        insertNameAsFirstChild(document, data, "HISTORY");

        List<Element> dataChildren = XML.getChildElements(data);
        boolean hasOrigin = false;
        for (Element child : dataChildren) {
            if (child.getNodeName().equals("origin")) {
                hasOrigin = true;
            } else {
                document.renameNode(child, null, "events");
            }
        }
        if (hasOrigin == false) {
            Element origin = document.createElement("origin");
            data.insertBefore(origin, dataChildren.get(1));
            Element value = document.createElement("value");
            value.setTextContent(LocalDateTime.now().toString());
            origin.appendChild(value);
        }
    }

    protected void transformPointEvent(Document document, Element element) {
        //TODO
    }

}
