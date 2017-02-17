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
 * Provides the Petri nets of the crash course.
 *
 * @author Manuel Gieseking
 */
public class CrashCourseNets {
	private CrashCourseNets() {
	}

	private static PetriNet getNet(String counter) {
		return ParserTestUtils.getAptPN("nets/crashkurs-cc" + counter + "-net.apt");
	}

	public static PetriNet getCCNet1() {
		return getNet("1");
	}

	public static PetriNet getCCNet2() {
		return getNet("2");
	}

	public static PetriNet getCCNet2inf() {
		return getNet("2inf");
	}

	public static PetriNet getCCNet3() {
		return getNet("3");
	}

	public static PetriNet getCCNet4() {
		return getNet("4");
	}

	public static PetriNet getCCNet5() {
		return getNet("5");
	}

	public static PetriNet getCCNet6() {
		return getNet("6");
	}

	public static PetriNet getCCNet7() {
		return getNet("7");
	}

	public static PetriNet getCCNet8() {
		return getNet("8");
	}

	public static PetriNet getCCNet9() {
		return getNet("9");
	}

	public static PetriNet getCCNet10() {
		return getNet("10");
	}

	public static PetriNet getCCNet11() {
		return getNet("11");
	}

	public static PetriNet getCCNet12() {
		return getNet("12");
	}

	public static PetriNet getCCNet13() {
		return getNet("13");
	}

	public static PetriNet getCCNet14() {
		return getNet("14");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
