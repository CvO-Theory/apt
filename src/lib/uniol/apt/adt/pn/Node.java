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

package uniol.apt.adt.pn;

/**
 * Represents a node of a Petri net. That means it can either be a Transition or a Place. It's just a class for hiding
 * the Generics.
 * @author Manuel Gieseking
 */
public class Node extends uniol.apt.adt.Node<PetriNet, Flow, Node> {

	/**
	 * Constructor for creating a Node.
	 * @param net the net this node belongs to.
	 * @param id  the id this node should have.
	 */
	Node(PetriNet net, String id) {
		super(net, id);
	}

	/**
	 * Constructor for copying a Node. The constructor also copies the references of the extensions.
	 * @param net the net this node belongs to.
	 * @param n   the node that should get copied.
	 */
	Node(PetriNet net, Node n) {
		super(net, n);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
