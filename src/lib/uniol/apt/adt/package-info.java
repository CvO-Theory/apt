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

/**
 * Implementation of various abstract data types. This package contains a generic interface for graphs called {@link
 * IGraph}. Based on this, the {@link uniol.apt.adt.pn.PetriNet} and {@link uniol.apt.adt.ts.TransitionSystem}
 * classes are implemented. Additionally, there is {@link uniol.apt.adt.automaton.FiniteAutomaton} representing a finite
 * automaton. This class does not implement the {@link IGraph} interface.
 */
package uniol.apt.adt;

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
