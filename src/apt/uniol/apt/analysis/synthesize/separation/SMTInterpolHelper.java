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

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;

import de.uni_freiburg.informatik.ultimate.logic.Logics;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.SMTInterpol;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.analysis.synthesize.UnreachableException;
import uniol.apt.util.DifferentPairsIterable;
import uniol.apt.util.Pair;

/**
 * Helper class for solving separation problems.
 * @author Uli Schlachter
 */
public class SMTInterpolHelper {
	private final Script script = Log4JInitializationHelper.INSTANCE.createScript();
	private final RegionUtility utility;
	private final PNProperties properties;
	private final String[] locationMap;

	private static class Log4JInitializationHelper {
		public static final Log4JInitializationHelper INSTANCE = new Log4JInitializationHelper();

		private Log4JInitializationHelper() {
			// Set up SMTInterpol in a way that it doesn't produce debug output
			Logger.getRootLogger().addAppender(new NullAppender());
		}

		public Script createScript() {
			return new SMTInterpol(Logger.getRootLogger(), false);
		}
	}

	/**
	 * Create a new instance of this class. This prepares an SMTInterpol instance so that Petri nets can be
	 * synthesized. It does so by defining a function called 'isRegion'.
	 * @param utility The region utility for which we are synthesizing.
	 * @param properties The properties that the synthesized net should have.
	 * @param locationMap The location mapping that should be obeyed.
	 * @see getScript
	 */
	public SMTInterpolHelper(RegionUtility utility, PNProperties properties, String[] locationMap) {
		this.utility = utility;
		this.properties = properties;
		this.locationMap = locationMap;

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
		if (properties.isConflictFree())
			isRegion.addAll(requireConflictFree(weight, backwardWeight));
		if (properties.isTNet() || properties.isMarkedGraph()) {
			isRegion.addAll(requireTNetOrMarkedGraph(backwardWeight, properties.isMarkedGraph()));
			isRegion.addAll(requireTNetOrMarkedGraph(forwardWeight, properties.isMarkedGraph()));
		}
		if (properties.isHomogenous())
			isRegion.addAll(requireHomogenous(backwardWeight));

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
	 * Add the needed inequalities so that the system may only produce homogenous regions for the given weights.
	 * @param backwardWeight Terms representing the weights of transitions.
	 * @return The needed terms.
	 */
	private List<Term> requireHomogenous(Term[] backwardWeight) {
		final int numberEvents = utility.getNumberOfEvents();
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
