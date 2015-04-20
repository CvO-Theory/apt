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
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniol.apt.util.EquivalenceRelation;
import uniol.apt.util.IEquivalenceRelation;

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

	// Get an automaton with the given initial state
	static private DeterministicFiniteAutomaton getAutomaton(DFAState state) {
		final DFAState s = state;
		return new DeterministicFiniteAutomaton() {
			@Override
			public DFAState getInitialState() {
				return s;
			}

			@Override
			public Set<Symbol> getAlphabet() {
				return s.getDefinedSymbols();
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
		// Since we do not implement equals(), we must always return the same state and make use of equals() by
		// object identity.
		final State endState = new StateWithoutArcs(true);
		final Symbol a = atom;
		return getAutomaton(new AbstractState(false) {
			@Override
			public Set<Symbol> getDefinedSymbols() {
				return Collections.singleton(a);
			}

			@Override
			public Set<State> getFollowingStates(Symbol arg) {
				if (arg.equals(a))
					return Collections.<State>singleton(endState);
				return Collections.emptySet();
			}
		});
	}

	/**
	 * Get a finite automaton accepting the union of the languages of two automatons. A word is in the union of the
	 * languages if it is in at least one of the individual languages.
	 * @param a1 The first automaton of the union.
	 * @param a2 The second automaton of the union.
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
	 * Get a finite automaton accepting the negation of the language of the given automaton. A word is in the
	 * negation of the language if is is not in the language itself.
	 * @param a The automaton whose language is to negate.
	 * @return An automaton accepting the negation.
	 */
	static public DeterministicFiniteAutomaton negate(DeterministicFiniteAutomaton a) {
		return getAutomaton(new NegationState(a.getInitialState()));
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

	/**
	 * Construct a deterministic finite automaton from a general finite automaton via the powerset construction.
	 * @param a the automaton to determinize
	 * @return A deterministic finite automaton accepting the same language
	 */
	static public DeterministicFiniteAutomaton constructDFA(FiniteAutomaton a) {
		if (a instanceof DeterministicFiniteAutomaton)
			return (DeterministicFiniteAutomaton) a;
		return new PowerSetConstruction(a);
	}

	/**
	 * Get the unique deterministic automaton with the minimal number of states that accepts the same language as
	 * the given automaton.
	 * @param a the automaton to minimize
	 * @return The minimal automaton for the language
	 */
	static public DeterministicFiniteAutomaton minimize(FiniteAutomaton a) {
		if (a instanceof MinimalDeterministicFiniteAutomaton)
			return (DeterministicFiniteAutomaton) a;
		return new MinimalDeterministicFiniteAutomaton(a);
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

	// Implementation of the power set construction used by constructDFA().
	static private class PowerSetConstruction implements DeterministicFiniteAutomaton {
		private final Set<Symbol> alphabet;
		private final DFAState initialState;

		PowerSetConstruction(FiniteAutomaton a) {
			// Calculate some alphabet
			Set<Symbol> alphabet = new HashSet<>();
			Deque<State> unhandled = new LinkedList<>();
			Set<State> states = new HashSet<>();

			unhandled.add(a.getInitialState());
			states.add(a.getInitialState());

			while (!unhandled.isEmpty()) {
				State state = unhandled.removeFirst();
				alphabet.addAll(state.getDefinedSymbols());

				Set<State> nextStates = new HashSet<>(state.getFollowingStates(Symbol.EPSILON));
				for (Symbol symbol : state.getDefinedSymbols())
					nextStates.addAll(state.getFollowingStates(symbol));
				for (State next : nextStates)
					if (states.add(next))
						unhandled.add(next);
			}
			assert !alphabet.contains(Symbol.EPSILON);

			// Remember the alphabet for later and construct an initial state
			this.alphabet = Collections.unmodifiableSet(alphabet);
			this.initialState = new PowerSetState(Collections.singleton(a.getInitialState()));
		}

		@Override
		public Set<Symbol> getAlphabet() {
			return alphabet;
		}

		@Override
		public DFAState getInitialState() {
			return initialState;
		}

		private class PowerSetState extends DFAState {
			private final Set<State> states;

			public PowerSetState(Set<State> states) {
				this.states = followEpsilons(states);
			}

			@Override
			public Set<Symbol> getDefinedSymbols() {
				return getAlphabet();
			}

			@Override
			public boolean isFinalState() {
				for (State state : states)
					if (state.isFinalState())
						return true;
				return false;
			}

			@Override
			public DFAState getFollowingState(Symbol atom) {
				if (!getAlphabet().contains(atom))
					return null;

				Set<State> newStates = new HashSet<>();
				for (State state : states)
					newStates.addAll(state.getFollowingStates(atom));

				return new PowerSetState(newStates);
			}

			@Override
			public int hashCode() {
				return states.hashCode();
			}

			@Override
			public boolean equals(Object o) {
				if (!(o instanceof PowerSetState))
					return false;
				PowerSetState other = (PowerSetState) o;
				return states.equals(other.states);
			}
		}
	}

	// Implementation of the table filing algorithm used by minimize().
	static private class MinimalDeterministicFiniteAutomaton implements DeterministicFiniteAutomaton {
		private final Set<Symbol> alphabet;
		private final MinimalState[] states;

		public MinimalDeterministicFiniteAutomaton(FiniteAutomaton a) {
			DeterministicFiniteAutomaton dfa = constructDFA(a);
			this.alphabet = Collections.unmodifiableSet(dfa.getAlphabet());

			// Calculate equivalent states via the table filling algorithm
			EquivalenceRelation<DFAState> partition = getInitialPartitionForMinimize(dfa);
			partition = refinePartition(partition, alphabet);
			this.states = constructStates(partition, dfa.getInitialState());
		}

		@Override
		public DFAState getInitialState() {
			return states[0];
		}

		@Override
		public Set<Symbol> getAlphabet() {
			return alphabet;
		}

		static private class MinimalState extends DFAState {
			private final MinimalDeterministicFiniteAutomaton automaton;
			private final Map<Symbol, Integer> postset;
			private final boolean isFinalState;

			public MinimalState(MinimalDeterministicFiniteAutomaton automaton, Map<Symbol, Integer> postset, boolean isFinalState) {
				this.automaton = automaton;
				this.isFinalState = isFinalState;
				this.postset = postset;
			}

			@Override
			public boolean isFinalState() {
				return isFinalState;
			}

			@Override
			public Set<Symbol> getDefinedSymbols() {
				return automaton.getAlphabet();
			}

			@Override
			public DFAState getFollowingState(Symbol atom) {
				if (!automaton.getAlphabet().contains(atom))
					return null;
				return automaton.states[postset.get(atom)];
			}
		}

		static private EquivalenceRelation<DFAState> getInitialPartitionForMinimize(DeterministicFiniteAutomaton a) {
			DFAState finalState = null;
			DFAState nonFinalState = null;
			EquivalenceRelation<DFAState> relation = new EquivalenceRelation<>();
			Deque<DFAState> unhandled = new LinkedList<>();
			Set<State> states = new HashSet<>();

			// Go through all states
			unhandled.add(a.getInitialState());
			states.add(a.getInitialState());
			while (!unhandled.isEmpty()) {
				DFAState state = unhandled.removeFirst();

				// Add this state to the correct set
				if (state.isFinalState()) {
					if (finalState == null)
						finalState = state;
					relation.joinClasses(finalState, state);
				} else {
					if (nonFinalState == null)
						nonFinalState = state;
					relation.joinClasses(nonFinalState, state);
				}

				// Calculate all post-set states
				for (Symbol symbol : state.getDefinedSymbols()) {
					DFAState next = state.getFollowingState(symbol);
					if (states.add(next))
						unhandled.add(next);
				}
			}

			return relation;
		}

		static private EquivalenceRelation<DFAState> refinePartition(EquivalenceRelation<DFAState> partition, Set<Symbol> alphabet) {
			EquivalenceRelation<DFAState> lastPartition;
			do {
				lastPartition = partition;
				for (final Symbol symbol : alphabet) {
					// If there are two states in the same equivalence class and they have a
					// transition with symbol 'symbol' going to different classes, then this class
					// must be split.
					final IEquivalenceRelation<DFAState> current = partition;
					IEquivalenceRelation<DFAState> predicate = new IEquivalenceRelation<DFAState>() {
						@Override
						public boolean isEquivalent(DFAState state1, DFAState state2) {
							DFAState follow1 = state1.getFollowingState(symbol);
							DFAState follow2 = state2.getFollowingState(symbol);
							return current.isEquivalent(follow1, follow2);
						}
					};
					partition = partition.refine(predicate);
				}
			} while (lastPartition != partition);
			return partition;
		}

		private MinimalState[] constructStates(EquivalenceRelation<DFAState> partition, DFAState initialState) {
			Map<Set<DFAState>, Integer> stateIndex = new HashMap<>();
			Map<Integer, Boolean> isFinalState = new HashMap<>();
			stateIndex.put(partition.getClass(initialState), 0);
			isFinalState.put(0, initialState.isFinalState());
			int nextIndex = 1;
			Map<Integer, Map<Symbol, Integer>> transitions = new HashMap<>();
			Deque<Set<DFAState>> unhandled = new LinkedList<>();

			unhandled.add(partition.getClass(initialState));
			while (!unhandled.isEmpty()) {
				Set<DFAState> state = unhandled.removeFirst();

				DFAState representingState = state.iterator().next();
				Map<Symbol, Integer> postset = new HashMap<>();
				for (Symbol symbol : getAlphabet()) {
					DFAState followingState = representingState.getFollowingState(symbol);
					Set<DFAState> next = partition.getClass(followingState);
					if (!stateIndex.containsKey(next)) {
						isFinalState.put(nextIndex, followingState.isFinalState());
						stateIndex.put(next, nextIndex++);
						unhandled.add(next);
					}

					postset.put(symbol, stateIndex.get(next));
				}
				transitions.put(stateIndex.get(state), postset);
			}

			int numStates = nextIndex;
			MinimalState[] states = new MinimalState[numStates];
			for (int i = 0; i < numStates; i++)
				states[i] = new MinimalState(this, transitions.get(i), isFinalState.get(i));

			return states;
		}
	}

	static private class NegationState extends DFAState {
		private final DFAState originalState;

		public NegationState(DFAState originalState) {
			this.originalState = originalState;
		}

		@Override
		public boolean isFinalState() {
			return !originalState.isFinalState();
		}

		@Override
		public Set<Symbol> getDefinedSymbols() {
			return originalState.getDefinedSymbols();
		}

		@Override
		public DFAState getFollowingState(Symbol symbol) {
			DFAState state = originalState.getFollowingState(symbol);
			if (state == null)
				return null;
			return new NegationState(state);
		}

		@Override
		public int hashCode() {
			return originalState.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof NegationState))
				return false;
			NegationState other = (NegationState) o;
			return originalState.equals(other.originalState);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
