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

package uniol.apt;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * Collection of static functions to construct Petri nets for tests
 *
 * @author vsp
 */
public class TestNetCollection {

	/**
	 * Private constructor to prevent instantiation of this class
	 */
	private TestNetCollection() { /* empty */ }

	/**
	 * Construct a empty Petri net
	 *
	 * @return PN: (empty)
	 */
	public static PetriNet getEmptyNet() {
		return new PetriNet("EmptyNet");
	}

	/**
	 * Construct a Petri net consisting of only one empty place
	 *
	 * @return PN:
	 * <pre>
	 * 0
	 * </pre>
	 */
	public static PetriNet getNoTransitionOnePlaceNet() {
		PetriNet pn = new PetriNet("NoTransitionOnePlaceNet");

		pn.createPlace("p1");

		return pn;
	}

	/**
	 * Construct a Petri net consisting of one place
	 *
	 * @return PN:
	 * <pre>
	 * []
	 * </pre>
	 */
	public static PetriNet getOneTransitionNoPlaceNet() {
		PetriNet pn = new PetriNet("OneTransitionNoPlaceNet");

		pn.createTransition("t1");

		return pn;
	}

	/**
	 * Construct a small unbounded Petri net
	 *
	 * @return PN:
	 * <pre>
	 * [] -> 0
	 * </pre>
	 */
	public static PetriNet getTokenGeneratorNet() {
		PetriNet pn = new PetriNet("TokenGeneratorNet");

		Place p = pn.createPlace("p1");

		Transition t = pn.createTransition("t1");

		pn.createFlow(t, p);

		return pn;
	}

	/**
	 * Construct a Petri net containing a dead lock
	 *
	 * @return PN:
	 * <pre>
	 * [] <- 1 -> []
	 * </pre>
	 */
	public static PetriNet getDeadlockNet() {
		PetriNet pn = new PetriNet("DeadlockNet");

		Place p = pn.createPlace("p1");

		p.setInitialToken(1);

		Transition t1 = pn.createTransition("t1");
		Transition t2 = pn.createTransition("t2");

		pn.createFlow(p, t1);
		pn.createFlow(p, t2);

		return pn;
	}

	/**
	 * Construct a non persistent Petri net
	 *
	 * @return PN:
	 * <pre>
	 *      1
	 *    / ^ \
	 *   v  |  v
	 *  [] []  []
	 *   \  ^  /
	 *    v | v
	 *      0
	 * </pre>
	 */
	public static PetriNet getNonPersistentNet() {
		PetriNet pn = new PetriNet("NonPersistentNet");

		Place p1 = pn.createPlace("p1");
		Place p2 = pn.createPlace("p2");

		p1.setInitialToken(1);

		Transition ta = pn.createTransition("a");
		Transition tb = pn.createTransition("b");
		Transition tc = pn.createTransition("c");

		pn.createFlow(p1, ta);
		pn.createFlow(ta, p2);
		pn.createFlow(p1, tb);
		pn.createFlow(tb, p2);
		pn.createFlow(tc, p1);
		pn.createFlow(p2, tc);

		return pn;
	}

	/**
	 * Construct a persistent and BiCF but not BCF Petri net
	 *
	 * @return PN:
	 * <pre>
	 *     []      []
	 *   ^ | ^   ^ | ^
	 *  /  v  \ /  v  \
	 *  1  0   2   0  1
	 *  ^  |  ^ ^  |  ^
	 *   \ v /   \ v /
	 *     []      []
	 * </pre>
	 */
	public static PetriNet getPersistentBiCFNet() {
		PetriNet pn = new PetriNet("PersistentBiCFNet");

		Place p1 = pn.createPlace("p1");
		Place p2 = pn.createPlace("p2");
		Place p3 = pn.createPlace("p3");
		Place p4 = pn.createPlace("p4");
		Place p5 = pn.createPlace("p5");

		p1.setInitialToken(1);
		p3.setInitialToken(2);
		p5.setInitialToken(1);

		Transition ta = pn.createTransition("a");
		Transition tb = pn.createTransition("b");
		Transition tc = pn.createTransition("c");
		Transition td = pn.createTransition("d");

		pn.createFlow(p1, ta);
		pn.createFlow(ta, p2);
		pn.createFlow(p3, ta);
		pn.createFlow(p3, tb);
		pn.createFlow(tb, p4);
		pn.createFlow(p5, tb);
		pn.createFlow(tc, p1);
		pn.createFlow(p2, tc);
		pn.createFlow(tc, p3);
		pn.createFlow(td, p3);
		pn.createFlow(p4, td);
		pn.createFlow(td, p5);

		return pn;
	}

