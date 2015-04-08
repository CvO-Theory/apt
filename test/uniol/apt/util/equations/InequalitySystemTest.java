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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/** @author Uli Schlachter */
@Test
public class InequalitySystemTest {
	static public class TestSolver {
		@Test
		public void testSimpleSystem0() {
			InequalitySystem system = new InequalitySystem();
			system.addInequality(0, ">=", 1, 0, 1);
			system.addInequality(0, ">=", -1, 0, -1);
			system.addInequality(0, ">=", 0, 1, 1);
			system.addInequality(0, ">=", 0, -1, -1);

			List<Integer> solution = system.findSolution();
			assertThat(solution, hasSize(3));
			int x = solution.get(0), y = solution.get(1), z = solution.get(2);
			assertThat(x + z, is(0));
			assertThat(y + z, is(0));
		}

		@Test
		public void testSimpleSystem1() {
			InequalitySystem system = new InequalitySystem();
			system.addInequality(0, ">=", 2, 1, 3);
			system.addInequality(0, ">=", 1, 1, 2);
			system.addInequality(0, ">=", 1, 2, 3);

			List<Integer> solution = system.findSolution();
			assertThat(solution, hasSize(3));
			int x = solution.get(0), y = solution.get(1), z = solution.get(2);
			assertThat(2 * x + 1 * y + 3 * z, lessThanOrEqualTo(0));
			assertThat(1 * x + 1 * y + 2 * z, lessThanOrEqualTo(0));
			assertThat(1 * x + 2 * y + 3 * z, lessThanOrEqualTo(0));
		}

		@Test
		public void testSimpleSystem2() {
			InequalitySystem system = new InequalitySystem();
			system.addInequality(1, ">=", 2, 1, 3);
			system.addInequality(2, ">=", 1, 1, 2);
			system.addInequality(3, ">=", 1, 2, 3);
			system.addInequality(4, ">=", 3, 3, 6);

			List<Integer> solution = system.findSolution();
			assertThat(solution, hasSize(3));
			int x = solution.get(0), y = solution.get(1), z = solution.get(2);
			assertThat(2 * x + 1 * y + 3 * z, lessThanOrEqualTo(1));
			assertThat(1 * x + 1 * y + 2 * z, lessThanOrEqualTo(2));
			assertThat(1 * x + 2 * y + 3 * z, lessThanOrEqualTo(3));
			assertThat(3 * x + 3 * y + 6 * z, lessThanOrEqualTo(4));
		}

		@Test
		public void testSimpleSystem3() {
			InequalitySystem system = new InequalitySystem();
			system.addInequality(0, ">=", 1, 2);
			system.addInequality(0, ">=", 0, 1, 1);
			system.addInequality(0, ">=", 1, 0, 1);

			List<Integer> solution = system.findSolution();
			assertThat(solution, hasSize(3));
			int x = solution.get(0), y = solution.get(1), z = solution.get(2);
			assertThat(1 * x + 2 * y + 0 * z, lessThanOrEqualTo(0));
			assertThat(0 * x + 1 * y + 1 * z, lessThanOrEqualTo(0));
			assertThat(1 * x + 0 * y + 1 * z, lessThanOrEqualTo(0));
		}

		@Test
		public void testSimpleSystem4() {
			InequalitySystem system = new InequalitySystem();
			system.addInequality(10, ">=", 4, 2, 6);
			system.addInequality(10, ">=", 2, 2, 4);
			system.addInequality(10, ">=", 2, 4, 6);

			List<Integer> solution = system.findSolution();
			assertThat(solution, hasSize(3));
			int x = solution.get(0), y = solution.get(1), z = solution.get(2);
			assertThat(4 * x + 2 * y + 6 * z, lessThanOrEqualTo(10));
			assertThat(2 * x + 2 * y + 4 * z, lessThanOrEqualTo(10));
			assertThat(2 * x + 4 * y + 6 * z, lessThanOrEqualTo(10));
		}

		@Test
		public void testSimpleSystem5() {
			InequalitySystem system = new InequalitySystem();
			system.addInequality(0, ">=", 4, 2, 5);
			system.addInequality(0, ">=", 2, 2, 4);

			List<Integer> solution = system.findSolution();
			assertThat(solution, hasSize(3));
			int x = solution.get(0), y = solution.get(1), z = solution.get(2);
			assertThat(4 * x + 2 * y + 5 * z, lessThanOrEqualTo(0));
			assertThat(2 * x + 2 * y + 4 * z, lessThanOrEqualTo(0));
		}

		@Test
		public void testSimpleSystem6() {
			InequalitySystem system = new InequalitySystem();

			List<Integer> solution = system.findSolution();
			assertThat(solution, empty());
		}

