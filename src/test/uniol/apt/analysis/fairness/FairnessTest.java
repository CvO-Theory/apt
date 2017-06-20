/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017       vsp
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

package uniol.apt.analysis.fairness;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.TransitionSystem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.adt.matcher.Matchers.*;
import static uniol.apt.util.matcher.Matchers.*;
import static uniol.apt.TestTSCollection.*;

/** @author vsp */
public class FairnessTest {
	@DataProvider(name = "goodLts")
	private Object[][] createGoodTestLts() {
		return new Object[][]{
				{getSingleStateTS()},
				{getSingleStateTSWithLoop()},
				{getThreeStatesTwoEdgesTS()},
				{getTwoStateCycleSameLabelTS()},
				{getSingleStateWithUnreachableTS()},
				{getNonDeterministicTS()},
				{getPersistentTS()},
				{getPersistentNonDeterministicTS()},
				{getNonPersistentNonDeterministicTS()},
				{getNonPersistentTSReversed()},
				{getNotTotallyReachableTS()},
				{getReversibleTS()},
				{getOneCycleLTS()},
				{getPathTS()},
				{getPureSynthesizablePathTS()},
				{getTwoBThreeATS()},
				{getABandA()},
				{getPlainTNetReachabilityTS()},
				{getACBCCLoopTS()},
				{getFairWithUnreachableUnfairStateTS()}
		};
	}

	private static Matcher<Iterable<? extends Arc>> sequenceWithLabels(String... labels) {
		List<Matcher<? super Arc>> arcs = new ArrayList<>();
		for (String l : labels) {
			arcs.add(arcWithLabel(l));
		}
		return contains(arcs);
	}

	@DataProvider(name = "badLts")
	private Object[][] createBadTestLts() {
		return new Object[][]{
				{getNonPersistentButActivatedTS(), 2, "b", "r", sequenceWithLabels("b"), sequenceWithLabels("fail", "a"), sequenceWithLabels("a", "fail")},
				{getImpureSynthesizablePathTS(), 0, "a", "u", sequenceWithLabels("a", "c"), sequenceWithLabels("b"), empty()}
		};
	}

	@Test(dataProvider = "goodLts")
	public void testGoodLts(TransitionSystem ts) {
		FairnessResult result = Fairness.checkFairness(ts);
		assertThat(result.isFair(), is(true));
	}

	// bad LTS where the result is unique
	@Test(dataProvider = "badLts")
	public void testBadLts(TransitionSystem ts, int k, String t, String s, Matcher<Iterable<? extends Arc>> start, Matcher<Iterable<? extends Arc>> cycle, Matcher<Iterable<? extends Arc>> activate) {
		FairnessResult result = Fairness.checkFairness(ts);
		assertThat(result.isFair(), is(false));
		assertThat(result.k, is(k));
		assertThat(result.unfairState, nodeWithID(s));
		assertThat(result.unfairEvent.getLabel(), is(t));
		assertThat(result.sequence, start);
		assertThat(result.cycle, cycle);
		assertThat(result.enabling, activate);
	}

	// bad LTS where multiple witnesses are possible

	@Test
	public void testDifferentCyclesTS() {
		TransitionSystem ts = getDifferentCyclesTS();
		FairnessResult result = Fairness.checkFairness(ts);
		assertThat(result.isFair(), is(false));
		assertThat(result.k, is(0));
		assertThat(result.unfairEvent.getLabel(), anyOf(is("a"), is("b"), is("c"), is("d")));
		Matcher<Iterable<? extends Arc>> cycle = null;
		switch (result.unfairEvent.getLabel()) {
			case "a":
			case "b":
				cycle = containsRotated(arcWithLabel("c"), arcWithLabel("d"));
				break;
			case "c":
			case "d":
				cycle = containsRotated(arcWithLabel("a"), arcWithLabel("b"));
				break;
			default:
				throw new AssertionError();
		}
		assertThat(result.sequence, hasSize(lessThanOrEqualTo(2)));
		assertThat(result.cycle, cycle);
		assertThat(result.enabling, empty());
	}

	@Test
	public void testDeterministicReachableReversibleNonPersistentTS() {
		TransitionSystem ts = getDeterministicReachableReversibleNonPersistentTS();
		FairnessResult result = Fairness.checkFairness(ts);
		assertThat(result.isFair(), is(false));
		assertThat(result.k, is(0));
		assertThat(result.unfairEvent.getLabel(), anyOf(is("a"), is("b")));
		assertThat(result.sequence, hasSize(lessThanOrEqualTo(1)));
		assertThat(result.cycle, anyOf(sequenceWithLabels("a", "a"), sequenceWithLabels("b", "b")));
		assertThat(result.enabling, empty());
	}

	@Test
	public void testcc1LTS() {
		TransitionSystem ts = getcc1LTS();
		FairnessResult result = Fairness.checkFairness(ts);
		assertThat(result.isFair(), is(false));
		assertThat(result.k, is(0));
		assertThat(result.unfairEvent.getLabel(), anyOf(is("a"), is("b"), is("c"), is("d")));
		Matcher<Iterable<? extends Arc>> cycle = null;
		switch (result.unfairEvent.getLabel()) {
			case "a":
			case "c":
				cycle = containsRotated(arcWithLabel("b"), arcWithLabel("d"));
				break;
			case "b":
			case "d":
				cycle = containsRotated(arcWithLabel("a"), arcWithLabel("c"));
				break;
			default:
				throw new AssertionError();
		}
		assertThat(result.sequence, hasSize(lessThanOrEqualTo(2)));
		assertThat(result.cycle, cycle);
		assertThat(result.enabling, empty());
	}

	@Test
	public void testgetNonDisjointCyclesTS() {
		TransitionSystem ts = getNonDisjointCyclesTS();
		FairnessResult result = Fairness.checkFairness(ts);
		assertThat(result.isFair(), is(false));
		assertThat(result.k, is(0));
		assertThat(result.unfairEvent.getLabel(), anyOf(is("a"), is("b"), is("c")));
		Matcher<Iterable<? extends Arc>> cycle = null;
		switch (result.unfairEvent.getLabel()) {
			case "a":
				cycle = anyOf(sequenceWithLabels("b", "c", "b", "c", "b", "c"),
						sequenceWithLabels("c", "b", "c", "b", "c", "b"));
				break;
			case "b":
			case "c":
				cycle = sequenceWithLabels("a", "a", "a");
				break;
			default:
				throw new AssertionError();
		}
		assertThat(result.sequence, hasSize(lessThanOrEqualTo(3)));
		assertThat(result.cycle, cycle);
		assertThat(result.enabling, empty());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
