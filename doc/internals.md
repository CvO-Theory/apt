# Internal Structure of APT

The source code of APT is split into multiple parts. The purpose of these
different parts will now be explained. Each part has its own subdirectory under
`src/`.

## lib

This contains the basic library parts of APT. These are the abstract data types
(ADT) to represent transition systems and Petri nets and some general utility
functions.

## main

The command line entry point is in this part. This also contains some
functionality for transforming between the string representation and the
internal representation of module arguments and return values.

## module

All the functionality of APT lives in the `module` part. This is were actual
checks and algorithms are implemented.

## io

The non-trivial I/O functionality lives in the `io` part. These are parsers for
various file formats, as well as renderers that turn an in-memory representation
into a file.

## json

The [JSON-interface for APT](json.md) is implemented in the `json` part.

## test

This part contains unit and integration tests.

## ant

The code in this part is needed to build APT. It is not part of the final
`apt.jar` file. Since the code in here is executed by Ant from the `build.xml`
file, it is called `ant`.

In this part is a utility for integration tests of the command line utility
(`IntegrationTestTask`), a brute-force approach for testing parsers
(`ParsableTask`), some general quality assurance for modules
(`ModuleParameterVerifyTask` and `ServiceVerifyTask`) and some utility for
driving the test suite (`WriteTestsXML`).

## compiler

APT uses some marker annotations on [modules](extending.md) and other
objects (`@AptParser` for parsers, `@AptRenderer` for renderers,
`@AptParameterTransformation` for parameter transformations, and
`@AptReturnValueTransformation` for return value transformations). These work by
having a processor that is executed by the Java compiler. This processor writes
a list of all annotated classes under the folder `META-INF/services/` in the
output directory of the compiler.

This annotation processor is implemented in the `compiler` part. In addition to
writing these files, it does some sanity checks, for example checking that only
implementations of `Module` are annotated with `@AptModule`.

## glue

This part contains interfaces and annotations that are needed by APT itself as
well as the code from the `compiler` part.
