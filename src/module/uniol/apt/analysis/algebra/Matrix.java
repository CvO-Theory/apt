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

	private String[][] matrix;
	private String[][] stringIncidence;
	private String[][] stringBackward;
	private String[][] stringForward;
	private PetriNet pn;
	private Set<Flow> arcs;

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

		this.arcs = this.pn.getEdges();
	}

	/**
	 * computes the forwardmatrix of a given PN
	 * @return forwardmatrix
	 */
	public String[][] getStringForward() {
		// create forward matrix
		this.stringForward = new String[this.pn.getPlaces().size() + 1][this.pn
			.getTransitions().size() + 1];
		for (int i = 0; i < this.matrix.length; i++) {
			for (int j = 0; j < this.matrix[0].length; j++) {
				String value = this.matrix[i][j];
				this.stringForward[i][j] = value;
			}
		}
		this.stringForward[0][0] = "F";
		for (int i = 1; i < this.matrix.length; i++) {
			for (int j = 1; j < this.matrix[0].length; j++) {
				String place = matrix[i][0];
				String transition = matrix[0][j];
				// fill forward matrix with 0
				// this.stringForward[i - 1][j - 1] = "0";
				this.stringForward[i][j] = "0";
				// run through arcs and look up for matching places &
				// transitions
				for (Flow arc : this.arcs) {
					if (place == arc.getTarget().getId()
						&& transition == arc.getSource().getId()) {
						this.stringForward[i][j] = String.valueOf(arc
							.getWeight());
					}
				}

			}
		}
		return this.stringForward;
	}

	/**
	 * computes the backwardmatrix of a given PN
	 * @return backwardmatrix
	 */
	public String[][] getStringBackward() {
		this.stringBackward = new String[this.pn.getPlaces().size() + 1][this.pn
			.getTransitions().size() + 1];
		for (int i = 0; i < this.matrix.length; i++) {
			for (int j = 0; j < this.matrix[0].length; j++) {
				String value = this.matrix[i][j];
				this.stringBackward[i][j] = value;
			}
		}
		this.stringBackward[0][0] = "B";
		for (int i = 1; i < this.matrix.length; i++) {
			for (int j = 1; j < this.matrix[0].length; j++) {
				String place = matrix[i][0];
				String transition = matrix[0][j];
				// fill forward matrix with 0
				this.stringBackward[i][j] = "0";
				// run through arcs and look up for matching places &
				// transitions
				for (Flow arc : this.arcs) {
					if (place == arc.getSource().getId()
						&& transition == arc.getTarget().getId()) {
						this.stringBackward[i][j] = String.valueOf(arc.getWeight());
					}
				}
			}
		}
		return this.stringBackward;
	}

	/**
	 * computes the incidencematrix of a given PN: C = F - B
	 * @return incidencematirx
	 */
	public String[][] getStringIncidence() {
		this.stringIncidence = new String[this.pn.getPlaces().size() + 1][this.pn
			.getTransitions().size() + 1];
		for (int i = 0; i < this.matrix.length; i++) {
			for (int j = 0; j < this.matrix[0].length; j++) {
				String value = this.matrix[i][j];
				this.stringIncidence[i][j] = value;
			}
		}
		// compute forward & backward
		getStringBackward();
		getStringForward();
		for (int i = 1; i < this.stringIncidence.length; i++) {
			for (int j = 1; j < this.stringIncidence[0].length; j++) {
				// C = (F-B)*1
				this.stringIncidence[i][j] = String.valueOf(Integer
					.parseInt(this.stringForward[i][j]) - Integer
					.parseInt(this.stringBackward[i][j]));
			}
		}
		this.stringIncidence[0][0] = "C";
		return this.stringIncidence;
	}

	/**
	 * generates the input for R
	 * @return Matrix as String
	 */
	public String getRMatrices() {
		String matr = "";

		/*
		 * regard all the three types of matrices and prepare console output for
		 * inserting in R
		 */
		for (int i = 0; i < 3; i++) {
			// forward
			if (i == 0) {
				matr += R(getStringForward(), "forward");
			}
			// backward
			if (i == 1) {
				matr += R(getStringBackward(), "backward");
			}
			// incidence
			if (i == 2) {
				matr += R(getStringIncidence(), "incidence");
			}
		}
		return matr;
	}

	public String R(String[][] matrix, String typ) {
		String returner = "";
		// fill each row with values
		for (int i = 1; i < matrix.length; i++) {
			returner += "row" + typ + i + " <- c(";
			for (int j = 1; j < matrix[0].length; j++) {
				if (j != matrix[0].length - 1) {
					returner += matrix[i][j] + ",";
				} else {
					returner += matrix[i][j] + ")";
				}

			}
			returner += "\n";
		}
		// combine rows to matrix
		returner += typ + " <- matrix(c(";
		for (int i = 1; i < matrix.length; i++) {
			if (i != matrix.length - 1) {
				returner += "row" + typ + i + ",";
			} else {
				returner += "row" + typ + i + "),";
				returner += matrix.length - 1 + ") \n";
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
		returner += "colnames(" + typ + ") <- c(";
		for (int i = 0; i < colnames.size(); i++) {
			if (i != colnames.size() - 1) {
				returner += "\"" + colnames.get(i) + "\", ";
			} else {
				returner += "\"" + colnames.get(i) + "\"";
			}
		}
		returner += ") \n";

		// set names of dimension
		returner += "dimnames(" + typ + ") <- list(c(";
		for (int i = 0; i < dimnames.size(); i++) {
			if (i != dimnames.size() - 1) {
				returner += "\"" + dimnames.get(i) + "\", ";
			} else {
				returner += "\"" + dimnames.get(i) + "\"";
			}
		}
		returner += ")) \n \n";

		return returner;
	}

	/*
	 * generates the input for MatLab
	 */
	/**
	 * Creates MatLab String.
	 * @return Matrix as String
	 */
	public String getMatLabMatrices() {
		String matr = null;
		for (int h = 0; h < 3; h++) {
			String tmp[][] = null;
			// prepare each type of matrix

			// forward
			if (h == 0) {
				tmp = getStringForward();
				matr = "forward=[";
			}
			// backward
			if (h == 1) {
				tmp = getStringBackward();
				matr += "\nbackward=[";
			}
			// incidence
			if (h == 2) {
				tmp = getStringIncidence();
				matr += "\nincidence=[";
			}
			for (int i = 0; i < tmp.length; i++) { // i = 1
				for (int j = 0; j < tmp[0].length; j++) { // j = 1
					if (j == tmp[0].length - 1) {
						matr += tmp[i][j] + "; ";
					} else {
						matr += tmp[i][j] + ", ";
					}
				}
			}
			matr += "]";
		}
		return matr;
	}
}
// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120

