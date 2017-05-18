Obtaining APT
=============

There are two options for obtaining apt.jar:

- Download a pre-build JAR file
- Build your own version

Download a pre-build JAR file
-----------------------------

A pre-build "apt.jar" files is available
[here](http://cvo-theory.github.io/apt-javadoc/apt.jar).
This file always contains the latest version of APT. It is automatically updated
when changes are made to the APT repository on GitHub.

Build your own version
----------------------

This software is written in the [Java programming language](http://java.com).
Thus it should work on various architectures and operating systems. For building
APT, a Java Development Kit (JDK) compatible with Java 7 and [Apache
Ant](http://ant.apache.org) is required. The configuration for Ant is contained
in a file called "build.xml". The way that Ant is called varies depending on the
operating system and integrated development environment used.  This guide
explains how a JAR file can be built on Unix-like systems.  Please refer to the
relevant online documentation for other systems.

Building and using a JAR file on the command line of Unix-like systems:

Just call "ant jar" in the directory containing "build.xml". This will build a
file called "apt.jar". You can run this file via "java -jar apt.jar".

If Ant complains "javac: invalid target release: 1.7", you are not using a
Java 7 JDK. Please update your Java installation.

APT as a library
----------------

When you execute the "jar" target via Ant, not only a file `apt.jar` is
generated. The following files can be found in the `artifacts/` directory:

- `apt-lib.jar`: This contains the data structures that APT uses and some
  fundamental algorithms and interfaces. This JAR file is build from the
  contents of `src/glue` and `src/lib`.
- `apt-io.jar`: This JAR file contains the parsers and renderers. Thus, via this
  JAR file you can read and write files based on the data structures from
  `apt-lib.jar`.
- `apt-module.jar`: This provides all the actual algorithms and modules of APT.
- `apt.jar`: This is the main executable of APT. It contains the contents of
  `apt-lib.jar`, `apt-io.jar`, and `apt-module.jar`. Additionally, this contains
  the annotation processor built from `src/compiler` needed for building your
  own modules. Finally, this also contains the libraries that APT depends on.
- `apt-json.jar`: In addition to the contents of `apt.jar`, this also contains
  the JSON-interface to APT which is built from `src/json`, and the JSON-library
  used by that code.
