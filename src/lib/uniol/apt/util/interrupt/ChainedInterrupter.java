/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017  Uli Schlachter
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

package uniol.apt.util.interrupt;

/**
 * Interrupter that checks if any out of a given list of interrupters triggered.
 * @author Uli Schlachter
 */
public class ChainedInterrupter implements Interrupter {
	private final Interrupter[] interrupters;

	/**
	 * Create a new instance of this class checking the given interrupters. If any of the given interrupters reports
	 * a timeout, then this interrupter will do so as well.
	 * @param interrupters The interrupters to check.
	 */
	public ChainedInterrupter(Interrupter... interrupters) {
		this.interrupters = interrupters;
	}

	@Override
	public boolean isInterruptRequested() {
		for (Interrupter interrupter : interrupters)
			if (interrupter.isInterruptRequested())
				return true;
		return false;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
