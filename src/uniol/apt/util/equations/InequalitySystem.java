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
import java.util.Collection;
import java.util.Iterator;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * Representation of an inequality system.
 * @author Uli Schlachter
 */
public class InequalitySystem {
	private final int numVariables;
	private final List<int[]> rows = new ArrayList<>();
	private final List<Integer> rightHandSides = new ArrayList<>();

	private static void debug(String message) {
		//System.err.println("InequalitySystem: " + message);
	}

	private static void debug() {
		debug("");
	}

	private static void debug(Object obj) {
		debug(obj.toString());
	}

	/**
	 * Construct a new inequality system.
	 * @param numVariables The number of variables in the inequality system.
	 */
	public InequalitySystem(int numVariables) {
		assert numVariables >= 0;

		this.numVariables = numVariables;
	}

	/**
	 * Add an inequality of the form <pre>sum(x[i] * coefficients[i]) &lt;= rhs</pre> to the inequality system.
	 * @param coefficients List of coefficients for the inequality. This must have exactly one entry for each variable
	 * in the inequality system.
	 * @param rhs The right hand side of the inequality.
	 */
	public void addInequality(int rhs, int... coefficients) {
		assert coefficients.length == numVariables;
		int row[] = Arrays.copyOf(coefficients, numVariables);
		rows.add(row);
		rightHandSides.add(rhs);
	}

	/**
	 * Add an inequality of the form <pre>sum(x[i] * coefficients[i]) &lt;= rhs</pre> to the inequality system.
	 * @param coefficients List of coefficients for the inequality. This must have exactly one entry for each variable
	 * in the inequality system.
	 * @param rhs The right hand side of the inequality.
	 */
	public void addInequality(int rhs, Collection<Integer> coefficients) {
		assert coefficients.size() == numVariables;
		int row[] = new int[numVariables];
		Iterator<Integer> it = coefficients.iterator();

		for (int i = 0; i < numVariables; i++)
			row[i] = it.next();
		assert !it.hasNext();

		rows.add(row);
		rightHandSides.add(rhs);
	}

	/**
	 * Calculate a solution of the inequality system.
	 */
	public List<Integer> findSolution() {
		assert rows.size() == rightHandSides.size();

		Solver solver = new Solver();
		IntVar[] vars = VariableFactory.integerArray("x", numVariables, VariableFactory.MIN_INT_BOUND, VariableFactory.MAX_INT_BOUND, solver);
		for (int i = 0; i < rows.size(); i++) {
			IntVar rhsVar = VariableFactory.fixed(rightHandSides.get(i), solver);
			solver.post(IntConstraintFactory.scalar(vars, rows.get(i), "<=", rhsVar));
		}

		if (!solver.findSolution())
		{
			debug("No solution found for:");
			debug(this);
			return Collections.emptyList();
		}

		List<Integer> solution = new ArrayList<>();
		for (int i = 0; i < numVariables; i++)
			solution.add(vars[i].getValue());

		debug("Solution:");
		debug(solution);

		return Collections.unmodifiableList(solution);
	}

	@Override
	public String toString() {
		StringWriter buffer = new StringWriter();
		buffer.write("[\n");
		for (int i = 0; i < rows.size(); i++) {
			int[] row = rows.get(i);
			boolean first = true;
			for (int j = 0; j < numVariables; j++) {
				if (row[j] == 0)
					continue;

				if (!first)
					buffer.write(" + ");
				buffer.write("" + row[j]);
				buffer.write("*x[");
				buffer.write("" + j);
				buffer.write("]");
				first = false;
			}
			if (first)
				buffer.write("0");
			buffer.write(" <= ");
			buffer.write("" + rightHandSides.get(i));
			buffer.write("\n");
		}
		buffer.write("]");
		return buffer.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
