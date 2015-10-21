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

import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.module.exception.ModuleException;

/**
 * A NoSuchTransitionException is thrown when a user-specified transition does not exist in the given Petri net.
 * @author Uli Schlachter
 */
public class NoSuchTransitionException extends ModuleException {
	// Chosen by fair dice roll. Guaranteed to be random.
	public static final long serialVersionUID = 4L;

	/**
	 * Constructor creates a new NoSuchTransitionException for the given arguments.
	 * @param pn The Petri net which was given
	 * @param id The id of the missing transition
	 */
	public NoSuchTransitionException(PetriNet pn, String id) {
		super("Petri net " + pn.getName() + " does not contain a transition '" + id + "'");
		try {
			assert pn.getTransition(id) == null;
		} catch (NoSuchNodeException e) {
			// This is expected, all is good
		}
	}

	/**
	 * Constructor creates a new NoSuchTransitionException for the given arguments.
	 * @param pn The Petri net which was given
	 * @param e The NoSuchNodeException that was thrown from getTransition()
	 */
	public NoSuchTransitionException(PetriNet pn, NoSuchNodeException e) {
		this(pn, e.getNodeId());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
