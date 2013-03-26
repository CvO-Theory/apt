/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
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

package uniol.apt.analysis.language;

import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.Arc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/**
 * This class represents a deterministic finite automaton (DFA). A DFA consists of a number of states. One state is the
 * initial state and any number of states can be accepting states. The DFA gets input from a given alphabet and changes
 * its state due to this. This can be used to describe a language, because a word is considered to be in the language if
 * the DFA is in an accepting state after being "fed" the word.
 * @author Uli Schlachter
 */
class DeterministicFiniteAutomaton {
	private final Set<DeterministicFiniteAutomatonState> states;
	private final Set<String> labels;
	private final DeterministicFiniteAutomatonState initialState;

	private static class ConstructFromLTS {
		// Map from a set of LTS-states to the corresponding DFA-states
		private final Map<Set<State>, DeterministicFiniteAutomatonState> stateGroups = new HashMap<>();
		// The set of DFA-states which still need to be fully explored
		private final Set<Set<State>> unhandled = new HashSet<>();
		// All labels that exist in the LTS
		private final Set<String> labels = new HashSet<>();

		private final DeterministicFiniteAutomatonState initialState;
		private final Set<DeterministicFiniteAutomatonState> states = new HashSet<>();

		/** @return The set of states of the resulting DFA */
		public Set<DeterministicFiniteAutomatonState> getStates() {
			return states;
		}

		/** @return The set of labels of the resulting DFA */
		public Set<String> getLabels() {
			return labels;
		}

		/** @return The initial state of the resulting DFA */
		public DeterministicFiniteAutomatonState getInitialState() {
			return initialState;
		}

		/**
		 * Construct the DFA for the given LTS via the powerset construction. This turns the NFA represented by
		 * the transition system into a DFA.
		 */
		public ConstructFromLTS(TransitionSystem lts) {
			// Figure out all of the lts' labels
			for (Arc edge : lts.getEdges())
				labels.add(edge.getLabel());
			assert lts.getInitialState() != null;

			// The DFA's initial state is { lts.getInitialState() }
			Set<State> initialSet = new HashSet<>();
			initialSet.add(lts.getInitialState());
			initialState = getState(initialSet);

			// Now visit all states that are needed
			while (!unhandled.isEmpty()) {
				Iterator<Set<State>> it = unhandled.iterator();
				Set<State> current = it.next();
				it.remove();

				exploreState(current);
			}
		}

		/**
		 * Get the DEA's state that represents the given nodes. This function automatically creates new states
		 * if none exists yet. It also makes sure that this new state will get explored later.
		 * @param nodes A set of states in the LTS that need a state in the DEA.
		 * @return A valid DEA state.
		 */
		private DeterministicFiniteAutomatonState getState(Set<State> nodes) {
			DeterministicFiniteAutomatonState state = stateGroups.get(nodes);
			if (state != null)
				return state;

			// All states are accepting, except the error state
			state = new DeterministicFiniteAutomatonState(!nodes.isEmpty());
			stateGroups.put(nodes, state);
			states.add(state);
			unhandled.add(nodes);
			return state;
		}

		/**
		 * Get the following states of a set of nodes.
		 * @param nodes The set of nodes which are the current state.
		 * @param label The label that should be followed.
		 * @return The new state of the LTS.
		 */
		private Set<State> followingState(Set<State> nodes, String label) {
			Set<State> result = new HashSet<>();
			for (State node : nodes) {
				for (Arc edge : node.getPostsetEdges()) {
					if (!label.equals(edge.getLabel()))
						// Not the label we are looking for
						continue;
					// Yay, the state is entered
					result.add(edge.getTarget());
				}
			}

			return result;
		}

		/**
		 * Explore the DEA's state for the given LTS states and create the following states.
		 * @param node The LTS' state to be explored.
		 */
		private void exploreState(Set<State> node) {
			DeterministicFiniteAutomatonState state = stateGroups.get(node);
			assert state != null;

			// Now explore all following states
			for (String label : labels) {
				Set<State> follow = followingState(node, label);
				DeterministicFiniteAutomatonState followState = getState(follow);
				state.addTransition(label, followState);
			}
		}
	}

