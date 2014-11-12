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

import uniol.apt.TestTSCollection;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.analysis.synthesize.Matchers.*;

/** @author Uli Schlachter */
@SuppressWarnings("unchecked") // I hate generics
@Test
public class RegionBasisTest {
	@Test
	public void testSingleStateTS() {
		RegionBasis basis = new RegionBasis(TestTSCollection.getSingleStateTS());

		assertThat(basis, emptyIterable());
	}

	@Test
	public void testcc1LTS() {
		RegionBasis basis = new RegionBasis(TestTSCollection.getcc1LTS());

		assertThat(basis, contains(anyOf(
						pureRegionWithWeights(Arrays.asList("a", "b", "c", "d"),
							Arrays.asList(1, -1, -1, 1)),
						pureRegionWithWeights(Arrays.asList("a", "b", "c", "d"),
							Arrays.asList(-1, 1, 1, -1)))));
	}

	@Test
	public void testThreeStatesTwoEdgesTS() {
		RegionBasis basis = new RegionBasis(TestTSCollection.getThreeStatesTwoEdgesTS());

		assertThat(basis, containsInAnyOrder(
					pureRegionWithWeights(Arrays.asList("a", "b"), Arrays.asList(1, 0)),
					pureRegionWithWeights(Arrays.asList("a", "b"), Arrays.asList(0, 1))));
	}

	@Test
	public void testPersistentTS() {
		RegionBasis basis = new RegionBasis(TestTSCollection.getPersistentTS());

		assertThat(basis, containsInAnyOrder(
					pureRegionWithWeights(Arrays.asList("a", "b"), Arrays.asList(1, 0)),
					pureRegionWithWeights(Arrays.asList("a", "b"), Arrays.asList(0, 1))));
	}

	@Test
	public void testNotTotallyReachableTS() {
		RegionBasis basis = new RegionBasis(TestTSCollection.getNotTotallyReachableTS());

		assertThat(basis, containsInAnyOrder(
					pureRegionWithWeights(Arrays.asList("a", "b"), Arrays.asList(1, 0)),
					pureRegionWithWeights(Arrays.asList("a", "b"), Arrays.asList(0, 1))));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
