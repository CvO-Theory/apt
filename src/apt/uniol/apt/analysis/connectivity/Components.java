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

package uniol.apt.analysis.connectivity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import uniol.apt.adt.INode;

/**
 * This class represents all components of a graph. It is needed for the module system.
 * @author Uli Schlachter
 */
public class Components extends HashSet<Component> {
	public static final long serialVersionUID = 0x1l;

	/**
	 * Constructor.
	 */
	public Components() {
	}

	/**
	 * Copy constructor.
	 * @param c The collection to copy
	 */
	public Components(Collection<? extends Set<? extends INode<?, ?, ?>>> c) {
		super();
		for (Set<? extends INode<?, ?, ?>> s : c)
			add(new Component(s));
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
