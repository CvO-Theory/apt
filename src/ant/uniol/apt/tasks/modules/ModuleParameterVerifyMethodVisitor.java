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

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.ASM4;

/** @author vsp */
public class ModuleParameterVerifyMethodVisitor extends MethodVisitor {
	/**
	 * Marker class, references to the Type of this class get used to mark that a parameter occurs with different
	 * Types
	 */
	private static interface DifferentTypesDetected { /* empty */ }

	static public final Type DIFFERENT_TYPES_DETECTED_TYPE = Type.getType(DifferentTypesDetected.class);
	private final String targetClass;
	private final String targetMethod;
	private final Map<String, Type> parameterMap = new HashMap<String, Type>() {
		public static final long serialVersionUID = 0xfee1deadL;

		@Override
		public Type put(String key, Type value) {
			Type old = get(key);

			if (old != null && !old.equals(value)) {
				value = DIFFERENT_TYPES_DETECTED_TYPE;
			}

			return super.put(key, value);
		}
	};

	/** The last loaded constant of type String */
	private String ldcLastString = null;
	/** A string constant which might be the name parameter of the method */
	private String ldcName       = null;
	/** A class type constant which might be the klass parameter of the method */
	private Type   ldcClass      = null;

	/**
	 * Constructor
	 *
	 * @param clazz Calls to which class or interface should be searched?
	 * @param method Calls to which method should be searched?
	 * @param mv next MethodVisitor (for chaining)
	 */
	ModuleParameterVerifyMethodVisitor(Class<?> clazz, String method, MethodVisitor mv) {
		super(ASM4, mv);
		targetClass  = Type.getType(clazz).getInternalName();
		targetMethod = method;
	}

	/**
	 * Constructor
	 *
	 * @param clazz Calls to which class or interface should be searched?
	 * @param method Calls to which method should be searched?
	 */
	ModuleParameterVerifyMethodVisitor(Class<?> clazz, String method) {
		this(clazz, method, null);
	}

	@Override
	public void visitLdcInsn(Object cst) {
		super.visitLdcInsn(cst);

		if (cst instanceof String) {
			// constant is a string constant
			ldcLastString = (String) cst;
		} else if (cst instanceof Type) {
			// constant doesn't references a basic type
			Type typeCst = (Type) cst;
			switch (typeCst.getSort()) {
				case Type.ARRAY:
				case Type.OBJECT:
					// constant references a class, which could be the second parameter of the method
					// the string constant which we got before this could be the first parameter
					ldcClass = typeCst;
					ldcName  = ldcLastString;
					break;
				default:
					/* do nothing */
			}
		}
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		super.visitMethodInsn(opcode, owner, name, desc);

		if (owner.equals(targetClass) && name.equals(targetMethod)) {
			// the observed method get called
			if (ldcName != null && ldcClass != null) {
				parameterMap.put(ldcName, ldcClass);
			} else {
				// too complex program structure or the compiler didn't produced the expected bytecode
				// => silently fail
			}

			// we don't support nested calls of this methods => clear constants
			ldcLastString  = null;
			ldcName        = null;
			ldcClass       = null;
		}
	}

	/**
	 * @return Mapping of parameters to types or DIFFERENT_TYPES_DETECTED_TYPE if they have more than one type
	 */
	Map<String, Type> getParameterMap() {
		return parameterMap;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
