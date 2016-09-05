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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.TestNetCollection.*;
import static uniol.apt.adt.matcher.Matchers.*;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Marking;

/** @author Uli Schlachter */
public class MarkingIterableTest {
	@Test
	public void testEmptyNets() {
		PetriNet pn = getEmptyNet();
		Marking mark = pn.getInitialMarking();

		Collection<PetriNet> nets = new LinkedList<>();
		nets.add(pn);
		nets.add(pn);

		Collection<Matcher<? super PetriNet>> netMatchers = new LinkedList<>();
		netMatchers.add(both(netWithSameStructureAs(pn)).and(netWithInitialMarkingThat(markingThatIs(mark))));
		netMatchers.add(both(netWithSameStructureAs(pn)).and(netWithInitialMarkingThat(markingThatIs(mark))));

		assertThat(new MarkingIterable(nets, 1), containsInAnyOrder(netMatchers));
	}

	@Test
	public void testTwoNets() {
		PetriNet pn1 = getEmptyNet();
		Marking mark1 = pn1.getInitialMarking();

		PetriNet pn2 = getNoTransitionOnePlaceNet();

		Collection<PetriNet> nets = new LinkedList<>();
		nets.add(pn1);
		nets.add(pn2);

		Collection<Matcher<? super PetriNet>> netMatchers = new LinkedList<>();
		netMatchers.add(both(netWithSameStructureAs(pn1)).and(netWithInitialMarkingThat(markingThatIs(mark1))));

		Map<String, Long> marking = new HashMap<>();

		marking.put("p1", 0l);
		netMatchers.add(both(netWithSameStructureAs(pn2)).and(
					netWithInitialMarkingThat(markingThatIs(marking))));

		marking.put("p1", 1l);
		netMatchers.add(both(netWithSameStructureAs(pn2)).and(
					netWithInitialMarkingThat(markingThatIs(marking))));

		marking.put("p1", 2l);
		netMatchers.add(both(netWithSameStructureAs(pn2)).and(
					netWithInitialMarkingThat(markingThatIs(marking))));

		marking.put("p1", 3l);
		netMatchers.add(both(netWithSameStructureAs(pn2)).and(
					netWithInitialMarkingThat(markingThatIs(marking))));

		assertThat(new MarkingIterable(nets, 3), containsInAnyOrder(netMatchers));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
