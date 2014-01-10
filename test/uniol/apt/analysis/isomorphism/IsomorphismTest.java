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

package uniol.apt.analysis.isomorphism;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static uniol.apt.BestNetCollection.*;
import static uniol.apt.TestNetCollection.getNoTransitionOnePlaceNet;
import static uniol.apt.TestNetsForIsomorphism.*;

import java.io.IOException;

import org.testng.annotations.Test;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.module.exception.ModuleException;

/**
 * Collection of nets to test the isomorphism-module
 *
 * notation:
 * weak isomorphism: isomorphism, that ignores labels
 * strong isomorphism: isomorphism, that doesn't ignore labels
 *
 * @author Maike Schwammberger
 */
public class IsomorphismTest {

	/**
	 * Test strong isomorphism (which includes weak isomorphism)
	 *
	 * @param pn1
	 * @param pn2
	 * @throws UnboundedException
	 */
	private void testIsomorphism(PetriNet pn1, PetriNet pn2) throws UnboundedException {
		IsomorphismLogic logic = new IsomorphismLogic(pn1, pn2, false);
		assertTrue(logic.isIsomorphic());
		logic = new IsomorphismLogic(pn1, pn2, true);
		assertTrue(logic.isIsomorphic());
	}

	/**
	 * Test weak isomorphism, but non-strong isomorphism.
	 *
	 * @param pn1
	 * @param pn2
	 * @throws UnboundedException
	 */
	private void testWeakIsomorphism(PetriNet pn1, PetriNet pn2) throws UnboundedException {
		IsomorphismLogic logic = new IsomorphismLogic(pn1, pn2, false);
		assertTrue(logic.isIsomorphic());
		logic = new IsomorphismLogic(pn1, pn2, true);
		assertFalse(logic.isIsomorphic());
	}

	/**
	 * Test not non-weak isomorphism (which includes non-strong isomorphism)
	 *
	 * @param pn1
	 * @param pn2
	 * @throws UnboundedException
	 */
	private void testNonWeakIsomorphism(PetriNet pn1, PetriNet pn2) throws UnboundedException {
		IsomorphismLogic logic = new IsomorphismLogic(pn1, pn2, false);
		assertFalse(logic.isIsomorphic());
		logic = new IsomorphismLogic(pn1, pn2, true);
		assertFalse(logic.isIsomorphic());
	}

	//Tests for strong isomorphic nets:

	@Test
	public void testIsomorphicWithItSelf() throws IOException, ModuleException {
		testIsomorphism(getNet3A(), getNet3A());
	}

	@Test
	public void testIsomorphicNets1() throws IOException, ModuleException {
		testIsomorphism(getNet3A(), getNet3B());
	}

	/**
	 * The nets IsoNet1A and IsoNet1B are identically,
	 * except that the labels are exchanged.
	 *
	 * @throws IOException
	 * @throws ModuleException
	 */
	@Test
	public void testIsomorphicNets2() throws IOException, ModuleException {
		testIsomorphism(getIsoNet1A(), getIsoNet1B());
	}

	@Test
	public void testIsomorphicNets3() throws IOException, ModuleException {
		testIsomorphism(getIsoNet2A(), getIsoNet2B());
	}

	// Tests for non isomorphic nets (neither weak nor strong isomorphism):

	/**
	 * A net with one transition and no place isn't isomorphic to another
	 * net with one place and no transition.
	 *
	 * @throws IOException
	 * @throws ModuleException
	 */
	@Test
	public void testEmptyNet() throws IOException, ModuleException {
		testNonWeakIsomorphism(getOneTransitionNet(), getNoTransitionOnePlaceNet());
	}

	@Test
	public void testNonIsomorphicNets1() throws IOException, ModuleException {
		testNonWeakIsomorphism(getNet1A(), getNet1B());
	}

	@Test
	public void testNonIsomorphicNets2() throws IOException, ModuleException {
		testNonWeakIsomorphism(getNet2A(), getNet2B());
	}

	@Test
	public void testNonIsomorphicNets3() throws IOException, ModuleException {
		testNonWeakIsomorphism(getNet4A(), getNet4B());
	}

	//Tests for (non) weak isomorphism

	/**
	 * Test to check, if the initial nodes are mapped correctly
	 * (IsoNet3A and IsoNet4a have isomorphic reachability graphs,
	 * but their initial nodes don't map to each other)
	 *
	 * @throws IOException
	 * @throws ModuleException
	 */
	@Test
	public void testNonIsomorphicNets3Own() throws IOException, ModuleException {
		testWeakIsomorphism(getIsoNet3A(), getIsoNet3B());
	}

	/**
	 * IsoNet4A and IsoNet4B are "almost isomorphic". There is just a single edge of difference
	 * and that edge is not easily reachable, but needs four firings before it appears.
	 *
	 * @throws IOException
	 * @throws ModuleException
	 */
	@Test
	public void testNonIsomorphicNets4() throws IOException, ModuleException {
		testNonWeakIsomorphism(getIsoNet4A(), getIsoNet4B());
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
