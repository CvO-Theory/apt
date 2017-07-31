/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
 * Copyright (C) 2017 Uli Schlachter
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

import org.testng.annotations.Test;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.io.parser.ParserTestUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.adt.matcher.Matchers.*;

/**
 * @author Uli Schlachter
 */
@SuppressWarnings("unchecked") // Sigh, generics
public class SynthesisFactorisationTest {
	@Test
	public void testPersistentTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		SynthesisFactorisation fact = new SynthesisFactorisation();
		assertThat(fact.factorize(ts), containsInAnyOrder(
					both(tsWithInitialState("s0")).and(tsWith(contains(arcThatConnectsVia("s0", "l", "a")))),
					both(tsWithInitialState("s0")).and(tsWith(contains(arcThatConnectsVia("s0", "r", "b"))))));
	}

	/**
	 * Test TS:
	 *
	 * <pre>
	 * s0 ---(a)--> s1 <--(b)-- s2 ---(c)--> s3
	 *                             <--(b)---
	 * </pre>
	 */
	private TransitionSystem getTestTS1() {
		TransitionSystem ts = new TransitionSystem();
		ts.createState("s0");
		ts.createState("s1");
		ts.createState("s2");
		ts.createState("s3");
		ts.setInitialState("s0");
		ts.createArc("s0", "s1", "a");
		ts.createArc("s2", "s1", "b");
		ts.createArc("s2", "s3", "c");
		ts.createArc("s3", "s2", "b");
		return ts;
	}

	@Test
	public void testTestTS1() throws Exception {
		TransitionSystem ts = getTestTS1();
		SynthesisFactorisation fact = new SynthesisFactorisation();
		assertThat(fact.factorize(ts), contains(sameInstance(ts)));
	}

	@Test
	public void testCrashcurs1LTS() throws Exception {
		TransitionSystem ts = ParserTestUtils.getAptLTS("nets/crashkurs-cc1-aut.apt");
		SynthesisFactorisation fact = new SynthesisFactorisation();
		assertThat(fact.factorize(ts), containsInAnyOrder(
					both(tsWithInitialState("s0")).and(tsWith(containsInAnyOrder(
								arcThatConnectsVia("s0", "s1", "a"),
								arcThatConnectsVia("s1", "s0", "c")))),
					both(tsWithInitialState("s0")).and(tsWith(containsInAnyOrder(
								arcThatConnectsVia("s0", "s2", "b"),
								arcThatConnectsVia("s2", "s0", "d"))))));
	}

	@Test
	public void testCrashcurs2LTS() throws Exception {
		TransitionSystem ts = ParserTestUtils.getAptLTS("nets/crashkurs-cc2-aut.apt");
		SynthesisFactorisation fact = new SynthesisFactorisation();
		assertThat(fact.factorize(ts), contains(sameInstance(ts)));
	}

	@Test
	public void testCrashcurs3LTS() throws Exception {
		TransitionSystem ts = ParserTestUtils.getAptLTS("nets/crashkurs-cc3-aut.apt");
		SynthesisFactorisation fact = new SynthesisFactorisation();
		assertThat(fact.factorize(ts), contains(sameInstance(ts)));
	}

	@Test
	public void testCircle() throws Exception {
		TransitionSystem ts = new TransitionSystem();
		ts.createState("s0");
		ts.createState("s1");
		ts.setInitialState("s0");
		ts.createArc("s0", "s1", "a");
		ts.createArc("s1", "s0", "b");

		SynthesisFactorisation fact = new SynthesisFactorisation();
		assertThat(fact.factorize(ts), contains(sameInstance(ts)));
	}

	@Test
	public void testLoop() throws Exception {
		TransitionSystem ts = new TransitionSystem();
		ts.createState("s0");
		ts.setInitialState("s0");
		ts.createArc("s0", "s0", "a");
		ts.createArc("s0", "s0", "b");
		ts.createArc("s0", "s0", "c");

		SynthesisFactorisation fact = new SynthesisFactorisation();
		assertThat(fact.factorize(ts), containsInAnyOrder(
					both(tsWithInitialState("s0")).and(tsWith(contains(arcThatConnectsVia("s0", "s0", "a")))),
					both(tsWithInitialState("s0")).and(tsWith(contains(arcThatConnectsVia("s0", "s0", "b")))),
					both(tsWithInitialState("s0")).and(tsWith(contains(arcThatConnectsVia("s0", "s0", "c"))))));
	}

	@Test(expectedExceptions = PreconditionFailedException.class)
	public void testNonDeterministicTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getNonDeterministicTS();
		new SynthesisFactorisation().factorize(ts);
	}

	@Test(expectedExceptions = PreconditionFailedException.class)
	public void testNonBackwardsDeterministicTS() throws Exception {
		TransitionSystem ts = TestTSCollection.getNonBackwardsDeterministicTS();
		new SynthesisFactorisation().factorize(ts);
	}

	/**
	 * Test: Persistent TS with two arcs reversed still has gdiam-property
	 * for a, b.
	 */
	@Test
	public void testIsGdiamReverseArcs() throws Exception {
		TransitionSystem ts = new TransitionSystem();
		ts.createState("s0");
		ts.createState("l");
		ts.createState("r");
		ts.createState("s1");
		ts.setInitialState("s0");
		ts.createArc("s0", "l", "a");
		ts.createArc("r", "s0", "b");
		ts.createArc("s1", "l", "b");
		ts.createArc("r", "s1", "a");

		SynthesisFactorisation fact = new SynthesisFactorisation();
		assertThat(fact.factorize(ts), containsInAnyOrder(
					both(tsWithInitialState("s0")).and(tsWith(contains(arcThatConnectsVia("s0", "l", "a")))),
					both(tsWithInitialState("s0")).and(tsWith(contains(arcThatConnectsVia("r", "s0", "b"))))));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
