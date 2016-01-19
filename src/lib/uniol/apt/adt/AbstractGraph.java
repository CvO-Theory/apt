/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

package uniol.apt.adt;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import uniol.apt.adt.extension.Extensible;

/**
 * {@link AbstractGraph} is an abstract implementation of the {@link IGraph} interface. It extends {@link
 * Extensible} and adds an implementation of the listener methods. To invoke listeners, the {@link invokeListeners}
 * method can be called.
 * @param <G> The type of the graph itself.
 * @param <E> The type of the edges.
 * @param <N> The type of the nodes.
 * @author Uli Schlachter
 */
public abstract class AbstractGraph<G extends IGraph<G, E, N>, E extends IEdge<G, E, N>, N extends INode<G, E, N>>
	extends Extensible implements IGraph<G, E, N> {
	private final Set<IGraphListener<G, E, N>> listeners = new HashSet<>();

	@Override
	public boolean addListener(IGraphListener<G, E, N> listener) {
		return listeners.add(listener);
	}

	@Override
	public boolean removeListener(IGraphListener<G, E, N> listener) {
		return listeners.remove(listener);
	}

	/**
	 * This method invokes all listeners and remove those that request to be removed.
	 */
	protected void invokeListeners() {
		Iterator<IGraphListener<G, E, N>> iter = listeners.iterator();
		while (iter.hasNext()) {
			IGraphListener<G, E, N> listener = iter.next();
			if (!listener.changeOccurred(this))
				iter.remove();
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
