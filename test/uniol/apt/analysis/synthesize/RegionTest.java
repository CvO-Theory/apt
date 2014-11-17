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
@Test
public class RegionTest {
	private List<Integer> pureParikhVector(int... args) {
		assert args.length % 2 == 0;

		Integer[] vector = new Integer[args.length / 2];
		for (int i = 0; i < args.length; i += 2) {
			assertThat(args[i], is(greaterThanOrEqualTo(0)));
			assertThat(args[i], is(lessThan(args.length / 2)));

			vector[args[i]] = args[i + 1];
		}

		return Arrays.asList(vector);
	}

	private List<Integer> impureParikhVector(int... args) {
		assert args.length % 3 == 0;

		Integer[] vector = new Integer[args.length * 2 / 3];
		for (int i = 0; i < args.length; i += 3) {
			assertThat(args[i], is(greaterThanOrEqualTo(0)));
			assertThat(args[i], is(lessThan(args.length / 3)));

			vector[args[i]] = args[i + 1];
			vector[args[i] + args.length / 3] = args[i + 2];
		}

		return Arrays.asList(vector);
	}

	@Test
	public void testCreatePureEmptyRegion() {
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		RegionUtility utility = new RegionUtility(ts);

		Region region = Region.createPureRegionFromVector(utility, pureParikhVector());

		assertThat(region, is(pureRegion()));
	}

	@Test
	public void testCreateImpureEmptyRegion() {
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		RegionUtility utility = new RegionUtility(ts);

		Region region = Region.createImpureRegionFromVector(utility, pureParikhVector());

		assertThat(region, is(pureRegion()));
	}

	@Test
	public void testCreatePureRegion() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createPureRegionFromVector(utility, pureParikhVector(a, 1, b, 3, c, -1));

