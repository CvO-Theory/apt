/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016       vsp
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

package uniol.apt.analysis.cycles.lts;

import java.util.Set;

import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.TransitionSystem;
import static uniol.apt.util.MathTools.gcd;

/**
 * This class checks if the given lts fulfills the prime cycles property.
 * @author vsp
 */
public class PrimeCycleProperty {
	public ParikhVector check(TransitionSystem ts) {
		Set<? extends CyclePV> parikhs = new ComputeSmallestCycles().computePVsOfSmallestCycles(ts);

		for (CyclePV cycle : parikhs) {
			ParikhVector pv = cycle.getParikhVector();
			if (! isPrimeCyclePV(pv)) {
				return pv;
			}
		}
		return null;
	}

	private boolean isPrimeCyclePV(ParikhVector pv) {
		int gcd = 0;
		for (String event: pv.getLabels()) {
			gcd = gcd(gcd, pv.get(event));
		}
		return gcd == 1;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
