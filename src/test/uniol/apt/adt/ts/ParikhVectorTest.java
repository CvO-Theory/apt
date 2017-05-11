/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Uli Schlachter
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

package uniol.apt.adt.ts;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.*;

/** @author Uli Schlachter */
public class ParikhVectorTest {
	static private void compare(ParikhVector p1, ParikhVector p2, ParikhVector.Comparison result) {
		assertThat(p1.compare(p2), equalTo(result));

		// Swap the two vectors and compare again
		switch (result) {
			case GREATER_THAN:
				result = ParikhVector.Comparison.LESS_THAN;
				break;
			case LESS_THAN:
				result = ParikhVector.Comparison.GREATER_THAN;
				break;
			default:
				// EQUAL and INCOMPARABLE are symmetric
				break;
		}
		assertThat(p2.compare(p1), equalTo(result));
	}

	@Test
	public void testEquals() {
		ParikhVector pv = new ParikhVector("whatever");
		assertThat(pv.equals(null), equalTo(false));
		assertThat(pv.equals("a"), equalTo(false));
	}

	@Test
	public void testEmptyVector() {
		ParikhVector pv = new ParikhVector();
		assertThat(pv, equalTo(pv));
		assertThat(pv.add(pv), equalTo(pv));
		assertThat(pv.compare(pv), equalTo(ParikhVector.Comparison.EQUAL));
		assertThat(pv.isUncomparableTo(pv), equalTo(false));
		assertThat(pv.mutuallyDisjoint(pv), equalTo(true));
		assertThat(pv.sameOrMutuallyDisjoint(pv), equalTo(true));
		assertThat(pv.get("a"), equalTo(0));
		assertThat(pv.getLabels(), empty());
		assertThat(pv, hasToString("{}"));
	}

	@Test
	public void testVectorOneEntry() {
		ParikhVector pv = new ParikhVector("a");
		ParikhVector pvDoubled = pv.add(pv);
		ParikhVector pv2 = new ParikhVector("a", "a");

		assertThat(pv, equalTo(pv));
		assertThat(pvDoubled, equalTo(pv2));
		assertThat(pvDoubled.hashCode(), equalTo(pv2.hashCode()));
		assertThat(pv.get("a"), equalTo(1));
		assertThat(pv.get("b"), equalTo(0));
		assertThat(pv.getLabels(), contains("a"));
		assertThat(pv, hasToString("{a=1}"));
		assertThat(pv2, hasToString("{a=2}"));
		compare(pv, pv2, ParikhVector.Comparison.LESS_THAN);
		assertThat(pv.isUncomparableTo(pv2), equalTo(false));
		assertThat(pv2.isUncomparableTo(pv), equalTo(false));
		assertThat(pv.mutuallyDisjoint(pv2), equalTo(false));
		assertThat(pv.sameOrMutuallyDisjoint(pv2), equalTo(false));
	}

	@Test
	public void testAdd() {
		ParikhVector p = new ParikhVector();
		ParikhVector pa = new ParikhVector("a");
		ParikhVector pab = new ParikhVector("a", "b");

		assertThat(p.add("a"), equalTo(pa));
		assertThat(p.add("a", "b"), equalTo(pab));
		assertThat(pa.add(Arrays.asList("b")), equalTo(pab));
	}

	@Test
	public void testUncomparable() {
		ParikhVector pv1 = new ParikhVector("b", "b", "a");
		ParikhVector pv2 = new ParikhVector("a", "b", "c");

		assertThat(pv1.get("a"), equalTo(1));
		assertThat(pv1.get("b"), equalTo(2));
		assertThat(pv1.get("c"), equalTo(0));
		assertThat(pv1.getLabels(), contains("a", "b"));
		assertThat(pv2.getLabels(), contains("a", "b", "c"));
		assertThat(pv1, hasToString("{a=1, b=2}"));
		assertThat(pv2, hasToString("{a=1, b=1, c=1}"));
		compare(pv1, pv2, ParikhVector.Comparison.INCOMPARABLE);
		assertThat(pv1.isUncomparableTo(pv2), equalTo(true));
		assertThat(pv1.mutuallyDisjoint(pv2), equalTo(false));
		assertThat(pv1.sameOrMutuallyDisjoint(pv2), equalTo(false));
	}

