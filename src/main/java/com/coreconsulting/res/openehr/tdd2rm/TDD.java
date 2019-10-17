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
import java.util.List;


/**
 * This class implements functionality related to TDD handling, such as deserializing it from a XML instance,
 * resolving the openEHR namespace prefix, @template_id and associated {@link TDS}, generic node manipulation and
 * transformation to an openEHR Reference Model composition.
 * The TDD is expected to match a TDS represented by a XML Schema exported from Ocean's Template Designer.
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public class TDD extends XML {

    /**
     * Prefix for the default openEHR namespace. Will be parsed from the TDD.
     */
    protected String OPENEHR_NS_PREFIX;
    /**
     * Default openEHR namespace.
     */
    public static final String OPENEHR_NS = "http://schemas.openehr.org/v1";
    /**
     * openEHR Reference Model version. Hardcoded to 1.0.2 for now.
     */
    public static final String OPENEHR_RM_VERSION = "1.0.2";
    /**
     * Default openEHR composition XML Schema location, used to assert the xsi:schemaLocation attribute.
     */
    public static final String OPENEHR_NS_LOCATION =
            "https://specifications.openehr.org/releases/" + OPENEHR_RM_VERSION + "/its/XML-schema/Composition.xsd";

    /**
     * {@literal @xsi:schemaLocation} relating {@link #OPENEHR_NS} to {@link #OPENEHR_NS_LOCATION}.
     */
    public static final String OPENEHR_XSI_LOCATION = OPENEHR_NS + " " + OPENEHR_NS_LOCATION;

    /**
     * TDS which this TDD complies to. Parsed from the @template_id through local cache or a remote URI.
     */
    protected TDS tds;
    /**
     * {@literal @template_id} attribute. Parsed from the TDD.
     */
    protected String templateId;

    /**
     * Default constructor, not used but kept for serialization purposes.
     */
    public TDD() {}

    /**
     * Creates a TDD from a {@link File} object, expected to be a XML document available on the local filesystem.
     *
     * @param file XML document available on the local filesystem
     */
    public TDD(File file) {
        super(file);
        log.trace("TDD({})", () -> file.getAbsolutePath());
    }

    /**
     * Creates a TDD from a {@link String} object, expected to be an in-memory textual representation of the XML
     * document.
     *
     * @param string textual representation of the XML document
     */
    public TDD(String string) {
        super(string);
        log.trace("TDD({})", () -> string);
    }

    /**
     * Creates a TDD from a {@link URI}, expected to be a reachable remote instance of the XML document.
     *
     * @param uri reachable URI to retrieve the XML document
     */
    public TDD(URI uri) {
        super(uri);
        log.trace("TDD({})", () -> uri);
    }

    /**
     * Parses the openEHR namespace prefix used used in the TDD by matching the namespace declaration of {
     * {@link #OPENEHR_NS}}. Once parsed, it is stored as a field for future use. If {@link #OPENEHR_NS} is the
     * default namespace, it is reassigned to the prefix "oe:" to prevent XSI issues with abstract types (@xsi:type).
     *
     * @return openEHR namespace prefix.
     */
    public String getNamespacePrefix() {
        log.trace("getNamespacePrefix({})", () -> "");
        if (OPENEHR_NS_PREFIX == null) {
            // Iterate through all namespace declarations
            Element composition = xml.getDocumentElement();
            NamedNodeMap attributes = composition.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                String key = attribute.getNodeName();
                String value = attribute.getNodeValue();
                if (key.equals("xmlns") && value.equals(OPENEHR_NS)) {
                    // If openEHR is the default namespace (no prefix), we reassign it to oe: to prevent XSI issues
                    OPENEHR_NS_PREFIX = "oe:";
                    log.debug("added OPENEHR_NS_PREFIX={} for OPENEHR_NS={}", () -> OPENEHR_NS_PREFIX,
                            () -> OPENEHR_NS);
                    break;
                } else if (key.startsWith("xmlns:") && value.equals(OPENEHR_NS)) {
                    // Otherwise, we grab the prefix that matches the openEHR namespace
                    OPENEHR_NS_PREFIX = key.substring(key.indexOf(":") + 1, key.length()) + ":";
                    log.debug("parsed OPENEHR_NS_PREFIX={} for OPENEHR_NS={}", () -> OPENEHR_NS_PREFIX,
                            () -> OPENEHR_NS);
                    break;
                }
            }
        }
        return OPENEHR_NS_PREFIX;
    }

    /**
     * Returns the openEHR Reference Model release version.
     *
     * @return openEHR Reference Model release version
     */
    public String getRMVersion() {
        return OPENEHR_RM_VERSION;
    }

    /**
     * Parses the TDS schema location (@xsi:schemaLocation) from the TDD by matching the xsi:schemaLocation entry
     * that matches Ocean's Template Designer default namespace (http://schemas.oceanehr.com/templates).
     *
     * @return TDS schema location
     */
    public String getTDSLocation() {
        log.trace("getTDSLocation({})", () -> "");
        String schemaLocation = getXPathAsString("/*[1]/@schemaLocation");
        log.debug("parsed schemaLocation={}", () -> schemaLocation);
        String tdsLocation = RegEx.getFirstMatch(schemaLocation, "http:\\/\\/schemas\\.oceanehr\\.com\\/templates ([^\\s]*)");
        log.debug("parsed TDS location={}", () -> tdsLocation);
        return tdsLocation;
    }

    /**
     * Parses the @template_id from the TDD root element. Once parsed, it is stored as a field for future use.
     *
     * @return @template_id from the TDD root element
     */
    public String getTemplateId() {
        log.trace("getTemplateId({})", () -> "");
        if (templateId == null) {
            templateId = getXPathAsString("/*[1]/@template_id");
            log.debug("parsed template_id={} from the TDD", () -> templateId);
        }
        return templateId;
    }

    /**
     * Parses the TDS which this TDD complies to. First it attempts to look it up from a local cache using the
     * {@literal @template_id} as a key, then, if unavailable, it grabs it from {@literal @xsi:schemaLocation}. Once
     * parsed, it is stored as a field for future use.
     *
     * @return TDS which this TDD complies to
     */
    public TDS getTDS() {
        log.trace("getTDS({})", () -> "");
        if (tds == null) {
            // Try to look up the TDS from the local cache before parsing a remote location
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

    /**
     * Generic node transformation method that inserts a name as the first child of a specific element. Used for
     * cases in which the TDD and TDS don't specify a name but the openEHR Reference Model requires one.
     *
     * @param parent parent element to insert the name into
     * @param text value of the name child
     */
    public void insertNameAsFirstChild(Element parent, String text) {
        log.trace("insertNameAsFirstChild({}, {})", () -> parent.getNodeName(), () -> text);
        Node node = parent.getFirstChild();
        while (node instanceof Element == false)
            node = node.getNextSibling();
        insertNameBeforeElement(parent, (Element) node, text);
    }

    /**
     * Generic node transformation method that inserts a name as a child before a specific sibling within a parent
     * element. Used for classes in which the TDD and TDS don't specify a name but the openEHR Reference Model
     * requires one.
     *
     * @param parent parent element to insert the name into
     * @param sibling sibling element which the name child is to be inserted before
     * @param text value of the name child
     */
    public void insertNameBeforeElement(Element parent, Element sibling, String text) {
        log.trace("insertNameBeforeElement({}, {})", () -> parent.getNodeName(), () -> text);
        Document document = parent.getOwnerDocument();
        Element name = document.createElement("name");
        parent.insertBefore(name, sibling);
        Element value = document.createElement("value");
        name.appendChild(value);
        value.setTextContent(text);
    }

    /**
     * Generates a clone of the TDD document and transforms it into a openEHR Reference Model composition, parsing
     * the document from the root element.
     *
     * @return openEHR Reference Model composition generated from the TDD
     * @throws UnsupportedTypeException
     */
    public Document toRM() throws UnsupportedTypeException {
        log.trace("toRM({})", () -> "");
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            // Clone the TDD document se we keep the original one intact
            Document rm = builder.newDocument();
            Node root = rm.importNode(xml.getDocumentElement(), true);
            rm.appendChild(root);
            log.debug("{}", () -> "cloned the TDD object into a RM object");

            // Transform the clone starting from the root node
            transformNode(root, new StringBuilder("/schema[1]/element[1]"));
            log.debug("{}", () -> "transformed the TDD into a COMPOSITION");

            return rm;
        } catch (ParserConfigurationException e) {
            log.error("error creating the DOM builder", e);
            return null;
        }
    }

    /**
     * Transforms a node from the TDD into the standardized representation from the openEHR Reference Model.
     * The algorithm is a postorder tree traversal, so it recursively traverses all descendants and then applies any
     * transformations. The XPath expression to look up the definition of the TDD element in the related TDS is also
     * built recursively through node introspection.
     *
     * @param node current node being traversed
     * @param xsdXPath XPath expression that matches the current node in the associated TDS
     * @throws UnsupportedTypeException when trying to transform a node of a type with no transformation available
     * (see {@link TransformerFactory})
     */
    protected void transformNode(Node node, StringBuilder xsdXPath) throws UnsupportedTypeException {
        log.trace("transformNode({}, {})", () -> node.getNodeName(), () -> xsdXPath);
        // Look up the @archetype_node_id in the TDS, as it is optional in the TDD
        String nodeId = getTDS().getCachedXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='archetype_node_id'][1" +
                "]/@fixed");
        // If we reach something that is not LOCATABLE, we don't need to descend from this node
        if (nodeId == null)
            return;

        // Postorder tree traversal
        List<Element> children = getChildElements(node);
        for (Element child : children) {
            // Create copies of the XDS Xpath so siblings don't interfere with each other
            transformNode(child,
                    new StringBuilder("(").append(xsdXPath).append("//element[@name='").append(child.getNodeName()).append("'])[1]"));
        }

        // Look up the @type (from archetype root or attribute type) to find out which transformations are required
        String type = RegEx.getFirstMatch(nodeId, "openEHR\\-\\w+\\-([^\\.]+).*");
        if (type == null)
            type = getTDS().getCachedXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='type'][1]/@fixed");

        String _type = type;
        log.debug("transforming node={} [@nodeId={}, @type={}]", () -> node.getNodeName(), () -> nodeId,
                () -> _type);

        // Apply the general LOCATABLE transformation that applies to any element at this point
        Element element = (Element) node;
        TransformerFactory.getTransformer("LOCATABLE").transformElement(this, element, nodeId, type, xsdXPath);

        // Apply specific additional transformations according to the type
        if (type != null) {
            TransformerFactory.getTransformer(type).transformElement(this, element, nodeId, type, xsdXPath);
        }
    }

}
