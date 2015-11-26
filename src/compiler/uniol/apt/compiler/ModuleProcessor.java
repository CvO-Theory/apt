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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
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

import uniol.apt.module.AptModule;
import uniol.apt.module.Module;

/**
 * Annotation processor which scans for an AptModule annotation, analyzes all classes marked by this
 * annotation and writes their names in a file in META-INF
 *
 * @author vsp
 */
public class ModuleProcessor extends AbstractProcessor {
	private final Class<? extends Annotation> annotationClass;
	private final String interfaceName;

	private Elements elements;
	private Types types;
	private Filer filer;
	private Messager messager;

	/**
	 * Constructor
	 */
	public ModuleProcessor() {
		this.annotationClass = AptModule.class;
		this.interfaceName   = Module.class.getCanonicalName();
	}

	private Set<String> modules;

	@Override
	public synchronized void init(ProcessingEnvironment procEnv) {
		super.init(procEnv);
		this.elements = procEnv.getElementUtils();
		this.types    = procEnv.getTypeUtils();
		this.filer    = processingEnv.getFiler();
		this.messager = processingEnv.getMessager();

		this.modules  = new HashSet<>();
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
		if (this.modules == null) // only run in the first round
			return false;

		for (Element ele : roundEnv.getElementsAnnotatedWith(this.annotationClass)) {
			if (!isValidClass(ele)) {
				return false;
			}
			TypeElement classEle = (TypeElement) ele;
			String className     = classEle.getQualifiedName().toString();
			this.modules.add(className);
		}

		String resourceName = "META-INF/services/" + this.interfaceName;
		try {
			// read already listed modules
			try {
				FileObject fo = this.filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
				try (InputStream is = fo.openInputStream()) {
					LineIterator lIter = IOUtils.lineIterator(is, "UTF-8");
					while (lIter.hasNext()) {
						String parserName = lIter.next();
						this.modules.add(parserName);
					}
				}
			} catch (FileNotFoundException ex) {
				/* It's ok if the resource can't get found; we only skip reading it */
			}

			// write new list
			FileObject fo = this.filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
			Writer writer = fo.openWriter();
			for (String line : this.modules) {
				writer.append(line + "\n");
			}
			writer.close();
		} catch (IOException ex) {
			error("Caught IOException: %s", ex.getMessage());
		}

		this.modules = null; // prevent running in subsequent rounds

		return true;
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

		TypeMirror expected = this.elements.getTypeElement(this.interfaceName).asType();
		boolean found = false;
		for (TypeMirror actual : classEle.getInterfaces()) {
			if (this.types.isSameType(actual, expected)) {
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

		if (!classEle.getTypeParameters().isEmpty()) {
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

	private void error(String fmt, Object... args) {
		this.messager.printMessage(Diagnostic.Kind.ERROR, String.format(fmt, args));
		this.modules = null; // we had an error, don't try to run in subsequent rounds
	}

	private void error(Element ele, String fmt, Object... args) {
		this.messager.printMessage(Diagnostic.Kind.ERROR, String.format(fmt, args), ele);
		this.modules = null; // we had an error, don't try to run in subsequent rounds
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
