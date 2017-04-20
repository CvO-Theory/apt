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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Predicate;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.interrupt.InterrupterRegistry;
import uniol.apt.util.EquivalenceRelation;
import uniol.apt.util.IEquivalenceRelation;
import uniol.apt.util.Pair;

/**
 * Utility functions for constructing and working with {@link FiniteAutomaton} instances.
 * @author Uli Schlachter
 */
public class FiniteAutomatonUtility {
	private FiniteAutomatonUtility() {
	}

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
			return getAutomaton(new EpsilonState());
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

	static private class EpsilonState extends DFAState {
		@Override
		public boolean isFinalState() {
			return true;
		}

		@Override
		public Set<Symbol> getDefinedSymbols() {
			return Collections.emptySet();
		}

		@Override
		public DFAState getFollowingState(Symbol arg) {
			return null;
		}
	}

	/**
	 * Get a finite automaton accepting the union of the languages of two automatons. A word is in the union of the
	 * languages if it is in at least one of the individual languages.
	 * @param a1 The first automaton of the union.
	 * @param a2 The second automaton of the union.
	 * @return An automaton accepting the union.
	 */
	static public FiniteAutomaton union(FiniteAutomaton a1, FiniteAutomaton a2) {
		if (a1 instanceof DeterministicFiniteAutomaton && a2 instanceof DeterministicFiniteAutomaton)
			return union((DeterministicFiniteAutomaton) a1, (DeterministicFiniteAutomaton) a2);

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
	 * Get a finite automaton accepting the union of the languages of two automatons. A word is in the union of the
	 * languages if it is in at least one of the individual languages.
	 * @param a1 The first automaton of the union.
	 * @param a2 The second automaton of the union.
	 * @return An automaton accepting the union.
	 */
	static public DeterministicFiniteAutomaton union(DeterministicFiniteAutomaton a1,
			DeterministicFiniteAutomaton a2) {
		Set<Symbol> alphabet = new HashSet<>(a1.getAlphabet());
		alphabet.addAll(a2.getAlphabet());
		return getAutomaton(new SynchronousParallelComposition(alphabet, a1.getInitialState(),
					a2.getInitialState(), SynchronousParallelComposition.Mode.UNION));
	}

	/**
	 * Get a finite automaton accepting the intersection of the languages of two automatons. A word is in the
	 * intersection of the languages if it is in all of the individual languages.
	 * @param a1 The first automaton of the intersection.
	 * @param a2 The second automaton of the intersection.
	 * @return An automaton accepting the intersection.
	 */
	static public FiniteAutomaton intersection(FiniteAutomaton a1, FiniteAutomaton a2) {
		// I don't know a nice construction for the intesection on nondeterministic automatons
		return intersection(constructDFA(a1), constructDFA(a2));
	}

	/**
	 * Get a finite automaton accepting the intersection of the languages of two automatons. A word is in the
	 * intersection of the languages if it is in all of the individual languages.
	 * @param a1 The first automaton of the intersection.
	 * @param a2 The second automaton of the intersection.
	 * @return An automaton accepting the intersection.
	 */
	static public DeterministicFiniteAutomaton intersection(DeterministicFiniteAutomaton a1,
			DeterministicFiniteAutomaton a2) {
		Set<Symbol> alphabet = new HashSet<>(a1.getAlphabet());
		alphabet.retainAll(a2.getAlphabet());
		return getAutomaton(new SynchronousParallelComposition(alphabet, a1.getInitialState(),
					a2.getInitialState(), SynchronousParallelComposition.Mode.INTERSECTION));
	}

	/**
	 * Get a finite automaton accepting the negation of the language of the given automaton. A word is in the
	 * negation of the language if is is not in the language itself.
	 * @param a The automaton whose language is to negate.
	 * @return An automaton accepting the negation.
	 */
	static public DeterministicFiniteAutomaton negate(DeterministicFiniteAutomaton a) {
		return negate(a, a.getAlphabet());
	}

	/**
	 * Get a finite automaton accepting the negation of the language of the given automaton. A word is in the
	 * negation of the language if is is not in the language itself.
	 * @param a The automaton whose language is to negate.
	 * @param alphabet The alphabet of the negation
	 * @return An automaton accepting the negation.
	 */
	static public DeterministicFiniteAutomaton negate(FiniteAutomaton a, Set<Symbol> alphabet) {
		return negate(constructDFA(a), alphabet);
	}
	/**
	 * Get a finite automaton accepting the negation of the language of the given automaton. A word is in the
	 * negation of the language if is is not in the language itself.
	 * @param a The automaton whose language is to negate.
	 * @param alphabet The alphabet of the negation
	 * @return An automaton accepting the negation.
	 */
	static public DeterministicFiniteAutomaton negate(DeterministicFiniteAutomaton a, Set<Symbol> alphabet) {
		if (!alphabet.containsAll(a.getAlphabet())) {
			throw new IllegalArgumentException(
					"Alphabet of the automaton isn't subset of the given alphabet.");
		}

		return getAutomaton(new NegationState(a.getInitialState(), new HashSet<>(alphabet)));
	}

	/**
	 * Get a finite automaton accepting the concatenation of the languages of two automatons. A word is in the
	 * concatenation if it can be split into two words so that the first word is accepted by the first automaton and
	 * the second word by the second automaton.
	 * @param a1 The first automaton of the concatenation.
	 * @param a2 The second automaton of the concatenation.
	 * @return An automaton accepting the concatenation.
	 */
	static public FiniteAutomaton concatenate(FiniteAutomaton a1, FiniteAutomaton a2) {
		return getAutomaton(ConcatenateDecoratorState.getState(a1.getInitialState(), a2.getInitialState()));
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
	 * Get a finite automaton accepting repeatitions of the language of an automaton.
	 * @param a The automaton to repeat
	 * @param min minimum number of repeatitions
	 * @param max maximum number of repeatitions
	 * @return An automation accepting the repeatitions.
	 */
	static public FiniteAutomaton repeat(FiniteAutomaton a, int min, int max) {
		if (max < 0 || min < 0)
			throw new IllegalArgumentException("min and max must not be negative");
		if (min > max)
			throw new IllegalArgumentException("min must be less or equal max");

		if (max == 0)
			return getAtomicLanguage(Symbol.EPSILON);
		if (min > 0)
			return concatenate(a, repeat(a, min - 1, max - 1));
		else
			return optional(concatenate(a, repeat(a, 0, max - 1)));

	}

	/**
	 * Get a finite automaton accepting a word accepted by the given automaton or the empty word.
	 * @param a The automaton
	 * @return An automaton accepting the empty word or a word accepted by the given automaton.
	 */
	static public FiniteAutomaton optional(FiniteAutomaton a) {
		if (a instanceof DeterministicFiniteAutomaton)
			return optional((DeterministicFiniteAutomaton) a);

		final State initial = a.getInitialState();
		if (initial.isFinalState())
			return a;

		return getAutomaton(new StateWithoutArcs(true) {
			@Override
			public Set<State> getFollowingStates(Symbol atom) {
				if (atom.isEpsilon())
					return Collections.singleton(initial);
				return Collections.emptySet();
			}
		});
	}

	/**
	 * Get a finite automaton accepting a word accepted by the given automaton or the empty word.
	 * @param a The automaton
	 * @return An automaton accepting the empty word or a word accepted by the given automaton.
	 */
	static public DeterministicFiniteAutomaton optional(DeterministicFiniteAutomaton a) {
		final DFAState initial = a.getInitialState();
		if (initial.isFinalState())
			return a;

		// Create a new state which behaves just as the initial state but is accepting. It leads to the
		// "normal" states of the DFA.
		return getAutomaton(new DFAState() {
			@Override
			public boolean isFinalState() {
				return true;
			}

			@Override
			public Set<Symbol> getDefinedSymbols() {
				return initial.getDefinedSymbols();
			}

			@Override
			public DFAState getFollowingState(Symbol atom) {
				return initial.getFollowingState(atom);
			}

			// equals() and hashCode() are not needed: This state can not be reached again
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
	static private MinimalDeterministicFiniteAutomaton minimizeInternal(FiniteAutomaton a) {
		if (a instanceof MinimalDeterministicFiniteAutomaton)
			return (MinimalDeterministicFiniteAutomaton) a;
		return new MinimalDeterministicFiniteAutomaton(a);
	}

	/**
	 * Get the unique deterministic automaton with the minimal number of states that accepts the same language as
	 * the given automaton.
	 * @param a the automaton to minimize
	 * @return The minimal automaton for the language
	 */
	static public DeterministicFiniteAutomaton minimize(FiniteAutomaton a) {
		// The only difference is the return type (MinimalDeterministicFiniteAutomaton is private!)
		return minimizeInternal(a);
	}

	/**
	 * Get a finite automaton accepting the prefix closure of the language of the given automaton. A word is in the
	 * prefix closure if it is the prefix of a word in the language.
	 * @param a The automaton whose prefix closure should be generated.
	 * @return An automaton accepting the prefix closure.
	 */
	static public DeterministicFiniteAutomaton prefixClosure(FiniteAutomaton a) {
		if (a instanceof PrefixClosureAutomaton)
			return (PrefixClosureAutomaton) a;
		return new PrefixClosureAutomaton(a);
	}

	/**
	 * Test if two automaton are language equivalent. Automatons are language equivalent if they accept the same
	 * language.
	 * @param a1 The first automaton to test with
	 * @param a2 The second automaton to test with
	 * @return true if and only if both automaton accept the same language.
	 */
	static public boolean languageEquivalent(FiniteAutomaton a1, FiniteAutomaton a2) {
		return findWordDifference(a1, a2) == null;
	}

	/**
	 * Return a finite automaton that accepts all words which are only accepted by one of the automatons. This
	 * constructs (a1 minus a2) union (a2 minus a1).
	 * @param a1 The first automaton to test with
	 * @param a2 The second automaton to test with
	 * @return An automaton that accepts the difference of the two languages.
	 */
	static public FiniteAutomaton getDifferenceAutomaton(FiniteAutomaton a1, FiniteAutomaton a2) {
		DeterministicFiniteAutomaton dfa1 = constructDFA(a1);
		DeterministicFiniteAutomaton dfa2 = constructDFA(a2);

		Set<Symbol> alphabet = new HashSet<>(dfa1.getAlphabet());
		alphabet.addAll(dfa2.getAlphabet());
		// We are looking for words that dfa1 accepts and dfa2 does not accept or that dfa1 does not accept and
		// dfa2 accepts. This can be expressed as an automaton which accepts the empty language iff the two
		// input automaton are language equivalent
		DeterministicFiniteAutomaton notDfa1 = negate(dfa1, alphabet);
		DeterministicFiniteAutomaton notDfa2 = negate(dfa2, alphabet);
		return union(intersection(dfa1, notDfa2), intersection(notDfa1, dfa2));
	}

	/**
	 * Find a word that is only accepted by one of the automatons.
	 * @param a1 The first automaton to test with
	 * @param a2 The second automaton to test with
	 * @return A word that is only accepted by one of the automatons
	 */
	static public List<String> findWordDifference(FiniteAutomaton a1, FiniteAutomaton a2) {
		return findAcceptedWord(minimize(getDifferenceAutomaton(a1, a2)));
	}

	// Find a word that the given automaton accepts
	static private List<String> findAcceptedWord(DeterministicFiniteAutomaton dfa) {
		Set<DFAState> statesSeen = new HashSet<>();
		LinkedList<String> word = new LinkedList<>();
		Deque<Pair<DFAState, Iterator<Symbol>>> trace = new LinkedList<>();
		DFAState initial = dfa.getInitialState();
		trace.add(new Pair<>(initial, initial.getDefinedSymbols().iterator()));

		while (!trace.isEmpty()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			Pair<DFAState, Iterator<Symbol>> pair = trace.peekLast();
			if (!pair.getSecond().hasNext()) {
				trace.removeLast();
				word.pollLast();
			} else {
				Symbol symbol = pair.getSecond().next();
				DFAState nextState = pair.getFirst().getFollowingState(symbol);

				// Only follow this state if we haven't followed it yet before
				if (statesSeen.add(nextState)) {
					trace.add(new Pair<>(nextState, nextState.getDefinedSymbols().iterator()));
					word.add(symbol.getEvent());

					if (nextState.isFinalState())
						return word;
				}
			}
		}

		return null;
	}

	/**
	 * Find a word whose prefixes (including the word) conform to a given predicate and which itself also conforms
	 * to a second predicate.
	 *
	 * This method uses a depth-first search. A breath-first search would use more memory.
	 *
	 * @param a The automaton whose accepted words should get checked.
	 * @param prefixPredicate The predicate to check the prefixes.
	 * @param wordPredicate The predicate to check the words.
	 * @return A word which conforms to the predicates.
	 */
	static public List<String> findPredicateWord(FiniteAutomaton a, Predicate<List<String>> prefixPredicate,
			Predicate<List<String>> wordPredicate) {
		MinimalDeterministicFiniteAutomaton dfa = minimizeInternal(a);
		Deque<Pair<DFAState, Iterator<Symbol>>> trace = new ArrayDeque<>();
		LinkedList<String> word = new LinkedList<>();
		DFAState initial   = dfa.getInitialState();
		DFAState sinkState = findSinkState(dfa);
		trace.add(new Pair<>(initial, initial.getDefinedSymbols().iterator()));

		while (!trace.isEmpty()) {
			Pair<DFAState, Iterator<Symbol>> pair = trace.peekLast();
			if (!pair.getSecond().hasNext()) {
				trace.removeLast();
				word.pollLast();
			} else {
				Symbol symbol = pair.getSecond().next();
				DFAState nextState = pair.getFirst().getFollowingState(symbol);
				if (!nextState.equals(sinkState)) {
					word.add(symbol.getEvent());

					List<String> roWord = ListUtils.unmodifiableList(word);

					if (prefixPredicate.evaluate(roWord)) {
						trace.addLast(new Pair<>(nextState,
									nextState.getDefinedSymbols().iterator()));

						if (nextState.isFinalState() && wordPredicate.evaluate(roWord))
							return word;
					} else {
						word.removeLast();
					}
				}
			}
		}

		return null;
	}

	// find the sink state of an DFA if it exists
	static private DFAState findSinkState(MinimalDeterministicFiniteAutomaton dfa) {
		// A minimal DFA can have at most one "sink state". All words which cannot be extended into words of the
		// language will reach that sink state. Let's find that sink state and skip it in our translation.
		for (DFAState state : statesIterable(dfa)) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			// The sink state is not a final state and all arcs go back to itself
			if (state.isFinalState())
				continue;
			boolean sink = true;
			for (Symbol sym : dfa.getAlphabet())
				if (!state.getFollowingState(sym).equals(state)) {
					sink = false;
					break;
				}
			if (sink) {
				return state;
			}
		}
		return null;
	}

	/**
	 * Construct a transition system that describes the prefix language of the given finite automaton. For this, the
	 * minimal DFA is calculated and transformed.
	 * @param a The automaton whose prefix language should be generated.
	 * @return A transition system where sequences are enabled that correspond to prefixes of words which are
	 * accepted by a.
	 */
	static public TransitionSystem prefixLanguageLTS(FiniteAutomaton a) {
		MinimalDeterministicFiniteAutomaton dfa = minimizeInternal(a);
		DFAState sinkState = findSinkState(dfa);

		// Now create the transition system, but skip the sink state (if there is one)
		Map<DFAState, uniol.apt.adt.ts.State> stateMap = new HashMap<>();
		TransitionSystem result = new TransitionSystem();

		for (DFAState dfaState : statesIterable(dfa)) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			if (!dfaState.equals(sinkState))
				stateMap.put(dfaState, result.createState());
		}
		for (DFAState dfaState : statesIterable(dfa)) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			if (dfaState.equals(sinkState))
				continue;

			uniol.apt.adt.ts.State tsState = stateMap.get(dfaState);
			for (Symbol sym : dfa.getAlphabet()) {
				DFAState next = dfaState.getFollowingState(sym);
				if (!next.equals(sinkState))
					result.createArc(tsState, stateMap.get(next), sym.getEvent());
			}
		}

		if (stateMap.isEmpty())
			result.setInitialState(result.createState());
		else
			result.setInitialState(stateMap.get(dfa.getInitialState()));
		return result;
	}

	/**
	 * Construct a finite automaton from a transition system. Each sequence that reaches at least some state will be
	 * accepted by the automaton. Each sequence which reaches no state will be rejected.
	 * @param lts The transition system to transform.
	 * @return A finite automaton for the LTS' prefix language.
	 */
	static public FiniteAutomaton fromPrefixLanguageLTS(TransitionSystem lts) {
		return getAutomaton(new LTSAdaptorState(lts.getInitialState()));
	}

	/**
	 * Construct a finite automaton from a transition system with final states. Each sequence that reaches one of
	 * the final states will be accepted. All other sequences are rejected.
	 * @param lts The transition system to transform.
	 * @param finalStates Sequences reaching a state from this collection are accepted.
	 * @return A finite automaton for the LTS' language with the given final states.
	 */
	static public FiniteAutomaton fromLTS(TransitionSystem lts, Collection<uniol.apt.adt.ts.State> finalStates) {
		finalStates = new HashSet<>(finalStates);
		return getAutomaton(new LTSAdaptorState(lts.getInitialState(), finalStates));
	}

	/**
	 * Render a finite automaton in the Graphviz file format.
	 * @param aut The automaton to render
	 * @return The automaton in the Graphviz file format.
	 */
	static public String renderToGraphviz(FiniteAutomaton aut) {
		StringBuffer result = new StringBuffer("digraph G {\n");
		Map<State, String> identifier = new HashMap<>();

		int id = 0;
		for (State state : statesIterable(aut)) {
			if (state.isFinalState())
				result.append("  s" + id + " [peripheries=2];\n");
			identifier.put(state, "s" + id++);
		}

		result.append("  start [shape=point, color=white, fontcolor=white];\n");
		result.append("  start -> " + identifier.get(aut.getInitialState()) + ";\n");
		for (State state : statesIterable(aut)) {
			String stateId = identifier.get(state);
			Set<Symbol> symbols = new HashSet<>(state.getDefinedSymbols());
			symbols.add(Symbol.EPSILON);
			for (Symbol sym : symbols) {
				for (State next : state.getFollowingStates(sym)) {
					String nextId = identifier.get(next);
					result.append("  " + stateId + " -> " + nextId
							+ " [label=\"" + sym.toString() + "\"];\n");
				}
			}
		}

		result.append("}\n");
		return result.toString();
	}

	/**
	 * Get an Iterable iterating over all of the automaton's states. The initial state will be the first state
	 * returned by the iterable's iterators.
	 * @param a The automaton whose states should be iterated
	 * @return An iterable over the automaton's states.
	 */
	static public Iterable<State> statesIterable(FiniteAutomaton a) {
		return statesIterable(a.getInitialState());
	}

	/**
	 * Get an Iterable iterating over all of the automaton's states. The initial state will be the first state
	 * returned by the iterable's iterators.
	 * @param a The automaton whose states should be iterated
	 * @return An iterable over the automaton's states.
	 */
	static public Iterable<DFAState> statesIterable(DeterministicFiniteAutomaton a) {
		return statesIterable(a.getInitialState());
	}

	// Get an iterable iterating recursively over a state's postset.
	static private <S extends State> Iterable<S> statesIterable(final S initialState) {
		return new Iterable<S>() {
			@Override
			public Iterator<S> iterator() {
				final Deque<State> unhandled = new LinkedList<>();
				final Set<State> seen = new HashSet<>();
				unhandled.add(initialState);
				seen.add(initialState);
				return new Iterator<S>() {
					@Override
					public boolean hasNext() {
						return !unhandled.isEmpty();
					}

					@Override
					public S next() {
						State state = unhandled.pollFirst();
						if (state == null)
							throw new NoSuchElementException();
						for (State next : state.getFollowingStates(Symbol.EPSILON))
							if (seen.add(next))
								unhandled.add(next);
						for (Symbol symbol : state.getDefinedSymbols())
							for (State next : state.getFollowingStates(symbol))
								if (seen.add(next))
									unhandled.add(next);
						@SuppressWarnings("unchecked")
						S ret = (S) state;
						return ret;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
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
	// initial state of the second automaton. Internally, this is generalized to concatenation a list of automaton.
	static private class ConcatenateDecoratorState extends AbstractState {
		final private State currentState;
		final private List<State> targetStates;

		// Get a new ConcatenateDecoratorState, but avoid nesting such states for performance reason
		static public ConcatenateDecoratorState getState(State currentState, State targetState) {
			if (targetState instanceof ConcatenateDecoratorState) {
				ConcatenateDecoratorState target = (ConcatenateDecoratorState) targetState;
				List<State> targets = new ArrayList<>();
				targets.add(target.currentState);
				targets.addAll(target.targetStates);
				return getState(currentState, targets);
			}
			return getState(currentState, Collections.singletonList(targetState));
		}

		// Get a new ConcatenateDecoratorState, but avoid nesting such states for performance reason
		static private ConcatenateDecoratorState getState(State currentState, List<State> targetStates) {
			if (currentState instanceof ConcatenateDecoratorState) {
				ConcatenateDecoratorState current = (ConcatenateDecoratorState) currentState;
				List<State> targets = new ArrayList<>();
				targets.addAll(current.targetStates);
				targets.addAll(targetStates);
				return new ConcatenateDecoratorState(current.currentState, targets);
			}
			return new ConcatenateDecoratorState(currentState, targetStates);
		}

		// Use getState() instead of this constructor
		private ConcatenateDecoratorState(State currentState, List<State> targetStates) {
			super(false);

			assert !targetStates.isEmpty();
			assert noConcatenationStates(Collections.singleton(currentState));
			assert noConcatenationStates(targetStates);

			this.currentState = currentState;
			this.targetStates = targetStates;
		}

		static private boolean noConcatenationStates(Collection<State> states) {
			for (State state : states)
				if (state instanceof ConcatenateDecoratorState)
					return false;
			return true;
		}

		@Override
		public Set<Symbol> getDefinedSymbols() {
			return currentState.getDefinedSymbols();
		}

		@Override
		public Set<State> getFollowingStates(Symbol atom) {
			Set<State> result = new HashSet<>();
			for (State state : currentState.getFollowingStates(atom))
				result.add(getState(state, targetStates));
			if (currentState.isFinalState() && atom.isEpsilon()) {
				if (targetStates.size() == 1)
					result.add(targetStates.get(0));
				else
					result.add(new ConcatenateDecoratorState(targetStates.get(0),
								targetStates.subList(1, targetStates.size())));
			}
			return Collections.unmodifiableSet(result);
		}

		@Override
		public int hashCode() {
			return currentState.hashCode() + 31 * targetStates.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ConcatenateDecoratorState))
				return false;
			ConcatenateDecoratorState other = (ConcatenateDecoratorState) o;
			return currentState.equals(other.currentState) && targetStates.equals(other.targetStates);
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

	// Adaptor used by fromPrefixLanguageLTS(). Turns a LTS into a FiniteAutomaton
	static private class LTSAdaptorState implements State {
		final private Map<Symbol, Set<uniol.apt.adt.ts.State>> transitions;
		final private Collection<uniol.apt.adt.ts.State> finalStates;
		final private uniol.apt.adt.ts.State currentState;

		public LTSAdaptorState(uniol.apt.adt.ts.State state) {
			this(state, null);
		}

		public LTSAdaptorState(uniol.apt.adt.ts.State state, Collection<uniol.apt.adt.ts.State> finalStates) {
			this.finalStates = finalStates;
			this.currentState = state;

			Map<Symbol, Set<uniol.apt.adt.ts.State>> trans = new HashMap<>();
			for (Arc arc : state.getPostsetEdges()) {
				Symbol symbol = new Symbol(arc.getLabel());
				Set<uniol.apt.adt.ts.State> states = trans.get(symbol);
				if (states == null) {
					states = new HashSet<>();
					trans.put(symbol, states);
				}
				states.add(arc.getTarget());
			}
			this.transitions = Collections.unmodifiableMap(trans);
		}

		@Override
		public boolean isFinalState() {
			return finalStates == null || finalStates.contains(currentState);
		}

		@Override
		public Set<Symbol> getDefinedSymbols() {
			return transitions.keySet();
		}

		@Override
		public Set<State> getFollowingStates(Symbol atom) {
			Set<State> result = new HashSet<>();
			if (transitions.containsKey(atom))
				for (uniol.apt.adt.ts.State state : transitions.get(atom))
					result.add(new LTSAdaptorState(state, finalStates));
			return result;
		}

		@Override
		public int hashCode() {
			return currentState.hashCode() + Objects.hashCode(finalStates);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof LTSAdaptorState))
				return false;
			LTSAdaptorState other = (LTSAdaptorState) o;
			boolean result = currentState.equals(other.currentState) &&
				Objects.equals(finalStates, other.finalStates);
			if (result)
				assert transitions.equals(other.transitions);
			return result;
		}
	}

	// Implementation of the power set construction used by constructDFA().
	static private class PowerSetConstruction implements DeterministicFiniteAutomaton {
		private final Set<Symbol> alphabet;
		private final DFAState initialState;

		// Speed up PowerSetState.equals() by having a canonical version of each state so that this == o hits.
		private final Map<Set<State>, PowerSetState> stateIdentityCache = new HashMap<>();

		private PowerSetState getState(Set<State> states) {
			PowerSetState canonical = stateIdentityCache.get(states);
			if (canonical != null)
				return canonical;

			PowerSetState result = new PowerSetState(states);
			canonical = stateIdentityCache.get(result.states);
			if (canonical != null)
				result = canonical;

			stateIdentityCache.put(states, result);
			stateIdentityCache.put(result.states, result);
			return result;
		}

		PowerSetConstruction(FiniteAutomaton a) {
			// Calculate some alphabet
			Set<Symbol> alph = new HashSet<>();
			for (State state : statesIterable(a))
				alph.addAll(state.getDefinedSymbols());
			assert !alph.contains(Symbol.EPSILON);

			// Remember the alphabet for later and construct an initial state
			this.alphabet = Collections.unmodifiableSet(alph);
			this.initialState = getState(Collections.singleton(a.getInitialState()));
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
			private final int hashCode;
			private final Map<Symbol, DFAState> transitions = new HashMap<>();

			private PowerSetState(Set<State> states) {
				this.states = followEpsilons(states);
				this.hashCode = states.hashCode();
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

				DFAState result = transitions.get(atom);
				if (result != null)
					return result;

				Set<State> newStates = new HashSet<>();
				for (State state : states)
					newStates.addAll(state.getFollowingStates(atom));

				result = getState(newStates);
				transitions.put(atom, result);
				return result;
			}

			@Override
			public int hashCode() {
				return hashCode;
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

			public MinimalState(MinimalDeterministicFiniteAutomaton automaton, Map<Symbol, Integer> postset,
					boolean isFinalState) {
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

		static private EquivalenceRelation<DFAState> getInitialPartitionForMinimize(
				DeterministicFiniteAutomaton a) {
			DFAState finalState = null;
			DFAState nonFinalState = null;
			EquivalenceRelation<DFAState> relation = new EquivalenceRelation<>();
			for (DFAState state : statesIterable(a)) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
				if (state.isFinalState()) {
					if (finalState == null)
						finalState = state;
					relation.joinClasses(finalState, state);
				} else {
					if (nonFinalState == null)
						nonFinalState = state;
					relation.joinClasses(nonFinalState, state);
				}
			}

			return relation;
		}

		static private EquivalenceRelation<DFAState> refinePartition(EquivalenceRelation<DFAState> partition,
				Set<Symbol> alphabet) {
			EquivalenceRelation<DFAState> lastPartition;
			do {
				lastPartition = partition;
				for (final Symbol symbol : alphabet) {
					InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
					// If there are two states in the same equivalence class and they have a
					// transition with symbol 'symbol' going to different classes, then this class
					// must be split.
					final IEquivalenceRelation<DFAState> current = partition;
					IEquivalenceRelation<DFAState> predicate
						= new IEquivalenceRelation<DFAState>() {
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
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
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
			MinimalState[] result = new MinimalState[numStates];
			for (int i = 0; i < numStates; i++)
				result[i] = new MinimalState(this, transitions.get(i), isFinalState.get(i));

			return result;
		}
	}

	// An automaton representing the prefix closure of a given automaton.
	/*
	 * Algorithmic detail: In the minimal automaton there may be a sink state, which is a non-accepting state which
	 * cannot be left once it is reached. Because of the minimality, this state is unique (if it exists). Thus, a
	 * word is not in the prefix closure if and only if it reaches this sink state. To construct the prefix closure,
	 * we just turn all other states into accepting states.
	 */
	static private class PrefixClosureAutomaton implements DeterministicFiniteAutomaton {
		private final MinimalDeterministicFiniteAutomaton dfa;
		private final DFAState nonAcceptingState; // may be null!

		public PrefixClosureAutomaton(FiniteAutomaton a) {
			this.dfa = minimizeInternal(a);
			this.nonAcceptingState = findSinkState(dfa);
		}

		@Override
		public DFAState getInitialState() {
			return new PrefixClosureState(dfa.getInitialState(), nonAcceptingState);
		}

		@Override
		public Set<Symbol> getAlphabet() {
			return dfa.getAlphabet();
		}

		static private class PrefixClosureState extends DFAState {
			private final DFAState originalState;
			private final DFAState nonAcceptingState;

			public PrefixClosureState(DFAState originalState, DFAState nonAcceptingState) {
				this.originalState = originalState;
				this.nonAcceptingState = nonAcceptingState;
			}

			@Override
			public boolean isFinalState() {
				return !originalState.equals(nonAcceptingState);
			}

			@Override
			public Set<Symbol> getDefinedSymbols() {
				return originalState.getDefinedSymbols();
			}

			@Override
			public DFAState getFollowingState(Symbol atom) {
				DFAState followingState = originalState.getFollowingState(atom);
				if (followingState == null)
					return null;
				return new PrefixClosureState(followingState, nonAcceptingState);
			}

			@Override
			public int hashCode() {
				return originalState.hashCode();
			}

			@Override
			public boolean equals(Object o) {
				if (!(o instanceof PrefixClosureState))
					return false;
				PrefixClosureState other = (PrefixClosureState) o;
				return originalState.equals(other.originalState);
			}
		}
	}


	static private class NegationState extends DFAState {
		private final Set<Symbol> alphabet;
		private final DFAState originalState;

		public NegationState(DFAState originalState, Set<Symbol> alphabet) {
			this.alphabet = Collections.unmodifiableSet(alphabet);
			this.originalState = originalState;
		}

		@Override
		public boolean isFinalState() {
			return originalState == null || !originalState.isFinalState();
		}

		@Override
		public Set<Symbol> getDefinedSymbols() {
			return alphabet;
		}

		@Override
		public DFAState getFollowingState(Symbol symbol) {
			if (!alphabet.contains(symbol))
				return null;
			if (originalState == null)
				return this;
			DFAState state = originalState.getFollowingState(symbol);
			return new NegationState(state, alphabet);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(originalState);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof NegationState))
				return false;
			NegationState other = (NegationState) o;
			return Objects.equals(originalState, other.originalState);
		}
	}

	static private class SynchronousParallelComposition extends DFAState {
		private final Set<Symbol> alphabet;
		private final DFAState state1;
		private final DFAState state2;
		private final Mode mode;

		static public enum Mode {
			UNION {
				@Override
				public boolean isFinal(boolean state1Final, boolean state2Final) {
					return state1Final || state2Final;
				}
			},
			INTERSECTION {
				@Override
				public boolean isFinal(boolean state1Final, boolean state2Final) {
					return state1Final && state2Final;
				}
			};

			abstract public boolean isFinal(boolean state1Final, boolean state2Final);
		};

		public SynchronousParallelComposition(Set<Symbol> alphabet, DFAState state1, DFAState state2,
				Mode mode) {
			this.alphabet = alphabet;
			this.state1 = state1;
			this.state2 = state2;
			this.mode = mode;
		}

		@Override
		public boolean isFinalState() {
			return mode.isFinal(state1 != null && state1.isFinalState(),
					state2 != null && state2.isFinalState());
		}

		@Override
		public Set<Symbol> getDefinedSymbols() {
			return Collections.unmodifiableSet(alphabet);
		}

		@Override
		public DFAState getFollowingState(Symbol symbol) {
			if (!alphabet.contains(symbol))
				return null;
			DFAState follow1 = state1 == null ? null : state1.getFollowingState(symbol);
			DFAState follow2 = state2 == null ? null : state2.getFollowingState(symbol);
			return new SynchronousParallelComposition(alphabet, follow1, follow2, mode);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(state1) ^ Objects.hashCode(state2);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof SynchronousParallelComposition))
				return false;
			SynchronousParallelComposition other = (SynchronousParallelComposition) o;
			return Objects.equals(state1, other.state1) && Objects.equals(state2, other.state2);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
