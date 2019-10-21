package com.coreconsulting.res.openehr.tdd2canonical.transformer;

import com.coreconsulting.res.openehr.tdd2canonical.exceptions.UnsupportedTypeException;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the functionality related to providing transformers for the TDD transformation.
 * Once a new concrete implementation is added, it has to be registered for it to be recognized.
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public class TransformerFactory {

    /**
     * {@link Map} used to catalog known transformers at runtime
     */
    protected static Map<String, AbstractTransformer> transformers = new HashMap<>();

    /**
     * Populates {@link #transformers} at initialization
     */
    static {
        transformers.put(ActionTransformer.type, new ActionTransformer());
        transformers.put(ActivityTransformer.type, new ActivityTransformer());
        transformers.put(AdminEntryTransformer.type, new AdminEntryTransformer());
        transformers.put(ClusterTransformer.type, new ClusterTransformer());
        transformers.put(CompositionTransformer.type, new CompositionTransformer());
        transformers.put(ElementTransformer.type, new ElementTransformer());
        transformers.put(EvaluationTransformer.type, new EvaluationTransformer());
        transformers.put(InstructionTransformer.type, new InstructionTransformer());
        transformers.put(IntervalEventTransformer.type, new IntervalEventTransformer());
        transformers.put(ItemTreeTransformer.type, new ItemTreeTransformer());
        transformers.put(LocatableTransformer.type, new LocatableTransformer());
        transformers.put(ObservationTransformer.type, new ObservationTransformer());
        transformers.put(PointEventTransformer.type, new PointEventTransformer());
        transformers.put(SectionTransformer.type, new SectionTransformer());
    }

    /**
     * Retrieves an actual transformer by the type of element to be transformed.
     *
     * @param type type of the element to be transformed
     * @return actual transformer intended to transform the element
     * @throws UnsupportedTypeException when retrieving a transformer for a type that hasn't been registered
     */
    public static AbstractTransformer getTransformer(String type) throws UnsupportedTypeException {
        AbstractTransformer transformer = transformers.get(type);
        if (transformer == null) {
            log.error("unsupported type={}", () -> type);
            throw new UnsupportedTypeException(type);
        }
        return transformer;
    }

}
