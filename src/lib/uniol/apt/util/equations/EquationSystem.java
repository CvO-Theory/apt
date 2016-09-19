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

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uniol.apt.util.interrupt.InterrupterRegistry;

import static uniol.apt.util.DebugUtil.debug;
import static uniol.apt.util.DebugUtil.debugFormat;

/**
 * Representation of an equation system.
 * @author Uli Schlachter
 */
public class EquationSystem {
	private final int numVariables;
	private final Collection<List<BigInteger>> equations = new HashSet<>();

	private static class EquationSystemSolver {
		// Algorithm 4 from "Petri Net Synthesis" by Badouel, Bernardinello and Darondeau, page 190

		// The invariant of the algorithm is:
		// There exists some y so that x = equations1 * y and equations2 * y = 0.
		// So each equation in the equations has variables y_0 to y_{n-1}
		private final List<List<BigInteger>> equations1 = new ArrayList<>();
		private final List<List<BigInteger>> equations2 = new ArrayList<>();

		private final int numVariables;

		EquationSystemSolver(int numVariables, Collection<List<BigInteger>> equations) {
			this.numVariables = numVariables;

			// Input equations, but x_i is substituted with y_i
			equations2.addAll(equations);

			for (int i = 0; i < numVariables; i++) {
				// Set x_i = y_i in equations1
				List<BigInteger> equation = new ArrayList<>(numVariables);
				for (int j = 0; j < numVariables; j++) {
					equation.add(j == i ? BigInteger.ONE : BigInteger.ZERO);
				}
				equations1.add(equation);
			}
		}

		public List<List<BigInteger>> solve() {
			debug("solve called");
			debug("============");

			while (!equations2.isEmpty()) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

				debug("New round");
				debug(equations1);
				debug(equations2);
				debug();

				// Get an equation and ensure it only has non-negative coefficients
				List<BigInteger> equation = equations2.get(0);
				for (int i = 0; i < numVariables; i++) {
					if (equation.get(i).compareTo(BigInteger.ZERO) < 0)
						invertVariableY(i);
				}

				// "Reduce" the equation to a single, non-zero coefficient
				// TODO: Baaaaad performance
				while (true) {
					InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
					boolean restart = false;

					// Find two coefficients with 0 < lambda_i <= lambda_j
					for (int i = 0; i < numVariables; i++) {
						BigInteger lambdai = equation.get(i);
						if (lambdai.equals(BigInteger.ZERO))
							continue;
						for (int j = i + 1; j < numVariables; j++) {
							BigInteger lambdaj = equation.get(j);
							if (lambdaj.equals(BigInteger.ZERO))
								continue;

							// Swap the two equations if necessary
							if (lambdaj.compareTo(lambdai) < 0) {
								int tmp1 = i;
								i = j;
								j = tmp1;

								BigInteger tmp2 = lambdai;
								lambdai = lambdaj;
								lambdaj = tmp2;
							}

							// Substitute y_j -> y_j - floor(lambda_j / lambda_i) * y_i
							BigDecimal a = new BigDecimal(lambdaj);
							BigDecimal b = new BigDecimal(lambdai);
							BigDecimal result = a.divide(b, RoundingMode.FLOOR);
							substituteVariable(j, result.toBigIntegerExact().negate(), i);
							restart = true;
							break;
						}
						if (restart)
							break;
					}

					// We didn't find two non-zero coefficients, so at most a single one is left
					if (!restart)
						break;
				}
				debug(equations1);
				debug(equations2);
				debug();

				removeRedundancy();
			}

			debug("Result:");
			debug(equations1);

			return Collections.unmodifiableList(equations1);
		}

		/**
		 * Simplify equations2 by removing simple forms of redundancy.
		 * This handles equations of the form k*y_i = 0 and 0 = 0.
		 */
		private void removeRedundancy() {
			// For all equations k*y_i = 0 with k!=0 in equations2, remove this equation and remove
			// variable y_i (since it must be zero)
			for (int i = 0; i < equations2.size(); i++) {
				Integer nonZeroIndex = null;
				List<BigInteger> equation = equations2.get(i);

				for (int j = 0; j < numVariables; j++) {
					if (equation.get(j).equals(BigInteger.ZERO))
						continue;
					if (nonZeroIndex == null) {
						nonZeroIndex = j;
					} else {
						nonZeroIndex = null;
						break;
					}
				}

				if (nonZeroIndex != null) {
					equations2.remove(i--);
					removeVariable(nonZeroIndex);
				}
			}

			// Eliminate redundant equations or the trivial equation 0=0 from E2
			for (int i = 0; i < equations2.size(); i++) {
				List<BigInteger> equation = equations2.get(i);
				boolean found = false;

				for (int j = 0; j < numVariables; j++)
					if (!equation.get(j).equals(BigInteger.ZERO)) {
						found = true;
						break;
					}

				if (!found)
					equations2.remove(i--);
			}
		}

