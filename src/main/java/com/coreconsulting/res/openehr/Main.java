package com.coreconsulting.res.openehr;

import org.w3c.dom.Document;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        long t = System.currentTimeMillis();
        Document rm =
                new TDD(new File("C:\\Repositories\\res\\SoapUI\\2019-08-22\\reg-atendimento-clinico_doc1.xml")).toRM();
        System.out.println(XML.toString(rm));
        System.out.println((System.currentTimeMillis() - t) + "ms");
    }

}
