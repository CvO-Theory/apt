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
 * Classes for systematic generation of {@link uniol.apt.adt.pn.PetriNet}s and {@link
 * uniol.apt.adt.ts.TransitionSystem}s. For example, {@link uniol.apt.generator.cycle.CycleGenerator} creates a {@link
 * uniol.apt.adt.pn.PetriNet} where tokens are moved from one place to another in a cyclic fashion.
 */
package uniol.apt.generator;

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
