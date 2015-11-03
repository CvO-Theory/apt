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

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor6;

/**
 * An additional function to work with TypeMirrors
 *
 * @author vsp
 */
public class MyTypes {
	private MyTypes() { /* hide */ }

	/**
	 * Cast a TypeMirror to an DeclaredType.
	 *
	 * @param typeM the TypeMirror to cast
	 * @return the DeclaredType contained in the TypeMirror
	 * @throws IllegalArgumentException if the TypeMirror doesn't represent an DeclaredType
	 */
	public static DeclaredType asDeclaredType(TypeMirror typeM) {
		return typeM.accept(new SimpleTypeVisitor6<DeclaredType, Void>() {
			@Override
			protected DeclaredType defaultAction(TypeMirror e, Void p) {
				throw new IllegalArgumentException(e + " does not represent a declared type.");
			}

			@Override
			public DeclaredType visitDeclared(DeclaredType e, Void p) {
				return e;
			}
		}, null);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
