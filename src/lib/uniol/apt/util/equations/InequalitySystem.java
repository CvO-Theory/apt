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
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.collections4.iterators.UnmodifiableIterator.unmodifiableIterator;

/**
 * Representation of an inequality system.
 * @author Uli Schlachter
 */
public class InequalitySystem extends AbstractCollection<InequalitySystem.Inequality> {
	private final List<Inequality> inequalities = new ArrayList<>();

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
		LESS_THAN_OR_EQUAL("<="),
		LESS_THAN("<"),
		EQUAL("="),
		GREATER_THAN(">"),
		GREATER_THAN_OR_EQUAL(">=");

		private final String representation;

		private Comparator(String representation) {
			this.representation = representation;
		}

		@Override
		public String toString() {
			return representation;
		}

		/**
		 * Compare a value via this comparator. For example, GREATER_THAN.compare(3, 4) would check that 3 &gt;
		 * 4 and thus return false.
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
		 * Return the comparator which is described by the given string. Must be one of "&lt;=", "&lt;", "=",
		 * "&gt;", "&gt;=".
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
	public void addInequality(int lhs, Comparator comparator, Collection<BigInteger> coefficients, String comment) {
		addInequality(new Inequality(BigInteger.valueOf(lhs), comparator, new ArrayList<>(coefficients),
					comment));
	}

	/**
	 * Add an inequality of the form <pre>lhs [comparator] sum(x[i] * coefficients[i])</pre> to the inequality
	 * system.
	 * @param lhs The left hand side of the inequality.
	 * @param comparator Comparator for the inequality,
	 * @param coefficients List of coefficients for the inequality.
	 */
	public void addInequality(int lhs, Comparator comparator, Collection<BigInteger> coefficients) {
		addInequality(new Inequality(BigInteger.valueOf(lhs), comparator, new ArrayList<>(coefficients)));
	}

	/**
	 * Add an inequality of the form <pre>lhs [comparator] sum(x[i] * coefficients[i])</pre> to the inequality
	 * system.
	 * @param lhs The left hand side of the inequality.
	 * @param comparator Comparator for the inequality,
	 * @param coefficients List of coefficients for the inequality.
	 * @param comment A comment describing the inequality.
	 */
	public void addInequality(int lhs, String comparator, Collection<BigInteger> coefficients, String comment) {
		addInequality(lhs, Comparator.fromString(comparator), coefficients, comment);
	}

	/**
	 * Add an inequality of the form <pre>lhs [comparator] sum(x[i] * coefficients[i])</pre> to the inequality
	 * system.
	 * @param lhs The left hand side of the inequality.
	 * @param comparator Comparator for the inequality,
	 * @param coefficients List of coefficients for the inequality.
	 */
	public void addInequality(int lhs, String comparator, Collection<BigInteger> coefficients) {
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

	@Override
	public int size() {
		return inequalities.size();
	}

	@Override
	public Iterator<Inequality> iterator() {
		return unmodifiableIterator(inequalities.iterator());
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
