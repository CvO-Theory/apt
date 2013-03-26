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

package uniol.apt.generator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import uniol.apt.adt.pn.PetriNet;

/**
 * This class makes it easier to apply some "generator function" to each petri net in an Iterable which generates more
 * petri nets. Feel free to rename this class if you have any better ideas for a name.
 * @author Uli Schlachter
 */
abstract public class AbstractPetriNetIterableWrapper implements Iterable<PetriNet> {
	// An iterable providing various petri nets
	private final Iterable<PetriNet> iterable;

	/**
	 * Create a new instance of this class which gets petri nets from the given iterable.
	 * @param iterable the list of petri nets that should be used
	 */
	protected AbstractPetriNetIterableWrapper(Iterable<PetriNet> iterable) {
		this.iterable = iterable;
	}

	/**
	 * Given a petri net, this function should return an iterator which provides new nets that should be returned.
	 * @param pn The petri net that 'feeds' the generator.
	 * @return a new iterator.
	 */
	abstract protected Iterator<PetriNet> newSubIterator(PetriNet pn);

	@Override
	public Iterator<PetriNet> iterator() {
		return new Iterator<PetriNet>() {
			private final Iterator<PetriNet> netGenerator = iterable.iterator();
			private Iterator<PetriNet> subnetGenerator;

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				while (subnetGenerator == null || !subnetGenerator.hasNext()) {
					if (!netGenerator.hasNext())
						return false;

					PetriNet pn = netGenerator.next();
					subnetGenerator = newSubIterator(pn);
				}
				return true;
			}

			@Override
			public PetriNet next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return subnetGenerator.next();
			}
		};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
