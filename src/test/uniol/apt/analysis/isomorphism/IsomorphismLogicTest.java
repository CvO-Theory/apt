
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

package uniol.apt.analysis.isomorphism;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static uniol.apt.BestNetCollection.getTs1A;
import static uniol.apt.BestNetCollection.getTs1B;
import static uniol.apt.BestNetCollection.getTs2A;
import static uniol.apt.BestNetCollection.getTs2B;
import static uniol.apt.BestNetCollection.getTs3A;
import static uniol.apt.BestNetCollection.getTs3B;
import static uniol.apt.BestNetCollection.getTs4A;
import static uniol.apt.BestNetCollection.getTs4B;
import static uniol.apt.TestNetsForIsomorphism.getIsoTs1A;
import static uniol.apt.TestNetsForIsomorphism.getIsoTs1B;
import static uniol.apt.TestNetsForIsomorphism.getIsoTs2A;
import static uniol.apt.TestNetsForIsomorphism.getIsoTs2B;
import static uniol.apt.TestNetsForIsomorphism.getIsoTs3A;
import static uniol.apt.TestNetsForIsomorphism.getIsoTs3B;
import static uniol.apt.TestNetsForIsomorphism.getIsoTs4A;
import static uniol.apt.TestNetsForIsomorphism.getIsoTs4B;
import static uniol.apt.TestTSCollection.getSingleStateTS;
import static uniol.apt.TestTSCollection.getSingleStateTSWithLoop;
import static uniol.apt.adt.matcher.Matchers.nodeWithID;

import org.apache.commons.collections4.BidiMap;
import org.testng.annotations.Test;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * Collection of nets to test the isomorphism-module
 *
 * notation:
 * weak isomorphism: isomorphism, that ignores labels
 * strong isomorphism: isomorphism, that doesn't ignore labels
 *
 * @author Uli Schlachter
 */
public class IsomorphismLogicTest extends AbstractIsomorphismTest {

	// Test strong isomorphism (which includes weak isomorphism)
	@Override
	protected BidiMap<State, State> testIsomorphism(TransitionSystem lts1, TransitionSystem lts2) {
		IsomorphismLogic logic1 = new IsomorphismLogic(lts1, lts2, false);
		assertTrue(logic1.isIsomorphic());
		IsomorphismLogic logic2 = new IsomorphismLogic(lts1, lts2, true);
		assertTrue(logic2.isIsomorphic());
		assertTrue(logic1.getIsomorphism().equals(logic2.getIsomorphism()));
		return logic1.getIsomorphism();
	}

	// Test weak isomorphism, but non-strong isomorphism.
	@Override
	protected BidiMap<State, State> testWeakIsomorphism(TransitionSystem lts1, TransitionSystem lts2) {
		IsomorphismLogic logic1 = new IsomorphismLogic(lts1, lts2, false);
		assertTrue(logic1.isIsomorphic());
		IsomorphismLogic logic2 = new IsomorphismLogic(lts1, lts2, true);
		assertFalse(logic2.isIsomorphic());
		assertTrue(logic2.getIsomorphism().isEmpty());
		return logic1.getIsomorphism();
	}

	// Test not non-weak isomorphism (which includes non-strong isomorphism)
	@Override
	protected void testNonWeakIsomorphism(TransitionSystem lts1, TransitionSystem lts2) {
		IsomorphismLogic logic = new IsomorphismLogic(lts1, lts2, false);
		assertFalse(logic.isIsomorphic());
		assertTrue(logic.getIsomorphism().isEmpty());
		logic = new IsomorphismLogic(lts1, lts2, true);
		assertFalse(logic.isIsomorphic());
		assertTrue(logic.getIsomorphism().isEmpty());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
