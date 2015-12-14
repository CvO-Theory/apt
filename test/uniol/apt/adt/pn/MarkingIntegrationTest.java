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

package uniol.apt.adt.pn;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import static uniol.apt.TestNetCollection.*;

/**
 *
 * @author vsp
 */
public class MarkingIntegrationTest {

	@Test
	public void testSafeNet() {
		PetriNet pn = new PetriNet("foo");
		Place p = pn.createPlace("place");
		Transition t = pn.createTransition("transition");
		pn.createFlow(p, t);
		pn.createFlow(t, p);
		p.setInitialToken(1);

		assertEquals(pn.getInitialMarking().getToken(p), Token.valueOf(1));
	}

	@Test
	public void testDeadlock() {
		PetriNet pn = new PetriNet("foo");
		Place p = pn.createPlace("place");
		Transition t1 = pn.createTransition("transition1");
		Transition t2 = pn.createTransition("transition2");
		pn.createFlow(p, t1);
		pn.createFlow(p, t2);
		p.setInitialToken(1);

		assertEquals(pn.getInitialMarking().getToken(p), Token.valueOf(1));
	}

	@Test
	public void testDefaultMarking() {
		PetriNet pn = new PetriNet("foo");
		Place p = pn.createPlace("place");

		assertEquals(pn.getInitialMarking().getToken(p), Token.valueOf(0));
	}

	private long getTokenCountConcurrentDiamondNet(Marking m) {
		return m.getToken("p1").getValue() + m.getToken("p2").getValue();
	}

	@Test
	public void testFiringConcurrentDiamondNet() {
		PetriNet pn = getConcurrentDiamondNet();
		assertEquals(getTokenCountConcurrentDiamondNet(pn.getInitialMarking()), 2);
		Transition t[] = pn.getTransitions().toArray(new Transition[0]);

		Marking markings[] = new Marking[4];

		markings[0] = pn.getInitialMarking();

		markings[0] = markings[0].fireTransitions(t[0]);

		assertEquals(getTokenCountConcurrentDiamondNet(markings[0]), 1);

		markings[1] = markings[0].fireTransitions(t[1]);

		assertEquals(getTokenCountConcurrentDiamondNet(markings[0]), 1);
		assertEquals(getTokenCountConcurrentDiamondNet(markings[1]), 0);

		markings[2] = new Marking(markings[1]);

		assertEquals(getTokenCountConcurrentDiamondNet(markings[0]), 1);
		assertEquals(getTokenCountConcurrentDiamondNet(markings[1]), 0);
		assertEquals(getTokenCountConcurrentDiamondNet(markings[2]), 0);

		markings[3] = t[1].fire(pn.getInitialMarking());

		assertEquals(getTokenCountConcurrentDiamondNet(markings[3]), 1);

		markings[3] = markings[3].fireTransitions(t[0]);

		assertEquals(getTokenCountConcurrentDiamondNet(markings[3]), 0);
		assertEquals(markings[3], markings[2]);
		assertEquals(getTokenCountConcurrentDiamondNet(markings[0]), 1);
		assertEquals(getTokenCountConcurrentDiamondNet(markings[1]), 0);
		assertEquals(getTokenCountConcurrentDiamondNet(markings[2]), 0);
		assertEquals(getTokenCountConcurrentDiamondNet(markings[3]), 0);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
