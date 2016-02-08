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

package uniol.apt.analysis.cycles.lts;

/**
 * Factory for {@link ComputeSmallestCycles} instances
 * @author vsp, Uli Schlachter
 */
public enum ComputeSmallestCyclesAlgorithms {
	FLOYD_WARSHALL('f') {
		@Override
		public ComputeSmallestCycles getInstance() {
			return new ComputeSmallestCyclesFloydWarshall();
		}
	},
	DFS('d') {
		@Override
		public ComputeSmallestCycles getInstance() {
			return new ComputeSmallestCyclesDFS();
		}
	},
	JOHNSON('j') {
		@Override
		public ComputeSmallestCycles getInstance() {
			return new ComputeSmallestCyclesJohnson();
		}
	};

	private final char character;

	ComputeSmallestCyclesAlgorithms(char character) {
		this.character = character;
	}

	/**
	 * Get the character which selects this algorithm
	 * @return Character which selects this algorithm
	 */
	public char getChar() {
		return character;
	}

	public abstract ComputeSmallestCycles getInstance();

	/**
	 * Get the default algorithm.
	 * @return The default algorithm
	 */
	public static ComputeSmallestCyclesAlgorithms getDefaultAlgorithm() {
		return JOHNSON;
	}

	/**
	 * Get a description of the meaning of the char parameter
	 * @return description
	 */
	public static String getAlgorithmCharDescription() {
		return "Select the algorithm to use:"
			+ " 'd' selects an depth first search algorithm;"
			+ " 'f' selects an adapted Floyd-Warshall algorithm;"
			+ " 'j' selects Johnson's algorithm";
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
