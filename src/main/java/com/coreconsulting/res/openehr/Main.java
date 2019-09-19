package com.coreconsulting.res.openehr;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        new TDD(new File("C:\\Repositories\\res\\SoapUI\\2019-08-22\\reg-atendimento-clinico_doc1.xml")).toRM();
    }

}
