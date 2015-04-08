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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static uniol.apt.util.DebugUtil.debug;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;

import de.uni_freiburg.informatik.ultimate.logic.ConstantTerm;
import de.uni_freiburg.informatik.ultimate.logic.Logics;
import de.uni_freiburg.informatik.ultimate.logic.Model;
import de.uni_freiburg.informatik.ultimate.logic.Rational;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.SMTInterpol;

/**
 * Representation of an inequality system.
 * @author Uli Schlachter
 */
public class InequalitySystem {
	private final List<Inequality> inequalities = new ArrayList<>();

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
		 * Compare a value via this comparator. For example, GREATER_THAN.compare(3, 4) would check that 3 > 4
		 * and thus return false.
		 * @param left The left side
		 * @param right The right side
		 * @return true if this comparator is satisfied.
		 * @param <T> the type of comparable to compare
		 */
		public <T> boolean compare(Comparable<T> left, T right) {
			int result = left.compareTo(right);
			switch (this) {
				case LESS_THAN_OR_EQUAL:
					return result <= 0;
				case LESS_THAN:
					return result < 0;
				case EQUAL:
					return result == 0;
				case GREATER_THAN:
					return result > 0;
				case GREATER_THAN_OR_EQUAL:
					return result >= 0;
				default:
					throw new AssertionError("Came across a Comparator with an invalid value: " +
							this.toString());
			}
		}

		/**
		 * Return the comparator which is described by the given string. Must be one of "<=", "<", "=", ">",
		 * ">=".
		 * @param str String representation of comparator.
		 * @return The matching comparator.
		 */
		static public Comparator fromString(String str) {
			for (Comparator comp : values())
				if (comp.toString().equals(str))
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
		private final String comment;

		/**
		 * Construct a new linear inequality.
		 * @param leftHandSide The left hand side of the inequality.
		 * @param comparator The comparator used between both sides.
		 * @param coefficients The coefficients of the unknowns on the right hand side.
		 * @param comment A comment that describes the meaning of this inequality.
		 */
		public Inequality(BigInteger leftHandSide, Comparator comparator, List<BigInteger> coefficients,
				String comment) {
			this.leftHandSide = leftHandSide;
			this.comparator = comparator;
			this.coefficients = Collections.unmodifiableList(coefficients);
			this.comment = comment;
		}

		/**
		 * Construct a new linear inequality.
		 * @param leftHandSide The left hand side of the inequality.
		 * @param comparator The comparator used between both sides.
		 * @param coefficients The coefficients of the unknowns on the right hand side.
		 */
		public Inequality(BigInteger leftHandSide, Comparator comparator, List<BigInteger> coefficients) {
			this(leftHandSide, comparator, coefficients, "");
		}

		/**
		 * Get the comment for this inequality.
		 * @return The comment.
		 */
		public String getComment() {
			return comment;
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

		/**
		 * Test if the given values for the unknowns fulfill this inequality.
		 * @param values The values for each variable.
		 * @return true if this inequality is fulfilled.
		 */
		public boolean fulfilledBy(List<? extends Number> values) {
			BigInteger rhs = BigInteger.ZERO;
			for (int i = 0; i < coefficients.size(); i++) {
				Number numberValue = values.get(i);
				BigInteger value;
				if (numberValue instanceof Integer) {
					value = BigInteger.valueOf((Integer) numberValue);
				} else if (numberValue instanceof BigInteger) {
					value = (BigInteger) numberValue;
				} else {
					throw new AssertionError("Sorry, cannot handle this type of list");
				}
				BigInteger coeff = coefficients.get(i);
				rhs = rhs.add(coeff.multiply(value));
			}

			return comparator.compare(leftHandSide, rhs);
		}

		/**
		 * Test if this inequality is homogeneous. A homogeneous inequality has the special property that if x
		 * is a solution, then so is r*x for any r >= 1. Please note that r is required to be at least one!
		 * @return True if this inequality is homegenous.
		 */
		public boolean isHomogeneous() {
			/* Also, since we are looking at linear inequalities, if we multiply an r >= 1 to the x in
			 * x[0]*c[0]+...+x[n]*c[n], then the r can be be factored out and the whole result of this line
			 * will be multiplied by r as well. Since r >= 1, this means that the following conditions must
			 * hold (but only if we are looking at integer solutions, else > and < need to change!).
			 */
			switch (getComparator()) {
				case LESS_THAN_OR_EQUAL:
					if (leftHandSide.signum() < 0)
						return false;
					break;
				case LESS_THAN:
					if (leftHandSide.compareTo(BigInteger.ONE.negate()) < 0)
						return false;
					break;
				case EQUAL:
					if (leftHandSide.signum() != 0)
						return false;
					break;
				case GREATER_THAN:
					if (leftHandSide.compareTo(BigInteger.ONE) > 0)
						return false;
					break;
				case GREATER_THAN_OR_EQUAL:
					if (leftHandSide.signum() > 0)
						return false;
					break;
				default:
					throw new AssertionError("Came across a Comparator with an invalid value: " +
							this.toString());
			}

			return true;
		}

		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(leftHandSide).append(' ');
			buffer.append(comparator.toString()).append(' ');
			boolean first = true;
			for (int j = 0; j < coefficients.size(); j++) {
				BigInteger c = coefficients.get(j);
				if (c.equals(BigInteger.ZERO))
					continue;

				if (!first)
					buffer.append(" + ");
				buffer.append(c);
				buffer.append("*x[");
				buffer.append(j);
				buffer.append("]");
				first = false;
			}
			if (first)
				buffer.append("0");
			if (!comment.isEmpty()) {
				buffer.append("\t(");
				buffer.append(comment);
				buffer.append(")");
			}
			return buffer.toString();
		}
	}

