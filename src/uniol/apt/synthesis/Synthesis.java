package uniol.apt.synthesis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

public class Synthesis {

	private final TransitionSystem lts;
		
	private final SpanningTree span;
	
	private ArrayList<int[]> generators; 
	
	
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
