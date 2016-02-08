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

package uniol.apt.analysis.invariants;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.AptModule;
import uniol.apt.module.Category;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * This module computes all semipositive S- or T-invariants.
 * @author Manuel Gieseking
 */
@AptModule
public class ComputeMinSemiPosInvariantsModule extends AbstractModule implements Module {

	private final static String DESCRIPTION = "Compute a generator set of S- or T-invariants";
	private final static String TITLE = "invariants";
	private final static String NAME = "invariants";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined");
		inputSpec.addParameter("inv", Character.class, "Parameter 's' for s-invariants "
			+ "and 't' for t-invariants.");
		inputSpec.addOptionalParameter("algo", Character.class, 'p', "Parameter 'f' for Farkas algorithm and"
			+ " 'p' for the adapted Farkas algorithm of PIPE.");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("mapping", String.class);
		outputSpec.addReturnValue("invariants", Set.class);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);
		Character para = input.getParameter("inv", Character.class);
		Character algo = input.getParameter("algo", Character.class);
		Set<List<Integer>> invariants = null;
		Set<? extends Node> nodes;
		InvariantCalculator.InvariantAlgorithm alg = (algo == 'f')
			? InvariantCalculator.InvariantAlgorithm.FARKAS : InvariantCalculator.InvariantAlgorithm.PIPE;
		if (para == 's') {
			invariants = InvariantCalculator.calcSInvariants(pn, alg);
			nodes = pn.getPlaces();
		} else if (para == 't') {
			invariants = InvariantCalculator.calcTInvariants(pn, alg);
			nodes = pn.getTransitions();
		} else {
			throw new ModuleException("Parameter for " + getName() + " has to be [s/t]");
		}


		// Mapping
		StringBuilder sb = new StringBuilder("[");
		for (Iterator<? extends Node> it = nodes.iterator(); it.hasNext();) {
			Node node = it.next();
			sb.append(node.getId()).append("; ");
		}
		if (sb.length() - 2 >= 0) {
			sb.replace(sb.length() - 2, sb.length() - 1, "]");
		} else {
			sb.append("]");
		}
		output.setReturnValue("mapping", String.class, sb.toString());

		// Invariants
		Set<Vector> invs = new HashSet<>();
		for (List<Integer> list : invariants) {
			invs.add(new Vector(list));
		}
		output.setReturnValue("invariants", Set.class, invs);
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public String getShortDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getLongDescription() {
		return getShortDescription() + ".\n\n"
			+ "An invariant is a semi-positive vector from the nullspace of the incidence matrix C."
			+ " For a T-invariant x≥0 this means C*x=0 and a S-invariant x≥0 satisfies Cᵀ*x=0."
			+ " This module finds the set of generators for all S- or T-invariants.";
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.PN};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
