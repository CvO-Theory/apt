/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

package uniol.apt.analysis.synthesize;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.logic.ConstantTerm;
import de.uni_freiburg.informatik.ultimate.logic.Model;
import de.uni_freiburg.informatik.ultimate.logic.Rational;
import de.uni_freiburg.informatik.ultimate.logic.ReasonUnknown;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.separation.SMTInterpolHelper;
import uniol.apt.analysis.synthesize.separation.SeparationUtility;
import uniol.apt.util.interrupt.UncheckedInterruptedException;
import uniol.apt.util.DifferentPairsIterable;
import uniol.apt.util.Pair;

import static uniol.apt.util.DebugUtil.debug;
import static uniol.apt.util.DebugUtil.debugFormat;

/**
 * Synthesize a minimal Petri Net from a transition system.
 * @author Uli Schlachter
 */
public class MinimizePN {
	private final SynthesizePN synthesize;
	private final RegionUtility utility;
	private final Set<Region> regions;
	private final boolean onlyEventSeparation;

	/**
	 * Calculate a Petri net with a minimal number of places solving some PN-synthesis problem.
	 * @param synthesize The SynthesizePN instance whose result should be minimized.
	 * @throws UnsupportedOperationException If the given synthesize instance did not successfully separate its
	 * input.
	 * @see synthesizePetriNet
	 */
	public MinimizePN(SynthesizePN synthesize) {
		this.synthesize = synthesize;
		this.utility = synthesize.getUtility();
		this.onlyEventSeparation = synthesize.onlyEventSeparation();

		if (!synthesize.wasSuccessfullySeparated())
			throw new UnsupportedOperationException("Net was not successfully synthesized "
					+ "and thus cannot be minimized");

		try {
			Set<Region> separatingRegions = synthesize.getSeparatingRegions();
			while (!separatingRegions.isEmpty()) {
				debugFormat("Have solution with %d regions, trying to find solution with "
						+ "one region less", separatingRegions.size());
				Set<Region> newRegions = synthesizeWithLimit(separatingRegions.size() - 1);
				if (newRegions == null)
					break;

				// minimizeRegions() can often reduce the number of regions even more
				SynthesizePN.minimizeRegions(utility.getTransitionSystem(), newRegions,
						onlyEventSeparation);
				separatingRegions = newRegions;
			}
			debug("Could not reduce number of regions any more");
			this.regions = Collections.unmodifiableSet(separatingRegions);
		} catch (UnreachableException e) {
			throw new RuntimeException("Previous synthesis was successful "
					+ "and now an unreachable state shows up?!", e);
		}
	}

	/**
	 * Get all separating regions which were calculated
	 * @return All separating regions found.
	 */
	public Set<Region> getSeparatingRegions() {
		return Collections.unmodifiableSet(regions);
	}

	/**
	 * Synthesize a Petri net with the minimal number of places.
	 * @return A minimal Petri net.
	 */
	public PetriNet synthesizePetriNet() {
		return synthesize.synthesizePetriNet(regions);
	}

	private Set<Region> synthesizeWithLimit(int limit) throws UnreachableException {
		Set<Region> result = synthesizeWithLimit(limit, Collections.<State>emptySet());
		if (result == null || onlyEventSeparation)
			// We don't have to look at state separation
			return result;

		Set<State> statesToSeparate = new HashSet<>();
		while (true) {
			Set<State> unseparated = SynthesizePN.calculateUnseparatedStates(
					utility.getTransitionSystem().getNodes(), result);
			debug("Current solution: ", result);
			debug("Unseparated states: ", unseparated);
			if (unseparated.isEmpty())
				// All states were separated, return the result
				return result;

			// Try again, but also force these unseparated states to be separated
			statesToSeparate.addAll(unseparated);
			debug("Trying again, now separating ", statesToSeparate);
			result = synthesizeWithLimit(limit, statesToSeparate);
			if (result == null)
				// We need more than <limit> places
				return result;
		}
	}

