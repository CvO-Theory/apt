/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

import de.uni_freiburg.informatik.ultimate.logic.ConstantTerm;
import de.uni_freiburg.informatik.ultimate.logic.Logics;
import de.uni_freiburg.informatik.ultimate.logic.Model;
import de.uni_freiburg.informatik.ultimate.logic.Rational;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.SMTInterpol;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.analysis.synthesize.UnreachableException;

import static uniol.apt.util.DebugUtil.debug;

/**
 * Helper class for solving separation problems.
 * @author Uli Schlachter
 */
class InequalitySystemSeparation implements Separation {
	private final RegionUtility utility;
	private final PNProperties properties;
	private final String[] locationMap;
	private final Script script = Log4JInitializationHelper.INSTANCE.createScript();
	private final Term regionInitialMarking;
	private final Term[] regionWeights;
	private final Term[] regionBackwardWeights;
	private final Term[] regionForwardWeights;

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
	 * Construct a new instance for solving separation problems.
	 * @param utility The region utility to use.
	 * @param properties Properties that the calculated region should satisfy.
	 * @param locationMap Mapping that describes the location of each event.
	 */
	public InequalitySystemSeparation(RegionUtility utility, PNProperties properties, String[] locationMap) {
		final int numberEvents = utility.getNumberOfEvents();
		final List<String> eventList = utility.getEventList();
		this.utility = utility;
		this.properties = new PNProperties(properties);
		this.locationMap = locationMap;
		script.setLogic(Logics.QF_LIA);

		defineIsRegion();

		// Finally, we define the needed variables
		Sort[] emptySort = new Sort[0];
		regionWeights = new Term[numberEvents];
		script.declareFun("m0", emptySort, script.sort("Int"));
		regionInitialMarking = script.term("m0");

		Term[] params = new Term[1 + numberEvents + (properties.isPure() ? 0 : numberEvents)];
		params[0] = regionInitialMarking;
		if (properties.isPure()) {
			regionBackwardWeights = null;
			regionForwardWeights = null;
			for (int event = 0; event < eventList.size(); event++) {
				script.declareFun("e-" + eventList.get(event), emptySort, script.sort("Int"));
				regionWeights[event] = script.term("e-" + eventList.get(event));
				params[1 + event] = regionWeights[event];
			}
		} else {
			regionBackwardWeights = new Term[numberEvents];
			regionForwardWeights = new Term[numberEvents];
			for (int event = 0; event < eventList.size(); event++) {
				String evStr = eventList.get(event);
				script.declareFun("b-" + evStr, emptySort, script.sort("Int"));
				script.declareFun("f-" + evStr, emptySort, script.sort("Int"));
				regionBackwardWeights[event] = script.term("b-" + evStr);
				regionForwardWeights[event] = script.term("f-" + evStr);

				regionWeights[event] = script.term("-", script.term("f-" + evStr),
						script.term("b-" + evStr));

				params[1 + event] = regionBackwardWeights[event];
				params[1 + event + numberEvents] = regionForwardWeights[event];
			}
		}
		script.assertTerm(script.term("isRegion", params));
	}

