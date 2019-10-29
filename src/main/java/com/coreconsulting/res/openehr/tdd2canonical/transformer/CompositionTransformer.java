package com.coreconsulting.res.openehr.tdd2canonical.transformer;

import com.coreconsulting.res.openehr.tdd2canonical.TDD;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.List;

/**
 * This class implements the transformation of a COMPOSITION from the TDD so that it complies to the openEHR Reference
 * Model. See {@link AbstractTransformer} for more details.
 * The transformation renames the element to "composition" and its archetyped children to "content".
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public class CompositionTransformer extends AbstractTransformer {

    public static String type = "COMPOSITION";

    @Override
    public void transformElement(TDD tdd, Element element, String nodeId, String type, StringBuilder xsdXPath) {
        log.trace("transformElement({}, {}, {}, {})", () -> tdd.getTemplateId(), () -> element.getNodeName(),
                () -> nodeId, () -> type);
        log.debug("renaming {} to composition", () -> element.getNodeName());
        Document document = element.getOwnerDocument();
        document.renameNode(element, null, "composition");

        List<Element> children = tdd.getChildElements(element);
        for (int i = children.size() - 1; i >= 0; i--) {
            Node child = children.get(i);
            // Rename all the children from the last one until we reach "context"
            if (child.getNodeName().equals("context"))
                break;
            log.trace("renaming {} to content", () -> child.getNodeName());
            document.renameNode(child, null, "content");
        }

        /* Since we are doing a postorder traversal, the COMPOSITION is the last element transformed, therefore we
        can now fix the namespaces and prefixes */
        transformNamespaces(tdd, element);
    }

    /**
     * Transforms the namespace prefix of an element and its descendants, to replace any namespaces (including
     * Ocean's one in the TDD) to the openEHR Reference Model.
     *
     * @param tdd {@link TDD} to fetch the openEHR Reference Model namespace prefix from
     * @param element {@link Element} to transform the namespace prefix and descend into
     */
    protected void transformNamespacePrefix(TDD tdd, Element element) {
        log.trace("transformNamespacePrefix({}, {})", () -> tdd.getTemplateId(), () -> element.getNodeName());
        for (Element child : tdd.getChildElements(element))
            transformNamespacePrefix(tdd, child);
        // For now, hardcode the namespace declaration as the default for compatibility with EtherCIS and EHRbase
        String nodeName = element.getNodeName();
        if (nodeName.contains(":")) {
            String localName = nodeName.substring(nodeName.indexOf(":") + 1);
            element.getOwnerDocument().renameNode(element, null, localName);
        }
        // Transform xsi:type, if present
        String type = element.getAttribute("xsi:type");
        if (type.isEmpty() == false && type.contains(":")) {
            // For now, hardcode the namespace declaration as the default for compatibility with EtherCIS and EHRbase
            type = type.substring(type.indexOf(":") + 1);
            /*type = tdd.getNamespacePrefix() + type.substring(type.indexOf(":") + 1);*/
            element.setAttribute("xsi:type", type);
        }
    }

    /**
     * Transform the namespaces of a composition, removing the additional ones and setting the @xsi:schemaLocation to
     * point to the openEHR Reference Model standard Composition.xsd.
     * The @template_id is also removed, as it should be provided through
     * *:/composition/*:archetype_details/*:template_id/*:value.
     *
     * @param tdd {@link TDD} to fetch the openEHR Reference Model namespace prefix from
     * @param composition root {@link Element} that holds the namespace declarations
     */
    protected void transformNamespaces(TDD tdd, Element composition) {
        log.trace("transformNamespaces({}, {})", () -> tdd.getTemplateId(), () -> composition.getNodeName());
        // Remove @template_id, which in canonical format is represented within archetype_details
        composition.removeAttribute("template_id");
        // For now, hardcode the namespace declaration as the default for compatibility with EtherCIS and EHRbase
        if (tdd.getNamespacePrefix().isEmpty() == false)
            composition.removeAttribute("xmlns:" + tdd.getNamespacePrefix().substring(0,
                    tdd.getNamespacePrefix().length() - 1));
        composition.setAttribute("xmlns", TDD.OPENEHR_NS);
        composition.setAttribute("xsi:schemaLocation", TDD.OPENEHR_XSI_LOCATION);
        transformNamespacePrefix(tdd, composition);
    }

}
