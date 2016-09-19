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

package uniol.apt.analysis.trapsAndSiphons;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.copyOf;

import org.sat4j.core.VecInt;
import org.sat4j.core.ReadOnlyVecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.opt.MinOneDecorator;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.OptToSatAdapter;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * Computes either all minimal traps or all minimal siphons in a Petri net pn by
 * using an iterated SAT Algorithm.
 *
 * @author Maike Schwammberger, Uli Schlachter
 *
 */
public class TrapsAndSiphonsLogic {

	private ArrayList<Place> placesList;
	// Representation of CNF for SAT Solver
	private ArrayList<VecInt> clauses = new ArrayList<>();
	private Set<Set<Place>> result = new HashSet<Set<Place>>();
	private boolean searchForTraps;
	private boolean searchForSiphons;

	/**
	 *
	 * @param pn
	 *            Petri net that should be examined.
	 * @param siphons
	 *            true if all minimal siphons shall be computed.
	 * @param traps
	 *            true if all minimal traps shall be computed.
	 */
	public TrapsAndSiphonsLogic(PetriNet pn, boolean siphons, boolean traps) {
		if (siphons) {
			this.searchForSiphons = siphons;
		} else if (traps && !siphons) {
			this.searchForTraps = traps;
		}
		placesList = new ArrayList<Place>(pn.getPlaces());
		try {
			start();
		} catch (ContradictionException e) {
			// All solutions were found and we are done, ignore the exception
		} catch (TimeoutException e) {
			assert false : "We set no timeout and thus timeouts cannot occur";
		}
	}

	// Starts algorithm.
	private void start() throws ContradictionException, TimeoutException {
		// Get CNF for given pn
		computeCNF(placesList);

		// SAT Solver
		ISolver solver;
		do {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
			solver = new OptToSatAdapter(new MinOneDecorator(SolverFactory.newDefault()));

			// Add all clauses to Solver
			for (VecInt clause : clauses) {
				solver.addClause(new ReadOnlyVecInt(clause));
			}
			if (!solver.isSatisfiable()) {
				break;
			}

			// Model, that represents one siphon or trap
			int[] model = solver.model();
			Set<Place> tempSet = new HashSet<Place>();
			for (int i : model) {
				// Take only positive literals for result-siphon or -trap
				if (i > 0) {
					tempSet.add(placesList.get(i - 1));
				}
			}
			// Add minimal siphon or trap to result
			result.add(tempSet);

			// Make sure that no larger traps/siphons are found
			int[] exclude = new int[0];
			for (int i = 0; i < model.length; i++) {
				if (model[i] > 0) {
					exclude = copyOf(exclude, exclude.length + 1);
					exclude[exclude.length - 1] = -model[i];
				}
			}
			assert exclude.length > 0;
			clauses.add(new VecInt(exclude));
		} while (true);

		setResult(result);
	}

	/**
	 * Create a CNF for the boolean model of given pn.
	 *
	 * @param placesList
	 *            ArrayList with all places in pn
	 */
	private void computeCNF(ArrayList<Place> placesList) {
		// Make sure the empty trap/siphon is not found by adding a clause that
		// says "at least one var must be true"
		int[] excludeEmptyClause = new int[placesList.size()];
		for (int i = 1; i <= placesList.size(); i++) {
			excludeEmptyClause[i - 1] = i;
		}
		clauses.add(new VecInt(excludeEmptyClause));

		// To search for siphons build clauses by taking a place and its'
		// pre-set.
		if (searchForSiphons) {
			for (int i = 1; i <= placesList.size(); i++) {
				for (Transition t : placesList.get(i - 1).getPreset()) {
					InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
					int[] clausel = new int[t.getPreset().size() + 1];
					clausel[0] = -i;
					int j = 1;
					for (Place p : t.getPreset()) {
						clausel[j++] = placesList.indexOf(p) + 1;
					}
					clauses.add(new VecInt(clausel));
				}
			}
			// To search for traps, build clauses by taking a place and its'
			// post-set.
		} else if (searchForTraps) {
			for (int i = 1; i <= placesList.size(); i++) {
				for (Transition t : placesList.get(i - 1).getPostset()) {
					InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();
					int[] clausel = new int[t.getPostset().size() + 1];
					clausel[0] = -i;
					int j = 1;
					for (Place p : t.getPostset()) {
						clausel[j++] = placesList.indexOf(p) + 1;
					}
					clauses.add(new VecInt(clausel));
				}
			}
		}
	}

	public Set<Set<Place>> getResult() {
		return result;
	}

	public void setResult(Set<Set<Place>> result) {
		this.result = result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