		@Test
		public void testSimpleSystem7() {
			InequalitySystem system = new InequalitySystem();
			system.addInequality(0, ">=", 0, 42);

			List<Integer> solution = system.findSolution();
			assertThat(solution, hasSize(2));
			int y = solution.get(1);
			assertThat(y, lessThanOrEqualTo(0));
		}

		@Test
		public void testSimpleSystem8() {
			InequalitySystem system = new InequalitySystem();
			system.addInequality(2, ">", 1, 1);
			system.addInequality(1, "<=", 1, 1);
			system.addInequality(1, "<", 1, 0, 1);
			system.addInequality(1, "=", 0, 0, 1);

			List<Integer> solution = system.findSolution();
			assertThat(solution, hasSize(3));
			int x = solution.get(0), y = solution.get(1), z = solution.get(2);
			assertThat(x + y, is(1));
			assertThat(x + z, greaterThan(1));
			assertThat(z, is(1));
		}

		@Test
		public void testEmptySystem1() {
			InequalitySystem system = new InequalitySystem();
			system.addInequality(0, ">=");

			List<Integer> solution = system.findSolution();
			assertThat(solution, empty());
		}

		@Test
		public void testEmptySystem2() {
			InequalitySystem system = new InequalitySystem();

			List<Integer> solution = system.findSolution();
			assertThat(solution, empty());
		}

		@Test
		public void testLotsOfTrivialInequalities() {
			InequalitySystem system = new InequalitySystem();
			for (int i = 1; i <= 300; i++)
				system.addInequality(-1, ">=", i);

			List<Integer> solution = system.findSolution();
			assertThat(solution, hasSize(1));
			int x = solution.get(0);
			assertThat(x, lessThanOrEqualTo(-1));
		}

		@Test
		public void testSystemWithIncorrectSolution() {
			// The following system was created while synthesizing the word b(ab^20)^10b. The solution found
			// was x = (20, 0) which violates the first inequality: 0 > 1*x[1]. A correct solution is, for
			// example, (201, -10) or (21, -1)
			InequalitySystem system = new InequalitySystem();

			system.addInequality(0, ">", 0, 1);
			for (int i = 0; i < 200; i++)
				system.addInequality(0, ">", -1 - (i / 20), -i);
			system.addInequality(0, ">", -10, -200);

			List<Integer> solution = system.findSolution();
			assertThat(solution, hasSize(2));

			int x = solution.get(0), y = solution.get(1);
			assertThat(x, greaterThan(-20 * y));
			assertThat(y, lessThan(0));
			assertThat(system.fulfilledBy(solution), is(true));
		}

		@Test
		public void testAnyOf() {
			InequalitySystem[] required = new InequalitySystem[] { new InequalitySystem() };
			required[0].addInequality(42, "=", 1);

			InequalitySystem[] anyOf = new InequalitySystem[] {
				new InequalitySystem(), new InequalitySystem()
			};
			anyOf[0].addInequality(21, "=", 1);
			anyOf[1].addInequality(21, "=", 1, -1);

			List<Integer> solution = InequalitySystem.findSolution(required, anyOf);
			assertThat(solution, hasSize(2));

			int x = solution.get(0), y = solution.get(1);
			assertThat(x, equalTo(42));
			assertThat(y, equalTo(21));
			assertThat(required[0].fulfilledBy(solution), is(true));
			assertThat(anyOf[0].fulfilledBy(solution), is(false));
			assertThat(anyOf[1].fulfilledBy(solution), is(true));
		}

		@Test
		public void testAnyOfUnsat() {
			// x[0] is either 10 or 20
			InequalitySystem[] first = new InequalitySystem[] {
				new InequalitySystem(), new InequalitySystem()
			};
			first[0].addInequality(10, "=", 1);
			first[1].addInequality(20, "=", 1);

			// 0 = x[0] + x[1] or 0 = x[0] - x[1]
			InequalitySystem[] second = new InequalitySystem[] {
				new InequalitySystem(), new InequalitySystem()
			};
			second[0].addInequality(0, "=", 1, 1);
			second[1].addInequality(0, "=", 1, -1);

			// x[1] is either 1 or 2
			InequalitySystem[] third = new InequalitySystem[] {
				new InequalitySystem(), new InequalitySystem()
			};
			third[0].addInequality(1, "=", 0, 1);
			third[1].addInequality(2, "=", 0, 1);

			List<Integer> solution = InequalitySystem.findSolution(first, second, third);
			assertThat(solution, empty());
		}

		@Test
		public void testAnyOfEmpty() {
			InequalitySystem[] required = new InequalitySystem[] { new InequalitySystem() };
			required[0].addInequality(42, "=", 1);

			InequalitySystem[] empty = new InequalitySystem[0];

			List<Integer> solution = InequalitySystem.findSolution(empty, required, empty);
			assertThat(solution, hasSize(1));

			int x = solution.get(0);
			assertThat(x, equalTo(42));
		}
	}

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
		system.addInequality(0, "<=", Arrays.asList(0), "Just ensuring the trivial");
		system.addInequality(0, "<=", Arrays.asList(1), "and something useful");
		system.addInequality(0, ">=", 1);

