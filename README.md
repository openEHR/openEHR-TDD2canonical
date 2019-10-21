#openEHR-TDD2canonical

This repository provides a *work in progress* solution to convert openEHR compositions serialized as TDD (*Template
 Data Document*) to canonical XML, so that they comply to the Reference Model (see
 [Composition.xsd](./src/test/resources/RM/XML-schemas/Composition.xsd)). It was first based on the original code by
  [Ricardo Gonçalves](mailto:ricardofago@gmail.com) @ 
  [Core Consulting (Brasília, Brazil)](http://coreconsulting.com.br).

##Introduction

The TDD is a format used to serialize openEHR compositions using XML documents that are instances of a XML schema
known as TDS (*Template Data Schema*). It enforces all restrictions specified within a template and its archetypes,
flattening the information model into a complete definition to replace archetype node ids, classes and paths with a
more readable syntax intended to make it meaningful for developers with low exposure to the openEHR formalism.

As the TDD is generated from a TDS, it complies to a specific template. However, many openEHR solutions currently
support canonical JSON and XML instances of the Reference Model, which, albeit more generic, is a vocabulary that
requires deeper knowledge about the openEHR formalism. The goal for this project is to act a bridge, allowing parties
capable of handling TDD with standard tooling to interact with such solutions.

Assuming Ocean's [Templeate Designer](https://oceanhealthsystems.com/products/template-designer) to be, historically,
the *de facto* community modelling tool for openEHR template maintainers, we expect the TDD to comply to a TDS instance
that was generated using the transformation shipped with it (see ./Transforms/tds-default.xsl from the installation
 directory).  

##Architecture

###Activity diagram
![Flow.png](./uml/Flow.png)

###Class diagram
![ClassDiagram.png](./uml/ClassDiagram.png)

##Getting started
* Maven
* Logging
-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -Dorg.apache.logging
.log4j.level=TRACE

##Extensions and optimizations
###Properties

###Caching and local folders

##Roadmap
* extend type support
* improve tests
* make it answer through a web server, optimizing cache
* possibly replace String processing with java-libs


