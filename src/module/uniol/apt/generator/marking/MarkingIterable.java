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

package uniol.apt.generator.marking;

import java.util.Iterator;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.generator.AbstractPetriNetIterableWrapper;

/**
 * Given some Petri nets, this class provides an Iterable that also returns some variations in the initial markings for
 * the nets.
 * @author Uli Schlachter
 */
public class MarkingIterable extends AbstractPetriNetIterableWrapper {
	// Argument for MarkingNetGenerator
	private final int tokenLimit;

	/**
	 * Create a new instance that returns Petri nets for various initial markings for the given nets.
	 * @param iterable the list of Petri nets that should be used
	 * @param tokenLimit the maximum number of token that should be added to the initial marking of the Petri nets.
	 */
	public MarkingIterable(Iterable<PetriNet> iterable, int tokenLimit) {
		super(iterable);
		this.tokenLimit = tokenLimit;
	}

	@Override
	protected Iterator<PetriNet> newSubIterator(PetriNet pn) {
		return new MarkingNetGenerator(pn, tokenLimit).iterator();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
