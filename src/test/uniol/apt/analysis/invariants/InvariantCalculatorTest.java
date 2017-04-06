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

package uniol.apt.analysis.invariants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.annotations.Test;
import uniol.apt.TestNetCollection;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.impl.PnmlPNParser;

import static uniol.apt.io.parser.ParserTestUtils.getAptPN;

/**
 * @author Manuel Gieseking
 */
public class InvariantCalculatorTest {

	private void testSInvariant(PetriNet pn, Set<List<Integer>> expected) {
		Set<List<Integer>> calculated = InvariantCalculator.calcSInvariants(pn);
		for (List<Integer> inv : expected) {
			assertTrue(calculated.contains(inv), inv.toString() + " " + calculated.toString());
		}
		calculated = InvariantCalculator.calcSInvariants(pn, InvariantCalculator.InvariantAlgorithm.FARKAS);
		for (List<Integer> inv : expected) {
			assertTrue(calculated.contains(inv), inv.toString() + " " + calculated.toString());
		}
	}

	private void testTInvariant(PetriNet pn, Set<List<Integer>> expected) {
		Set<List<Integer>> calculated = InvariantCalculator.calcTInvariants(pn);
		for (List<Integer> inv : expected) {
			assertTrue(calculated.contains(inv), inv.toString() + " " + calculated.toString());
		}
		calculated = InvariantCalculator.calcTInvariants(pn, InvariantCalculator.InvariantAlgorithm.FARKAS);
		for (List<Integer> inv : expected) {
			assertTrue(calculated.contains(inv), inv.toString() + " " + calculated.toString());
		}
	}

	private void coveredBySInvariant(PetriNet pn, boolean covered) {
		assertEquals(InvariantCalculator.coveredBySInvariants(pn,
			InvariantCalculator.InvariantAlgorithm.FARKAS) != null, covered);
		assertEquals(InvariantCalculator.coveredBySInvariants(pn,
			InvariantCalculator.InvariantAlgorithm.PIPE) != null, covered);
	}

	private void coveredByTInvariant(PetriNet pn, boolean covered) {
		assertEquals(InvariantCalculator.coveredByTInvariants(pn,
			InvariantCalculator.InvariantAlgorithm.FARKAS) != null, covered);
		assertEquals(InvariantCalculator.coveredByTInvariants(pn,
			InvariantCalculator.InvariantAlgorithm.PIPE) != null, covered);
	}

