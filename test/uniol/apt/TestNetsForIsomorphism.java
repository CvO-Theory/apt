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

package uniol.apt;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.ParserTestUtils;


/**
 * Test nets to test isomorphism-module
 *
 * @author Maike Schwammberger
 *
 */
public class TestNetsForIsomorphism {

	/**
	 * Private constructor to prevent instantiation of this class
	 */
	private TestNetsForIsomorphism() { /* empty */ }

	/**
	 * Method to parse nets in folder "nets/isomorphism-nets/" easier.
	 *
	 * @param prefixnet Name of wanted net
	 * @return wanted net
	 */
	private static PetriNet getIsoNet(String prefixnet) {
		return ParserTestUtils.getAptPN("nets/isomorphism-nets/" + prefixnet + ".apt");
	}

	/**
	 * Method to parse ts in folder "nets/isomorphism-nets/" easier.
	 *
	 * @param prefixnet Name of wanted ts
	 * @return wanted ts
	 */
	private static TransitionSystem getIsoTs(String prefixnet) {
		return ParserTestUtils.getAptLTS("nets/isomorphism-nets/" + prefixnet + "-aut.apt");
	}

	/**
	 * Method to parse nets in folder "nets/" easier.
	 *
	 * @param prefixnet Name of wanted net
	 * @return wanted net
	 */
	private static PetriNet getOtherNet(String prefixnet) {
		return ParserTestUtils.getAptPN("nets/" + prefixnet + ".apt");
	}

	public static PetriNet getIsoNet1A() {
		return getIsoNet("iso-net-1A");
	}

	public static TransitionSystem getIsoTs1A() {
		return getIsoTs("iso-net-1A");
	}

	public static PetriNet getIsoNet1B() {
		return getIsoNet("iso-net-1B");
	}

	public static TransitionSystem getIsoTs1B() {
		return getIsoTs("iso-net-1B");
	}

	public static PetriNet getIsoNet2A() {
		return getIsoNet("iso-net-2A");
	}

	public static TransitionSystem getIsoTs2A() {
		return getIsoTs("iso-net-2A");
	}

	public static PetriNet getIsoNet2B() {
		return getIsoNet("iso-net-2B");
	}

	public static TransitionSystem getIsoTs2B() {
		return getIsoTs("iso-net-2B");
	}

	public static PetriNet getIsoNet3A() {
		return getIsoNet("iso-net-3A");
	}

	public static TransitionSystem getIsoTs3A() {
		return getIsoTs("iso-net-3A");
	}

	public static PetriNet getIsoNet3B() {
		return getIsoNet("iso-net-3B");
	}

	public static TransitionSystem getIsoTs3B() {
		return getIsoTs("iso-net-3B");
	}

	public static PetriNet getIsoNet4A() {
		return getIsoNet("iso-net-4A");
	}

	public static TransitionSystem getIsoTs4A() {
		return getIsoTs("iso-net-4A");
	}

	public static PetriNet getIsoNet4B() {
		return getIsoNet("iso-net-4B");
	}

	public static TransitionSystem getIsoTs4B() {
		return getIsoTs("iso-net-4B");
	}

	public static PetriNet getOneTransitionNet() {
		return getOtherNet("one-transition-net");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