	private Set<Region> synthesizeWithLimit(int limit, Set<State> statesToSeparate) throws UnreachableException {
		TransitionSystem ts = utility.getTransitionSystem();
		List<String> eventList = utility.getEventList();
		int numberEvents = utility.getNumberOfEvents();
		PNProperties properties = synthesize.getProperties();
		boolean pure = properties.isPure();
		SMTInterpolHelper helper;

		try {
			// getLocationMap() handles output-nonbranching
			helper = new SMTInterpolHelper(utility, properties.setOutputNonbranching(false),
					SeparationUtility.getLocationMap(utility, properties));
		} catch (MissingLocationException e) {
			throw new RuntimeException("Previous synthesis was successful "
					+ "and now we have a missing location!?", e);
		}
		Script script = helper.getScript();

		// Declare all regions
		Term[][] effects = new Term[limit][];
		for (int i = 0; i < limit; i++) {
			effects[i] = new Term[numberEvents];
			Term[] region;
			if (pure) {
				region = new Term[1 + numberEvents];
				for (int event = 0; event < numberEvents; event++) {
					String suffix = eventList.get(event) + "-" + i;
					script.declareFun("e-" + suffix, new Sort[0], script.sort("Int"));
					region[1 + event] = script.term("e-" + suffix);
					effects[i][event] = region[1 + event];
				}
			} else {
				region = new Term[1 + 2 * numberEvents];
				for (int event = 0; event < numberEvents; event++) {
					String suffix = eventList.get(event) + "-" + i;
					script.declareFun("b-" + suffix, new Sort[0], script.sort("Int"));
					script.declareFun("f-" + suffix, new Sort[0], script.sort("Int"));
					region[1 + event] = script.term("b-" + suffix);
					region[1 + event + numberEvents] = script.term("f-" + suffix);
					effects[i][event] = script.term("-", region[1 + event + numberEvents],
							region[1 + event]);
				}
			}
			script.declareFun("m0-" + i, new Sort[0], script.sort("Int"));
			region[0] = script.term("m0-" + i);
			script.assertTerm(script.term("isRegion", region));
		}

		// Define separation problems and require all of them to be solved
		boolean firstProblem = true;
		for (Pair<State, String> problem : new SynthesizePN.EventStateSeparationProblems(ts)) {
			if (limit == 0) {
				// No regions do not solve anything
				script.assertTerm(script.term("false"));
				break;
			}

			// We require the first region to solve the first separation problem
			int candidateRegions = firstProblem ? 1 : limit;
			Term[] problemSolved = new Term[candidateRegions];
			for (int i = 0; i < candidateRegions; i++) {
				Term term = helper.evaluateReachingParikhVector(script.term("m0-" + i),
						effects[i], problem.getFirst());
				if (pure)
					term = script.term("+", term,
							script.term("e-" + problem.getSecond() + "-" + i));
				else
					term = script.term("-", term,
							script.term("b-" + problem.getSecond() + "-" + i));
				problemSolved[i] = script.term(">", script.numeral(BigInteger.ZERO), term);
			}
			script.assertTerm(collectTerms(script, "or", problemSolved, "false"));
			firstProblem = false;
		}
		if (onlyEventSeparation)
			assert statesToSeparate.isEmpty();
		else
			for (Pair<State, State> problem :
					new DifferentPairsIterable<>(statesToSeparate)) {
				Term[] problemSolved = new Term[limit];
				State state1 = problem.getFirst();
				State state2 = problem.getSecond();
				for (int i = 0; i < limit; i++) {
					Term pv0 = helper.evaluateReachingParikhVector(script.term("m0-" + i),
							effects[i], state1);
					Term pv1 = helper.evaluateReachingParikhVector(script.term("m0-" + i),
							effects[i], state2);
					problemSolved[i] = script.term("not", script.term("=", pv0, pv1));
				}
				script.assertTerm(collectTerms(script, "or", problemSolved, "false"));
			}

		// Is there a model?
		LBool isSat = script.checkSat();
		if (isSat == LBool.UNKNOWN) {
			assert ReasonUnknown.TIMEOUT.equals(script.getInfo(":reason-unknown"))
				: script.getInfo(":reason-unknown");
			throw new UncheckedInterruptedException();
		} else if (isSat == LBool.UNSAT) {
			return null;
		}

		// Extract all regions
		Model model = script.getModel();
		Set<Region> result = new HashSet<>();
		for (int numRegion = 0; numRegion < limit; numRegion++) {
			Region.Builder r;
			if (pure) {
				List<BigInteger> weights = new ArrayList<>();
				for (String event : eventList)
					weights.add(getValue(model,
								script.term("e-" + event + "-" + numRegion)));
				r = Region.Builder.createPure(utility, weights);
			} else {
				List<BigInteger> backwardWeights = new ArrayList<>();
				List<BigInteger> forwardWeights = new ArrayList<>();
				for (String event : eventList) {
					backwardWeights.add(getValue(model,
								script.term("b-" + event + "-" + numRegion)));
					forwardWeights.add(getValue(model,
								script.term("f-" + event + "-" + numRegion)));
				}
				r = new Region.Builder(utility, backwardWeights, forwardWeights);
			}
			BigInteger initialMarking = getValue(model, script.term("m0-" + numRegion));
			result.add(r.withInitialMarking(initialMarking));
		}

		return result;
	}

	private BigInteger getValue(Model model, Term term) {
		Term evald = model.evaluate(term);
		assert evald instanceof ConstantTerm : evald;

		Object value = ((ConstantTerm) evald).getValue();
		assert value instanceof Rational : value;

		Rational rat = (Rational) value;
		assert rat.denominator().equals(BigInteger.ONE) : value;

		return rat.numerator();
	}

	private Term collectTerms(Script script, String operation, Term[] terms, String def) {
		switch (terms.length) {
			case 0:
				return script.term(def);
			case 1:
				return terms[0];
			default:
				return script.term(operation, terms);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
