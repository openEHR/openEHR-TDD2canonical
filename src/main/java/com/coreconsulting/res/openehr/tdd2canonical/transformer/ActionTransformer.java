package com.coreconsulting.res.openehr.tdd2canonical.transformer;

import com.coreconsulting.res.openehr.tdd2canonical.TDD;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Element;

/**
 * This class implements the transformation of an ACTION from the TDD so that it complies to the openEHR Reference
 * Model. See {@link AbstractTransformer} for more details.
 * Currently, no transformations are required.
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public class ActionTransformer extends AbstractTransformer {

    public static String type = "ACTION";

    @Override
    public void transformElement(TDD tdd, Element element, String nodeId, String type, StringBuilder xsdXPath) {
        log.trace("transformElement({}, {}, {}, {})", () -> tdd.getTemplateId(), () -> element.getNodeName(),
                () -> nodeId, () -> type);
    }
}
