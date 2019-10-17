package com.coreconsulting.res.openehr.tdd2rm.transformer;

import com.coreconsulting.res.openehr.tdd2rm.TDD;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

/**
 * This class implements the transformation of a LOCATABLE from the TDD so that it complies to the openEHR Reference
 * Model. See {@link AbstractTransformer} for more details.
 * The transformation sets the @archetype_node_id and, when present, the @xsi:type based on the definitions in the TDS.
 * The transformation also adds the "archetype_details" child to every element that is an archetype root.
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public class LocatableTransformer extends AbstractTransformer {

    public static String type = "LOCATABLE";

    @Override
    public void transformElement(TDD tdd, Element element, String nodeId, String type, StringBuilder xsdXPath) {
        log.trace("transformElement({}, {}, {}, {})", () -> tdd.getTemplateId(), () -> element.getNodeName(),
                () -> nodeId, () -> type);
        log.debug("setting @archetype_node_id={} and @type={} for {}", () -> nodeId, () -> type,
                () -> element.getNodeName());
        element.setAttribute("archetype_node_id", nodeId);
        if (type != null)
            element.setAttribute("xsi:type", tdd.getNamespacePrefix() + type);

        if (nodeId.startsWith("openEHR-")) {
            List<Element> children = tdd.getChildElements(element);
            Element reference = children.get(1);
            for (int i = children.size() - 1; i >= 0; i--) {
                Element child = children.get(i);
                if (child.getNodeName().equals("feeder_audit")) {
                    reference = child;
                    break;
                }
            }

            Element _reference = reference;
            log.trace("adding child archetype_details before {}", () -> _reference.getNodeName());
            Document document = element.getOwnerDocument();
            Element archetypeDetails = document.createElement("archetype_details");
            element.insertBefore(archetypeDetails, reference);
            Element archetypeId = document.createElement("archetype_id");
            archetypeDetails.appendChild(archetypeId);
            Element archetypeIdValue = document.createElement("value");
            archetypeId.appendChild(archetypeIdValue);
            archetypeIdValue.setTextContent(nodeId);
            Element templateId = document.createElement("template_id");
            archetypeDetails.appendChild(templateId);
            Element templateIdValue = document.createElement("value");
            templateId.appendChild(templateIdValue);
            templateIdValue.setTextContent(tdd.getTemplateId());
            Element rmVersion = document.createElement("rm_version");
            archetypeDetails.appendChild(rmVersion);
            rmVersion.setTextContent(tdd.getRMVersion());
        }
    }
}
