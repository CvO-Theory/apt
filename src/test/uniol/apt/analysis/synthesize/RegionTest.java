/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

package uniol.apt.analysis.synthesize;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.math.BigInteger.ZERO;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.TransitionSystem;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.analysis.synthesize.matcher.Matchers.*;

/** @author Uli Schlachter */
public class RegionTest {
	static private List<BigInteger> makeVector(int... args) {
		assert args.length % 2 == 0;

		BigInteger[] vector = new BigInteger[args.length / 2];
		for (int i = 0; i < args.length; i += 2) {
			assertThat(args[i], is(greaterThanOrEqualTo(0)));
			assertThat(args[i], is(lessThan(args.length / 2)));

			vector[args[i]] = BigInteger.valueOf(args[i + 1]);
		}

		return Arrays.asList(vector);
	}

	static private List<BigInteger> asBigIntegerList(int... list) {
		List<BigInteger> result = new ArrayList<>(list.length);
		for (int i = 0; i < list.length; i++)
			result.add(BigInteger.valueOf(list[i]));
		return result;
	}

	@Test
	public void testCreatePureEmptyRegion() {
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		RegionUtility utility = new RegionUtility(ts);

		Region region = Region.Builder.createPure(utility, makeVector()).withInitialMarking(ZERO);

		assertThat(region, is(pureRegion()));
	}

	@Test
	public void testCreatePureRegion() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.Builder.createPure(utility, makeVector(a, 1, b, 3, c, -1))
				.withInitialMarking(ZERO);

