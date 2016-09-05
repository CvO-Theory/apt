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

package uniol.apt.generator.marking;

import org.hamcrest.Matcher;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.TestNetCollection.*;
import static uniol.apt.adt.matcher.Matchers.*;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;

/** @author Uli Schlachter */
public class MarkingNetGeneratorTest {

	private void test(MarkingNetGenerator generator, PetriNet pn, Iterable<Matcher<? super Marking>> marks) {
		Collection<Matcher<? super PetriNet>> netMatchers = new LinkedList<>();

		for (Matcher<? super Marking> mark : marks) {
			netMatchers.add(both(netWithSameStructureAs(pn)).and(netWithInitialMarkingThat(mark)));
		}

		assertThat(generator, containsInAnyOrder(netMatchers));
	}

	private void testAddToNet(PetriNet pn, int m, Iterable<Matcher<? super Marking>> marks,
			Collection<Set<Place>> requiredPlaces) {
		test(new MarkingNetGenerator(pn, m, true, requiredPlaces), pn, marks);
	}

	private void testAddToNet(PetriNet pn, int m, Iterable<Matcher<? super Marking>> marks) {
		test(new MarkingNetGenerator(pn, m, true), pn, marks);
	}

	private void testNet(PetriNet pn, int m, Iterable<Matcher<? super Marking>> marks,
			Collection<Set<Place>> requiredPlaces) {
		test(new MarkingNetGenerator(pn, m, false, requiredPlaces), pn, marks);
	}

	private void testNet(PetriNet pn, int m, Iterable<Matcher<? super Marking>> marks) {
		test(new MarkingNetGenerator(pn, m), pn, marks);
	}

	@Test
	public void testEmptyNet() {
		PetriNet pn = getEmptyNet();
		Collection<Matcher<? super Marking>> markings = new LinkedList<>();
		// For a net without places, just the empty initial marking should be generated
		markings.add(markingThatIs(pn.getInitialMarking()));
		testNet(pn, 5, markings);
	}

	@Test
	public void testNoTransitionOnePlaceNet() {
		PetriNet pn = getNoTransitionOnePlaceNet();
		Map<String, Long> marking = new HashMap<>();
		Collection<Matcher<? super Marking>> markings = new LinkedList<>();

		marking.put("p1", 0l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 1l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 2l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 3l);
		markings.add(markingThatIs(marking));

		testNet(pn, 3, markings);
	}

	@Test
	public void testOneTransitionNoPlaceNet() {
		PetriNet pn = getOneTransitionNoPlaceNet();
		Collection<Matcher<? super Marking>> markings = new LinkedList<>();
		// For a net without places, just the empty initial marking should be generated
		markings.add(markingThatIs(pn.getInitialMarking()));
		testNet(pn, 5, markings);
	}

	@Test
	public void testNonPersistentNet() {
		PetriNet pn = getNonPersistentNet();
		Map<String, Long> marking = new HashMap<>();
		Collection<Matcher<? super Marking>> markings = new LinkedList<>();

		// As you see, the number of possible initial markings gets quite large quite fast
		marking.put("p1", 0l);
		marking.put("p2", 0l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 1l);
		marking.put("p2", 0l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 2l);
		marking.put("p2", 0l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 3l);
		marking.put("p2", 0l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 0l);
		marking.put("p2", 1l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 1l);
		marking.put("p2", 1l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 2l);
		marking.put("p2", 1l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 0l);
		marking.put("p2", 2l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 1l);
		marking.put("p2", 2l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 0l);
		marking.put("p2", 3l);
		markings.add(markingThatIs(marking));

		testNet(pn, 3, markings);
	}

