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

package uniol.apt.analysis.conpres;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * This module tests if a Petri net is concurrency-preserving. That is:
 * <code>\forall t \in T: \mid t^\bullet \mid = \mid {}^\bullet t \mid </code>
 * @author Manuel Gieseking
 */
public class ConcurrencyPreserving {

	private final PetriNet pn;
	private Transition witness = null;

	/**
	 * Constructor.
	 * @param pn - the net which should be checked.
	 */
	public ConcurrencyPreserving(PetriNet pn) {
		this.pn = pn;
	}

	/**
	 * Testing if a Petri net is concurrency-preserving. That is:
	 * \forall t \in T: \mid t^\bullet \mid = \mid {}^\bullet t \mid
	 * @return true if the Petri net is concurrency-preserving.
	 */
	public boolean check() {
		witness = null;
		for (Transition t : pn.getTransitions()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			if (t.getPostset().size() != t.getPreset().size()) {
				witness = t;
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the transition where the pre- and postsets are not equal
	 * if existent otherwise null.
	 *
	 * @return null if check is not called or net is concurrency-preserving.
	 * Otherwise it returns a transition where the preset is not equal to the
	 * postset of this transition. The witness of the last call of check.
	 */
	public Transition getWitness() {
		return witness;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
