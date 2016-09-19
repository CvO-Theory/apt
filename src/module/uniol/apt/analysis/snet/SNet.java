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

package uniol.apt.analysis.snet;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.analysis.plain.Plain;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * This package provides a method to check if a Petri net is a s-net.
 * The Petri net must be plain.
 *
 * @author Daniel, Manuel
 * @version 1.0
 */
public class SNet {

	private final PetriNet petriNet;
	private SNetResult result;

	/**
	 * Class constructor.
	 *
	 * @param petriNet Net which will be checked
	 */
	public SNet(PetriNet petriNet) {
		this.petriNet = petriNet;
		this.result = new SNetResult();

	}

	/**
	 * Checks if a Petri net is a s-net.
	 *
	 * @return true: Net is a SNet - false: Net it NOT a SNet
	 * @throws PreconditionFailedException Precondition failed exception
	 */
	public boolean testPlainSNet() throws PreconditionFailedException {
		if (!new Plain().checkPlain(petriNet)) {
			throw new PreconditionFailedException("the net is not plain.");
		}
		// over all transitions
		for (Transition transition : petriNet.getTransitions()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			// is here a synchronization?
			if (transition.getPreset().size() > 1) {
				result.addSynchronizationLabel(transition.getLabel());
				result.setSNet(false);
			}
			// is here a splitting?
			if (transition.getPostset().size() > 1) {
				result.addSplittingLabel(transition.getLabel());
				result.setSNet(false);
			}
		}

		return result.isSNet();
	}

	/**
	 * Get detailed information about test.
	 *
	 * @return result
	 */
	public SNetResult getResult() {
		return this.result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
