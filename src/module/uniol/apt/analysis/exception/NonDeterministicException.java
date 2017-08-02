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

package uniol.apt.analysis.exception;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * A NonDeterministicException is thrown when an analysis needs a deterministic input, but was given a non-deterministic
 * one.
 * @author Uli Schlachter
 */
public class NonDeterministicException extends PreconditionFailedException {
	public static final long serialVersionUID = 1L;

	private final TransitionSystem ts;
	private final State node;
	private final String label;
	private final boolean direction;

	/**
	 * Constructor creates a new NonDeterministicException saying that the given TransitionSystem is
	 * non-deterministic.
	 * @param ts A non-deterministic transition system.
	 * @param node The node where the transition system is not deterministic.
	 * @param label The label where the transition system is not deterministic.
	 * @param direction True if forward determinism does not hold, else false.
	 */
	public NonDeterministicException(TransitionSystem ts, State node, String label, boolean direction) {
		super("Transition system " + ts.getName() + " is non-deterministic, only deterministic inputs are"
				+ "(currently) supported.");
		this.ts = ts;
		this.node = node;
		this.label = label;
		this.direction = direction;
	}

	/**
	 * Get the transition system which was not deterministic.
	 * @return The transition system
	 */
	public TransitionSystem getTS() {
		return ts;
	}

	/**
	 * Get the node where the transition system was not deterministic.
	 * @return The node
	 */
	public State getNode() {
		return node;
	}

	/**
	 * Get the label where the transition system was not deterministic.
	 * @return The label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Was a counter-example to forward-determinism found?
	 * @return true if yes
	 */
	public boolean isForwardsCounterExample() {
		return direction;
	}

	/**
	 * Was a counter-example to backward-determinism found?
	 * @return true if yes
	 */
	public boolean isBackwardsCounterExample() {
		return direction;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
