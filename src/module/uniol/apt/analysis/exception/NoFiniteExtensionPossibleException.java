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

import uniol.apt.adt.ts.State;
import uniol.apt.module.exception.ModuleException;

/**
 * A NoFiniteExtensionPossibleException is thrown when the class {@link
 * uniol.apt.analysis.lts.extension.ExtendDeterministicPersistent} notices that no finite extension is possible.
 * @author Uli Schlachter
 */
public class NoFiniteExtensionPossibleException extends ModuleException {
	public static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param state A state that is reachable via different, non-Parikh-equivalent firing sequences.
	 */
	public NoFiniteExtensionPossibleException(State state) {
		super(String.format("State %s is reachable via different, non-Parikh-equivalent firing sequences"
					+ " and needs completion", state.getId()));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
