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

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * Some test nets for testing separation module.
 *
 * @author Daniel
 *
 */
public class TestNetsForSeparation {

	private TestNetsForSeparation() {
		// nothing
	}

	public static PetriNet getStrongSeparableFromLecture() {
		PetriNet pn = new PetriNet();

		Place p1 = pn.createPlace("p1");
		Place p2 = pn.createPlace("p2");
		Place p3 = pn.createPlace("p3");
		Place p4 = pn.createPlace("p4");

		p1.setInitialToken(4);
		p2.setInitialToken(0);
		p3.setInitialToken(2);

		Transition ta = pn.createTransition("ta");
		Transition tt = pn.createTransition("tt");

		pn.createFlow(p1, ta);
		pn.createFlow(ta, p2);
		pn.createFlow(p2, tt);
		pn.createFlow(p3, tt);
		pn.createFlow(tt, p4);

		return (pn);
	}

	public static PetriNet getStrongSeparableFromLectureWithoutToken() {
		PetriNet pn = new PetriNet();

		Place p1 = pn.createPlace("p1");
		Place p2 = pn.createPlace("p2");
		Place p3 = pn.createPlace("p3");
		Place p4 = pn.createPlace("p4");

		p1.setInitialToken(0);
		p2.setInitialToken(0);
		p3.setInitialToken(0);

		Transition ta = pn.createTransition("ta");
		Transition tt = pn.createTransition("tt");

		pn.createFlow(p1, ta);
		pn.createFlow(ta, p2);
		pn.createFlow(p2, tt);
		pn.createFlow(p3, tt);
		pn.createFlow(tt, p4);

		return (pn);
	}

	public static PetriNet getWeakSeparableFromLecture() {
		PetriNet pnWeak = new PetriNet();

		Place wp1 = pnWeak.createPlace("p1");
		Place wp2 = pnWeak.createPlace("p2");
		Place wp3 = pnWeak.createPlace("p3");

		wp1.setInitialToken(2);
		wp2.setInitialToken(0);
		wp3.setInitialToken(0);

		Transition wta = pnWeak.createTransition("a");
		Transition wtb = pnWeak.createTransition("b");
		Transition wtc = pnWeak.createTransition("c");

		pnWeak.createFlow(wp1, wta);
		pnWeak.createFlow(wta, wp2);
		pnWeak.createFlow(wta, wp3);
		pnWeak.createFlow(wp2, wtb);
		pnWeak.createFlow(wtb, wp3);
		pnWeak.createFlow(wp3, wtb);
		pnWeak.createFlow(wp3, wtc);

		return (pnWeak);
	}

	public static PetriNet getNoSeparableFromLecture() {
		PetriNet pnNo = new PetriNet();

		Place noP1 = pnNo.createPlace("p1");
		Place noP2 = pnNo.createPlace("p2");
		Place noP3 = pnNo.createPlace("p3");
		Place noP4 = pnNo.createPlace("p4");

		noP1.setInitialToken(2);
		noP2.setInitialToken(2);
		noP3.setInitialToken(0);
		noP4.setInitialToken(0);

		Transition noTb = pnNo.createTransition("b");
		Transition noTt = pnNo.createTransition("t");
		Transition noTa = pnNo.createTransition("a");

		pnNo.createFlow(noP1, noTb);
		pnNo.createFlow(noTb, noP3);
		pnNo.createFlow(noP2, noTt);
		pnNo.createFlow(noP3, noTt);
		pnNo.createFlow(noTt, noP4);
		pnNo.createFlow(noP4, noTa);
		pnNo.createFlow(noTa, noP3);

		return (pnNo);
	}

	public static PetriNet getNoSeparable() {
		PetriNet pnNon = new PetriNet();

		Place nonP1 = pnNon.createPlace("p1");
		Place nonP2 = pnNon.createPlace("p2");
		Place nonP3 = pnNon.createPlace("p3");

		nonP1.setInitialToken(2);
		nonP2.setInitialToken(0);
		nonP3.setInitialToken(0);

		Transition nonTa = pnNon.createTransition("a");
		Transition nonTb = pnNon.createTransition("b");

		pnNon.createFlow(nonP1, nonTa);
		pnNon.createFlow(nonTa, nonP2);
		pnNon.createFlow(nonP2, nonTb);
		pnNon.createFlow(nonP1, nonTb);
		pnNon.createFlow(nonTb, nonP3);

		return (pnNon);
	}

	public static PetriNet getSeparableCycle() {
		PetriNet pnC = new PetriNet();

		Place cP1 = pnC.createPlace("p1");
		Place cP2 = pnC.createPlace("p2");

		cP1.setInitialToken(2);
		cP2.setInitialToken(0);

		Transition cTa = pnC.createTransition("a");
		Transition cTb = pnC.createTransition("b");

		pnC.createFlow(cP1, cTa);
		pnC.createFlow(cTa, cP2);
		pnC.createFlow(cP2, cTb);
		pnC.createFlow(cTb, cP1);

		return (pnC);
	}

