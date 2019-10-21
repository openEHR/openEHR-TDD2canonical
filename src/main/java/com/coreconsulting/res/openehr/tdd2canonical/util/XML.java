package com.coreconsulting.res.openehr.tdd2canonical.util;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements functionality related to generic XML handling, such as deserializing it from a {@link File},
 * {@link String} or {@link URI}, listing child elements, evaluating XPath expressions and serializing the contents
 * into textual {@link String}.
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public class XML {

    /**
     * XPath static instance shared for expression evaluation.
     */
    @Getter
    static protected XPath xpath = XPathFactory.newInstance().newXPath();
    /**
     * The {@link Document} instance.
     */
    @Getter
    protected Document xml;
    /**
     * The {@link DocumentBuilder} instance shared to build instances of {@link Document} from different sources.
     * It is transient because it can't be serialized as a field, but it can also be refactored into the constructors.
     */
    transient DocumentBuilder builder;

    /**
     * Default constructor. Not used, but kept fro serialization purposes.
     */
    public XML() {
        log.trace("XML({})", () -> "");
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("error creating DOM builder", e);
        }
    }

    /**
     * Creates a XML from a {@link File} object, expected to be a XML document available on the local filesystem.
     *
     * @param file XML document available on the local filesystem
     */
    public XML(File file) {
        this();
        log.trace("XML({})", () -> file.getAbsolutePath());
        try {
            xml = builder.parse(file);
            log.debug("{}", () -> "parsed the XML document from file");
        } catch (SAXException e) {
            log.error("error parsing the XML file", e);
        } catch (IOException e) {
            log.error("error opening the XML file", e);
            e.printStackTrace();
        }
    }

    /**
     * Creates a XML from a {@link String} object, expected to be an in-memory textual representation of the XML
     * document.
     *
     * @param string textual representation of the XML document
     */
    public XML(String string) {
        this();
        log.trace("XML({})", () -> "...");
        try {
            xml = builder.parse(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
            log.debug("{}", () -> "parsed the XML document from string");
        } catch (SAXException e) {
            log.error("error parsing the XML file", e);
        } catch (IOException e) {
            log.error("error opening the XML file", e);
            e.printStackTrace();
        }
    }

    /**
     * Creates a XML from a {@link URI}, expected to be a reachable remote instance of the XML document.
     *
     * @param uri reachable URI to retrieve the XML document
     */
    public XML(URI uri) {
        this();
        log.trace("XML({})", () -> uri);
        try {
            xml = builder.parse(uri.toString());
            log.debug("{}", () -> "parsed the XML document from URI");
        } catch (SAXException e) {
            log.error("error parsing the XML file", e);
        } catch (IOException e) {
            log.error("error opening the XML file", e);
            e.printStackTrace();
        }
    }

    /**
     * Returns a {@link List} of children of an {@link Node} that are instances of {@link Element}.
     *
     * @param node {@link Node} which children are to be listed
     * @return {@link List} of children that are instances of {@link Element}
     */
    public static List<Element> getChildElements(Node node) {
        log.trace("getChildElements({})", () -> node.getNodeName());
        List<Element> elements = new ArrayList<Element>();

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE)
                elements.add((Element) child);
        }

        return elements;

    }

    /**
     * Returns a textual representation of a {@link Document}.
     *
     * @param document {@link Document} to be represented as text
     * @return textual representation of the {@link Document}
     */
    public static String toString(Document document) {
        log.trace("toString({})", () -> document.getNodeName());
        try {
            final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            final LSSerializer writer = impl.createLSSerializer();

            writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
            writer.getDomConfig().setParameter("xml-declaration", true);

            String asString = writer.writeToString(document);
            return asString;
        } catch (Exception e) {
            log.error("error serializing the XML document", e);
            return null;
        }
    }

    /**
     * Runs a XPath expression on the XML and returns the text content of the first result.
     *
     * @param xpath XPath expression to be evaluated
     * @return text content of the first result evaluated
     */
    public String getXPathAsString(String xpath) {
        log.trace("getXPathAsString({})", () -> xpath);
        NodeList nodes = getXPathAsNodeList(xpath);
        if (nodes.getLength() == 0) {
            return null;
        } else {
            String value = nodes.item(0).getTextContent();
            return value;
        }
    }

    /**
     * Runs a XPath expression on the XML and returns the results as a {@link NodeList}.
     *
     * @param xpath XPath expression to be evaluated
     * @return {@link NodeList} of the results evaluated
     */
    public NodeList getXPathAsNodeList(String xpath) {
        log.trace("getXPathAsNodeList({})", () -> xpath);
        try {
            NodeList nodes = (NodeList) XML.xpath.compile(xpath).evaluate(xml, XPathConstants.NODESET);
            return nodes;
        } catch (XPathExpressionException e) {
            log.error("malformed XPath expression", e);
            return null;
        }
    }

    /**
     * Returns a textual representation of the {@link Document} within this {@link XML}.
     *
     * @return textual representation of this instance
     */
    @Override
    public String toString() {
        return toString(xml);
    }

}
