/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014-2015  Uli Schlachter
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
import java.util.Arrays;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/** @author Uli Schlachter */
public class InequalitySystemTest {
	@Test
	public void testToStringEmptySystem1() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(0, "<");

		assertThat(system, hasToString("[\n0 < 0\n]"));
	}

	@Test
	public void testToStringEmptySystem2() {
		InequalitySystem system = new InequalitySystem();

		assertThat(system, hasToString("[\n]"));
	}

	@Test
	public void testToStringSimpleSystem() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(2, ">=", 1, 0, 1);
		system.addInequality(3, ">=", 0, 1, 1);

		assertThat(system, hasToString("[\n2 >= 1*x[0] + 1*x[2]\n3 >= 1*x[1] + 1*x[2]\n]"));
	}

	@Test
	public void testToStringSimpleSystemDifferentVariableNumber() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(2, ">=", 1);
		system.addInequality(3, ">=", 0, 1);

		assertThat(system, hasToString("[\n2 >= 1*x[0]\n3 >= 1*x[1]\n]"));
	}

	@Test
	public void testToStringTrivialSystem() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(0, "<=", 0);

		assertThat(system, hasToString("[\n0 <= 0\n]"));
	}

	@Test
	public void testToStringWithComments() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(0, "<=", Arrays.asList(BigInteger.ZERO), "Just ensuring the trivial");
		system.addInequality(0, "<=", Arrays.asList(BigInteger.ONE), "and something useful");
		system.addInequality(0, ">=", 1);

		assertThat(system, hasToString("[\n0 <= 0\t(Just ensuring the trivial)\n"
				+ "0 <= 1*x[0]\t(and something useful)\n0 >= 1*x[0]\n]"));
	}

	@Test
	public void testNumberOfVariables0() {
		InequalitySystem system = new InequalitySystem();

		assertThat(system.getNumberOfVariables(), equalTo(0));
	}

	@Test
	public void testNumberOfVariables1() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(0, "<=", 0);

		assertThat(system.getNumberOfVariables(), equalTo(1));
	}

	@Test
	public void testNumberOfVariables3() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(0, "<=", 0);
		system.addInequality(2, "<=", 0, 1, 2);
		system.addInequality(1, "<=", 0, 2);

		assertThat(system.getNumberOfVariables(), equalTo(3));
	}

	@Test
	public void testNumberOfVariablesTrailing0() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(0, "<=", 0, 0, 0);

		assertThat(system.getNumberOfVariables(), equalTo(3));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
