/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016       vsp
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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import uniol.apt.adt.PetriNetOrTransitionSystem;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.module.impl.ModuleInvoker;

import static uniol.apt.TestTSCollection.*;

/** @author vsp */
public class PrimeCyclePropertyTest {
	private boolean testLTS(TransitionSystem ts) {
		return new PrimeCycleProperty().check(ts) == null;
	}

	private boolean testLTSModule(TransitionSystem ts) throws Exception {
		PrimeCyclePropertyModule module = new PrimeCyclePropertyModule();
		ModuleInvoker invoker = new ModuleInvoker();
		return (Boolean) (invoker.invoke(module, new PetriNetOrTransitionSystem(ts)).get(0));
	}

	@DataProvider(name = "goodLTS")
	private Object[][] createGoodTestLTS() {
		return new Object[][]{
			{getSingleStateTS()},
			{getThreeStatesTwoEdgesTS()},
			{getSingleStateLoop()},
			{getReversibleTS()},
			{getDifferentCyclesTS()},
			{getOneCycleLTS()},
			{getPathTS()},
			{getPureSynthesizablePathTS()},
			{getImpureSynthesizablePathTS()},
			{getTwoBThreeATS()},
			{getABandA()},
			{getABandB()},
			{getABandBUnfolded()},
			{getPlainTNetReachabilityTS()},
		};
	}

	@DataProvider(name = "badLTS")
	private Object[][] createBadTestLTS() {
		return new Object[][]{
			{getTwoStateCycleSameLabelTS()},
			{getDeterministicReachableReversibleNonPersistentTS()},
		};
	}

	@Test(dataProvider = "goodLTS")
	public void testGoodLTS(TransitionSystem ts) throws Exception {
		assertTrue(testLTS(ts), "Examining LTS " + ts.getName() + " which should be good");
		assertTrue(testLTSModule(ts), "Examining LTS " + ts.getName() + " which should be good");
	}

	@Test(dataProvider = "badLTS")
	public void testBadLTS(TransitionSystem ts) throws Exception {
		assertFalse(testLTS(ts), "Examining LTS " + ts.getName() + " which should fail");
		assertFalse(testLTSModule(ts), "Examining LTS " + ts.getName() + " which should fail");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
