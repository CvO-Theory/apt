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
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/**
 * Representation of an inequality system.
 * @author Uli Schlachter
 */
public class InequalitySystem {
	private final List<Inequality> inequalities = new ArrayList<>();
	private Implementation implementation;

	private static void debug(String message) {
		//System.err.println("InequalitySystem: " + message);
	}

	private static void debug() {
		debug("");
	}

	private static void debug(Object obj) {
		debug(obj.toString());
	}

	private static List<BigInteger> toBigIntegerList(Collection<Integer> collection) {
		List<BigInteger> result = new ArrayList<>(collection.size());
		for (int value : collection)
			result.add(BigInteger.valueOf(value));

		return result;
	}

	private static List<BigInteger> toBigIntegerList(int... array) {
		List<BigInteger> result = new ArrayList<>(array.length);
		for (int value : array)
			result.add(BigInteger.valueOf(value));

		return result;
	}

	public static enum Implementation {
		CHOCO, OJALGO
	}

	final static public Implementation DEFAULT_IMPLEMENTATION = Implementation.OJALGO;

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
		 * @param str String representation of comparator.
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
		private final BigInteger leftHandSide;
		private final Comparator comparator;
		private final List<BigInteger> coefficients;

		/**
		 * Construct a new linear inequality.
		 * @param leftHandSide The left hand side of the inequality.
		 * @param comparator The comparator used between both sides.
		 * @param coefficients The coefficients of the unknowns on the right hand side.
		 */
		public Inequality(BigInteger leftHandSide, Comparator comparator, List<BigInteger> coefficients) {
			this.leftHandSide = leftHandSide;
			this.comparator = comparator;
			this.coefficients = Collections.unmodifiableList(coefficients);
		}

		/**
		 * Get the left hand side of the inequality
		 * @return The left hand side.
		 */
		public BigInteger getLeftHandSide() {
			return leftHandSide;
		}

		/**
		 * Get the comparator of the inequality.
		 * @return The comparator.
		 */
		public Comparator getComparator() {
			return comparator;
		}

		/**
		 * Get the coefficients of the right hand side of the inequality.
		 * @return The coefficients.
		 */
		public List<BigInteger> getCoefficients() {
			return coefficients;
		}

		/**
		 * Get the number of coefficients in the inequality.
		 * @return The number of coefficients.
		 */
		public int getNumberOfCoefficients() {
			return coefficients.size();
		}