		assertThat(system, hasToString("[\n0 <= 0\t(Just ensuring the trivial)\n0 <= 1*x[0]\t(and something useful)\n0 >= 1*x[0]\n]"));
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

	@Test
	public void testCopyConstructor() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(2, ">=", 1);
		system.addInequality(3, ">=", 0, 1);

		system = new InequalitySystem(system);

		assertThat(system, hasToString("[\n2 >= 1*x[0]\n3 >= 1*x[1]\n]"));
	}

	@DataProvider(name = "createHomogeneous")
	private Object[][] createHomogeneous() {
		BigInteger ZERO = BigInteger.ZERO;
		BigInteger P_ONE = BigInteger.ONE;
		BigInteger P_TEN = BigInteger.TEN;
		BigInteger M_ONE = P_ONE.negate();
		BigInteger M_TEN = P_TEN.negate();

		// Expected result, LHS, comparator, coefficients
		return new Object[][] {
			{ true,   ZERO, InequalitySystem.Comparator.LESS_THAN_OR_EQUAL, Collections.singletonList(P_ONE) },
			{ true,  P_ONE, InequalitySystem.Comparator.LESS_THAN_OR_EQUAL, Collections.singletonList(P_ONE) },
			{ true,  P_TEN, InequalitySystem.Comparator.LESS_THAN_OR_EQUAL, Collections.singletonList(P_ONE) },
			{ false, M_ONE, InequalitySystem.Comparator.LESS_THAN_OR_EQUAL, Collections.singletonList(P_ONE) },
			{ false, M_TEN, InequalitySystem.Comparator.LESS_THAN_OR_EQUAL, Collections.singletonList(P_ONE) },
			{ true,   ZERO, InequalitySystem.Comparator.LESS_THAN, Collections.singletonList(P_ONE) },
			{ true,  P_ONE, InequalitySystem.Comparator.LESS_THAN, Collections.singletonList(P_ONE) },
			{ true,  P_TEN, InequalitySystem.Comparator.LESS_THAN, Collections.singletonList(P_ONE) },
			{ true,  M_ONE, InequalitySystem.Comparator.LESS_THAN, Collections.singletonList(P_ONE) },
			{ false, M_TEN, InequalitySystem.Comparator.LESS_THAN, Collections.singletonList(P_ONE) },
			{ true,   ZERO, InequalitySystem.Comparator.EQUAL, Collections.singletonList(P_ONE) },
			{ false, P_ONE, InequalitySystem.Comparator.EQUAL, Collections.singletonList(P_ONE) },
			{ false, P_TEN, InequalitySystem.Comparator.EQUAL, Collections.singletonList(P_ONE) },
			{ false, M_ONE, InequalitySystem.Comparator.EQUAL, Collections.singletonList(P_ONE) },
			{ false, M_TEN, InequalitySystem.Comparator.EQUAL, Collections.singletonList(P_ONE) },
			{ true,   ZERO, InequalitySystem.Comparator.GREATER_THAN, Collections.singletonList(P_ONE) },
			{ true,  P_ONE, InequalitySystem.Comparator.GREATER_THAN, Collections.singletonList(P_ONE) },
			{ false, P_TEN, InequalitySystem.Comparator.GREATER_THAN, Collections.singletonList(P_ONE) },
			{ true,  M_ONE, InequalitySystem.Comparator.GREATER_THAN, Collections.singletonList(P_ONE) },
			{ true,  M_TEN, InequalitySystem.Comparator.GREATER_THAN, Collections.singletonList(P_ONE) },
			{ true,   ZERO, InequalitySystem.Comparator.GREATER_THAN_OR_EQUAL, Collections.singletonList(P_ONE) },
			{ false, P_ONE, InequalitySystem.Comparator.GREATER_THAN_OR_EQUAL, Collections.singletonList(P_ONE) },
			{ false, P_TEN, InequalitySystem.Comparator.GREATER_THAN_OR_EQUAL, Collections.singletonList(P_ONE) },
			{ true,  M_ONE, InequalitySystem.Comparator.GREATER_THAN_OR_EQUAL, Collections.singletonList(P_ONE) },
			{ true,  M_TEN, InequalitySystem.Comparator.GREATER_THAN_OR_EQUAL, Collections.singletonList(P_ONE) },
		};
	}

	@Test(dataProvider = "createHomogeneous")
	public void testHomogeneous(boolean expected, BigInteger lhs, InequalitySystem.Comparator comparator,
			List<BigInteger> coefficients) {
		InequalitySystem.Inequality inequality = new InequalitySystem.Inequality(lhs, comparator, coefficients);
		assertThat(inequality.isHomogeneous(), is(expected));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
