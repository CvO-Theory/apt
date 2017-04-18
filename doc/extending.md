Extending APT with own modules
==============================

Overview
--------

If you want to implement your own module, you have to implement the
Module-interface that is available in APT. Additionally, your class should be
have the @AptModule annotation so that it is automatically registered with APT.
To make this work, `apt.jar` contains an annotation processor that must be run
by your Java compiler. How to ensure this is explained below.

As an example for a module, you can look at the module that provides the
coverability graph. Its source code is available in
`src/module/uniol/apt/analysis/coverability/CoverabilityModule.java`.

The latest Javadoc documentation is always available at:

http://CvO-theory.github.io/apt-javadoc/

Own modules with Eclipse
------------------------

To develop a module with Eclipse, follow these steps:

1. Create a new Eclipse project
2. Get a copy of the file apt.jar, for example from
   [here](http://cvo-theory.github.io/apt-javadoc/apt.jar), and add it as a
   library to your project.
3. Open the project properties and navigate to "Java Compiler/Annotation
   Processing".
   1. Check "Enable project specific settings".
   2. Check "Enable annotation processing".
   3. Check "Enable annotation processing in editor".
4. Open the project properties and navigate to "Java Compiler/Annotation
   Processing/Factory Path".
   1. Check "Enable project specific settings".
   2. Add the `apt.jar` as an external jar.
   3. Check that `uniol.apt.compiler.ModuleProcessor` is listed under "Advanced".
5. Create a new run configuration of the type "Java Application" and set
   `uniol.apt.APT` as the main class. Here you can also provide command line
   arguments (for example, the name of your module should be the first
   argument).

The above ensures that APT's annotation processor is run by Eclipse. It creates
the needed files so that APT will automatically find your module.

It is not recommended to build APT directly from Eclipse, because of problems
with the project structure. You can however compile APT from Eclipse by
opening APT's `build.xml` file and running the `jar` target via Ant.

Own modules with IntelliJ IDEA
------------------------------

1. Create a new Java project.
2. Enable annotation processing
   * Go to "Settings/Build, Execution, Deployment/Compiler/Annotation
     Processors".
   * Make sure "Enable annotation processing" is enabled.
3. Get a copy of the file apt.jar, for example from
   [here](http://cvo-theory.github.io/apt-javadoc/apt.jar), and add it as a
   library to your project.
   * Go to "Project Structure/Libraries".
   * Add `apt.jar` as a new Java library.
4. Make sure a working JAR file is built.
   * Go to "Project Structure/Artifacts".
   * Add a new "JAR" artifact that is built "From Modules with dependencies",
     has main class "uniol.apt.APT" and select "Copy to the output directory and
     link via manifest".
   * It is recommended to also select "Include in project build" so the JAR
     file is updated when the project is build.

The above ensures that any modules that you write is automatically found. Make
sure that your module has the `@AptModule` annotation!

It is not recommended to build APT directly from IDEA. You can however compile
APT by executing the `jar` target of APT's `build.xml` via Ant.

Own modules from the command line
---------------------------------

The following assumes that your source code is in a folder called src.

1. Get a copy of the file apt.jar, for example from
   [here](http://cvo-theory.github.io/apt-javadoc/apt.jar).
2. Compile your source code against APT.

        mkdir classes
        javac -d classes -cp apt.jar src/*

3. Run the new module

        java -cp apt.jar:classes uniol.apt.APT your_module_name example_argument

The above example is for unixoid systems. On Windows, Java uses `;` instead of
`:` as separator in the class path argument. This means you have to use `-cp
apt.jar;classes` instead on Windows.
