package com.coreconsulting.res.openehr;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;

@Slf4j
public class XML {

    @Getter
    protected Document xml;

    @Getter
    static protected XPath xpath = XPathFactory.newInstance().newXPath();

    DocumentBuilder builder;

    private XML() {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            log.debug("created the DOM builder and XPath compiler/evaluator");
        } catch (ParserConfigurationException e) {
            log.error("error creating the DOM builder", e);
        }
    }

    public XML(File file) {
        this();
        try {
            xml = builder.parse(file);
            log.debug("parsed the XML document from file");
        } catch (SAXException e) {
            log.error("error parsing the XML file", e);
        } catch (IOException e) {
            log.error("error opening the XML file", e);
            e.printStackTrace();
        }
    }

    public XML(String string) {
        this();
        try {
            xml = builder.parse(new ByteArrayInputStream(string.getBytes("UTF-8")));
            log.debug("parsed the XML document from string");
        } catch (SAXException e) {
            log.error("error parsing the XML file", e);
        } catch (IOException e) {
            log.error("error opening the XML file", e);
            e.printStackTrace();
        }
    }

    public XML(URI uri) {
        this();
        try {
            xml = builder.parse(uri.toString());
            log.debug("parsed the XML document from URI");
        } catch (SAXException e) {
            log.error("error parsing the XML file", e);
        } catch (IOException e) {
            log.error("error opening the XML file", e);
            e.printStackTrace();
        }
    }

    public String getXPathAsString(String xpath) {
        try {
            return this.xpath.compile(xpath).evaluate(xml);
        } catch (XPathExpressionException e) {
            log.warn("malformed XPath expression", e);
            return null;
        }
    }

    public NodeList getXPathAsNodeList(String xpath) {
        try {
            return (NodeList) this.xpath.compile(xpath).evaluate(xml, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            log.warn("malformed XPath expression", e);
            return null;
        }
    }

    @Override
    public String toString() {
        return toString(xml);
    }

    public static String toString(Document document) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            log.debug("serialized the XML document");
            return writer.toString();
        } catch (TransformerConfigurationException e) {
            log.error("error serializing the XML document");
            return null;
        } catch (TransformerException e) {
            log.error("error serializing the XML document");
            return null;
        }
    }


}
