package com.coreconsulting.res.openehr;

import lombok.extern.log4j.Log4j2;
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

@Log4j2
public class TDD extends XML {

    protected TDS tds;
    protected String templateId;

    public TDD(File file) {
        super(file);
        log.trace("TDD({})", () -> file.getAbsolutePath());
    }

    public TDD(String string) {
        super(string);
        log.trace("TDD({})", () -> string);
    }

    public TDD(URI uri) {
        super(uri);
        log.trace("TDD({})", () -> uri);
    }

    public String getTDSLocation() {
        log.trace("getTDSLocation({})", () -> "");
        String schemaLocation = getXPathAsString("/*[1]/@schemaLocation");
        log.debug("parsed schemaLocation={}", () -> schemaLocation);
        String tdsLocation = RegEx.getFirstMatch(schemaLocation, "http:\\/\\/schemas\\.oceanehr\\.com\\/templates ([^\\s]*)");
        log.debug("parsed TDS location={}", () -> tdsLocation);
        return tdsLocation;
    }

    public String getTemplateId() {
        log.trace("getTemplateId({})", () -> "");
        if (templateId == null) {
            templateId = getXPathAsString("/*[1]/@template_id");
            log.debug("parsed template_id={} from the TDD", () -> templateId);
        }
        return templateId;
    }

    public TDS getTDS() {
        log.trace("getTDS({})", () -> "");
        if (tds == null) {
            tds = TDS.fromTemplateId(getTemplateId());
            if (tds != null) {
                log.debug("{}", () -> "loaded cached TDS for the TDD");
            } else {
                tds = TDS.fromTDSLocation(getTDSLocation());
                log.debug("{}", () -> "loaded remote TDS for the TDD");
            }
        }
        return tds;
    }

    protected void insertNameAsFirstChild(Element parent, String text) {
        log.trace("insertNameAsFirstChild({}, {})", () -> parent.getNodeName(), () -> text);
        Node node = parent.getFirstChild();
        while (node instanceof Element == false)
            node = node.getNextSibling();
        insertNameBeforeElement(parent, (Element) node, text);
    }

    protected void insertNameBeforeElement(Element parent, Element sibling, String text) {
        log.trace("insertNameBeforeElement({}, {})", () -> parent.getNodeName(), () -> text);
        Document document = parent.getOwnerDocument();
        Element name = document.createElement("name");
        parent.insertBefore(name, sibling);
        Element value = document.createElement("value");
        name.appendChild(value);
        value.setTextContent(text);
    }

