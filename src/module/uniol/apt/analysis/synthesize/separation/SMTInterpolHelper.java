/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014-2015  Uli Schlachter
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

package uniol.apt.analysis.synthesize.separation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.collection.CompositeCollection;

import de.uni_freiburg.informatik.ultimate.logic.Logics;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.smtinterpol.DefaultLogger;
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.SMTInterpol;
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.TerminationRequest;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.analysis.synthesize.UnreachableException;
import uniol.apt.util.DifferentPairsIterable;
import uniol.apt.util.DomainEquivalenceRelation;
import uniol.apt.util.IEquivalenceRelation;
import uniol.apt.util.Pair;
import uniol.apt.util.interrupt.InterrupterRegistry;
import static uniol.apt.util.DebugUtil.debug;

/**
 * Helper class for solving separation problems.
 * @author Uli Schlachter
 */
public class SMTInterpolHelper {
	private final Script script;
	private final RegionUtility utility;
	private final PNProperties properties;
	private final String[] locationMap;

	/**
	 * Create a new instance of this class. This prepares an SMTInterpol instance so that Petri nets can be
	 * synthesized. It does so by defining a function called 'isRegion'.
	 * @param utility The region utility for which we are synthesizing.
	 * @param properties The properties that the synthesized net should have.
	 * @param locationMap The location mapping that should be obeyed.
	 * @see getScript
	 */
	public SMTInterpolHelper(RegionUtility utility, PNProperties properties, String[] locationMap) {
		DefaultLogger logger = new DefaultLogger();
		this.script = new SMTInterpol(logger, new TerminationRequest() {
			@Override
			public boolean isTerminationRequested() {
				return InterrupterRegistry.getCurrentThreadInterrupter().isInterruptRequested();
			}
		});
		logger.setLoglevel(DefaultLogger.LOGLEVEL_OFF);
		this.utility = utility;
		this.properties = properties;
		this.locationMap = Arrays.copyOf(locationMap, locationMap.length);

		script.setLogic(Logics.QF_LIA);

		final int numberEvents = utility.getNumberOfEvents();
		final List<String> eventList = utility.getEventList();

		TermVariable[] params;
		TermVariable initialMarking;
		TermVariable[] weight = new TermVariable[numberEvents];
		TermVariable[] backwardWeight = new TermVariable[numberEvents];
		TermVariable[] forwardWeight = new TermVariable[numberEvents];
		TermVariable[] letVariables;
		Term[] letTerms;

		for (int event = 0; event < numberEvents; event++) {
			weight[event] = script.variable("e-" + eventList.get(event), script.sort("Int"));
			backwardWeight[event] = script.variable("b-" + eventList.get(event), script.sort("Int"));
			forwardWeight[event] = script.variable("f-" + eventList.get(event), script.sort("Int"));
		}

		if (properties.isPure()) {
			params = new TermVariable[1 + numberEvents];
			letVariables = new TermVariable[2 * numberEvents];
			letTerms = new Term[2 * numberEvents];

			Term zero = script.numeral(BigInteger.ZERO);
			for (int event = 0; event < numberEvents; event++) {
				params[1 + event] = weight[event];

				letVariables[event] = backwardWeight[event];
				letVariables[event + numberEvents] = forwardWeight[event];

				// If the weight is negative, then backwardWeight = -weight, else backwardWeight = 0
				letTerms[event] =
					script.term("ite", script.term(">", zero, weight[event]),
							script.term("-", weight[event]), zero);
				// If the weight is positive, then forwardWeight = weight, else forwardWeight = 0
				letTerms[event + numberEvents] =
					script.term("ite", script.term("<", zero, weight[event]),
							weight[event], zero);
			}
		} else {
			params = new TermVariable[1 + 2 * numberEvents];
			letTerms = new Term[numberEvents];
			letVariables = weight;
			for (int event = 0; event < numberEvents; event++) {
				params[1 + event] = backwardWeight[event];
				params[1 + event + numberEvents] = forwardWeight[event];
				letTerms[event] = script.term("-", forwardWeight[event], backwardWeight[event]);
			}
		}
		params[0] = script.variable("m0", script.sort("Int"));
		initialMarking = params[0];

		List<Term> isRegion = new ArrayList<>();
		isRegion.addAll(requireRegion(initialMarking, weight, backwardWeight, forwardWeight));

		if (properties.isKBounded())
			isRegion.addAll(requireKBounded(initialMarking, weight, properties.getKForKBounded()));

		// Our definition of conflict-free requires plainness
		if (properties.isPlain() || properties.isConflictFree())
			isRegion.addAll(requirePlainness(initialMarking, backwardWeight, forwardWeight));

		// ON is handled in SeparationUtility by messing with the locationMap
		assert !properties.isOutputNonbranching();

		isRegion.addAll(requireDistributableNet(backwardWeight));
		if (properties.isMergeFree())
			isRegion.addAll(requireMergeFree(forwardWeight));
		if (properties.isConflictFree())
			isRegion.addAll(requireConflictFree(weight, backwardWeight));
		if (properties.isTNet() || properties.isMarkedGraph()) {
			isRegion.addAll(requireTNetOrMarkedGraph(backwardWeight, properties.isMarkedGraph()));
			isRegion.addAll(requireTNetOrMarkedGraph(forwardWeight, properties.isMarkedGraph()));
		}
		if (properties.isHomogeneous())
			isRegion.addAll(requireHomogeneous(backwardWeight));

		if (properties.isKMarking())
			isRegion.addAll(requireKMarking(initialMarking, properties.getKForKMarking()));

		if (properties.isBehaviourallyConflictFree())
			isRegion.addAll(requireBehaviourallyConflictFree(backwardWeight));
		if (properties.isBinaryConflictFree())
			isRegion.addAll(requireBinaryConflictFree(initialMarking, weight, backwardWeight));
		if (properties.isEqualConflict())
			isRegion.addAll(requireEqualConflict(utility, backwardWeight));

		// Now we can define the "isRegion" function
		Term isRegionTerm = collectTerms("and", isRegion.toArray(new Term[isRegion.size()]),
				script.term("true"));
		isRegionTerm = script.let(letVariables, letTerms, isRegionTerm);
		script.defineFun("isRegion", params, script.sort("Bool"), isRegionTerm);
	}

