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

package uniol.apt.util.equations;

import java.util.List;

import org.testng.annotations.Test;
import org.hamcrest.collection.IsIterableWithSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/** @author Uli Schlachter */
@Test
public class InequalitySystemTest {
	@Test
	public void testSimpleSystem0() {
		InequalitySystem system = new InequalitySystem(3);
		system.addInequality(0, 1, 0, 1);
		system.addInequality(0, -1, 0, -1);
		system.addInequality(0, 0, 1, 1);
		system.addInequality(0, 0, -1, -1);

		List<Integer> solution = system.findSolution();
		assertThat(solution, IsIterableWithSize.<Integer>iterableWithSize(3));
		int x = solution.get(0), y = solution.get(1), z = solution.get(2);
		assertThat(x + z, is(0));
		assertThat(y + z, is(0));
	}

	@Test
	public void testSimpleSystem1() {
		InequalitySystem system = new InequalitySystem(3);
		system.addInequality(0, 2, 1, 3);
		system.addInequality(0, 1, 1, 2);
		system.addInequality(0, 1, 2, 3);

		List<Integer> solution = system.findSolution();
		assertThat(solution, IsIterableWithSize.<Integer>iterableWithSize(3));
		int x = solution.get(0), y = solution.get(1), z = solution.get(2);
		assertThat(2*x + 1*y + 3*z, lessThanOrEqualTo(0));
		assertThat(1*x + 1*y + 2*z, lessThanOrEqualTo(0));
		assertThat(1*x + 2*y + 3*z, lessThanOrEqualTo(0));
	}

	@Test
	public void testSimpleSystem2() {
		InequalitySystem system = new InequalitySystem(3);
		system.addInequality(1, 2, 1, 3);
		system.addInequality(2, 1, 1, 2);
		system.addInequality(3, 1, 2, 3);
		system.addInequality(4, 3, 3, 6);

		List<Integer> solution = system.findSolution();
		assertThat(solution, IsIterableWithSize.<Integer>iterableWithSize(3));
		int x = solution.get(0), y = solution.get(1), z = solution.get(2);
		assertThat(2*x + 1*y + 3*z, lessThanOrEqualTo(1));
		assertThat(1*x + 1*y + 2*z, lessThanOrEqualTo(2));
		assertThat(1*x + 2*y + 3*z, lessThanOrEqualTo(3));
		assertThat(3*x + 3*y + 6*z, lessThanOrEqualTo(4));
	}

	@Test
	public void testSimpleSystem3() {
		InequalitySystem system = new InequalitySystem(3);
		system.addInequality(0, 1, 2, 0);
		system.addInequality(0, 0, 1, 1);
		system.addInequality(0, 1, 0, 1);

		List<Integer> solution = system.findSolution();
		assertThat(solution, IsIterableWithSize.<Integer>iterableWithSize(3));
		int x = solution.get(0), y = solution.get(1), z = solution.get(2);
		assertThat(1*x + 2*y + 0*z, lessThanOrEqualTo(0));
		assertThat(0*x + 1*y + 1*z, lessThanOrEqualTo(0));
		assertThat(1*x + 0*y + 1*z, lessThanOrEqualTo(0));
	}

	@Test
	public void testSimpleSystem4() {
		InequalitySystem system = new InequalitySystem(3);
		system.addInequality(10, 4, 2, 6);
		system.addInequality(10, 2, 2, 4);
		system.addInequality(10, 2, 4, 6);

		List<Integer> solution = system.findSolution();
		assertThat(solution, IsIterableWithSize.<Integer>iterableWithSize(3));
		int x = solution.get(0), y = solution.get(1), z = solution.get(2);
		assertThat( 4*x + 2*y + 6*z, lessThanOrEqualTo(10));
		assertThat( 2*x + 2*y + 4*z, lessThanOrEqualTo(10));
		assertThat( 2*x + 4*y + 6*z, lessThanOrEqualTo(10));
	}

	@Test
	public void testSimpleSystem5() {
		InequalitySystem system = new InequalitySystem(3);
		system.addInequality(0, 4, 2, 5);
		system.addInequality(0, 2, 2, 4);

		List<Integer> solution = system.findSolution();
		assertThat(solution, IsIterableWithSize.<Integer>iterableWithSize(3));
		int x = solution.get(0), y = solution.get(1), z = solution.get(2);
		assertThat(4*x + 2*y + 5*z, lessThanOrEqualTo(0));
		assertThat(2*x + 2*y + 4*z, lessThanOrEqualTo(0));
	}

	@Test
	public void testSimpleSystem6() {
		InequalitySystem system = new InequalitySystem(3);

		List<Integer> solution = system.findSolution();
		assertThat(solution, IsIterableWithSize.<Integer>iterableWithSize(3));
	}

	@Test
	public void testSimpleSystem7() {
		InequalitySystem system = new InequalitySystem(3);
		system.addInequality(0, 0, 42, 0);

		List<Integer> solution = system.findSolution();
		assertThat(solution, IsIterableWithSize.<Integer>iterableWithSize(3));
		int y = solution.get(1);
		assertThat(y, lessThanOrEqualTo(0));
	}

	@Test
	public void testEmptySystem1() {
		InequalitySystem system = new InequalitySystem(0);
		system.addInequality(0);

		List<Integer> solution = system.findSolution();
		assertThat(solution, empty());
	}

	@Test
	public void testEmptySystem2() {
		InequalitySystem system = new InequalitySystem(0);

		List<Integer> solution = system.findSolution();
		assertThat(solution, empty());
	}

	@Test
	public void testToStringEmptySystem1() {
		InequalitySystem system = new InequalitySystem(0);
		system.addInequality(0);

		assertThat(system, hasToString("[\n0 <= 0\n]"));
	}

	@Test
	public void testToStringEmptySystem2() {
		InequalitySystem system = new InequalitySystem(0);

		assertThat(system, hasToString("[\n]"));
	}

	@Test
	public void testToStringSimpleSystem() {
		InequalitySystem system = new InequalitySystem(3);
		system.addInequality(2, 1, 0, 1);
		system.addInequality(3, 0, 1, 1);

		assertThat(system, hasToString("[\n1*x[0] + 1*x[2] <= 2\n1*x[1] + 1*x[2] <= 3\n]"));
	}

	@Test
	public void testToStringTrivialSystem() {
		InequalitySystem system = new InequalitySystem(1);
		system.addInequality(0, 0);

		assertThat(system, hasToString("[\n0 <= 0\n]"));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
