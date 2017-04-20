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
 * Handles {@link Interrupter} implementations for different threads.
 *
 * @author Jonas Prellberg
 *
 */
public class InterrupterRegistry {

	private static final ThreadLocal<Interrupter> THREAD_LOCAL_INTERRUPTER = new ThreadLocal<>();
	private static final Interrupter NO_OP_INTERRUPTER = new NoOpInterrupter();

	/**
	 * Clear the interrupter that was set for the currently executing
	 * (calling) thread.
	 */
	public static void clearCurrentThreadInterrupter() {
		THREAD_LOCAL_INTERRUPTER.remove();
	}

	/**
	 * Set the interrupter for the currently executing (calling) thread.
	 *
	 * @param interrupter
	 *                interrupter that will be used for the current thread
	 */
	public static void setCurrentThreadInterrupter(Interrupter interrupter) {
		THREAD_LOCAL_INTERRUPTER.set(interrupter);
	}

	/**
	 * Returns the interrupter for the currently executing (calling) thread.
	 * @return The interrupter for the current thread.
	 */
	public static Interrupter getCurrentThreadInterrupter() {
		Interrupter res = THREAD_LOCAL_INTERRUPTER.get();
		if (res != null) {
			return res;
		} else {
			return NO_OP_INTERRUPTER;
		}
	}

	/**
	 * Throws an exception if the interrupter for the currently executing
	 * (calling) thread determines that a task should be aborted.
	 *
	 * @throws UncheckedInterruptedException on interruption
	 */
	public static void throwIfInterruptRequestedForCurrentThread() {
		if (getCurrentThreadInterrupter().isInterruptRequested()) {
			throw new UncheckedInterruptedException();
		}
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
