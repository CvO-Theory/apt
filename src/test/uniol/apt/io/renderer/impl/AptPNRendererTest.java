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

package uniol.apt.io.renderer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.TestNetCollection.*;
import static uniol.apt.io.matcher.StringEqualsIgnoringLineEndings.equalsIgnoringLineEndings;

import org.testng.annotations.Test;

import uniol.apt.adt.extension.ExtensionProperty;
import uniol.apt.adt.pn.PetriNet;

/** @author vsp */
public class AptPNRendererTest {
	// Please only add tests to this class after you verified that our parser can actually parse them!

	private String render(PetriNet pn) throws Exception {
		return new AptPNRenderer().render(RendererTestUtils.getSortedNet(pn));
	}

	@Test
	public void testTokenGeneratorNet() throws Exception {
		String expected = ".name \"TokenGeneratorNet\"\n.type LPN\n\n" +
				".places\np1\n\n.transitions\nt1\n\n" +
				".flows\nt1: {} -> {1*p1}\n\n.initial_marking {}";
		assertThat(render(getTokenGeneratorNet()), equalsIgnoringLineEndings(expected));
	}

	@Test
	public void testDeadlockNet() throws Exception {
		String expected = ".name \"DeadlockNet\"\n.type LPN\n\n" +
				".places\np1\n\n.transitions\nt1\nt2\n\n" +
				".flows\nt1: {1*p1} -> {}\nt2: {1*p1} -> {}\n\n" +
				".initial_marking {1*p1}";
		assertThat(render(getDeadlockNet()), equalsIgnoringLineEndings(expected));
	}

	@Test
	public void testNonPersistentNet() throws Exception {
		String expected = ".name \"NonPersistentNet\"\n.type LPN\n\n" +
				".places\np1\np2\n\n.transitions\na\nb\nc\n\n" +
				".flows\na: {1*p1} -> {1*p2}\nb: {1*p1} -> {1*p2}\n" +
				"c: {1*p2} -> {1*p1}\n\n.initial_marking {1*p1}";
		assertThat(render(getNonPersistentNet()), equalsIgnoringLineEndings(expected));
	}

	@Test
	public void checkPersistentBiCFNet() throws Exception {
		String expected = ".name \"PersistentBiCFNet\"\n.type LPN\n\n" +
				".places\np1\np2\np3\np4\np5\n\n" +
				".transitions\na\nb\nc\nd\n\n" +
				".flows\na: {1*p1, 1*p3} -> {1*p2}\n" +
				"b: {1*p3, 1*p5} -> {1*p4}\n" +
				"c: {1*p2} -> {1*p1, 1*p3}\n" +
				"d: {1*p4} -> {1*p3, 1*p5}\n\n" +
				".initial_marking {1*p1, 2*p3, 1*p5}";
		assertThat(render(getPersistentBiCFNet()), equalsIgnoringLineEndings(expected));
	}

	@Test
	public void testConcurrentDiamondNet() throws Exception {
		String expected = ".name \"ConcurrentDiamondNet\"\n.type LPN\n\n" +
				".places\np1\np2\n\n" +
				".transitions\nt1\nt2\n\n" +
				".flows\nt1: {1*p1} -> {}\nt2: {1*p2} -> {}\n\n" +
				".initial_marking {1*p1, 1*p2}";
		assertThat(render(getConcurrentDiamondNet()), equalsIgnoringLineEndings(expected));
	}

	@Test
	public void testConflictingDiamondNet() throws Exception {
		String expected = ".name \"ConflictingDiamondNet\"\n.type LPN\n\n" +
				".places\np1\np2\np3\n\n" +
				".transitions\nt1\nt2\n\n" +
				".flows\nt1: {1*p1, 1*p3} -> {1*p3}\n" +
				"t2: {1*p2, 1*p3} -> {1*p3}\n\n" +
				".initial_marking {1*p1, 1*p2, 1*p3}";
		assertThat(render(getConflictingDiamondNet()), equalsIgnoringLineEndings(expected));
	}

	@Test
	public void testEmptyNetWithOption() throws Exception {
		String expected = ".name \"\"\n.type LPN\n.options\nfortytwo=\"42\"\n\n.places\n\n.transitions\n\n" +
			".flows\n\n.initial_marking {}";
		PetriNet pn = new PetriNet();
		pn.putExtension("fortytwo", "42", ExtensionProperty.WRITE_TO_FILE);
		assertThat(render(pn), equalsIgnoringLineEndings(expected));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
