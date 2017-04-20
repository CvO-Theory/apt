/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Schlachter
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

package uniol.apt.adt.automaton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import uniol.apt.util.interrupt.InterrupterRegistry;
import uniol.apt.util.Pair;

import static uniol.apt.adt.automaton.FiniteAutomatonUtility.minimize;
import static uniol.apt.adt.automaton.FiniteAutomatonUtility.statesIterable;

/**
 * Convert a finite automaton to a language-equivalent regular expression.
 *
 * Define L^k_{i,j} to be all words that go from state i to state j and
 * in-between only visit states whose number is at most k. Then the language of
 * a finite automaton is the union of all L^n_{1,j} where n is the number of
 * states and j is some final state. All of these languages are regular. This
 * code computes this construction.
 * @author Uli Schlachter
 */
public class AutomatonToRegularExpression {
	private AutomatonToRegularExpression() { /* hide */ }

	/**
	 * Get an equivalent regular expression for a finite automaton.
	 * @param automaton The automaton to translate into a regex.
	 * @return The regex for the automaton.
	 */
	public static String automatonToRegularExpression(FiniteAutomaton automaton) {
		DeterministicFiniteAutomaton dfa = minimize(automaton);

		List<DFAState> states = new ArrayList<DFAState>();
		for (DFAState state : statesIterable(dfa)) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			states.add(state);
		}

		// Try with two different orders
		String regex1 = automatonToRegularExpression(dfa, states);
		Collections.reverse(states);
		String regex2 = automatonToRegularExpression(dfa, states);

