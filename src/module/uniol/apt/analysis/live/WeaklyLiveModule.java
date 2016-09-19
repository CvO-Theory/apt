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

package uniol.apt.analysis.live;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import uniol.apt.module.AptModule;
import uniol.apt.module.InterruptibleModule;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * Provide the weakly live test as a module.
 * @author Uli Schlachter, vsp
 */
@AptModule
public class WeaklyLiveModule extends AbstractLiveModule implements InterruptibleModule {

	@Override
	public String getShortDescription() {
		return "Check if a Petri net or a transition (if given) is weakly live";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ". A transition is weakly live if an infinite fire sequence exists "
			+ "which fires this transition infinitely often. A Petri net is weakly live when all of its "
			+ "transitions are weakly live";
	}

	@Override
	public String getName() {
		return "weakly_live";
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("weakly_live", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("sample_witness_transition", Transition.class);
	}

	@Override
	protected void findNonLiveTransition(ModuleOutput output, PetriNet pn) throws ModuleException {
		Transition trans = Live.findNonWeaklyLiveTransition(pn);
		output.setReturnValue("weakly_live", Boolean.class, trans == null);
		output.setReturnValue("sample_witness_transition", Transition.class, trans);
	}

	@Override
	protected void checkTransitionLiveness(ModuleOutput output, PetriNet pn, Transition transition)
			throws ModuleException {
		boolean live = Live.checkWeaklyLive(pn, transition);
		output.setReturnValue("weakly_live", Boolean.class, live);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