		assertThat(region, is(pureRegionWithWeights(Arrays.asList("a", "b", "c"), Arrays.asList(1, 3, -1))));
	}

	@Test
	public void testCreateImpureRegion1() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 0, 1, b, 0, 3, c, 1, 0));

		assertThat(region, is(pureRegionWithWeights(Arrays.asList("a", "b", "c"), Arrays.asList(1, 3, -1))));
	}

	@Test
	public void testCreateImpureRegion2() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

		assertThat(region, is(impureRegionWithWeights(Arrays.asList("a", "b", "c"), Arrays.asList(1, 2, 3, 4, 5, 6))));
	}

	@Test
	public void testToString() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

		assertThat(region.toString(), anyOf(
					equalTo("{ 1:a:2, 3:b:4, 5:c:6 }"),
					equalTo("{ 1:a:2, 5:c:6, 3:b:4 }"),
					equalTo("{ 3:b:4, 1:a:2, 5:c:6 }"),
					equalTo("{ 3:b:4, 5:c:6, 1:a:2 }"),
					equalTo("{ 5:c:6, 1:a:2, 3:b:4 }"),
					equalTo("{ 5:c:6, 3:b:4, 1:a:2 }")
					));
	}

	@Test
	public void testEvaluateParikhVectorEmpty() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

		assertThat(region.evaluateParikhVector(pureParikhVector(a, 0, b, 0, c, 0)), is(equalTo(0)));
	}

	@Test
	public void testEvaluateParikhVectorSingle() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

		assertThat(region.evaluateParikhVector(pureParikhVector(a, 1, b, 0, c, 0)), is(equalTo(1)));
	}

	@Test
	public void testEvaluateParikhVectorDouble() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

		assertThat(region.evaluateParikhVector(pureParikhVector(a, 2, b, 0, c, 0)), is(equalTo(2)));
	}

	@Test
	public void testEvaluateParikhVectorMixed() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

		assertThat(region.evaluateParikhVector(pureParikhVector(a, -6, b, 7, c, 9)), is(equalTo(10)));
	}

	@Test
	public void testEvaluateParikhVectorEmptyWithEvent() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

		assertThat(region.evaluateParikhVector(pureParikhVector(a, 0, b, 0, c, 0), a), is(equalTo(-1)));
	}

	@Test
	public void testEvaluateParikhVectorSingleWithEvent() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

		assertThat(region.evaluateParikhVector(pureParikhVector(a, 1, b, 0, c, 0), a), is(equalTo(0)));
	}

	@Test
	public void testEvaluateParikhVectorDoubleWithEvent() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

		assertThat(region.evaluateParikhVector(pureParikhVector(a, 2, b, 0, c, 0), b), is(equalTo(-1)));
	}

	@Test
	public void testEvaluateParikhVectorMixedWithEvent() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

		assertThat(region.evaluateParikhVector(pureParikhVector(a, -6, b, 7, c, 9), c), is(equalTo(5)));
	}

	@Test
	public void testGetNormalRegionMarking() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

		assertThat(region.getNormalRegionMarking(), is(equalTo(3)));
	}

	@Test
	public void testGetNormalRegionMarking2() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 2, 1, b, 3, 0, c, 5, 3));

		assertThat(region.getNormalRegionMarking(), is(equalTo(9)));
	}

	@Test
	public void testEqualsTrue() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region1 = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));
		Region region2 = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

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

		Region region1 = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));
		Region region2 = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 8));

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
		Region r2 = Region.createPureRegionFromVector(utility, pureParikhVector(a, 1, b, 3, c, -1));
		Region r3 = Region.createImpureRegionFromVector(utility, impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

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

		Region r1 = Region.createPureRegionFromVector(utility, pureParikhVector(a, 1, b, 3, c, -2));
		Region r2 = Region.createPureRegionFromVector(utility, pureParikhVector(a, 2, b, 6, c, -4));
		Region r3 = Region.createPureRegionFromVector(utility, pureParikhVector(a, 3, b, 9, c, -6));

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
		Region r2 = Region.createPureRegionFromVector(utility, pureParikhVector(a, 1, b, 3, c, -2));
		Region r3 = Region.createImpureRegionFromVector(utility, impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

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

		Region r1 = Region.createPureRegionFromVector(utility, pureParikhVector(a, 1, b, 3, c, -2));
		Region r2 = Region.createImpureRegionFromVector(utility, impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

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

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 4, 3, c, 5, 6));

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
	public void testGetNormalRegionMarkingForState() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 1, 2, b, 3, 4, c, 5, 6));

		assertThat(region.getNormalRegionMarkingForState(ts.getNode("s")), equalTo(3));
		assertThat(region.getNormalRegionMarkingForState(ts.getNode("t")), equalTo(4));
		assertThat(region.getNormalRegionMarkingForState(ts.getNode("u")), equalTo(5));
		assertThat(region.getNormalRegionMarkingForState(ts.getNode("v")), equalTo(6));
		assertThat(region.getNormalRegionMarkingForState(ts.getNode("w")), equalTo(7));
	}

	@Test
	public void testGetNormalRegionMarkingForState2() {
		TransitionSystem ts = TestTSCollection.getPathTS();
		RegionUtility utility = new RegionUtility(ts);

		int a = utility.getEventIndex("a");
		int b = utility.getEventIndex("b");
		int c = utility.getEventIndex("c");

		Region region = Region.createImpureRegionFromVector(utility,
				impureParikhVector(a, 2, 1, b, 3, 0, c, 5, 3));

		assertThat(region.getNormalRegionMarkingForState(ts.getNode("s")), equalTo(9));
		assertThat(region.getNormalRegionMarkingForState(ts.getNode("t")), equalTo(8));
		assertThat(region.getNormalRegionMarkingForState(ts.getNode("u")), equalTo(5));
		assertThat(region.getNormalRegionMarkingForState(ts.getNode("v")), equalTo(3));
		assertThat(region.getNormalRegionMarkingForState(ts.getNode("w")), equalTo(2));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
