/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Uli Schlachter
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

import uniol.apt.adt.ts.Arc;
import uniol.apt.module.exception.ModuleException;

/**
 * A MustLeadToSameStateException is thrown when two arcs must lead to the same state, but don't.
 * @author Uli Schlachter
 */
public class MustLeadToSameStateException extends ModuleException {
	public static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param arc1 An arc that should reach the same state as arc2.
	 * @param arc2 An arc that should reach the same state as arc1.
	 */
	public MustLeadToSameStateException(Arc arc1, Arc arc2) {
		super(String.format("Arcs %s and %s must lead to the same state.", arc1, arc2));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
