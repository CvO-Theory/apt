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
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Abstract annotation processor base class which scans for an annotation, analyzes all classes marked by this
 * annotation and writes their names in files in META-INF
 *
 * @author vsp
 */
public abstract class AbstractAptServiceProcessor extends AbstractServiceProcessor {
	private Map<String, Set<String>> services;

	/**
	 * Constructor
	 *
	 * @param annotationClass Class object which describes the annotation
	 * @param interfaceClass Class object which describes the interface which the annotated classes must implement
	 */
	protected AbstractAptServiceProcessor(Class<? extends Annotation> annotationClass, Class<?> interfaceClass) {
		super(annotationClass, interfaceClass, false);
	}

	/**
	 * Constructor
	 *
	 * @param annotationClass Class object which describes the annotation
	 * @param interfaceName Full Name of the interface which describes the interface which the annotated classes
	 * must implement
	 */
	protected AbstractAptServiceProcessor(Class<? extends Annotation> annotationClass, String interfaceName) {
		super(annotationClass, interfaceName, false);
	}

	private Set<String> getSet(String type) {
		Set<String> ret = this.services.get(type);
		if (ret == null) {
			ret = new HashSet<>();
			this.services.put(type, ret);
		}
		return ret;
	}

	@Override
	public synchronized void init(ProcessingEnvironment procEnv) {
		super.init(procEnv);

		this.services  = new HashMap<>();
	}

	@Override
	protected void visitClass(TypeElement classEle, String className) {
		TypeMirror searched = this.types.erasure(this.elements.getTypeElement(this.interfaceName)
				.asType());
		for (TypeMirror actual : classEle.getInterfaces()) {
			if (this.types.isSameType(this.types.erasure(actual), searched)) {
				String type = MyTypes.asDeclaredType(actual).getTypeArguments().get(0)
					.toString();
				getSet(type).add(className);
			}
		}
	}

	@Override
	protected void produceOutput() {
		try {
			String resourcePrefix = "META-INF/uniol/apt/compiler/" + this.interfaceName + "/";
			for (Map.Entry<String, Set<String>> entry: this.services.entrySet()) {
				writeResourceList(resourcePrefix + entry.getKey(), entry.getValue());
			}
		} catch (IOException ex) {
			error("Caught IOException: %s", ex.getMessage());
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
