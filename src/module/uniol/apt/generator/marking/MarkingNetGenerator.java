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

package uniol.apt.generator.marking;

import uniol.apt.adt.pn.Token;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import static uniol.apt.generator.GeneratorUtils.cloneNet;

/**
 * Given a Petri net, generate other nets with all possible initial markings up to a given limit m.
 * @author Uli Schlachter
 */
public class MarkingNetGenerator implements Iterable<PetriNet> {
	// The original Petri net which we generate markings for

	private final PetriNet pn;
	// This gives the places of the net an order
	private final List<Place> places = new ArrayList<>();
	// The limit for the total number of token in the initial marking
	private final int tokenLimit;
	// If true, new tokens are added to the initial marking. Otherwise, the initial marking is ignored.
	private final boolean addToInitialMarking;
	// A collection of components, each of which must get at least one token
	private final Collection<Set<Place>> requiredPlaces;

	/**
	 * Create a new MarkingNetGenerator instance for the given arguments.
	 * @param pn The Petri net for which all initial markings are generated.
	 * @param tokenLimit The limit for the number of token on the net.
	 */
	public MarkingNetGenerator(PetriNet pn, int tokenLimit) {
		this(pn, tokenLimit, false);
	}

	/**
	 * Create a new MarkingNetGenerator instance for the given arguments.
	 * @param pn The Petri net for which all initial markings are generated.
	 * @param tokenLimit The limit for the number of token on the net.
	 * @param addToInitialMarking if true, the initial marking of the Petri nets is only increased; otherwise the
	 *        initial marking is ignored and overwritten.
	 */
	public MarkingNetGenerator(PetriNet pn, int tokenLimit, boolean addToInitialMarking) {
		this(pn, tokenLimit, addToInitialMarking, null);
	}

	/**
	 * Create a new MarkingNetGenerator instance for the given arguments.
	 * @param pn The Petri net for which all initial markings are generated.
	 * @param tokenLimit The limit for the number of token on the net.
	 * @param addToInitialMarking if true, the initial marking of the Petri nets is only increased; otherwise the
	 *        initial marking is ignored and overwritten.
	 * @param requiredPlaces a collection of places which must get at least one token. For each element, all
	 *        markings which do not put at least one token on this nodes are ignored.
	 */
	public MarkingNetGenerator(PetriNet pn, int tokenLimit, boolean addToInitialMarking,
			Collection<Set<Place>> requiredPlaces) {
		assert tokenLimit >= 0;
		this.pn = pn;
		this.tokenLimit = tokenLimit;
		this.addToInitialMarking = addToInitialMarking;
		this.places.addAll(pn.getPlaces());
		this.requiredPlaces = requiredPlaces;
	}

	/**
	 * Create a fresh copy of our Petri net. This function deep-copies the Petri Net that was given to this class'
	 * constructor. Then the initial marking of this new Petri net is set to the specified marking.
	 * @param marking The initial marking for the fresh clone of the Petri net
	 * @return A fresh clone of this class' Petri net with the given marking
	 */
	private PetriNet getMarkedNet(Map<String, Integer> marking) {
		PetriNet net = cloneNet(this.pn, this.pn.getName() + " with initial marking " + marking.toString());

		// Now all that is missing is the initial marking
		for (Place p : net.getPlaces()) {
			Integer token = marking.get(p.getId());
			if (token == null) {
				token = 0;
			}
			Token mark;
			if (this.addToInitialMarking) {
				mark = this.pn.getPlace(p.getId()).getInitialToken();
				// A Petri net should never have an omega in its initial marking.
				// However, let's better be safe than sorry.
				if (!mark.isOmega()) {
					mark = Token.valueOf(mark.getValue() + token);
				}
			} else {
				mark = Token.valueOf(token);
			}
			p.setInitialToken(mark);
		}

		return net;
	}

	/**
	 * Check if this marking marks the required places.
	 * @param marking The marking to check.
	 * @return true if everything is ok
	 */
	private boolean checkRequiredPlaces(Map<String, Integer> marking) {
		// If nothing is required, everything is ok
		if (this.requiredPlaces == null) {
			return true;
		}

		for (Set<Place> set : requiredPlaces) {
			boolean ok = false;
			for (Place p : set) {
				Integer token = marking.get(p.getId());
				if (token == null) {
					continue;
				}

				// We found a place which gets some tokens
				assert token > 0;
				ok = true;
				break;
			}
			if (!ok) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Increase the given marking without having more than m token.
	 * @param marking The last marking that was generated. This marking will be modified by this function.
	 * @return true if a new marking was successfully generated, else false.
	 */
	private boolean increaseMarking(Map<String, Integer> marking) {
		// First count how many token the given marking contains
		int token = 0;
		for (Map.Entry<String, Integer> entry : marking.entrySet()) {
			token += entry.getValue();
		}
		assert token <= this.tokenLimit;

		// Now increase the marking by one
		for (int i = 0; i < places.size(); i++) {
			Place place = places.get(i);
			Integer t = marking.get(place.getId());
			if (t == null) {
				t = 0;
			}

			if (token < tokenLimit) {
				// We can increase this place's token
				t++;
				marking.put(place.getId(), t);

				// If the required places are not OK, try again
				if (!checkRequiredPlaces(marking)) {
					return increaseMarking(marking);
				}

				// And now we are done
				return true;
			}

			// Have to reset this place's token and try again on the next one
			token -= t;
			marking.remove(place.getId());
		}

		// All markings for this net were generated, thus we are done
		return false;
	}

	@Override
	public Iterator<PetriNet> iterator() {
		return new Iterator<PetriNet>() {
			// The next marking that was generated. Initially, this is all 0...

			private Map<String, Integer> marking = new HashMap<>();

			{
				// ...however, only if "all 0" is allowed.
				if (requiredPlaces != null) {
					next();
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				return marking != null;
			}

			@Override
			public PetriNet next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				PetriNet result = getMarkedNet(marking);
				if (!increaseMarking(marking)) { // We went through all possible markings
					marking = null;
				}
				return result;
			}
		};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
