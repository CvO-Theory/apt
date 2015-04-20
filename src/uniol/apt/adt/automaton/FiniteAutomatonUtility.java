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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility functions for constructing and working with {@link FiniteAutomaton} instances.
 * @author Uli Schlachter
 */
public class FiniteAutomatonUtility {
	private FiniteAutomatonUtility() {}

	// Get an automaton with the given initial state
	static private FiniteAutomaton getAutomaton(State state) {
		final State s = state;
		return new FiniteAutomaton() {
			@Override
			public State getInitialState() {
				return s;
			}
		};
	}

	/**
	 * Get a finite automaton accepting the empty language.
	 * @return The automaton
	 */
	static public FiniteAutomaton getEmptyLanguage() {
		return getAutomaton(new StateWithoutArcs(false));
	}

	/**
	 * Get a finite automaton accepting just the word consisting of the given symbol.
	 * @param atom The only symbol after which the automaton accepts.
	 * @return The automaton
	 */
	static public FiniteAutomaton getAtomicLanguage(Symbol atom) {
		if (atom.isEpsilon())
			return getAutomaton(new StateWithoutArcs(true));
		final Symbol a = atom;
		return getAutomaton(new AbstractState(false) {
			@Override
			public Set<Symbol> getDefinedSymbols() {
				return Collections.singleton(a);
			}

			@Override
			public Set<State> getFollowingStates(Symbol arg) {
				if (arg.equals(a))
					return Collections.<State>singleton(new StateWithoutArcs(true));
				return Collections.emptySet();
			}
		});
	}

	/**
	 * Get a finite automaton accepting the union of the languages of two automatons. A word is in the union of the
	 * languages if it is in at least one of the individual languages.
	 * @param a1 The first automaton of the union.
	 * @param a1 The second automaton of the union.
	 * @return An automaton accepting the union.
	 */
	static public FiniteAutomaton union(FiniteAutomaton a1, FiniteAutomaton a2) {
		final Set<State> initialStates = new HashSet<>();
		initialStates.add(a1.getInitialState());
		initialStates.add(a2.getInitialState());
		return getAutomaton(new AbstractState(false) {
			@Override
			public Set<Symbol> getDefinedSymbols() {
				return Collections.emptySet();
			}

			@Override
			public Set<State> getFollowingStates(Symbol arg) {
				if (arg.isEpsilon())
					return initialStates;
				return Collections.emptySet();

			}
		});
	}

	/**
	 * Get a finite automaton accepting the concatenation of the languages of two automatons. A word is in the
	 * concatenation if it can be split into two words so that the first word is accepted by the first automaton and
	 * the second word by the second automaton.
	 * @param a1 The first automaton of the concatenation.
	 * @param a1 The second automaton of the concatenation.
	 * @return An automaton accepting the concatenation.
	 */
	static public FiniteAutomaton concatenate(FiniteAutomaton a1, FiniteAutomaton a2) {
		return getAutomaton(new ConcatenateDecoratorState(a1.getInitialState(), a2.getInitialState()));
	}

	/**
	 * Get a finite automaton accepting the kleene star of the language of an automaton. The kleene star iterates
	 * the language of an automaton. It starts with the language containing just the empty word and iteratively adds
	 * the concatenation of the result so far and the language itself.
	 * @param a The automaton to iterate
	 * @return An automation accepting the kleene star closure.
	 */
	static public FiniteAutomaton kleeneStar(FiniteAutomaton a) {
		return optional(kleenePlus(a));
	}

	/**
	 * Get a finite automaton accepting the kleene plus of the language of an automaton. The kleene plus iterates
	 * the language of an automaton. It starts with the language itself and iteratively adds the concatenation of
	 * the result so far and the language itself.
	 * @param a The automaton to iterate
	 * @return An automation accepting the kleene plus closure.
	 */
	static public FiniteAutomaton kleenePlus(FiniteAutomaton a) {
		return getAutomaton(new KleeneDecoratorState(a.getInitialState(), a.getInitialState()));
	}

	/**
	 * Get a finite automaton accepting a word accepted by the given automaton or the empty word.
	 * @param a The automaton
	 * @return An automaton accepting the empty word or a word accepted by the given automaton.
	 */
	static public FiniteAutomaton optional(FiniteAutomaton a) {
		final State initial = a.getInitialState();
		return getAutomaton(new StateWithoutArcs(true){
			@Override
			public Set<State> getFollowingStates(Symbol atom) {
				if (atom.isEpsilon())
					return Collections.singleton(initial);
				return Collections.emptySet();
			}
		});
	}

