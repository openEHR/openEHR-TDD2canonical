package com.coreconsulting.res.openehr.tdd2rm.transformer;

import com.coreconsulting.res.openehr.tdd2rm.exceptions.UnsupportedTypeException;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class TransformerFactory {

    protected static Map<String, AbstractTransformer> transformers = new HashMap<>();

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

    public static AbstractTransformer getTransformer(String type) throws UnsupportedTypeException {
        AbstractTransformer transformer = transformers.get(type);
        if (transformer == null) {
            log.error("unsupported type={}", () -> type);
            throw new UnsupportedTypeException(type);
        }
        return transformer;
    }

}
