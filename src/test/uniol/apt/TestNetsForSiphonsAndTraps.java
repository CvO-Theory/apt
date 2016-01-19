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
import uniol.apt.io.parser.ParserTestUtils;

/**
 * Collection of pn to test the module for traps and siphons
 *
 * @author Maike Schwammberger
 */
public class TestNetsForSiphonsAndTraps {

	/**
	 * Private constructor to prevent instantiation of this class
	 */
	private TestNetsForSiphonsAndTraps() {
	}

	/**
	 * Method to parse nets in folder "nets/siphon-and-trap-nets/" easier.
	 *
	 * @param prefixnet Name of wanted net
	 * @return wanted net
	 */
	private static PetriNet getNet(String prefixnet) {
		return ParserTestUtils.getAptPN("nets/siphon-and-trap-nets/" + prefixnet + ".apt");
	}

	/**
	 * Method to parse nets in folder "nets/eb-nets/trap-siphon-linalg/" easier.
	 *
	 * @param prefixnet Name of wanted net
	 * @return wanted net
	 */
	private static PetriNet getBestNet(String prefixnet) {
		return ParserTestUtils.getAptPN("nets/eb-nets/trap-siphon-linalg/" + prefixnet + ".apt");
	}

	/**
	 * Net with isolated element s3
	 *
	 * @return PN:
	 * <pre>
	 * {@code
	 * s0 -> a -> s1    s2        s3
	 * ^          |      ^
	 * |          v      |
	 * |--------- b -----|
	 * }
	 * </pre>
	 */
	public static PetriNet getIsolatedElementNet() {
		return getNet("isolated-element-net");
	}

	/**
	 * This is the dual net to duality-test-net2.apt
	 *
	 * @return PN:
	 * <pre>
	 * {@code
	 * s0 -> a -> s1 -> b -> s2
	 * }
	 * </pre>
	 */
	public static PetriNet getDualityTestNet1() {
		return getNet("duality-test-net1");
	}

	/**
	 * This is the dual net to duality-test-net1.apt
	 *
	 * @return PN:
	 * <pre>
	 * {@code
	 * s0 <- a <- s1 <- b <- s2
	 * }
	 * </pre>
	 */
	public static PetriNet getDualityTestNet2() {
		return getNet("duality-test-net2");
	}

	/**
	 * @return PN:
	 * <pre>
	 * {@code
	 * c <----------
	 * |           |
	 * v           |
	 * s1 -> a -> s3 <- b <- s2
	 * ^     |
	 * |_____|
	 * }
	 * </pre>
	 */
	public static PetriNet getSiphonAndTrapNet() {
		return getNet("siphon-and-trap-net");
	}

	/**
	 * Net where the total set is no siphon.
	 *
	 * @return PN:
	 * <pre>
	 * {@code
	 * a -> s0
	 * }
	 * </pre>
	 */
	public static PetriNet getNetWithNoTotalSiphon() {
		return getNet("siphon-test-net");
	}

	/**
	 * Net where the total set is no trap.
	 *
	 * @return PN:
	 * <pre>
	 * {@code
	 * s0 -> a
	 * }
	 * </pre>
	 */
	public static PetriNet getNetWithNoTotalTrap() {
		return getNet("trap-test-net");
	}

	/**
	 * This petri net is from the petri net course in 2011 by Eike Best, assignment 17 (Paper #6).
	 *
	 * @return PN:
	 * <pre>
	 * {@code
	 * p1->t1          t5<-s5
	 * ^   |  ^      /^^   ^
	 * |   v    \   / /|   |
	 * |   p2    p0< / s6  |
	 * |   |     ^ \ / ^   |
	 * |   v    /   /\ |   |
	 * t4  t2   /   / >t6  t8
	 * ^   | ^  /   /  |   ^
	 * |   v   x   /   v   |
	 * |   p3 / \p9    s7  |
	 * |   |  / ^      |   |
	 * |   v / /       v   |
	 * t4<-t3          t7->s8
	 * }
	 * </pre>
	 */
	public static PetriNet getNonFCTrapSiphonNet() {
		return getNet("non-fc-net");
	}

	/**
	 * This petri net is from the petri net course in 2011 by Eike Best, assignment 18 (Paper #6).
	 *
	 * @return PN:
	 * <pre>
	 * {@code
	 * p1->t1->p2<-t2<-p3
	 * ^   ^   |       ^
	 * |   |   v       |
	 * t3  p4  t4<-p5<-t5
	 * ^   ^   |       ^
	 * |   |   v       |
	 * p6<-t6<-p7->t7->p8
	 * }
	 * </pre>
	 */
	public static PetriNet getFCTrapSiphonNet() {
		return getBestNet("traps-siphons-1-net");
	}

	/**
	 * Totally connected net
	 *
	 * @return PN
	 */
	public static PetriNet getTotallyConnectedNet() {
		return getNet("totally-connected-net");
	}

	/**
	 * Not connected net
	 *
	 * @return PN:
	 * <pre>
	 * s0    a
	 * s1    b
	 * s2    c
	 * </pre>
	 */
	public static PetriNet getNotConnectedNet() {
		return getNet("not-connected-net");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
