package com.coreconsulting.res.openehr.tdd2rm.exceptions;

/**
 * This exception implements the handling of transforming a type in the TDD that doesn't have a concrete instance of
 * {@link com.coreconsulting.res.openehr.tdd2rm.transformer.AbstractTransformer} available. Right now this means the
 * application doesn't know how to handle such type. However, not all types require transformation, so currently
 * there are transformers that do nothing. As the code evolve to greater maturity, covering more types and providing
 * more tests, it is possible to relax this into a subtype of {@link RuntimeException}, so the absence a transformer
 * for a type that has no impact doesn't stop the execution.
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
public class UnsupportedTypeException extends Exception {

    public UnsupportedTypeException(String type) {
        super("unsupported type: " + type);
    }

}
