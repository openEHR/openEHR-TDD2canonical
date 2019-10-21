package com.coreconsulting.res.openehr.tdd2canonical.util;

import lombok.extern.log4j.Log4j2;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class implements funcionality related to object serialization, including reading objects from streams and
 * writing objects into streams.
 * It is based on <a href="https://github.com/RuedigerMoeller/fast-serialization">FST</a>
 * , an open-source drop-in replacement for the standard JVM serialization engine with significantly better performance.
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public class Serializer {

    /**
     * Reads an {@link Object} from an {@link InputStream} (usually a {@link java.io.FileInputStream}).
     *
     * @param stream {@link InputStream} to read the {@link Object} from
     * @return {@link Object} deserialized
     */
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

    /**
     * Writes an {@link Object} into an {@link OutputStream} (usually a {@link java.io.FileOutputStream}).
     *
     * @param stream {@link OutputStream} stream to write the {@link Object} into
     * @param object {@link Object} to be written
     */
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