	// TESTNETCOLLECTION ....
	@Test
	public void testEmptyNet() {
		PetriNet net = TestNetCollection.getEmptyNet();
		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());
		coveredBySInvariant(net, false);
		coveredByTInvariant(net, false);
	}

	@Test
	public void testABCLanguageNet() {
		PetriNet net = TestNetCollection.getABCLanguageNet();
		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{1, 0, 1, 0, 1}));
		this.testTInvariant(net, expected);
		this.testSInvariant(net, expected);
		coveredBySInvariant(net, false);
		coveredByTInvariant(net, false);
	}

	@Test
	public void testConcurrentDiamondNet() {
		PetriNet net = TestNetCollection.getConcurrentDiamondNet();
		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());
		coveredBySInvariant(net, false);
		coveredByTInvariant(net, false);
	}

	@Test
	public void testConflictingDiamondNet() {
		PetriNet net = TestNetCollection.getConflictingDiamondNet();
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());
		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{0, 0, 1}));
		this.testSInvariant(net, expected);
		coveredBySInvariant(net, false);
		coveredByTInvariant(net, false);
	}

	@Test
	public void testDeadTransitionNet() {
		PetriNet net = TestNetCollection.getDeadTransitionNet();
		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());
		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{0, 1}));
		this.testTInvariant(net, expected);
		coveredBySInvariant(net, false);
		coveredByTInvariant(net, false);
	}

	@Test
	public void testDeadlockNet() {
		PetriNet net = TestNetCollection.getDeadlockNet();
		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());
		coveredBySInvariant(net, false);
		coveredByTInvariant(net, false);
	}

	@Test
	public void testMultiArcNet() {
		PetriNet net = TestNetCollection.getMultiArcNet();
		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());
		coveredBySInvariant(net, false);
		coveredByTInvariant(net, false);
	}

	@Test
	public void testNoTransitionOnePlaceNet() {
		PetriNet net = TestNetCollection.getNoTransitionOnePlaceNet();
		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());
		coveredBySInvariant(net, false);
		coveredByTInvariant(net, false);
	}

	@Test
	public void testNonPersistentNet() {
		PetriNet net = TestNetCollection.getNonPersistentNet();

		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{1, 0, 1}));
		expected.add(Arrays.asList(new Integer[]{0, 1, 1}));
		this.testTInvariant(net, expected);

		expected.clear();
		expected.add(Arrays.asList(new Integer[]{1, 1}));
		this.testSInvariant(net, expected);

		coveredBySInvariant(net, true);
		coveredByTInvariant(net, true);
	}

	@Test
	public void testOneTransitionNoPlaceNet() {
		PetriNet net = TestNetCollection.getOneTransitionNoPlaceNet();

		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());

		coveredBySInvariant(net, false);
		coveredByTInvariant(net, false);
	}

	@Test
	public void testPersistentBiCFNet() {
		PetriNet net = TestNetCollection.getPersistentBiCFNet();

		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{0, 1, 0, 1}));
		expected.add(Arrays.asList(new Integer[]{1, 0, 1, 0}));
		this.testTInvariant(net, expected);

		expected.clear();
		expected.add(Arrays.asList(new Integer[]{1, 1, 0, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{0, 1, 1, 1, 0}));
		expected.add(Arrays.asList(new Integer[]{0, 0, 0, 1, 1}));
		this.testSInvariant(net, expected);

		coveredBySInvariant(net, true);
		coveredByTInvariant(net, true);
	}

	@Test
	public void testTokenGeneratorNet() {
		PetriNet net = TestNetCollection.getTokenGeneratorNet();

		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcSInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.PIPE)
			.isEmpty());
		assertTrue(InvariantCalculator.calcTInvariants(net, InvariantCalculator.InvariantAlgorithm.FARKAS)
			.isEmpty());

		coveredBySInvariant(net, false);
		coveredByTInvariant(net, false);
	}

	// NORMAL TESTS
	@Test
	public void testReaderWriter() {
		PetriNet pn = getAptPN("nets/readerWriter-net.apt");

		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{1, 1, 0, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{0, 0, 0, 1, 1}));
		expected.add(Arrays.asList(new Integer[]{0, 1, 1, 0, 3}));
		this.testSInvariant(pn, expected);

		expected.clear();
		expected.add(Arrays.asList(new Integer[]{1, 1, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{0, 0, 1, 1}));
		this.testTInvariant(pn, expected);

		this.coveredBySInvariant(pn, true);
		this.coveredByTInvariant(pn, true);
	}

	@Test
	public void testAabbccNet() {
		PetriNet pn = getAptPN("nets/eb-nets/more/aabbcc-net.apt");

		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{1, 1, 1}));
		this.testSInvariant(pn, expected);
		this.testTInvariant(pn, expected);

		this.coveredBySInvariant(pn, true);
		this.coveredByTInvariant(pn, true);
	}

	@Test
	public void testTrapsSiphons1Net() {
		PetriNet pn = getAptPN("nets/eb-nets/trap-siphon-linalg/traps-siphons-1-net.apt");

		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{0, 1, 0, 1, 1, 0, 1}));
		this.testTInvariant(pn, expected);

		expected.clear();
		expected.add(Arrays.asList(new Integer[]{0, 1, 1, 1, 0, 0, 1, 1}));
		expected.add(Arrays.asList(new Integer[]{1, 1, 1, 0, 0, 1, 1, 1}));
		this.testSInvariant(pn, expected);

		this.coveredBySInvariant(pn, false);
		this.coveredByTInvariant(pn, false);
	}

	@Test
	public void testTrapsSiphons2Net() {
		PetriNet pn = getAptPN("nets/eb-nets/trap-siphon-linalg/traps-siphons-2-net.apt");

		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{0, 1, 1, 0, 1, 0, 0, 1, 1}));
		expected.add(Arrays.asList(new Integer[]{1, 0, 0, 1, 1, 0, 0, 1, 1}));
		expected.add(Arrays.asList(new Integer[]{0, 1, 0, 0, 0, 1, 0, 1, 1}));
		expected.add(Arrays.asList(new Integer[]{1, 0, 0, 0, 1, 0, 1, 0, 1}));
		this.testTInvariant(pn, expected);

		expected.clear();
		expected.add(Arrays.asList(new Integer[]{2, 1, 1, 1, 1, 1}));
		this.testSInvariant(pn, expected);

		this.coveredByTInvariant(pn, true);
		this.coveredBySInvariant(pn, true);
	}

	@Test
	public void testTrapsSiphons3Net() {
		PetriNet pn = getAptPN("nets/eb-nets/trap-siphon-linalg/traps-siphons-3-net.apt");

		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{0, 0, 0, 1, 1, 0, 0, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{1, 1, 1, 0, 0, 1, 1, 1, 1}));
		this.testTInvariant(pn, expected);

		expected.clear();
		expected.add(Arrays.asList(new Integer[]{1, 1, 0, 0, 0, 0, 0, 0, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{1, 0, 1, 2, 1, 1, 1, 0, 1, 1}));
		expected.add(Arrays.asList(new Integer[]{0, 1, 1, 0, 1, 1, 1, 2, 1, 1}));
		expected.add(Arrays.asList(new Integer[]{0, 0, 1, 1, 1, 1, 1, 1, 1, 1}));
		this.testSInvariant(pn, expected);

		this.coveredByTInvariant(pn, true);
		this.coveredBySInvariant(pn, true);
	}

	@Test
	public void testPersFig1Net() {
		PetriNet pn = getAptPN("nets/eb-nets/pers/pers-fig1-net.apt");

		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{1, 0, 1, 0}));
		expected.add(Arrays.asList(new Integer[]{0, 1, 0, 1}));
		this.testTInvariant(pn, expected);

		expected.clear();
		expected.add(Arrays.asList(new Integer[]{1, 1, 0, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{0, 1, 1, 1, 0}));
		expected.add(Arrays.asList(new Integer[]{0, 0, 0, 1, 1}));
		this.testSInvariant(pn, expected);

		this.coveredByTInvariant(pn, true);
		this.coveredBySInvariant(pn, true);
	}

	@Test
	public void testPersFig2Net() {
		PetriNet pn = getAptPN("nets/eb-nets/pers/pers-fig2-net.apt");

		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{2, 1, 1}));
		this.testTInvariant(pn, expected);

		expected.clear();
		expected.add(Arrays.asList(new Integer[]{1, 1, 0, 1}));
		expected.add(Arrays.asList(new Integer[]{0, 1, 1, 2}));
		this.testSInvariant(pn, expected);

		this.coveredByTInvariant(pn, true);
		this.coveredBySInvariant(pn, true);
	}

	@Test
	public void testPersFig4Net() {
		PetriNet pn = getAptPN("nets/eb-nets/pers/pers-fig4-net.apt");

		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{1, 2}));
		this.testTInvariant(pn, expected);

		expected.clear();
		expected.add(Arrays.asList(new Integer[]{1, 1}));
		this.testSInvariant(pn, expected);

		this.coveredByTInvariant(pn, true);
		this.coveredBySInvariant(pn, true);
	}

	@Test
	public void testPersFig5Net() {
		PetriNet pn = getAptPN("nets/eb-nets/pers/pers-fig5-net.apt");

		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{1, 1, 1, 1, 1, 1, 1, 1}));
		this.testTInvariant(pn, expected);

		expected.clear();
		expected.add(Arrays.asList(new Integer[]{1, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1}));
		expected.add(Arrays.asList(new Integer[]{0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 1}));
		expected.add(Arrays.asList(new Integer[]{0, 1, 0, 0, 1, 1, 0, 1, 0, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{0, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{1, 2, 0, 0, 1, 2, 2, 0, 0, 1, 2}));
		this.testSInvariant(pn, expected);

		this.coveredByTInvariant(pn, true);
		this.coveredBySInvariant(pn, true);
	}

	@Test
	public void testbdNet() {
		PetriNet pn = getAptPN("nets/eb-nets/more/bd-net.apt");

		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{1, 1, 3, 1, 1, 1, 2, 2, 2, 1, 1}));
		Set<List<Integer>> calculated = InvariantCalculator.calcTInvariants(pn,
			InvariantCalculator.InvariantAlgorithm.PIPE);
		for (List<Integer> inv : expected) {
			assertTrue(calculated.contains(inv), inv.toString() + " " + calculated.toString());
		}

		expected.clear();
		expected.add(Arrays.asList(new Integer[]{0, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 1}));
		expected.add(Arrays.asList(new Integer[]{0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{1, 2, 1, 1, 1, 0, 2, 0, 0, 0, 1, 1, 0, 0, 1}));
		expected.add(Arrays.asList(new Integer[]{0, 1, 1, 0, 1, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{0, 1, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 1}));
		calculated = InvariantCalculator.calcSInvariants(pn,
			InvariantCalculator.InvariantAlgorithm.PIPE);
		for (List<Integer> inv : expected) {
			assertTrue(calculated.contains(inv), inv.toString() + " " + calculated.toString());
		}


		assertEquals(InvariantCalculator.coveredByTInvariants(pn) != null, true);
		assertEquals(InvariantCalculator.coveredBySInvariants(pn) != null, true);
	}

	@Test
	public void testVasyNet() {
		PetriNet pn = null;
		try {
			pn = new PnmlPNParser().parseFile("nets/pnml-iso/Vasy2003.pnml");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}

		Set<List<Integer>> expected = new HashSet<>();
		expected.add(Arrays.asList(new Integer[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
		Set<List<Integer>> calculated = InvariantCalculator.calcSInvariants(pn,
			InvariantCalculator.InvariantAlgorithm.PIPE);

		for (List<Integer> inv : expected) {
			assertTrue(calculated.contains(inv), inv.toString() + " " + calculated.toString());
		}

		assertEquals(InvariantCalculator.coveredBySInvariants(pn) != null, true);
	}

	
	@Test
	public void testNotCovered() {
		PetriNet pn = getAptPN("nets/eb-nets/trap-siphon-linalg/no-sinv-cover-net.apt");
		this.coveredBySInvariant(pn, false);

		pn = getAptPN("nets/eb-nets/more/3-2-net.apt");
		this.coveredBySInvariant(pn, false);
		this.coveredByTInvariant(pn, false);

		pn = getAptPN("nets/eb-nets/cover/cover1-net.apt");
		this.coveredBySInvariant(pn, false);
		this.coveredByTInvariant(pn, false);

		pn = getAptPN("nets/eb-nets/cover/cover2-net.apt");
		this.coveredBySInvariant(pn, false);
		this.coveredByTInvariant(pn, false);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