    public Document toRM() {
        log.trace("toRM({})", () -> "");
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document rm = builder.newDocument();
            Node root = rm.importNode(xml.getDocumentElement(), true);
            rm.appendChild(root);
            log.debug("{}", () -> "cloned the TDD object into a RM object");

            transformNode(root, new StringBuilder("/schema[1]/element[1]"));
            log.debug("{}", () -> "transformed the TDD into a COMPOSITION");
            
            return rm;
        } catch (ParserConfigurationException e) {
            log.error("error creating the DOM builder", e);
            throw new RuntimeException(e);
        }
    }

    protected void transformNode(Node node, StringBuilder xsdXPath) {
        log.trace("transformNode({}, {})", () -> node.getNodeName(), () -> xsdXPath);
        String nodeId = getTDS().getXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='archetype_node_id" +
                "'][1]/@fixed");
        if (nodeId == null)
            return;

        List<Element> children = XML.getChildElements(node);
        for (Element child : children)
            transformNode(child,
                    new StringBuilder("(").append(xsdXPath).append("//element[@name='").append(child.getNodeName()).append("'])[1]"));

        String type = RegEx.getFirstMatch(nodeId, "openEHR\\-\\w+\\-([^\\.]+).*");
        if (type == null)
            type = getTDS().getXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='type'][1]/@fixed");

        String _type = type;
        log.debug("transforming node = {} [@nodeId = {}, @type = {}", () -> node.getNodeName(), () -> nodeId,
                () -> _type);

        transformLocatable((Element) node, nodeId, type);

        if (type == null) {
            return;
        } else if (type.equals("ACTION")) {
            transformAction((Element) node);
        } else if (type.equals("ACTIVITY")) {
            transformActivity((Element) node);
        } else if (type.equals("ADMIN_ENTRY")) {
            transformAdminEntry((Element) node);
        } else if (type.equals("CLUSTER")) {
            transformClusterOrSection((Element) node);
        } else if (type.equals("COMPOSITION")) {
            transformComposition((Element) node);
        } else if (type.equals("ELEMENT")) {
            transformElement((Element) node, xsdXPath);
        } else if (type.equals("EVALUATION")) {
            transformEvaluation((Element) node);
        } else if (type.equals("INSTRUCTION")) {
            transformInstruction((Element) node);
        } else if (type.equals("INTERVAL_EVENT")) {
            transformIntervalEvent((Element) node);
        } else if (type.equals("ITEM_TREE")) {
            transformItemTree((Element) node);
        } else if (type.equals("OBSERVATION")) {
            transformObservation((Element) node);
        } else if (type.equals("POINT_EVENT")) {
            transformPointEvent((Element) node);
        } else if (type.equals("SECTION")) {
            transformClusterOrSection((Element) node);
        } else {
            log.error("unsupported type = {}", () -> _type);
            throw new RuntimeException("unsupported type " + type);
        }
    }

    protected void transformLocatable(Element element, String nodeId, String type) {
        log.trace("transformLocatable({}, {}, {})", () -> element.getNodeName(), () -> nodeId, () -> type);
        log.debug("setting @archetype_node_id = {} and @type = {} for {}", () -> nodeId, () -> type,
                () -> element.getNodeName());
        element.setAttribute("archetype_node_id", nodeId);
        if (type != null)
            element.setAttribute("xsi:type", type);

        if (nodeId.startsWith("openEHR-")) {
            List<Element> children = XML.getChildElements(element);
            Element reference = children.get(1);
            for (int i = children.size() - 1; i >= 0; i--) {
                Element child = children.get(i);
                if (child.getNodeName().equals("feeder_audit")) {
                    reference = child;
                    break;
                }
            }

            Element _reference = reference;
            log.debug("adding child archetype_details before {}", () -> _reference.getNodeName());
            Document document = element.getOwnerDocument();
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

    protected void transformAction(Element element) {
        log.trace("transformAction({})", () -> element.getNodeName());
        // não foram necessárias transformações especializadas até o momento
    }

    protected void transformActivity(Element element) {
        log.trace("transformActivity({})", () -> element.getNodeName());
        Document document = element.getOwnerDocument();
        document.renameNode(element, null, "activities");

        List<Element> children = XML.getChildElements(element);
        log.debug("{}", () -> "reversing children timing and description");
        element.removeChild(children.get(1));
        element.insertBefore(children.get(1), children.get(3));
    }

    protected void transformAdminEntry(Element element) {
        log.trace("transformAdminEntry({})", () -> element.getNodeName());
        // não foram necessárias transformações especializadas até o momento
    }

    protected void transformClusterOrSection(Element element) {
        log.trace("transformClusterOrSection({})", () -> element.getNodeName());
        List<Element> children = XML.getChildElements(element);
        Document document = element.getOwnerDocument();
        for (Element child : children) {
            String nodeName = child.getNodeName();
            if (nodeName.equals("name") || nodeName.equals("archetype_details"))
                continue;
            log.debug("renaming {} to items", () -> child.getNodeName());
            document.renameNode(child, null, "items");
        }
    }

    protected void transformComposition(Element element) {
        log.trace("transformComposition({})", () -> element.getNodeName());
        log.debug("renaming {} to composition", () -> element.getNodeName());
        Document document = element.getOwnerDocument();
        document.renameNode(element, null, "composition");

        List<Element> children = XML.getChildElements(element);
        for (int i = children.size() - 1; i >= 0; i--) {
            Node child = children.get(i);
            if (child.getNodeName().equals("context"))
                break;
            log.debug("renaming {} to content", () -> child.getNodeName());
            document.renameNode(child, null, "content");
        }
    }

    protected void transformElement(Element element, StringBuilder xsdXPath) {
        log.trace("transformElement({}, {})", element.getNodeName(), xsdXPath);
        String type = getTDS().getXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='valueType'][1]/@fixed");
        List<Element> children = XML.getChildElements(element);
        for (Element child : children) {
            if (child.getNodeName().equals("value")) {
                log.debug("setting @type to {}", () -> type);
                child.setAttribute("xsi:type", type);
                break;
            }
        }
    }

    protected void transformEvaluation(Element element) {
        log.trace("transformEvaluation({})", () -> element.getNodeName());
        // não foram necessárias transformações especializadas até o momento
    }

    protected void transformInstruction(Element element) {
        log.trace("transformInstruction({})", () -> element.getNodeName());
        // não foram necessárias transformações especializadas até o momento
    }

    protected void transformIntervalEvent(Element element) {
        log.trace("transformIntervalEvent({})", () -> element.getNodeName());
        // não foram necessárias transformações especializadas até o momento
    }

    protected void transformItemTree(Element element) {
        log.trace("transformItemTree({})", () -> element.getNodeName());
        // não foram necessárias transformações especializadas até o momento
        List<Element> children = XML.getChildElements(element);
        if (children.size() > 0) {
            log.debug("adding child name[value=ITEM_TREE] before {}", () -> children.get(0).getNodeName());
            insertNameBeforeElement(element, children.get(0), "ITEM_TREE");
        }
        Document document = element.getOwnerDocument();
        for (Element child : children) {
            log.debug("renaming {} to items", () -> child.getNodeName());
            document.renameNode(child, null, "items");
        }
    }

    protected void transformObservation(Element element) {
        log.trace("transformObservation({})", () -> element.getNodeName());
        List<Element> children = XML.getChildElements(element);

        Element data = null;
        for (Element child : children) {
            if (child.getNodeName().equals("data")) {
                data = child;
                break;
            }
        }
        log.debug("setting @type to {}", () -> "HISTORY");
        data.setAttribute("type", "HISTORY");
        insertNameAsFirstChild(data, "HISTORY");

        Document document = element.getOwnerDocument();
        List<Element> dataChildren = XML.getChildElements(data);
        boolean hasOrigin = false;
        for (Element child : dataChildren) {
            if (child.getNodeName().equals("name")) {
                continue;
            } else if (child.getNodeName().equals("origin")) {
                hasOrigin = true;
            } else {
                log.debug("renaming {} to events", () -> child.getNodeName());
                document.renameNode(child, null, "events");
            }
        }
        if (hasOrigin == false) {
            log.debug("adding child origin[value=now() before {}", () -> dataChildren.get(1).getNodeName());
            Element origin = document.createElement("origin");
            data.insertBefore(origin, dataChildren.get(1));
            Element value = document.createElement("value");
            value.setTextContent(LocalDateTime.now().toString());
            origin.appendChild(value);
        }
    }

    protected void transformPointEvent(Element element) {
        log.trace("transformPointEvent({})", () -> element.getNodeName());
        Element name = null;
        for (Element child : XML.getChildElements(element)) {
            if (child.getNodeName().equals("name")) {
                name = child;
                break;
            }
        }
        for (Element child : XML.getChildElements(name)) {
            if (child.getNodeName().equals("value")) {
                log.debug("renaming name/value from {} to ANY_EVENT", () -> child.getTextContent());
                child.setTextContent("ANY_EVENT");
                break;
            }
        }
    }

}
