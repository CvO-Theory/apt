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

package uniol.apt.pnanalysis;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;

/**
 * Iterator for cycle-T-systems with a certain number of states in his reachability graph and if necessary with a
 * maximum number of token in the T-system.
 * @author Raffaela Ferrari
 *
 */
public class CycleTNetIterator implements Iterator<PetriNet> {
	// Size g
	private final int maxPlaces;
	//number of states in the reachability graph of the given Petri net
	private final int stateNumber;
	//number of arcs from the initial state in the reachability graph of the given Petri net
	private final int initialStateArcsSize;
	//maximal number of token, for which a T-system should be created
	private int maxToken = -1;
	//current number of places, for which a T-net should be created
	private int placesCount = 1;
	//current t-system
	private PetriNet currentPn;
	//same as above, but for hasNext()-asking
	private int currentPlacesCount;

	//variable for remembering that hasNext has been called
	private boolean hasNextWasCalled;

	/**
	 * Creates a Iterator for cycle-T-Systems
	 * @param stateNumber number of states in the reachability graph of the given Petri net
	 * @param maxPlaces Size g
	 * @param maxToken maximal number of token, for which a T-system should be created
	 * @param initialStateArcsSize number of arcs from the initial state in the reachability graph of the given
	 * Petri net
	 */
	public CycleTNetIterator(int stateNumber, int maxPlaces, Integer maxToken, int initialStateArcsSize) {
		assert stateNumber > 0;
		assert maxPlaces > 0;
		this.maxPlaces = maxPlaces;
		this.stateNumber = stateNumber;
		this.initialStateArcsSize = initialStateArcsSize;
		this.hasNextWasCalled = false;
		if (maxToken != null && maxToken >= 0) {
			this.maxToken = maxToken;
		}
	}

	/**
	 * Creates a T-system with a certain number of places and a certain number of tokens.
	 * @param placesSize number of places, for which a T-system should be created
	 * @param token number of token, for which a T-system should be created
	 * @param initialStateArcsSize number of arcs from the initial state in the reachability graph of the given
	 * Petri net
	 * @return a T-system with a certain number of places and a certain number of tokens
	 */
	private static PetriNet createCycleTNet(int placesSize, int token, int initialStateArcsSize) {
		if (placesSize < initialStateArcsSize || token < initialStateArcsSize) {
			return null;
		}
		PetriNet pn = new PetriNet();
		List<Place> places = new ArrayList<>();
		List<Transition> transitions = new ArrayList<>();
		for (int i = 0; i < placesSize; i++) {
			Place p = pn.createPlace();
			places.add(p);
			Transition t = pn.createTransition();
			transitions.add(t);
		}
		Map<String, Integer> markingMap = new HashMap<>();
		int placesWithToken = initialStateArcsSize;
		for (Place place : places) {
			int indexOfPlace = places.indexOf(place);
			pn.createFlow(transitions.get(indexOfPlace), place);
			pn.createFlow(place, transitions.get((indexOfPlace + 1) % placesSize));
			if (placesWithToken > 1) {
				markingMap.put(place.getId(), token / initialStateArcsSize);
				token = token - (token / initialStateArcsSize);
			} else if (placesWithToken == 1) {
				markingMap.put(place.getId(), token);
			}
			if (placesWithToken > 0) {
				placesWithToken -= 1;
			}
		}
		pn.setInitialMarking(new Marking(pn, markingMap));
		return pn;
	}

	@Override
	public boolean hasNext() {
		hasNextWasCalled = true;
		currentPlacesCount = placesCount;
		while (currentPlacesCount <= maxPlaces) {
			int stateCount = 0;
			int tokenNumber = 1;
			while (stateCount <= stateNumber) {
				stateCount = computeBinomialCoeffizient(new BigInteger(String
							.valueOf(currentPlacesCount)),
						new BigInteger(String.valueOf(tokenNumber)));
				if (stateCount == stateNumber) {
					currentPn = createCycleTNet(currentPlacesCount, tokenNumber,
							initialStateArcsSize);
					if (currentPn != null) {
						return true;
					}
					break;
				}
				if (currentPlacesCount == 1) {
					break;
				}
				tokenNumber++;
				if (maxToken != -1 && tokenNumber > maxToken) {
					break;
				}
			}
			currentPlacesCount++;
		}
		return false;
	}

	@Override
	public PetriNet next() {
		if (!hasNextWasCalled && !hasNext()) {
			throw new NoSuchElementException();
		}
		hasNextWasCalled = false;
		placesCount = currentPlacesCount + 1;
		return currentPn;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Compute the Binomial coeffizient for combination with repition
	 * @param n size of a set of elements
	 * @param k elements, which should be selected of n
	 * @return the Binomial coeffizient
	 */
	private int computeBinomialCoeffizient(BigInteger n, BigInteger k) {
		n = n.add(k).subtract(BigInteger.ONE);
		BigInteger nMinusK = n.subtract(k);
		if (nMinusK.compareTo(k) < 0) {
			BigInteger temp = k;
			k = nMinusK;
			nMinusK = temp;
		}

		BigInteger numerator = BigInteger.ONE;
		BigInteger denominator = BigInteger.ONE;

		for (BigInteger j = BigInteger.ONE; j.compareTo(k) <= 0; j = j.add(BigInteger.ONE)) {
			numerator = numerator.multiply(j.add(nMinusK));
			denominator = denominator.multiply(j);
			BigInteger gcd = numerator.gcd(denominator);
			numerator = numerator.divide(gcd);
			denominator = denominator.divide(gcd);
		}
		return numerator.intValue();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
