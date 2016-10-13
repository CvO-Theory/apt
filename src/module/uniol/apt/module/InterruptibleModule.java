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

package uniol.apt.module;

import uniol.apt.module.exception.ModuleException;
import uniol.apt.util.interrupt.Interrupter;
import uniol.apt.util.interrupt.UncheckedInterruptedException;

/**
 * Interface for modules that are interruptible.
 *
 * @author Jonas Prellberg
 *
 */
public interface InterruptibleModule extends Module {

	/**
	 * Perform some computation in an interruptible manner on the values
	 * stored in the {@link ModuleInput} object and store the results in the
	 * {@link ModuleOutput} object.
	 *
	 * To retrieve values from {@link ModuleInput} object the
	 * {@link ModuleInput#getParameter(String, Class)} method can be used.
	 *
	 * To store values in the {@link ModuleOutput} object the
	 * {@link ModuleOutput#setReturnValue(String, Class, Object)} method can
	 * be used.
	 *
	 * The possible values in both the {@link ModuleInput} and
	 * {@link ModuleOutput} object are determined entirely by the
	 * specifications in the {@link #require(ModuleInputSpec)} and
	 * {@link #provide(ModuleOutputSpec)} method respectively.
	 *
	 * @param input
	 *                storage of the inputs
	 * @param output
	 *                storage of the outputs
	 * @param interrupter
	 *                callback that will be periodically checked and decides
	 *                if the module should prematurely abort with an
	 *                InterruptedException
	 * @throws ModuleException
	 *                 On various kinds of errors that prevent the module
	 *                 from working.
	 * @throws UncheckedInterruptedException
	 *                 thrown when the module is interrupted before
	 *                 completion
	 */
	public void run(ModuleInput input, ModuleOutput output, Interrupter interrupter)
			throws ModuleException, UncheckedInterruptedException;

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
