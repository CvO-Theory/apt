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
 * A collection of simple test transition systems.
 *
 * TODO: Add some more visualizations
 *
 * @author Vincent Göbel, Renke Grunwald
 *
 */
public class TestTSCollection {
	private TestTSCollection() {
	}

	public static TransitionSystem getSingleStateTS() {
		TransitionSystem ts = new TransitionSystem();

		State s0 = ts.createState("s0");

		ts.setInitialState(s0);

		return ts;
	}

	public static TransitionSystem getSingleStateTSWithLoop() {
		TransitionSystem ts = new TransitionSystem();

		State s0 = ts.createState("s0");
		ts.createArc(s0, s0, "a");

		ts.setInitialState(s0);

		return ts;
	}

	/**
	 *
	 * The transitions system:
	 * <pre>
	 *   [s]
	 *  a/ \b
	 *  v   v
	 * (t) (v)
	 * </pre>
	 *
	 * @return a ts with three states and two edges
	 */
	public static TransitionSystem getThreeStatesTwoEdgesTS() {
		TransitionSystem ts = new TransitionSystem();

		State s = ts.createState("s");
		State t = ts.createState("t");
		State v = ts.createState("v");

		ts.createArc(s, t, "a");

		ts.createArc(s, v, "b");
		ts.setInitialState(s);

		return ts;
	}

	/**
	 *
	 * The transitions system:
	 * <pre>
	 *       a
	 * [s] <---> (t)
	 * </pre>
	 *
	 * @return a ts with two states (a cycle); both edges have the same label
	 */
	public static TransitionSystem getTwoStateCycleSameLabelTS() {
		TransitionSystem ts = new TransitionSystem();

		State s = ts.createState("s");
		State t = ts.createState("t");

		ts.createArc(s, t, "a");

		ts.createArc(t, s, "a");

		ts.setInitialState(s);

		return ts;
	}

	/**
	 *
	 * The transitions system:
	 * <pre>
	 * [s]-\
	 *  ^  | a
	 *  \--/
	 * </pre>
	 *
	 * @return a ts with a single state loop
	 */
	public static TransitionSystem getSingleStateLoop() {
		TransitionSystem ts = new TransitionSystem();

		State s = ts.createState("s");

		ts.createArc(s, s, "a");

		ts.setInitialState(s);

		return ts;
	}

	public static TransitionSystem getSingleStateWithUnreachableTS() {
		TransitionSystem ts = getSingleStateTS();

		State s1 = ts.createState("s1");
		ts.createArc(s1, s1, "NotA");

		return ts;
	}

	public static TransitionSystem getSingleStateSingleTransitionTS() {
		TransitionSystem ts = new TransitionSystem();

		State s0 = ts.createState("s0");

		ts.setInitialState(s0);

		ts.createArc(s0, s0, "NotA");
		return ts;
	}

	public static TransitionSystem getNonDeterministicTS() {
		TransitionSystem ts = new TransitionSystem();

		State s0 = ts.createState("s0");
		State s1 = ts.createState("s1");
		State s2 = ts.createState("s2");

		ts.setInitialState(s0);

		ts.createArc(s0, s1, "a");
		ts.createArc(s0, s2, "a");
		return ts;
	}

	public static TransitionSystem getPersistentTS() {
		TransitionSystem ts = new TransitionSystem();

		State s0 = ts.createState("s0");
		State left = ts.createState("l");
		State right = ts.createState("r");
		State s1 = ts.createState("s1");

		ts.setInitialState(s0);

		ts.createArc(s0, left, "a");
		ts.createArc(s0, right, "b");
		ts.createArc(left, s1, "b");
		ts.createArc(right, s1, "a");

		return ts;
	}

	public static TransitionSystem getNonPersistentTS() {
		TransitionSystem ts = new TransitionSystem();

		State s0 = ts.createState("s0");
		State left = ts.createState("l");
		State right = ts.createState("r");
		State s1 = ts.createState("s1");
		State right2 = ts.createState("r2");

		ts.setInitialState(s0);

		ts.createArc(s0, left, "a");
		ts.createArc(s0, right, "b");
		ts.createArc(left, s1, "b");
		ts.createArc(right, s1, "a");
		ts.createArc(right, right2, "fail");
		return ts;
	}

	public static TransitionSystem getNotTotallyReachableTS() {
		TransitionSystem ts = new TransitionSystem();

		State s0 = ts.createState("s0");
		State s1 = ts.createState("s1");
		State fail = ts.createState("fail");

		ts.setInitialState(s0);

		ts.createArc(s0, s1, "a");
		ts.createArc(fail, s0, "b");

		return ts;
	}

	public static TransitionSystem getReversibleTS() {
		TransitionSystem ts = new TransitionSystem();

		State s0 = ts.createState("s0");
		State s1 = ts.createState("s1");
		State s2 = ts.createState("s2");

		ts.setInitialState(s0);

		ts.createArc(s0, s1, "a");
		ts.createArc(s1, s2, "b");
		ts.createArc(s2, s0, "c");
		return ts;
	}

