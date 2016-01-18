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
import java.util.List;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/** @author Uli Schlachter */
public class InequalitySystemSolverTest {
	static private BigInteger bi(long num) {
		return BigInteger.valueOf(num);
	}

	@Test
	public void testSimpleSystem0() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(0, ">=", 1, 0, 1);
		system.addInequality(0, ">=", -1, 0, -1);
		system.addInequality(0, ">=", 0, 1, 1);
		system.addInequality(0, ">=", 0, -1, -1);

		List<BigInteger> solution = new InequalitySystemSolver().assertDisjunction(system).findSolution();
		assertThat(solution, hasSize(3));
		BigInteger x = solution.get(0), y = solution.get(1), z = solution.get(2);
		assertThat(x.add(z), is(bi(0)));
		assertThat(y.add(z), is(bi(0)));
	}

	@Test
	public void testSimpleSystem1() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(0, ">=", 2, 1, 3);
		system.addInequality(0, ">=", 1, 1, 2);
		system.addInequality(0, ">=", 1, 2, 3);

		List<BigInteger> solution = new InequalitySystemSolver().assertDisjunction(system).findSolution();
		assertThat(solution, hasSize(3));
		BigInteger x = solution.get(0), y = solution.get(1), z = solution.get(2);
		assertThat(bi(2).multiply(x).add(bi(1).multiply(y)).add(bi(3).multiply(z)), lessThanOrEqualTo(bi(0)));
		assertThat(bi(1).multiply(x).add(bi(1).multiply(y)).add(bi(2).multiply(z)), lessThanOrEqualTo(bi(0)));
		assertThat(bi(1).multiply(x).add(bi(2).multiply(y)).add(bi(3).multiply(z)), lessThanOrEqualTo(bi(0)));
	}

	@Test
	public void testSimpleSystem2() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(1, ">=", 2, 1, 3);
		system.addInequality(2, ">=", 1, 1, 2);
		system.addInequality(3, ">=", 1, 2, 3);
		system.addInequality(4, ">=", 3, 3, 6);

		List<BigInteger> solution = new InequalitySystemSolver().assertDisjunction(system).findSolution();
		assertThat(solution, hasSize(3));
		BigInteger x = solution.get(0), y = solution.get(1), z = solution.get(2);
		assertThat(bi(2).multiply(x).add(bi(1).multiply(y)).add(bi(3).multiply(z)), lessThanOrEqualTo(bi(1)));
		assertThat(bi(1).multiply(x).add(bi(1).multiply(y)).add(bi(2).multiply(z)), lessThanOrEqualTo(bi(2)));
		assertThat(bi(1).multiply(x).add(bi(2).multiply(y)).add(bi(3).multiply(z)), lessThanOrEqualTo(bi(3)));
		assertThat(bi(3).multiply(x).add(bi(3).multiply(y)).add(bi(6).multiply(z)), lessThanOrEqualTo(bi(4)));
	}

	@Test
	public void testSimpleSystem3() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(0, ">=", 1, 2);
		system.addInequality(0, ">=", 0, 1, 1);
		system.addInequality(0, ">=", 1, 0, 1);

		List<BigInteger> solution = new InequalitySystemSolver().assertDisjunction(system).findSolution();
		assertThat(solution, hasSize(3));
		BigInteger x = solution.get(0), y = solution.get(1), z = solution.get(2);
		assertThat(bi(1).multiply(x).add(bi(2).multiply(y)).add(bi(0).multiply(z)), lessThanOrEqualTo(bi(0)));
		assertThat(bi(0).multiply(x).add(bi(1).multiply(y)).add(bi(1).multiply(z)), lessThanOrEqualTo(bi(0)));
		assertThat(bi(1).multiply(x).add(bi(0).multiply(y)).add(bi(1).multiply(z)), lessThanOrEqualTo(bi(0)));
	}

	@Test
	public void testSimpleSystem4() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(10, ">=", 4, 2, 6);
		system.addInequality(10, ">=", 2, 2, 4);
		system.addInequality(10, ">=", 2, 4, 6);

		List<BigInteger> solution = new InequalitySystemSolver().assertDisjunction(system).findSolution();
		assertThat(solution, hasSize(3));
		BigInteger x = solution.get(0), y = solution.get(1), z = solution.get(2);
		assertThat(bi(4).multiply(x).add(bi(2).multiply(y)).add(bi(6).multiply(z)), lessThanOrEqualTo(bi(10)));
		assertThat(bi(2).multiply(x).add(bi(2).multiply(y)).add(bi(4).multiply(z)), lessThanOrEqualTo(bi(10)));
		assertThat(bi(2).multiply(x).add(bi(4).multiply(y)).add(bi(6).multiply(z)), lessThanOrEqualTo(bi(10)));
	}

	@Test
	public void testSimpleSystem5() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(0, ">=", 4, 2, 5);
		system.addInequality(0, ">=", 2, 2, 4);

		List<BigInteger> solution = new InequalitySystemSolver().assertDisjunction(system).findSolution();
		assertThat(solution, hasSize(3));
		BigInteger x = solution.get(0), y = solution.get(1), z = solution.get(2);
		assertThat(bi(4).multiply(x).add(bi(2).multiply(y)).add(bi(5).multiply(z)), lessThanOrEqualTo(bi(0)));
		assertThat(bi(2).multiply(x).add(bi(2).multiply(y)).add(bi(4).multiply(z)), lessThanOrEqualTo(bi(0)));
	}

	@Test
	public void testSimpleSystem6() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(0, ">=", 0, 0);

		List<BigInteger> solution = new InequalitySystemSolver().assertDisjunction(system).findSolution();
		assertThat(solution, hasSize(2));
	}

	@Test
	public void testSimpleSystem7() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(0, ">=", 0, 42);

		List<BigInteger> solution = new InequalitySystemSolver().assertDisjunction(system).findSolution();
		assertThat(solution, hasSize(2));
		BigInteger y = solution.get(1);
		assertThat(y, lessThanOrEqualTo(bi(0)));
	}

	@Test
	public void testSimpleSystem8() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(2, ">", 1, 1);
		system.addInequality(1, "<=", 1, 1);
		system.addInequality(1, "<", 1, 0, 1);
		system.addInequality(1, "=", 0, 0, 1);

		List<BigInteger> solution = new InequalitySystemSolver().assertDisjunction(system).findSolution();
		assertThat(solution, hasSize(3));
		BigInteger x = solution.get(0), y = solution.get(1), z = solution.get(2);
		assertThat(x.add(y), is(bi(1)));
		assertThat(x.add(z), greaterThan(bi(1)));
		assertThat(z, is(bi(1)));
	}

	@Test
	public void testEmptySystem1() {
		InequalitySystem system = new InequalitySystem();
		system.addInequality(0, ">=");

		List<BigInteger> solution = new InequalitySystemSolver().assertDisjunction(system).findSolution();
		assertThat(solution, empty());
	}

	@Test
	public void testEmptySystem2() {
		InequalitySystem system = new InequalitySystem();

		List<BigInteger> solution = new InequalitySystemSolver().assertDisjunction(system).findSolution();
		assertThat(solution, empty());
	}

	@Test
	public void testLotsOfTrivialInequalities() {
		InequalitySystem system = new InequalitySystem();
		for (int i = 1; i <= 300; i++)
			system.addInequality(-1, ">=", i);

		List<BigInteger> solution = new InequalitySystemSolver().assertDisjunction(system).findSolution();
		assertThat(solution, hasSize(1));
		BigInteger x = solution.get(0);
		assertThat(x, lessThanOrEqualTo(bi(-1)));
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

		List<BigInteger> solution = new InequalitySystemSolver().assertDisjunction(system).findSolution();
		assertThat(solution, hasSize(2));

		BigInteger x = solution.get(0), y = solution.get(1);
		assertThat(x, greaterThan(bi(-20).multiply(y)));
		assertThat(y, lessThan(bi(0)));
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

		List<BigInteger> solution = new InequalitySystemSolver()
			.assertDisjunction(required)
			.assertDisjunction(anyOf)
			.findSolution();
		assertThat(solution, hasSize(2));

		BigInteger x = solution.get(0), y = solution.get(1);
		assertThat(x, equalTo(bi(42)));
		assertThat(y, equalTo(bi(21)));
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

		List<BigInteger> solution = new InequalitySystemSolver()
			.assertDisjunction(first)
			.assertDisjunction(second)
			.assertDisjunction(third)
			.findSolution();
		assertThat(solution, empty());
	}

	@Test
	public void testAnyOfEmpty() {
		InequalitySystem[] required = new InequalitySystem[] { new InequalitySystem() };
		required[0].addInequality(42, "=", 1);

		InequalitySystem[] empty = new InequalitySystem[0];

		List<BigInteger> solution = new InequalitySystemSolver()
			.assertDisjunction(empty)
			.assertDisjunction(required)
			.assertDisjunction(empty)
			.findSolution();
		assertThat(solution, hasSize(1));

		BigInteger x = solution.get(0);
		assertThat(x, equalTo(bi(42)));
	}

	@Test
	public void testAnyOfEmpty2() {
		InequalitySystem[] required = new InequalitySystem[] { new InequalitySystem(), new InequalitySystem() };
		required[0].addInequality(42, "=", 1);

		InequalitySystem[] empty = new InequalitySystem[0];

		List<BigInteger> solution = new InequalitySystemSolver()
			.assertDisjunction(empty)
			.assertDisjunction(required)
			.assertDisjunction(empty)
			.findSolution();
		assertThat(solution, hasSize(1));

		BigInteger x = solution.get(0);
		assertThat(x, equalTo(bi(42)));
	}

	@Test
	public void testPushPop() {
		InequalitySystemSolver solver = new InequalitySystemSolver();

		// x[0] is 42
		InequalitySystem system = new InequalitySystem();
		system.addInequality(42, "=", 1);
		solver.assertDisjunction(system);
		assertThat(solver.findSolution(), contains(bi(42)));

		solver.push();

		// x[0] == -x[1]
		system = new InequalitySystem();
		system.addInequality(0, "=", 1, 1);
		solver.assertDisjunction(system);
		assertThat(solver.findSolution(), contains(bi(42), bi(-42)));

		solver.pop();

		// x[0] == 2*x[1]
		system = new InequalitySystem();
		system.addInequality(0, "=", 2, -1);
		solver.assertDisjunction(system);
		assertThat(solver.findSolution(), contains(bi(42), bi(84)));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
