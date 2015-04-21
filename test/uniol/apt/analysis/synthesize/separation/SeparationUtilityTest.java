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

package uniol.apt.analysis.synthesize.separation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.synthesize.MissingLocationException;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.analysis.synthesize.Matchers.*;

/** @author Uli Schlachter */
@Test
@SuppressWarnings("unchecked") // I hate generics
public class SeparationUtilityTest {
	public static abstract class Tester {
		protected TransitionSystem ts;
		protected RegionUtility utility;
		protected List<Region> regionBasis;
		protected Region region1;
		protected Region region2;
		protected Region region3;

		abstract protected TransitionSystem getTS();

		@BeforeClass
		public void setup() {
			ts = getTS();

			// Add an unreachable state
			State unreachable = ts.createState("unreachable");
			ts.createArc(unreachable, unreachable, "c");

			utility = new RegionUtility(ts);

			// I'm lazy, let's hope that we always end up with this order (which is currently guaranteed because
			// TransitionSystem.getAlphabet() uses a SortedSet).
			assertThat(utility.getEventIndex("a"), is(0));
			assertThat(utility.getEventIndex("b"), is(1));
			assertThat(utility.getEventIndex("c"), is(2));

			region1 = Region.createPureRegionFromVector(utility, Arrays.asList(-1, 0, 0));
			region2 = Region.createPureRegionFromVector(utility, Arrays.asList(0, -1, 0));
			region3 = Region.createPureRegionFromVector(utility, Arrays.asList(0, 0, -1));

			regionBasis = new ArrayList<>();
			regionBasis.add(region1);
			regionBasis.add(region2);
			regionBasis.add(region3);
		}

		@Test
		public void testStateSelfSeparation() {
			// A state cannot be separated from itself
			for (State state : utility.getTransitionSystem().getNodes()) {
				assertThat(SeparationUtility.isSeparatingRegion(region1, state, state), is(false));
				assertThat(SeparationUtility.isSeparatingRegion(region2, state, state), is(false));
				assertThat(SeparationUtility.isSeparatingRegion(region3, state, state), is(false));
			}
		}

		@Test
		public void testStateSeparation() {
			State s = utility.getTransitionSystem().getNode("s");
			State w = utility.getTransitionSystem().getNode("w");
			assertThat(SeparationUtility.isSeparatingRegion(region1, s, w), is(true));
			assertThat(SeparationUtility.isSeparatingRegion(region2, s, w), is(false));
			assertThat(SeparationUtility.isSeparatingRegion(region3, s, w), is(true));
		}

		@Test
		public void testEventSeparation1() {
			State w = utility.getTransitionSystem().getNode("w");
			assertThat(SeparationUtility.isSeparatingRegion(region1, w, "c"), is(false));
			assertThat(SeparationUtility.isSeparatingRegion(region2, w, "c"), is(false));
			assertThat(SeparationUtility.isSeparatingRegion(region3, w, "c"), is(true));
		}

		@Test
		public void testEventSeparation2() {
			State s = utility.getTransitionSystem().getNode("s");
			assertThat(SeparationUtility.isSeparatingRegion(region1, s, "c"), is(false));
			assertThat(SeparationUtility.isSeparatingRegion(region2, s, "c"), is(false));
			assertThat(SeparationUtility.isSeparatingRegion(region3, s, "c"), is(false));
		}

		@Test
		public void testStateSeparationUnreachable() {
			assertThat(SeparationUtility.isSeparatingRegion(region1, ts.getNode("unreachable"), ts.getInitialState()), is(false));
			assertThat(SeparationUtility.isSeparatingRegion(region2, ts.getNode("unreachable"), ts.getInitialState()), is(false));
			assertThat(SeparationUtility.isSeparatingRegion(region3, ts.getNode("unreachable"), ts.getInitialState()), is(false));
		}

		@Test
		public void testEventSeparationUnreachable() {
			assertThat(SeparationUtility.isSeparatingRegion(region1, ts.getNode("unreachable"), "a"), is(false));
			assertThat(SeparationUtility.isSeparatingRegion(region2, ts.getNode("unreachable"), "a"), is(false));
			assertThat(SeparationUtility.isSeparatingRegion(region3, ts.getNode("unreachable"), "a"), is(false));
		}
	}

	@Test
	public static class PureSynthesizablePathTSTest extends Tester {
		@Override
		protected TransitionSystem getTS() {
			return TestTSCollection.getPureSynthesizablePathTS();
		}
	}

	@Test
	public static class ImpureSynthesizablePathTSTest extends Tester {
		@Override
		protected TransitionSystem getTS() {
			return TestTSCollection.getImpureSynthesizablePathTS();
		}
	}

	@Test
	public void testNoLocations() throws Exception {
		TransitionSystem ts = TestTSCollection.getPersistentTS();

		assertThat(SeparationUtility.getLocationMap(new RegionUtility(ts), new PNProperties()),
				arrayContaining(nullValue(), nullValue()));
	}

	@Test
	public void testLocations() throws Exception {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		ts.getArc("s0", "l", "a").putExtension("location", "X");
		ts.getArc("s0", "r", "b").putExtension("location", "Y");
		ts.getArc("l", "s1", "b").putExtension("location", "Y");
		ts.getArc("r", "s1", "a").putExtension("location", "X");

		assertThat(SeparationUtility.getLocationMap(new RegionUtility(ts), new PNProperties()),
				arrayContaining("X", "Y"));
	}

	@Test(expectedExceptions = MissingLocationException.class)
	public void testMissingLocation() throws Exception {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		ts.getArc("s0", "l", "a").putExtension("location", "X");
		ts.getArc("r", "s1", "a").putExtension("location", "X");

		SeparationUtility.getLocationMap(new RegionUtility(ts), new PNProperties());
	}

	@Test(expectedExceptions = MissingLocationException.class)
	public void testMissingLocationON() throws Exception {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		PNProperties properties = new PNProperties();
		properties.setOutputNonbranching(true);

		ts.getArc("s0", "l", "a").putExtension("location", "X");
		ts.getArc("r", "s1", "a").putExtension("location", "X");

		SeparationUtility.getLocationMap(new RegionUtility(ts), properties);
	}

	@Test
	public void testOutputNonbranching() throws Exception {
		TransitionSystem ts = TestTSCollection.getPersistentTS();
		PNProperties properties = new PNProperties();
		properties.setOutputNonbranching(true);

		assertThat(SeparationUtility.getLocationMap(new RegionUtility(ts), properties),
				arrayContaining("0", "1"));
	}

	@Test
	public void testOutputNonbranchingOnlyOneEvent() throws Exception {
		TransitionSystem ts = TestTSCollection.getNonDeterministicTS();
		PNProperties properties = new PNProperties();
		properties.setOutputNonbranching(true);

		String nullStr = null;
		assertThat(SeparationUtility.getLocationMap(new RegionUtility(ts), properties),
				arrayContaining(nullStr));
	}

	@Test
	public void testNoEvents() throws Exception {
		TransitionSystem ts = TestTSCollection.getSingleStateTS();
		PNProperties properties = new PNProperties();

		assertThat(SeparationUtility.getLocationMap(new RegionUtility(ts), properties),
				emptyArray());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
