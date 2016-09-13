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

package uniol.apt.module;

import uniol.apt.module.exception.ModuleException;
import uniol.apt.util.interrupt.Interrupter;
import uniol.apt.util.interrupt.InterrupterRegistry;
import uniol.apt.util.interrupt.UncheckedInterruptedException;

/**
 * Abstract implementation of the {@link InterruptibleModule} interface.
 *
 * <p>The interruptible version of the run-method sets the given interrupter in the
 * interrupter registry and clears it after the module is finished.
 *
 * @author Jonas Prellberg
 */
public abstract class AbstractInterruptibleModule extends AbstractModule implements InterruptibleModule {
	@Override
	public void run(ModuleInput input, ModuleOutput output, Interrupter interrupter)
			throws ModuleException, UncheckedInterruptedException {
		try {
			InterrupterRegistry.setCurrentThreadInterrupter(interrupter);
			run(input, output);
		} finally {
			InterrupterRegistry.clearCurrentThreadInterrupter();
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