	/**
	 * Get a term describing the marking of the given state.
	 * @param initialMarking A term for the initial marking of the region.
	 * @param weight An array of terms describing the effective weights of transitions.
	 * @param state The state whose marking should be calculated.
	 * @return A term describing the marking of the given state.
	 * @throws UnreachableException if the given state is unreachable.
	 */
	public Term evaluateReachingParikhVector(Term initialMarking, Term[] weight, State state)
			throws UnreachableException {
		Term result = evaluateParikhVector(weight,
				utility.getReachingParikhVector(state));
		return script.term("+", initialMarking, result);
	}

	/**
	 * Get a term describing the effect of the given Parikh vector.
	 * @param weight An array of terms describing the effective weights of transitions.
	 * @param pv The Parikh vector to evaluate.
	 * @return A term describing the effect of the vector.
	 */
	private Term evaluateParikhVector(Term[] weight, List<BigInteger> pv) {
		assert weight.length == pv.size();
		Term[] summands = new Term[weight.length];
		for (int event = 0; event < weight.length; event++) {
			Term addend;
			BigInteger w = pv.get(event);
			if (w.compareTo(BigInteger.ZERO) >= 0)
				addend = script.numeral(w);
			else
				addend = script.term("-", script.numeral(w.negate()));
			summands[event] = script.term("*", addend, weight[event]);
		}
		return collectTerms("+", summands, script.numeral(BigInteger.ZERO));
	}

