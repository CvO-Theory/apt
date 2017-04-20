/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016  Uli Schlachter
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

package uniol.apt.analysis.synthesize;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.module.exception.ModuleException;

/**
 * An InvalidRegionException is thrown when a Region turns out not to be a region.
 * @see Region#checkValidRegion()
 * @author Uli Schlachter
 */
public class InvalidRegionException extends ModuleException {
	public static final long serialVersionUID = 0L;

	/**
	 * Constructor creates an InvalidRegionException because the region prevents the given event in the given state.
	 * @param state The state in which an event is prevented
	 * @param event The event that is prevented
	 */
	public InvalidRegionException(State state, String event) {
		super(String.format("The given region prevents the enabled event '%s' in state %s",
					event, state.getId()));
	}

	/**
	 * Constructor creates an InvalidRegionException because the region is not consistent for the given arc. This
	 * means that the number of tokens assigned to the source of the arc plus the effect of the label of the arc
	 * does not equal the number of tokens assigned to the target of the arc.
	 * @param arc The arc that is not handled correctly
	 */
	public InvalidRegionException(Arc arc) {
		super(String.format("With the given region, the arc %s does not lead to the expected state", arc));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
