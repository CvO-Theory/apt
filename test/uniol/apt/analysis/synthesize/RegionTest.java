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

import java.util.Arrays;
import java.util.List;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.TransitionSystem;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.analysis.synthesize.Matchers.*;

/** @author Uli Schlachter */
public class RegionTest {
	private List<Integer> makeVector(int... args) {
		assert args.length % 2 == 0;

		Integer[] vector = new Integer[args.length / 2];
		for (int i = 0; i < args.length; i += 2) {
			assertThat(args[i], is(greaterThanOrEqualTo(0)));
			assertThat(args[i], is(lessThan(args.length / 2)));

			vector[args[i]] = args[i + 1];
		}

		return Arrays.asList(vector);
	}

	@Test
	public void testCreatePureEmptyRegion() {
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		RegionUtility utility = new RegionUtility(ts);

		Region region = Region.createPureRegionFromVector(utility, makeVector());

		assertThat(region, is(pureRegion()));
	}

	@Test
	public void testCreatePureRegion() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createPureRegionFromVector(utility, makeVector(a, 1, b, 3, c, -1));

		assertThat(region, is(pureRegionWithWeights(Arrays.asList("a", "b", "c"), Arrays.asList(1, 3, -1))));
	}

	@Test
	public void testCreateImpureRegion() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6));

		assertThat(region, is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						Arrays.asList(1, 2, 3, 4, 5, 6))));
	}

	@Test
	public void testToString1() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6), 0);

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

		Region region = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6))
			.withInitialMarking(0);

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

		Region region = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6));

		assertThat(region.evaluateParikhVector(makeVector(a, 0, b, 0, c, 0)), is(equalTo(0)));
	}

	@Test
	public void testEvaluateParikhVectorSingle() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6));

		assertThat(region.evaluateParikhVector(makeVector(a, 1, b, 0, c, 0)), is(equalTo(1)));
	}

	@Test
	public void testEvaluateParikhVectorDouble() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6));

		assertThat(region.evaluateParikhVector(makeVector(a, 2, b, 0, c, 0)), is(equalTo(2)));
	}

	@Test
	public void testEvaluateParikhVectorMixed() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6));

		assertThat(region.evaluateParikhVector(makeVector(a, -6, b, 7, c, 9)), is(equalTo(10)));
	}

	@Test
	public void testGetNormalRegionMarking() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6));

		assertThat(region.getNormalRegionMarking(), is(equalTo(0)));
	}

	@Test
	public void testGetNormalRegionMarking2() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region(utility, makeVector(a, 2, b, 3, c, 5), makeVector(a, 1, b, 0, c, 3));

		assertThat(region.getNormalRegionMarking(), is(equalTo(7)));
	}

	@Test
	public void testEqualsTrue() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region1 = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6));
		Region region2 = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6));

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

		Region region1 = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6));
		Region region2 = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 8));

		assertNotEquals(region1, region2);
	}

	@Test
	public void testCreateTrivialRegion() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		assertThat(Region.createTrivialRegion(utility),
				pureRegionWithWeights(Arrays.asList("a", "b", "c"), Arrays.asList(0, 0, 0)));
	}

	@Test
	public void testAddRegions1() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region r1 = Region.createTrivialRegion(utility);
		Region r2 = Region.createPureRegionFromVector(utility, makeVector(a, 1, b, 3, c, -1));
		Region r3 = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6));

		assertThat(r1.addRegion(r1), equalTo(r1));
		assertThat(r1.addRegion(r2), equalTo(r2));
		assertThat(r2.addRegion(r1), equalTo(r2));
		assertThat(r1.addRegionWithFactor(r2, 0), equalTo(r1));
		assertThat(r2.addRegionWithFactor(r1, 42), equalTo(r2));
		assertThat(r3.addRegionWithFactor(r2, 1), is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						Arrays.asList(1, 3, 3, 7, 6, 6))));
		assertThat(r3.addRegionWithFactor(r2, 2), is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						Arrays.asList(1, 4, 3, 10, 7, 6))));
	}

	@Test
	public void testAddRegions2() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region r1 = Region.createPureRegionFromVector(utility, makeVector(a, 1, b, 3, c, -2));
		Region r2 = Region.createPureRegionFromVector(utility, makeVector(a, 2, b, 6, c, -4));
		Region r3 = Region.createPureRegionFromVector(utility, makeVector(a, 3, b, 9, c, -6));

		assertThat(r1.addRegion(r1), equalTo(r2));
		assertThat(r1.addRegionWithFactor(r1, 2), equalTo(r3));
		assertThat(r1.addRegion(r2), equalTo(r3));
		assertThat(r2.addRegion(r1), equalTo(r3));
	}

	@Test
	public void testAddRegions3() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region r1 = Region.createTrivialRegion(utility);
		Region r2 = Region.createPureRegionFromVector(utility, makeVector(a, 1, b, 3, c, -2));
		Region r3 = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6));

		assertThat(r2.addRegionWithFactor(r2, -1), is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						Arrays.asList(1, 1, 3, 3, 2, 2))));
		assertThat(r3.addRegionWithFactor(r3, -1), is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						Arrays.asList(3, 3, 7, 7, 11, 11))));
		assertThat(r1.addRegionWithFactor(r3, -1), is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						Arrays.asList(2, 1, 4, 3, 6, 5))));
	}

	@Test
	public void testAddRegions4() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region r1 = Region.createPureRegionFromVector(utility, makeVector(a, 1, b, 3, c, -2));
		Region r2 = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6));

		Region result = r1.addRegionWithFactor(r1, 4);
		assertThat(result, is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						Arrays.asList(0, 5, 0, 15, 10, 0))));

		result = result.addRegionWithFactor(r2, 21);
		assertThat(result, is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						Arrays.asList(21, 47, 63, 99, 115, 126))));

		result = result.addRegionWithFactor(r1, -6);
		assertThat(result, is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						Arrays.asList(27, 47, 81, 99, 115, 138))));
	}

	@Test
	public void testMakePure1() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		Region region = Region.createTrivialRegion(utility);

		assertThat(region.makePure(), equalTo(region));
	}

	@Test
	public void makePure2() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region(utility, makeVector(a, 1, b, 4, c, 5), makeVector(a, 2, b, 3, c, 6));

		assertThat(region.makePure(), is(pureRegionWithWeights(Arrays.asList("a", "b", "c"),
						Arrays.asList(1, -1, 1))));
	}

	@Test
	public void unitRegion1() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int b = utility.getEventIndex("b");
		Region region = Region.createUnitRegion(utility, b);

		assertThat(region, is(impureRegionWithWeights(Arrays.asList("a", "b", "c"),
						Arrays.asList(0, 0, 1, 1, 0, 0))));
	}

	@Test(expectedExceptions = IndexOutOfBoundsException.class)
	public void unitRegion2() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		Region.createUnitRegion(utility, 23);
	}

	@Test
	public void testGetMarkingForState() throws UnreachableException {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region(utility, makeVector(a, 1, b, 3, c, 5), makeVector(a, 2, b, 4, c, 6));

		assertThat(region.getMarkingForState(ts.getNode("s")), equalTo(0));
		assertThat(region.getMarkingForState(ts.getNode("t")), equalTo(1));
		assertThat(region.getMarkingForState(ts.getNode("u")), equalTo(2));
		assertThat(region.getMarkingForState(ts.getNode("v")), equalTo(3));
		assertThat(region.getMarkingForState(ts.getNode("w")), equalTo(4));
	}

	@Test
	public void testGetMarkingForState2() throws UnreachableException {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = new Region(utility, makeVector(a, 2, b, 3, c, 5), makeVector(a, 1, b, 0, c, 3));

		assertThat(region.getMarkingForState(ts.getNode("s")), equalTo(7));
		assertThat(region.getMarkingForState(ts.getNode("t")), equalTo(6));
		assertThat(region.getMarkingForState(ts.getNode("u")), equalTo(3));
		assertThat(region.getMarkingForState(ts.getNode("v")), equalTo(1));
		assertThat(region.getMarkingForState(ts.getNode("w")), equalTo(0));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
