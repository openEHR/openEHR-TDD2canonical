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
that was generated using the transformation shipped with it (see ``./Transforms/tds-default.xsl`` from the installation
 directory).  

##Architecture

The idea behind the TDD to canonical transformation is pretty simple. We traverse the TDD document recursively and
perform any required transformations on the nodes we visit. Due to the complexity and legacy nature of 
``tds-default.xsl``, currently the gap between the TDD and the canonical formats is being identified by manually
comparing composition instances from local use cases (*e.g.* TDSs being used in Brazil), so we can cover the relevant
scenarios sooner, and once we reach an element that we can't transform we throw an exception in order to quickly
identify it. As not all elements require transformation, it is possible to relax this at a more mature stage.
 
At most cases, as some important identifiers are optional from the TDS definition, we need to introspect metadata when
transforming a TDD element to its canonical form. This is done using a XPath composite that is built recursively with
the tree traversal, pointing to its definition in the TDS.

Considering that the amount of look up operations that may happen during the transformation of a composition, there
is a mechanism to store TDSs locally, preload their reachable XPath composites once, index the object in an in-memory
registry and serialize it to the disk, which drastically improves de algorithm execution time.

As for the transformation of specific types according to the openEHR Reference Model, it is the major extensibility
point for this solution. The actual logic is exposed from an abstract class, and the actual transformers implement
it and are provided through a factory indexed by the type name. Feel free to create an issue if you reach any
unsupported type (be sure to provide the TDD and its TDS), or even better, to raise a pull request sharing your
contribution to the current solution.

###Activity diagram

The diagram below introduces a more detailed flow of the transformation logic. It doesn't cover all the invocation
stack, specially for utility classes (RegEx, XPath, serialization, etc.), but it provides a comprehensible overview
 of the business logic.

![Flow.png](./uml/Flow.png)

The [source](./uml/Flow.plantuml) was written and processed used [PlantUML](http://plantuml.com/).

###Class diagram

The diagram below shows the class definitions and relationships. More details are available in the
[JavaDoc](https://ricardofago.github.io/openehr-TDD2canonical). Be noted that the concrete transformers that
are instances of ``AbstractTransformer`` are not represented, as they are abstracted to the transformation caller
(``TDD.transformNode(Node, StringBuilder)``), and they may include additional fields or methods internally if
 appropriate.

![ClassDiagram.png](./uml/ClassDiagram.png)

The [source](./uml/Flow.plantuml) was written and processed used [PlantUML](http://plantuml.com/).

##Getting started
* Maven
* Logging
-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -Dorg.apache.logging
.log4j.level=TRACE

##Extensions and optimizations
###Properties

###Caching and local folders

##Roadmap
* extend type support and maybe remove UnsupportedTypeException
* improve tests
* make it answer through a web server, optimizing cache
* possibly replace String processing with java-libs


