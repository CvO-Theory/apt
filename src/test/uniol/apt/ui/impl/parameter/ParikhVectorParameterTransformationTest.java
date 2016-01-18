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

package uniol.apt.ui.impl.parameter;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.module.exception.ModuleException;

/**
 * @author Renke Grunwald
 *
 */
public class ParikhVectorParameterTransformationTest {
	private TransitionSystem ts;
	private ParikhVectorParameterTransformation pvTransformation;

	@BeforeClass
	public void createTS() {
		ts = new TransitionSystem();

		State s1 = ts.createState();
		State s2 = ts.createState();
		State s3 = ts.createState();

		ts.createArc(s1, s2, "a");
		ts.createArc(s2, s3, "b");
		ts.createArc(s3, s1, "c");
	}

	@BeforeClass
	public void createTransformation() {
		pvTransformation = new ParikhVectorParameterTransformation();
	}

	@Test
	public void testSimplePV() throws ModuleException {
		ParikhVector pv = pvTransformation.transform("{a=1,b=2}");
		pv.connectToTransitionSystem(ts);

		assertEquals(pv.getPV().get("a"), (Integer) 1);
		assertEquals(pv.getPV().get("b"), (Integer) 2);
	}

	@Test
	public void testPVWithSpaces() throws ModuleException {
		ParikhVector pv = pvTransformation.transform("{a = 1 , b = 2}");
		pv.connectToTransitionSystem(ts);

		assertEquals(pv.getPV().get("a"), (Integer) 1);
		assertEquals(pv.getPV().get("b"), (Integer) 2);
		assertEquals(pv.getPV().get("c"), (Integer) 0);
	}

	@Test
	public void testEmptyPV() throws ModuleException {
		ParikhVector pv = pvTransformation.transform("{}");
		pv.connectToTransitionSystem(ts);

		assertEquals(pv.getPV().get("a"), (Integer) 0);
		assertEquals(pv.getPV().get("b"), (Integer) 0);
		assertEquals(pv.getPV().get("c"), (Integer) 0);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