	/**
	 * Define the isRegion function in the script.
	 */
	private void defineIsRegion() {
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
		initialMarking = params[0] = script.variable("m0", script.sort("Int"));

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
		if (properties.isTNet()) {
			isRegion.addAll(requireTNet(backwardWeight));
			isRegion.addAll(requireTNet(forwardWeight));
		}

		// Now we can define the "isRegion" function
		Term isRegionTerm = script.term("and", isRegion.toArray(new Term[isRegion.size()]));
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
	private Term evaluateReachingParikhVector(Term initialMarking, Term[] weight, State state) throws UnreachableException {
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
	private Term evaluateParikhVector(Term[] weight, List<Integer> pv) {
		assert weight.length == pv.size();
		Term[] summands = new Term[weight.length];
		for (int event = 0; event < weight.length; event++) {
			Term addend;
			int w = pv.get(event);
			if (w >= 0)
				addend = script.numeral(BigInteger.valueOf(w));
			else
				addend = script.term("-", script.numeral(BigInteger.valueOf(w).negate()));
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
	private List<Term> requireRegion(Term initialMarking, Term[] weight, Term[] backwardWeight, Term[] forwardWeight) {
		List<Term> result = new ArrayList<>();
		final int events = utility.getNumberOfEvents();
		Term zero = script.numeral(BigInteger.ZERO);

		// Cycles must reach the same marking again
		for (Arc chord : utility.getSpanningTree().getChords()) {
			try {
				List<Integer> pv = utility.getParikhVectorForEdge(chord);
				Term term = evaluateParikhVector(weight, pv);
				term = script.term("=", zero, term);
				result.add(term);
			} catch (UnreachableException e) {
				throw new RuntimeException("Chords of a spanning tree cannot belong to "
						+ "unreachable states?!", e);
			}
		}

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

		// Some terms must not be zero
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
		Term K = script.numeral(BigInteger.valueOf(k));
		for (State state : utility.getTransitionSystem().getNodes()) {
			try {
				Term term = evaluateReachingParikhVector(initialMarking, weight, state);
				result.add(script.term("<=", term, K));
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
	 * Add the needed inequalities so that the system may only produce T-Net regions for the given weights. This
	 * must be called for both the presets and postsets.
	 * @param weight Terms representing the weights of transitions.
	 * @return The needed terms.
	 */
	private List<Term> requireTNet(Term[] weight) {
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
		}

		return Collections.singletonList(collectTerms("or", result, script.term("true")));
	}

	/**
	 * Add the needed inequalities to guarantee that a distributable Petri Net region is calculated.
	 * @param backwardWeight Terms representing the backward weights of transitions.
	 * @return The needed terms.
	 */
	private List<Term> requireDistributableNet(Term[] backwardWeight) {
		List<Term> result = new ArrayList<>();
		Set<String> locations = new HashSet<>(Arrays.asList(locationMap));
		locations.remove(null);
		if (locations.isEmpty())
			// No locations specified
			return result;

		Term zero = script.numeral(BigInteger.ZERO);
		for (String location : locations) {
			Term term = zero;

			// Only events having location "location" may consume token.
			for (int eventIndex = 0; eventIndex < utility.getNumberOfEvents(); eventIndex++) {
				if (locationMap[eventIndex] != null && !locationMap[eventIndex].equals(location))
					term = script.term("+", backwardWeight[eventIndex], term);
			}

			result.add(script.term("=", zero, term));
		}

		return result;
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
		Term sumOfAllBackwards = collectTerms("+", backwardWeight, zero);

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
	 * Try to get a region from the script.
	 * @return A region or null.
	 */
	private Region regionFromSolution() {
		LBool isSat = script.checkSat();
		if (isSat != LBool.SAT)
			return null;

		Model model = script.getModel();
		Region r;
		if (properties.isPure()) {
			List<Integer> weights = new ArrayList<>();
			for (Term term : regionWeights)
				weights.add(getIntValue(model, term));
			r = Region.createPureRegionFromVector(utility, weights);
		} else {
			List<Integer> backwardWeight = new ArrayList<>();
			List<Integer> forwardWeight = new ArrayList<>();
			for (Term term : regionBackwardWeights)
				backwardWeight.add(getIntValue(model, term));
			for (Term term : regionForwardWeights)
				forwardWeight.add(getIntValue(model, term));
			r = new Region(utility, backwardWeight, forwardWeight);
		}
		r = r.withInitialMarking(getIntValue(model, regionInitialMarking));
		debug("region: ", r);

		assert r.getNormalRegionMarking() <= r.getInitialMarking() : model;
		return r;
	}

	/**
	 * Get a region solving some separation problem.
	 * @param state The first state of the separation problem
	 * @param otherState The second state of the separation problem
	 * @return A region solving the problem or null.
	 */
	@Override
	public Region calculateSeparatingRegion(State state, State otherState) {
		// Unreachable states cannot be separated
		if (!utility.getSpanningTree().isReachable(state) || !utility.getSpanningTree().isReachable(otherState))
			return null;

		script.push(1);
		try {
			// We want r_S(s) != r_S(s'). Since for each region there exists a complementary region (we are
			// only looking at the bounded case!), we can require r_S(s) < r_S(s')
			Term term1 = evaluateReachingParikhVector(regionInitialMarking, regionWeights, state);
			Term term2 = evaluateReachingParikhVector(regionInitialMarking, regionWeights, state);
			script.assertTerm(script.term("<", term1, term2));

			return regionFromSolution();
		} catch (UnreachableException e) {
			throw new AssertionError("Made sure state is reachable, but still it isn't?!", e);
		} finally {
			script.pop(1);
		}
	}

	/**
	 * Get a region solving some separation problem.
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 * @return A region solving the problem or null.
	 */
	@Override
	public Region calculateSeparatingRegion(State state, String event) {
		// Unreachable states cannot be separated
		if (!utility.getSpanningTree().isReachable(state))
			return null;

		script.push(1);
		try {
			final int eventIndex = utility.getEventIndex(event);

			// Each state must be reachable in the resulting region, but event 'event' should be disabled
			// in state. We want -1 >= r_S(s) - r_B(event)
			Term marking = evaluateReachingParikhVector(regionInitialMarking, regionWeights, state);

			Term term;
			if (properties.isPure()) {
				// In the pure case, in the above -r_B(event) is replaced with +r_E(event). Since all
				// states must be reachable, this makes sure that r_E(event) really is negative and thus
				// the resulting region solves ESSP.
				term = script.term("+", marking, regionWeights[eventIndex]);
			} else {
				term = script.term("-", marking, regionBackwardWeights[eventIndex]);
			}

			script.assertTerm(script.term(">", script.numeral(BigInteger.ZERO), term));
			return regionFromSolution();
		} catch (UnreachableException e) {
			throw new AssertionError("Made sure state is reachable, but still it isn't?!", e);
		} finally {
			script.pop(1);
		}
	}

	private int getIntValue(Model model, Term term) {
		term = model.evaluate(term);
		assert term instanceof ConstantTerm : term;

		Object value = ((ConstantTerm) term).getValue();
		assert value instanceof Rational : value;

		Rational rat = (Rational) value;
		assert rat.denominator().equals(BigInteger.ONE) : value;
		return rat.numerator().intValue();
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
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
