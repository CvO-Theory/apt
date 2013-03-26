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

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import uniol.apt.adt.pn.PetriNet;

import static uniol.apt.TestNetCollection.*;

import uniol.apt.module.exception.ModuleException;

/** @author vsp */
@Test
public class APTRendererTest {
	// Please only add tests to this class after you verified that our parser can actually parse them!

	private String render(PetriNet pn) throws ModuleException {
		return new APTRenderer().render(RendererTestUtils.getSortedNet(pn));
	}

	@Test
	public void testTokenGeneratorNet() throws Exception {
		assertEquals(render(getTokenGeneratorNet()), ".name \"TokenGeneratorNet\"\n.type LPN\n\n" +
				".places\np1\n\n.transitions\nt1\n\n" +
				".flows\nt1: {} -> {1*p1}\n\n.initial_marking {}\n");
	}

	@Test
	public void testDeadlockNet() throws Exception {
		assertEquals(render(getDeadlockNet()), ".name \"DeadlockNet\"\n.type LPN\n\n" +
				".places\np1\n\n.transitions\nt1\nt2\n\n" +
				".flows\nt1: {1*p1} -> {}\nt2: {1*p1} -> {}\n\n" +
				".initial_marking {1*p1}\n");
	}

	@Test
	public void testNonPersistentNet() throws Exception {
		assertEquals(render(getNonPersistentNet()), ".name \"NonPersistentNet\"\n.type LPN\n\n" +
				".places\np1\np2\n\n.transitions\na\nb\nc\n\n" +
				".flows\na: {1*p1} -> {1*p2}\nb: {1*p1} -> {1*p2}\n" +
				"c: {1*p2} -> {1*p1}\n\n.initial_marking {1*p1}\n");
	}

	@Test
	public void checkPersistentBiCFNet() throws Exception {
		assertEquals(render(getPersistentBiCFNet()), ".name \"PersistentBiCFNet\"\n.type LPN\n\n" +
				".places\np1\np2\np3\np4\np5\n\n" +
				".transitions\na\nb\nc\nd\n\n" +
				".flows\na: {1*p1, 1*p3} -> {1*p2}\n" +
				"b: {1*p3, 1*p5} -> {1*p4}\n" +
				"c: {1*p2} -> {1*p1, 1*p3}\n" +
				"d: {1*p4} -> {1*p3, 1*p5}\n\n" +
				".initial_marking {1*p1, 2*p3, 1*p5}\n");
	}

	@Test
	public void testConcurrentDiamondNet() throws Exception {
		assertEquals(render(getConcurrentDiamondNet()), ".name \"ConcurrentDiamondNet\"\n.type LPN\n\n" +
				".places\np1\np2\n\n" +
				".transitions\nt1\nt2\n\n" +
				".flows\nt1: {1*p1} -> {}\nt2: {1*p2} -> {}\n\n" +
				".initial_marking {1*p1, 1*p2}\n");
	}

	@Test
	public void testConflictingDiamondNet() throws Exception {
		assertEquals(render(getConflictingDiamondNet()), ".name \"ConflictingDiamondNet\"\n.type LPN\n\n" +
				".places\np1\np2\np3\n\n" +
				".transitions\nt1\nt2\n\n" +
				".flows\nt1: {1*p1, 1*p3} -> {1*p3}\n" +
				"t2: {1*p2, 1*p3} -> {1*p3}\n\n" +
				".initial_marking {1*p1, 1*p2, 1*p3}\n");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