	/**
	 * Construct a persistent and BiCF but not BCF Petri net
	 * Marking is variable.
	 *
	 * @author vsp, Daniel (just added "withMarks")
	 *
	 * @return PN:
	 * <pre>
	 *     []      []
	 *   ^ | ^   ^ | ^
	 *  /  v  \ /  v  \
	 *  1  0   1   0  1
	 *  ^  |  ^ ^  |  ^
	 *   \ v /   \ v /
	 *     []      []
	 * </pre>
	 */
	public static PetriNet getPersistentBiCFNetWithMarks(int m1, int m2, int m3, int m4, int m5) {
		PetriNet pn = new PetriNet("PersistentBiCFNetWithMarks");

		Place p1 = pn.createPlace("p1");
		Place p2 = pn.createPlace("p2");
		Place p3 = pn.createPlace("p3");
		Place p4 = pn.createPlace("p4");
		Place p5 = pn.createPlace("p5");

		p1.setInitialToken(m1);
		p2.setInitialToken(m2);
		p3.setInitialToken(m3);
		p4.setInitialToken(m4);
		p5.setInitialToken(m5);

		Transition ta = pn.createTransition("a");
		Transition tb = pn.createTransition("b");
		Transition tc = pn.createTransition("c");
		Transition td = pn.createTransition("d");

		pn.createFlow(p1, ta);
		pn.createFlow(ta, p2);
		pn.createFlow(p3, ta);
		pn.createFlow(p3, tb);
		pn.createFlow(tb, p4);
		pn.createFlow(p5, tb);
		pn.createFlow(tc, p1);
		pn.createFlow(p2, tc);
		pn.createFlow(tc, p3);
		pn.createFlow(td, p3);
		pn.createFlow(p4, td);
		pn.createFlow(td, p5);

		return pn;
	}

	/**
	 * Construct a Petri net producing a concurrent diamond
	 *
	 * @return PN:
	 * <pre>
	 * 1   1
	 * |   |
	 * v   v
	 * [] []
	 * </pre>
	 */
	public static PetriNet getConcurrentDiamondNet() {
		PetriNet pn = new PetriNet("ConcurrentDiamondNet");

		Place p1 = pn.createPlace("p1");
		Place p2 = pn.createPlace("p2");

		p1.setInitialToken(1);
		p2.setInitialToken(1);

		Transition ta = pn.createTransition("t1");
		Transition tb = pn.createTransition("t2");

		pn.createFlow(p1, ta);
		pn.createFlow(p2, tb);

		return pn;
	}

	/**
	 * Construct a Petri net producing a concurrent diamond
	 *
	 * @return PN:
	 * <pre>
	 * 1             1
	 * |             |
	 * v             v
	 * [] <-> 1 <-> []
	 * </pre>
	 */
	public static PetriNet getConflictingDiamondNet() {
		PetriNet pn = new PetriNet("ConflictingDiamondNet");

		Place p1 = pn.createPlace("p1");
		Place p2 = pn.createPlace("p2");
		Place p3 = pn.createPlace("p3");

		p1.setInitialToken(1);
		p2.setInitialToken(1);
		p3.setInitialToken(1);

		Transition ta = pn.createTransition("t1");
		Transition tb = pn.createTransition("t2");

		pn.createFlow(p1, ta);
		pn.createFlow(p2, tb);
		pn.createFlow(p3, ta);
		pn.createFlow(ta, p3);
		pn.createFlow(p3, tb);
		pn.createFlow(tb, p3);

		return pn;
	}

	/**
	 * Construct a Petri net producing the language a^mb^mc^m (if we had end states).
	 * The prefix language of this Petri net is... complicated.
	 *
	 * @return PN:
	 * <pre>
	 * ta1 ->  p2 -> tb1 ->  p4 -> tc
	 *  ^             ^            ^
	 *  |             |            |
	 *  v             v            v
	 *  p1 -> ta2 ->  p3 -> tb2 -> p5
	 * </pre>
	 */
	public static PetriNet getABCLanguageNet() {
		PetriNet pn = new PetriNet("ABCLanguageNet");

		Place p1 = pn.createPlace("p1");
		Place p2 = pn.createPlace("p2");
		Place p3 = pn.createPlace("p3");
		Place p4 = pn.createPlace("p4");
		Place p5 = pn.createPlace("p5");

		p1.setInitialToken(1);
		p4.setInitialToken(1);

		Transition ta1 = pn.createTransition("ta1");
		Transition ta2 = pn.createTransition("ta2");
		Transition tb1 = pn.createTransition("tb1");
		Transition tb2 = pn.createTransition("tb2");
		Transition tc = pn.createTransition("tc");

		ta1.setLabel("a");
		ta2.setLabel("a");
		tb1.setLabel("b");
		tb2.setLabel("b");
		tc.setLabel("c");

		pn.createFlow(ta1, p1);
		pn.createFlow(p1, ta1);
		pn.createFlow(ta1, p2);
		pn.createFlow(p2, tb1);
		pn.createFlow(tb1, p3);
		pn.createFlow(p3, tb1);
		pn.createFlow(tb1, p4);
		pn.createFlow(p4, tc);
		pn.createFlow(tc, p5);
		pn.createFlow(p5, tc);
		pn.createFlow(p1, ta2);
		pn.createFlow(ta2, p3);
		pn.createFlow(p3, tb2);
		pn.createFlow(tb2, p5);

		return pn;
	}

	/**
	 * Construct a Petri net with a dead transition.
	 *
	 * @return PN:
	 * <pre>
	 * 0 -> td   tl
	 * </pre>
	 */
	public static PetriNet getDeadTransitionNet() {
		PetriNet pn = new PetriNet("DeadTransitionNet");

		Place p1 = pn.createPlace("p1");

		Transition td = pn.createTransition("td");
		pn.createTransition("tl");

		pn.createFlow(p1, td);

		return pn;
	}

	/**
	 * Construct a Petri net whose reachability graph has a two arcs with the same label, but from different
	 * transitions.
	 *
	 * @return PN:
	 * <pre>
	 * [] []
	 * </pre>
	 */
	public static PetriNet getMultiArcNet() {
		PetriNet pn = new PetriNet("MultiArcNet");

		pn.createTransition("ta").setLabel("l");
		pn.createTransition("tb").setLabel("l");

		return pn;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
