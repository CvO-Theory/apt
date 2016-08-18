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

package uniol.apt.analysis.lts.extension;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.cycles.lts.ComputeSmallestCycles;
import uniol.apt.analysis.persistent.PersistentTS;
import uniol.apt.analysis.reversible.ReversibleTS;

/**
 * @author Renke Grunwald, Sören, Vincent
 *
 *         This class looks for an LTS which is reversible, persistent and all
 *         smallest cycles have the same parikh vectors.
 *
 */
public class ExtendTransitionSystem {

	private final TransitionSystem lts;
	private final BitSet ltsCode;
	private final ArrayList<String> ltsNodes;
	private final ArrayList<String> ltsLabels;
	private List<BitSet> knownMinimalValids;

	private BitSet lastGenerated;
	private boolean lastGeneratedValid;
	private boolean lastGeneratedMinimalValid;

	public ExtendTransitionSystem(TransitionSystem lts, int g) {
		this.lts = lts;
		ltsNodes = new ArrayList<>();
		Iterator<State> it = lts.getNodes().iterator();
		while (it.hasNext()) {
			ltsNodes.add(it.next().getId());
		}
		Collections.sort(ltsNodes);

		for (int i = 1; i <= g; i++) {
			String lastID = ltsNodes.get(lts.getNodes().size() - 1);
			String newNode = lastID + i;
			ltsNodes.add(newNode);
		}

		ltsLabels = new ArrayList<String>();
		for (Arc e : lts.getEdges()) {
			if (!ltsLabels.contains(e.getLabel()))
				ltsLabels.add(e.getLabel());
		}
		Collections.sort(ltsLabels);
		this.ltsCode = generateCode();

		knownMinimalValids = new ArrayList<>();
	}

	public ExtendTransitionSystem(TransitionSystem lts, int g, List<BitSet> minimals) {
		this(lts, g);
		knownMinimalValids = minimals;
	}

	/**
	 * Find the next valid code for an lts.
	 */
	public void findNextValid() {
		BitSet code = generateCode();
		TransitionSystem lts = buildLTS(code);
		while (!check(lts) || !newNodesReachable(lts)) {

			code = nextCode(code);
			if (code == null) {
				lastGenerated = null;
				lastGeneratedValid = false;
				lastGeneratedMinimalValid = false;
				return;
			}

			lts = buildLTS(code);
		}

		lastGenerated = code;
		lastGeneratedValid = true;
		lastGeneratedMinimalValid = true;
		knownMinimalValids.add(code);
	}

	/**
	 * If there is already a code for an lts, this method has to be used.
	 *
	 * @param oldCode The old code to start the search from
	 */
	public void findNextValid(BitSet oldCode) {
		BitSet code = nextCode(oldCode);

		TransitionSystem lts = buildLTS(code);
		while (!check(lts) || !newNodesReachable(lts)) {
			code = nextCode(code);
			if (code == null) {
				lastGenerated = null;
				lastGeneratedValid = false;
				lastGeneratedMinimalValid = false;
				return;
			}
			lts = buildLTS(code);
		}

		lastGenerated = code;
		lastGeneratedValid = true;
		lastGeneratedMinimalValid = noSmallerKnownValid(code);
		if (lastGeneratedMinimalValid)
			knownMinimalValids.add(code);
	}

	private boolean newNodesReachable(TransitionSystem lts) {
		for (int i = this.lts.getNodes().size(); i < lts.getNodes().size(); i++) {
			String id = ltsNodes.get(i);
			if (!reaches(lts.getInitialState(), lts.getNode(id))) {
				return false;
			}
		}

		return true;
	}

	private boolean reaches(State start, State goal) {
		Set<State> all = new HashSet<>();
		Set<State> now = new HashSet<>();
		Set<State> next = new HashSet<>();

		if (start.equals(goal)) {
			return true;
		}
		now.add(start);
		next.addAll(start.getPostsetNodes());

		while (next.size() > 0) {
			all.addAll(now);
			now = next;
			next = new HashSet<>();
			if (now.contains(goal)) {
				return true;
			}

			for (State node : now) {
				Set<State> candidates = node.getPostsetNodes();
				for (State cand : candidates) {
					if (!all.contains(cand) && !now.contains(cand)) {
						next.add(cand);
					}
				}
			}
		}

		return false;
	}

	public void findNextMinimal() {
		findNextValid();
	}

	public void findNextMinimal(BitSet oldCode) {
		BitSet code = (BitSet) oldCode.clone();

		boolean minimal;
		do {
			code = nextCode(code);
			minimal = noSmallerKnownValid(code);
		} while (!minimal || !check(buildLTS(code)));

		lastGenerated = code;
		lastGeneratedValid = true;
		lastGeneratedMinimalValid = true;
		knownMinimalValids.add(code);
	}

	private boolean noSmallerKnownValid(BitSet code) {
		for (BitSet known : knownMinimalValids) {
			if (isSubgraph(code, known))
				return false;
		}
		return true;
	}

