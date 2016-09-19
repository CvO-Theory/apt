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
import java.util.HashSet;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * This class computes if a net is strongly or weakly separable with respect to k.
 *
 * @author Daniel
 *
 */
public class SeparationLogic {
	private final PetriNet petriNet_;
	private final long k;
	private final int maxFiringSequenceLength;

	private ArrayList<ArrayList<String>> firableSequences; // eg: [[t1,t2],[t2,t3]]
	private ArrayList<ArrayList<String>> separatedFirableSequences;

	private SeparationSingleResult result;

	/**
	 * Main idea is to compute reachability graph for net with marking k*M0
	 * (rg_kM0) and reachability graph with marking M0 (rg_M0).
	 *
	 * <p>Note: We check every possible firable sequence -- using Coverabily
	 * Class, so unbounded nets will be sorted out.
	 *
	 * <p>Strongly separation: Try to realize every sequence of rg_kM0 with
	 * sequences from rg_M0. Sequences from rg_M0 can be used k times. It is
	 * allowed to switch between sequences of rg_M0 but the order in a
	 * sequence must not be changed.
	 *
	 * <p>Weakly separation: Same as strongly but here the order in a sequence
	 * can be changed.
	 *
	 *
	 * @param petriNet
	 *                Net which will be checked
	 * @param stronglyCheck
	 *                check strong or weakly
	 * @param initK
	 *                Value for k, k == 0 -&gt; compute the maximum k and
	 *                use it. k must be "ok" - see separation class
	 * @param chosenFiringSequence
	 *                check this given firing sequence - null for unused
	 * @param maxLength
	 *                check all sequences up this length - must be greater
	 *                than 0 - unused if chosenFiringSequence was set
	 * @param fullOutput
	 *                outputs every firing sequence
	 * @throws UnboundedException
	 *                 Unbounded exception
	 */
	public SeparationLogic(PetriNet petriNet, boolean stronglyCheck, long initK,
			ArrayList<String> chosenFiringSequence,
			int maxLength, boolean fullOutput) throws UnboundedException {
		petriNet_ = petriNet;

		this.firableSequences = new ArrayList<>();

		// handling of maximal firing sequence length
		if (chosenFiringSequence == null) {
			this.maxFiringSequenceLength = maxLength;
		} else {
			this.maxFiringSequenceLength = chosenFiringSequence.size();
		}


		// k handling
		// calculate the largest k
		LargestK kCalculator = new LargestK(petriNet);
		long kMax = kCalculator.computeLargestK();

		// auto set if k <= 0:
		if (initK <= 0) {
			this.k = kMax;
		} else {
			this.k = initK;
		}

		// init result
		this.result = new SeparationSingleResult();
		this.result.setStronglyCheck(stronglyCheck);
		this.result.setK(this.k);
		this.result.setLenghtOfTestedSequences(maxLength);

		// check if chosenTransitions are ids of transitions in petri net
		// via set to avoid double outputs
		if (chosenFiringSequence != null) {
			HashSet<String> notExistingTransitions = new HashSet<>();
			for (String chosenTransition : chosenFiringSequence) {
				boolean transitionIsInPetriNet = false;
				for (Transition transition : petriNet.getTransitions()) {
					if (chosenTransition.equals(transition.getId())) {
						transitionIsInPetriNet = true;
					}
				}
				if (!transitionIsInPetriNet) {
					notExistingTransitions.add(chosenTransition);
				}
			}

			if (!notExistingTransitions.isEmpty()) {
				for (String notExistingTransition : notExistingTransitions) {
					result.addNotExistingTransition(notExistingTransition);
				}
				result.setSeparable(false);

				return;
			}
		}
		computeSeparation(chosenFiringSequence, stronglyCheck, fullOutput);
	}