		// And pick the shorter of the two results
		if (regex1.length() > regex2.length())
			return regex2;
		return regex1;
	}

	// Convert a DFA to a regex, iterating over states in the given order
	private static String automatonToRegularExpression(DeterministicFiniteAutomaton dfa,
			Collection<DFAState> states) {
		// Mapping contains regexes to get from one state to another
		Map<Pair<DFAState, DFAState>, RegEx> mapping = getInitialMapping(dfa);
		for (DFAState state : states)
			mapping = handleNextState(dfa, mapping, state);

		RegEx result = EMPTY_REGEX;
		for (DFAState state : states) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			if (!state.isFinalState())
				continue;
			Pair<DFAState, DFAState> pair = new Pair<>(dfa.getInitialState(), state);
			result = UnionRegEx.union(result, mapping.get(pair));
		}

		return result.toString();
	}

	// Get the initial mapping for the algorithm
	// A state can be reached from itself via epsilon. If there is an arc between two states (may also be twice the
	// same state!), then the symbol on that arc gets from one state to the other. In all other cases, the empty
	// regex (=nothing) does the job.
	private static Map<Pair<DFAState, DFAState>, RegEx> getInitialMapping(DeterministicFiniteAutomaton dfa) {
		Map<Pair<DFAState, DFAState>, RegEx> result = new HashMap<>();
		for (DFAState state1 : statesIterable(dfa)) {
			for (DFAState state2 : statesIterable(dfa)) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				RegEx regex;
				if (state1.equals(state2))
					regex = EPSILON_REGEX;
				else
					regex = EMPTY_REGEX;
				result.put(new Pair<>(state1, state2), regex);
			}
		}
		for (DFAState state : statesIterable(dfa)) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			for (Symbol symbol : dfa.getAlphabet()) {
				Pair<DFAState, DFAState> pair = new Pair<>(state, state.getFollowingState(symbol));
				RegEx regex = UnionRegEx.union(result.get(pair), SymbolRegEx.symbol(symbol));
				result.put(pair, regex);
			}
		}
		return result;
	}

	// Extend the given mapping to get from state a to state b by also considering paths going through newState.
	// This constructs regexes aToNew (newToNew)* newToB and adds it to each mapping
	private static Map<Pair<DFAState, DFAState>, RegEx> handleNextState(DeterministicFiniteAutomaton dfa,
			Map<Pair<DFAState, DFAState>, RegEx> mapping, DFAState newState) {
		Map<Pair<DFAState, DFAState>, RegEx> result = new HashMap<>(mapping);
		for (DFAState state1 : statesIterable(dfa)) {
			for (DFAState state2 : statesIterable(dfa)) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

				RegEx state1ToNew = mapping.get(new Pair<>(state1, newState));
				RegEx newToNew = mapping.get(new Pair<>(newState, newState));
				RegEx newToState2 = mapping.get(new Pair<>(newState, state2));
				Pair<DFAState, DFAState> pair = new Pair<>(state1, state2);
				RegEx state1ToState2 = mapping.get(pair);

				state1ToState2 = UnionRegEx.union(state1ToState2, ConcatenationRegEx.concatenate(
							state1ToNew, RepetitionRegEx.kleeneStar(newToNew),
							newToState2));
				result.put(pair, state1ToState2);
			}
		}
		return result;
	}

	// Precedence order inside a regex, earlier items bind weaker than following ones
	private static enum Precedence {
		UNION,
		CONCATENATION,
		REPETITION
	}

	private static interface RegEx {
		public String toStringInsideOfPrecendence(Precedence precedence);
	}

	// Regex representing a single symbol (may also be epsilon)
	private static class SymbolRegEx implements RegEx {
		static final public SymbolRegEx EPSILON = new SymbolRegEx(Symbol.EPSILON);
		private final Symbol symbol;

		private SymbolRegEx(Symbol symbol) {
			this.symbol = symbol;
		}

		@Override
		public String toString() {
			if (symbol.isEpsilon())
				return "$";
			String event = symbol.getEvent();
			if (event.length() == 1)
				return event;
			return "<" + event + ">";
		}

		@Override
		public String toStringInsideOfPrecendence(Precedence precedence) {
			return toString();
		}

		@Override
		public int hashCode() {
			return symbol.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof SymbolRegEx))
				return false;
			return symbol.equals(((SymbolRegEx) o).symbol);
		}

		public static RegEx symbol(Symbol sym) {
			if (sym.isEpsilon())
				return EPSILON;
			return new SymbolRegEx(sym);
		}
	}

	private static final RegEx EPSILON_REGEX = SymbolRegEx.EPSILON;

	// Regex representing the empty language
	private static final RegEx EMPTY_REGEX = new RegEx() {
		@Override
		public String toString() {
			return "~";
		}

		@Override
		public String toStringInsideOfPrecendence(Precedence precedence) {
			return toString();
		}
	};

	// Special regex which can "eat neighbors" inside a concatenation or alternation, e.g. a*a is the same as a+.
	private static interface MergingRegEx extends RegEx {
		// return null for "no replacement", else a regex replacing both this regex and after
		RegEx concatenateRight(RegEx right);
		RegEx concatenateLeft(RegEx left);
		RegEx union(RegEx other);
	}

	// A regex representing a concatenation of some regexes
	private static class ConcatenationRegEx implements RegEx {
		private final List<RegEx> regexes;

		private ConcatenationRegEx(List<RegEx> regexes) {
			this.regexes = regexes;
		}

		@Override
		public String toString() {
			StringBuilder res = new StringBuilder();
			for (RegEx regex : regexes)
				res.append(regex.toStringInsideOfPrecendence(Precedence.CONCATENATION));
			return res.toString();
		}

		@Override
		public String toStringInsideOfPrecendence(Precedence precedence) {
			if (Precedence.CONCATENATION.compareTo(precedence) < 0)
				return "(" + toString() + ")";
			return toString();
		}

		@Override
		public int hashCode() {
			return regexes.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof ConcatenationRegEx))
				return false;
			return regexes.equals(((ConcatenationRegEx) o).regexes);
		}

		public static RegEx concatenate(RegEx... regexes) {
			List<RegEx> list = new ArrayList<>();
			for (RegEx regex : regexes) {
				if (regex instanceof ConcatenationRegEx) {
					// Concatenation is associative, thus we can "absorb" this
					list.addAll(((ConcatenationRegEx) regex).regexes);
				} else if (EMPTY_REGEX.equals(regex)) {
					// Concatenation with the empty regex is the same just as the empty regex
					return EMPTY_REGEX;
				} else if (!EPSILON_REGEX.equals(regex)) {
					// EPSILON disappears in a concatenation
					list.add(regex);
				}
			}

			// Now go through the list and handle MergingRegEx instances and replaces repetitions with
			// RepetitionRegEx.
			ListIterator<RegEx> it = list.listIterator();
			RegEx previous = null;
			while (it.hasNext()) {
				RegEx current = it.next();
				if (previous != null) {
					RegEx replacement = null;
					if (replacement == null && previous instanceof MergingRegEx)
						replacement = ((MergingRegEx) previous).concatenateRight(current);
					if (replacement == null && current instanceof MergingRegEx)
						replacement = ((MergingRegEx) current).concatenateLeft(previous);
					if (replacement == null && current.equals(previous))
						replacement = RepetitionRegEx.repeat(current, 2);
					if (replacement != null) {
						// Remove this element and replace previous one
						it.remove();
						RegEx tmp = it.previous();
						assert tmp == previous;
						it.set(replacement);

						// If there is an element before, allow it to try to concatenate with
						// our current one
						if (it.hasPrevious()) {
							it.previous();
							current = it.next();
						} else
							current = null;
					}
				}
				previous = current;
			}
			if (list.isEmpty())
				return EMPTY_REGEX;
			if (list.size() == 1)
				return list.get(0);

			return new ConcatenationRegEx(list);
		}
	}

	// A regex representing several alternatives
	private static class UnionRegEx implements RegEx {
		private final List<RegEx> regexes;

		private UnionRegEx(List<RegEx> regexes) {
			this.regexes = regexes;
		}

		@Override
		public String toString() {
			StringBuilder res = new StringBuilder();
			boolean first = true;
			for (RegEx regex : regexes) {
				if (!first)
					res.append('|');
				res.append(regex.toStringInsideOfPrecendence(Precedence.UNION));
				first = false;
			}
			return res.toString();
		}

		@Override
		public String toStringInsideOfPrecendence(Precedence precedence) {
			if (Precedence.UNION.compareTo(precedence) < 0)
				return "(" + toString() + ")";
			return toString();
		}

		public RegEx unionWithoutEpsilon() {
			if (!regexes.contains(EPSILON_REGEX))
				return this;
			List<RegEx> list = new ArrayList<>(regexes);
			list.remove(EPSILON_REGEX);
			return union(list);
		}

		public static RegEx union(RegEx... regexes) {
			return union(Arrays.asList(regexes));
		}

		public static RegEx union(List<RegEx> regexes) {
			List<RegEx> list = new ArrayList<>();
			boolean hadExplicitEpsilon = false;
			boolean hadImplicitEpsilon = false;
			Deque<RegEx> regexesToAdd = new LinkedList<>(regexes);
			while (!regexesToAdd.isEmpty()) {
				RegEx regex = regexesToAdd.removeFirst();
				if (regex instanceof RepetitionRegEx) {
					hadImplicitEpsilon |= ((RepetitionRegEx) regex).containsEpsilon();
				}
				if (EPSILON_REGEX.equals(regex)) {
					hadExplicitEpsilon = true;
				} else if (EMPTY_REGEX.equals(regex)) {
					// The empty language disappears in an union
				} else if (list.contains(regex)) {
					// Duplicates disappear in an union
				} else {
					if (regex instanceof RepetitionRegEx) {
						RepetitionRegEx rep = (RepetitionRegEx) regex;
						Iterator<RegEx> it = list.iterator();
						while (it.hasNext()) {
							RegEx item = it.next();
							RegEx replacement = null;
							if (replacement == null && item instanceof RepetitionRegEx)
								replacement = ((RepetitionRegEx) item).union(rep);
							if (replacement == null)
								replacement = rep.union(item);
							if (replacement != null) {
								it.remove();
								rep = null;
								regexesToAdd.addFirst(replacement);
								break;
							}
						}
						if (rep != null)
							list.add(rep);
					} else {
						list.add(regex);
					}
				}
			}
			// a*|eps is the same as just a*, so if we dropped any epsilon above;
			// check if we have to add it back
			if (hadExplicitEpsilon && !hadImplicitEpsilon) {
				if (list.isEmpty())
					return EPSILON_REGEX;
				if (list.size() == 1)
					return RepetitionRegEx.optional(list.get(0));
				return RepetitionRegEx.optional(new UnionRegEx(list));
			} else {
				if (list.isEmpty())
					return EMPTY_REGEX;
				if (list.size() == 1)
					return list.get(0);

				return new UnionRegEx(list);
			}
		}
	}

	// A regex representing repetition. At least min repetitions are needed and at most max are allowed.
	// The operations ?, * and + are special cases of this.
	private static class RepetitionRegEx implements MergingRegEx {
		private static final int UNLIMITED = -1; // for max
		private final RegEx regex;
		private final int min;
		private final int max;

		private RepetitionRegEx(RegEx regex, int min, int max) {
			this.regex = regex;
			this.min = min;
			this.max = max;
		}

		public boolean containsEpsilon() {
			return min == 0;
		}

		// Check if we generate the underlying regex exactly once
		private boolean containsSingleRepetition() {
			return min <= 1 && (max == UNLIMITED || max >= 1);
		}

		@Override
		public String toString() {
			String str = regex.toStringInsideOfPrecendence(Precedence.REPETITION);
			if (max == UNLIMITED) {
				switch (min) {
					case 0:
						return str + "*";
					case 1:
						return str + "+";
					default:
						return str + "{" + min + ",}";
				}
			}
			if (min == max) {
				// Replace with direct repetition if that is shorter
				if (str.length() * min <= str.length() + 3) {
					StringBuilder result = new StringBuilder();
					for (int i = 0; i < min; i++)
						result.append(str);
					return result.toString();
				}
				return str + "{" + min + "}";
			}
			if (min == 0 && max == 1)
				return str + "?";
			return str + "{" + min + "," + max + "}";
		}

		@Override
		public String toStringInsideOfPrecendence(Precedence precedence) {
			if (Precedence.REPETITION.compareTo(precedence) < 0)
				return "(" + toString() + ")";
			return toString();
		}

		@Override
		public int hashCode() {
			return regex.hashCode() + min << 16 + max << 24;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof RepetitionRegEx))
				return false;
			RepetitionRegEx rep = (RepetitionRegEx) o;
			return min == rep.min && max == rep.max && regex.equals(rep.regex);
		}

		// (a|eps) is the same as a{0,1}, so treat it as such
		private static RegEx simplifyForUs(RegEx other) {
			if (other instanceof UnionRegEx) {
				RegEx withoutEps = ((UnionRegEx) other).unionWithoutEpsilon();
				if (!withoutEps.equals(other))
					other = new RepetitionRegEx(withoutEps, 0, 1);
			}
			return other;
		}

		@Override
		public RegEx concatenateRight(RegEx other) {
			other = simplifyForUs(other);
			// a{n,m}a{x,y} is the same as a{n+x,m+y} (if UNLIMITED is handled)
			if (other instanceof RepetitionRegEx) {
				RepetitionRegEx o = (RepetitionRegEx) other;
				if (!regex.equals(o.regex))
					return null;
				if (max == UNLIMITED || o.max == UNLIMITED)
					return new RepetitionRegEx(regex, min + o.min, UNLIMITED);
				return new RepetitionRegEx(regex, min + o.min, max + o.max);
			}

			// a{n,m}a is the same as a{n+1,m+1}
			if (regex.equals(other)) {
				if (max == UNLIMITED)
					return new RepetitionRegEx(regex, min + 1, UNLIMITED);
				return new RepetitionRegEx(regex, min + 1, max + 1);
			}

			return null;
		}

		@Override
		public RegEx concatenateLeft(RegEx other) {
			// All the things done above are symmetric
			return concatenateRight(other);
		}

		@Override
		public RegEx union(RegEx other) {
			other = simplifyForUs(other);

			// a{n,m}|a{x,y} is the same as a{min{n,x},max{m,y}} (if ranges overlap and UNLIMITED is handled)
			if (other instanceof RepetitionRegEx) {
				RepetitionRegEx o = (RepetitionRegEx) other;
				if (!regex.equals(o.regex))
					return null;

				if (o.min < min) {
					// o has the smaller minimum
					if (o.max != UNLIMITED && o.max + 1 < min) {
						// The ranges don't overlap
						return null;
					}
				} else if (o.min > min) {
					// We have the smaller minimum
					if (max != UNLIMITED && max + 1 < o.min) {
						// The ranges don't overlap
						return null;
					}
				}
				int newMin = Math.min(min, o.min);
				int newMax = Math.max(max, o.max);
				if (max == UNLIMITED || o.max == UNLIMITED)
					newMax = UNLIMITED;
				return new RepetitionRegEx(regex, newMin, newMax);
			}

			// a{n,m}|a is the same as a{n,m} if n,m are suitable
			if (containsSingleRepetition() && regex.equals(other))
				return this;
			return null;
		}

		// Produce regex{n}
		public static RegEx repeat(RegEx regex, int n) {
			regex = simplifyForUs(regex);
			if (n < 0)
				throw new IllegalArgumentException(n + " < 0");
			if (EMPTY_REGEX.equals(regex) || n == 0) {
				// Repeating the empty regex or repeating something 0 times produces the empty regex
				return EMPTY_REGEX;
			}
			if (EPSILON_REGEX.equals(regex) || n == 1) {
				// Repeating the epsilon regex (more than zero times) or one repetition is a no-op
				return regex;
			}
			return new RepetitionRegEx(regex, n, n);
		}

		// Produce regex?
		public static RegEx optional(RegEx regex) {
			regex = simplifyForUs(regex);
			if (EMPTY_REGEX.equals(regex) || EPSILON_REGEX.equals(regex)) {
				// ~? and $? both are the same as $
				return EPSILON_REGEX;
			}
			if (regex instanceof RepetitionRegEx) {
				RepetitionRegEx rep = (RepetitionRegEx) regex;
				// regex{0,x}? is the same as regex{0,x} for any x
				if (rep.min == 0)
					return regex;
				// regex{1,x}? is the same as regex{0,x} for any x
				if (rep.min == 1)
					return new RepetitionRegEx(rep.regex, 0, rep.max);
			}
			return new RepetitionRegEx(regex, 0, 1);
		}

		// Produce regex*
		public static RegEx kleeneStar(RegEx regex) {
			regex = simplifyForUs(regex);
			if (regex instanceof RepetitionRegEx) {
				RepetitionRegEx rep = (RepetitionRegEx) regex;
				// As long as the underlying regex is produced at least once, replace with kleeneStar
				if (rep.containsSingleRepetition())
					regex = rep.regex;
			}
			if (EMPTY_REGEX.equals(regex) || EPSILON_REGEX.equals(regex))
				// Iterating ~ or $ produces just the empty word
				return EPSILON_REGEX;
			if (regex instanceof UnionRegEx)
				regex = ((UnionRegEx) regex).unionWithoutEpsilon();
			return new RepetitionRegEx(regex, 0, UNLIMITED);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