	@Test
	public void testMutuallyDisjoint() {
		ParikhVector pv1 = new ParikhVector("b", "a", "b", "a");
		ParikhVector pv2 = new ParikhVector("c", "d", "e");

		assertThat(pv1.getLabels(), contains("a", "b"));
		assertThat(pv2.getLabels(), contains("c", "d", "e"));
		assertThat(pv1, hasToString("{a=2, b=2}"));
		assertThat(pv2, hasToString("{c=1, d=1, e=1}"));
		compare(pv1, pv2, ParikhVector.Comparison.INCOMPARABLE);
		assertThat(pv1.isUncomparableTo(pv2), equalTo(true));
		assertThat(pv1.mutuallyDisjoint(pv2), equalTo(true));
		assertThat(pv1.sameOrMutuallyDisjoint(pv2), equalTo(true));
	}

	@Test
	public void testComparable() {
		Map<String, Integer> map = new HashMap<>();

		map.put("zero", 0);
		map.put("a", 3);
		map.put("b", 8);
		ParikhVector pv1 = new ParikhVector(map);
		assertThat(pv1, hasToString("{a=3, b=8}"));
		assertThat(pv1.getLabels(), contains("a", "b"));

		map.put("a", 4);
		map.put("b", 4);
		compare(pv1, new ParikhVector(map), ParikhVector.Comparison.INCOMPARABLE);

		map.put("a", 3);
		map.put("b", 4);
		compare(pv1, new ParikhVector(map), ParikhVector.Comparison.GREATER_THAN);
	}

	@Test
	public void testMapNegativeEntries() {
		Map<String, Integer> map = new HashMap<>();
		map.put("zero", 0);
		map.put("a", 4);
		map.put("b", -1);
		ParikhVector pv = new ParikhVector(map);
		assertThat(pv, hasToString("{a=4}"));
		assertThat(pv.getLabels(), contains("a"));
	}

	@Test
	public void testTryRemoveMoreEntries() {
		ParikhVector pv = new ParikhVector("a", "b", "a", "a");
		pv = pv.tryRemove("a", 2);

		assertThat(pv, hasToString("{a=1, b=1}"));
		assertThat(pv.getLabels(), contains("a", "b"));
	}

	@Test
	public void testTryRemoveExactEntries() {
		ParikhVector pv = new ParikhVector("a", "b", "a");
		pv = pv.tryRemove("a", 2);

		assertThat(pv, hasToString("{b=1}"));
		assertThat(pv.getLabels(), contains("b"));
	}

	@Test
	public void testTryRemoveTooFewEntries() {
		ParikhVector pv = new ParikhVector("a", "b");
		pv = pv.tryRemove("a", 2);

		assertThat(pv, nullValue());
	}

	@Test
	public void testTryRemoveMissingEntry() {
		ParikhVector pv = new ParikhVector("b");
		pv = pv.tryRemove("a", 2);

		assertThat(pv, nullValue());
	}

	@Test
	public void testResidualMoreEntries() {
		ParikhVector pv = new ParikhVector("a", "b", "a", "a");
		pv = pv.residual(new ParikhVector("a", "a"));

		assertThat(pv, hasToString("{a=1, b=1}"));
		assertThat(pv.getLabels(), contains("a", "b"));
	}

	@Test
	public void testResidualExactEntries() {
		ParikhVector pv = new ParikhVector("a", "b", "a");
		pv = pv.residual(new ParikhVector("a", "a"));

		assertThat(pv, hasToString("{b=1}"));
		assertThat(pv.getLabels(), contains("b"));
	}

	@Test
	public void testResidualTooFewEntries() {
		ParikhVector pv = new ParikhVector("a", "b");
		pv = pv.residual(new ParikhVector("a", "a"));

		assertThat(pv, hasToString("{b=1}"));
		assertThat(pv.getLabels(), contains("b"));
	}

	@Test
	public void testResidualMissingEntry() {
		ParikhVector pv = new ParikhVector("b");
		pv = pv.residual(new ParikhVector("a", "a"));

		assertThat(pv, hasToString("{b=1}"));
		assertThat(pv.getLabels(), contains("b"));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
