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

package uniol.apt.analysis.coverability;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class is used for iterating over the edges of a coverability graph.
 * @author Uli Schlachter, vsp
 */
class CoverabilityGraphEdgesIterator implements Iterator<CoverabilityGraphEdge> {
	private final Iterator<CoverabilityGraphNode> nodeIter;
	private Iterator<CoverabilityGraphEdge> edgeIter;

	/**
	 * Constructor
	 * @param cover {@link CoverabilityGraph} to iterate over
	 */
	CoverabilityGraphEdgesIterator(CoverabilityGraph cover) {
		nodeIter = cover.getNodes().iterator();
		if (nodeIter.hasNext())
			edgeIter = nodeIter.next().getPostsetEdges().iterator();
	}

	/**
	 * This function updates the edgeIter member. It will either have a valid next element after this function or it
	 * will be null if all edges were iterated over.
	 * @return True if the nodeIter has a next element.
	 */
	private boolean updateEdgeIter() {
		while (!edgeIter.hasNext()) {
			if (nodeIter.hasNext()) {
				edgeIter = nodeIter.next().getPostsetEdges().iterator();
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean hasNext() {
		return updateEdgeIter();
	}

	@Override
	public CoverabilityGraphEdge next() {
		if (updateEdgeIter())
			return edgeIter.next();
		throw new NoSuchElementException();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}


// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
