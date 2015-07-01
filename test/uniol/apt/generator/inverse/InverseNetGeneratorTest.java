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

package uniol.apt.generator.inverse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static uniol.apt.TestNetCollection.getABCLanguageNet;
import static uniol.apt.TestNetCollection.getConcurrentDiamondNet;
import static uniol.apt.TestNetCollection.getConflictingDiamondNet;
import static uniol.apt.TestNetCollection.getDeadlockNet;
import static uniol.apt.TestNetCollection.getEmptyNet;
import static uniol.apt.TestNetCollection.getNoTransitionOnePlaceNet;
import static uniol.apt.TestNetCollection.getNonPersistentNet;
import static uniol.apt.TestNetCollection.getOneTransitionNoPlaceNet;
import static uniol.apt.TestNetCollection.getPersistentBiCFNet;
import static uniol.apt.TestNetCollection.getTokenGeneratorNet;
import static uniol.apt.adt.matcher.Matchers.flowThatConnects;
import static uniol.apt.adt.matcher.Matchers.nodeWithID;

import java.util.ArrayList;
import java.util.Collection;

import org.hamcrest.Matcher;
import org.testng.annotations.Test;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.generator.cycle.CycleGenerator;
import uniol.apt.generator.philnet.BistatePhilNetGenerator;
import uniol.apt.generator.philnet.QuadstatePhilNetGenerator;
import uniol.apt.generator.philnet.TristatePhilNetGenerator;
import uniol.tests.dataprovider.IntRangeDataProvider;
import uniol.tests.dataprovider.annotations.IntRangeParameter;

/** @author Uli Schlachter */
public class InverseNetGeneratorTest {
	private void testNet(PetriNet pn) {
		PetriNet inverse = InverseNetGenerator.invert(pn);

		Collection<Matcher<? super Place>> expectedPlaces = new ArrayList<>();
		Collection<Matcher<? super Transition>> expectedTransitions = new ArrayList<>();
		Collection<Matcher<? super Flow>> expectedFlows = new ArrayList<>();

		for (Place place : pn.getPlaces())
			expectedPlaces.add(nodeWithID(place.getId()));
		for (Transition transition : pn.getTransitions())
			expectedTransitions.add(nodeWithID(transition.getId()));
		for (Flow flow : pn.getEdges())
			expectedFlows.add(flowThatConnects(flow.getTarget().getId(), flow.getSource().getId()));

		assertThat(inverse.getPlaces(), containsInAnyOrder(expectedPlaces));
		assertThat(inverse.getTransitions(), containsInAnyOrder(expectedTransitions));
		assertThat(inverse.getEdges(), containsInAnyOrder(expectedFlows));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 1, end = 30)
	public void testCycle(int size) {
		testNet(new CycleGenerator().generateNet(size));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 3)
	public void testBistatePhilNet(int size) {
		testNet(new BistatePhilNetGenerator().generateNet(size));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 3)
	public void testTristatePhilNet(int size) {
		testNet(new TristatePhilNetGenerator().generateNet(size));
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 2, end = 3)
	public void testQuadstatePhilNet(int size) {
		testNet(new QuadstatePhilNetGenerator().generateNet(size));
	}

	@Test
	public void testEmptyNet() {
		testNet(getEmptyNet());
	}

	@Test
	public void testABCLanguageNet() {
		testNet(getABCLanguageNet());
	}

	@Test
	public void testNoTransitionOnePlaceNet() {
		testNet(getNoTransitionOnePlaceNet());
	}

	@Test
	public void testOneTransitionNoPlaceNet() {
		testNet(getOneTransitionNoPlaceNet());
	}

	@Test
	public void testTokenGeneratorNet() {
		testNet(getTokenGeneratorNet());
	}

	@Test
	public void testDeadlockNet() {
		testNet(getDeadlockNet());
	}

	@Test
	public void testNonPersistentNet() {
		testNet(getNonPersistentNet());
	}

	@Test
	public void checkPersistentBiCFNet() {
		testNet(getPersistentBiCFNet());
	}

	@Test
	public void testConcurrentDiamondNet() {
		testNet(getConcurrentDiamondNet());
	}

	@Test
	public void testConflictingDiamondNet() {
		testNet(getConflictingDiamondNet());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
