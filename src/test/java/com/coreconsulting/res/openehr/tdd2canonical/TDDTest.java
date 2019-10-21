package com.coreconsulting.res.openehr.tdd2canonical;

import com.coreconsulting.res.openehr.tdd2canonical.exceptions.UnsupportedTypeException;
import com.coreconsulting.res.openehr.tdd2canonical.util.XML;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * This class tests the TDD functionality, ranging from creating an instance from a serialized XML document,
 * processing the @template_id, locating the associated TDS (from the local cache or a remote location) and
 * transforming it to a composition that complies to the openEHR Reference Model XML schemas.
 * See {@link TDD} for details on the implementation.
 *
 * @author Ricardo Gonçalves {@literal <ricardo.goncalves@coreconsulting.com.br>}
 */
@Log4j2
public class TDDTest {

    /**
     * This is a major test, currently performing the entire TDD to composition transformation flow.
     * It fails because the standard XSD validation from the JVM misinterprets the XSI abstractions (xsi:type,
     * xsi:schemaLocation), but the resulting composition can be successfully validated with more capable tools.
     * The TDD and TDS being tested belong to a real use case in Brazil and are not intended to cover all the openEHR
     * Reference Model types that require transformation.
     */
    @Test
    void transformed_RAC_doc1_is_valid() {
        log.trace("transformed_RAC_doc1_is_valid({})", () -> "");
        TDD tdd = new TDD(new File("./src/test/resources/TDD/reg-atendimento-clinico_doc1.xml"));
        // Log the execution time
        long t = System.currentTimeMillis();
        Document rm = null;
        try {
            rm = tdd.toRM();
            log.debug("transformed_RAC_doc1_is_valid transformation took {}ms to complete",
                    () -> System.currentTimeMillis() - t);
            log.debug("transformed composition: \n" + XML.toString(rm));
        } catch (UnsupportedTypeException e) {
            /* Happens if the TDD has a openEHR Reference Model type that doesn't have a transformation implemented;
            could be relaxed as not all types require transformation but currently we keep empty transformations
            so we can track the types we are sure about and throw an exception otherwise. */
            fail(e);
        }

        // Validate the transformation composition against the openEHR Reference Model XML Schema
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        try {
            schema = factory.newSchema(new File("./src/test/resources/RM/XML-schemas/Composition.xsd"));
        } catch (SAXException e) {
            fail(e);
        }
        Validator validator = schema.newValidator();
        try {
            validator.validate(new DOMSource(rm));
            // If validation fails, it throws an exception, so at this point the composition is valid
            log.info("transformed_RAC_doc1_is_valid transformation is valid");
            return;
        } catch (SAXException e) {
            // For some reason, the JVM standard XSD validation rejects the XSI abstractions, we should sort this out
            // The validation for this test succeeds if we validate the composition with an external tool
            log.error("transformed_RAC_doc1_is_valid transformation is invalid");
            fail(e);
        } catch (IOException e) {
            fail(e);
        }
    }

}
