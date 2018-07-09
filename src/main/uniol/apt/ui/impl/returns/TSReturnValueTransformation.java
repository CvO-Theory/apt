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

package uniol.apt.ui.impl.returns;

import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.renderer.LTSRenderers;
import uniol.apt.ui.AptReturnValueTransformation;
import uniol.apt.ui.ReturnValueTransformation;

/**
 * Transforms a TransitionSystem into a String-description that uses the syntax
 * of the .apt file format.
 *
 * @author Vincent Göbel
 */
@AptReturnValueTransformation(TransitionSystem.class)
public class TSReturnValueTransformation extends AbstractRenderersReturnValueTransformation<TransitionSystem>
		implements ReturnValueTransformation<TransitionSystem> {
	public TSReturnValueTransformation() {
		super(LTSRenderers.INSTANCE, "apt");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
