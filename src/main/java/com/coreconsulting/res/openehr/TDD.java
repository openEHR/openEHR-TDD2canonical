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

    public static final String OPENEHR_NS = "http://schemas.openehr.org/v1";
    public static final String OPENEHR_NS_LOCATION = "https://specifications.openehr.org/releases/1.0/its/XML-schema/Composition.xsd";
    public static final String OPENEHR_XSI_LOCATION = OPENEHR_NS + " " + OPENEHR_NS_LOCATION;

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
        log.debug("transforming node = {} [@nodeId = {}, @type = {}]", () -> node.getNodeName(), () -> nodeId,
                () -> _type);

        Element element = (Element) node;
        transformLocatable(element, nodeId, type);

        if (type == null) {
            return;
        } else if (type.equals("ACTION")) {
            transformAction(element);
        } else if (type.equals("ACTIVITY")) {
            transformActivity(element);
        } else if (type.equals("ADMIN_ENTRY")) {
            transformAdminEntry(element);
        } else if (type.equals("CLUSTER")) {
            transformClusterOrSection(element);
        } else if (type.equals("COMPOSITION")) {
            transformComposition(element);
            transformNamespaces(element);
        } else if (type.equals("ELEMENT")) {
            transformElement(element, xsdXPath);
        } else if (type.equals("EVALUATION")) {
            transformEvaluation(element);
        } else if (type.equals("INSTRUCTION")) {
            transformInstruction(element);
        } else if (type.equals("INTERVAL_EVENT")) {
            transformIntervalEvent(element);
        } else if (type.equals("ITEM_TREE")) {
            transformItemTree(element);
        } else if (type.equals("OBSERVATION")) {
            transformObservation(element);
        } else if (type.equals("POINT_EVENT")) {
            transformPointEvent(element);
        } else if (type.equals("SECTION")) {
            transformClusterOrSection(element);
        } else {
            log.error("unsupported type = {}", () -> _type);
            throw new RuntimeException("unsupported type " + type);
        }
    }

    protected void transformLocatable(Element element, String nodeId, String type) {
        log.trace("transformLocatable({}, {}, {})", () -> element.getNodeName(), () -> nodeId, () -> type);
        log.trace("setting @archetype_node_id = {} and @type = {} for {}", () -> nodeId, () -> type,
                () -> element.getNodeName());
        element.setAttribute("archetype_node_id", nodeId);
        if (type != null)
            element.setAttribute("xsi:type", "oe:" + type);

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
            log.trace("adding child archetype_details before {}", () -> _reference.getNodeName());
            Document document = element.getOwnerDocument();
            Element archetypeDetails = document.createElement("archetype_details");
            element.insertBefore(archetypeDetails, reference);
            Element archetypeId = document.createElement("archetype_id");
            archetypeDetails.appendChild(archetypeId);
            Element archetypeIdValue = document.createElement("value");
            archetypeId.appendChild(archetypeIdValue);
            archetypeIdValue.setTextContent(nodeId);
            Element templateId = document.createElement("template_id");
            archetypeDetails.appendChild(templateId);
            Element templateIdValue = document.createElement("value");
            templateId.appendChild(templateIdValue);
            templateIdValue.setTextContent(getTemplateId());
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
        log.trace("{}", () -> "reversing ACTIVITY children timing and description");
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
            log.trace("renaming {} to items", () -> child.getNodeName());
            document.renameNode(child, null, "items");
        }
    }

    protected void transformComposition(Element element) {
        log.trace("transformComposition({})", () -> element.getNodeName());
        log.trace("renaming {} to composition", () -> element.getNodeName());
        Document document = element.getOwnerDocument();
        document.renameNode(element, null, "composition");

        List<Element> children = XML.getChildElements(element);
        for (int i = children.size() - 1; i >= 0; i--) {
            Node child = children.get(i);
            if (child.getNodeName().equals("context"))
                break;
            log.trace("renaming {} to content", () -> child.getNodeName());
            document.renameNode(child, null, "content");
        }
    }

    protected void transformElement(Element element, StringBuilder xsdXPath) {
        log.trace("transformElement({}, {})", element.getNodeName(), xsdXPath);
        String type = getTDS().getXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='valueType'][1]/@fixed");
        List<Element> children = XML.getChildElements(element);
        for (Element child : children) {
            if (child.getNodeName().equals("name")) {
                log.trace("removing {} children other than {}", () -> "name", () -> "value");
                List<Element> nameChildren = XML.getChildElements(child);
                for (int i = 1; i < nameChildren.size(); i++)
                    child.removeChild(nameChildren.get(i));
            }
            if (child.getNodeName().equals("value")) {
                log.trace("setting @type to {}", () -> type);
                child.setAttribute("xsi:type", "oe:" + type);
                if (type.equals("DV_PROPORTION")) {
                    log.trace("inferring {} denominator from type", () -> type);
                    List<Element> proportionChildren = XML.getChildElements(child);
                    Element proportionType = proportionChildren.get(1);
                    if (proportionType.getNodeName().equals("type")) {
                        Element denominator = element.getOwnerDocument().createElement("denominator");
                        child.insertBefore(denominator, proportionType);
                        if (proportionType.getTextContent().equals("1")) {
                            denominator.setTextContent("1");
                        } else if (proportionType.getTextContent().equals("2")) {
                            denominator.setTextContent("100");
                        }
                    }
                } else if (type.equals("DV_QUANTITY")) {
                    log.trace("reversing {} children precision and units", () -> type);
                    List<Element> quantityChildren = XML.getChildElements(child);
                    if (quantityChildren.size() > 2) {
                        Node units = child.removeChild(quantityChildren.get(quantityChildren.size() - 1));
                        child.insertBefore(units, quantityChildren.get(quantityChildren.size() - 2));
                    }
                }
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
            log.trace("adding child name[value=ITEM_TREE] before {}", () -> children.get(0).getNodeName());
            insertNameBeforeElement(element, children.get(0), "ITEM_TREE");
        }
        Document document = element.getOwnerDocument();
        for (Element child : children) {
            log.trace("renaming {} to items", () -> child.getNodeName());
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
        log.trace("setting @type to {}", () -> "HISTORY");
        data.setAttribute("xsi:type", "oe:HISTORY");
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
                log.trace("renaming {} to events", () -> child.getNodeName());
                document.renameNode(child, null, "events");
            }
        }
        if (hasOrigin == false) {
            log.trace("adding child origin[value=now() before {}", () -> dataChildren.get(1).getNodeName());
            Element origin = document.createElement("origin");
            data.insertBefore(origin, dataChildren.get(1));
            Element value = document.createElement("value");
            value.setTextContent(LocalDateTime.now().toString());
            origin.appendChild(value);
        }
    }

    protected void transformPointEvent(Element element) {
        log.trace("transformPointEvent({})", () -> element.getNodeName());
        List<Element> children = XML.getChildElements(element);
        Element name = null;
        for (Element child : children) {
            if (child.getNodeName().equals("name")) {
                name = child;
                break;
            }
        }
        for (Element child : XML.getChildElements(name)) {
            if (child.getNodeName().equals("value")) {
                log.trace("renaming name/value from {} to ANY_EVENT", () -> child.getTextContent());
                child.setTextContent("ANY_EVENT");
                break;
            }
        }

        Element state = children.get(children.size() - 1);
        if (state.getNodeName().equals("state") && XML.getChildElements(state).size() == 0) {
            log.trace("removing empty {}", () -> "state");
            element.removeChild(state);
        }
    }

    protected void transformNamespaces(Element composition) {
        composition.removeAttribute("template_id");
        composition.removeAttribute("xmlns");
        composition.removeAttribute("xmlns:oe");
        composition.setAttribute("xsi:schemaLocation", OPENEHR_XSI_LOCATION);
        transformNamespacePrefix(composition);
    }

    protected void transformNamespacePrefix(Element element) {
        for (Element child : XML.getChildElements(element))
            transformNamespacePrefix(child);
        if (element.getNodeName().startsWith("oe:") == false)
            element.getOwnerDocument().renameNode(element, OPENEHR_NS, "oe:" + element.getNodeName());
    }

}
