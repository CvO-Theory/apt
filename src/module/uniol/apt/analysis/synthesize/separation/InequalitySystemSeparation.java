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
import java.util.List;


import de.uni_freiburg.informatik.ultimate.logic.ConstantTerm;
import de.uni_freiburg.informatik.ultimate.logic.Model;
import de.uni_freiburg.informatik.ultimate.logic.Rational;
import de.uni_freiburg.informatik.ultimate.logic.ReasonUnknown;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;

import uniol.apt.adt.ts.State;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.analysis.synthesize.UnreachableException;
import uniol.apt.util.interrupt.UncheckedInterruptedException;

import static uniol.apt.util.DebugUtil.debug;

/**
 * Helper class for solving separation problems.
 * @author Uli Schlachter
 */
class InequalitySystemSeparation implements Separation {
	private final SMTInterpolHelper helper;
	private final Script script;
	private final RegionUtility utility;
	private final PNProperties properties;
	private final Term regionInitialMarking;
	private final Term[] regionWeights;
	private final Term[] regionBackwardWeights;
	private final Term[] regionForwardWeights;

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
		this.properties = properties;
		this.helper = new SMTInterpolHelper(utility, properties, locationMap);
		this.script = helper.getScript();

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
	 * Try to get a region from the script.
	 * @return A region or null.
	 */
	private Region regionFromSolution() {
		LBool isSat = script.checkSat();
		if (isSat == LBool.UNKNOWN) {
			assert ReasonUnknown.TIMEOUT.equals(script.getInfo(":reason-unknown"))
				: script.getInfo(":reason-unknown");
			throw new UncheckedInterruptedException();
		} else if (isSat == LBool.UNSAT) {
			return null;
		}

		Model model = script.getModel();
		Region.Builder builder;
		if (properties.isPure()) {
			List<BigInteger> weights = new ArrayList<>();
			for (Term term : regionWeights)
				weights.add(getValue(model, term));
			builder = Region.Builder.createPure(utility, weights);
		} else {
			List<BigInteger> backwardWeight = new ArrayList<>();
			List<BigInteger> forwardWeight = new ArrayList<>();
			for (Term term : regionBackwardWeights)
				backwardWeight.add(getValue(model, term));
			for (Term term : regionForwardWeights)
				forwardWeight.add(getValue(model, term));
			builder = new Region.Builder(utility, backwardWeight, forwardWeight);
		}
		Region r = builder.withInitialMarking(getValue(model, regionInitialMarking));
		debug("region: ", r);

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
			// We want r_S(s) != r_S(s'). Note that we cannot just strengthen this to "<", because e.g.
			// locations and output-nonbranching mean that for some regions, there might not be a
			// complementary region and so "!=" could be solvable, but "<" unsolvable.
			Term term1 = helper.evaluateReachingParikhVector(regionInitialMarking, regionWeights, state);
			Term term2 = helper.evaluateReachingParikhVector(regionInitialMarking, regionWeights,
					otherState);
			script.assertTerm(script.term("not", script.term("=", term1, term2)));

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
			Term marking = helper.evaluateReachingParikhVector(regionInitialMarking, regionWeights, state);

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

	private BigInteger getValue(Model model, Term term) {
		Term evald = model.evaluate(term);
		assert evald instanceof ConstantTerm : evald;

		Object value = ((ConstantTerm) evald).getValue();
		assert value instanceof Rational : value;

		Rational rat = (Rational) value;
		assert rat.denominator().equals(BigInteger.ONE) : value;

		return rat.numerator();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
