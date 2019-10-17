package com.coreconsulting.res.openehr.tdd2rm.transformer;

import com.coreconsulting.res.openehr.tdd2rm.TDD;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class abstracts the concept of a transformer that is capable of transforming a TDD element of a specific
 * type, making it compliant to the openEHR Reference Model.
 * Every concrete subclass is expected to override this, providing the supported {@link #type} and a specific
 * {@link #transformElement(TDD, Element, String, String, StringBuilder)}, depending on the adjustments identified
 * through the gap between th TDD and the openEHR Reference Model for the type.
 * This is the major extensibility point of the application. Once a new concrete implementation is added, it has to
 * be registered through the {@link TransformerFactory} for it to be recognized by {@link TDD#transformNode(Node, StringBuilder)}.
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public abstract class AbstractTransformer {

    /**
     * The type of element this transformer handles
     */
    public static String type;

    /**
     * Transforms an {@link Element} of a {@link TDD}. May use the @archetype_node_id and @type for decision logic,
     * and might also use the equivalent path to its definition in the
     * {@link com.coreconsulting.res.openehr.tdd2rm.TDS} to look up additional information, such as the @valueType
     * that identifies the concrete type of a DATA_VALUE according to the openEHR Reference Model.
     *
     * @param tdd {@link TDD} instance that owns the {@link Element} being transformed
     * @param element {@link Element} element being transformed
     * @param nodeId @archetype_node_id of the element
     * @param type type of the element
     * @param xsdXPath path to the definition in the {@link com.coreconsulting.res.openehr.tdd2rm.TDS}
     */
    public abstract void transformElement(TDD tdd, Element element, String nodeId, String type, StringBuilder xsdXPath);

}
