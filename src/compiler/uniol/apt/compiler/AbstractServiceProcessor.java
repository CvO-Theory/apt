/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015       vsp
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package uniol.apt.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/**
 * Abstract annotation processor base class which scans for an annotation, analyzes all classes marked by this
 * annotation and writes their names in files in META-INF
 *
 * @author vsp
 */
public abstract class AbstractServiceProcessor extends AbstractProcessor {
	protected final Class<? extends Annotation> annotationClass;
	protected final String interfaceName;
	protected final boolean allowGenerics;

	protected Elements elements;
	protected Types types;
	private Filer filer;
	private Messager messager;
	private boolean finished;

	/**
	 * Constructor
	 *
	 * @param annotationClass Class object which describes the annotation
	 * @param interfaceClass Class object which describes the interface which the annotated classes must implement
	 * @param allowGenerics Are generic implementations of the interface allowed? Else, they are an error
	 */
	protected AbstractServiceProcessor(Class<? extends Annotation> annotationClass, Class<?> interfaceClass,
			boolean allowGenerics) {
		this(annotationClass, interfaceClass.getCanonicalName(), allowGenerics);
	}

	/**
	 * Constructor
	 *
	 * @param annotationClass Class object which describes the annotation
	 * @param interfaceName Full Name of the interface which describes the interface which the annotated classes
	 * must implement
	 * @param allowGenerics Are generic implementations of the interface allowed? Else, they are an error
	 */
	protected AbstractServiceProcessor(Class<? extends Annotation> annotationClass, String interfaceName,
			boolean allowGenerics) {
		this.annotationClass = annotationClass;
		this.interfaceName = interfaceName;
		this.allowGenerics = allowGenerics;
	}

	/**
	 * Function that is called on every annotated class that is visited. During {@link process}, every class that is
	 * annotated with our annotation has some sanity-checks applied (e.g. does it implement the required interface)
	 * and then this function is called on it.
	 * @param classEle The TypeElement that represents the class that is being compiled.
	 * @param className The name of the class represented by classEle.
	 */
	abstract protected void visitClass(TypeElement classEle, String className);

	/**
	 * Function that is called after all annotation processing is finished.
	 * This is where output should be produced.
	 * @see writeResourceList
	 */
	abstract protected void produceOutput();

	@Override
	public synchronized void init(ProcessingEnvironment procEnv) {
		super.init(procEnv);
		this.elements = procEnv.getElementUtils();
		this.types    = procEnv.getTypeUtils();
		this.filer    = processingEnv.getFiler();
		this.messager = processingEnv.getMessager();
		this.finished = false;
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotations = new HashSet<String>();
		annotations.add(this.annotationClass.getCanonicalName());
		return annotations;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (this.finished) // only run in the first round
			return false;

		for (Element ele : roundEnv.getElementsAnnotatedWith(this.annotationClass)) {
			if (!isValidClass(ele)) {
				return false;
			}
			TypeElement classEle = (TypeElement) ele;
			String className     = classEle.getQualifiedName().toString();
			visitClass(classEle, className);
		}

		produceOutput();

		this.finished = true; // prevent running in subsequent rounds

		return true;
	}

	/**
	 * Function that writes a resource list into the class output directory.
	 * Existing entries are preserved. This means that this function only ever adds new entries.
	 * @param resourceName Name of the file in which the resource list should be saved.
	 * @param entries Entries that should be added to the list.
	 * @throws IOException In case I/O errors occur.
	 */
	protected void writeResourceList(String resourceName, Collection<String> entries) throws IOException {
		entries = new TreeSet<>(entries);

		// read already listed services
		try {
			FileObject fo = this.filer.getResource(StandardLocation.CLASS_OUTPUT, "",
					resourceName);
			try (InputStream is = fo.openInputStream()) {
				LineIterator lIter = IOUtils.lineIterator(is, "UTF-8");
				while (lIter.hasNext()) {
					String entry = lIter.next();
					entries.add(entry);
				}
			}
		} catch (IOException ex) {
			/* It's ok if the resource can't get found; we only skip reading it */
		}

		// write new list
		FileObject fo = this.filer.createResource(StandardLocation.CLASS_OUTPUT, "",
				resourceName);
		Writer writer = fo.openWriter();
		for (String entry : entries) {
			writer.append(entry + "\n");
		}
		writer.close();
	}

	private boolean isValidClass(Element ele) {
		if (ele.getKind() != ElementKind.CLASS) {
			error(ele, "Non-Class %s annotated with %s.", ele.getSimpleName().toString(),
					this.annotationClass.getCanonicalName());
		}

		TypeElement classEle = (TypeElement) ele;

		if (!classEle.getModifiers().contains(Modifier.PUBLIC)) {
			error(classEle, "Class %s is not public.", classEle.getQualifiedName().toString());
			return false;
		}

		if (classEle.getModifiers().contains(Modifier.ABSTRACT)) {
			error(classEle, "Class %s is abstract.", classEle.getQualifiedName().toString());
			return false;
		}

		TypeMirror expected = this.types.erasure(this.elements.getTypeElement(this.interfaceName).asType());
		boolean found = false;
		for (TypeMirror actual : classEle.getInterfaces()) {
			if (this.types.isAssignable(actual, expected)) {
				found = true;
				break;
			}
		}
		if (!found) {
			error(classEle, "Class %s doesn't implement interface %s.",
					classEle.getQualifiedName().toString(),
					this.interfaceName);
			return false;
		}

		if (!allowGenerics && !classEle.getTypeParameters().isEmpty()) {
			error(classEle, "Class %s is generic.", classEle.getQualifiedName().toString());
			return false;
		}

		for (Element enclosed : classEle.getEnclosedElements()) {
			if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
				ExecutableElement constructorEle = (ExecutableElement) enclosed;
				if (constructorEle.getParameters().size() == 0 &&
						constructorEle.getModifiers().contains(Modifier.PUBLIC)) {
					return true;
				}
			}
		}

		error(classEle, String.format("Class %s needs an public no-arg constructor",
					classEle.getQualifiedName().toString()));
		return false;
	}

	/**
	 * Prints an error message using the {@link Messager} object.
	 *
	 * @param fmt
	 *                format string for
	 *                {@link String#format(String, Object...)}
	 * @param args
	 *                arguments used in the format string
	 */
	protected void error(String fmt, Object... args) {
		this.messager.printMessage(Diagnostic.Kind.ERROR, String.format(fmt, args));
		this.finished = true; // we had an error, don't try to run in subsequent rounds
	}

	/**
	 * Prints an error message using the {@link Messager} object that is
	 * associated with the given element's location.
	 *
	 * @param ele
	 *                element that the error message is associated with
	 * @param fmt
	 *                format string for
	 *                {@link String#format(String, Object...)}
	 * @param args
	 *                arguments used in the format string
	 */
	protected void error(Element ele, String fmt, Object... args) {
		this.messager.printMessage(Diagnostic.Kind.ERROR, String.format(fmt, args), ele);
		this.finished = true; // we had an error, don't try to run in subsequent rounds
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
