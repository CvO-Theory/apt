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

package uniol.apt.analysis.deterministic;

import java.util.HashSet;
import java.util.Set;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.exception.NonDeterministicException;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Check if a labeled transition system is deterministic.
 *
 * @author Renke Grunwald
 *
 */
public class Deterministic {

	private final TransitionSystem ts;
	private boolean deterministic = false;
	private String label = null;
	private State node = null;
	private boolean forward;

	/**
	 * Creates a new {@link Deterministic} instance that operates on the
	 * given transition system.
	 *
	 * @param ts
	 *                transition system to check
	 */
	public Deterministic(TransitionSystem ts) {
		this(ts, true);
	}

	/**
	 * Creates a new {@link Deterministic} instance that operates on the
	 * given transition system.
	 *
	 * @param ts transition system to check
	 * @param forward If true, forward determinism is checked, else backward determinism.
	 */
	public Deterministic(TransitionSystem ts, boolean forward) {
		this.ts = ts;
		this.forward = forward;
		check();
	}

	static private class StateNameWithLabel {

		final String stateName;
		final String label;

		public StateNameWithLabel(String arcName, String label) {
			this.stateName = arcName;
			this.label = label;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
				+ ((stateName == null) ? 0 : stateName.hashCode());
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object object) {
			if (!(object instanceof StateNameWithLabel))
				return false;

			StateNameWithLabel other = (StateNameWithLabel) object;

			if (!stateName.equals(other.stateName)) {
				return false;
			}
			if (!label.equals(other.label)) {
				return false;
			}

			return true;
		}
	}

	/**
	 * Compute the values deterministic, label and node.
	 */
	private void check() {
		Set<StateNameWithLabel> statesLabels = new HashSet<StateNameWithLabel>();

		for (Arc arc : ts.getEdges()) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			String state = forward ? arc.getSourceId() : arc.getTargetId();
			StateNameWithLabel stateLabel = new StateNameWithLabel(state, arc.getLabel());

			if (statesLabels.contains(stateLabel)) {
				deterministic = false;
				label = stateLabel.label;
				node = forward ? arc.getSource() : arc.getTarget();
				return;
			}

			statesLabels.add(stateLabel);
		}

		deterministic = true;
		label = null;
		node = null;
	}

	/**
	 * Throw a {@link NonDeterministicException} if the labeled transition system is not deterministic, else do
	 * nothing.
	 * @throws NonDeterministicException If the transition system is not deterministic.
	 */
	public void throwIfNonDeterministic() throws NonDeterministicException {
		if (deterministic)
			return;
		throw new NonDeterministicException(ts, node, label, forward);
	}

	/**
	 * Check if the labeled transition system is deterministic
	 *
	 * @return true if deterministic, false otherwise
	 */
	public boolean isDeterministic() {
		return deterministic;
	}

	/**
	 * Gets the label that caused the labeled transition system to be
	 * non-deterministic.
	 *
	 * @return the label, or null if the labeled transition system is deterministic
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Gets the node that caused the labeled transition system to be
	 * non-deterministic.
	 *
	 * @return the node, or null if the labeled transition system is deterministic
	 */
	public State getNode() {
		return node;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
