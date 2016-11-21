/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016  Uli Schlachter
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

package uniol.apt.analysis.processmining;

import java.util.Arrays;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.TransformerUtils;

import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/** @author Uli Schlachter */
public class CreateLTSTest {
	@Test
	public void createEmpty() {
		CreateLTS create = new CreateLTS();
		TransitionSystem ts = create.getTransitionSystem();

		assertThat(ts, not(sameInstance(create.getTransitionSystem())));
		assertThat(ts.getNodes(), hasSize(1));
		assertThat(ts.getEdges(), hasSize(0));
		assertThat(ts.getInitialState(), not(nullValue()));
	}

	@Test
	public void createABAndBA() {
		CreateLTS create = new CreateLTS();
		create.addWord(Arrays.asList("a", "b"));
		create.addWord(Arrays.asList("b", "a"));
		TransitionSystem ts = create.getTransitionSystem();

		assertThat(ts, not(sameInstance(create.getTransitionSystem())));
		assertThat(ts.getNodes(), hasSize(4));
		assertThat(ts.getEdges(), hasSize(4));
		assertThat(ts.getInitialState(), not(nullValue()));

		assertThat(ts.getAlphabet(), containsInAnyOrder("a", "b"));

		State s0 = ts.getInitialState();
		assertThat(s0.getPostsetNodesByLabel("a"), hasSize(1));
		assertThat(s0.getPostsetNodesByLabel("b"), hasSize(1));

		State s1 = s0.getPostsetNodesByLabel("a").iterator().next();
		assertThat(s1.getPostsetNodesByLabel("a"), hasSize(0));
		assertThat(s1.getPostsetNodesByLabel("b"), hasSize(1));

		State s2 = s0.getPostsetNodesByLabel("b").iterator().next();
		assertThat(s2.getPostsetNodesByLabel("a"), hasSize(1));
		assertThat(s2.getPostsetNodesByLabel("b"), hasSize(0));

		State s3 = s1.getPostsetNodesByLabel("b").iterator().next();
		assertThat(s3.getPostsetNodes(), hasSize(0));
	}

	@Test
	public void createABAndBAWithConstantPV() {
		Transformer<ParikhVector, ParikhVector> constantTransformer =
			TransformerUtils.constantTransformer(new ParikhVector());
		CreateLTS create = new CreateLTS(constantTransformer);
		create.addWord(Arrays.asList("a", "b"));
		create.addWord(Arrays.asList("b", "a"));
		TransitionSystem ts = create.getTransitionSystem();

		assertThat(ts, not(sameInstance(create.getTransitionSystem())));
		assertThat(ts.getNodes(), hasSize(1));
		assertThat(ts.getEdges(), hasSize(2));
		assertThat(ts.getInitialState(), not(nullValue()));

		assertThat(ts.getAlphabet(), containsInAnyOrder("a", "b"));

		State s0 = ts.getInitialState();
		assertThat(s0.getPostsetNodesByLabel("a"), hasSize(1));
		assertThat(s0.getPostsetNodesByLabel("b"), hasSize(1));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
