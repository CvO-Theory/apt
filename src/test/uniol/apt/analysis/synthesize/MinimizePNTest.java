/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

package uniol.apt.analysis.synthesize;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.*;

import uniol.apt.TestTSCollection;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.analysis.bounded.Bounded;
import uniol.apt.analysis.cf.ConflictFree;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.isomorphism.IsomorphismLogic;
import uniol.apt.analysis.language.LanguageEquivalence;
import uniol.apt.analysis.on.OutputNonBranching;
import uniol.apt.analysis.plain.Plain;
import uniol.apt.analysis.sideconditions.Pure;
import static uniol.apt.analysis.synthesize.SynthesizeUtils.*;

/** @author Uli Schlachter */
public class MinimizePNTest {
	static private Set<Region> createRegions(RegionUtility utility, int numRegions) {
		Set<Region> result = new HashSet<>();
		for (int i = 0; i < numRegions; i++)
			result.add(new Region.Builder(utility).withInitialMarking(BigInteger.valueOf(i)));
		return result;
	}

	static private SynthesizePN mockSynthesize(TransitionSystem ts, PNProperties properties, int numRegions,
			boolean onlyEventSeparation) {
		RegionUtility utility = new RegionUtility(ts);
		SynthesizePN synth = mock(SynthesizePN.class);
		when(synth.wasSuccessfullySeparated()).thenReturn(true);
		when(synth.getUtility()).thenReturn(utility);
		when(synth.getProperties()).thenReturn(properties);
		when(synth.onlyEventSeparation()).thenReturn(onlyEventSeparation);
		when(synth.getSeparatingRegions()).thenReturn(createRegions(utility, numRegions));
		return synth;
	}

	static private SynthesizePN mockSynthesize(TransitionSystem ts, PNProperties properties, int numRegions) {
		return mockSynthesize(ts, properties, numRegions, false);
	}

	static private void testSolution(PNProperties properties, SynthesizePN synth, MinimizePN min) throws Exception {
		RegionUtility utility = synth.getUtility();
		TransitionSystem ts = utility.getTransitionSystem();
		PetriNet pn = SynthesizePN.synthesizePetriNet(synth.getUtility(), min.getSeparatingRegions());

		// Test if the synthesized PN really satisfies all the properties that it should
		if (properties.isPure())
			assertThat(Pure.checkPure(pn), is(true));
		if (properties.isPlain())
			assertThat(new Plain().checkPlain(pn), is(true));
		if (properties.isTNet())
			assertThat(SynthesizePN.isGeneralizedTNet(pn), is(true));
		if (properties.isMarkedGraph())
			assertThat(SynthesizePN.isGeneralizedMarkedGraph(pn), is(true));
		if (properties.isKBounded())
			assertThat(Bounded.checkBounded(pn).k <= properties.getKForKBounded(), is(true));
		if (properties.isOutputNonbranching())
			assertThat(new OutputNonBranching(pn).check(), is(true));
		if (properties.isConflictFree())
			assertThat(new ConflictFree(pn).check(), is(true));

		if (!synth.onlyEventSeparation())
			assertThat(new IsomorphismLogic(CoverabilityGraph.get(pn).toReachabilityLTS(), ts, true)
					.isIsomorphic(), is(true));
		else
			assertThat(LanguageEquivalence.checkLanguageEquivalence(
					CoverabilityGraph.get(pn).toReachabilityLTS(), ts), nullValue());

		assertThat(SynthesizePN.isDistributedImplementation(utility, properties, pn), is(true));
	}

	private void doTestCC1LTS(PNProperties properties, int size) throws Exception {
		TransitionSystem ts = TestTSCollection.getcc1LTS();
		SynthesizePN synth = mockSynthesize(ts, properties, 6);
		MinimizePN min = new MinimizePN(synth);

		assertThat(min.getSeparatingRegions(), hasSize(size));
		testSolution(properties, synth, min);
	}

	@Test
	public void testCC1LTS() throws Exception {
		PNProperties properties = new PNProperties();
		doTestCC1LTS(properties, 3);
	}

	@Test
	public void testCC1LTSSafe() throws Exception {
		PNProperties properties = new PNProperties().requireSafe();
		doTestCC1LTS(properties, 4);
	}

	@Test
	public void testCC1LTSPure() throws Exception {
		PNProperties properties = new PNProperties().setPure(true);
		doTestCC1LTS(properties, 3);
	}

	@Test
	public void testA() throws Exception {
		PNProperties properties = new PNProperties();
		TransitionSystem ts = makeTS(Arrays.asList("a"));
		SynthesizePN synth = mockSynthesize(ts, properties, 2);
		MinimizePN min = new MinimizePN(synth);

		// This tests the code for a solution with 0 regions
		assertThat(min.getSeparatingRegions(), hasSize(1));
		testSolution(properties, synth, min);
	}

	@Test
	public void testCC1LTSLanguageEquivalence() throws Exception {
		PNProperties properties = new PNProperties();
		TransitionSystem ts = TestTSCollection.getcc1LTS();
		SynthesizePN synth = mockSynthesize(ts, properties, 5, true);
		MinimizePN min = new MinimizePN(synth);

		assertThat(min.getSeparatingRegions(), hasSize(3));
		testSolution(properties, synth, min);
	}

	private void doStateSeparation(boolean languageEquivalence) throws Exception {
		PNProperties properties = new PNProperties();
		TransitionSystem ts = TestTSCollection.getNeedsRegionForStateSeperationTS();
		SynthesizePN synth = mockSynthesize(ts, properties, 5, languageEquivalence);
		MinimizePN min = new MinimizePN(synth);

		assertThat(min.getSeparatingRegions(), hasSize(2));
		testSolution(properties, synth, min);
	}

	@Test
	public void testStateSeparation() throws Exception {
		doStateSeparation(false);
	}

	@Test
	public void testStateSeparationLanguageEquivalence() throws Exception {
		doStateSeparation(true);
	}

	@Test(expectedExceptions = { UnsupportedOperationException.class })
	public void testABAndA() throws Exception {
		SynthesizePN synth = mock(SynthesizePN.class);
		when(synth.wasSuccessfullySeparated()).thenReturn(false);
		new MinimizePN(synth);
	}

	@Test
	public void testON() throws Exception {
		PNProperties properties = new PNProperties().setOutputNonbranching(true);
		TransitionSystem ts = makeTS(Arrays.asList("a"));
		SynthesizePN synth = mockSynthesize(ts, properties, 2);
		MinimizePN min = new MinimizePN(synth);

		// This tests the code for a solution with 0 regions
		assertThat(min.getSeparatingRegions(), hasSize(1));
		testSolution(properties, synth, min);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
