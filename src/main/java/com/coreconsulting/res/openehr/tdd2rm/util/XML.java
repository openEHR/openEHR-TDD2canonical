package com.coreconsulting.res.openehr.tdd2rm.util;

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

@Log4j2
public class XML {

    @Getter
    static protected XPath xpath = XPathFactory.newInstance().newXPath();
    @Getter
    protected Document xml;
    transient DocumentBuilder builder;

    public XML() {
        log.trace("XML({})", () -> "");
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("error creating DOM builder", e);
        }
    }

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

    @Override
    public String toString() {
        return toString(xml);
    }

}
