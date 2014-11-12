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
	private final List<Inequality> inequalities = new ArrayList<>();

	private static void debug(String message) {
		//System.err.println("InequalitySystem: " + message);
	}

	private static void debug() {
		debug("");
	}

	private static void debug(Object obj) {
		debug(obj.toString());
	}

	private static int[] toIntArray(Collection<Integer> collection) {
		int result[] = new int[collection.size()];
		int i = 0;
		for (int value : collection)
			result[i++] = value;

		return result;
	}

	/**
	 * An enumeration of comparators on numbers.
	 */
	public static enum Comparator {
		LESS_THAN_OR_EQUAL("<=", ">="),
		LESS_THAN("<", ">"),
		EQUAL("=", "="),
		GREATER_THAN(">", "<"),
		GREATER_THAN_OR_EQUAL(">=", "<=");

		private final String representation;
		private final String opposite;

		private Comparator(String representation, String opposite) {
			this.representation = representation;
			this.opposite = opposite;
		}

		/**
		 * Return the opposite comparator
		 * @return The opposite.
		 */
		public Comparator getOpposite() {
			return fromString(opposite);
		}

		@Override
		public String toString() {
			return representation;
		}

		/**
		 * Return the comparator which is described by the given string. Must be one of "<=", "<", "=", ">",
		 * ">=".
		 * @return The matching comparator.
		 */
		static public Comparator fromString(String str) {
			for (Comparator comp : values())
				if (comp.toString() == str)
					return comp;
			throw new AssertionError("Unknown Comparator '" + str + "'");
		}
	}

	/**
	 * Instances of this class represent a linear inequality in a number of unknowns.
	 */
	public static class Inequality {
		private final int leftHandSide;
		private final Comparator comparator;
		private final int[] coefficients;

		// TODO: Document
		public Inequality(int leftHandSide, Comparator comparator, int[] coefficients) {
			this.leftHandSide = leftHandSide;
			this.comparator = comparator;
			this.coefficients = Arrays.copyOf(coefficients, coefficients.length);
		}

		// TODO: Document
		public int getLeftHandSide() {
			return leftHandSide;
		}

		// TODO: Document
		public Comparator getComparator() {
			return comparator;
		}

		// TODO: Document
		public int[] getCoefficients() {
			// TODO: This is ugly
			return Arrays.copyOf(coefficients, coefficients.length);
		}

		@Override
		public String toString() {
			StringWriter buffer = new StringWriter();
			buffer.write("" + leftHandSide);
			buffer.write(" " + comparator.toString() + " ");
			boolean first = true;
			for (int j = 0; j < coefficients.length; j++) {
				if (coefficients[j] == 0)
					continue;

				if (!first)
					buffer.write(" + ");
				buffer.write("" + coefficients[j]);
				buffer.write("*x[");
				buffer.write("" + j);
				buffer.write("]");
				first = false;
			}
			if (first)
				buffer.write("0");
			return buffer.toString();
		}
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
	 * Add an inequality of the form <pre>lhs [comparator] sum(x[i] * coefficients[i])</pre> to the inequality
	 * system.
	 * @param inequality The inequality to add.
	 */
	public void addInequality(Inequality inequality) {
		assert inequality.getCoefficients().length == numVariables;
		inequalities.add(inequality);
	}

	/**
	 * Add an inequality of the form <pre>lhs [comparator] sum(x[i] * coefficients[i])</pre> to the inequality
	 * system.
	 * @param lhs The left hand side of the inequality.
	 * @param comparator Comparator for the inequality,
	 * @param coefficients List of coefficients for the inequality. This must have exactly one entry for each
	 * variable in the inequality system.
	 */
	public void addInequality(int lhs, Comparator comparator, int... coefficients) {
		addInequality(new Inequality(lhs, comparator, coefficients));
	}

	/**
	 * Add an inequality of the form <pre>lhs [comparator] sum(x[i] * coefficients[i])</pre> to the inequality
	 * system.
	 * @param lhs The left hand side of the inequality.
	 * @param comparator Comparator for the inequality,
	 * @param coefficients List of coefficients for the inequality. This must have exactly one entry for each
	 * variable in the inequality system.
	 */
	public void addInequality(int lhs, String comparator, int... coefficients) {
		addInequality(lhs, Comparator.fromString(comparator), coefficients);
	}

	/**
	 * Add an inequality of the form <pre>lhs [comparator] sum(x[i] * coefficients[i])</pre> to the inequality
	 * system.
	 * @param lhs The left hand side of the inequality.
	 * @param comparator Comparator for the inequality,
	 * @param coefficients List of coefficients for the inequality. This must have exactly one entry for each
	 * variable in the inequality system.
	 */
	public void addInequality(int lhs, Comparator comparator, Collection<Integer> coefficients) {
		addInequality(lhs, comparator, toIntArray(coefficients));
	}

	/**
	 * Add an inequality of the form <pre>lhs [comparator] sum(x[i] * coefficients[i])</pre> to the inequality
	 * system.
	 * @param lhs The left hand side of the inequality.
	 * @param comparator Comparator for the inequality,
	 * @param coefficients List of coefficients for the inequality. This must have exactly one entry for each
	 * variable in the inequality system.
	 */
	public void addInequality(int lhs, String comparator, Collection<Integer> coefficients) {
		addInequality(lhs, Comparator.fromString(comparator), toIntArray(coefficients));
	}

	/**
	 * Calculate a solution of the inequality system.
	 * @return A solution to the system or an empty list
	 */
	public List<Integer> findSolution() {
		Solver solver = new Solver();
		IntVar[] vars = VariableFactory.integerArray("x", numVariables,
				VariableFactory.MIN_INT_BOUND, VariableFactory.MAX_INT_BOUND, solver);
		for (Inequality inequality : inequalities) {
			IntVar lhsVar = VariableFactory.fixed(inequality.getLeftHandSide(), solver);
			String comparator = inequality.getComparator().getOpposite().toString();
			int[] coefficients = inequality.getCoefficients();
			solver.post(IntConstraintFactory.scalar(vars, coefficients, comparator, lhsVar));
		}

		if (!solver.findSolution()) {
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
		for (Inequality inequality : inequalities) {
			buffer.write(inequality.toString());
			buffer.write("\n");
		}
		buffer.write("]");
		return buffer.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