	/**
	 * Construct a new inequality system as a copy of another system.
	 * @param system The inequality system to copy from.
	 */
	public InequalitySystem(InequalitySystem system) {
		inequalities.addAll(system.inequalities);
	}

	/**
	 * Construct a new inequality system.
	 */
	public InequalitySystem() {
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
	 * @param comment A comment describing the inequality.
	 */
	public void addInequality(int lhs, Comparator comparator, int[] coefficients, String comment) {
		addInequality(new Inequality(BigInteger.valueOf(lhs), comparator, toBigIntegerList(coefficients),
					comment));
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
	 * @param comment A comment describing the inequality.
	 */
	public void addInequality(int lhs, String comparator, int[] coefficients, String comment) {
		addInequality(lhs, Comparator.fromString(comparator), coefficients, comment);
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
	 * @param comment A comment describing the inequality.
	 */
	public void addInequality(int lhs, Comparator comparator, Collection<Integer> coefficients, String comment) {
		addInequality(new Inequality(BigInteger.valueOf(lhs), comparator, toBigIntegerList(coefficients),
					comment));
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
	 * @param comment A comment describing the inequality.
	 */
	public void addInequality(int lhs, String comparator, Collection<Integer> coefficients, String comment) {
		addInequality(lhs, Comparator.fromString(comparator), coefficients, comment);
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
	 * Test if the given values for the unknowns fulfill this inequality system.
	 * @param values The values for each variable.
	 * @return true if this inequality system is fulfilled.
	 */
	public boolean fulfilledBy(List<? extends Number> values) {
		for (Inequality inequality : inequalities) {
			if (!inequality.fulfilledBy(values))
				return false;
		}
		return true;
	}

	/**
	 * Test if this inequality system is homogeneous. A homogeneous inequality system has the special property that
	 * if x is a solution, then so is r*x for any r >= 1. Please note that r is required to be at least one!
	 * @return True if this inequality is homegenous.
	 */
	public boolean isHomogeneous() {
		for (Inequality inequality : inequalities)
			if (!inequality.isHomogeneous())
				return false;
		return true;
	}

	/**
	 * Calculate a solution of the inequality system.
	 * @return A solution to the system or an empty list
	 */
	public List<Integer> findSolution() {
		return findSolution(new InequalitySystem[] { this });
	}

	/**
	 * Calculate a solution to a conjunction of disjunctions of inequality systems.
	 * When called with a parameter like <pre>{ { A, B }, { C }, { B, D, E } }</pre> where A to E are inequality
	 * systems, this tries to find a solution for <pre>(A or B) and C and (B or D or E)</pre>.
	 * @param systems Contains a hierarchy of systems where individual systems are connected by disjunctions and the
	 * disjunctions are connected by conjunctions.
	 * @return A solution to the systems or an empty list if unsolvable.
	 */
	static public List<Integer> findSolution(InequalitySystem[]... systems) {
		int numVariables = 0;
		for (int i = 0; i < systems.length; i++)
			for (int j = 0; j < systems[i].length; j++)
				numVariables = Math.max(numVariables, systems[i][j].getNumberOfVariables());

		Script script = createScript(numVariables);
		for (int i = 0; i < systems.length; i++) {
			Term[] orTerms = new Term[systems[i].length];
			for (int j = 0; j < systems[i].length; j++)
				orTerms[j] = toTerm(script, systems[i][j].inequalities);
			if (orTerms.length == 1)
				script.assertTerm(orTerms[0]);
			else if (orTerms.length > 1)
				script.assertTerm(script.term("or", orTerms));
		}

		List<Integer> solution = handleSolution(script, numVariables);
		if (solution.isEmpty()) {
			debug("No solution found for:");
			for (int i = 0; i < systems.length; i++) {
				if (i == 0)
					debug("at least one of:");
				else
					debug("and at least one of:");

				for (int j = 0; j < systems[i].length; j++)
					debug(systems[i][j]);
			}
		} else {
			debug("Solution:");
			debug(solution);
			assert isSolution(solution, systems) : solution + " should solve this system but does not";
		}
		return Collections.unmodifiableList(solution);
	}

	static private boolean isSolution(List<Integer> solution, InequalitySystem[][] systems) {
		for (int i = 0; i < systems.length; i++) {
			boolean foundSolution = false;
			for (int j = 0; j < systems[i].length; j++)
				if (foundSolution = systems[i][j].fulfilledBy(solution))
					break;
			if (!foundSolution && systems[i].length > 0) {
				debug("Not a valid solution for sub-system with index ", i);
				return false;
			}
		}
		return true;
	}

	static private Script createScript(int numVariables) {
		// Set up SMTInterpol in a way that it doesn't produce debug output
		Logger logger = Logger.getRootLogger();
		logger.addAppender(new NullAppender());
		Script script = new SMTInterpol(logger, false);
		script.setLogic(Logics.QF_LIA);

		// Create variables
		Sort sort = script.sort("Int");
		for (int i = 0; i < numVariables; i++)
			script.declareFun("var" + i, new Sort[0], sort);

		return script;
	}

	static private Term toTerm(Script script, Collection<Inequality> inequalities) {
		if (inequalities.isEmpty())
			return script.term("true");

		// Handle each inequality
		Term[] system = new Term[inequalities.size()];
		int nextSystemEntry = 0;
		for (Inequality inequality : inequalities) {
			List<BigInteger> coefficients = inequality.getCoefficients();
			Term rhs;
			if (coefficients.isEmpty())
				rhs = script.numeral(BigInteger.ZERO);
			else {
				Term terms[] = new Term[coefficients.size()];
				for (int i = 0; i < coefficients.size(); i++)
					terms[i] = script.term("*",
							script.numeral(coefficients.get(i)), script.term("var" + i));
				if (coefficients.size() == 1)
					rhs = terms[0];
				else
					rhs = script.term("+", terms);
			}

			Term lhs = script.numeral(inequality.getLeftHandSide());
			String comparator = inequality.getComparator().toString();
			system[nextSystemEntry++] = script.term(comparator, lhs, rhs);
		}

		if (nextSystemEntry == 1)
			return system[0];
		return script.term("and", system);
	}

	static private List<Integer> handleSolution(Script script, int numVariables) {
		LBool isSat = script.checkSat();
		if (isSat != LBool.SAT) {
			debug("SMTInterpol produced unsat: " + isSat.toString());
			return Collections.emptyList();
		}

		// Transform the solution
		Model model = script.getModel();
		List<Integer> solution = new ArrayList<>(numVariables);
		for (int i = 0; i < numVariables; i++) {
			Term term = model.evaluate(script.term("var" + i));
			assert term instanceof ConstantTerm : term;

			Object value = ((ConstantTerm) term).getValue();
			assert value instanceof Rational : value;

			Rational rat = (Rational) value;
			solution.add(rat.numerator().intValue());
			assert rat.denominator().equals(BigInteger.ONE) : value;
		}
		return solution;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[\n");
		for (Inequality inequality : inequalities) {
			buffer.append(inequality.toString());
			buffer.append("\n");
		}
		buffer.append("]");
		return buffer.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