	/**
	 * Call setupDone() on all states.
	 */
	private void setupDone() {
		for (DeterministicFiniteAutomatonState state : states)
			state.setupDone();

		// Do some basic sanity checks
		assert !labels.isEmpty() || states.size() == 1;
		assert !states.isEmpty();
		assert initialState != null;
		assert states.contains(initialState);
	}

	/**
	 * Construct a DFA from a transition system. The transition system is understood as a nondeterministic finite
	 * automaton (NFA) where every state is an accepting state. The powerset construction is used for converting
	 * this into a DFA.
	 * @param lts The LTS which should be transformed.
	 */
	DeterministicFiniteAutomaton(TransitionSystem lts) {
		ConstructFromLTS construct = new ConstructFromLTS(lts);
		states = unmodifiableSet(construct.getStates());
		initialState = construct.getInitialState();
		labels = unmodifiableSet(construct.getLabels());
		setupDone();
	}

	/**
	 * Construct a DFA from another DFA and a partition of its state. The new DFA will have one state for each
	 * equivalence class in the partition.
	 */
	private DeterministicFiniteAutomaton(DeterministicFiniteAutomaton dfa,
			Set<Set<DeterministicFiniteAutomatonState>> partition) {
		this.labels = unmodifiableSet(dfa.labels);
		Set<DeterministicFiniteAutomatonState> generatedStates = new HashSet<>();
		// Map equivalence classes of the old automaton to the new one
		Map<Set<DeterministicFiniteAutomatonState>, DeterministicFiniteAutomatonState> newStates
			= new HashMap<>();

		// Now create the states
		for (Set<DeterministicFiniteAutomatonState> klass : partition) {
			assert newStates.get(klass) == null;
			// All states in the class should have the same value for isAccepting(), so choose a random one
			boolean accepting = klass.iterator().next().isAccepting();
			DeterministicFiniteAutomatonState state = new DeterministicFiniteAutomatonState(accepting);
			newStates.put(klass, state);
			generatedStates.add(state);
		}

		// Also create the edges
		for (Set<DeterministicFiniteAutomatonState> klass : partition) {
			// Get a random state from the equivalence class
			DeterministicFiniteAutomatonState state = klass.iterator().next();
			for (String label : labels) {
				// Which state does this switch to for transition label?
				DeterministicFiniteAutomatonState next = state.getState(label);
				// Now figure out the corresponding states in this DFA
				DeterministicFiniteAutomatonState ownState = newStates.get(klass);
				DeterministicFiniteAutomatonState newState = newStates.get(findClass(partition, next));

				ownState.addTransition(label, newState);
			}
		}

		this.states = unmodifiableSet(generatedStates);
		// Class of the initial state
		this.initialState = newStates.get(findClass(partition, dfa.initialState));
		assert this.initialState != null;
		setupDone();
	}

	/**
	 * Construct the DFA which accepts the same language as this DFA and has a minimum number of states. The
	 * returned DFA is unique for the accepted language.
	 * @return The minimal DFA.
	 */
	public DeterministicFiniteAutomaton minimize() {
		/* We want to partition the DFA's state. Two states will be in the same equivalence class if they cannot
		 * be distinguished from each other by observing their behavior.
		 */
		Set<Set<DeterministicFiniteAutomatonState>> partition = findMinimalPartition();
		return new DeterministicFiniteAutomaton(this, partition);
	}

	/**
	 * Calculate the partition of the DFA's states into sets of undistinguishable states.
	 * @return The calculated partition
	 */
	private Set<Set<DeterministicFiniteAutomatonState>> findMinimalPartition() {
		Set<Set<DeterministicFiniteAutomatonState>> partition = new HashSet<>();

		/* We begin with two sets: All accepting and all rejecting states. */
		Set<DeterministicFiniteAutomatonState> accepting = new HashSet<>();
		Set<DeterministicFiniteAutomatonState> rejecting = new HashSet<>();
		for (DeterministicFiniteAutomatonState state : states) {
			if (state.isAccepting())
				accepting.add(state);
			else
				rejecting.add(state);
		}

		if (!accepting.isEmpty())
			partition.add(accepting);
		if (!rejecting.isEmpty())
			partition.add(rejecting);

		/* Now try to refine the partition. This is done by finding distinguishable states that are in the same
		 * class and splitting the class up.
		 */
		refinePartition(partition);
		return partition;
	}

