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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

/**
 * An implementation of the synet Petri net synthesis
 * algorithm as described in
 * 
 * <p>
 * Badouel, Éric and Caillaud, Benoît: Distributing Finite Automata Through Petri Net Synthesis.
 * Formal Aspects of Computing 13(6), pp. 447-470
 * </p>
 * 
 * @author Thomas Strathmann
 *
 */
public class Synthesis {

	private final TransitionSystem lts;
		
	private final SpanningTree span;
	
	private ArrayList<int[]> generators;
	
	private HashSet<IntVector> solutions;
	
	private final boolean loggingEnabled;

	private boolean separated;
	
	private boolean separationChecked;
	
	
	private static final BigDecimal minusOne = new BigDecimal(-1);
	
	
	public Synthesis(TransitionSystem lts) {
		this(lts, false);
	}
	
	public Synthesis(TransitionSystem lts, boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
		
		this.lts = lts;
		
		this.span = new SpanningTree(lts);
		
		int[][] A = span.matrix();
		
		if(loggingEnabled) {
			System.err.println("Matrix:");
			System.err.println(LinearAlgebra.matrixToString(A));
		}
		
		this.generators = new ArrayList<int[]>(LinearAlgebra.solutionBasis(A));
		
		if(loggingEnabled) {
			System.err.println("Generators:");
			for(int[] g : this.generators) {
				System.err.println("  " + Arrays.toString(g));
			}
		}
	}
	
	/**
	 * Check if the LTS is separated (i.e. the algorithm can
	 * synthesize a net with an isomorphic reachability graph).
	 * 
	 * @return true iff the LTS satisfies both separation axioms
	 */
	public boolean isSeparated() {
		// do not recompute anything
		if(this.separationChecked)
			return this.separated;
		
		if(lts.getEdges().isEmpty()) {
			// if the LTS does not contain any arcs we need not do any work
			this.separated = true;
		} else {
			this.separated = checkStateSeparation();			
			this.separated = checkStateEventSeparation();		
		}
		
		this.separationChecked = true;
		
		return this.separated;
	}
	
	/**
	 * Synthesize a Petri net from the given LTS.
	 * 
	 * @return a Petri net built from the regions of the LTS computed
	 * 		by the algorithm (regardless of separation)
	 */
	public PetriNet getPetriNet() {
		// check if ...
		this.isSeparated();
		
		ArrayList<int[]> gens = computeAdmissibleRegionsGenerators();
		ArrayList<Region> admissibleRegions = computeRegions(gens);
		
		// build the resulting net
		PetriNet net = new PetriNet();
		
		for(String e : lts.getAlphabet()) {
			Transition t = net.createTransition(e);
			t.setLabel(e);
		}
		
		if(loggingEnabled) System.err.println();
		for(int i=0; i<admissibleRegions.size(); ++i) {
			Region r = admissibleRegions.get(i);
			final String id = "x_" + i; 
			Place p = net.createPlace(id);
			
			p.setInitialToken(r.sigma.get(lts.getInitialState()));
			
			for(String e : lts.getAlphabet()) {
				net.createFlow(id, e, r.pre.get(e));
				net.createFlow(e, id, r.post.get(e));
			}
			
			if(loggingEnabled) System.err.println(r.toString());
		}
		
		return net;
	}
	
	private boolean checkStateSeparation() {
		boolean separated = true;
		final int n = lts.getNodes().size();
		
		if(loggingEnabled) System.err.println("Checking state/state separation:");
		
		ArrayList<int[]> columns = new ArrayList<int[]>(n); 
		for(State s : span.getOrderedStates()) {
			int[] col = new int[generators.size()];
			int i = 0;
			if(loggingEnabled) System.err.print("  " + s.getId() + ": ");
			for(int[] eta : generators) {
				int[] psi = span.parikhVector(s);
				col[i++] = LinearAlgebra.dotProduct(eta, psi);
				if(loggingEnabled) System.err.print(String.format("%3d  ", col[i-1]));
			}
			if(loggingEnabled) System.err.println();
			columns.add(col);
		}
		
		for(int i=0; i<n; ++i) {
			for(int j=i+1; j<n; ++j) {
				if(Arrays.equals(columns.get(i), columns.get(j))) {
					State si = span.getOrderedStates().get(i);
					State sj = span.getOrderedStates().get(j);
					System.out.println("States " + si.getId() + " and " + sj.getId() + 
							" are not separated.");
					separated = false;
				}
 			}
		}
		
		return separated;
	}
	
	
	private boolean checkStateEventSeparation() {
		boolean separated = true;
		
		if(loggingEnabled) System.err.println("Checking ESSA: ");
		
		solutions = new HashSet<>();
		
		// check separation of each pair (s, e) where
		// s is a state of the LTS and e an event
		// that is disabled at s
		for(State s : lts.getNodes()) {
			for(String e : lts.getAlphabet()) {
				if(s.activates(e)) continue;

				final ExpressionsBasedModel model = buildSystem(s, e);
				Optimisation.Result result = model.solve();

				// if the system has a solution, collect the results
				if(result.getState().isSuccess()) {
					if(loggingEnabled) System.err.println("with solutions: ");
					IntVector sol = new IntVector(generators.size());
					for(int i=0; i<generators.size(); ++i) {
						BigDecimal x = result.get(i);
						BigDecimal rounded = x.round(MathContext.DECIMAL32);
						int value = rounded.intValueExact();
						sol.v[i] = value;
						if(loggingEnabled) System.err.println(String.format(" z%d = %2d (rounded from %s)", i, value, x));
					}
					solutions.add(sol);
				} else {
					System.out.println("Event " + e + " and state " + s.getId() + " are not separated.");
					separated = false;
				}
			}
		}
		
		if(loggingEnabled && separated) {
			System.err.println("unique solutions to ESSA linear inequality systems: ");
			for(IntVector sol : solutions) {
				for(int i=0; i<sol.v.length; ++i) {
					System.err.print(String.format("z%d = %2d%s", i, sol.v[i],
							i < sol.v.length-1 ? ", " : ""));
				}
				System.err.println();
			}
		}
		
		return separated;
	}
	