	/**
	 * Get a list of terms that is required to describe a region.
	 * @param initialMarking A term representing the initial marking of the region.
	 * @param weight Terms representing the effective weights of transitions.
	 * @param backwardWeight Terms representing the backward weights of transitions.
	 * @param forwardWeight Terms representing the forward weights of transitions.
	 * @return A list of terms all together describing a region.
	 * @return An inequality system prepared for calculating separating regions.
	 */
	private List<Term> requireRegion(Term initialMarking, Term[] weight, Term[] backwardWeight,
			Term[] forwardWeight) {
		List<Term> result = new ArrayList<>();
		Term zero = script.numeral(BigInteger.ZERO);

		// Cycles must reach the same marking again
		Set<List<BigInteger>> parikhVectorsOfCycles = new HashSet<>();
		for (Arc chord : utility.getSpanningTree().getChords()) {
			try {
				parikhVectorsOfCycles.add(utility.getParikhVectorForEdge(chord));
			} catch (UnreachableException e) {
				throw new RuntimeException("Chords of a spanning tree cannot belong to "
						+ "unreachable states?!", e);
			}
		}
		for (List<BigInteger> pv : parikhVectorsOfCycles)
			result.add(script.term("=", zero, evaluateParikhVector(weight, pv)));

		// Each arc must be enabled
		for (Arc arc : utility.getTransitionSystem().getEdges()) {
			try {
				int event = utility.getEventIndex(arc.getLabel());
				Term term = evaluateReachingParikhVector(initialMarking, weight, arc.getSource());
				result.add(script.term("<=", backwardWeight[event], term));
			} catch (UnreachableException e) {
				// Just ignore unreachable arcs
				continue;
			}
		}

		// Some terms must not be negative
		result.add(script.term("<=", zero, initialMarking));
		for (Term term : backwardWeight)
			result.add(script.term("<=", zero, term));
		for (Term term : forwardWeight)
			result.add(script.term("<=", zero, term));

		return result;
	}

	/**
	 * Add the needed inequalities so that the system may only produce k-bounded regions.
	 * @param initialMarking A term representing the initial marking of the region.
	 * @param weight Terms representing the effective weights of transitions.
	 * @param k The limit for the bound.
	 * @return The needed terms.
	 */
	private List<Term> requireKBounded(Term initialMarking, Term[] weight, int k) {
		List<Term> result = new ArrayList<>();
		Term biK = script.numeral(BigInteger.valueOf(k));
		for (State state : utility.getTransitionSystem().getNodes()) {
			try {
				Term term = evaluateReachingParikhVector(initialMarking, weight, state);
				result.add(script.term("<=", term, biK));
			} catch (UnreachableException e) {
				continue;
			}
		}
		return result;
	}

	/**
	 * Add the needed inequalities so that the system may only produce plain regions.
	 * @param initialMarking A term representing the initial marking of the region.
	 * @param backwardWeight Terms representing the backward weights of transitions.
	 * @param forwardWeight Terms representing the forward weights of transitions.
	 * @return The needed terms.
	 */
	private List<Term> requirePlainness(Term initialMarking, Term[] backwardWeight, Term[] forwardWeight) {
		List<Term> result = new ArrayList<>();
		Term one = script.numeral(BigInteger.ONE);
		for (int event = 0; event < utility.getNumberOfEvents(); event++) {
			result.add(script.term(">=", one, backwardWeight[event]));
			result.add(script.term(">=", one, forwardWeight[event]));
		}
		return result;
	}

