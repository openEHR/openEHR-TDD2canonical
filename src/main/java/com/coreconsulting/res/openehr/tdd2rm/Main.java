package com.coreconsulting.res.openehr.tdd2rm;

import com.coreconsulting.res.openehr.tdd2rm.util.XML;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;

import java.io.File;

@Log4j2
public class Main {

    public static void main(String[] args) {
        log.trace("main()", () -> "");
        TDD tdd = new TDD(new File("C:\\Repositories\\res\\SoapUI\\2019-08-22\\reg-atendimento-clinico_doc1.xml"));
        long t = System.currentTimeMillis();
        Document rm = tdd.toRM();
        log.debug(XML.toString(rm));
        log.info("runtime: {}ms", () -> System.currentTimeMillis() - t);
    }

}
