package com.coreconsulting.res.openehr;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        long t = System.currentTimeMillis();
        new TDD(new File("C:\\Repositories\\res\\SoapUI\\2019-08-22\\reg-atendimento-clinico_doc1.xml")).toRM();
        System.out.println("runtime: " + (System.currentTimeMillis() - t) + "ms");
    }

}