	/**
	 * Add the needed inequalities so that the system may only produce T-Net/marked graph regions for the given
	 * weights. This must be called for both the presets and postsets.
	 * @param weight Terms representing the weights of transitions.
	 * @param markedGraph If this is true, a marked graph is required, else a T-net.
	 * @return The needed terms.
	 */
	private List<Term> requireTNetOrMarkedGraph(Term[] weight, boolean markedGraph) {
		final int numberEvents = utility.getNumberOfEvents();
		Term[] result = new Term[numberEvents];
		Term zero = script.numeral(BigInteger.ZERO);

		for (int event = 0; event < numberEvents; event++) {
			Term[] sum = new Term[utility.getNumberOfEvents() - 1];
			for (int idx = 0; idx < utility.getNumberOfEvents(); idx++) {
				if (idx < event)
					sum[idx] = weight[idx];
				else if (idx > event)
					sum[idx - 1] = weight[idx];
			}
			result[event] = script.term("=", zero, collectTerms("+", sum, zero));
			if (markedGraph)
				result[event] = script.term("and", result[event],
						script.term("<", zero, weight[event]));
		}

		return Collections.singletonList(collectTerms("or", result, script.term("true")));
	}

	/**
	 * Add the needed inequalities to guarantee that a distributable Petri Net region is calculated.
	 * @param backwardWeight Terms representing the backward weights of transitions.
	 * @return The needed terms.
	 */
	private List<Term> requireDistributableNet(Term[] backwardWeight) {
		Set<String> locations = new HashSet<>(Arrays.asList(locationMap));
		locations.remove(null);
		if (locations.isEmpty())
			// No locations specified
			return Collections.emptyList();

		Term zero = script.numeral(BigInteger.ZERO);
		Term[] terms = new Term[locations.size()];
		int index = 0;
		for (String location : locations) {
			Term term = zero;

			// Only events having location "location" may consume token.
			for (int eventIndex = 0; eventIndex < utility.getNumberOfEvents(); eventIndex++) {
				if (locationMap[eventIndex] != null && !locationMap[eventIndex].equals(location))
					term = script.term("+", backwardWeight[eventIndex], term);
			}

			terms[index++] = script.term("=", zero, term);
		}

		return Collections.singletonList(collectTerms("or", terms, script.term("true")));
	}

	/**
	 * Add the needed inequalities to guarantee that a merge-free region is calculated.
	 * @param forwardWeight Terms representing the forward weights of transitions.
	 * @return The needed terms.
	 */
	private List<Term> requireMergeFree(Term[] forwardWeight) {
		final int numberEvents = utility.getNumberOfEvents();
		Term zero = script.numeral(BigInteger.ZERO);
		Term[] result = new Term[numberEvents];

		for (int event = 0; event < numberEvents; event++) {
			Term[] sum = new Term[numberEvents - 1];
			for (int idx = 0; idx < numberEvents; idx++) {
				if (idx < event)
					sum[idx] = forwardWeight[idx];
				else if (idx > event)
					sum[idx - 1] = forwardWeight[idx];
			}
			result[event] = script.term("=", zero, collectTerms("+", sum, zero));
		}

		return Collections.singletonList(collectTerms("or", result, script.term("true")));
	}

	/**
	 * Generate the necessary inequalities for a conflict free solution.
	 * @param weight Terms representing the effective weights of transitions.
	 * @param backwardWeight Terms representing the backward weights of transitions.
	 * @return The needed terms.
	 */
	private List<Term> requireConflictFree(Term[] weight, Term[] backwardWeight) {
		Term[] result = new Term[utility.getNumberOfEvents() + 1];
		Term zero = script.numeral(BigInteger.ZERO);

		// Conflict free: Either there is just a single transition consuming token...
		// (And thus this automatically satisfies any distribution)
		for (int event = 0; event < utility.getNumberOfEvents(); event++) {
			Term[] sum = new Term[utility.getNumberOfEvents() - 1];
			for (int idx = 0; idx < utility.getNumberOfEvents(); idx++) {
				if (idx < event)
					sum[idx] = backwardWeight[idx];
				else if (idx > event)
					sum[idx - 1] = backwardWeight[idx];
			}
			result[event] = script.term("=", zero, collectTerms("+", sum, zero));
		}

		// ...or the preset is contained in the postset
		// (Note that this only works because we require plainness)
		Term[] presetPostset = new Term[utility.getNumberOfEvents()];
		for (int event = 0; event < utility.getNumberOfEvents(); event++) {
			presetPostset[event] = script.term("<=", zero, weight[event]);
		}
		result[utility.getNumberOfEvents()] = collectTerms("and", presetPostset, script.term("false"));

		return Collections.singletonList(collectTerms("or", result, script.term("true")));
	}

