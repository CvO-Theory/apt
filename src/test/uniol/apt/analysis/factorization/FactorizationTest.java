/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
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

package uniol.apt.analysis.factorization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static uniol.apt.adt.matcher.Matchers.arcThatConnectsVia;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.generator.bitnet.SimpleBitNetGenerator;
import uniol.apt.io.parser.ParserTestUtils;

@SuppressWarnings("unchecked")
public class FactorizationTest {

	@Test
	public void testFactorize1() {
		TransitionSystem ts = ParserTestUtils.getAptLTS("nets/crashkurs-cc1-aut.apt");
		Factorization fact = new Factorization(ts);
		assertThat(fact.hasFactors(), equalTo(true));

		Set<Set<Arc>> factorArcs = new HashSet<>();
		factorArcs.add(fact.getFactor1().getEdges());
		factorArcs.add(fact.getFactor2().getEdges());
		assertThat(factorArcs, containsInAnyOrder(
			containsInAnyOrder(
					arcThatConnectsVia("s0", "s2", "b"),
					arcThatConnectsVia("s2", "s0", "d")
			),
			containsInAnyOrder(
					arcThatConnectsVia("s0", "s1", "a"),
					arcThatConnectsVia("s1", "s0", "c")
			)
		));
	}

	@Test
	public void testFactorize2() {
		TransitionSystem ts = new TransitionSystem();
		State s0 = ts.createState();
		State s1 = ts.createState();
		ts.setInitialState(s0);
		ts.createArc(s0, s1, "a");
		ts.createArc(s1, s0, "b");

		Factorization fact = new Factorization(ts);
		assertThat(fact.hasFactors(), equalTo(false));
	}

	@Test
	public void testFactorize3() {
		PetriNet pn = new SimpleBitNetGenerator().generateNet(2);
		TransitionSystem ts = CoverabilityGraph.get(pn).toCoverabilityLTS();

		Factorization fact = new Factorization(ts);
		assertThat(fact.hasFactors(), equalTo(true));

		Set<Set<Arc>> factorArcs = new HashSet<>();
		factorArcs.add(fact.getFactor1().getEdges());
		factorArcs.add(fact.getFactor2().getEdges());
		assertThat(factorArcs, containsInAnyOrder(
			containsInAnyOrder(
					arcThatConnectsVia("s0", "s1", "set0"),
					arcThatConnectsVia("s1", "s0", "unset0")
			),
			containsInAnyOrder(
					arcThatConnectsVia("s0", "s2", "set1"),
					arcThatConnectsVia("s2", "s0", "unset1")
			)
		));
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
