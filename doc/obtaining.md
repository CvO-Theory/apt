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