	/**
	 * Add the needed inequalities so that the system may only produce homogeneous regions for the given weights.
	 * @param backwardWeight Terms representing the weights of transitions.
	 * @return The needed terms.
	 */
	private List<Term> requireHomogeneous(Term[] backwardWeight) {
		List<Term> result = new ArrayList<>();
		Term zero = script.numeral(BigInteger.ZERO);

		for (Pair<Term, Term> weightPair : new DifferentPairsIterable<>(Arrays.asList(backwardWeight))) {
			result.add(script.term("or",
					script.term("=", zero, weightPair.getFirst()),
					script.term("=", zero, weightPair.getSecond()),
					script.term("=", weightPair.getFirst(), weightPair.getSecond())
				));
		}

		return result;
	}

	/**
	 * Add the necessary constraints to produce a k-marking. A k-marking means that the initial marking of each
	 * place is a multiple of the given k.
	 * @param initialMarking Terms representing the initial marking of a place.
	 * @return The needed terms.
	 */
	private List<Term> requireKMarking(Term initialMarking, int k) {
		return Collections.singletonList(script.term("divisible",
					new BigInteger[] { BigInteger.valueOf(k) },
					null, initialMarking));
	}

	/**
	 * Add the necessary constraints to produce a behaviourally conflict free (BCF) Petri net. A Petri net is BCF if
	 * for every place and every reachable marking, there is at most one activated transition consuming tokens from
	 * p.
	 * @param backwardWeight Terms representing the backwards weights of transitions.
	 * @return The needed terms.
	 */
	private List<Term> requireBehaviourallyConflictFree(Term[] backwardWeight) {
		// Calculate simultaneously activated transitions
		Set<Set<Integer>> simultaneouslyActivated = new HashSet<>();
		for (State state : utility.getTransitionSystem().getNodes()) {
			Set<Integer> activated = new HashSet<>();
			for (Arc arc : state.getPostsetEdges())
				activated.add(utility.getEventIndex(arc.getLabel()));
			if (!activated.isEmpty())
				simultaneouslyActivated.add(activated);
		}

		List<Term> result = new ArrayList<>();
		Term zeroTerm = script.numeral(BigInteger.ZERO);
		// For each set of simultaneously activated transitions...
		for (Set<Integer> activated : simultaneouslyActivated) {
			// ...at most one of them may consume tokens from our region. Which means that all but one do
			// not consume tokens, so the sum of their backwards weights must be zero.
			Term[] terms = new Term[activated.size()];
			int nextTermsIndex = 0;
			for (int allowed : activated) {
				Term[] summands = new Term[activated.size() - 1];
				int nextSummandsIndex = 0;
				for (int notAllowed : activated) {
					if (allowed != notAllowed)
						summands[nextSummandsIndex++] = backwardWeight[notAllowed];
				}
				terms[nextTermsIndex++] = script.term("=", zeroTerm,
						collectTerms("+", summands, zeroTerm));
			}
			result.add(collectTerms("or", terms, script.term("true")));
		}

		return result;
	}

