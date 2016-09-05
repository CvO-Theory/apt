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

package uniol.apt.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;

import org.hamcrest.Matcher;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;

import uniol.apt.CrashCourseNets;
import uniol.apt.TestNetCollection;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

import static org.hamcrest.Matchers.*;
import static uniol.apt.adt.matcher.Matchers.*;
import static uniol.apt.adt.pn.Token.OMEGA;

/** @author Uli Schlachter */
public class GenerateStepNetTest {
	@Test
	public void testEmptyStepLabel() {
		assertThat(GenerateStepNet.getStepLabel(Collections.<Transition>emptyList()), equalTo("{}"));
	}

	@Test
	public void testStepLabel() {
		PetriNet pn = new PetriNet();
		Collection<Transition> transitions = asList(pn.createTransitions("foo", "bar"));
		assertThat(GenerateStepNet.getStepLabel(transitions), equalTo("{foo,bar}"));
	}

	@Test
	public void testEmptyMarkings() {
		PetriNet pn = new PetriNet();
		Marking mark = new Marking(pn);
		assertThat(GenerateStepNet.isMarkingLessOrEqual(mark, mark), is(true));
	}

	@Test
	public void testEqualMarkings() {
		PetriNet pn = new PetriNet();
		pn.createPlaces(3);
		Marking mark1 = new Marking(pn, 0, 1, 2);
		Marking mark2 = new Marking(pn, 0, 1, 2);
		assertThat(GenerateStepNet.isMarkingLessOrEqual(mark1, mark2), is(true));
	}

	@Test
	public void testLessMarkings() {
		PetriNet pn = new PetriNet();
		pn.createPlaces(3);
		Marking mark1 = new Marking(pn, 0, 1, 2);
		Marking mark2 = new Marking(pn, 0, 2, 2);
		assertThat(GenerateStepNet.isMarkingLessOrEqual(mark1, mark2), is(true));
		assertThat(GenerateStepNet.isMarkingLessOrEqual(mark2, mark1), is(false));
	}

	@Test
	public void testGetMaximalReachableMarkingsDeadNet() {
		PetriNet pn = TestNetCollection.getDeadNet();
		assertThat(GenerateStepNet.getMaximalReachableMarkings(pn), contains(pn.getInitialMarking()));
	}

	@Test
	public void testGetMaximalReachableMarkingsABCNet() {
		PetriNet pn = TestNetCollection.getABCLanguageNet();
		int o = 42; /* Omega marking, for readability below */
		Marking[] markings = new Marking[3];
		//            new Marking(pn, 1, 0, 0, 1, 0); // Firing sequence empty, this is smaller than after ta1
		//            new Marking(pn, 0, 0, 1, 1, 0); // Firing sequence ta2, this is smaller than after ta1,ta2,tb1
		//            new Marking(pn, 0, 0, 0, 1, 1); // Firing sequence ta2,tb2, this is smaller than after ta1,ta2,tb1,tb2
		//            new Marking(pn, 0, 0, 0, 0, 1); // Firing sequence ta2,tb2tc, this is smaller than after ta1,ta2,tb1,tb2
		markings[0] = new Marking(pn, 1, o, 0, 1, 0); // Firing sequence ta1
		//            new Marking(pn, 0, o, 1, 1, 0); // Firing sequence ta1,ta2, this is smaller than after ta1,ta2,tb1
		//            new Marking(pn, 0, o, 0, 1, 1); // Firing sequence ta1,ta2,tb2, this is smaller than after ta1,ta2,tb1,tb2
		//            new Marking(pn, 0, o, 0, 0, 1); // Firing sequence ta1,ta2,tb2,tc, this is smaller than after ta1,ta2,tb1,tb2
		markings[1] = new Marking(pn, 0, o, 1, o, 0); // Firing sequence ta1,ta2,tb1
		markings[2] = new Marking(pn, 0, o, 0, o, 1); // Firing sequence ta1,ta2,tb1,tb2

		markings[0] = markings[0].setTokenCount("p2", OMEGA);
		markings[1] = markings[1].setTokenCount("p2", OMEGA);
		markings[1] = markings[1].setTokenCount("p4", OMEGA);
		markings[2] = markings[2].setTokenCount("p2", OMEGA);
		markings[2] = markings[2].setTokenCount("p4", OMEGA);

		assertThat(GenerateStepNet.getMaximalReachableMarkings(pn), containsInAnyOrder(markings));
	}

	@Test
	public void testStepReasonable() {
		PetriNet pn = TestNetCollection.getPersistentBiCFNet();
		GenerateStepNet gen = new GenerateStepNet(pn);
		Transition a = pn.getTransition("a");
		Transition b = pn.getTransition("b");
		Transition c = pn.getTransition("c");
		Transition d = pn.getTransition("d");

		// Since the implementation caches results, do this twice to also test the cache
		for (int i = 0; i < 2; i++) {
			assertThat(gen.isStepReasonable(Collections.<Transition>emptyList()), is(false));
			assertThat(gen.isStepReasonable(asList(a)), is(true));
			assertThat(gen.isStepReasonable(asList(b)), is(true));
			assertThat(gen.isStepReasonable(asList(c)), is(true));
			assertThat(gen.isStepReasonable(asList(d)), is(true));
			assertThat(gen.isStepReasonable(asList(a, b)), is(true));
			assertThat(gen.isStepReasonable(asList(a, d)), is(true));
			assertThat(gen.isStepReasonable(asList(b, c)), is(true));
			assertThat(gen.isStepReasonable(asList(c, b)), is(true));
			assertThat(gen.isStepReasonable(asList(a, c)), is(false));
			assertThat(gen.isStepReasonable(asList(b, d)), is(false));
			assertThat(gen.isStepReasonable(asList(a, b, c)), is(false));
			assertThat(gen.isStepReasonable(asList(a, b, d)), is(false));
			assertThat(gen.isStepReasonable(asList(a, c, d)), is(false));
			assertThat(gen.isStepReasonable(asList(b, c, d)), is(false));
			assertThat(gen.isStepReasonable(asList(a, b, c, d)), is(false));
		}
	}

	@Test
	public void testGenerateStepNet() {
		PetriNet pn = CrashCourseNets.getCCNet1();
		GenerateStepNet gen = new GenerateStepNet(pn);
		PetriNet stepPN = gen.getStepNet();

		Collection<Matcher<? super Place>> expectedPlaces = new ArrayList<>();
		for (Place place : pn.getPlaces())
			expectedPlaces.add(nodeWithID(place.getId()));
		assertThat(stepPN.getPlaces(), containsInAnyOrder(expectedPlaces));
		assertThat(stepPN.getInitialMarking(), markingThatIs(pn.getInitialMarking()));
		assertThat(stepPN.getTransitions(), hasSize(8));
		assertThat(stepPN.getEdges(), hasSize(4 * 2 + 4 * 4));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