	/**
	 * Refine the partition by splitting up distinguishable classes in two subclasses.
	 * @param partition The current partition
	 */
	private void refinePartition(Set<Set<DeterministicFiniteAutomatonState>> partition) {
		Iterator<Set<DeterministicFiniteAutomatonState>> it = partition.iterator();
		while (it.hasNext()) {
			Set<DeterministicFiniteAutomatonState> equivClass = it.next();
			for (String label : labels) {
				if (refinePartition(partition, equivClass, label)) {
					// The partition was modified, thus we must restart iterating
					it = partition.iterator();
				}
			}
		}
	}

	/**
	 * Refine the partition by splitting up distinguishable classes in two subclasses.
	 * @param partition The current partition
	 * @param equivClass An equivalence class that should be checked
	 * @param label A label which could split the class
	 * @return true if some class was split up
	 */
	private boolean refinePartition(Set<Set<DeterministicFiniteAutomatonState>> partition,
			Set<DeterministicFiniteAutomatonState> equivClass, String label) {
		// We check if all states in the equivalence class go to the same other equivalence class under label
		Set<DeterministicFiniteAutomatonState> other = null;
		Map<Set<DeterministicFiniteAutomatonState>, Set<DeterministicFiniteAutomatonState>> subPartition
			= new HashMap<>();
		for (DeterministicFiniteAutomatonState state : equivClass) {
			DeterministicFiniteAutomatonState follow = state.getState(label);
			Set<DeterministicFiniteAutomatonState> followClass = findClass(partition, follow);
			if (other == null) {
				other = followClass;
			} else if (!other.contains(follow)) {
				// The class splits!
				Set<DeterministicFiniteAutomatonState> p = subPartition.get(followClass);
				if (p == null) {
					p = new HashSet<>();
					subPartition.put(followClass, p);
				}
				p.add(state);
			}
		}

		if (subPartition.isEmpty())
			// Nothing was split
			return false;

		// Now split up the equivalence class (We are modifying an entry of partition and thus must temporarily
		// remove it!)
		boolean wasRemoved = partition.remove(equivClass);
		assert wasRemoved == true;
		for (Set<DeterministicFiniteAutomatonState> newClass : subPartition.values()) {
			// Remove these entries from the "parent" class
			for (DeterministicFiniteAutomatonState entry : newClass) {
				wasRemoved = equivClass.remove(entry);
				assert wasRemoved == true;
			}
			// Add this new class into the partition
			partition.add(newClass);
		}
		partition.add(equivClass);

		return true;
	}

	/**
	 * Find the equivalence class of the given state.
	 * @param partition partition of all states.
	 * @param state state whose equivalence class is wanted.
	 * @return The state's equivalence class.
	 */
	private Set<DeterministicFiniteAutomatonState> findClass(Set<Set<DeterministicFiniteAutomatonState>> partition,
			DeterministicFiniteAutomatonState state) {
		for (Set<DeterministicFiniteAutomatonState> group : partition) {
			if (group.contains(state))
				return group;
		}

		// A partition must contain a suitable equivalence class!
		throw new ArrayIndexOutOfBoundsException();
	}

	/**
	 * Check if two DFA accept the same language. This class does so by doing a depth-first-search through both DFA
	 * and checking if it reaches a state where only one of the DFA accept the current word. The path from the root
	 * to this state describes a word which is only in one of the accepted languages.
	 */
	private static class Equivalence {
		private static class CombinedState {
			public DeterministicFiniteAutomatonState stateA;
			public DeterministicFiniteAutomatonState stateB;

			CombinedState(DeterministicFiniteAutomatonState stateA,
					DeterministicFiniteAutomatonState stateB) {
				this.stateA = stateA;
				this.stateB = stateB;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null || this.getClass() != obj.getClass())
					return false;
				CombinedState combined = (CombinedState) obj;
				return stateA.equals(combined.stateA) && stateB.equals(combined.stateB);
			}

			@Override
			public int hashCode() {
				return stateA.hashCode() + stateB.hashCode();
			}
		}

