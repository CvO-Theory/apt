/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015 Uli Schlachter
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
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;


/**
 * Annotation processor generating ServiceLoader-style SPI information in META-INF.
 * @author Uli Schlachter
 */
abstract public class AbstractSPIServiceProcessor extends AbstractServiceProcessor {
	/**
	 * Constructor
	 *
	 * @param annotationClass Class object which describes the annotation
	 * @param interfaceClass Class object which describes the interface which the annotated classes must implement
	 * @param allowGenerics Are generic implementations of the interface allowed? Else, they are an error
	 */
	protected AbstractSPIServiceProcessor(Class<? extends Annotation> annotationClass, Class<?> interfaceClass,
			boolean allowGenerics) {
		super(annotationClass, interfaceClass, allowGenerics);
	}

	/**
	 * Constructor
	 *
	 * @param annotationClass Class object which describes the annotation
	 * @param interfaceName Full Name of the interface which describes the interface which the annotated classes
	 * must implement
	 * @param allowGenerics Are generic implementations of the interface allowed? Else, they are an error
	 */
	protected AbstractSPIServiceProcessor(Class<? extends Annotation> annotationClass, String interfaceName,
			boolean allowGenerics) {
		super(annotationClass, interfaceName, allowGenerics);
	}

	private Set<String> services;

	@Override
	public synchronized void init(ProcessingEnvironment procEnv) {
		super.init(procEnv);

		this.services  = new HashSet<>();
	}

	@Override
	protected void visitClass(TypeElement classEle, String className) {
		this.services.add(className);
	}

	@Override
	protected void produceOutput() {
		try {
			writeResourceList("META-INF/services/" + this.interfaceName, services);
		} catch (IOException ex) {
			error("Caught IOException: %s", ex.getMessage());
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
