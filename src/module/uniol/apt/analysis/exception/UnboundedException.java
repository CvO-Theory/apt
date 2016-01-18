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

import uniol.apt.adt.pn.PetriNet;

/**
 * A UnboundedException is thrown when an analysis needs a bounded Petri net as its inbounded but is fed an unbounded
 * system.
 * @author Uli Schlachter, vsp
 */
public class UnboundedException extends PreconditionFailedException {
	public static final long serialVersionUID = 1L;

	/**
	 * Constructor creates a new UnboundedException saying that the given Petri net is unbounded.
	 * @param pn An unbounded Petri net.
	 */
	public UnboundedException(PetriNet pn) {
		super("Petri net " + pn.getName() + " is unbounded, only bounded Petri Nets are supported.");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
