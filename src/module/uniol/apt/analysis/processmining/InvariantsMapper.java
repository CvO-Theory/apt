/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016  Uli Schlachter
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

package uniol.apt.analysis.processmining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.Transformer;

import uniol.apt.adt.ts.ParikhVector;
import uniol.apt.analysis.processmining.algebra.ArrayMatrix;
import uniol.apt.analysis.processmining.algebra.Matrix;
import uniol.apt.analysis.processmining.algebra.SmithNormalForm;

import static uniol.apt.util.DebugUtil.debug;
import static uniol.apt.util.DebugUtil.debugFormat;

/**
 * @author Uli Schlachter
 */
public class InvariantsMapper implements Transformer<ParikhVector, List<Integer>> {
	private final List<String> alphabetList;
	private final List<List<Integer>> newEventWeights = new ArrayList<>();

	/**
	 * Define an invariant mapper over the given alphabet with the given words being invariant. All {@link
	 * ParikhVector}s that should later be mapped must be over the given alphabet. For example, with the only
	 * invariant being "a","b", the words "c" and "a","c","b" will be mapped to the equal results.
	 * @param alphabet The alphabet over which Parikh vectors will be given.
	 * @param invariantWords A collection of words which should be invariant.
	 */
	public InvariantsMapper(Set<String> alphabet, Collection<List<String>> invariantWords) {
		alphabetList = new ArrayList<>(alphabet);

		if (alphabetList.isEmpty())
			return;

		Set<ParikhVector> invariants = new HashSet<>();

		for (List<String> word : invariantWords) {
			ParikhVector wordPV = new ParikhVector(word);
			invariants.add(wordPV);
			if (!alphabet.containsAll(wordPV.getLabels()))
				throw new IllegalArgumentException();
		}

		Matrix transformation;
		List<Integer> diagonals;

		if (invariants.isEmpty()) {
			// This would need a matrix with dimension zero. Instead, give the result explicitly.
			debug("No invariants giving, using identity mapping");
			transformation = ArrayMatrix.createIdentityMatrix(alphabetList.size(), alphabetList.size());
			diagonals = Collections.emptyList();
		} else {
			// Turn the invariants into a matrix where each column is the Parikh vector of an invariant
			Matrix matrix = ArrayMatrix.createIdentityMatrix(alphabetList.size(), invariants.size());
			int invariantNumber = 0;
			for (ParikhVector invariant : invariants) {
				int eventNumber = 0;
				for (String event : alphabetList) {
					matrix.set(eventNumber, invariantNumber, invariant.get(event));
					eventNumber++;
				}
				invariantNumber++;
			}

			// Calculate the Smith normal form of the above matrix
			SmithNormalForm nf = new SmithNormalForm(matrix);
			transformation = nf.getLeftHandMatrixInverse();
			diagonals = nf.getDiagonalEntries();
			debugFormat("Calculated smith normal form: %s*(%s)*%s",
					nf.getLeftHandMatrix(), diagonals, nf.getRightHandMatrix());
			debugFormat("Inverse matrices are %s and %s",
					nf.getLeftHandMatrixInverse(), nf.getRightHandMatrixInverse());
		}

		// Each non-zero diagonal entry corresponds to a "vanishing dimension", i.e. some new-event who will be
		// ignored in the following. Each zero diagonal entry produces a new event.

		for (int index = 0; index < alphabetList.size(); index++) {
			int diag = index < diagonals.size() ? diagonals.get(index) : 0;
			if (diag != 0) {
				// haven't reached the zero entries yet
				continue;
			}

			List<Integer> weights = new ArrayList<>();
			int eventIndex = 0;
			for (String event : alphabetList) {
				weights.add(transformation.get(index, eventIndex));
				eventIndex++;
			}
			newEventWeights.add(weights);
			debugFormat("New event e_%d has weights %s in %s", index, weights, alphabetList);
		}
	}

	@Override
	public List<Integer> transform(ParikhVector pv) {
		if (!alphabetList.containsAll(pv.getLabels()))
			throw new IllegalArgumentException();
		List<Integer> result = new ArrayList<>();
		for (List<Integer> eventWeights : newEventWeights) {
			int count = 0;
			for (int index = 0; index < alphabetList.size(); index++)
				count += eventWeights.get(index) * pv.get(alphabetList.get(index));
			result.add(count);
		}
		return result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
