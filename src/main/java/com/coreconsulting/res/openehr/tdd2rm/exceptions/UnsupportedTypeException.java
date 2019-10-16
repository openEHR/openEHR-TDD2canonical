package com.coreconsulting.res.openehr.tdd2rm.exceptions;

public class UnsupportedTypeException extends Exception {

    public UnsupportedTypeException(String type) {
        super("unsupported type: " + type);
    }

}
