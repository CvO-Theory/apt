package uniol.apt.synthesis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.IntegerSolver;

public class Synthesis {

	private final TransitionSystem lts;
		
	private final SpanningTree span;
	
	private ArrayList<int[]> generators; 
	
	
	private static final BigDecimal minusOne = new BigDecimal(-1);
	
	
	public Synthesis(TransitionSystem lts) {
		this.lts = lts;
		
		this.span = new SpanningTree(lts);
		
		int[][] A = span.matrix();
		this.generators = new ArrayList<int[]>(LinearAlgebra.solutionBasis(A));
		// if the generating set is empty, add the zero vector
		// to ensure that later stages of the algorithm work as expected
		if(generators.isEmpty()) {
			int[] zero = new int[lts.getAlphabet().size()];
			generators.add(zero);
		}
	}
	
	public boolean checkStateSeparation() {
		boolean separated = true;
		
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
		
		final int n = lts.getNodes().size();
		for(int i=0; i<n; ++i) {
			for(int j=i+1; j<n; ++j) {
				if(Arrays.equals(columns.get(i), columns.get(j))) {
					State si = span.getOrderedStates().get(i);
					State sj = span.getOrderedStates().get(j);
					System.out.println("States " + si.getId() + " and " + sj.getId() + 
							" not separated pairwise.");
					separated = false;
				}
 			}
		}
		
		return separated;
	}
	
	
	public boolean checkStateEventSeparation() {
		boolean separated = true;
		
		ArrayList<HashSet<Integer>> solutions = new ArrayList<HashSet<Integer>>();
		for(int i=0; i<generators.size(); ++i) {
			solutions.add(new HashSet<Integer>());
 		}
		
		// check separation of each pair (s, e) where
		// s is a state of the LTS and e an event
		// that is disabled at s
		for(State s : lts.getNodes()) {
			for(String e : lts.getAlphabet()) {
				if(s.activates(e)) continue;
				
				final ExpressionsBasedModel model = buildSystem(s, e);
				final IntegerSolver solver = IntegerSolver.make(model);
				Optimisation.Result result = solver.solve();
			
				//System.out.println("(" + s + ", " + e + ") ---> " + result);
				
				// if the system has a solution, collect the results
				if(hasSolution(result)) {
					for(int i=0; i<generators.size(); ++i) {
						int value = result.get(i).intValueExact();
						solutions.get(i).add(value);
					}
				} else {
					System.out.println("Event " + e + " and state " + s.getId() + " not separated.");
					separated = false;
				}
			}
		}
		
		if(separated) {
			System.out.println("solutions: ");
			for(int i=0; i<generators.size(); ++i) {
				for(int x : solutions.get(i)) {
					System.out.println("z" + i + " = " + x);
				}
			}
		}
		
		
		return separated;
	}
	
	private ExpressionsBasedModel buildSystem(State s, String e) {
		final ExpressionsBasedModel model = new ExpressionsBasedModel();
		
		//ArrayList<Variable> vars = new ArrayList<Variable>();
		for(int i=0; i<generators.size(); ++i) {
			Variable zi = Variable.make("z" + i).integer(true);
			model.addVariable(zi);
			//vars.add(zi);
		}
		
		for(State s2 : lts.getNodes()) {
			if(!s2.activates(e)) continue;
			
			final Expression c = model.addExpression("").upper(minusOne);
			for(int i=0; i<generators.size(); ++i) {
				//int b = beta(i, s, s2);
				//System.out.println("beta(" + i + ", " + s + ", " + e + ") = " + b);
				c.setLinearFactor(i /*vars.get(i)*/, beta(i, s, s2));
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
		return LinearAlgebra.dotProduct(eta, psi);
	}
	
}