	private BitSet generateCode() {
		int n = ltsNodes.size();
		int l = ltsLabels.size();
		int nl = n * l;

		BitSet code = new BitSet(n * n * l);

		for (Arc e : lts.getEdges()) {
			State source = e.getSource();
			State target = e.getTarget();
			String label = e.getLabel();

			int index = ltsNodes.indexOf(target.getId()) * nl;
			index += ltsLabels.indexOf(label) * n;
			index += ltsNodes.indexOf(source.getId());

			code.set(index);
		}

		return code;
	}

	public void findNext() {
		findNext(ltsCode);
	}

	public void findNext(BitSet oldCode) {
		BitSet code = nextCode(oldCode);

		lastGenerated = code;
		lastGeneratedValid = check(buildLTS(code));
		lastGeneratedMinimalValid = noSmallerKnownValid(code);
		if (lastGeneratedMinimalValid)
			knownMinimalValids.add(code);
	}

	private BitSet nextCode(final BitSet code) {
		BitSet next = (BitSet) ltsCode.clone();

		BitSet diff = (BitSet) code.clone();
		diff.andNot(ltsCode);
		int lastFreeBit = code.nextClearBit(0);
		if (lastFreeBit >= numberOfPossibleEdges()) {
			// All possible edges are already in place
			return null;
		}
		int additionalEdges = diff.cardinality();
		int lastDiffBit = diff.nextSetBit(0);
		boolean done;
		do {
			done = true;
			int nextFreeBit = ltsCode.nextClearBit(lastDiffBit + 1);
			/*
			 * All possible combinations of the current number of edges are used
			 * up -> increase number of edges
			 */
			if (nextFreeBit >= numberOfPossibleEdges() || additionalEdges == 0) {
				additionalEdges++;
				diff.clear(0, diff.size());
				int last = -1;
				for (int i = 0; i < additionalEdges; i++) {
					last = ltsCode.nextClearBit(last + 1);
					diff.set(last);
				}
				diff.or(ltsCode);
				return diff;
			}

			if (diff.get(nextFreeBit)) {
				diff.clear(lastDiffBit);
				diff.set(lastFreeBit);
				BitSet nevv = ((BitSet) ltsCode.clone());
				nevv.or(diff);
				lastFreeBit = nevv.nextClearBit(0);
				done = false;
				lastDiffBit = diff.nextSetBit(lastDiffBit + 1);
			} else {
				diff.clear(lastDiffBit);
				diff.set(nextFreeBit);
				next.or(diff);
			}

		} while (!done);
		return next;
	}

	public TransitionSystem buildLTS(BitSet code) {
		TransitionSystem lts = new TransitionSystem(this.lts);
		BitSet diff = (BitSet) code.clone();
		diff.andNot(ltsCode);
		int n = ltsNodes.size();
		int l = ltsLabels.size();
		int nl = n * l;
		for (int i = diff.nextSetBit(0); i > -1; i = diff.nextSetBit(i + 1)) {
			int source = i % n;
			int target = i / nl;
			int label = (i % nl) / n;

			try {
				lts.getNode(ltsNodes.get(source));
			} catch (Exception e) {
				lts.createState(ltsNodes.get(source));
			}

			try {
				lts.getNode(ltsNodes.get(target));
			} catch (Exception e) {
				lts.createState(ltsNodes.get(target));
			}
			// if (lts.getNode(ltsNodes.get(source)) == null) {
			// lts.createState(ltsNodes.get(source));
			// }

			// if (lts.getNode(ltsNodes.get(target)) == null) {
			// lts.createState(ltsNodes.get(target));
			// }

			lts.createArc(lts.getNode(ltsNodes.get(source)), lts.getNode(ltsNodes.get(target)),
					ltsLabels.get(label));
		}

		return lts;
	}

	private int numberOfPossibleEdges() {
		int n = ltsNodes.size();
		int l = ltsLabels.size();
		return n * n * l;
	}

	private boolean isSubgraph(BitSet code1, BitSet code2) {
		BitSet diff1 = (BitSet) code1.clone();
		BitSet diff2 = (BitSet) code2.clone();
		diff1.andNot(code2);
		diff2.andNot(code1);
		if (diff2.cardinality() == 0)
			return true;

		return false;
	}

	/**
	 * Checks the three conditions: persistent, reversible and if the smalles cycles
	 * have the same parikh vector.
	 * @param ts The transition system to check
	 * @return True if the conditions are true
	 */
	private boolean check(TransitionSystem ts) {
		PersistentTS p = new PersistentTS(ts);
		ReversibleTS r = new ReversibleTS(ts);
		ComputeSmallestCycles s = new ComputeSmallestCycles();

		if (!p.isPersistent() || !r.isReversible() || !s.checkSamePVs(ts))
			return false;

		return true;
	}

	public BitSet getLTSCode() {
		return ltsCode;
	}

	public List<BitSet> getListOfMinimals() {
		return this.knownMinimalValids;
	}

	public BitSet getLastGenerated() {
		return lastGenerated;
	}

	public boolean isLastGeneratedValid() {
		return lastGeneratedValid;
	}

	public boolean isLastGeneratedMinimal() {
		return lastGeneratedMinimalValid;
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
