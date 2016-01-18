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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matcher;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/** @author Uli Schlachter */
public class EquationSystemTest {
	// TODO: Technically, all these tests are too specific. A module has more than one basis and any of them would
	// be a correct result for the code under tests. However, testing if two basis are (in some sense) equal isn't
	// completely trivial.

	static Matcher<? super Iterable<? extends BigInteger>> equation(Integer... equation) {
		List<Matcher<? super BigInteger>> eq = new ArrayList<>();
		for (int i = 0; i < equation.length; i++)
			eq.add(equalTo(BigInteger.valueOf(equation[i])));
		Matcher<Iterable<? extends BigInteger>> first = contains(eq);

		eq = new ArrayList<>();
		for (int i = 0; i < equation.length; i++)
			eq.add(equalTo(BigInteger.valueOf(-equation[i])));
		return either(first).or(contains(eq));
	}

	@Test
	public void testSimpleSystem0() {
		EquationSystem system = new EquationSystem(3);
		system.addEquation(1, 0, 1);
		system.addEquation(0, 1, 1);

		Set<List<BigInteger>> basis = system.findBasis();
		List<Matcher<? super Iterable<? extends BigInteger>>> matchers = new ArrayList<>();
		matchers.add(equation(-1, -1, 1));
		assertThat(basis, containsInAnyOrder(matchers));
	}

	@Test
	public void testSimpleSystem1() {
		EquationSystem system = new EquationSystem(3);
		system.addEquation(2, 1, 3);
		system.addEquation(1, 1, 2);
		system.addEquation(1, 2, 3);

		Set<List<BigInteger>> basis = system.findBasis();
		List<Matcher<? super Iterable<? extends BigInteger>>> matchers = new ArrayList<>();
		matchers.add(equation(1, 1, -1));
		assertThat(basis, containsInAnyOrder(matchers));
	}

	@Test
	public void testSimpleSystem2() {
		EquationSystem system = new EquationSystem(3);
		system.addEquation(2, 1, 3);
		system.addEquation(1, 1, 2);
		system.addEquation(1, 2, 3);
		system.addEquation(3, 3, 6);

		Set<List<BigInteger>> basis = system.findBasis();
		List<Matcher<? super Iterable<? extends BigInteger>>> matchers = new ArrayList<>();
		matchers.add(equation(1, 1, -1));
		assertThat(basis, containsInAnyOrder(matchers));
	}

	@Test
	public void testSimpleSystem3() {
		EquationSystem system = new EquationSystem(3);
		system.addEquation(1, 2, 0);
		system.addEquation(0, 1, 1);
		system.addEquation(1, 0, 1);

		Set<List<BigInteger>> basis = system.findBasis();
		assertThat(basis, empty());
	}

	@Test
	public void testSimpleSystem4() {
		EquationSystem system = new EquationSystem(3);
		system.addEquation(4, 2, 6);
		system.addEquation(2, 2, 4);
		system.addEquation(2, 4, 6);

		Set<List<BigInteger>> basis = system.findBasis();
		List<Matcher<? super Iterable<? extends BigInteger>>> matchers = new ArrayList<>();
		matchers.add(equation(1, 1, -1));
		assertThat(basis, containsInAnyOrder(matchers));
	}

	@Test
	public void testSimpleSystem5() {
		EquationSystem system = new EquationSystem(3);
		system.addEquation(4, 2, 5);
		system.addEquation(2, 2, 4);

		Set<List<BigInteger>> basis = system.findBasis();
		List<Matcher<? super Iterable<? extends BigInteger>>> matchers = new ArrayList<>();
		matchers.add(equation(1, 3, -2));
		assertThat(basis, containsInAnyOrder(matchers));
	}

	@Test
	public void testSimpleSystem6() {
		EquationSystem system = new EquationSystem(3);

		Set<List<BigInteger>> basis = system.findBasis();
		List<Matcher<? super Iterable<? extends BigInteger>>> matchers = new ArrayList<>();
		matchers.add(equation(1, 0, 0));
		matchers.add(equation(0, 1, 0));
		matchers.add(equation(0, 0, 1));
		assertThat(basis, containsInAnyOrder(matchers));
	}

	@Test
	public void testSimpleSystem7() {
		EquationSystem system = new EquationSystem(3);
		system.addEquation(0, 42, 0);

		Set<List<BigInteger>> basis = system.findBasis();
		List<Matcher<? super Iterable<? extends BigInteger>>> matchers = new ArrayList<>();
		matchers.add(equation(1, 0, 0));
		matchers.add(equation(0, 0, 1));
		assertThat(basis, containsInAnyOrder(matchers));
	}

	@Test
	public void testSimpleSystem8() {
		EquationSystem system = new EquationSystem(3);
		system.addEquation(0, 42, 5);

		Set<List<BigInteger>> basis = system.findBasis();
		List<Matcher<? super Iterable<? extends BigInteger>>> matchers = new ArrayList<>();
		matchers.add(equation(1, 0, 0));
		matchers.add(equation(0, 5, -42));
		assertThat(basis, containsInAnyOrder(matchers));
	}

	@Test
	public void testEmptySystem1() {
		EquationSystem system = new EquationSystem(0);
		system.addEquation();

		Set<List<BigInteger>> basis = system.findBasis();
		assertThat(basis, empty());
	}

	@Test
	public void testEmptySystem2() {
		EquationSystem system = new EquationSystem(0);

		Set<List<BigInteger>> basis = system.findBasis();
		assertThat(basis, empty());
	}

	@Test
	public void testToStringEmptySystem1() {
		EquationSystem system = new EquationSystem(0);
		system.addEquation();

		assertThat(system, hasToString("[\n0 = 0\n]"));
	}

	@Test
	public void testToStringEmptySystem2() {
		EquationSystem system = new EquationSystem(0);

		assertThat(system, hasToString("[\n]"));
	}

	@Test
	public void testToStringSimpleSystem() {
		EquationSystem system = new EquationSystem(3);
		system.addEquation(1, 0, 1);
		system.addEquation(0, 1, 1);

		assertThat(system, anyOf(
					hasToString("[\n1*x[0] + 1*x[2] = 0\n1*x[1] + 1*x[2] = 0\n]"),
					hasToString("[\n1*x[1] + 1*x[2] = 0\n1*x[0] + 1*x[2] = 0\n]")));
	}

	@Test
	public void testToStringTrivialSystem() {
		EquationSystem system = new EquationSystem(1);
		system.addEquation(0);

		assertThat(system, hasToString("[\n0 = 0\n]"));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
