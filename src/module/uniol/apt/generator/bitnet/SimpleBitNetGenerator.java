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

package uniol.apt.generator.bitnet;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * Generate a bit net
 *
 * Each bit has two states:
 * 0, when the bit isn't set
 * 1, when the bit is set
 *
 * and two transitions:
 * set, which sets the bit
 * unset, which unsets the bit
 *
 * @author vsp
 */
public class SimpleBitNetGenerator extends AbstractBitNetGenerator {

	@Override
	protected Bit addBitPTA(PetriNet pn, int i) {
		Place p0 = pn.createPlace("bit" + Integer.toString(i) + "_state0");
		Place p1 = pn.createPlace("bit" + Integer.toString(i) + "_state1");

		p0.setInitialToken(1);

		Transition tset = pn.createTransition("set" + Integer.toString(i));
		Transition tunset = pn.createTransition("unset" + Integer.toString(i));

		pn.createFlow(p0, tset);
		pn.createFlow(tset, p1);
		pn.createFlow(p1, tunset);
		pn.createFlow(tunset, p0);

		Bit bit = new Bit(new Place[]{p0, p1}, new Transition[]{tset, tunset});

		p0.putExtension("bit", bit);
		p1.putExtension("bit", bit);
		tset.putExtension("bit", bit);
		tunset.putExtension("bit", bit);

		return bit;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