		// Invert variable y_j in all equations.
		private void invertVariableY(int j) {
			debugFormat("Inverting variable y_%d", j);
			for (int i = 0; i < equations1.size(); i++) {
				List<BigInteger> equation = equations1.get(i);
				equation.set(j, equation.get(j).negate());
			}
			for (int i = 0; i < equations2.size(); i++) {
				List<BigInteger> equation = equations2.get(i);
				equation.set(j, equation.get(j).negate());
			}
		}

		// Remove variable y_j from all equations.
		private void removeVariable(int j) {
			debugFormat("Removing variable y_%d", j);
			for (int i = 0; i < equations1.size(); i++)
				equations1.get(i).set(j, BigInteger.ZERO);
			for (int i = 0; i < equations2.size(); i++)
				equations2.get(i).set(j, BigInteger.ZERO);
		}

		// Substitute variable y_variableIndex with y_variableIndex + factor * y_addendIndex in all equations.
		private void substituteVariable(int variableIndex, BigInteger factor, int addendIndex) {
			debugFormat("Substituting variable y_%d with y_%d + %d * y_%d",
					variableIndex, variableIndex, factor, addendIndex);
			for (int i = 0; i < equations1.size(); i++)
				substituteVariable(equations1.get(i), variableIndex, factor, addendIndex);
			for (int i = 0; i < equations2.size(); i++)
				substituteVariable(equations2.get(i), variableIndex, factor, addendIndex);
		}

		// Substitute variable y_variableIndex with y_variableIndex + factor * y_addendIndex in the given
		// equation.
		private static void substituteVariable(List<BigInteger> equation, int variableIndex, BigInteger factor,
				int addendIndex) {
			BigInteger addend = equation.get(addendIndex);
			BigInteger variableValue = equation.get(variableIndex);
			// newValue = variableValue + factor * addend
			BigInteger newValue = variableValue.add(factor.multiply(addend));
			equation.set(variableIndex, newValue);
		}
	}

	/**
	 * Construct a new equation system.
	 * @param numVariables The number of variables in the equation system.
	 */
	public EquationSystem(int numVariables) {
		assert numVariables >= 0;

		this.numVariables = numVariables;
	}

	/**
	 * Add an equation to the equation system.
	 * @param coefficients List of coefficients for the equation. This must have exactly one entry for each variable
	 * in the equation system.
	 */
	public void addEquation(int... coefficients) {
		assert coefficients.length == numVariables;
		ArrayList<BigInteger> row = new ArrayList<>(numVariables);
		for (int i = 0; i < coefficients.length; i++)
			row.add(BigInteger.valueOf(coefficients[i]));
		equations.add(row);
	}

	/**
	 * Add an equation to the equation system.
	 * @param coefficients List of coefficients for the equation. This must have exactly one entry for each variable
	 * in the equation system.
	 */
	public void addEquation(Collection<BigInteger> coefficients) {
		assert coefficients.size() == numVariables;
		ArrayList<BigInteger> row = new ArrayList<>(coefficients);
		equations.add(row);
	}

	/**
	 * Calculate a basis of the equation system.
	 * @return The set of basis vectors
	 */
	public Set<List<BigInteger>> findBasis() {
		Set<List<BigInteger>> result = new HashSet<>();
		EquationSystemSolver solver = new EquationSystemSolver(numVariables, equations);

		// The *columns* of the matrix provide a basis of the solution
		List<List<BigInteger>> solution = solver.solve();
		assert solution.size() == numVariables;

		for (int i = 0; i < numVariables; i++) {
			boolean allZero = true;
			List<BigInteger> row = new ArrayList<>(numVariables);
			for (int j = 0; j < numVariables; j++) {
				BigInteger value = solution.get(j).get(i);
				if (!value.equals(BigInteger.ZERO))
					allZero = false;
				row.add(value);
			}

			if (!allZero)
				result.add(Collections.unmodifiableList(row));
		}

		debug("Basis found: ", result);
		debug("");

		return Collections.unmodifiableSet(result);
	}

	@Override
	public String toString() {
		StringWriter buffer = new StringWriter();
		buffer.write("[\n");
		for (List<BigInteger> equation : equations) {
			boolean first = true;
			for (int j = 0; j < numVariables; j++) {
				if (equation.get(j).equals(BigInteger.ZERO))
					continue;

				if (!first)
					buffer.write(" + ");

				buffer.write("" + equation.get(j));
				buffer.write("*x[");
				buffer.write("" + j);
				buffer.write("]");
				first = false;
			}
			if (first)
				buffer.write("0");
			buffer.write(" = 0\n");
		}
		buffer.write("]");
		return buffer.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
