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

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * Some test nets for testing bisimulation module.
 *
 * @author Raffaela Ferrari
 *
 */
public class TestTSForBisimulation {

	private TestTSForBisimulation() {
	}

	public static TransitionSystem getTestTS1A() {
		TransitionSystem ts = new TransitionSystem();
		State p0 = ts.createState("p0");
		ts.setInitialState(p0);
		State p1 = ts.createState("p1");
		State p2 = ts.createState("p2");

		ts.createArc(p0, p1, "a");
		ts.createArc(p0, p2, "a");
		ts.createArc(p1, p0, "b");
		ts.createArc(p1, p2, "a");
		ts.createArc(p2, p0, "b");
		ts.createArc(p2, p2, "a");

		return ts;
	}

	public static TransitionSystem getTestTS1B() {
		TransitionSystem ts = new TransitionSystem();
		State q0 = ts.createState("q0");
		ts.setInitialState(q0);
		State q1 = ts.createState("q1");
		State q2 = ts.createState("q2");

		ts.createArc(q0, q1, "a");
		ts.createArc(q1, q2, "b");
		ts.createArc(q1, q1, "a");
		ts.createArc(q2, q1, "a");

		return ts;
	}

	public static TransitionSystem getTestTS2A() {
		TransitionSystem ts = new TransitionSystem();

		State p0 = ts.createState("p0");
		ts.setInitialState(p0);
		State p1 = ts.createState("p1");
		State p2 = ts.createState("p2");
		State p3 = ts.createState("p3");

		ts.createArc(p0, p1, "a");
		ts.createArc(p0, p2, "a");
		ts.createArc(p2, p3, "b");

		return ts;
	}

	public static TransitionSystem getTestTS2B() {
		TransitionSystem ts = new TransitionSystem();

		State q0 = ts.createState("q0");
		ts.setInitialState(q0);
		State q1 = ts.createState("q1");
		State q2 = ts.createState("q2");

		ts.createArc(q0, q1, "a");
		ts.createArc(q1, q2, "b");

		return ts;
	}

	public static TransitionSystem getTestTS3A() {
		TransitionSystem ts = new TransitionSystem();

		State p0 = ts.createState("p0");
		ts.setInitialState(p0);

		ts.createArc(p0, p0, "a");

		return ts;
	}

	public static TransitionSystem getTestTS3B() {
		TransitionSystem ts = new TransitionSystem();

		State q0 = ts.createState("q0");
		ts.setInitialState(q0);
		State q1 = ts.createState("q1");

		ts.createArc(q0, q1, "a");
		ts.createArc(q1, q0, "a");

		return ts;
	}

	public static TransitionSystem getTestTS3C() {
		TransitionSystem ts = new TransitionSystem();

		State q0 = ts.createState("q0");
		ts.setInitialState(q0);

		ts.createArc(q0, q0, "b");

		return ts;
	}

	public static TransitionSystem getTestTS3D() {
		TransitionSystem ts = new TransitionSystem();

		State q0 = ts.createState("q0");
		ts.setInitialState(q0);
		State q1 = ts.createState("q1");

		ts.createArc(q0, q1, "a");
		ts.createArc(q1, q0, "b");

		return ts;
	}

	/**
	 * <pre>
	 * {@code
	 *  p0 -a> p1 -b> p2 <c- (self loop)
	 *            \b> p3 <d- (self loop)
	 * }
	 * </pre>
	 * @return The above TS
	 */
	public static TransitionSystem getTestTS4A() {
		TransitionSystem ts = new TransitionSystem();

		State p0 = ts.createState("p0");
		ts.setInitialState(p0);
		State p1 = ts.createState("p1");
		State p2 = ts.createState("p2");
		State p3 = ts.createState("p3");

		ts.createArc(p0, p1, "a");
		ts.createArc(p1, p2, "b");
		ts.createArc(p1, p3, "b");
		ts.createArc(p2, p2, "c");
		ts.createArc(p3, p3, "d");

		return ts;
	}

	/**
	 * <pre>
	 * {@code
	 *  q0 -a> q1 -b> q4 <d- (self loop)
	 *     \a> q2 \b\
	 *          |\-b->q3 <c- (self loop)
	 *           \b-> q5 <d- (self loop)
	 * }
	 * </pre>
	 * @return The above TS
	 */
	public static TransitionSystem getTestTS4B() {
		TransitionSystem ts = new TransitionSystem();

		State q0 = ts.createState("q0");
		ts.setInitialState(q0);
		State q1 = ts.createState("q1");
		State q2 = ts.createState("q2");
		State q3 = ts.createState("q3");
		State q4 = ts.createState("q4");
		State q5 = ts.createState("q5");

		ts.createArc(q0, q1, "a");
		ts.createArc(q0, q2, "a");
		ts.createArc(q1, q3, "b");
		ts.createArc(q1, q4, "b");
		ts.createArc(q2, q3, "b");
		ts.createArc(q2, q5, "b");
		ts.createArc(q3, q3, "c");
		ts.createArc(q4, q4, "d");
		ts.createArc(q5, q5, "d");

		return ts;
	}

	public static TransitionSystem getTestTS4C() {
		TransitionSystem ts = new TransitionSystem();

		State q0 = ts.createState("q0");
		ts.setInitialState(q0);
		State q1 = ts.createState("q1");
		State q2 = ts.createState("q2");
		State q3 = ts.createState("q3");
		State q4 = ts.createState("q4");

		ts.createArc(q0, q1, "a");
		ts.createArc(q0, q2, "a");
		ts.createArc(q1, q3, "b");
		ts.createArc(q2, q4, "b");
		ts.createArc(q3, q3, "c");
		ts.createArc(q4, q4, "d");

		return ts;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
