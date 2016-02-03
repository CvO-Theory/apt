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

package uniol.apt.io.renderer.impl;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.Node;
import uniol.apt.adt.pn.Flow;

import java.util.TreeSet;
import java.util.Set;
import java.util.Comparator;
import uniol.apt.adt.pn.Marking;

/** @author Uli Schlachter, vsp */
public class RendererTestUtils {

	/** Hide the constructor */
	private RendererTestUtils() { /* empty */ }

	// Get a TreeSet which contains the elements from the given set and uses the given comparator
	private static <T> TreeSet<T> getOrderedSet(Set<T> elems, Comparator<T> comparator) {
		TreeSet<T> result = new TreeSet<>(comparator);
		result.addAll(elems);
		return result;
	}

	/**
	 * Transform the given nodes into "sorted nodes".
	 * @param klass The klass that mockito should mock for creating the sorted nodes.
	 * @param nodes The nodes that should be transformed
	 * @param comparator The comparator that the resulting TreeSet uses
	 * @param arcComparator The comparator which sorts the pre- and postsets of nodes.
	 * @param <T> The type of elements in the set.
	 * @return The equivalent sorted nodes.
	 */
	private static <T extends Node<?, Flow, uniol.apt.adt.pn.Node>> Set<T> getNodeSet(Class<? extends T> klass,
		Set<? extends T> nodes, Comparator<Node<?, ?, ?>> comparator,
		Comparator<Flow> arcComparator) {
		Set<T> result = new TreeSet<>(comparator);
		for (T node : nodes) {
			T s = mock(klass);
			when(s.getPresetEdges()).thenReturn(getOrderedSet(node.getPresetEdges(), arcComparator));
			when(s.getPostsetEdges()).thenReturn(getOrderedSet(node.getPostsetEdges(), arcComparator));
			when(s.getId()).thenReturn(node.getId());
			result.add(s);
		}
		return result;
	}

	/**
	 * Return a modified view on the given {@link PetriNet} which returns
	 * places, transitions and arcs as sorted sets.
	 * @param pn The {@link PetriNet} to wrap
	 * @return The modified view.
	 */
	static PetriNet getSortedNet(PetriNet pn) {
		/* Create a mockito spy for the Petri net which sorts everything */
		PetriNet newPN = spy(pn);
		final Comparator<Node<?, ?, ?>> nodeComparator = new Comparator<Node<?, ?, ?>>() {

			@Override
			public int compare(Node<?, ?, ?> o1, Node<?, ?, ?> o2) {
				return o1.getId().compareTo(o2.getId());
			}
		};
		final Comparator<Flow> arcComparator = new Comparator<Flow>() {

			@Override
			public int compare(Flow o1, Flow o2) {
				int cmp = nodeComparator.compare(o1.getSource(), o2.getSource());
				if (cmp != 0)
					return cmp;
				return nodeComparator.compare(o1.getTarget(), o2.getTarget());
			}
		};
		Set<Place> places = getNodeSet(Place.class, pn.getPlaces(),
			nodeComparator, arcComparator);
		Set<Transition> transitions = getNodeSet(Transition.class, pn.getTransitions(),
			nodeComparator, arcComparator);
		Set<Flow> flows = getOrderedSet(pn.getEdges(), arcComparator);

		Marking initialMarking = mock(Marking.class);
		Marking orig = pn.getInitialMarking();
		for (Place place : places) {
			String id = place.getId();
			when(initialMarking.getToken(place)).thenReturn(orig.getToken(id));
		}

		when(newPN.getPlaces()).thenReturn(places);
		when(newPN.getTransitions()).thenReturn(transitions);
		when(newPN.getInitialMarking()).thenReturn(initialMarking);
		when(newPN.getEdges()).thenReturn(flows);
		return newPN;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
