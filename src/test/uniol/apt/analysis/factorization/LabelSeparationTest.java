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
import static org.hamcrest.Matchers.equalTo;
import static uniol.apt.analysis.factorization.LabelSeparationResultMatcher.labelSeparationResultMatches;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.product.Product;
import uniol.apt.io.parser.ParserTestUtils;

/**
 * @author Jonas Prellberg
 */
public class LabelSeparationTest {

	@Test
	public void testLabelSeparation1() {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet()), equalTo(true));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("a")), equalTo(true));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("b")), equalTo(true));
		assertThat(LabelSeparation.isSeparated(ts, ts.getAlphabet()), equalTo(true));
	}

	@Test
	public void testLabelSeparation2() {
		TransitionSystem ts = getTestTS();
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet()), equalTo(true));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("a")), equalTo(true));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("b")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("c")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("b", "c")), equalTo(true));
		assertThat(LabelSeparation.isSeparated(ts, ts.getAlphabet()), equalTo(true));
	}

	@Test
	public void testLabelSeparation3() {
		TransitionSystem ts = ParserTestUtils.getAptLTS("nets/crashkurs-cc1-aut.apt");
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet()), equalTo(true));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("a")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("b")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("c")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("d")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("a", "b")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("a", "c")), equalTo(true));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("a", "d")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("b", "a")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("b", "c")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("b", "d")), equalTo(true));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("c", "d")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("a", "b", "c")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("a", "b", "d")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("b", "c", "d")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, ts.getAlphabet()), equalTo(true));
	}

	@Test
	public void testLabelSeparation4() {
		TransitionSystem ts = ParserTestUtils.getAptLTS("nets/crashkurs-cc2-aut.apt");
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("a", "c")), equalTo(true));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("b", "d")), equalTo(true));
	}

	@Test
	public void testLabelSeparation5() {
		TransitionSystem ts = ParserTestUtils.getAptLTS("nets/crashkurs-cc3-aut.apt");
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet()), equalTo(true));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("a")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("c")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("e")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("a", "c")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("a", "e")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("c", "e")), equalTo(false));
		assertThat(LabelSeparation.isSeparated(ts, ts.getAlphabet()), equalTo(true));
	}

	/**
	 * Async product implies separation: Let TS1 = (S1, R1, T1, u) and TS2 =
	 * (S2, R2, T2, v) be two lts with disjoint label sets, then TS1 x TS2
	 * is T1-separated and T2-separated.
	 */
	@Test
	public void testLabelSetSeparationWithProduct() {
		TransitionSystem ts = getProductTS();
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("a", "b")), equalTo(true));
		assertThat(LabelSeparation.isSeparated(ts, getLabelSet("c")), equalTo(true));
	}

	@Test
	public void testLabelSeparationWitnesses() {
		TransitionSystem ts = getTestTS();
		LabelSeparationResult r = LabelSeparation.checkSeparated(ts, getLabelSet("b"));
		LabelSeparationResult e = new LabelSeparationResult(ts.getNode("s2"), ts.getNode("s3"));
		assertThat(r, labelSeparationResultMatches(e));
	}

	private Set<String> getLabelSet(String... labels) {
		Set<String> set = new HashSet<>();
		for (String str : labels) {
			set.add(str);
		}
		return set;
	}

	/**
	 * Test TS:
	 *
	 * <pre>
	 * s0 ---(a)--> s1 <--(b)-- s2 ---(c)--> s3
	 *                             <--(b)---
	 * </pre>
	 */
	private TransitionSystem getTestTS() {
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

	/**
	 * Returns a TS that is the async product of two TS with disjoint label
	 * sets {"a", "b"} and {"c"}.
	 */
	private TransitionSystem getProductTS() {
		TransitionSystem ts1 = new TransitionSystem();
		State s0 = ts1.createState();
		State s1 = ts1.createState();
		State s2 = ts1.createState();
		ts1.setInitialState(s0);
		ts1.createArc(s0, s1, "a");
		ts1.createArc(s0, s2, "b");

		TransitionSystem ts2 = new TransitionSystem();
		State p0 = ts2.createState();
		State p1 = ts2.createState();
		ts2.setInitialState(p0);
		ts2.createArc(p0, p1, "c");

		Product product = new Product(ts1, ts2);
		return product.getAsyncProduct();
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
