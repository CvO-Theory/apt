/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
 * Copyright (C)      2017  Uli Schlachter
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

package uniol.apt.analysis.fc;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.analysis.plain.Plain;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Checks whether a given Petri net is a weighted free choice net. That is:
 * \forall t1,t2 \in T: ^{\bullet}t1 \Cap ^{\bullet}t2 \neq \emptyset \Rightarrow ^{\bullet}t1 \eq ^{\bullet}t2.
 * @author Dennis-Michael Borde, Uli Schlachter
 */
public class WeightedFreeChoice {

	/**
	 * Checks whether a given Petri net is a weighted free choice net. That is:
	 * \forall t1,t2 \in T: ^{\bullet}t1 \Cap ^{\bullet}t2 \neq \emptyset \Rightarrow ^{\bullet}t1 \eq ^{\bullet}t2.
	 * @param net - the petri net to check.
	 * @return true if the given net fullfills the freechoice property.
	 */
	public boolean check(PetriNet net) {
		// for each transition t1, t2 ...
		for (Transition t1 : net.getTransitions()) {
			for (Transition t2 : net.getTransitions()) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				// ... get the presets ...
				Collection<?> set1 = t1.getPreset();
				Collection<?> set2 = t2.getPreset();
				// ... and check whether they violate the free choice property.
				if (!Collections.disjoint(set1, set2) && !Objects.equals(set1, set2)) {
					return false;
				}
			}
		}
		// obviously they don't.
		return true;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
