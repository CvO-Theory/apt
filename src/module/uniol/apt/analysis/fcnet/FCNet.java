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

package uniol.apt.analysis.fcnet;

import java.util.Collection;
import java.util.Collections;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.analysis.plain.Plain;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Precondition: plain Petri net
 * Testing if a plain Petri net ist restricted-free-choice. That is:
 * \forall t_1,t_2\in T\colon{}^\bullet t_1\cap{}^\bullet t_2\neq\es\impl|{}^\bullet t_1|=|{}^\bullet t_2|=1
 * @author Manuel Gieseking
 */
public class FCNet {

	private final PetriNet pn;

	/**
	 * Constructor.
	 * @param pn - the net which should be checked.
	 */
	public FCNet(PetriNet pn) {
		this.pn = pn;
	}

	/**
	 * Precondition: plain Petri net
	 * Testing if a plain Petri net ist restricted-free-choice. That is:
	 * \forall t_1,t_s\in T\colon{}^\bullet t_1\cap{}^\bullet t_2\neq\es\impl|{}^\bullet t_1|=|{}^\bullet t_2|=1
	 * @return true if the Petri net ist free-choice
	 * @throws PreconditionFailedException thrown if the given petri net is not plain.
	 */
	public boolean check() throws PreconditionFailedException {
		if (!new Plain().checkPlain(pn)) {
			throw new PreconditionFailedException("the net is not plain.");
		}
		for (Transition t1 : pn.getTransitions()) {
			for (Transition t2 : pn.getTransitions()) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				Collection<?> set1 = t1.getPreset();
				Collection<?> set2 = t2.getPreset();
				if (!(Collections.disjoint(set1, set2) || (set1.size() == 1 && set2.size() == 1))) {
					return false;
				}
			}
		}
		return true;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
