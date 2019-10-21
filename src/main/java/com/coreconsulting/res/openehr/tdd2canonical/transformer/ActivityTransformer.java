package com.coreconsulting.res.openehr.tdd2canonical.transformer;

import com.coreconsulting.res.openehr.tdd2canonical.TDD;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

/**
 * This class implements the transformation of an ACTIVITY from the TDD so that it complies to the openEHR Reference
 * Model. See {@link AbstractTransformer} for more details.
 * The transformation renames the element to "activity" and reverses its children "timing" and "description" position.
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public class ActivityTransformer extends AbstractTransformer {

    public static String type = "ACTIVITY";

    @Override
    public void transformElement(TDD tdd, Element element, String nodeId, String type, StringBuilder xsdXPath) {
        log.trace("transformElement({}, {}, {}, {})", () -> tdd.getTemplateId(), () -> element.getNodeName(),
                () -> nodeId, () -> type);
        Document document = element.getOwnerDocument();
        document.renameNode(element, null, "activities");

        List<Element> children = tdd.getChildElements(element);
        log.debug("{}", () -> "reversing ACTIVITY children timing and description");
        element.removeChild(children.get(1));
        element.insertBefore(children.get(1), children.get(3));
    }
}
