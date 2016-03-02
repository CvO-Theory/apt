/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
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

package uniol.apt.analysis.product;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.testng.annotations.Test;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * @author Jonas Prellberg
 */
public class ProductTest {

	@Test
	public void testSyncProductDeterministic() {
		TransitionSystem ts1 = new TransitionSystem();
		State s0 = ts1.createState();
		State s1 = ts1.createState();
		State s2 = ts1.createState();
		State s3 = ts1.createState();
		State s4 = ts1.createState();
		ts1.setInitialState(s0);
		ts1.createArc(s0, s1, "a");
		ts1.createArc(s0, s2, "b");
		ts1.createArc(s1, s4, "a");
		ts1.createArc(s2, s3, "a");

		TransitionSystem ts2 = new TransitionSystem();
		State p0 = ts2.createState();
		State p1 = ts2.createState();
		State p2 = ts2.createState();
		State p3 = ts2.createState();
		ts2.setInitialState(p0);
		ts2.createArc(p0, p1, "b");
		ts2.createArc(p0, p2, "c");
		ts2.createArc(p1, p3, "a");

		Product product = new Product(ts1, ts2);
		TransitionSystem result = product.getSyncProduct();

		// Result should look like this:
		// s0p0 --(b)--> s2p2 --(a)--> s3p3
		assertThat(result.getNodes(), hasSize(3));
		assertThat(result.getEdges(), hasSize(2));
		assertThat(result.getInitialState().getPostsetEdges(), hasSize(1));
		Arc arc1 = result.getInitialState().getPostsetEdges().iterator().next();
		assertThat(arc1.getTarget().getPostsetEdges(), hasSize(1));
		Arc arc2 = arc1.getTarget().getPostsetEdges().iterator().next();
		assertThat(arc1.getLabel(), equalTo("b"));
		assertThat(arc2.getLabel(), equalTo("a"));
	}

	@Test
	public void testSyncProductNondeterministic() {
		TransitionSystem ts1 = new TransitionSystem();
		State s0 = ts1.createState();
		State s1 = ts1.createState();
		State s2 = ts1.createState();
		ts1.setInitialState(s0);
		ts1.createArc(s0, s1, "a");
		ts1.createArc(s0, s2, "a");

		TransitionSystem ts2 = new TransitionSystem();
		State p0 = ts2.createState();
		State p1 = ts2.createState();
		ts2.setInitialState(p0);
		ts2.createArc(p0, p1, "a");

		Product product = new Product(ts1, ts2);
		TransitionSystem result = product.getSyncProduct();

		// Result should look like this:
		// s0p0 --(a)--> s1p1
		// . \----(a)--> s2p1
		assertThat(result.getNodes(), hasSize(3));
		assertThat(result.getEdges(), hasSize(2));
		assertThat(result.getInitialState().getPostsetEdges(), hasSize(2));
		Iterator<Arc> iter = result.getInitialState().getPostsetEdges().iterator();
		Arc arc1 = iter.next();
		Arc arc2 = iter.next();
		assertThat(arc1.getLabel(), equalTo("a"));
		assertThat(arc2.getLabel(), equalTo("a"));
	}

	@Test
	public void testSyncProductEmpty() {
		TransitionSystem ts1 = new TransitionSystem();
		State s0 = ts1.createState();
		State s1 = ts1.createState();
		ts1.setInitialState(s0);
		ts1.createArc(s0, s1, "a");

		TransitionSystem ts2 = new TransitionSystem();
		State p0 = ts2.createState();
		ts2.setInitialState(p0);

		Product product = new Product(ts1, ts2);
		TransitionSystem result = product.getSyncProduct();

		// Result should look like this:
		// s0p0
		assertThat(result.getNodes(), hasSize(1));
		assertThat(result.getEdges(), hasSize(0));
	}

	@Test
	public void testSyncProductLoop() {
		TransitionSystem ts1 = new TransitionSystem();
		State s0 = ts1.createState();
		State s1 = ts1.createState();
		ts1.setInitialState(s0);
		ts1.createArc(s0, s1, "a");
		ts1.createArc(s1, s1, "b");

		TransitionSystem ts2 = new TransitionSystem();
		State p0 = ts2.createState();
		State p1 = ts2.createState();
		ts2.setInitialState(p0);
		ts2.createArc(p0, p0, "b");
		ts2.createArc(p0, p1, "a");
		ts2.createArc(p1, p0, "b");

		Product product = new Product(ts1, ts2);
		TransitionSystem result = product.getSyncProduct();

		// Result should look like this:
		// s0p0 --(a)--> s1p1 --(b)--> s1p0 ⟲ (b)
		assertThat(result.getNodes(), hasSize(3));
		assertThat(result.getEdges(), hasSize(3));
		assertThat(result.getInitialState().getPostsetEdges(), hasSize(1));
		Arc arc1 = result.getInitialState().getPostsetEdges().iterator().next();
		assertThat(arc1.getTarget().getPostsetEdges(), hasSize(1));
		Arc arc2 = arc1.getTarget().getPostsetEdges().iterator().next();
		assertThat(arc2.getTarget().getPostsetEdges(), hasSize(1));
		Arc arc3 = arc2.getTarget().getPostsetEdges().iterator().next();
		assertThat(arc1.getLabel(), equalTo("a"));
		assertThat(arc2.getLabel(), equalTo("b"));
		assertThat(arc3.getLabel(), equalTo("b"));
		assertThat(arc3.getSourceId(), equalTo(arc3.getTargetId()));
	}

	@Test
	public void testAsyncProduct() {
		TransitionSystem ts1 = new TransitionSystem();
		State s0 = ts1.createState();
		State s1 = ts1.createState();
		State s2 = ts1.createState();
		ts1.setInitialState(s0);
		ts1.createArc(s0, s1, "a");
		ts1.createArc(s0, s2, "b");

		TransitionSystem ts2 = new TransitionSystem();
		State p0 = ts2.createState();
		State p1 = ts2.createState();
		ts2.setInitialState(p0);
		ts2.createArc(p0, p1, "c");

		Product product = new Product(ts1, ts2);
		TransitionSystem result = product.getAsyncProduct();

		// Result should look like this:
		// s0p0 --(a)--> s1p0 --(c)--> s1p1 <---\
		// \ \----(b)--> s2p0 --(c)--> s2p1 <--\ \
		// .\-----(c)--> s0p1 --(b)------------/ /
		// . \----(a)---------------------------/
		assertThat(result.getNodes(), hasSize(6));
		assertThat(result.getEdges(), hasSize(7));
		assertThat(result.getInitialState().getPostsetEdges(), hasSize(3));

		Set<String> labels = new HashSet<>();
		for (Arc arc : result.getInitialState().getPostsetEdges()) {
			labels.add(arc.getLabel());
		}
		assertThat(labels, containsInAnyOrder("a", "b", "c"));
	}

}

//vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
