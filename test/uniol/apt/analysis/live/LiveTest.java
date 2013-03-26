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

package uniol.apt.analysis.live;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

import static uniol.apt.adt.matcher.Matchers.*;

import static uniol.apt.TestNetCollection.*;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.generator.philnet.TristatePhilNetGenerator;

import static java.util.Collections.emptyList;

/** @author Uli Schlachter, vsp */
@Test
public class LiveTest {

	private void testLiveness(PetriNet pn, Transition transition, boolean simply,
		boolean weakly, boolean strongly) throws Exception {
		assertEquals(Live.checkSimplyLive(pn, transition), simply);
		assertEquals(Live.checkWeaklyLive(pn, transition), weakly);
		assertEquals(Live.checkStronglyLive(pn, transition), strongly);
	}

	private void testLiveNet(PetriNet pn, boolean result) throws Exception {
		testLiveNet(pn, result, result, result);
	}

	private void testLiveNet(PetriNet pn, boolean simply, boolean weakly, boolean strongly) throws Exception {
		for (Transition t : pn.getTransitions()) {
			testLiveness(pn, t, simply, weakly, strongly);
		}
	}

	@Test
	public void testOneTransitionNoPlaceNet() throws Exception {
		testLiveNet(getOneTransitionNoPlaceNet(), true);
	}

	@Test(expectedExceptions = UnboundedException.class)
	public void testTokenGeneratorNet() throws Exception {
		testLiveNet(getTokenGeneratorNet(), true);
	}

	@Test
	public void testDeadlockNet() throws Exception {
		PetriNet pn = getDeadlockNet();
		Transition trans1 = pn.getTransition("t1");
		Transition trans2 = pn.getTransition("t2");
		testLiveNet(pn, true, false, false);
		assertThat(Live.findKillingFireSequence(pn, trans1), anyOf(contains(is(trans1)), contains(is(trans2))));
		assertThat(Live.findKillingFireSequence(pn, trans2), anyOf(contains(is(trans1)), contains(is(trans2))));
	}

	@Test
	public void testNonPersistentNet() throws Exception {
		testLiveNet(getNonPersistentNet(), true);
	}

	@Test
	public void testPhilDeadlock() throws Exception {
		testLiveNet(new TristatePhilNetGenerator().generateNet(3), true, true, false);
	}

	@Test
	public void testDeadTransitionNet() throws Exception {
		PetriNet pn = getDeadTransitionNet();
		testLiveness(pn, pn.getTransition("td"), false, false, false);
		testLiveness(pn, pn.getTransition("tl"), true, true, true);
		assertEquals(Live.findKillingFireSequence(pn, pn.getTransition("td")), emptyList());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
