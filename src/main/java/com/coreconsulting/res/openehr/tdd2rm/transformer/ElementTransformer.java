package com.coreconsulting.res.openehr.tdd2rm.transformer;

import com.coreconsulting.res.openehr.tdd2rm.TDD;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

/**
 * This class implements the transformation of an ELEMENT from the TDD so that it complies to the openEHR Reference
 * Model. See {@link AbstractTransformer} for more details.
 * The transformation removes the "name/*" children except for "name/value" and sets the "value/@xsi:type" attribute
 * according to the DATA_VALUE type (based on the @valueType in the {@link TDD}'s
 * {@link com.coreconsulting.res.openehr.tdd2rm.TDS}).
 * For a DV_PROPORTION, the transformation also computes the "denominator" based on its "type".
 * For a DV_QUANTITY, the transformation also reverses its children "precision" and "units" position;
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
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
                for (int i = 1; i < nameChildren.size(); i++) {
                    // "name" is the first index, so we skip it
                    child.removeChild(nameChildren.get(i));
                }
            }
            if (child.getNodeName().equals("value")) {
                log.trace("setting @type to {}", () -> valueType);
                child.setAttribute("xsi:type", tdd.getNamespacePrefix() + valueType);
                if (valueType.equals("DV_PROPORTION")) {
                    log.trace("inferring {} denominator from type", () -> valueType);
                    List<Element> proportionChildren = tdd.getChildElements(child);
                    Element proportionType = proportionChildren.get(1);
                    if (proportionType.getNodeName().equals("type")) {
                        // set DV_PROPORTION "denominator" based on "type"
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
