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

package uniol.apt.analysis.separation;

import java.util.ArrayList;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;

/**
 * This class calls the separation logic class.
 *
 * @author Daniel
 *
 */
public class Separation {
	private SeparationResult result;


	/**
	 * Class constructor.
	 * Calls SeparationLogic
	 *
	 * @param petriNet petriNet Net which will be checked
	 * @param stronglyCheck stronglyCheck check strong or weakly
	 * @param initK initK Value for k, k == 0 -&gt; compute the maximum k and use it
	 * @param chosenFiringSequence check this given firing sequence
	 * @param verbose if true method will not stop after first counterexample
	 * @throws UnboundedException Unbounded exception
	 */
	public Separation(PetriNet petriNet, boolean stronglyCheck, long initK,
			ArrayList<String> chosenFiringSequence, boolean verbose) throws UnboundedException {
		// calculate the largest k
		LargestK kCalculator = new LargestK(petriNet);
		long kMax = kCalculator.computeLargestK();
		boolean foundNonSeparableExample = false;

		// auto set if initK <= 0:
		if (initK <= 0) {
			initK = kMax;
		}

		result = new SeparationResult();
		result.setTestedJustASingleSequence(true);
		result.setStronglyCheck(stronglyCheck);
		result.setkMax(kMax);
		result.setK(initK);
		result.setTestedSequence(chosenFiringSequence);

		if (kMax == 0) {
			result.setNoMarks(true);
			result.setSeparable(true);
		} else if (initK > kMax) {
			result.setkGreaterKMax(true);
			result.setSeparable(false);
		} else if ((kMax % initK) != 0) {
			result.setkNoDivisorOfKMax(true);
			result.setSeparable(false);
		} else { // k is ok
			if (!isSequenceASequenceOfNet(petriNet, chosenFiringSequence)) {
				result.setNoSequenceOfNet(true);
				result.setSeparable(false);
			} else { // is a sequence
				for (long i = initK; i > 1; i--) {  // test k and all divisor of k
					if ((initK % i) == 0) { // is it a divisor of k?
						SeparationLogic separation = new SeparationLogic(
							petriNet, stronglyCheck, i, chosenFiringSequence, 0, true);

						this.result.setSeparable(separation.getResult().isSeparable());

						// here we get the output of the check (for every k)
						this.result.addSingleResult(separation.getResult());

						if (this.result.isSeparable() == false) {
							if (verbose) {
								foundNonSeparableExample = true;
							} else {
								return;
							}
						}
					}
				}
				if (!foundNonSeparableExample) {
					this.result.setNoCounterExampleFound(true);
				}
			}
		}
	}

	/**
	 * Class constructor.
	 * Calls SeparationLogic
	 *
	 * @param petriNet petriNet Net which will be checked
	 * @param stronglyCheck stronglyCheck check strong or weakly
	 * @param initK initK Value for k, k == 0 -&gt; compute the maximum k and use it
	 * @param maxLength maxLength check all sequences up this length
	 * @param verbose outputs every firing sequence
	 * @throws UnboundedException Unbounded exception
	 */
	public Separation(PetriNet petriNet, boolean stronglyCheck, long initK,
			int maxLength, boolean verbose) throws UnboundedException {
		// calculate the largest k
		LargestK kCalculator = new LargestK(petriNet);
		long kMax = kCalculator.computeLargestK();
		boolean foundNonSeparableExample = false;

		// auto set if initK == 0:
		if (initK == 0) {
			initK = kMax;
		}

		result = new SeparationResult();
		result.setTestedJustASingleSequence(false);
		result.setMaxLength(maxLength);
		result.setkMax(kMax);
		result.setK(initK);


		if (kMax == 0) {
			result.setNoMarks(true);
			result.setSeparable(true);
		} else if (initK > kMax) {
			result.setkGreaterKMax(true);
			result.setSeparable(false);
		} else if ((kMax % initK) != 0) {
			result.setkNoDivisorOfKMax(true);
			result.setSeparable(false);
		} else { // k is ok
			for (long i = initK; i > 1; i--) { // test k and all divisor of k
				if ((initK % i) == 0) { // is it a divisor of k?
					SeparationLogic separation = new SeparationLogic(
						petriNet, stronglyCheck, i, null, maxLength, false);

					this.result.setSeparable(separation.getResult().isSeparable());

					// here we get the output of the check (for every k)
					this.result.addSingleResult(separation.getResult());

					if (this.result.isSeparable() == false) {
						if (verbose) {
							foundNonSeparableExample = true;
						} else {
							return;
						}
					}
				}
			}

			if (!foundNonSeparableExample) {
				this.result.setNoCounterExampleFound(true);
			}

		}
	}

	/**
	 * Checks if a firing sequence is a sequence of the net
	 *
	 * @param petriNet Net which will be checked
	 * @param chosenFiringSequence given firing sequence
	 * @return result
	 */
	public static boolean isSequenceASequenceOfNet(PetriNet petriNet,
			ArrayList<String> chosenFiringSequence) {
		CoverabilityGraph coverability = CoverabilityGraph.get(petriNet);
		TransitionSystem coverabilityGraph = null;

		// in separationLogic we use toReachabilityLTS() ...
		coverabilityGraph = coverability.toCoverabilityLTS();

		State node = coverabilityGraph.getInitialState();

		// all transition of firing sequence
		for (String transition : chosenFiringSequence) {
			State nextNode = null;
			// search for transition in next possible edges
			for (Arc edge : node.getPostsetEdges()) {
				if (transition.equals(edge.getLabel())) {
					nextNode = edge.getTarget();
				}
			}
			// no matching node was found
			if (nextNode == null) {
				return (false);
			} else {
				node = nextNode;
			}
		}

		return (true);
	}

	/**
	 * This returns output.
	 *
	 * @return Output
	 */
	public String getOutputLog() {
		return result.toString();
	}

	/**
	 * Returns if a net is separable
	 *
	 * Note: true means that no counterexample was found
	 *
	 * @return separable?
	 */
	public boolean isSeparable() {
		return result.isSeparable();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
