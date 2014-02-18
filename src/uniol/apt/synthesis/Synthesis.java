package uniol.apt.synthesis;

import java.math.BigDecimal;
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

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

public class Synthesis {

	private final TransitionSystem lts;
		
	private final SpanningTree span;
	
	private ArrayList<int[]> generators;
	
	private HashSet<IntVector> solutions;
	
	
	private static final BigDecimal minusOne = new BigDecimal(-1);
	
	
	public Synthesis(TransitionSystem lts) {
		this.lts = lts;
		
		this.span = new SpanningTree(lts);
		
		int[][] A = span.matrix();		
		System.err.println("matrix:");
		LinearAlgebra.printMatrix(A);
		
		generators = new ArrayList<int[]>(LinearAlgebra.solutionBasis(A));
		System.err.println("generators:");
		for(int[] g : generators) {
			System.err.println("  " + Arrays.toString(g));
		}
	}
	
	public boolean checkStateSeparation() {
		boolean separated = true;
		
		System.err.println("Checking SSA: ");
		ArrayList<int[]> columns = new ArrayList<int[]>(lts.getNodes().size()); 
		for(State s : span.getOrderedStates()) {
			int[] col = new int[generators.size()];
			int i = 0;
			System.err.print("  " + s.getId() + ": ");
			for(int[] eta : generators) {
				int[] psi = span.parikhVector(s);
				col[i++] = LinearAlgebra.dotProduct(eta, psi);
				System.err.print(String.format("%3d  ", col[i-1]));
			}
			System.err.println();
			columns.add(col);
		}
		
		final int n = lts.getNodes().size();
		for(int i=0; i<n; ++i) {
			for(int j=i+1; j<n; ++j) {
				if(Arrays.equals(columns.get(i), columns.get(j))) {
					State si = span.getOrderedStates().get(i);
					State sj = span.getOrderedStates().get(j);
					System.out.println("States " + si.getId() + " and " + sj.getId() + 
							" not separated.");
					separated = false;
				}
 			}
		}
		
		return separated;
	}
	
	
	public boolean checkStateEventSeparation() {
		boolean separated = true;
		
		System.err.println("Checking ESSA: ");
		
		solutions = new HashSet<>();
		
		// check separation of each pair (s, e) where
		// s is a state of the LTS and e an event
		// that is disabled at s
		for(State s : lts.getNodes()) {
			for(String e : lts.getAlphabet()) {
				if(s.activates(e)) continue;

				final ExpressionsBasedModel model = buildSystem(s, e);
				//final IntegerSolver solver = IntegerSolver.make(model);
				Optimisation.Result result = model.minimise();//solver.solve();

				// if the system has a solution, collect the results
				if(hasSolution(result)) {
					IntVector sol = new IntVector(generators.size());
					for(int i=0; i<generators.size(); ++i) {
						/*
						BigDecimal x = result.get(i);
						BigDecimal rounded = x.round(MathContext.DECIMAL32);
						System.err.println("****** z_" + i + " = " + x + " rounded to " + rounded);
						int value = rounded.intValueExact();
						*/
						int value = result.get(i).intValueExact();
						sol.v[i] = value;
					}
					solutions.add(sol);
				} else {
					System.err.println("Event " + e + " and state " + s.getId() + " not separated.");
					separated = false;
				}
			}
		}
		
		if(separated) {
			System.err.println("solutions to ESSA linear inequality systems: ");
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
	
	public ArrayList<int[]> computeAdmissibleRegions() {
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
	public ArrayList<Region> computeRegions(ArrayList<int[]> generators) {
		ArrayList<Region> regions = new ArrayList<>();
		
		final State s0 = lts.getInitialState();
		final List<String> alphabet = span.getOrderedAlphabet();
		
		for(int[] eta : generators) {
			Region r = new Region();
			r.generator = eta;
			
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
		
		for(State s2 : lts.getNodes()) {
			if(!s2.activates(e)) continue;
			
			final Expression c = model.addExpression("").upper(minusOne);
			for(int i=0; i<generators.size(); ++i) {
				c.setLinearFactor(i, beta(i, s, s2));
			}
		}
		
		return model;
	}

	// TODO: make sure that this is really correct!
	private static boolean hasSolution(Optimisation.Result result) {
		switch(result.getState()) {
		case OPTIMAL:
		case DISTINCT:
		case INDISTINCT:
		case FEASIBLE:
			return true;

		default:
			return false;
		}
	}

	private int beta(int i, State s1, State s2) {
		int[] eta = generators.get(i);
		int[] psi1 = span.parikhVector(s1);
		int[] psi2 = span.parikhVector(s2);
		int a1 = LinearAlgebra.dotProduct(eta, psi1);
		int a2 = LinearAlgebra.dotProduct(eta, psi2);
		return a1 - a2;
	}
	
	private int pathIntegral(State s1, State s2, int[] eta) {
		int[] psi = span.pathWeights(s1, s2);
		int pi = LinearAlgebra.dotProduct(eta, psi);
		/*
		System.err.println(String.format("integral(%s, %s, %s) = %d",
				s1.getId(), s2.getId(), Arrays.toString(eta), pi));
		System.err.println("   [with weights: " + Arrays.toString(psi) + "]");
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
	
	public class Region {
		public int[] generator;
		public HashMap<State, Integer> sigma = new HashMap<>();
		public HashMap<String, Integer> pre = new HashMap<>();
		public HashMap<String, Integer> post = new HashMap<>();
		
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
