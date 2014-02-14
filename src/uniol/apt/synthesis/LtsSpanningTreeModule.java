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

package uniol.apt.synthesis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.Category;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * This module returns a spanning tree of the given LTS
 * <p/>
 * @author Thomas Strathmann
 */
public class LtsSpanningTreeModule extends AbstractModule {

	private final static String SHORTDESCRIPTION = "Compute a spanning tree of the LTS";
	private final static String LONGDESCRIPTION = SHORTDESCRIPTION;
	private final static String TITLE = "LtsSpanningTree";
	private final static String NAME = "spanning_tree";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("lts", TransitionSystem.class, "The LTS for which a spanning tree should be computed");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("spanningTree", TransitionSystem.class,
				ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		TransitionSystem lts = input.getParameter("lts", TransitionSystem.class);
		SpanningTree span = new SpanningTree(lts);
		output.setReturnValue("spanningTree", TransitionSystem.class, span.getSpanningTree());
	
		System.err.println("Parikh vectors of fundamental cycles:");
		int[][] A = span.matrix();
		System.err.println("matrix:");
		LinearAlgebra.printMatrix(A);
		
		System.err.println("generators:");	
		Set<int[]> generators = LinearAlgebra.solutionBasis(A);
		// if the generating set is empty, add the zero vector
		// to ensure that later stages of the algorithm work as expected
		if(generators.isEmpty()) {
			int[] zero = new int[lts.getAlphabet().size()];
			generators.add(zero);
		}
		
		for(String a : span.getOrderedAlphabet()) {
			System.err.print(String.format("  %3s", a));
		}
		System.err.println();
		for(int[] g : generators) {
			for(int l=0; l<g.length; ++l) {
				System.err.print(String.format("  %3d", g[l]));
			}
			System.err.println();
		}
		
		/*
		System.err.println("some path integrals:");
		for(int[] eta : generators) {
			for(State s1 : lts.getNodes()) {
				for(State s2 : lts.getNodes()) {
					System.err.print("(" + s1 + ", " + s2 + ") [ " + eta + " ] = ");
					int[] psi = span.pathWeights(s1, s2);
					int integral = LinearAlgebra.dotProduct(eta, psi);
					System.err.println(integral);
				}
			}
		}
		*/
		
		/*
		final int n = lts.getNodes().size();
		
		// SSA
		System.err.println("Checking SSA: ");
		ArrayList<int[]> columns = new ArrayList<int[]>(lts.getNodes().size()); 
		for(State s : span.getOrderedStates()) {
			int[] col = new int[generators.size()];
			int i = 0;
			System.err.print("  " + s + ": ");
			for(int[] eta : generators) {
				int[] psi = span.parikhVector(s);
				col[i++] = LinearAlgebra.dotProduct(eta, psi);
				System.err.print(col[i-1] + "  ");
			}
			System.err.println();
			columns.add(col);
		}
		
		for(int i=0; i<n; ++i) {
			for(int j=i+1; j<n; ++j) {
				if(Arrays.equals(columns.get(i), columns.get(j))) {
					State si = span.getOrderedStates().get(i);
					State sj = span.getOrderedStates().get(j);
					System.out.println("States " + si.getId() + " and " + sj.getId() + 
							" not separated pairwise.");
				}
 			}
		}
		*/
		
		Synthesis synth = new Synthesis(lts);
		
		synth.checkStateSeparation();
		
		// ESSA
		
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