		private List<Word> wordDifference = new LinkedList<>();
		private Word currentWord = new Word();
		private final Set<CombinedState> visitedStates = new HashSet<>();
		private final Set<String> labels;

		/**
		 * Check if the given two DFA accept the same language.
		 */
		Equivalence(DeterministicFiniteAutomaton a, DeterministicFiniteAutomaton b, boolean all) {
			labels = new HashSet<>(a.getLabels());
			labels.addAll(b.getLabels());

			followState(new CombinedState(a.getInitialState(), b.getInitialState()), all);
			wordDifference = unmodifiableList(wordDifference);
		}

		/**
		 * Get the word that is only accepted by one of the DFA.
		 * @return The word or null if both DFA accept the same language.
		 */
		public List<Word> getWordDifference() {
			return wordDifference;
		}

		/**
		 * Do a depth-first-search through the two DFAs.
		 * @param combined A combined state of the two DFAs that should be explored.
		 * @param all if all words or just the first one should be found.
		 * @return true if a suitable word was found. this.wordDifference is the word that was found.
		 */
		private boolean followState(CombinedState combined, boolean all) {
			DeterministicFiniteAutomatonState stateA = combined.stateA;
			DeterministicFiniteAutomatonState stateB = combined.stateB;

			if (stateA.isAccepting() != stateB.isAccepting()) {
				Word word = new Word(currentWord);
				wordDifference.add(word);
				// All words with this prefix will be different, no point in following this state
				return !all;
			}

			for (String label : labels) {
				DeterministicFiniteAutomatonState followA = stateA.getState(label);
				DeterministicFiniteAutomatonState followB = stateB.getState(label);

				// Only one of them might be null, else something is really, really wrong
				assert (followA != null) || (followB != null);

				// If the DFAs have different label sets, one of the states could be null.
				if (followA == null || followB == null) {
					// If the one existing state is accepting, we found a word difference
					if (followA != null && !followA.isAccepting())
						continue;
					if (followB != null && !followB.isAccepting())
						continue;
					Word word = new Word(currentWord);
					word.add(label);
					wordDifference.add(word);
					if (!all)
						return true;
					continue;
				}

				CombinedState followingState = new CombinedState(followA, followB);

				if (visitedStates.add(followingState)) {
					/* State was not yet visited, so do that */
					currentWord.add(label);
					if (followState(followingState, all))
						return true;

					String last = currentWord.remove(currentWord.size() - 1);
					assert last.equals(label);
				}
			}

			return false;
		}
	}

	/**
	 * Check if the two given automaton accept the same language. This works by calculating the reachable part of
	 * the product of the two automatons. When any state is found which is an accepting state in just one of the two
	 * automatons, then the automatons don't accept the same language.
	 * @param a The first automaton that should be used.
	 * @param b The second automaton.
	 * @return A word that isn't accepted by both automatons, or nil if they are equivalent.
	 */
	public static Word checkAutomatonEquivalence(DeterministicFiniteAutomaton a,
			DeterministicFiniteAutomaton b) {
		List<Word> list = checkAutomatonEquivalence(a, b, false);
		if (list.isEmpty())
			return null;
		return list.iterator().next();
	}

	/**
	 * Check if the two given automaton accept the same language. This works by calculating the reachable part of
	 * the product of the two automatons. When any state is found which is an accepting state in just one of the two
	 * automatons, then the automatons don't accept the same language.
	 * @param a The first automaton that should be used.
	 * @param b The second automaton.
	 * @param all If this is false, only the first word found is returned, else all words which can be found via a
	 * simple depth-first-search in the product automaton.
	 * @return A list of words which aren't accepted by both automatons.
	 */
	public static List<Word> checkAutomatonEquivalence(DeterministicFiniteAutomaton a,
			DeterministicFiniteAutomaton b, boolean all) {
		return new Equivalence(a, b, all).getWordDifference();
	}

	/**
	 * Get the DFAs labels.
	 * @return All labels that appear in the DFA.
	 */
	public Set<String> getLabels() {
		return labels;
	}

	/**
	 * Get the DFAs initial state.
	 * @return the initial state.
	 */
	public DeterministicFiniteAutomatonState getInitialState() {
		return initialState;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
