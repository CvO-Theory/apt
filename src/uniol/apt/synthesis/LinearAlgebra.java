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
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;

/**
 * Linear algebra routines that are used by the synet Petri net
 * synthesis algorithm.
 * 
 * Some of the algorithms are taken more or less verbatim from
 * the following document:
 * [1] Kerstin Susewind: "Berechnungsalgorithmen für Hermite-Normalformen und deren Anwendung zur Bestimmung ganzzahliger Lösungen linearer Gleichungssysteme"
 *     Studienarbeit bei Professor Dr. rer. nat. Kurt Lautenbach (Universität Koblenz-Landau) 18. Februar 2008
 * 
 * @author Thomas Strathmann
 */
public class LinearAlgebra {

	/**
	 * Compute a set of generators of the solution space
	 * for the linear homogeneous diophantine equation
	 * system A * x = 0.
	 * 
	 * @param A the coefficient matrix
	 * @return a set of generators (or null if matrix is empty)
	 */
	public static Set<int[]> solutionBasis(int[][] A) {
		
		// check if A is empty
		if(A.length == 0 || A[0].length == 0) {
			return null;
		}
		
		if(allZeroRows(A)) {
			HashSet<int[]> generators = new HashSet<>();
			final int n = A[0].length;
			for(int i=0; i<n; ++i) {
				int[] g = new int[n];
				g[i] = 1;
				generators.add(g);
			}
			return generators;
		}
		
		final int[][] B = linearlyIndependentRows2(A);
		int[][] U = computeHNF(B)[1];
		
		if(U == null) {
			HashSet<int[]> generators = new HashSet<>();
			int[] g = new int[A[0].length];
			generators.add(g);
			return generators;
		}
	
		int m = B.length;
		int n = B[0].length;
	
		Set<int[]> generators = new HashSet<int[]>();
		for(int k = m; k < n; ++k) {
			int[] gk = new int[n];
			for(int i=0; i<n; ++i) {
				gk[i] = U[i][k];
			}
			generators.add(gk);
		}
	
		return generators;
	}

	/**
	 * Compute the Hermite normal form of the given matrix using
	 * the algorithm by Nemhauser/Wolsey as described in [1].
	 * 
	 * <b>Caveat: This implementation assumes that the
	 * coefficient matrix has full row rank!</b> 
	 * 
	 * @param A the matrix
	 * @return a pair (H, U) where H is the Hermite normal form of A
	 * 		and U is a unimodular matrix such that U*A = H 
	 */
	private static int[][][] computeHNF(int[][] A) {
		assert(A != null);
		assert(A.length > 0);
		
		final int m = A.length;
		if(m == 0) return null;
		final int n = A[0].length;
		
		assert(m <= n);

		// initialise
		int[][] H = cloneMatrix(A);
		int[][] U = identity(n);

		// auxiliary variables
		final int[] tmp_i = new int[n];
		final int[] tmp_j = new int[n];		    

		// for all rows of A do
		for(int i=0; i<m; ++i) {
			// step 1: select column
			int j = i + 1;

			// step 2: elimination
			for(; j < n; ++j) {
				if(H[i][j] != 0) {
					int[] euclid = extendedGCD(H[i][i], H[i][j]);
					int r = euclid[0];
					int p = euclid[1];
					int q = euclid[2];

					int alpha = -H[i][j] / r;
					int beta  =  H[i][i] / r;
					for(int l=0; l<m; ++l) {
						tmp_i[l] = p * H[l][i] + q * H[l][j];
						tmp_j[l] = alpha * H[l][i] + beta * H[l][j];
					}
					for(int l=0; l<m; ++l) {
						H[l][i] = tmp_i[l];
						H[l][j] = tmp_j[l];
					}
					for(int l=0; l<n; ++l) {
						tmp_i[l] = p * U[l][i] + q * U[l][j];
						tmp_j[l] = alpha * U[l][i] + beta * U[l][j];
					}
					for(int l=0; l<n; ++l) {
						U[l][i] = tmp_i[l];
						U[l][j] = tmp_j[l];
					}
				}
			}

			// step 3: multiply column with -1
			if(H[i][i] < 0) {
				H[i][i] *= -1;
				U[i][i] *= -1;
			}

			// step 4: normalisation
			j = 0;
			do {
				if(j == i) {
					if(i == m-1) {
						return new int[][][] {H, U};
					} else {
						break;
					}
				}

				int c =  -(int)Math.ceil((double)H[i][j] / H[i][i]);
				for(int l=0; l<m; ++l)
					H[l][j] += H[l][i] * c;
				for(int l=0; l<n; ++l)
					U[l][j] += U[l][i] * c;

				if(j == i-1) {
					if(i >= m-1) {
						return new int[][][] {H, U};
					} else {
						break;
					}
				}

				j += 1;
			} while(j <= i-1);
		}

		return null;
	}

