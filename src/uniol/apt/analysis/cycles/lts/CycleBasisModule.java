/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2014  Members of the project group APT
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

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.Category;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * This module returns a minimal cycle basis of the given LTS.
 * <p/>
 * @author Thomas Strathmann
 */
public class CycleBasisModule extends AbstractModule {

	private final static String SHORTDESCRIPTION = "Compute a minimal directed cycle basis of the LTS";
	private final static String LONGDESCRIPTION = SHORTDESCRIPTION;
	private final static String TITLE = "MinimalCycleBasis";
	private final static String NAME = "minimal_cycle_basis";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "The LTS that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("minimalCycleBasis", Set.class);
		outputSpec.addReturnValue("minimalCycleBasisParikhVectors", Set.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem lts = input.getParameter("lts", TransitionSystem.class);
		Set<Vector<Arc>> cycleBasis = CycleBasis.cycleBasis(lts);

		// sort the cycles so that they conform to the usual notation for paths
		Set<Vector<Arc>> sortedCycleBasis = new HashSet<Vector<Arc>>();
		for(Vector<Arc> unsortedCycle : cycleBasis) {
			Vector<Arc> sortedCycle = new Vector<Arc>();
			
			// begin with the initial state (if this cycle contains it) or
			// the state with the minimal label
			int min = 0;
			for(int i=1; i<unsortedCycle.size(); ++i) {
				Arc amin = unsortedCycle.get(min);
				if(amin.getSource() == lts.getInitialState())
					break;
				Arc ai = unsortedCycle.get(i);
				if(ai.getSource() == lts.getInitialState()) {
					min = i;
					break;
				}
				if(amin.getSource().compareTo(ai.getSource()) == 1)
					min = i;
			}
			sortedCycle.add(unsortedCycle.get(min));
			unsortedCycle.remove(min);
			
			// sort cycle in the usual path order
			while(!unsortedCycle.isEmpty()) {
				int i;
				for(i=0; i<unsortedCycle.size(); ++i) {
					if(unsortedCycle.get(i).getSource() == 
							sortedCycle.lastElement().getTarget()) {
						break;
					}
				}
				sortedCycle.add(unsortedCycle.get(i));
				unsortedCycle.remove(i);
			}
			sortedCycleBasis.add(sortedCycle);
		}

		// compute Parikh vectors
		Set<ParikhVector> pvs = new HashSet<ParikhVector>();
		for(Vector<Arc> c : sortedCycleBasis) {
			pvs.add(new ParikhVector(lts, c));
		}
		
		output.setReturnValue("minimalCycleBasis", Set.class, sortedCycleBasis);
		output.setReturnValue("minimalCycleBasisParikhVectors", Set.class, pvs);
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public String getShortDescription() {
		return SHORTDESCRIPTION;
	}

	@Override
	public String getLongDescription() {
		return LONGDESCRIPTION;
	}

	@Override
	public Category[] getCategories() {
		return new Category[] { Category.LTS };
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
