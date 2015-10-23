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
 * Provides the Petri nets of package eb-nets.
 *
 * @author Raffaela Ferrari
 *
 */
public class BestNetCollection {

	/**
	 * Private constructor to prevent instantiation of this class
	 */
	private BestNetCollection() { /* empty */ }

	private static PetriNet getNet(String packageName, String prefixnet) {
		return ParserTestUtils.getAptPN("nets/eb-nets" + "/" + packageName + "/" + prefixnet + "-net.apt");
	}

	private static TransitionSystem getTs(String packageName, String prefixnet) {
		return ParserTestUtils.getAptLTS("nets/eb-nets" + "/" + packageName + "/" + prefixnet + "-lts.apt");
	}

	public static PetriNet getNet1A() {
		return getNet("aeq-iso-bisim", "1a");
	}

	public static TransitionSystem getTs1A() {
		return getTs("aeq-iso-bisim", "1a");
	}

	public static PetriNet getNet1B() {
		return getNet("aeq-iso-bisim", "1b");
	}

	public static TransitionSystem getTs1B() {
		return getTs("aeq-iso-bisim", "1b");
	}

	public static PetriNet getNet2A() {
		return getNet("aeq-iso-bisim", "2a");
	}

	public static TransitionSystem getTs2A() {
		return getTs("aeq-iso-bisim", "2a");
	}

	public static PetriNet getNet2B() {
		return getNet("aeq-iso-bisim", "2b");
	}

	public static TransitionSystem getTs2B() {
		return getTs("aeq-iso-bisim", "2b");
	}

	public static PetriNet getNet3A() {
		return getNet("aeq-iso-bisim", "3a");
	}

	public static TransitionSystem getTs3A() {
		return getTs("aeq-iso-bisim", "3a");
	}

	public static PetriNet getNet3B() {
		return getNet("aeq-iso-bisim", "3b");
	}

	public static TransitionSystem getTs3B() {
		return getTs("aeq-iso-bisim", "3b");
	}

	public static PetriNet getNet4A() {
		return getNet("aeq-iso-bisim", "4a");
	}

	public static TransitionSystem getTs4A() {
		return getTs("aeq-iso-bisim", "4a");
	}

	public static PetriNet getNet4B() {
		return getNet("aeq-iso-bisim", "4b");
	}

	public static TransitionSystem getTs4B() {
		return getTs("aeq-iso-bisim", "4b");
	}

	public static PetriNet getNetEB1() {
		return getNet("basic", "eb1");
	}

	public static PetriNet getNetEB2() {
		return getNet("basic", "eb2");
	}

	public static PetriNet getNetEB3A() {
		return getNet("basic", "eb3a");
	}

	public static PetriNet getNetEB3() {
		return getNet("basic", "eb3");
	}

	public static PetriNet getNetEB4() {
		return getNet("basic", "eb4");
	}

	public static PetriNet getNetPN1A() {
		return getNet("basic", "pn1a");
	}

	public static PetriNet getNetPN1B() {
		return getNet("basic", "pn1b");
	}

	public static PetriNet getNetPN2() {
		return getNet("basic", "pn2");
	}

	public static PetriNet getNetPN3() {
		return getNet("basic", "pn3");
	}

	public static PetriNet getNetCover1() {
		return getNet("cover", "cover1");
	}

	public static PetriNet getNetCover2() {
		return getNet("cover", "cover2");
	}

	public static PetriNet getNetDistrFig12() {
		return getNet("distr", "distr-fig12");
	}

	public static PetriNet getNetDistrFig13() {
		return getNet("distr", "distr-fig13");
	}

	public static PetriNet getNetDistrFig14() {
		return getNet("distr", "distr-fig14");
	}

	public static PetriNet getNetDistrFig19A() {
		return getNet("distr", "distr-fig19a");
	}

	public static PetriNet getNetDistrFig19B() {
		return getNet("distr", "distr-fig19b");
	}

	public static PetriNet getNetParikhNon1Alt() {
		return getNet("distr", "parikh-non-1-alt");
	}

	public static PetriNet getNetDistrParikhNon1() {
		return getNet("distr", "parikh-non-1");
	}

	public static PetriNet getNetFC1A() {
		return getNet("fc", "fc1a");
	}

	public static PetriNet getNetFC1B() {
		return getNet("fc", "fc1b");
	}

	public static PetriNet getNetNoHomestate1() {
		return getNet("fc", "no-homestate-1");
	}

	public static PetriNet getNetTNet1() {
		return getNet("fc", "tnet1");
	}

	public static PetriNet getNet32() {
		return getNet("more", "3-2");
	}

	public static PetriNet getNetPersParikhNon1() {
		return getNet("pers", "parikh-non-1");
	}

	public static PetriNet getNetParikhNon1Nonpers() {
		return getNet("pers", "parikh-non-1-nonpers");
	}

	public static PetriNet getNetPersFig1() {
		return getNet("pers", "pers-fig1");
	}

	public static PetriNet getNetPersFig2() {
		return getNet("pers", "pers-fig2");
	}

	public static PetriNet getNetPersFig4() {
		return getNet("pers", "pers-fig4");
	}

	public static PetriNet getNetPersFig5() {
		return getNet("pers", "pers-fig5");
	}

	public static PetriNet getNetSepBasic1() {
		return getNet("sep", "sep-basic1");
	}

	public static PetriNet getNetSepBasic2() {
		return getNet("sep", "sep-basic2");
	}

	public static PetriNet getNetSepBasic3() {
		return getNet("sep", "sep-basic3");
	}

	public static PetriNet getNetSepBasic4() {
		return getNet("sep", "sep-basic4");
	}

	public static PetriNet getNetSepBasic5() {
		return getNet("sep", "sep-basic5");
	}

	public static PetriNet getNetSepFc() {
		return getNet("sep", "sep-fc");
	}

	public static PetriNet getNetSepFiFig8AAuseinandergezogen() {
		return getNet("sep", "sep-fi.fig8a-auseinandergezogen");
	}

	public static PetriNet getNetSepFiFig8A() {
		return getNet("sep", "sep-fi-fig8a");
	}

	public static PetriNet getNetSepFiFig9() {
		return getNet("sep", "sep-fi-fig9");
	}

	public static PetriNet getNetSepRedK1() {
		return getNet("sep", "sep-red-k-1");
	}

	public static PetriNet getNetSepWeakButNotStrong() {
		return getNet("sep", "sep-weak-but-not-strong");
	}

	public static PetriNet getNetNoSinvCover() {
		return getNet("trap-siphon-linalg", "no-sinv-cover");
	}

	public static PetriNet getNetTrapsSiphons1() {
		return getNet("trap-siphon-linalg", "traps-siphons-1");
	}

	public static PetriNet getNetTrapsSiphons2() {
		return getNet("trap-siphon-linalg", "traps-siphons-2");
	}

	public static PetriNet getNetTrapsSiphons3() {
		return getNet("trap-siphon-linalg", "traps-siphons-3");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