	private ArrayList<int[]> computeAdmissibleRegionsGenerators() {
		assert(solutions != null);
		
		// from the solutions to the ESSA problem calculate a new set of generators
		ArrayList<int[]> newGenerators = new ArrayList<>();
		for(IntVector sol : solutions) {
			int[] v = sol.v;
			int[] g = new int[lts.getAlphabet().size()];
			for(int i=0; i<v.length; ++i) {
				for(int j=0; j<g.length; ++j) {
					g[j] += v[i] * generators.get(i)[j];
				}
			}	
			newGenerators.add(g);
		}
	
		return newGenerators;
	}
	
	// Definition 2.10
	private ArrayList<Region> computeRegions(ArrayList<int[]> generators) {
		ArrayList<Region> regions = new ArrayList<>();
		
		final State s0 = lts.getInitialState();
		final List<String> alphabet = span.getOrderedAlphabet();
		
		for(int[] eta : generators) {
			Region r = new Region(eta);
			
			int sigma_s0 = 0;
			for(State s : lts.getNodes()) {
				int x = pathIntegral(s, s0, eta);
				if(x > sigma_s0) sigma_s0 = x;
			}
			r.sigma.put(s0, sigma_s0);
			
			for(State s : lts.getNodes()) {
				if(s == s0) continue;
				int x = sigma_s0 + pathIntegral(s0, s, eta);
				r.sigma.put(s, x);
			}
			
			for(int i=0; i<alphabet.size(); ++i) {
				String e = alphabet.get(i); 
				
				int min = Integer.MAX_VALUE;
				for(State s : lts.getNodes()) {
					if(s.activates(e)) {
						int sigma_s = r.sigma.get(s);
						if(sigma_s < min) min = sigma_s;
					}
				}
				r.pre.put(e, min);
				
				r.post.put(e, eta[i] + min);
			}
			
			regions.add(r);
		}
		
		return regions;
	}
	
	private ExpressionsBasedModel buildSystem(State s, String e) {
		final ExpressionsBasedModel model = new ExpressionsBasedModel();
		
		for(int i=0; i<generators.size(); ++i) {
			Variable zi = Variable.make("z" + i).integer(true);
			model.addVariable(zi);
		}
		
		if(loggingEnabled) System.err.println("inequality system for (" + s.getId() + ", " + e + "):");
		
		for(State s2 : lts.getNodes()) {
			if(!s2.activates(e)) continue;
			
			if(loggingEnabled) System.err.print(" " + s2.getId() + ": ");
			
			final String id = String.format("c_%s_%s_%s", s.getId(), s2.getId(), e);
			final Expression c = model.addExpression(id).upper(minusOne);
			for(int i=0; i<generators.size(); ++i) {
				int b = beta(i, s, s2);
				c.setLinearFactor(i, b);
				
				if(loggingEnabled) System.err.print(String.format("%s%2d * z%d",
						i > 0 ? " + " : "",	b, i));	
			}
			
			if(loggingEnabled) System.err.println(" < 0");
		}
		
		return model;
	}

	private int beta(int i, State s1, State s2) {
		int[] eta = generators.get(i);
		int[] psi1 = span.parikhVector(s1);
		int[] psi2 = span.parikhVector(s2);
		/*
		System.out.println("## psi(P_" + s1.getId() + ") = " + Arrays.toString(psi1));
		System.out.println("## psi(P_" + s2.getId() + ") = " + Arrays.toString(psi2));
		*/
		int a1 = LinearAlgebra.dotProduct(eta, psi1);
		int a2 = LinearAlgebra.dotProduct(eta, psi2);
		return a1 - a2;
	}
	
	private int pathIntegral(State s1, State s2, int[] eta) {
		int[] psi = span.pathWeights(s1, s2);
		int pi = LinearAlgebra.dotProduct(eta, psi);
		/*
		System.err.print(String.format("integral(%s, %s, %s) = %d",
				s1.getId(), s2.getId(), Arrays.toString(eta), pi));
		System.err.println("  [with weights: " + Arrays.toString(psi) + "]");
		*/
		return pi;
	}
	
	
	private class IntVector {
		public int[] v;
		
		public IntVector(final int size) {
			this.v = new int[size];
		}
		
		@Override
		public boolean equals(Object that) {
			return (that instanceof IntVector) && Arrays.equals(this.v, ((IntVector)that).v);
		}
		
		@Override
		public int hashCode() {
			return Arrays.hashCode(this.v);
		}	
	}
	
	private class Region {
		public int[] generator;
		public HashMap<State, Integer> sigma = new HashMap<>();
		public HashMap<String, Integer> pre = new HashMap<>();
		public HashMap<String, Integer> post = new HashMap<>();
		
		public Region(final int[] generator) {
			this.generator = generator;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			
			sb.append("Region for generator ");
			sb.append(Arrays.toString(generator));
			sb.append(System.lineSeparator());
			
			for(State s : new TreeSet<>(sigma.keySet())) {
				sb.append(String.format("sigma(%s) = %2d", s.getId(), sigma.get(s)));
				sb.append(System.lineSeparator());
			}
			
			for(String e : new TreeSet<>(pre.keySet())) {
				sb.append(String.format("pre(%s) = %2d\tpost(%s) = %2d",
						e, pre.get(e), e, post.get(e)));
				sb.append(System.lineSeparator());
			}
			
			return sb.toString();
		}
	}
	
}
