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

package uniol.apt.analysis.cycles.lts;

import java.util.List;
import java.util.Set;

import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.util.Pair;

/**
 * This is a base class for implementing algorithms against the {@Link ComputeSmallestCycles} interface
 * @author Chris, Manuel
 */
public abstract class AbstractComputeSmallestCycles implements ComputeSmallestCycles {
	private CycleCounterExample counterExample; // Stored countercycles

	@Override
	public Set<Pair<List<String>, ParikhVector>> computePVsOfSmallestCycles(TransitionSystem ts) {
		return computePVsOfSmallestCycles(ts, true);
	}

	@Override
	public boolean checkSameOrMutallyDisjointPVs(TransitionSystem ts) {
		return checkSameOrMutallyDisjointPVs(ts, true);
	}

	@Override
	public boolean checkSamePVs(TransitionSystem ts) {
		return checkSamePVs(ts, true);
	}

	@Override
	public abstract Set<Pair<List<String>, ParikhVector>> computePVsOfSmallestCycles(TransitionSystem ts,
			boolean smallest);

	@Override
	public boolean checkSameOrMutallyDisjointPVs(TransitionSystem ts, boolean smallest) {
		return checkSameOrMutallyDisjointPVs(computePVsOfSmallestCycles(ts, smallest));
	}

	@Override
	public boolean checkSamePVs(TransitionSystem ts, boolean smallest) {
		return checkSamePVs(computePVsOfSmallestCycles(ts, smallest));
	}

	@Override
	public boolean checkSameOrMutallyDisjointPVs(Set<Pair<List<String>, ParikhVector>> cycles) {
		counterExample = null;
		for (Pair<List<String>, ParikhVector> pair : cycles) {
			for (Pair<List<String>, ParikhVector> pair1 : cycles) {
				if (pair1 != pair) {
					if (!pair.getSecond().sameOrMutuallyDisjoint(pair1.getSecond())) {
						counterExample = new CycleCounterExample(pair, pair1);
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean checkSamePVs(Set<Pair<List<String>, ParikhVector>> cycles) {
		counterExample = null;
		for (Pair<List<String>, ParikhVector> pair : cycles) {
			for (Pair<List<String>, ParikhVector> pair1 : cycles) {
				if (pair1 != pair) {
					if (!pair.getSecond().equals(pair1.getSecond())) {
						counterExample = new CycleCounterExample(pair, pair1);
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public CycleCounterExample getCounterExample() {
		return counterExample;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
