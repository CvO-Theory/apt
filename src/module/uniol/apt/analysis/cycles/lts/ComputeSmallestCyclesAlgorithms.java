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
 *
 * @author vsp
 */
public class ComputeSmallestCyclesAlgorithms {
	private ComputeSmallestCyclesAlgorithms() { /* hide constructor */ }

	/**
	 * Get the default implementation
	 * @return {@link ComputeSmallestCycles} instance using the default algorithm
	 */
	public static ComputeSmallestCycles getDefaultAlgorithm() {
		return new ComputeSmallestCyclesDFS();
	}

	/**
	 * Get the character which selects the default implementation in {@link #getAlgorithm(char)}.
	 * @return Character which selects the default implementation
	 */
	public static char getDefaultAlgorithmChar() {
		return 'd';
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

	/**
	 * Get a specific implementation of the {@link ComputeSmallestCycles} interface
	 *
	 * @param selection (User) input to select the algorithm
	 * @return {@link ComputeSmallestCycles} instance which uses the requested algorithm
	 */
	public static ComputeSmallestCycles getAlgorithm(char selection) {
		ComputeSmallestCycles algo;
		switch (selection) {
			case 'f':
				algo = new ComputeSmallestCyclesFloydWarshall();
				break;
			case 'd':
				algo = new ComputeSmallestCyclesDFS();
				break;
			case 'j':
				algo = new ComputeSmallestCyclesJohnson();
				break;
			default:
				throw new IllegalArgumentException("Unknown algorithm requested");
		}
		return algo;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
