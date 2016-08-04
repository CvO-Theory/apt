/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
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

package uniol.apt.analysis.presynthesis.pps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.Pair;

/**
 * Checks an LTS for several properties that must hold on the reachability graph
 * of a plain, pure and safe Petri net. This class can be used in a
 * pre-synthesis step for pps nets.
 *
 * <p>The properties are Proposition 1 (B) (D) (F) from
 * "K. Barylska, E. Best: Properties of Plain, Pure, and Safe Petri Nets."
 *
 * @author Jonas Prellberg
 *
 */
public class PpsPropertyChecker {

	/**
	 * Returns true if the given transition system satisfies all properties
	 * (B), (D) and (F).
	 *
	 * @param ts
	 *                transition system to examine
	 */
	public boolean hasProperties(TransitionSystem ts) {
		return hasPropertyB(ts) && hasPropertyD(ts) && hasPropertyF(ts, ts.getNodes().size() * 2);
	}

	/**
	 * Returns true if the given transition system satisfies the following
	 * property:
	 *
	 * <pre>
	 * If M'[a⟩M and M''[b⟩M, then [b⟩M' ⇔ [a⟩M''
	 * with transitions a, b
	 * </pre>
	 *
	 * @param ts
	 *                transition system to examine
	 */
	public boolean hasPropertyB(TransitionSystem ts) {
		for (State s : ts.getNodes()) {
			if (!hasPropertyB(s)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasPropertyB(State s) {
		List<Arc> preset = new ArrayList<>(s.getPresetEdges());
		for (int i = 0; i < preset.size(); i++) {
			for (int j = i + 1; j < preset.size(); j++) {
				Arc a = preset.get(i);
				Arc b = preset.get(j);
				boolean bToMQuote = !a.getSource().getPresetEdgesByLabel(b.getLabel()).isEmpty();
				boolean aToMQuoteQuote = !b.getSource().getPresetEdgesByLabel(a.getLabel()).isEmpty();
				if (bToMQuote != aToMQuoteQuote) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns true if the given transition system satisfies the following
	 * property:
	 *
	 * <pre>
	 * If M[a⟩ and M[b⟩ then for any K: (K[ab⟩ ⇔ K[ba⟩)
	 * with transitions a, b
	 * </pre>
	 *
	 * @param ts
	 *                transition system to examine
	 */
	public boolean hasPropertyD(TransitionSystem ts) {
		Set<Pair<String, String>> labelsToCheck = new HashSet<>();
		for (State s : ts.getNodes()) {
			labelsToCheck.addAll(getPostsetLabelPairs(s));
		}
		for (Pair<String, String> labels : labelsToCheck) {
			for (State sk : ts.getNodes()) {
				List<String> ab = Arrays.asList(labels.getFirst(), labels.getSecond());
				List<String> ba = Arrays.asList(labels.getSecond(), labels.getFirst());
				Set<State> kab = getSequenceDestinations(sk, ab);
				Set<State> kba = getSequenceDestinations(sk, ba);
				if (kab.isEmpty() != kba.isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns true if the given transition system satisfies the following
	 * property:
	 *
	 * <pre>
	 * If M[wv⟩ and M[vw⟩ and M[wc⟩ and M[vc⟩, then M[wvc⟩M' and M[vwc⟩M' and M[c⟩
	 * with sequences of transitions v, w and a transition c
	 * </pre>
	 *
	 * @param ts
	 *                transition system to examine
	 * @param maxPathLength
	 *                TODO
	 */
	public boolean hasPropertyF(TransitionSystem ts, int maxPathLength) {
		for (State s : ts.getNodes()) {
			if (!hasPropertyF(s, maxPathLength)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasPropertyF(State s, int maxPathLength) {
		for (Path w : new DfsPathIterator(s, maxPathLength)) {
			for (Path v : new DfsPathIterator(s, maxPathLength)) {
				// Check s[vc⟩ and s[wc⟩
				Set<Arc> postTargetW = new HashSet<>(w.getTarget().getPostsetEdges());
				Set<Arc> postTargetV = v.getTarget().getPostsetEdges();
				Set<String> candidatesC = new HashSet<>(arcToLabels(postTargetW));
				candidatesC.retainAll(arcToLabels(postTargetV));
				if (candidatesC.isEmpty()) {
					// Left side of the implication is not satisfied
					continue;
				}
				// Check s[vw⟩ and s[wv⟩
				List<String> labelsW = w.getLabels();
				List<String> labelsV = v.getLabels();
				Set<State> seqTargetsVW = getSequenceDestinations(v.getTarget(), labelsW);
				Set<State> seqTargetsWV = getSequenceDestinations(w.getTarget(), labelsV);
				if (seqTargetsVW.isEmpty() || seqTargetsWV.isEmpty()) {
					// Left side of the implication is not satisfied
					continue;
				}
				// Now the left side of the implication is satisfied
				// Check ∃c ∈ candidatesC: ...
				for (String c : candidatesC) {
					// Check s[c⟩
					if (s.getPostsetEdgesByLabel(c).isEmpty()) {
						continue;
					}
					// Check ∃s' ∈ S: (s[wvc⟩s' and s[vwc⟩s')
					for (State svw : seqTargetsVW) {
						for (State swv : seqTargetsWV) {
							Set<State> sQuoteCandidatesVW = new HashSet<>(svw.getPostsetNodesByLabel(c));
							Set<State> sQuoteCandidatesWV = swv.getPostsetNodesByLabel(c);
							sQuoteCandidatesVW.retainAll(sQuoteCandidatesWV);
							if (!sQuoteCandidatesVW.isEmpty()) {
								return true;
							}
						}
					}
				}
				return false;
			}
		}
		return true;
	}

	private Set<String> arcToLabels(Set<Arc> arcs) {
		Set<String> labels = new HashSet<>();
		for (Arc arc : arcs) {
			labels.add(arc.getLabel());
		}
		return labels;
	}

	private Set<Pair<String, String>> getPostsetLabelPairs(State s) {
		Set<Pair<String, String>> pairs = new HashSet<>();
		List<Arc> postset = new ArrayList<>(s.getPostsetEdges());
		for (int i = 0; i < postset.size(); i++) {
			for (int j = i + 1; j < postset.size(); j++) {
				Arc a = postset.get(i);
				Arc b = postset.get(j);
				pairs.add(new Pair<>(a.getLabel(), b.getLabel()));
			}
		}
		return pairs;
	}

	/**
	 * Returns the set of states that can be reached by a sequence of edges
	 * that leads from origin to destination with the given word. The result
	 * will be empty if the sequence does not exist.
	 *
	 * @param origin
	 *                origin state of the sequence
	 * @param labels
	 *                word that needs to be produced by the sequence
	 * @return set of reachable destination states
	 */
	private Set<State> getSequenceDestinations(State origin, List<String> labels) {
		if (labels.isEmpty()) {
			return Collections.emptySet();
		} else if (labels.size() == 1) {
			return origin.getPostsetNodesByLabel(labels.get(0));
		} else {
			Set<State> result = new HashSet<>();
			Set<State> postset = origin.getPostsetNodesByLabel(labels.get(0));
			for (State s : postset) {
				List<String> tail = new LinkedList<>(labels);
				tail.remove(0);
				result.addAll(getSequenceDestinations(s, tail));
			}
			return result;
		}
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
