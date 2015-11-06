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

package uniol.apt.generator.isolated;

import java.util.Iterator;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.generator.AbstractPetriNetIterableWrapper;

/**
 * Given some Petri nets, this class provides an Iterable that also returns some variations in the number of isolated
 * transitions for the nets.
 * @author Uli Schlachter
 */
public class IsolatedTransitionsIterable extends AbstractPetriNetIterableWrapper {
	// The maximum number of transitions that the nets should have.
	private final int maxTransitions;

	/**
	 * Create a new instance that returns Petri nets for some variations in the number of isolated transitions for
	 * the nets.
	 * @param iterable the list of Petri nets that should be used
	 * @param maxTransitions The maximum number of transitions that the resulting Petri nets should have.
	 */
	public IsolatedTransitionsIterable(Iterable<PetriNet> iterable, int maxTransitions) {
		super(iterable);
		this.maxTransitions = maxTransitions;
	}

	@Override
	protected Iterator<PetriNet> newSubIterator(PetriNet pn) {
		return new IsolatedTransitionsGenerator(pn, maxTransitions).iterator();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