		@Override
		public String toString() {
			StringWriter buffer = new StringWriter();
			buffer.write("" + leftHandSide);
			buffer.write(" " + comparator.toString() + " ");
			boolean first = true;
			for (int j = 0; j < coefficients.size(); j++) {
				BigInteger c = coefficients.get(j);
				if (c.equals(BigInteger.ZERO))
					continue;

				if (!first)
					buffer.write(" + ");
				buffer.write("" + c);
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
	 * Construct a new inequality system as a copy of another system.
	 * @param system The inequality system to copy from.
	 * @param implementation The implementation to use for solving systems.
	 */
	public InequalitySystem(InequalitySystem system, Implementation implementation) {
		inequalities.addAll(system.inequalities);
		this.implementation = implementation;
	}

	/**
	 * Construct a new inequality system.
	 */
	public InequalitySystem(InequalitySystem system) {
		this(system, DEFAULT_IMPLEMENTATION);
	}

	/**
	 * Construct a new inequality system.
	 * @param implementation The implementation to use for solving systems.
	 */
	public InequalitySystem(Implementation implementation) {
		this.implementation = implementation;
	}

	/**
	 * Construct a new inequality system.
	 */
	public InequalitySystem() {
		this(DEFAULT_IMPLEMENTATION);
	}

	/**
	 * Get the number of variables that this inequality system uses.
	 * @return The number of variables in the inequality system.
	 */
	public int getNumberOfVariables() {
		int numVariables = 0;
		for (Inequality inequality : inequalities)
			if (inequality.getNumberOfCoefficients() > numVariables)
				numVariables = inequality.getNumberOfCoefficients();
		return numVariables;
	}

	/**
	 * Choose which implementation to use for solving systems.
	 * @param implementation The implementation to use for solving systems.
	 */
	public void setImplementation(Implementation implementation) {
		this.implementation = implementation;
	}

	/**
	 * Get the currently selected implementation.
	 * @return The currently selected implementation.
	 */
	public Implementation getImplementation() {
		return implementation;
	}

	/**
	 * Add an inequality of the form <pre>lhs [comparator] sum(x[i] * coefficients[i])</pre> to the inequality
	 * system.
	 * @param inequality The inequality to add.
	 */
	public void addInequality(Inequality inequality) {
		inequalities.add(inequality);
	}

	/**
	 * Add an inequality of the form <pre>lhs [comparator] sum(x[i] * coefficients[i])</pre> to the inequality
	 * system.
	 * @param lhs The left hand side of the inequality.
	 * @param comparator Comparator for the inequality,
	 * @param coefficients List of coefficients for the inequality.
	 */
	public void addInequality(int lhs, Comparator comparator, int... coefficients) {
		addInequality(new Inequality(BigInteger.valueOf(lhs), comparator, toBigIntegerList(coefficients)));
	}

	/**
	 * Add an inequality of the form <pre>lhs [comparator] sum(x[i] * coefficients[i])</pre> to the inequality
	 * system.
	 * @param lhs The left hand side of the inequality.
	 * @param comparator Comparator for the inequality,
	 * @param coefficients List of coefficients for the inequality.
	 */
	public void addInequality(int lhs, String comparator, int... coefficients) {
		addInequality(lhs, Comparator.fromString(comparator), coefficients);
	}

	/**
	 * Add an inequality of the form <pre>lhs [comparator] sum(x[i] * coefficients[i])</pre> to the inequality
	 * system.
	 * @param lhs The left hand side of the inequality.
	 * @param comparator Comparator for the inequality,
	 * @param coefficients List of coefficients for the inequality.
	 */
	public void addInequality(int lhs, Comparator comparator, Collection<Integer> coefficients) {
		addInequality(new Inequality(BigInteger.valueOf(lhs), comparator, toBigIntegerList(coefficients)));
	}

	/**
	 * Add an inequality of the form <pre>lhs [comparator] sum(x[i] * coefficients[i])</pre> to the inequality
	 * system.
	 * @param lhs The left hand side of the inequality.
	 * @param comparator Comparator for the inequality,
	 * @param coefficients List of coefficients for the inequality.
	 */
	public void addInequality(int lhs, String comparator, Collection<Integer> coefficients) {
		addInequality(lhs, Comparator.fromString(comparator), coefficients);
	}

	/**
	 * Calculate a solution of the inequality system.
	 * @return A solution to the system or an empty list
	 */
	public List<Integer> findSolution() {
		List<Integer> solution;
		switch (this.implementation) {
			case CHOCO:
				solution = findSolutionChoco();
				break;
			case OJALGO:
				solution = findSolutionOJAlgo();
				break;
			default:
				throw new AssertionError("Unknown implementation requested");
		}
		if (solution.isEmpty()) {
			debug("No solution found for:");
			debug(this);
		} else {
			debug("Solution:");
			debug(solution);
		}
		return Collections.unmodifiableList(solution);
	}

	private List<Integer> findSolutionChoco() {
		final int numVariables = getNumberOfVariables();
		Solver solver = new Solver();

		IntVar[] vars = VariableFactory.integerArray("x", numVariables,
				VariableFactory.MIN_INT_BOUND, VariableFactory.MAX_INT_BOUND, solver);
		for (Inequality inequality : inequalities) {
			IntVar lhsVar = VariableFactory.fixed(inequality.getLeftHandSide().intValue(), solver);
			String comparator = inequality.getComparator().getOpposite().toString();

			List<BigInteger> coefficients = inequality.getCoefficients();
			int[] array = new int[numVariables];
			for (int i = 0; i < coefficients.size(); i++)
				array[i] = coefficients.get(i).intValue();

			solver.post(IntConstraintFactory.scalar(vars, array, comparator, lhsVar));
		}

		if (!solver.findSolution())
			return Collections.emptyList();

		List<Integer> solution = new ArrayList<>();
		for (int i = 0; i < numVariables; i++)
			solution.add(vars[i].getValue());

		return solution;
	}

	private List<Integer> findSolutionOJAlgo() {
		final int numVariables = getNumberOfVariables();
		ExpressionsBasedModel model = new ExpressionsBasedModel();

		Variable[] vars = new Variable[numVariables];
		for (int i = 0; i < numVariables; i++) {
			vars[i] = Variable.make("x" + i).integer(true);
			model.addVariable(vars[i]);
		}

		int inequalityNumber = 0;
		for (Inequality inequality : inequalities) {
			String id = "inequality_" + ++inequalityNumber;
			Expression c = model.addExpression(id);
			BigDecimal lhs;

			switch (inequality.getComparator()) {
				case LESS_THAN_OR_EQUAL:
					lhs = new BigDecimal(inequality.getLeftHandSide());
					c = c.lower(lhs);
					break;
				case LESS_THAN:
					lhs = new BigDecimal(inequality.getLeftHandSide().add(BigInteger.ONE));
					c = c.lower(lhs);
					break;
				case EQUAL:
					lhs = new BigDecimal(inequality.getLeftHandSide());
					c = c.level(lhs);
					break;
				case GREATER_THAN:
					lhs = new BigDecimal(inequality.getLeftHandSide().subtract(BigInteger.ONE));
					c = c.upper(lhs);
					break;
				case GREATER_THAN_OR_EQUAL:
					lhs = new BigDecimal(inequality.getLeftHandSide());
					c = c.upper(lhs);
					break;
			}

			List<BigInteger> coefficients = inequality.getCoefficients();
			for (int i = 0; i < coefficients.size(); i++)
				c.setLinearFactor(vars[i], coefficients.get(i));
		}

		Optimisation.Result result = model.solve();
		if (!result.getState().isSuccess())
			return Collections.emptyList();

		List<Integer> solution = new ArrayList<>();
		for (int i = 0; i < numVariables; i++)
			solution.add(result.get(i).round(MathContext.DECIMAL32).intValue());

		return solution;
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
