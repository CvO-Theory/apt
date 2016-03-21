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
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static uniol.apt.analysis.factorization.GeneralDiamondResultMatcher.gdiamResultMatches;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.ParserTestUtils;

public class GeneralDiamondTest {

	@Test
	public void testIsGdiam1() {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		assertThat(GeneralDiamond.isGdiam(ts, "a", "b"), equalTo(true));
	}

	@Test
	public void testIsGdiam2() {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		ts.createState("center");
		ts.createArc("s0", "center", "a");
		assertThat(GeneralDiamond.isGdiam(ts, "a", "b"), equalTo(false));
	}

	@Test
	public void testIsGdiam3() {
		TransitionSystem ts = getTestTS1();
		assertThat(GeneralDiamond.isGdiam(ts, "a", "b"), equalTo(false));
	}

	@Test
	public void testIsGdiam4() {
		TransitionSystem ts = getTestTS2();
		assertThat(GeneralDiamond.isGdiam(ts, "a", "b"), equalTo(true));
	}

	@Test
	public void testIsGdiam5() {
		TransitionSystem ts = getTestTS2();
		GeneralDiamondResult r = GeneralDiamond.checkGdiam(ts, "a", "c");
		GeneralDiamondResult e1 = new GeneralDiamondResult(ts.getNode("s2"), "a", "c", true, true);
		GeneralDiamondResult e2 = new GeneralDiamondResult(ts.getNode("s3"), "a", "c", true, true);
		assertThat(r, anyOf(gdiamResultMatches(e1), gdiamResultMatches(e2)));
	}

	@Test
	public void testIsGdiam6() {
		TransitionSystem ts = getTestTS2();
		assertThat(GeneralDiamond.isGdiam(ts, "b", "c"), equalTo(true));
	}

	@Test
	public void testIsGdiam7() {
		TransitionSystem ts = getTestTS3();
		assertThat(GeneralDiamond.isGdiam(ts, "a", "b"), equalTo(false));
	}

	/**
	 * Test: Persistent TS with two arcs reversed still has gdiam-property
	 * for a, b.
	 */
	@Test
	public void testIsGdiamReverseArcs() {
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
		assertThat(GeneralDiamond.isGdiam(ts, "a", "b"), equalTo(true));
	}

	/**
	 * Test: Every TS (so also the test-TS) is a ∅-gdiam.
	 */
	@Test
	public void testIsGdiamSet1() {
		TransitionSystem ts = getTestTS1();
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet()), equalTo(true));
	}

	/**
	 * Test: The test-TS is not an a- or b-gdiam.
	 */
	@Test
	public void testIsGdiamSet2() {
		TransitionSystem ts = getTestTS1();
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet("a")), equalTo(false));
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet("b")), equalTo(false));
	}

	/**
	 * Test: Every TS (so also the test-TS) is a T-gdiam with T = labels of
	 * TS.
	 */
	@Test
	public void testIsGdiamSet3() {
		TransitionSystem ts = getTestTS1();
		assertThat(GeneralDiamond.isGdiam(ts, ts.getAlphabet()), equalTo(true));
	}

	/**
	 * Test: If the TS is a T'-gdiam, then it is also a T\T'-gdiam with T =
	 * labels of TS.
	 */
	@Test
	public void testIsGdiamSet4() {
		TransitionSystem ts = getTestTS2();
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet("b")), equalTo(true));
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet("a", "c")), equalTo(true));
	}

	@Test
	public void testIsGdiamSet5() {
		TransitionSystem ts = getTestTS2();
		GeneralDiamondResult r = GeneralDiamond.checkGdiam(ts, getLabelSet("a"));
		GeneralDiamondResult e1 = new GeneralDiamondResult(ts.getNode("s2"), "a", "c", true, true);
		GeneralDiamondResult e2 = new GeneralDiamondResult(ts.getNode("s3"), "a", "c", true, true);
		assertThat(r, anyOf(gdiamResultMatches(e1), gdiamResultMatches(e2)));
	}

	/**
	 * Test the crashkurs 1 LTS.
	 */
	@Test
	public void testIsGdiamSet6() {
		TransitionSystem ts = ParserTestUtils.getAptLTS("nets/crashkurs-cc1-aut.apt");
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet("a")), equalTo(false));
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet("b")), equalTo(false));
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet("c")), equalTo(false));
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet("d")), equalTo(false));
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet("a", "b")), equalTo(false));
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet("a", "c")), equalTo(true));
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet("a", "d")), equalTo(false));
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet("b", "c")), equalTo(false));
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet("b", "d")), equalTo(true));
		assertThat(GeneralDiamond.isGdiam(ts, getLabelSet("c", "d")), equalTo(false));
	}

	/**
	 * Returns a set of the given strings.
	 */
	private Set<String> getLabelSet(String... labels) {
		Set<String> result = new HashSet<>();
		for (String label : labels) {
			result.add(label);
		}
		return result;
	}

	/**
	 * Test LTS 1:
	 *
	 * <pre>
	 * s0 ---(a)--> s1
	 *  ^           |
	 *  |          (b)
	 * (b)          |
	 *  |           v
	 * s2 <--(a)--- s3
	 *  |           ^
	 * (b)          |
	 *  |          (b)
	 *  v           |
	 * s4 ---(b)--> s5
	 * </pre>
	 */
	private TransitionSystem getTestTS1() {
		TransitionSystem ts = new TransitionSystem();
		ts.createState("s0");
		ts.createState("s1");
		ts.createState("s2");
		ts.createState("s3");
		ts.createState("s4");
		ts.createState("s5");
		ts.setInitialState("s0");
		ts.createArc("s0", "s1", "a");
		ts.createArc("s1", "s3", "b");
		ts.createArc("s2", "s0", "b");
		ts.createArc("s2", "s4", "b");
		ts.createArc("s3", "s2", "a");
		ts.createArc("s4", "s5", "b");
		ts.createArc("s5", "s3", "b");
		return ts;
	}

	/**
	 * Test LTS 2:
	 *
	 * <pre>
	 * s0 ---(b)--> s1
	 *  ^           ^
	 *  |           |
	 * (a)         (a)
	 *  |           |
	 * s2 ---(b)--> s3
	 *  |           |
	 * (c)         (c)
	 *  |           |
	 *  v           v
	 * s4 ---(b)--> s5
	 * </pre>
	 */
	private TransitionSystem getTestTS2() {
		TransitionSystem ts = new TransitionSystem();
		ts.createState("s0");
		ts.createState("s1");
		ts.createState("s2");
		ts.createState("s3");
		ts.createState("s4");
		ts.createState("s5");
		ts.setInitialState("s0");
		ts.createArc("s0", "s1", "b");
		ts.createArc("s2", "s0", "a");
		ts.createArc("s2", "s3", "b");
		ts.createArc("s2", "s4", "c");
		ts.createArc("s3", "s1", "a");
		ts.createArc("s3", "s5", "c");
		ts.createArc("s4", "s5", "b");
		return ts;
	}

	/**
	 * Test LTS 3:
	 *
	 * <pre>
	 * s0 ---(a)--> s1
	 *    <--(b)--
	 * </pre>
	 */
	private TransitionSystem getTestTS3() {
		TransitionSystem ts = new TransitionSystem();
		ts.createState("s0");
		ts.createState("s1");
		ts.setInitialState("s0");
		ts.createArc("s0", "s1", "a");
		ts.createArc("s1", "s0", "b");
		return ts;
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