		assertThat(region, is(pureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(1, 3, -1))));
	}

	@Test
	public void testCreateImpureRegion() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withNormalRegionInitialMarking();

		assertThat(region, is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						asBigIntegerList(1, 2, 3, 4, 5, 6))));
	}

	@Test
	public void testToString1() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withInitialMarking(ZERO);

		assertThat(region.toString(), anyOf(
					equalTo("{ init=0, 1:a:2, 3:b:4, 5:c:6 }"),
					equalTo("{ init=0, 1:a:2, 5:c:6, 3:b:4 }"),
					equalTo("{ init=0, 3:b:4, 1:a:2, 5:c:6 }"),
					equalTo("{ init=0, 3:b:4, 5:c:6, 1:a:2 }"),
					equalTo("{ init=0, 5:c:6, 1:a:2, 3:b:4 }"),
					equalTo("{ init=0, 5:c:6, 3:b:4, 1:a:2 }")
					));
	}

	@Test
	public void testToString2() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withInitialMarking(ZERO);

		assertThat(region.toString(), anyOf(
					equalTo("{ init=0, 1:a:2, 3:b:4, 5:c:6 }"),
					equalTo("{ init=0, 1:a:2, 5:c:6, 3:b:4 }"),
					equalTo("{ init=0, 3:b:4, 1:a:2, 5:c:6 }"),
					equalTo("{ init=0, 3:b:4, 5:c:6, 1:a:2 }"),
					equalTo("{ init=0, 5:c:6, 1:a:2, 3:b:4 }"),
					equalTo("{ init=0, 5:c:6, 3:b:4, 1:a:2 }")
					));
	}

	@Test
	public void testEvaluateParikhVectorEmpty() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withNormalRegionInitialMarking();

		assertThat(region.evaluateParikhVector(makeVector(a, 0, b, 0, c, 0)), is(equalTo(ZERO)));
	}

	@Test
	public void testEvaluateParikhVectorSingle() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withNormalRegionInitialMarking();

		assertThat(region.evaluateParikhVector(makeVector(a, 1, b, 0, c, 0)), is(equalTo(BigInteger.ONE)));
	}

	@Test
	public void testEvaluateParikhVectorDouble() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withNormalRegionInitialMarking();

		assertThat(region.evaluateParikhVector(makeVector(a, 2, b, 0, c, 0)),
				is(equalTo(BigInteger.valueOf(2))));
	}

	@Test
	public void testEvaluateParikhVectorMixed() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withNormalRegionInitialMarking();

		assertThat(region.evaluateParikhVector(makeVector(a, -6, b, 7, c, 9)),
				is(equalTo(BigInteger.valueOf(10))));
	}

	@Test
	public void testWithNormalRegionMarking() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withNormalRegionInitialMarking();

		assertThat(region.getInitialMarking(), is(equalTo(ZERO)));
	}

	@Test
	public void testWithNormalRegionMarking2() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region.Builder(utility, makeVector(a, 2, b, 3, c, 5), makeVector(a, 1, b, 0, c, 3))
				.withNormalRegionInitialMarking();

		assertThat(region.getInitialMarking(), is(equalTo(BigInteger.valueOf(7))));
	}

	@Test
	public void testCopyRegion() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);
		RegionUtility utility2 = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region.Builder(utility, makeVector(a, 2, b, 3, c, 5), makeVector(a, 1, b, 0, c, 3)).withNormalRegionInitialMarking();
		Region region2 = Region.Builder.copyRegionToUtility(utility2, region);

		assertThat(region.getInitialMarking(), is(equalTo(BigInteger.valueOf(7))));
		assertThat(region2.getInitialMarking(), is(equalTo(BigInteger.valueOf(7))));
		assertThat(region, is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						asBigIntegerList(2, 1, 3, 0, 5, 3))));
		assertThat(region2, is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						asBigIntegerList(2, 1, 3, 0, 5, 3))));
		assertThat(region.getTransitionSystem(), sameInstance(ts));
		assertThat(region2.getTransitionSystem(), sameInstance(ts));
		assertThat(region2.toString(), equalTo(region.toString()));
		assertThat(region2, not(equalTo(region)));
	}

	@Test
	public void testCopyRegionDifferentEventList() {
		TransitionSystem ts1 = TestTSCollection.getThreeStatesTwoEdgesTS();
		TransitionSystem ts2 = TestTSCollection.getPathTS();

		RegionUtility utility1 = new RegionUtility(ts1);
		RegionUtility utility2 = new RegionUtility(ts2);

		int a = utility1.getEventIndex("a");
		int b = utility1.getEventIndex("b");

		Region region1 = new Region.Builder(utility1, makeVector(a, 0, b, 1), makeVector(a, 7, b, 1)).withInitialMarking(BigInteger.ONE);
		Region region2 = Region.Builder.copyRegionToUtility(utility2, region1);

		assertThat(region1.getRegionUtility(), is(utility1));
		assertThat(region2.getRegionUtility(), is(utility2));
		assertThat(region2.getInitialMarking(), is(equalTo(BigInteger.ONE)));
		assertThat(region1, is(impureRegionWithWeights(Arrays.asList("a", "b"),
						asBigIntegerList(0, 7, 1, 1))));
		assertThat(region2, is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						asBigIntegerList(0, 7, 1, 1, 0, 0))));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testCopyRegionFailure() {
		RegionUtility utility = new RegionUtility(TestTSCollection.getPathTS());
		RegionUtility utility2 = new RegionUtility(TestTSCollection.getSingleStateTS());

		Region region = new Region.Builder(utility).withInitialMarking(ZERO);
		Region.Builder.copyRegionToUtility(utility2, region);
	}

	@Test
	public void testEqualsTrue() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region1 = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withNormalRegionInitialMarking();
		Region region2 = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withNormalRegionInitialMarking();

		assertEquals(region1, region2);
		assertEquals(region1.hashCode(), region2.hashCode());
	}

	@Test
	public void testEqualsFalse() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region1 = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withNormalRegionInitialMarking();
		Region region2 = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 8))
				.withNormalRegionInitialMarking();

		assertNotEquals(region1, region2);
	}

	@Test
	public void testCreateTrivialRegion() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		assertThat(new Region.Builder(utility).withInitialMarking(ZERO),
				pureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(0, 0, 0)));
	}

	@Test
	public void testAddRegions1() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region r1 = new Region.Builder(utility).withInitialMarking(ZERO);
		Region r2 = Region.Builder.createPure(utility, makeVector(a, 1, b, 3, c, -1)).withInitialMarking(ZERO);
		Region r3 = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withInitialMarking(ZERO);

		assertThat(new Region.Builder(r1).addRegionWithFactor(r1, BigInteger.ONE).withInitialMarking(ZERO),
				equalTo(r1));
		assertThat(new Region.Builder(r1).addRegionWithFactor(r2, BigInteger.ONE).withInitialMarking(ZERO),
				equalTo(r2));
		assertThat(new Region.Builder(r2).addRegionWithFactor(r1, BigInteger.ONE).withInitialMarking(ZERO),
				equalTo(r2));
		assertThat(new Region.Builder(r1).addRegionWithFactor(r2, ZERO).withInitialMarking(ZERO),
				equalTo(r1));
		assertThat(new Region.Builder(r2).addRegionWithFactor(r1, BigInteger.valueOf(42)).withInitialMarking(ZERO),
				equalTo(r2));
		assertThat(new Region.Builder(r3).addRegionWithFactor(r2, BigInteger.ONE).withInitialMarking(ZERO),
				is(impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(1, 3, 3, 7, 6, 6))));
		assertThat(new Region.Builder(r3).addRegionWithFactor(r2, BigInteger.valueOf(2)).withInitialMarking(ZERO),
				is(impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(1, 4, 3, 10, 7, 6))));
	}

	@Test
	public void testAddRegions2() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region r1 = Region.Builder.createPure(utility, makeVector(a, 1, b, 3, c, -2)).withInitialMarking(ZERO);
		Region r2 = Region.Builder.createPure(utility, makeVector(a, 2, b, 6, c, -4)).withInitialMarking(ZERO);
		Region r3 = Region.Builder.createPure(utility, makeVector(a, 3, b, 9, c, -6)).withInitialMarking(ZERO);

		assertThat(new Region.Builder(r1).addRegionWithFactor(r1, BigInteger.ONE).withInitialMarking(ZERO),
				equalTo(r2));
		assertThat(new Region.Builder(r1).addRegionWithFactor(r1, BigInteger.valueOf(2)).withInitialMarking(ZERO),
				equalTo(r3));
		assertThat(new Region.Builder(r1).addRegionWithFactor(r2, BigInteger.ONE).withInitialMarking(ZERO),
				equalTo(r3));
		assertThat(new Region.Builder(r2).addRegionWithFactor(r1, BigInteger.ONE).withInitialMarking(ZERO),
				equalTo(r3));
	}

	@Test
	public void testAddRegions3() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region r1 = new Region.Builder(utility).withInitialMarking(ZERO);
		Region r2 = Region.Builder.createPure(utility, makeVector(a, 1, b, 3, c, -2)).withInitialMarking(ZERO);
		Region r3 = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withNormalRegionInitialMarking();

		assertThat(new Region.Builder(r2).addRegionWithFactor(r2, BigInteger.valueOf(-1)).withInitialMarking(ZERO),
				is(impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(1, 1, 3, 3, 2, 2))));
		assertThat(new Region.Builder(r3).addRegionWithFactor(r3, BigInteger.valueOf(-1)).withInitialMarking(ZERO),
				is(impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(3, 3, 7, 7, 11, 11))));
		assertThat(new Region.Builder(r1).addRegionWithFactor(r3, BigInteger.valueOf(-1)).withInitialMarking(ZERO),
				is(impureRegionWithWeights(Arrays.asList("a", "b", "c"), asBigIntegerList(2, 1, 4, 3, 6, 5))));
	}

	@Test
	public void testAddRegions4() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region r1 = Region.Builder.createPure(utility, makeVector(a, 1, b, 3, c, -2)).withInitialMarking(ZERO);
		Region r2 = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withNormalRegionInitialMarking();

		Region result = new Region.Builder(r1).addRegionWithFactor(r1, BigInteger.valueOf(4)).withInitialMarking(ZERO);
		assertThat(result, is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						asBigIntegerList(0, 5, 0, 15, 10, 0))));

		result = new Region.Builder(result).addRegionWithFactor(r2, BigInteger.valueOf(21)).withInitialMarking(ZERO);
		assertThat(result, is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						asBigIntegerList(21, 47, 63, 99, 115, 126))));

		result = new Region.Builder(result).addRegionWithFactor(r1, BigInteger.valueOf(-6)).withInitialMarking(ZERO);
		assertThat(result, is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						asBigIntegerList(27, 47, 81, 99, 115, 138))));
	}

	@Test
	public void testMakePure1() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		Region.Builder builder = new Region.Builder(utility);
		Region region = builder.withInitialMarking(ZERO);

		assertThat(builder.makePure().withInitialMarking(ZERO), equalTo(region));
	}

	@Test
	public void makePure2() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region.Builder(utility, makeVector(a, 1, b, 4, c, 5), makeVector(a, 2, b, 3, c, 6))
				.makePure().withNormalRegionInitialMarking();

		assertThat(region, is(pureRegionWithWeights(Arrays.asList("a", "b", "c"),
						asBigIntegerList(1, -1, 1))));
	}

	@Test
	public void unitRegion1() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		Region region = new Region.Builder(utility).addLoopAround("b", BigInteger.ONE).withInitialMarking(ZERO);
		assertThat(region, is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						asBigIntegerList(0, 0, 1, 1, 0, 0))));
	}

	@Test(expectedExceptions = IndexOutOfBoundsException.class)
	public void unitRegion2() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		new Region.Builder(utility).addLoopAround(23, BigInteger.ONE);
	}

	@Test
	public void testGetMarkingForState() throws UnreachableException {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region.Builder(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
				.withNormalRegionInitialMarking();

		assertThat(region.getMarkingForState(ts.getNode("s")), equalTo(BigInteger.valueOf(0)));
		assertThat(region.getMarkingForState(ts.getNode("t")), equalTo(BigInteger.valueOf(1)));
		assertThat(region.getMarkingForState(ts.getNode("u")), equalTo(BigInteger.valueOf(2)));
		assertThat(region.getMarkingForState(ts.getNode("v")), equalTo(BigInteger.valueOf(3)));
		assertThat(region.getMarkingForState(ts.getNode("w")), equalTo(BigInteger.valueOf(4)));
	}

	@Test
	public void testGetMarkingForState2() throws UnreachableException {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region.Builder(utility, makeVector(a, 2, b, 3, c, 5), makeVector(a, 1, b, 0, c, 3))
				.withNormalRegionInitialMarking();

		assertThat(region.getMarkingForState(ts.getNode("s")), equalTo(BigInteger.valueOf(7)));
		assertThat(region.getMarkingForState(ts.getNode("t")), equalTo(BigInteger.valueOf(6)));
		assertThat(region.getMarkingForState(ts.getNode("u")), equalTo(BigInteger.valueOf(3)));
		assertThat(region.getMarkingForState(ts.getNode("v")), equalTo(BigInteger.valueOf(1)));
		assertThat(region.getMarkingForState(ts.getNode("w")), equalTo(BigInteger.valueOf(0)));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
