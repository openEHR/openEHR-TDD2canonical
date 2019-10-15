package com.coreconsulting.res.openehr.tdd2rm.util;

import lombok.extern.log4j.Log4j2;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Log4j2
public class FST {

    public static Object read(InputStream stream) {
        log.trace("read({})", () -> "");
        try {
            FSTObjectInput input = new FSTObjectInput(stream);
            Object object = input.readObject();
            input.close();
            return object;
        } catch (IOException e) {
            log.warn("failed to read object from cache", e);
            return null;
        } catch (ClassNotFoundException e) {
            log.warn("failed to instantiate class from cache object", e);
            return null;
        }
    }

    public static void write(OutputStream stream, Object object) {
        log.trace("write({})", () -> "");
        try {
            FSTObjectOutput output = new FSTObjectOutput(stream);
            output.writeObject(object);
            output.close();
        } catch (IOException e) {
            log.warn("failed to write object to cache", e);
        }
    }


}
