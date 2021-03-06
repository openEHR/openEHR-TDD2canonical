package com.coreconsulting.res.openehr.tdd2canonical.transformer;

import com.coreconsulting.res.openehr.tdd2canonical.TDD;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Element;

import java.util.List;

/**
 * This class implements the transformation of a POINT_EVENT from the TDD so that it complies to the openEHR Reference
 * Model. See {@link AbstractTransformer} for more details.
 * The transformation renames the element "name/value" to "ANY_EVENT" and removes empty "state" nodes.
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public class PointEventTransformer extends AbstractTransformer {

    public static String type = "POINT_EVENT";

    @Override
    public void transformElement(TDD tdd, Element element, String nodeId, String type, StringBuilder xsdXPath) {
        log.trace("transformElement({}, {}, {}, {})", () -> tdd.getTemplateId(), () -> element.getNodeName(),
                () -> nodeId, () -> type);
        List<Element> children = tdd.getChildElements(element);
        Element name = null;
        for (Element child : children) {
            if (child.getNodeName().equals("name")) {
                name = child;
                break;
            }
        }
        for (Element child : tdd.getChildElements(name)) {
            if (child.getNodeName().equals("value")) {
                log.trace("renaming name/value from {} to ANY_EVENT", () -> child.getTextContent());
                child.setTextContent("ANY_EVENT");
                break;
            }
        }

        Element state = children.get(children.size() - 1);
        if (state.getNodeName().equals("state") && tdd.getChildElements(state).size() == 0) {
            log.trace("removing empty {}", () -> "state");
            element.removeChild(state);
        }
    }
}
