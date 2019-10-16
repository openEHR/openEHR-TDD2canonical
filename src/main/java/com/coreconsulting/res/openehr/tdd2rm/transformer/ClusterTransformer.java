package com.coreconsulting.res.openehr.tdd2rm.transformer;

import com.coreconsulting.res.openehr.tdd2rm.TDD;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

@Log4j2
public class ClusterTransformer extends AbstractTransformer {

    public static String type = "CLUSTER";

    @Override
    public void transformElement(TDD tdd, Element element, String nodeId, String type, StringBuilder xsdXPath) {
        log.trace("transformElement({}, {}, {}, {})", () -> tdd.getTemplateId(), () -> element.getNodeName(),
                () -> nodeId, () -> type);
        List<Element> children = tdd.getChildElements(element);
        Document document = element.getOwnerDocument();
        for (Element child : children) {
            String nodeName = child.getNodeName();
            if (nodeName.equals("name") || nodeName.equals("archetype_details"))
                continue;
            log.trace("renaming {} to items", () -> child.getNodeName());
            document.renameNode(child, null, "items");
        }
    }
}
