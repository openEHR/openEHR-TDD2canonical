package com.coreconsulting.res.openehr.tdd2rm;

import static org.junit.jupiter.api.Assertions.*;

import com.coreconsulting.res.openehr.tdd2rm.exceptions.UnsupportedTypeException;
import com.coreconsulting.res.openehr.tdd2rm.util.XML;
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

@Log4j2
public class TDDTests {

    @Test
    void transformed_RAC_doc1_is_valid() {
        log.trace("transformed_RAC_doc1_is_valid({})", () -> "");
        TDD tdd = new TDD(new File("./src/test/resources/TDD/reg-atendimento-clinico_doc1.xml"));
        long t = System.currentTimeMillis();
        Document rm = null;
        try {
            rm = tdd.toRM();
            log.debug("transformed_RAC_doc1_is_valid transformation took {}ms to complete",
                    () -> System.currentTimeMillis() - t);
            log.debug("transformed composition: \n" + XML.toString(rm));
        } catch (UnsupportedTypeException e) {
            fail(e);
        }

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
            log.info("transformed_RAC_doc1_is_valid transformation is valid");
            return;
        } catch (SAXException e) {
            log.error("transformed_RAC_doc1_is_valid transformation is invalid");
            fail(e);
        } catch (IOException e) {
            fail(e);
        }
    }

}
