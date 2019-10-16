package com.coreconsulting.res.openehr.tdd2rm.transformer;

import com.coreconsulting.res.openehr.tdd2rm.TDD;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

@Log4j2
public class ItemTreeTransformer extends AbstractTransformer {

    public static String type = "ITEM_TREE";

    @Override
    public void transformElement(TDD tdd, Element element, String nodeId, String type, StringBuilder xsdXPath) {
        log.trace("transformElement({}, {}, {}, {})", () -> tdd.getTemplateId(), () -> element.getNodeName(),
                () -> nodeId, () -> type);
        List<Element> children = tdd.getChildElements(element);
        if (children.size() > 0) {
            log.trace("adding child name[value=ITEM_TREE] before {}", () -> children.get(0).getNodeName());
            tdd.insertNameBeforeElement(element, children.get(0), "ITEM_TREE");
        }
        Document document = element.getOwnerDocument();
        for (Element child : children) {
            log.trace("renaming {} to items", () -> child.getNodeName());
            document.renameNode(child, null, "items");
        }
    }
}
