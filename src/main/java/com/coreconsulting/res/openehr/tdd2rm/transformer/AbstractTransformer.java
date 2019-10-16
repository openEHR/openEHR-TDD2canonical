package com.coreconsulting.res.openehr.tdd2rm.transformer;

import com.coreconsulting.res.openehr.tdd2rm.TDD;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Element;

@Log4j2
public abstract class AbstractTransformer {
    
    public static String type;

    public abstract void transformElement(TDD tdd, Element element, String nodeId, String type, StringBuilder xsdXPath);

}