	/**
	 * This method compute the separation.
	 * Will be called after class constructor initialized parameter.
	 *
	 * @param chosenFiringSequence chosenFiringSequence check this given firing sequence - null for unused
	 * @param stronglyCheck true: checks strongly, false: check weakly
	 * @param fullOutput fullOutput outputs every firing sequence
	 * @throws UnboundedException Unbounded exception
	 */
	public final void computeSeparation(ArrayList<String> chosenFiringSequence,
			boolean stronglyCheck, boolean fullOutput) throws UnboundedException {

		if (chosenFiringSequence == null) {
			// compute all possible firable sequences (for k*M0)
			firableSequences = computeFireableSequences(petriNet_, false);
		}

		// compute petri net with marking M0 (petriNet_ has marking k*M0)
		// from k*M0 to M0 -- every place: value of mark divide by k
		for (Place place : petriNet_.getPlaces()) {
			//place.setInitialMark(place.getInitialMark().getValue() / this.k);
			place.setInitialToken(place.getInitialToken().getValue() / this.k);
		}

		// compute all possible firable sequences (for M0)
		separatedFirableSequences = computeFireableSequences(petriNet_, true);

		// restore marking -- so original petri net will be unchanged
		// from M0 to k*M0 -- every place: value of mark * k
		for (Place place : petriNet_.getPlaces()) {
			place.setInitialToken(place.getInitialToken().getValue() * this.k);
		}

		if (chosenFiringSequence != null) {
			// just a single test
			boolean outputSingleFiringSequence = true;
			checkSingleFiringSequence(chosenFiringSequence, stronglyCheck, outputSingleFiringSequence);

			result.setTestedJustASingleSequence(true);
			result.setNumberOfTestedSequences(1);
		} else {
			boolean outputSingleFiringSequence = fullOutput;

			// check every firable sequence (of k*M0)
			for (ArrayList<String> firableSequence : firableSequences) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				checkSingleFiringSequence(firableSequence, stronglyCheck, outputSingleFiringSequence);

				// if not strongly (or not weakly) separable, we can stop here.
				if ((!fullOutput) && (!result.isSeparable())) {
					break;
				}
			}

			result.setTestedJustASingleSequence(false);
			result.setNumberOfTestedSequences(firableSequences.size() + 1); // + empty sequence
		}
	}

	/**
	 * This methode check a single firing sequence.
	 *
	 * Note: Will write weakly and strongly result to class parameter
	 *
	 * @param firableSequence firing sequence to check
	 * @param stronglyCheck true: checks strongly, false: check weakly
	 * @param outputSingleFiringSequence should output be written to log
	 */
	private void checkSingleFiringSequence(ArrayList<String> firableSequence, boolean stronglyCheck,
			boolean outputSingleFiringSequence) {

		boolean thisSequenceSeparable = true;

		// here are all firable sequences which can be realized with net with marking M0
		FirableSequences firableSequencesOfM0 = new FirableSequences(separatedFirableSequences, k);

		// strongly
		if (stronglyCheck) {
			// every step of sequence
			for (int i = 0; i < firableSequence.size(); i++) {
				String wantedFire = firableSequence.get(i);

				// can this be NOT done with marking M0?
				if (!firableSequencesOfM0.fire(wantedFire, true)) {
					// not strong separable
					thisSequenceSeparable = false;
				}
			}
		} else { // weakly
			// every step of sequence
			for (int i = 0; i < firableSequence.size(); i++) {
				String wantedFire = firableSequence.get(i);

				// can this be NOT done with marking M0?
				if (!firableSequencesOfM0.fire(wantedFire, false)) {
					// not weak separable
					thisSequenceSeparable = false;
				}
			}
			// because of random order in weak separation we have to check
			// if a transition "from future" was fired
			if (!firableSequencesOfM0.isThereAValidFiringSequence()) {
				thisSequenceSeparable = false;
			}
		}

		// if this sequence not separable, net is not separable
		if (result.isSeparable() && !thisSequenceSeparable) {
			result.setSeparable(false);
			result.setNotSeparableExampleSequence(firableSequence);
		}

		// save result
		if (outputSingleFiringSequence) {
			result.setTestedSingleSequence(firableSequence);
			result.setPossibleWaySmallNetOrder(
					firableSequencesOfM0.getPossibleWaysSmallNetsOrderArray().get(0));
		}

	}

	/**
	 * This method computes all possible firable sequences.
	 *
	 * @param petriNet the net
	 * @param onlyLongest true: only return sequences with maxLength or at end of reachability graph
	 *                          this can be used by nets with marking M0
	 *                    false: return all sequences
	 *                          this must be used to check net with marking k*M0
	 *
	 * @return List with possible sequences - null if unbounded net.
	 * @throws UnboundedException Unbounded exception
	 */
	private ArrayList<ArrayList<String>> computeFireableSequences(PetriNet petriNet,
			boolean onlyLongest) throws UnboundedException {
		// to save output
		ArrayList<ArrayList<String>> fireableSequences = new ArrayList<>();

		// use coverability graph
		CoverabilityGraph coverability = CoverabilityGraph.get(petriNet);
		TransitionSystem coverabilityGraph = null;

		coverabilityGraph = coverability.toReachabilityLTS();

		if (this.maxFiringSequenceLength > 0) {
			// step recursive through reachability graph
			// just for "root elements"
			for (Arc edge : coverabilityGraph.getInitialState().getPostsetEdges()) {
				ArrayList<String> prefix = new ArrayList<>(0);
				computeFireableSequencesRecursiv(fireableSequences, edge, prefix, onlyLongest, 0);
			}
		}

		return fireableSequences;
	}

	/**
	 * This method will be called recursive to go through reachability graph.
	 *
	 * @param fireableSequences Output will be written to this.
	 * @param edge actual edge
	 * @param prefix prefix to reach actual edge
	 * @param onlyLongest should every sequence be save or just the longest
	 * @param actualLength actual length so we can abort if we reach maximum length
	 */
	private void computeFireableSequencesRecursiv(ArrayList<ArrayList<String>> fireableSequences,
			Arc edge, ArrayList<String> prefix, boolean onlyLongest, int actualLength) {
		InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

		// before using recursive got data, copy array, because of call by reference
		ArrayList<String> prefixCopy = new ArrayList<>(prefix);

		// get actual label
		prefixCopy.add(edge.getLabel());

		actualLength++;

		if (onlyLongest) {
			// save to array list -- only last elements
			if ((edge.getTarget().getPostsetEdges().isEmpty())
					|| (actualLength == this.maxFiringSequenceLength)) {
				fireableSequences.add(prefixCopy);
			}
		} else { // not only longest
			// save everything -- in sorted order
			if (fireableSequences.isEmpty()) {
				fireableSequences.add(prefixCopy);
			} else {
				boolean dataWritten = false;
				for (int i = 0; i < fireableSequences.size(); i++) {
					if (!dataWritten && (fireableSequences.get(i).size() >= prefixCopy.size())) {
						fireableSequences.add(i, prefixCopy);
						dataWritten = true;
					}
				}
				if (!dataWritten) {
					fireableSequences.add(prefixCopy);
				}
			}
		}

		if (actualLength < this.maxFiringSequenceLength) {
			// and pay attention to all following edges
			for (Arc postEdge : edge.getTarget().getPostsetEdges()) {
				computeFireableSequencesRecursiv(fireableSequences, postEdge, prefixCopy,
						onlyLongest, actualLength);
			}
		}
	}

	/**
	 * This returns result.
	 *
	 * @return Output
	 */
	public SeparationSingleResult getResult() {
		return this.result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
