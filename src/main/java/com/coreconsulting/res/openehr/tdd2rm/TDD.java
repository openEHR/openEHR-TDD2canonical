package com.coreconsulting.res.openehr.tdd2rm;

import com.coreconsulting.res.openehr.tdd2rm.exceptions.UnsupportedTypeException;
import com.coreconsulting.res.openehr.tdd2rm.transformer.TransformerFactory;
import com.coreconsulting.res.openehr.tdd2rm.util.RegEx;
import com.coreconsulting.res.openehr.tdd2rm.util.XML;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
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

    public static final String OPENEHR_NS = "http://schemas.openehr.org/v1";
    public static final String OPENEHR_NS_LOCATION = "https://specifications.openehr.org/releases/1.0/its/XML-schema/Composition.xsd";
    protected String OPENEHR_NS_PREFIX;
    public static final String OPENEHR_RM_VERSION = "1.0.2";
    public static final String OPENEHR_XSI_LOCATION = OPENEHR_NS + " " + OPENEHR_NS_LOCATION;

    protected TDS tds;
    protected String templateId;

    public TDD() {}

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

    public String getNamespacePrefix() {
        log.trace("getNamespacePrefix({})", () -> "");
        if (OPENEHR_NS_PREFIX == null) {
            Element composition = xml.getDocumentElement();
            NamedNodeMap attributes = composition.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                String key = attribute.getNodeName();
                String value = attribute.getNodeValue();
                if (key.equals("xmlns") && value.equals(OPENEHR_NS)) {
                    OPENEHR_NS_PREFIX = "oe:";
                    log.debug("added OPENEHR_NS_PREFIX={} for OPENEHR_NS={}", () -> OPENEHR_NS_PREFIX,
                            () -> OPENEHR_NS);
                    break;
                } else if (key.startsWith("xmlns:") && value.equals(OPENEHR_NS)) {
                    OPENEHR_NS_PREFIX = key.substring(key.indexOf(":") + 1, key.length()) + ":";
                    log.debug("parsed OPENEHR_NS_PREFIX={} for OPENEHR_NS={}", () -> OPENEHR_NS_PREFIX,
                            () -> OPENEHR_NS);
                    break;
                }
            }
        }
        return OPENEHR_NS_PREFIX;
    }

    public String getRMVersion() {
        return OPENEHR_RM_VERSION;
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
            tds = TDSRegistry.fromTemplateId(getTemplateId());
            if (tds != null) {
                log.debug("{}", () -> "loaded cached TDS for the TDD");
            } else {
                tds = TDSRegistry.fromTDSLocation(getTDSLocation());
                log.debug("{}", () -> "loaded remote TDS for the TDD");
            }
        }
        return tds;
    }

    public void insertNameAsFirstChild(Element parent, String text) {
        log.trace("insertNameAsFirstChild({}, {})", () -> parent.getNodeName(), () -> text);
        Node node = parent.getFirstChild();
        while (node instanceof Element == false)
            node = node.getNextSibling();
        insertNameBeforeElement(parent, (Element) node, text);
    }

    public void insertNameBeforeElement(Element parent, Element sibling, String text) {
        log.trace("insertNameBeforeElement({}, {})", () -> parent.getNodeName(), () -> text);
        Document document = parent.getOwnerDocument();
        Element name = document.createElement("name");
        parent.insertBefore(name, sibling);
        Element value = document.createElement("value");
        name.appendChild(value);
        value.setTextContent(text);
    }

    public Document toRM() throws UnsupportedTypeException {
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
            return null;
        }
    }

    protected void transformNode(Node node, StringBuilder xsdXPath) throws UnsupportedTypeException {
        log.trace("transformNode({}, {})", () -> node.getNodeName(), () -> xsdXPath);
        String nodeId = getTDS().getCachedXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='archetype_node_id'][1" +
                "]/@fixed");
        if (nodeId == null)
            return;

        List<Element> children = getChildElements(node);
        for (Element child : children)
            transformNode(child,
                    new StringBuilder("(").append(xsdXPath).append("//element[@name='").append(child.getNodeName()).append("'])[1]"));

        String type = RegEx.getFirstMatch(nodeId, "openEHR\\-\\w+\\-([^\\.]+).*");
        if (type == null)
            type = getTDS().getCachedXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='type'][1]/@fixed");

        String _type = type;
        log.debug("transforming node={} [@nodeId={}, @type={}]", () -> node.getNodeName(), () -> nodeId,
                () -> _type);

        Element element = (Element) node;
        TransformerFactory.getTransformer("LOCATABLE").transformElement(this, element, nodeId, type, xsdXPath);

        if (type != null) {
            TransformerFactory.getTransformer(type).transformElement(this, element, nodeId, type, xsdXPath);
        }
    }

}
