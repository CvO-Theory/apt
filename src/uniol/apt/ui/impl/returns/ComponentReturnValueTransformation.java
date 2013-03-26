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

package uniol.apt.ui.impl.returns;

import uniol.apt.adt.INode;
import uniol.apt.analysis.connectivity.Component;
import uniol.apt.ui.ReturnValueTransformation;

/**
 * Transform a component of a graph into a string listing the IDs.
 * @author Uli Schlachter
 */
public class ComponentReturnValueTransformation implements ReturnValueTransformation<Component> {
	@Override
	public String transform(Component component) {
		return transformComponent(component);
	}

	/**
	 * Transform the given component into a string describing its content.
	 * @param component The component to transform.
	 * @return A human readable description of the component.
	 */
	static public String transformComponent(Component component) {
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;

		for (INode<?, ?, ?> node : component) {
			if (!first)
				sb.append(", ");
			sb.append(node.getId());
			first = false;
		}

		sb.append("]");
		return sb.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
