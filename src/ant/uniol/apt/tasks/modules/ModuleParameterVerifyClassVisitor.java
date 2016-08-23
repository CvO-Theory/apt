/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
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

package uniol.apt.tasks.modules;

import static org.objectweb.asm.Opcodes.ASM4;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;

/** @author vsp */
public class ModuleParameterVerifyClassVisitor extends ClassVisitor {
	private final ModuleParameterVerifyMethodVisitor provideVisitor   =
			new ModuleParameterVerifyMethodVisitor(ModuleOutputSpec.class, "addReturnValue");
	private final ModuleParameterVerifyMethodVisitor optionalVisitor1 =
			new ModuleParameterVerifyMethodVisitor(ModuleInputSpec.class, "addOptionalParameterWithoutDefault", provideVisitor);
	private final ModuleParameterVerifyMethodVisitor optionalVisitor2 =
			new ModuleParameterVerifyMethodVisitor(ModuleInputSpec.class, "addOptionalParameterWithDefault", optionalVisitor1);
	private final ModuleParameterVerifyMethodVisitor requireVisitor   =
			new ModuleParameterVerifyMethodVisitor(ModuleInputSpec.class, "addParameter", optionalVisitor2);
	private final ModuleParameterVerifyMethodVisitor runInputVisitor  =
			new ModuleParameterVerifyMethodVisitor(ModuleInput.class, "getParameter", requireVisitor);
	private final ModuleParameterVerifyMethodVisitor runOutputVisitor =
			new ModuleParameterVerifyMethodVisitor(ModuleOutput.class, "setReturnValue", runInputVisitor);
	private String name;
	private String superName;

	/** Constructor */
	public ModuleParameterVerifyClassVisitor() {
		super(ASM4);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		this.name = name;
		this.superName = superName;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		return runOutputVisitor;
	}

	/**
	 * @return The internal name of the class.
	 */
	public String getClassName() {
		return name;
	}

	/**
	 * @return The internal name of the super class.
	 */
	public String getSuperclassName() {
		return superName;
	}

	/**
	 * @return Mapping of requested parameters to types or DIFFERENT_TYPES_DETECTED_TYPE if they have more than one type
	 */
	public Map<String, Type> getRequestedInputs() {
		return requireVisitor.getParameterMap();
	}

	/**
	 * @return Mapping of requested optional parameters to types or DIFFERENT_TYPES_DETECTED_TYPE if they have more than one type
	 */
	public Map<String, Type> getOptionalInputs() {
		Map<String, Type> result = new HashMap<>();
		result.putAll(optionalVisitor1.getParameterMap());
		result.putAll(optionalVisitor2.getParameterMap());
		return result;
	}

	/**
	 * @return Mapping of used parameters to types or DIFFERENT_TYPES_DETECTED_TYPE if they have more than one type
	 */
	public Map<String, Type> getUsedInputs() {
		return runInputVisitor.getParameterMap();
	}

	/**
	 * @return Mapping of announced return values to types or DIFFERENT_TYPES_DETECTED_TYPE if they have more than one type
	 */
	public Map<String, Type> getAnnouncedOutputs() {
		return provideVisitor.getParameterMap();
	}

	/**
	 * @return Mapping of provided return values to types or DIFFERENT_TYPES_DETECTED_TYPE if they have more than one type
	 */
	public Map<String, Type> getProvidedOuputs() {
		return runOutputVisitor.getParameterMap();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
