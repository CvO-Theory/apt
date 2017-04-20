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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.logic.ConstantTerm;
import de.uni_freiburg.informatik.ultimate.logic.Logics;
import de.uni_freiburg.informatik.ultimate.logic.Model;
import de.uni_freiburg.informatik.ultimate.logic.Rational;
import de.uni_freiburg.informatik.ultimate.logic.ReasonUnknown;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.smtinterpol.DefaultLogger;
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.SMTInterpol;
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.TerminationRequest;

import uniol.apt.util.interrupt.UncheckedInterruptedException;
import uniol.apt.util.interrupt.InterrupterRegistry;
import uniol.apt.util.equations.InequalitySystem.Inequality;

import static uniol.apt.util.DebugUtil.debug;

/**
 * Solve an inequality system.
 * @author Uli Schlachter
 */
public class InequalitySystemSolver {
	private final Script script;
	private final List<InequalitySystem[]> systems = new LinkedList<>();
	private final Deque<Integer> systemsLengthStack = new LinkedList<>();
	private final Deque<Integer> variablesStack = new LinkedList<>();

	/**
	 * Constructor. Yes, it's that simple.
	 */
	public InequalitySystemSolver() {
		DefaultLogger logger = new DefaultLogger();
		script = new SMTInterpol(logger, new TerminationRequest() {
			@Override
			public boolean isTerminationRequested() {
				return InterrupterRegistry.getCurrentThreadInterrupter().isInterruptRequested();
			}
		});
		logger.setLoglevel(DefaultLogger.LOGLEVEL_OFF);
		script.setLogic(Logics.QF_LIA);
		systemsLengthStack.addLast(0);
		variablesStack.addLast(0);
	}

	private Term toTerm(Collection<Inequality> inequalities) {
		if (inequalities.isEmpty())
			return script.term("false");

		// Handle each inequality
		Term[] system = new Term[inequalities.size()];
		int nextSystemEntry = 0;
		for (Inequality inequality : inequalities) {
			List<BigInteger> coefficients = inequality.getCoefficients();
			Term terms[] = new Term[coefficients.size()];
			int nextEntry = 0;
			for (int i = 0; i < coefficients.size(); i++) {
				BigInteger coeff = coefficients.get(i);
				if (coeff.equals(BigInteger.ZERO))
					continue;
				if (coeff.equals(BigInteger.ONE))
					terms[nextEntry++] = script.term("var" + i);
				else
					terms[nextEntry++] = script.term("*",
							script.numeral(coeff), script.term("var" + i));
			}

			Term rhs;
			if (nextEntry == 0)
				rhs = script.numeral(BigInteger.ZERO);
			else if (nextEntry == 1)
				rhs = terms[0];
			else
				rhs = script.term("+", Arrays.copyOf(terms, nextEntry));

			Term lhs = script.numeral(inequality.getLeftHandSide());
			String comparator = inequality.getComparator().toString();
			system[nextSystemEntry++] = script.term(comparator, lhs, rhs);
		}

		if (nextSystemEntry == 1)
			return system[0];
		return script.term("and", system);
	}

	/**
	 * Assert a new set of inequality systems.
	 * When called with a parameter like <pre>{ A, B }</pre> where A and B are inequality systems, this adds the
	 * requirement that either A or B have to be satisfied to solutions.
	 * @param disjunction Contains a disjunction of inequality systems.
	 * @return This solver instance
	 */
	public InequalitySystemSolver assertDisjunction(InequalitySystem... disjunction) {
		int numVariables = 0;
		for (int i = 0; i < disjunction.length; i++)
			numVariables = Math.max(numVariables, disjunction[i].getNumberOfVariables());

		systems.add(disjunction);

		int existingVariables = variablesStack.pollLast();
		Sort sort = script.sort("Int");
		for (int i = existingVariables; i < numVariables; i++)
			script.declareFun("var" + i, new Sort[0], sort);
		variablesStack.addLast(Math.max(numVariables, existingVariables));

		// Assert new terms
		Term[] orTerms = new Term[disjunction.length];
		for (int i = 0; i < disjunction.length; i++)
			orTerms[i] = toTerm(disjunction[i]);
		if (orTerms.length == 1)
			script.assertTerm(orTerms[0]);
		else if (orTerms.length > 1)
			script.assertTerm(script.term("or", orTerms));
		return this;
	}

	/**
	 * Push the current solver state onto a stack. All following modifications can be undone via {@link #pop()}.
	 * @return This solver instance
	 */
	public InequalitySystemSolver push() {
		script.push(1);
		systemsLengthStack.addLast(systems.size());
		variablesStack.addLast(variablesStack.peekLast());
		return this;
	}

	/**
	 * Pop the last disjunction that was added from the solver context.
	 * This undoes the effects of the last call to {@link #push()}.
	 * @return This solver instance
	 */
	public InequalitySystemSolver pop() {
		script.pop(1);
		systems.subList(systemsLengthStack.removeLast(), systems.size()).clear();
		variablesStack.removeLast();
		return this;
	}

	/**
	 * Calculate a solution to the conjunction of disjunctions that were added to this solver.
	 * @return A solution to the systems or an empty list if unsolvable
	 */
	public List<BigInteger> findSolution() {
		List<BigInteger> solution = handleSolution(script, variablesStack.peekLast());
		if (solution.isEmpty()) {
			debug("No solution found for:");
			for (InequalitySystem[] disjunction : systems) {
				debug("at least one of:");
				for (int i = 0; i < disjunction.length; i++)
					debug(disjunction[i]);
			}
		} else {
			debug("Solution:");
			debug(solution);
			assert isSolution(solution) : solution + " should solve this system but does not";
		}
		return Collections.unmodifiableList(solution);
	}

	private boolean isSolution(List<BigInteger> solution) {
		int index = 0;
		for (InequalitySystem[] disjunction : systems) {
			boolean foundSolution = false;
			for (int i = 0; i < disjunction.length; i++) {
				foundSolution = disjunction[i].fulfilledBy(solution);
				if (foundSolution)
					break;
			}
			if (!foundSolution && disjunction.length > 0) {
				debug("Not a valid solution for sub-system with index ", index);
				return false;
			}
			index++;
		}
		return true;
	}

	static private List<BigInteger> handleSolution(Script script, int numVariables) {
		LBool isSat = script.checkSat();
		if (isSat == LBool.UNKNOWN) {
			// This will happen if the TerminationRequest given to the
			// Script constructor triggers
			// TODO maybe introduce a different exception?
			assert ReasonUnknown.TIMEOUT.equals(script.getInfo(":reason-unknown"))
				: script.getInfo(":reason-unknown");
			throw new UncheckedInterruptedException();
		} else if (isSat == LBool.UNSAT) {
			debug("SMTInterpol produced unsat: ", isSat);
			return Collections.emptyList();
		}

		// Transform the solution
		Model model = script.getModel();
		List<BigInteger> solution = new ArrayList<>(numVariables);
		for (int i = 0; i < numVariables; i++) {
			Term term = model.evaluate(script.term("var" + i));
			assert term instanceof ConstantTerm : term;

			Object value = ((ConstantTerm) term).getValue();
			assert value instanceof Rational : value;

			Rational rat = (Rational) value;
			solution.add(rat.numerator());
			assert rat.denominator().equals(BigInteger.ONE) : value;
		}
		return solution;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