	/**
	 * Implementation of the extended Euclidean algorithm.
	 * Source: [1]
	 * 
	 * @param a first number
	 * @param b second number
	 * @return a triple (y,s,t) such that gcd(a,b) = y = a*s + b*t
	 */
	private static int[] extendedGCD(int a, int b) {
		if(b == 0) {
			if(a > 0) return new int[] {a, 0, 1};
			else return new int[] {-a, 0, -1};
		} else if(a == 0) {
			if(b > 0) return new int[] {b, 0, 1};
			else return new int[] {-b, 0, -1};
		}

		int a_ = Math.abs(a);
		int b_ = Math.abs(b);
		int x, y;
		if(a_ >= b_) {
			x = a_;
			y = b_;
		} else {
			x = b_;
			y = a_;
		}

		int s  = 0;
		int s1 = 1;
		int s2 = 0;
		int t  = 1;
		int t1 = 0;
		int t2 = 1;

		while(x % y != 0) {
			int g = x/y;
			int r = x % y;
			s  = s1 - g*s2;
			s1 = s2;
			s2 = s;
			t  = t1 - g*t2;
			t1 = t2;
			t2 = t;
			x = y;
			y = r;
		}

		if(a_ < b_) {
			int tmp = s;
			s = t;
			t = tmp;
		}

		if(a < 0) {
			s = -s;
		}
		if(b < 0) {
			t = -t;
		}

		return new int[] {y, s, t};
	}

	/**
	 * Return the nxn identity matrix.
	 * 
	 * @param n dimension of the matrix
	 * @return the nxn identity matrix
	 */
	private static int[][] identity(int n) {
		int[][] id = new int[n][n];
		for(int i=0; i<n; ++i) {
			id[i][i] = 1;
		}
		return id;
	}

	/**
	 * Check if a matrix is non-empty and consists only of zero rows.
	 * 
	 * @param A the matrix to check
	 * @return true iff A is non-empty and consists only of zero rows
	 */
	private static boolean allZeroRows(int[][] A) {
		if(A == null || A.length == 0 || A[0].length == 0)
			return false;
		
		for(int i=0; i<A.length; ++i) {
			for(int j=0; j<A[0].length; ++j) {
				if(A[i][j] != 0)
					return false;
			}
		}
		
		return true;
	}

