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

package uniol.apt.analysis.presynthesis.pps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uniol.apt.adt.ts.TransitionSystem;

/**
 * Test for {@link PpsPropertyChecker}.
 *
 * @author Jonas Prellberg
 */
public class PpsPropertyCheckerTest {

	/**
	 * Satisfies properties ¬B, ¬D, F.
	 */
	private TransitionSystem ts1;

	/**
	 * Satisfies properties B, ¬D, F.
	 */
	private TransitionSystem ts2;

	/**
	 * Satisfies properties B, ¬D, ¬F.
	 */
	private TransitionSystem ts3;

	/**
	 * Additional test for property F to make sure that sequences are used
	 * instead of single transitions. Satisfies F.
	 */
	private TransitionSystem ts4;

	/**
	 * Additional test for property D. Satisfies D.
	 */
	private TransitionSystem ts5;

	/**
	 * Class under test.
	 */
	private PpsPropertyChecker ppsPropChecker;

	@BeforeClass
	public void before() {
		ts1 = new TransitionSystem();
		ts1.createState("s0");
		ts1.createState("s1");
		ts1.createState("s2");
		ts1.createState("s3");
		ts1.createArc("s0", "s1", "a");
		ts1.createArc("s0", "s2", "c");
		ts1.createArc("s1", "s3", "b");
		ts1.createArc("s2", "s3", "a");
		ts1.setInitialState("s0");

		ts2 = new TransitionSystem();
		ts2.createState("s0");
		ts2.createState("s1");
		ts2.createState("s2");
		ts2.createState("s3");
		ts2.createArc("s0", "s1", "a");
		ts2.createArc("s0", "s2", "b");
		ts2.createArc("s1", "s3", "b");
		ts2.setInitialState("s0");

		ts3 = new TransitionSystem();
		ts3.createState("s0");
		ts3.createState("s1");
		ts3.createState("s2");
		ts3.createState("s3");
		ts3.createState("sl");
		ts3.createState("sr");
		ts3.createArc("s0", "s1", "a");
		ts3.createArc("s0", "s2", "b");
		ts3.createArc("s1", "s3", "b");
		ts3.createArc("s2", "s3", "a");
		ts3.createArc("s1", "sl", "c");
		ts3.createArc("s2", "sr", "c");
		ts3.setInitialState("s0");

		ts4 = new TransitionSystem();
		ts4.createState("s0");
		ts4.createState("s1");
		ts4.createState("s2");
		ts4.createState("s3");
		ts4.createState("s4");
		ts4.createState("s5");
		ts4.createState("s6");
		ts4.createState("s7");
		ts4.createState("s8");
		ts4.createState("s9");
		ts4.createArc("s0", "s1", "a");
		ts4.createArc("s0", "s4", "v");
		ts4.createArc("s0", "s6", "c");
		ts4.createArc("s1", "s2", "b");
		ts4.createArc("s2", "s3", "v");
		ts4.createArc("s2", "s8", "c");
		ts4.createArc("s3", "s9", "c");
		ts4.createArc("s4", "s5", "a");
		ts4.createArc("s4", "s7", "c");
		ts4.createArc("s5", "s3", "b");
		ts4.setInitialState("s0");

		ts5 = new TransitionSystem();
		ts5.createState("s0");
		ts5.createState("s1");
		ts5.createState("s2");
		ts5.createState("s3");
		ts5.createState("s4");
		ts5.createArc("s0", "s1", "a");
		ts5.createArc("s0", "s3", "c");
		ts5.createArc("s1", "s2", "b");
		ts5.createArc("s3", "s1", "d");
		ts5.createArc("s4", "s2", "a");

		ppsPropChecker = new PpsPropertyChecker();
	}

	@Test
	public void testPropertyB() {
		assertThat(ppsPropChecker.hasPropertyB(ts1), is(false));
		assertThat(ppsPropChecker.hasPropertyB(ts2), is(true));
		assertThat(ppsPropChecker.hasPropertyB(ts3), is(true));
	}

	@Test
	public void testPropertyD() {
		assertThat(ppsPropChecker.hasPropertyD(ts1), is(false));
		assertThat(ppsPropChecker.hasPropertyD(ts2), is(false));
		assertThat(ppsPropChecker.hasPropertyD(ts3), is(false));
		assertThat(ppsPropChecker.hasPropertyD(ts5), is(true));
	}

	@Test
	public void testPropertyF() {
		assertThat(ppsPropChecker.hasPropertyF(ts1, 10), is(true));
		assertThat(ppsPropChecker.hasPropertyF(ts2, 10), is(true));
		assertThat(ppsPropChecker.hasPropertyF(ts3, 10), is(false));
		assertThat(ppsPropChecker.hasPropertyF(ts4, 10), is(true));
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
