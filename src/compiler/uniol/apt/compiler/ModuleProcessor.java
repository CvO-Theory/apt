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
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import uniol.apt.module.AptModule;
import uniol.apt.module.Module;

/**
 * Annotation processor which scans for an AptModule annotation, analyzes all classes marked by this
 * annotation and writes their names in a file in META-INF
 *
 * @author vsp
 */
public class ModuleProcessor extends AbstractServiceProcessor {
	/**
	 * Constructor
	 */
	public ModuleProcessor() {
		super(AptModule.class, Module.class, false);
	}

	private Set<String> modules;

	@Override
	public synchronized void init(ProcessingEnvironment procEnv) {
		super.init(procEnv);

		this.modules  = new HashSet<>();
	}

	@Override
	protected void visitClass(TypeElement classEle, String className) {
		this.modules.add(className);
	}

	@Override
	protected void produceOutput() {
		try {
			writeResourceList("META-INF/services/" + this.interfaceName, modules);
		} catch (IOException ex) {
			error("Caught IOException: %s", ex.getMessage());
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
