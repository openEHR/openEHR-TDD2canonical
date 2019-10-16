package com.coreconsulting.res.openehr.tdd2rm.transformer;

import com.coreconsulting.res.openehr.tdd2rm.TDD;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

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
            if (child.getNodeName().equals("context"))
                break;
            log.trace("renaming {} to content", () -> child.getNodeName());
            document.renameNode(child, null, "content");
        }

        transformNamespaces(tdd, element);
    }

    protected void transformNamespacePrefix(TDD tdd, Element element) {
        for (Element child : tdd.getChildElements(element))
            transformNamespacePrefix(tdd, child);
        if (element.getNodeName().startsWith(tdd.getNamespacePrefix()) == false)
            element.getOwnerDocument().renameNode(element, TDD.OPENEHR_NS,
                    tdd.getNamespacePrefix() + element.getNodeName());
    }

    protected void transformNamespaces(TDD tdd, Element composition) {
        composition.removeAttribute("template_id");
        composition.removeAttribute("xmlns");
        composition.removeAttribute("xmlns:" + tdd.getNamespacePrefix().substring(0,
                tdd.getNamespacePrefix().length() - 1));
        composition.setAttribute("xsi:schemaLocation", TDD.OPENEHR_XSI_LOCATION);
        transformNamespacePrefix(tdd, composition);
    }

}
