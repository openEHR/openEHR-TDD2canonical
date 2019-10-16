package com.coreconsulting.res.openehr.tdd2rm.transformer;

import com.coreconsulting.res.openehr.tdd2rm.TDD;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

@Log4j2
public class ElementTransformer extends AbstractTransformer {

    public static String type = "ELEMENT";

    @Override
    public void transformElement(TDD tdd, Element element, String nodeId, String type, StringBuilder xsdXPath) {
        log.trace("transformElement({}, {}, {}, {})", () -> tdd.getTemplateId(), () -> element.getNodeName(),
                () -> nodeId, () -> type);
        String valueType = tdd.getTDS().getCachedXPathAsString(xsdXPath + "/complexType[1]/attribute[@name='valueType'][1" +
                "]/@fixed");
        List<Element> children = tdd.getChildElements(element);
        for (Element child : children) {
            if (child.getNodeName().equals("name")) {
                log.trace("removing {} children other than {}", () -> "name", () -> "value");
                List<Element> nameChildren = tdd.getChildElements(child);
                for (int i = 1; i < nameChildren.size(); i++)
                    child.removeChild(nameChildren.get(i));
            }
            if (child.getNodeName().equals("value")) {
                log.trace("setting @type to {}", () -> valueType);
                child.setAttribute("xsi:type", tdd.getNamespacePrefix() + valueType);
                if (valueType.equals("DV_PROPORTION")) {
                    log.trace("inferring {} denominator from type", () -> valueType);
                    List<Element> proportionChildren = tdd.getChildElements(child);
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
                } else if (valueType.equals("DV_QUANTITY")) {
                    log.trace("reversing {} children precision and units", () -> valueType);
                    List<Element> quantityChildren = tdd.getChildElements(child);
                    if (quantityChildren.size() > 2) {
                        Node units = child.removeChild(quantityChildren.get(quantityChildren.size() - 1));
                        child.insertBefore(units, quantityChildren.get(quantityChildren.size() - 2));
                    }
                }
                break;
            }
        }
    }
}