	/**
	 * Return a matrix of the linearly independent rows
	 * of a given matrix.
	 * 
	 * @param A the matrix to be reduced
	 * @return a matrix whose rows are the linearly independent rows of A
	 */
	private static int[][] linearlyIndependentRows2(int [][] A) {
		if(A == null || A.length == 0)
			return A;
		
		final int m = A.length;
		final int n = A[0].length;
		
		if(n == 0) 
			return A;
		
		/***** step 1: apply Gaussian elimination *****/		
		// build an intermediate double matrix
		ArrayList<double[]> B = new ArrayList<>(m);
		for(int i=0; i<m; ++i) {
			double[] row = new double[n];
			for(int j=0; j<n; ++j) {				
				row[j] = A[i][j];
			}
			B.add(row);
		}
		
		// keep a map from row numbers in the resulting matrix
		// to column numbers in the original matrix
		int[] rowIdx = new int[m];
		for(int i=0; i<m; ++i)
			rowIdx[i] = i;
		
		int j = 0;
		for(int i=0; i<m; ++i) {
			if(B.get(i)[j] == 0) {
				boolean swapped = false;
				for(int k=i+1; k<m; ++k) {					
					if(B.get(k)[j] != 0) {
						// swap rows
						double[] tmpRow = B.get(i);
						B.set(i, B.get(k));
						B.set(k, tmpRow);
						int tmp = rowIdx[i];
						rowIdx[i] = rowIdx[k];
						rowIdx[k] = tmp;
						swapped = true;
						break;
					}
				}
				if(!swapped) {
					if(++j >= n) break;
				}
			}

			double pivot = B.get(i)[j];			
			if(pivot != 0.0) {
				for(int k=0; k<n; ++k) {
					B.get(i)[k] /= pivot;
				}
			}

			for(int l=i+1; l<m; ++l) {
				double c = B.get(l)[j];
				if(c == 0)
					continue;
				for(int k=0; k<n; ++k) {
					B.get(l)[k] -= c * B.get(i)[k];
				}
			}

			if(++j >= n) break;
		}
	
		/***** step 2: build reduced matrix *****/
		// copy rows in the original matrix corresponding to
		// non-zero rows in the echelon form of the matrix
		Vector<Integer> nonZeroRows = new Vector<Integer>();
		for(int i=0; i<m; ++i) {
			for(j=0; j<n; ++j) {
				if(B.get(i)[j] != 0.0) {
					nonZeroRows.add(i);
					break;
				}
			}			
		}
		int[][] C = new int[nonZeroRows.size()][n];
		for(int i=0; i<nonZeroRows.size(); ++i) {
			int k = nonZeroRows.get(i);
			System.arraycopy(A[rowIdx[k]], 0, C[i], 0, n);
		}
		
		return C;
	}

	private static int[][] cloneMatrix(int[][] src) {
		int length = src.length;
		int[][] target = new int[length][src[0].length];
		for (int i = 0; i < length; i++) {
			System.arraycopy(src[i], 0, target[i], 0, src[i].length);
		}
		return target;
	}

	/** 
	 * Compute the dot product of two integer vectors
	 * of the same dimension.
	 * 
	 * @param a vector of dimension n
	 * @param b vector of dimension n
	 * @return the dot product of a and b
	 */
	public static int dotProduct(int[] a, int[] b) {
		assert(a != null && b != null);
		assert(a.length == b.length);
		
		int result = 0;
		
		for(int i=0; i<a.length; ++i) {
			result += a[i] * b[i];
		}
		
		return result;
	}

	
	public static String matrixToString(final int[][] A) {
		if(A == null || A.length == 0) {
			return "";
		}
		
		int m = A.length;
		int n = A[0].length;
		
		// find the entry with the longest string representation
		int longest = 0;
		for(int i=0; i<m; ++i) {
			for(int j=0; j<n; ++j) {
				String s = Integer.toString(A[i][j]);
				if(s.length() > longest)
					longest = s.length();
			}
		}

		StringBuilder sb = new StringBuilder();
		final String format = "%" + (longest+2) + "d";

		for(int i=0; i<m; ++i) {
			for(int j=0; j<n; ++j) {
				sb.append(String.format(format, A[i][j]));
			}
			sb.append(System.lineSeparator());
		}
		
		return sb.toString();
	}

	public static void printMatrix(double[][] A) {
		assert(A != null && A.length > 0);
		int m = A.length;
		int n = A[0].length;

		// find the entry with the longest string representation
		int longest = 0;
		for(int i=0; i<m; ++i) {
			for(int j=0; j<n; ++j) {
				String s = String.format("%3.3f", A[i][j]);
				if(s.length() > longest)
					longest = s.length();
			}
		}

		final String format = "%" + (longest+2) + ".3f";

		for(int i=0; i<m; ++i) {
			for(int j=0; j<n; ++j) {
				System.out.print(String.format(format, A[i][j]));
			}
			System.out.println();
		}
	}
 
}