	@Test
	public void testPersistentBiCFNet() {
		PetriNet pn = getPersistentBiCFNet();
		Map<String, Long> marking = new HashMap<>();
		Collection<Matcher<? super Marking>> markings = new LinkedList<>();

		// As you see, the number of possible initial markings gets quite large quite fast
		marking.put("p1", 0l);
		marking.put("p2", 0l);
		marking.put("p3", 0l);
		marking.put("p4", 0l);
		marking.put("p5", 0l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 1l);
		marking.put("p2", 0l);
		marking.put("p3", 0l);
		marking.put("p4", 0l);
		marking.put("p5", 0l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 0l);
		marking.put("p2", 1l);
		marking.put("p3", 0l);
		marking.put("p4", 0l);
		marking.put("p5", 0l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 0l);
		marking.put("p2", 0l);
		marking.put("p3", 1l);
		marking.put("p4", 0l);
		marking.put("p5", 0l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 0l);
		marking.put("p2", 0l);
		marking.put("p3", 0l);
		marking.put("p4", 1l);
		marking.put("p5", 0l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 0l);
		marking.put("p2", 0l);
		marking.put("p3", 0l);
		marking.put("p4", 0l);
		marking.put("p5", 1l);
		markings.add(markingThatIs(marking));

		testNet(pn, 1, markings);
	}

	@Test
	public void testPersistentBiCFNetConstraint() {
		PetriNet pn = getPersistentBiCFNet();
		Place p1 = pn.getPlace("p1");
		Place p2 = pn.getPlace("p2");
		Collection<Set<Place>> requiredPlaces = new LinkedList<>();
		Map<String, Long> marking = new HashMap<>();
		Collection<Matcher<? super Marking>> markings = new LinkedList<>();

		requiredPlaces.add(new HashSet<>(asList(p1, p2)));

		// The constraint makes this a lot easier
		marking.put("p1", 1l);
		marking.put("p2", 0l);
		marking.put("p3", 0l);
		marking.put("p4", 0l);
		marking.put("p5", 0l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 0l);
		marking.put("p2", 1l);
		marking.put("p3", 0l);
		marking.put("p4", 0l);
		marking.put("p5", 0l);
		markings.add(markingThatIs(marking));

		testNet(pn, 1, markings, requiredPlaces);
	}

	@Test
	public void testPersistentBiCFNetUnfulfilableConstraint() {
		PetriNet pn = getPersistentBiCFNet();
		Place p1 = pn.getPlace("p1");
		Place p2 = pn.getPlace("p2");
		Collection<Set<Place>> requiredPlaces = new LinkedList<>();
		Collection<Matcher<? super Marking>> markings = new LinkedList<>();

		requiredPlaces.add(new HashSet<>(asList(p1)));
		requiredPlaces.add(new HashSet<>(asList(p2)));

		// This is impossible and thus we expect the entry list
		testNet(pn, 1, markings, requiredPlaces);
	}

	@Test
	public void testDeadLockNetAddToken() {
		PetriNet pn = getDeadlockNet();
		Map<String, Long> marking = new HashMap<>();
		Collection<Matcher<? super Marking>> markings = new LinkedList<>();

		// As you see, the number of possible initial markings gets quite large quite fast
		marking.put("p1", 1l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 2l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 3l);
		markings.add(markingThatIs(marking));

		testAddToNet(pn, 2, markings);
	}

	@Test
	public void testDeadLockNetAddTokenConstraint() {
		PetriNet pn = getDeadlockNet();
		Place p1 = pn.getPlace("p1");
		Collection<Set<Place>> requiredPlaces = new LinkedList<>();
		Map<String, Long> marking = new HashMap<>();
		Collection<Matcher<? super Marking>> markings = new LinkedList<>();

		requiredPlaces.add(new HashSet<>(asList(p1)));

		// At least one token must be added, so p1=1 is forbidden (this would be the initial marking)

		marking.put("p1", 2l);
		markings.add(markingThatIs(marking));

		marking.put("p1", 3l);
		markings.add(markingThatIs(marking));

		testAddToNet(pn, 2, markings, requiredPlaces);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