	/**
	 * Add the necessary constraints to produce a binary conflict free (BiCF) Petri net. A Petri net is BiCF if
	 * for every place, every reachable marking and every pair of activated transitions, there are at least as many
	 * tokens on the place as both the transitions consume.
	 * @param initialMarking A term representing the initial marking of the region.
	 * @param weight Terms representing the effective weights of transitions.
	 * @param backwardWeight Terms representing the backwards weights of transitions.
	 * @return The needed terms.
	 */
	private List<Term> requireBinaryConflictFree(Term initialMarking, Term[] weight, Term[] backwardWeight) {
		List<Term> result = new ArrayList<>();
		// For each state...
		for (State state : utility.getTransitionSystem().getNodes()) {
			Term stateMarking;
			try {
				stateMarking = evaluateReachingParikhVector(initialMarking, weight, state);
			} catch (UnreachableException e) {
				continue;
			}

			// ..calculate its simultaneously activated transitions.
			Set<Integer> activated = new HashSet<>();
			for (Arc arc : state.getPostsetEdges())
				activated.add(utility.getEventIndex(arc.getLabel()));

			// For any pair of activated events, make sure enough tokens exist for both of them.
			for (Pair<Integer, Integer> pair : new DifferentPairsIterable<>(activated)) {
				result.add(script.term(">=", stateMarking,
							script.term("+", backwardWeight[pair.getFirst()],
								backwardWeight[pair.getSecond()])));
			}
		}
		return result;
	}

	/**
	 * Add the necessary constraints to produce an equal-conflict (EC) Petri net. A Petri net is EC if is
	 * homogeneous (all transitions consuming tokens from a place do so with the same weight) and transitions with
	 * non-disjoint presets have the same preset.
	 * @param utility The region utility.
	 * @param backwardWeight Terms representing the backwards weights of transitions.
	 * @return The needed terms.
	 */
	private List<Term> requireEqualConflict(RegionUtility utility, Term[] backwardWeight) {
		if (backwardWeight.length == 0)
			return Collections.emptyList();

		TransitionSystem ts = utility.getTransitionSystem();
		DomainEquivalenceRelation<String> relation = new DomainEquivalenceRelation<>(ts.getAlphabet());

		// Begin with assuming that all events are equivalent
		String someEvent = ts.getAlphabet().iterator().next();
		for (String otherEvent : ts.getAlphabet())
			relation.joinClasses(someEvent, otherEvent);

		// Then refine this so that in the end only events are equivalent which are always enabled together
		for (final State state : ts.getNodes()) {
			relation = relation.refine(new IEquivalenceRelation<String>() {
				@Override
				public boolean isEquivalent(String event1, String event2) {
					Set<State> postset1 = state.getPostsetNodesByLabel(event1);
					Set<State> postset2 = state.getPostsetNodesByLabel(event2);
					return postset1.isEmpty() == postset2.isEmpty();
				}
			});
		}
		debug("Enabling-equivalent transitions: ", relation);

		// The postset of the region must be an equivalence class (or the empty set)
		List<Term> result = new ArrayList<>();
		List<String> eventList = utility.getEventList();
		for (Set<String> equivalenceClass : new CompositeCollection<Set<String>>(relation,
					Collections.singleton(Collections.<String>emptySet()))) {
			Term[] current = new Term[eventList.size()];
			Term pivot = null;
			for (int index = 0; index < backwardWeight.length; index++) {
				if (equivalenceClass.contains(utility.getEventList().get(index))) {
					if (pivot == null) {
						// The first event in the class must have non-zero backward weight
						pivot = backwardWeight[index];
						current[index] = script.term("<", script.numeral(BigInteger.ZERO), pivot);
					} else {
						// All other events in the class must have the same weight as pivot
						current[index] = script.term("=", pivot, backwardWeight[index]);
					}
				} else {
					// Events outside the class must have weight zero
					current[index] = script.term("=", script.numeral(BigInteger.ZERO), backwardWeight[index]);
				}
			}
			result.add(collectTerms("and", current, script.term("true")));
		}
		return Arrays.asList(collectTerms("or", result.toArray(new Term[0]), script.term("false")));
	}

	private Term collectTerms(String operation, Term[] terms, Term def) {
		switch (terms.length) {
			case 0:
				return def;
			case 1:
				return terms[0];
			default:
				return script.term(operation, terms);
		}
	}

	/**
	 * Get the prepared SMTInterpol script.
	 * @return The script.
	 */
	public Script getScript() {
		return script;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
