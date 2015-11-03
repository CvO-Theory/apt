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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

import uniol.apt.io.parser.AptParser;
import uniol.apt.io.parser.Parser;

/**
 * Annotation Processor which searches for classes annotated with AptParser, analyzes them and write them in lists in
 * META-INF where the Parsers class can find them.
 *
 * @author vsp
 */
public class ParserProcessor extends AbstractProcessor {
	private Elements elements;
	private Types types;
	private Filer filer;
	private Messager messager;

	private Map<String, Set<String>> parsers;

	private Set<String> getSet(String produces) {
		Set<String> ret = this.parsers.get(produces);
		if (ret == null) {
			ret = new HashSet<>();
			this.parsers.put(produces, ret);
		}
		return ret;
	}

	@Override
	public synchronized void init(ProcessingEnvironment procEnv) {
		super.init(procEnv);
		this.elements = procEnv.getElementUtils();
		this.types    = procEnv.getTypeUtils();
		this.filer    = processingEnv.getFiler();
		this.messager = processingEnv.getMessager();

		this.parsers  = new HashMap<>();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotations = new HashSet<String>();
		annotations.add(AptParser.class.getCanonicalName());
		return annotations;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (this.parsers == null) // only run in the first round
			return false;

		for (Element ele : roundEnv.getElementsAnnotatedWith(AptParser.class)) {
			AptParser annotation = ele.getAnnotation(AptParser.class);
			if (!isValidClass(ele)) {
				return false;
			}
			TypeElement classEle = (TypeElement) ele;
			String className     = classEle.getQualifiedName().toString();
			TypeMirror searched = this.types.erasure(this.elements.getTypeElement(
						Parser.class.getCanonicalName()).asType());
			for (TypeMirror actual : classEle.getInterfaces()) {
				if (this.types.isSameType(this.types.erasure(actual), searched)) {
					String produces = MyTypes.asDeclaredType(actual).getTypeArguments().get(0)
						.toString();
					getSet(produces).add(className);
				}
			}
		}

		try {
			for (Map.Entry<String, Set<String>> entry: this.parsers.entrySet()) {
				String resoureName =  "META-INF/uniol/apt/parsers/" + entry.getKey();
				// read already listed parsers
				try {
					FileObject fo = this.filer.getResource(StandardLocation.CLASS_OUTPUT, "",
							resoureName);
					try (InputStream is = fo.openInputStream()) {
						LineIterator lIter = IOUtils.lineIterator(is, "UTF-8");
						while (lIter.hasNext()) {
							String parserName = lIter.next();
							getSet(entry.getKey()).add(parserName);
						}
					}
				} catch (FileNotFoundException ex) {
					/* It's ok if the resource can't get found; we only skip reading it */
				}

				// write new list
				FileObject fo = this.filer.createResource(StandardLocation.CLASS_OUTPUT, "",
						resoureName);
				Writer writer = fo.openWriter();
				for (String line : entry.getValue()) {
					writer.append(line + "\n");
				}
				writer.close();
			}
		} catch (IOException ex) {
			error("Caught IOException: %s", ex.getMessage());
		}

		this.parsers = null; // prevent running in subsequent rounds

		return true;
	}

	private boolean isValidClass(Element ele) {
		if (ele.getKind() != ElementKind.CLASS) {
			error(ele, "Non-Class %s annotated with %s.", ele.getSimpleName().toString(),
					AptParser.class.getCanonicalName());
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

		TypeMirror expected = this.types.erasure(this.elements.getTypeElement(Parser.class.getCanonicalName())
				.asType());
		boolean found = false;
		for (TypeMirror actual : classEle.getInterfaces()) {
			if (this.types.isSameType(this.types.erasure(actual), expected)) {
				found = true;
				break;
			}
		}
		if (!found) {
			error(classEle, "Class %s doesn't implement interface %s<?>.",
					classEle.getQualifiedName().toString(),
					Parser.class.getCanonicalName());
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
		this.parsers = null; // we had an error, don't try to run in subsequent rounds
	}

	private void error(Element ele, String fmt, Object... args) {
		this.messager.printMessage(Diagnostic.Kind.ERROR, String.format(fmt, args), ele);
		this.parsers = null; // we had an error, don't try to run in subsequent rounds
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