	public static TransitionSystem getcc1LTS() {
		TransitionSystem ts = new TransitionSystem();

		ts.createState("s0");
		ts.createState("s1");
		ts.createState("s2");
		ts.createState("s3");

		ts.setInitialState(ts.getNode("s0"));

		ts.createArc(ts.getNode("s0"), ts.getNode("s1"), "a");
		ts.createArc(ts.getNode("s0"), ts.getNode("s2"), "b");
		ts.createArc(ts.getNode("s1"), ts.getNode("s0"), "c");
		ts.createArc(ts.getNode("s1"), ts.getNode("s3"), "b");
		ts.createArc(ts.getNode("s2"), ts.getNode("s0"), "a");
		ts.createArc(ts.getNode("s2"), ts.getNode("s3"), "d");
		ts.createArc(ts.getNode("s3"), ts.getNode("s1"), "d");
		ts.createArc(ts.getNode("s3"), ts.getNode("s2"), "c");

		return ts;
	}

	public static TransitionSystem getOneCycleLTS() {
		TransitionSystem ts = new TransitionSystem();

		ts.createState("s0");
		ts.createState("s1");
		ts.createState("s2");
		ts.createState("s3");

		ts.setInitialState(ts.getNode("s0"));

		ts.createArc(ts.getNode("s0"), ts.getNode("s1"), "a");
		ts.createArc(ts.getNode("s1"), ts.getNode("s2"), "b");
		ts.createArc(ts.getNode("s2"), ts.getNode("s3"), "c");
		ts.createArc(ts.getNode("s3"), ts.getNode("s0"), "d");

		return ts;
	}

	/**
	 * The transitions system:
	 * <pre>
	 *      a        b        c        a
	 * [s] ---> [t] ---> [u] ---> [v] ---> [w] <-b (loop)
	 * </pre>
	 */
	public static TransitionSystem getPathTS() {
		TransitionSystem ts = new TransitionSystem();

		State s = ts.createState("s");
		State t = ts.createState("t");
		State u = ts.createState("u");
		State v = ts.createState("v");
		State w = ts.createState("w");

		ts.setInitialState(s);

		ts.createArc(s, t, "a");
		ts.createArc(t, u, "b");
		ts.createArc(u, v, "c");
		ts.createArc(v, w, "a");
		ts.createArc(w, w, "b");
		return ts;
	}

	/**
	 * The transitions system:
	 * <pre>
	 *      a        c        a        c
	 * [s] ---> [t] ---> [u] ---> [v] ---> [w]
	 *  \\                \\                \\
	 *   b                 b                 b
	 *   \\                \\                \\
	 *    v                 v                 v
	 *   [z]               [y]               [x]
	 * </pre>
	 *
	 * This ts is generated by a PN with three places. One limits 'a' to fire only twice. The next one is a conflict
	 * between a and b and makes sure that one of these transitions disables the other. Also, c places a token here.
	 * The last place is an output place of a and an input place of c.
	 */
	public static TransitionSystem getPureSynthesizablePathTS() {
		TransitionSystem ts = new TransitionSystem();

		State s = ts.createState("s");
		State t = ts.createState("t");
		State u = ts.createState("u");
		State v = ts.createState("v");
		State w = ts.createState("w");
		State x = ts.createState("x");
		State y = ts.createState("y");
		State z = ts.createState("z");

		ts.setInitialState(s);

		ts.createArc(s, t, "a");
		ts.createArc(t, u, "c");
		ts.createArc(u, v, "a");
		ts.createArc(v, w, "c");
		ts.createArc(w, x, "b");
		ts.createArc(u, y, "b");
		ts.createArc(s, z, "b");
		return ts;
	}

	/**
	 * The transitions system:
	 * <pre>
	 *      a        c        a        c
	 * [s] ---> [t] ---> [u] ---> [v] ---> [w] <-b (loop)
	 *                   /\\
	 *                    b (loop)
	 * </pre>
	 *
	 * The generating PN has four places. One place limits 'a' to only firing twice, one place makes c fire only
	 * after a fired, the third places makes a and b fire only after c fired (initially marked) and the last place
	 * makes b fire only after c fired at least once.
	 */
	public static TransitionSystem getImpureSynthesizablePathTS() {
		TransitionSystem ts = new TransitionSystem();

		State s = ts.createState("s");
		State t = ts.createState("t");
		State u = ts.createState("u");
		State v = ts.createState("v");
		State w = ts.createState("w");

		ts.setInitialState(s);

		ts.createArc(s, t, "a");
		ts.createArc(t, u, "c");
		ts.createArc(u, u, "b");
		ts.createArc(u, v, "a");
		ts.createArc(v, w, "c");
		ts.createArc(w, w, "b");
		return ts;
	}

	/**
	 * The transitions system:
	 * <pre>
	 *      b
	 * [s] ---> [u]
	 *  \\       ^\\
	 * a \\   a /  \\ a
	 *    v   /     v
	 *     [t] ---> [v]
	 *          b
	 * </pre>
	 */
	public static TransitionSystem getTwoBThreeATS() {
		TransitionSystem ts = new TransitionSystem();

		State s = ts.createState("s");
		State t = ts.createState("t");
		State u = ts.createState("u");
		State v = ts.createState("v");

		ts.setInitialState(s);

		ts.createArc(s, t, "a");
		ts.createArc(t, u, "a");
		ts.createArc(u, v, "a");
		ts.createArc(s, u, "b");
		ts.createArc(t, v, "b");
		return ts;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
