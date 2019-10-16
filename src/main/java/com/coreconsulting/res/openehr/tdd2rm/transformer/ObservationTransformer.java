package com.coreconsulting.res.openehr.tdd2rm.transformer;

import com.coreconsulting.res.openehr.tdd2rm.TDD;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.time.LocalDateTime;
import java.util.List;

@Log4j2
public class ObservationTransformer extends AbstractTransformer {

    public static String type = "OBSERVATION";

    @Override
    public void transformElement(TDD tdd, Element element, String nodeId, String type, StringBuilder xsdXPath) {
        log.trace("transformElement({}, {}, {}, {})", () -> tdd.getTemplateId(), () -> element.getNodeName(),
                () -> nodeId, () -> type);
        List<Element> children = tdd.getChildElements(element);

        Element data = null;
        for (Element child : children) {
            if (child.getNodeName().equals("data")) {
                data = child;
                break;
            }
        }
        log.debug("setting @type to {}", () -> "HISTORY");
        data.setAttribute("xsi:type", tdd.getNamespacePrefix() + "HISTORY");
        tdd.insertNameAsFirstChild(data, "HISTORY");

        Document document = element.getOwnerDocument();
        List<Element> dataChildren = tdd.getChildElements(data);
        boolean hasOrigin = false;
        for (Element child : dataChildren) {
            if (child.getNodeName().equals("name")) {
                continue;
            } else if (child.getNodeName().equals("origin")) {
                hasOrigin = true;
            } else {
                log.debug("renaming {} to events", () -> child.getNodeName());
                document.renameNode(child, null, "events");
            }
        }
        if (hasOrigin == false) {
            log.debug("adding child origin[value=now() before {}", () -> dataChildren.get(1).getNodeName());
            Element origin = document.createElement("origin");
            data.insertBefore(origin, dataChildren.get(1));
            Element value = document.createElement("value");
            value.setTextContent(LocalDateTime.now().toString());
            origin.appendChild(value);
        }
    }
}
