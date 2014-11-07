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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representation of an equation system.
 * @author Uli Schlachter
 */
public class EquationSystem {
	private final int numVariables;
	private final List<List<Integer>> equations = new ArrayList<>();

	private static void debug(String message) {
		//System.err.println("EquationSystem: " + message);
	}

	private static void debug() {
		debug("");
	}

	private static void debug(Object obj) {
		debug(obj.toString());
	}

	private static class EquationSystemSolver {
		// Algorithm 4 from "Petri Net Synthesis" by Badouel, Bernardinello and Darondeau, page 190

		// The invariant of the algorithm is:
		// There exists some y so that x = equations1 * y and equations2 * y = 0.
		// So each equation in the equations has variables y_0 to y_{n-1}
		private final List<List<Integer>> equations1 = new ArrayList<>();
		private final List<List<Integer>> equations2 = new ArrayList<>();

		private final int numVariables;

		EquationSystemSolver(int numVariables, List<List<Integer>> equations) {
			this.numVariables = numVariables;

			// Input equations, but x_i is substituted with y_i
			equations2.addAll(equations);

			for (int i = 0; i < numVariables; i++) {
				// Set x_i = y_i in equations1
				List<Integer> equation = new ArrayList<>(numVariables);
				for (int j = 0; j < numVariables; j++) {
					equation.add(j == i ? 1 : 0);
				}
				equations1.add(equation);
			}
		}

		public List<List<Integer>> solve() {
			debug("solve called");
			debug("============");

			while (!equations2.isEmpty()) {
				debug("New round");
				debug(equations1);
				debug(equations2);
				debug();

				// Get an equation and ensure it only has non-negative coefficients
				List<Integer> equation = equations2.get(0);
				for (int i = 0; i < numVariables; i++) {
					if (equation.get(i) < 0)
						invertVariableY(i);
				}

				// "Reduce" the equation to a single, non-zero coefficient
				// TODO: Baaaaad performance
				while (true) {
					boolean restart = false;

					// Find two coefficients with 0 < lambda_i <= lambda_j
					for (int i = 0; i < numVariables; i++) {
						int lambdai = equation.get(i);
						if (lambdai == 0)
							continue;
						for (int j = i + 1; j < numVariables; j++) {
							int lambdaj = equation.get(j);
							if (lambdaj == 0)
								continue;

							// Swap the two equations if necessary
							if (lambdaj < lambdai) {
								int tmp = i;
								i = j;
								j = tmp;

								tmp = lambdai;
								lambdai = lambdaj;
								lambdaj = tmp;
							}

							// Substitute y_j -> y_j - floor(lambda_j / lambda_i) * y_i
							substituteVariable(j, (int) -Math.floor(lambdaj * 1.0 / lambdai), i);
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
				List<Integer> equation = equations2.get(i);

				for (int j = 0; j < numVariables; j++) {
					if (equation.get(j) == 0)
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
			// TODO: What exactly are redundant equations? For now we can just ignore them and let solve()
			// handle them
			for (int i = 0; i < equations2.size(); i++) {
				List<Integer> equation = equations2.get(i);
				boolean found = false;

				for (int j = 0; j < numVariables; j++)
					if (equation.get(j) != 0) {
						found = true;
						break;
					}

				if (!found)
					equations2.remove(i--);
			}
		}

		/**
		 * Invert variable y_j in all equations.
		 */
		private void invertVariableY(int j) {
			debug("Inverting variable y_" + j);
			for (int i = 0; i < equations1.size(); i++) {
				List<Integer> equation = equations1.get(i);
				equation.set(j, -equation.get(j));
			}
			for (int i = 0; i < equations2.size(); i++) {
				List<Integer> equation = equations2.get(i);
				equation.set(j, -equation.get(j));
			}
		}

		/**
		 * Remove variable y_j from all equations.
		 */
		private void removeVariable(int j) {
			debug("Removing variable y_" + j);
			for (int i = 0; i < equations1.size(); i++)
				equations1.get(i).set(j, 0);
			for (int i = 0; i < equations2.size(); i++)
				equations2.get(i).set(j, 0);
		}

		/**
		 * Substitute variable y_variableIndex with y_variableIndex + factor * y_addendIndex in all equations.
		 */
		private void substituteVariable(int variableIndex, int factor, int addendIndex) {
			debug("Substituting variable y_" + variableIndex + " with y_" + variableIndex + " + " + factor + " * y_" + addendIndex);
			for (int i = 0; i < equations1.size(); i++)
				substituteVariable(equations1.get(i), variableIndex, factor, addendIndex);
			for (int i = 0; i < equations2.size(); i++)
				substituteVariable(equations2.get(i), variableIndex, factor, addendIndex);
		}

		/**
		 * Substitute variable y_variableIndex with y_variableIndex + factor * y_addendIndex in the given
		 * equation.
		 */
		private static void substituteVariable(List<Integer> equation, int variableIndex, int factor, int addendIndex) {
			int addend = equation.get(addendIndex);
			int variableValue = equation.get(variableIndex);
			equation.set(variableIndex, variableValue + factor * addend);
		}
	}

	/**
	 * Construct a new equation system.
	 * @param numVariables The number of variables in the equation system.
	 */
	public EquationSystem(int numVariables) {
		assert numVariables > 0;

		this.numVariables = numVariables;
	}

	/**
	 * Add an equation to the equation system.
	 * @param coefficients List of coefficients for the equation. This must have exactly one entry for each variable
	 * in the equation system.
	 */
	public void addEquation(int... coefficients) {
		assert coefficients.length == numVariables;
		ArrayList<Integer> row = new ArrayList<>(numVariables);
		for (int i = 0; i < coefficients.length; i++)
			row.add(coefficients[i]);
		equations.add(row);
	}

	/**
	 * Calculate a basis of the equation system.
	 * @return The set of basis vectors
	 */
	public Set<List<Integer>> findBasis() {
		Set<List<Integer>> result = new HashSet<>();
		EquationSystemSolver solver = new EquationSystemSolver(numVariables, equations);

		// The *columns* of the matrix provide a basis of the solution
		List<List<Integer>> solution = solver.solve();
		assert solution.size() == numVariables;

		for (int i = 0; i < numVariables; i++) {
			boolean allZero = true;
			List<Integer> row = new ArrayList<>(numVariables);
			for (int j = 0; j < numVariables; j++) {
				int value = solution.get(j).get(i);
				if (value != 0)
					allZero = false;
				row.add(value);
			}

			if (!allZero)
				result.add(Collections.unmodifiableList(row));
		}

		debug("Basis found: " + result);
		debug("");

		return Collections.unmodifiableSet(result);
	}

	public String toString() {
		StringWriter buffer = new StringWriter();
		buffer.append("[\n");
		for (List<Integer> row : equations) {
			buffer.write(row.toString());
			buffer.write("\n");
		}
		buffer.append("]");
		return buffer.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