	public static PetriNet getSeparableLine() {
		PetriNet pnC = new PetriNet();

		Place cP1 = pnC.createPlace("p1");
		Place cP2 = pnC.createPlace("p2");
		Place cP3 = pnC.createPlace("p3");

		cP1.setInitialToken(2);
		cP2.setInitialToken(0);
		cP3.setInitialToken(0);

		Transition cTa = pnC.createTransition("a");
		Transition cTb = pnC.createTransition("b");

		pnC.createFlow(cP1, cTa);
		pnC.createFlow(cTa, cP2);
		pnC.createFlow(cP2, cTb);
		pnC.createFlow(cTb, cP3);

		return (pnC);
	}

	public static PetriNet getSeparableTrivial() {
		PetriNet pn = new PetriNet();

		pn.createTransition("ta");

		return (pn);
	}

	/**
	 * 6-initial marking.
	 * 3-separable, but not 2-separable and not 6-separable
	 *
	 * @return PetriNet
	 */
	public static PetriNet getSeparableK3Not26() {
		PetriNet pn = new PetriNet();

		Place s = pn.createPlace();
		Place s1 = pn.createPlace();

		s.setInitialToken(6);
		s1.setInitialToken(0);

		Transition ta1 = pn.createTransition("ta1");
		Transition ta2 = pn.createTransition("ta2");
		Transition tb = pn.createTransition("tb");

		Flow arc = pn.createFlow(s, ta1);
		arc.setWeight(2);
		pn.createFlow(ta1, s1);

		pn.createFlow(s1, ta2);
		Flow arc2 = pn.createFlow(ta2, s);
		arc2.setWeight(2);

		pn.createFlow(s, tb);
		pn.createFlow(tb, s);

		return (pn);
	}

	/**
	 * 6-initial marking.
	 * 2-separable, but not 3-separable and not 6-separable
	 *
	 * @return PetriNet
	 */
	public static PetriNet getSeparableK2Not36() {
		PetriNet pn = new PetriNet();

		Place s = pn.createPlace();
		Place s1 = pn.createPlace();

		s.setInitialToken(6);
		s1.setInitialToken(0);

		Transition ta1 = pn.createTransition("ta1");
		Transition ta2 = pn.createTransition("ta2");
		Transition tb = pn.createTransition("tb");

		Flow arc = pn.createFlow(s, ta1);
		arc.setWeight(3);
		pn.createFlow(ta1, s1);

		pn.createFlow(s1, ta2);
		Flow arc2 = pn.createFlow(ta2, s);
		arc2.setWeight(3);

		pn.createFlow(s, tb);
		pn.createFlow(tb, s);

		return (pn);
	}

	/**
	 * 2-initial marking.
	 * alive, reversible, FC
	 * not 2-separable
	 * @return PetriNet
	 */
	public static PetriNet getNoSeparableLiveRevFC() {
		PetriNet pn = new PetriNet();

		Place s1 = pn.createPlace();
		Place s2 = pn.createPlace();
		Place s3 = pn.createPlace();
		Place s4 = pn.createPlace();
		Place s5 = pn.createPlace();

		s1.setInitialToken(2);
		s2.setInitialToken(0);
		s3.setInitialToken(0);
		s4.setInitialToken(0);
		s5.setInitialToken(2);

		Transition t1 = pn.createTransition("t1");
		Transition t2 = pn.createTransition("t2");
		Transition t3 = pn.createTransition("t3");
		Transition t4 = pn.createTransition("t4");
		Transition t5 = pn.createTransition("t5");

		pn.createFlow(s1, t1);
		pn.createFlow(s4, t1);
		pn.createFlow(t1, s2);

		pn.createFlow(s2, t2);
		pn.createFlow(t2, s3);
		pn.createFlow(t2, s4);

		pn.createFlow(s5, t3);
		pn.createFlow(t3, s4);

		pn.createFlow(s1, t4);
		pn.createFlow(s4, t4);
		pn.createFlow(t4, s3);
		pn.createFlow(t4, s5);

		pn.createFlow(s3, t5);
		pn.createFlow(t5, s1);

		return (pn);
	}

	/**
	 * 2-initial marking.
	 * weakly 2-separable but not strongly, reversible, FC
	 * "aacbbc" weakly but not strongly
	 *
	 * @return PetriNet
	 */
	public static PetriNet getWeakSeparable() {
		PetriNet pn = new PetriNet();

		Place s1 = pn.createPlace();
		Place s2 = pn.createPlace();
		Place s3 = pn.createPlace();

		s1.setInitialToken(0);
		s2.setInitialToken(2);
		s3.setInitialToken(0);

		Transition ta = pn.createTransition("ta");
		Transition tb = pn.createTransition("tb");
		Transition tc = pn.createTransition("tc");

		pn.createFlow(s2, ta);
		pn.createFlow(ta, s1);
		pn.createFlow(ta, s3);

		pn.createFlow(s1, tb);
		pn.createFlow(s3, tb);
		pn.createFlow(tb, s3);

		pn.createFlow(s3, tc);

		return (pn);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
