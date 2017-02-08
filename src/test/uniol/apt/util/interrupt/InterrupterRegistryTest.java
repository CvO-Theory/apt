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

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/** @author Uli Schlachter */
@SuppressWarnings("unchecked")
public class InterrupterRegistryTest {
	@Test(expectedExceptions = UncheckedInterruptedException.class)
	public void testInterruption() {
		assert !Thread.interrupted() : "The thread was already interrupted before this method was called";

		Interrupter interrupter = new ThreadStatusInterrupter();
		try {
			InterrupterRegistry.setCurrentThreadInterrupter(interrupter);
			assertThat(InterrupterRegistry.getCurrentThreadInterrupter(), sameInstance(interrupter));

			try {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			} catch (Throwable e) {
				throw new AssertionError(e);
			}

			Thread.currentThread().interrupt();
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
		} finally {
			InterrupterRegistry.clearCurrentThreadInterrupter();

			// Make sure the interruption flag is reset. First calls unsets it, second call checks that it
			// really was unset.
			if (!Thread.interrupted())
				throw new AssertionError("Interruption flag should have been set");
			if (Thread.interrupted())
				throw new AssertionError("Interruption flag should have been unset");
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
