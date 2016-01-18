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
import java.util.NoSuchElementException;

import uniol.apt.adt.pn.PetriNet;
import static uniol.apt.generator.GeneratorUtils.cloneNet;

/**
 * Given some Petri nets, this class provides an Iterable that also adds some isolated transitions to each net.
 * @author Uli Schlachter
 */
public class IsolatedTransitionsGenerator implements Iterable<PetriNet> {
	// The original Petri net which we generate isolated elements for

	private final PetriNet pn;
	// The maximum number of transitions that the nets should have.
	private final int maxTransitions;

	/**
	 * Create a new IsolatedTransitionsGenerator instance. The resulting iterable will first return the original
	 * Petri net and then start adding some isolated elements to it.
	 * @param pn The Petri net to use.
	 * @param maxTransitions The maximum number of transitions that the resulting Petri nets should have.
	 */
	public IsolatedTransitionsGenerator(PetriNet pn, int maxTransitions) {
		this.pn = pn;
		this.maxTransitions = maxTransitions;
	}

	/**
	 * Create a new clone of our Petri net.
	 * @param numIsolated the number of isolated transitions to add.
	 * @return the resulting Petri net
	 */
	private PetriNet getNewNet(int numIsolated) {
		PetriNet net = cloneNet(this.pn, this.pn.getName() + " with " + numIsolated + " isolated transitions");
		for (int i = 0; i < numIsolated; i++) {
			net.createTransition();
		}
		return net;
	}

	@Override
	public Iterator<PetriNet> iterator() {
		return new Iterator<PetriNet>() {
			// The next number of isolated elements to generate.

			private int numIsolated = 0;

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				return pn.getPlaces().size() + numIsolated <= maxTransitions;
			}

			@Override
			public PetriNet next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return getNewNet(numIsolated++);
			}
		};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