	// Recursively follow epsilon arcs in the given states
	static private Set<State> followEpsilons(Set<State> states) {
		Set<State> result = new HashSet<>(states);
		Deque<State> unhandled = new LinkedList<>(result);
		while (!unhandled.isEmpty()) {
			State state = unhandled.removeFirst();
			for (State newState : state.getFollowingStates(Symbol.EPSILON)) {
				if (result.add(newState))
					unhandled.add(newState);
			}
		}

		return result;
	}

	/**
	 * Test if a given word is accepted by a given finite automaton.
	 * @param a The automaton to use.
	 * @param word The word to check
	 * @return true if and only if the word is accepted by the automaton.
	 */
	static public boolean isWordInLanguage(FiniteAutomaton a, List<String> word) {
		Set<State> states = followEpsilons(Collections.singleton(a.getInitialState()));
		int position = 0;

		while (position < word.size()) {
			Symbol nextSymbol = new Symbol(word.get(position));
			Set<State> nextStates = new HashSet<>();
			for (State state : states)
				nextStates.addAll(state.getFollowingStates(nextSymbol));

			states = followEpsilons(nextStates);
			position++;
		}

		// We calculated all states reachable after 'word', see if there is some accepting state
		for (State state : states)
			if (state.isFinalState())
				return true;
		return false;
	}

	static private abstract class AbstractState implements State {
		private final boolean isFinalState;

		public AbstractState(boolean isFinalState) {
			this.isFinalState = isFinalState;
		}

		@Override
		public boolean isFinalState() {
			return isFinalState;
		}
	}

	static private class StateWithoutArcs extends AbstractState {
		public StateWithoutArcs(boolean isFinalState) {
			super(isFinalState);
		}

		@Override
		public Set<Symbol> getDefinedSymbols() {
			return Collections.emptySet();
		}

		@Override
		public Set<State> getFollowingStates(Symbol atom) {
			return Collections.emptySet();
		}
	}

	// Decorator used by concatenate(). It adds epsilon-transitions from final states of the first automaton to the
	// initial state of the second automaton.
	static private class ConcatenateDecoratorState extends AbstractState {
		final private State currentState;
		final private State targetState;

		public ConcatenateDecoratorState(State currentState, State targetState) {
			super(false);
			this.currentState = currentState;
			this.targetState = targetState;
		}

		@Override
		public Set<Symbol> getDefinedSymbols() {
			return currentState.getDefinedSymbols();
		}

		@Override
		public Set<State> getFollowingStates(Symbol atom) {
			Set<State> result = new HashSet<>();
			for (State state : currentState.getFollowingStates(atom))
				result.add(new ConcatenateDecoratorState(state, targetState));
			if (currentState.isFinalState() && atom.isEpsilon()) {
				result.add(targetState);
			}
			return result;
		}

		@Override
		public int hashCode() {
			return currentState.hashCode() + 31 * targetState.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ConcatenateDecoratorState))
				return false;
			ConcatenateDecoratorState other = (ConcatenateDecoratorState) o;
			return currentState.equals(other.currentState) && targetState.equals(other.targetState);
		}
	}

	// Decorator used by kleenePlus(). It adds epsilon-transitions from final states of the automaton back to its
	// initial state.
	static private class KleeneDecoratorState extends AbstractState {
		final private State currentState;
		final private State targetState;

		public KleeneDecoratorState(State currentState, State targetState) {
			super(currentState.isFinalState());
			this.currentState = currentState;
			this.targetState = targetState;
		}

		@Override
		public Set<Symbol> getDefinedSymbols() {
			return currentState.getDefinedSymbols();
		}

		@Override
		public Set<State> getFollowingStates(Symbol atom) {
			Set<State> result = new HashSet<>();
			for (State state : currentState.getFollowingStates(atom))
				result.add(new KleeneDecoratorState(state, targetState));
			if (currentState.isFinalState() && atom.isEpsilon()) {
				result.add(new KleeneDecoratorState(targetState, targetState));
			}
			return result;
		}

		@Override
		public int hashCode() {
			return currentState.hashCode() + 31 * targetState.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof KleeneDecoratorState))
				return false;
			KleeneDecoratorState other = (KleeneDecoratorState) o;
			return currentState.equals(other.currentState) && targetState.equals(other.targetState);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
