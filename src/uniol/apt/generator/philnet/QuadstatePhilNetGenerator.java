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

package uniol.apt.generator.philnet;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * Generate a philosophers net
 *
 * Each philosopher has four states:
 * thinking, where he don't hold forks
 * waiting, where he holds his own fork and waits for the fork of his neighbor
 * eating, where he hold his and his neighbors forks
 * cleaning, where he holds his own fork
 *
 * @author vsp
 */
public class QuadstatePhilNetGenerator extends AbstractPhilNetGenerator {

	@Override
	protected Philosopher addPhilPT(PetriNet pn, int i) {
		Place pfork = pn.createPlace("fork" + Integer.toString(i));
		Place peat = pn.createPlace("eating" + Integer.toString(i));
		Place pthink = pn.createPlace("thinking" + Integer.toString(i));
		Place pclean = pn.createPlace("cleaning" + Integer.toString(i));
		Place pwait = pn.createPlace("waiting" + Integer.toString(i));

		pthink.setInitialToken(1);
		pfork.setInitialToken(1);

		Transition tput1st = pn.createTransition("put1st" + Integer.toString(i));
		Transition tput2nd = pn.createTransition("put2nd" + Integer.toString(i));
		Transition ttake1st = pn.createTransition("take1st" + Integer.toString(i));
		Transition ttake2nd = pn.createTransition("take2nd" + Integer.toString(i));

		Philosopher phil = new Philosopher(pfork,
			new Place[]{pthink, pwait, peat, pclean},
			new Transition[]{ttake1st, ttake2nd, tput1st, tput2nd});
		pfork.putExtension("philosopher", phil);
		pthink.putExtension("philosopher", phil);
		pwait.putExtension("philosopher", phil);
		peat.putExtension("philosopher", phil);
		ttake1st.putExtension("philosopher", phil);
		ttake2nd.putExtension("philosopher", phil);
		tput1st.putExtension("philosopher", phil);
		tput2nd.putExtension("philosopher", phil);

		return phil;
	}

	@Override
	protected void addPhilA(PetriNet pn, int i, int next) {
		Place pforki = pn.getPlace("fork" + Integer.toString(i));
		Place pforkn = pn.getPlace("fork" + Integer.toString(next));
		Place peat = pn.getPlace("eating" + Integer.toString(i));
		Place pthink = pn.getPlace("thinking" + Integer.toString(i));
		Place pclean = pn.getPlace("cleaning" + Integer.toString(i));
		Place pwait = pn.getPlace("waiting" + Integer.toString(i));

		Transition tput1 = pn.getTransition("put1st" + Integer.toString(i));
		Transition tput2 = pn.getTransition("put2nd" + Integer.toString(i));
		Transition ttake1 = pn.getTransition("take1st" + Integer.toString(i));
		Transition ttake2 = pn.getTransition("take2nd" + Integer.toString(i));

		pn.createFlow(pforki, ttake1);
		pn.createFlow(pthink, ttake1);
		pn.createFlow(ttake1, pwait);
		pn.createFlow(pwait, ttake2);
		pn.createFlow(pforkn, ttake2);
		pn.createFlow(ttake2, peat);
		pn.createFlow(peat, tput1);
		pn.createFlow(tput1, pclean);
		pn.createFlow(tput1, pforkn);
		pn.createFlow(pclean, tput2);
		pn.createFlow(tput2, pthink);
		pn.createFlow(tput2, pforki);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
