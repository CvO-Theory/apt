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

package uniol.apt.analysis.algebra;

import java.util.ArrayList;
import java.util.Set;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * Class for compute and return a backward-, forward and incidencematrix by a given PN
 * @author Björn von der Linde
 */
public class Matrix {

	final private String[][] matrix;
	final private PetriNet pn;

	/**
	 * Constructor
	 * @param net Petri net
	 */
	public Matrix(PetriNet net) {
		this.pn = net;
		this.matrix = new String[this.pn.getPlaces().size() + 1][this.pn
			.getTransitions().size() + 1];
		this.matrix[0][0] = " M";

		int placeStart = 1;
		for (Place p : this.pn.getPlaces()) {
			this.matrix[placeStart][0] = p.getId();
			placeStart++;
		}
		int transStart = 1;
		for (Transition t : this.pn.getTransitions()) {
			this.matrix[0][transStart] = t.getId();
			transStart++;
		}
	}

	/**
	 * computes the forwardmatrix of a given PN
	 * @return forwardmatrix
	 */
	public String[][] getStringForward() {
		// create forward matrix
		String[][] result = new String[this.pn.getPlaces().size() + 1][this.pn.getTransitions().size() + 1];
		for (int i = 0; i < this.matrix.length; i++) {
			for (int j = 0; j < this.matrix[0].length; j++) {
				String value = this.matrix[i][j];
				result[i][j] = value;
			}
		}
		result[0][0] = "F";
		for (int i = 1; i < this.matrix.length; i++) {
			for (int j = 1; j < this.matrix[0].length; j++) {
				String place = matrix[i][0];
				String transition = matrix[0][j];
				// fill forward matrix with 0
				// result[i - 1][j - 1] = "0";
				result[i][j] = "0";
				// run through arcs and look up for matching places &
				// transitions
				for (Flow arc : this.pn.getEdges()) {
					if (place.equals(arc.getTarget().getId())
						&& transition.equals(arc.getSource().getId())) {
						result[i][j] = String.valueOf(arc.getWeight());
					}
				}

			}
		}
		return result;
	}

	/**
	 * computes the backwardmatrix of a given PN
	 * @return backwardmatrix
	 */
	public String[][] getStringBackward() {
		String[][] result = new String[this.pn.getPlaces().size() + 1][this.pn.getTransitions().size() + 1];
		for (int i = 0; i < this.matrix.length; i++) {
			for (int j = 0; j < this.matrix[0].length; j++) {
				String value = this.matrix[i][j];
				result[i][j] = value;
			}
		}
		result[0][0] = "B";
		for (int i = 1; i < this.matrix.length; i++) {
			for (int j = 1; j < this.matrix[0].length; j++) {
				String place = matrix[i][0];
				String transition = matrix[0][j];
				// fill forward matrix with 0
				result[i][j] = "0";
				// run through arcs and look up for matching places &
				// transitions
				for (Flow arc : this.pn.getEdges()) {
					if (place.equals(arc.getSource().getId())
						&& transition.equals(arc.getTarget().getId())) {
						result[i][j] = String.valueOf(arc.getWeight());
					}
				}
			}
		}
		return result;
	}

	/**
	 * computes the incidencematrix of a given PN: C = F - B
	 * @return incidencematirx
	 */
	public String[][] getStringIncidence() {
		String[][] result = new String[this.pn.getPlaces().size() + 1][this.pn.getTransitions().size() + 1];
		for (int i = 0; i < this.matrix.length; i++) {
			for (int j = 0; j < this.matrix[0].length; j++) {
				String value = this.matrix[i][j];
				result[i][j] = value;
			}
		}
		result[0][0] = "C";
		// compute forward & backward
		String[][] backward = getStringBackward();
		String[][]forward = getStringForward();
		for (int i = 1; i < result.length; i++) {
			for (int j = 1; j < result[0].length; j++) {
				// C = (F-B)*1
				result[i][j] = String.valueOf(Integer.parseInt(forward[i][j])
						- Integer.parseInt(backward[i][j]));
			}
		}
		return result;
	}

	/**
	 * generates the input for R
	 * @return Matrix as String
	 */
	public String getRMatrices() {
		StringBuilder result = new StringBuilder();

		getRMatricesHelper(result, getStringForward(), "forward");
		getRMatricesHelper(result, getStringBackward(), "backward");
		getRMatricesHelper(result, getStringIncidence(), "incidence");
		return result.toString();
	}

	static private void getRMatricesHelper(StringBuilder result, String[][] matrix, String typ) {
		// fill each row with values
		for (int i = 1; i < matrix.length; i++) {
			result.append("row").append(typ).append(i).append(" <- c(");
			for (int j = 1; j < matrix[0].length; j++) {
				if (j != matrix[0].length - 1) {
					result.append(matrix[i][j]).append(",");
				} else {
					result.append(matrix[i][j]).append(")");
				}

			}
			result.append("\n");
		}
		// combine rows to matrix
		result.append(typ).append(" <- matrix(c(");
		for (int i = 1; i < matrix.length; i++) {
			if (i != matrix.length - 1) {
				result.append("row").append(typ).append(i).append(",");
			} else {
				result.append("row").append(i).append("),");
				result.append(matrix.length - 1).append(") \n");
			}
		}

		// get names of columns and dimensions
		ArrayList<String> colnames = new ArrayList<>();
		ArrayList<String> dimnames = new ArrayList<>();
		for (int j = 0; j < matrix.length; j++) { // Reihe
			for (int j2 = 0; j2 < matrix[0].length - 1; j2++) { // Spalte
				if (j == 0) {
					colnames.add(matrix[j][j2 + 1]);
				} else if (j2 == 0 && j != 0) {
					dimnames.add(matrix[j][j2]);
				}
			}
		}

		// set names of columns
		result.append("colnames(").append(typ).append(") <- c(");
		for (int i = 0; i < colnames.size(); i++) {
			if (i != colnames.size() - 1) {
				result.append("\"").append(colnames.get(i)).append("\", ");
			} else {
				result.append("\"").append(colnames.get(i)).append("\"");
			}
		}
		result.append(") \n");

		// set names of dimension
		result.append("dimnames(").append(typ).append(") <- list(c(");
		for (int i = 0; i < dimnames.size(); i++) {
			if (i != dimnames.size() - 1) {
				result.append("\"").append(dimnames.get(i)).append("\", ");
			} else {
				result.append("\"").append(dimnames.get(i)).append("\"");
			}
		}
		result.append(")) \n \n");
	}

	/*
	 * generates the input for MatLab
	 */
	/**
	 * Creates MatLab String.
	 * @return Matrix as String
	 */
	public String getMatLabMatrices() {
		StringBuilder result = new StringBuilder();
		for (int h = 0; h < 3; h++) {
			String tmp[][] = null;
			// prepare each type of matrix

			// forward
			if (h == 0) {
				tmp = getStringForward();
				result.append("forward=[");
			}
			// backward
			if (h == 1) {
				tmp = getStringBackward();
				result.append("\nbackward=[");
			}
			// incidence
			if (h == 2) {
				tmp = getStringIncidence();
				result.append("\nincidence=[");
			}
			for (int i = 0; i < tmp.length; i++) { // i = 1
				for (int j = 0; j < tmp[0].length; j++) { // j = 1
					if (j == tmp[0].length - 1) {
						result.append(tmp[i][j]).append("; ");
					} else {
						result.append(tmp[i][j]).append(", ");
					}
				}
			}
			result.append("]");
		}
		return result.toString();
	}
}
// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120

