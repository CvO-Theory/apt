Apache Commons Collections
===========================

Welcome to the Collections component of the Apache Commons project.
This component contains many new collections and collection utilities.

Two jar files (apart from sources and javadocs) are produced by this component.
The first, commons-collections4-x.y.jar is the main jar used by applications.
The second, commons-collections4-x.y-tests.jar is an extension to junit
for testing new collection implementations and is not normally used by applications.

Building from source
=-=-=-=-=-=-=-=-=-=-

Maven2
------
Collections is usually built using Maven2.
It can be found here :

  http://maven.apache.org/

Once installed, the major goals are:

mvn package           - build jars
mvn install           - place jars in local maven repository
mvn assembly:assembly - build tar.gz/zips

Ant
---

It may also be built using Ant.
It can be found here :

  http://ant.apache.org/

For testing the project, you will also need JUnit :

  http://www.junit.org/

To let the test process find JUnit, you may make a 
copy of the build.properties.sample file, rename to
build.properties,  and modify to reflect
the location of the junit.jar on your computer.


Once you have Ant properly installed, and the
build.properties file correctly reflects the location
of your junit.jar, you are ready to build and test.
The major targets are:

ant compile      - compile the code
ant test         - test using junit
ant jar          - create a jar file
ant test-jar     - create the testframework jar file
ant javadoc      - build the javadoc
ant dist         - create folders as per a distribution
