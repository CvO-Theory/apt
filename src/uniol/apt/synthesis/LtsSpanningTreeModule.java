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

import java.util.HashMap;
import java.util.Set;

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
		Set<HashMap<String, Integer>> rows = span.cyclesParikhVectors();
		final int n = lts.getAlphabet().size();
		int[][] A = new int[rows.size()][n];
		int i = 0;
		for(HashMap<String, Integer> row : rows) {
			int j = 0; 
			for(String a : span.getOrderedAlphabet()) {
				A[i][j++] = row.get(a);
			}
			++i;
			System.err.println(row);
		}
		
		System.err.println("generators:");	
		Set<int[]> generators = LinearAlgebra.solutionBasis(A);
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
