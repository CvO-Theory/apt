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

package uniol.apt.util.interrupt;

/**
 * Interface for callbacks that can be supplied to modules before execution.
 * Modules will periodically use its method to check if they should continue
 * running or abort with an {@link InterruptedException}.
 *
 * @author Jonas Prellberg
 *
 */
public interface Interrupter {

	/**
	 * Determines if a task should be aborted.
	 *
	 * @return true if a task shoul be aborted
	 */
	boolean isInterruptRequested();

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
