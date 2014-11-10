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

package uniol.apt.synthesize;

import java.util.Arrays;
import java.util.List;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.TransitionSystem;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.synthesize.Matchers.*;

/** @author Uli Schlachter */
@Test
public class RegionTest {
	private List<Integer> pureParikhVector(int... args) {
		assert args.length % 2 == 0;

		Integer[] vector = new Integer[args.length / 2];
		for (int i = 0; i < args.length; i += 2) {
			assertThat(args[i], is(greaterThanOrEqualTo(0)));
			assertThat(args[i], is(lessThan(args.length / 2)));

			vector[args[i]] = args[i+1];
		}

		return Arrays.asList(vector);
	}

	private List<Integer> impureParikhVector(int... args) {
		assert args.length % 3 == 0;

		Integer[] vector = new Integer[args.length * 2 / 3];
		for (int i = 0; i < args.length; i += 3) {
			assertThat(args[i], is(greaterThanOrEqualTo(0)));
			assertThat(args[i], is(lessThan(args.length / 3)));

			vector[args[i]] = args[i+1];
			vector[args[i] + args.length / 3] = args[i+2];
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
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
