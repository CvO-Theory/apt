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

package uniol.apt.analysis.lts.extension;

import java.util.BitSet;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/** @author Vincent Göbel */
public class ExtendTransitionSystemTest {

	String persistentOne = "{2, 14, 19, 25, 28}";
	String persistentTwo = "{2, 14, 23, 25, 28}";

	String nonPersistentOne = "{3, 23, 41, 47, 59, 61, 65}";
	String nonPersistentTwo = "{3, 17, 23, 29, 41, 47, 61, 65}";

	String oneStateNetOne = "{0}"; //Its the same because its already valid.
	String oneStateNetTwo = "{0, 1, 2}";

	/**
	 * Test persistent transition system for next two valid transition systems.
	 * Valid is defined as: persistent, reversible and all smallest cycles have
	 * the same parikh vector.
	 */
	@Test
	public void persistentTest() {
		TransitionSystem ts = TestTSCollection.getPersistentTS();

		ExtendTransitionSystem ext = new ExtendTransitionSystem(ts, 0);
		ext.findNextValid();
		BitSet code = ext.getLastGenerated();
		assertEquals(code.toString(), persistentOne);
		ext.findNextValid(code);
		code = ext.getLastGenerated();
		assertEquals(code.toString(), persistentTwo);

		ts = TestTSCollection.getNonPersistentTS();
		ext = new ExtendTransitionSystem(ts, 0);
		ext.findNextValid();
		code = ext.getLastGenerated();
		assertEquals(code.toString(), nonPersistentOne);
		ext.findNextValid(code);
		code = ext.getLastGenerated();
		assertEquals(code.toString(), nonPersistentTwo);

		ts = getTS();
		ext = new ExtendTransitionSystem(ts, 1);
		ext.findNextValid();
		code = ext.getLastGenerated();
		assertEquals(code.toString(), oneStateNetOne);
		ext.findNextValid(code);
		code = ext.getLastGenerated();
		assertEquals(code.toString(), oneStateNetTwo);
	}


	private TransitionSystem getTS() {
		TransitionSystem ts = new TransitionSystem();

		State s0 = ts.createState("s0");

		ts.setInitialState(s0);

		ts.createArc(s0, s0, "a");
		return ts;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
