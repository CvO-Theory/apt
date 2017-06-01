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

package uniol.apt.adt.pn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.exception.StructureException;

/**
 * The Marking class represents the marking of a petri net. It stores the places of the net and the count of tokens on
 * the places in a hashmap.
 * At each access the consistency of the places of the marking is checked with the petri net.
 * @author Manuel Gieseking
 */
public class Marking {

	private final PetriNet net;
	private List<Place> placesList;
	private List<Token> tokenList = new ArrayList<>();

	/**
	 * Constructor. Sets the tokencount of every place of the given petri net to zero.
	 * @param net - the net for that the marking is created.
	 */
	public Marking(PetriNet net) {
		this.net = net;
		this.placesList = net.getPlacesList();
		for (Place place : placesList) {
			tokenList.add(Token.ZERO);
		}
	}

	/**
	 * Copy-Constructor.
	 * @param m - the marking that serves as template for creating the copy.
	 */
	public Marking(Marking m) {
		this.net = m.net;
		this.placesList = m.placesList;
		this.tokenList = new ArrayList<>(m.tokenList);
	}

	/**
	 * Constructor.
	 * @param net The net for which the marking instance gets created.
	 * @param m   A marking from which the mapping from places to token is copied.
	 * @throws StructureException if the places of the given net and tokenmap do not fit.
	 */
	public Marking(PetriNet net, Marking m) {
		this.net = net;
		if (this.net == m.net) {
			this.placesList = m.placesList;
			this.tokenList = new ArrayList<>(m.tokenList);
		} else {
			this.placesList = this.net.getPlacesList();
			this.tokenList = new ArrayList<>(Collections.nCopies(this.placesList.size(), Token.ZERO));
			for (int idx = 0; idx < m.placesList.size(); idx++) {
				int ownIdx = placesList.indexOf(net.getPlace(m.placesList.get(idx).getId()));
				this.tokenList.set(ownIdx, m.tokenList.get(idx));
			}
		}
	}

	/**
	 * Constructor.
	 * @param net The net for which the marking instance gets created.
	 * @param m   A mapping from Place ids to tokens.
	 * @throws StructureException if the places of the given net and tokenmap do not fit.
	 */
	public Marking(PetriNet net, Map<String, Integer> m) {
		this.net = net;
		this.placesList = net.getPlacesList();
		this.tokenList = new ArrayList<>(Collections.nCopies(this.placesList.size(), Token.ZERO));
		for (Map.Entry<String, Integer> entry : m.entrySet()) {
			int idx = this.placesList.indexOf(this.net.getPlace(entry.getKey()));
			if (idx == -1) {
				throw new StructureException("place '" + entry.getKey() + "' does not belong to net '"
					+ this.net.getName() + "'.");
			}
			this.tokenList.set(idx, Token.valueOf(entry.getValue()));
		}
		ensureConsistency();
	}

	/**
	 * Set the Marking for the given net.
	 * @param net                - the net the marking belongs to.
	 * @param orderedTokenCounts - list of token count for the places in lexical order.
	 * @throws StructureException       thrown if the size of places in the given net and the length of the list of
	 *                                  tokens does not fit.
	 * @throws IllegalArgumentException thrown if a token count is less than zero.
	 */
	public Marking(PetriNet net, int... orderedTokenCounts) {
		this.net = net;
		this.placesList = this.net.getPlacesList();
		if (orderedTokenCounts.length != this.placesList.size()) {
			throw new StructureException("Count of tokencounts does not match the count of"
				+ "places in graph '" + this.net.getName() + "'.");
		}
		for (int i = 0; i < orderedTokenCounts.length; i++)
			this.tokenList.add(Token.valueOf(orderedTokenCounts[i]));
	}

	/**
	 * Returns the corresponding net of this marking.
	 * @return A PetriNet reference to the net.
	 */
	public PetriNet getNet() {
		return this.net;
	}

	/**
	 * Fires the given transitions and creates a new marking.
	 * @param t - the transitions that should be fired.
	 * @return the marking reached after firing the transitions
	 */
	public Marking fireTransitions(Transition... t) {
		ensureConsistency();
		Marking result = this;
		for (int i = 0; i < t.length; i++) {
			result = this.net.fireTransition(t[i].getId(), result);
		}
		return result;
	}

	/**
	 * Fires the given transitions by changing this marking.
	 * @param t - the transitions that should be fired.
	 * @return this marking, changed by firing the transitions.
	 * @deprecated Markings should be used immutably. Use {@link fireTransitions} instead.
	 */
	@Deprecated
	public Marking fire(Transition... t) {
		ensureConsistency();
		Marking result = fireTransitions(t);
		this.placesList = result.placesList;
		this.tokenList = result.tokenList;
		return this;
	}

	/**
	 * Sets the given token(s) on a place with the given id.
	 * @param placeId the place id.
	 * @param m       the token.
	 * @return A copy of this marking with the given modification.
	 * @throws StructureException if the places of this net and the given token do not fit.
	 */
	public Marking setTokenCount(String placeId, Token m) {
		return setTokenCount(net.getPlace(placeId), m);
	}

	/**
	 * Sets the given token(s) on a place with the given id.
	 * @param placeId the place id.
	 * @param m       the token.
	 * @throws StructureException if the places of this net and the given token do not fit.
	 * @deprecated Markings should be used immutably. Use {@link setTokenCount} instead.
	 */
	@Deprecated
	public void setToken(String placeId, Token m) {
		setToken(net.getPlace(placeId), m);
	}

	/**
	 * Sets the given token(s) on a place with the given id.
	 * @param placeId the place id.
	 * @param m       the token.
	 * @return A copy of this marking with the given modification.
	 * @throws StructureException       thrown if the places of this net and the given token do not fit.
	 * @throws IllegalArgumentException thrown if a token count is less than zero.
	 */
	public Marking setTokenCount(String placeId, int m) {
		return setTokenCount(net.getPlace(placeId), m);
	}

	/**
	 * Sets the given token(s) on a place with the given id.
	 * @param placeId the place id.
	 * @param m       the token.
	 * @throws StructureException       thrown if the places of this net and the given token do not fit.
	 * @throws IllegalArgumentException thrown if a token count is less than zero.
	 * @deprecated Markings should be used immutably. Use {@link setTokenCount} instead.
	 */
	@Deprecated
	public void setToken(String placeId, int m) {
		setToken(net.getPlace(placeId), m);
	}

	/**
	 * Sets the given token(s) on a given place.
	 * @param p the place.
	 * @param m the token.
	 * @return A copy of this marking with the given modification.
	 * @throws StructureException if the the given place don't belong to the net of this marking.
	 */
	public Marking setTokenCount(Place p, Token m) {
		assert p != null && m != null;
		ensureConsistency();
		if (net != p.getGraph() || !this.placesList.contains(p)) {
			throw new StructureException("place '" + p.getId() + "' does not belong to net '"
				+ this.net.getName() + "'.");
		}
		Marking result = new Marking(this);
		int idx = result.placesList.indexOf(p);
		result.tokenList.set(idx, m);
		return result;
	}

	/**
	 * Sets the given token(s) on a given place.
	 * @param p the place.
	 * @param m the token.
	 * @throws StructureException if the the given place don't belong to the net of this marking.
	 * @deprecated Markings should be used immutably. Use {@link setTokenCount} instead.
	 */
	@Deprecated
	public void setToken(Place p, Token m) {
		assert p != null && m != null;
		ensureConsistency();
		if (net != p.getGraph() || !this.placesList.contains(p)) {
			throw new StructureException("place '" + p.getId() + "' does not belong to net '"
				+ this.net.getName() + "'.");
		}
		int idx = this.placesList.indexOf(p);
		this.tokenList.set(idx, m);
	}

	/**
	 * Sets the given token(s) on a given place.
	 * @param p the place.
	 * @param m the number of tokens on this place.
	 * @return A copy of this marking with the given modification.
	 * @throws StructureException       if the the given place don't belong to the net of this marking.
	 * @throws IllegalArgumentException thrown if a token count is less than zero.
	 */
	public Marking setTokenCount(Place p, int m) {
		return setTokenCount(p, Token.valueOf(m));
	}

	/**
	 * Sets the given token(s) on a given place.
	 * @param p the place.
	 * @param m the number of tokens on this place.
	 * @throws StructureException       if the the given place don't belong to the net of this marking.
	 * @throws IllegalArgumentException thrown if a token count is less than zero.
	 * @deprecated Markings should be used immutably. Use {@link setTokenCount} instead.
	 */
	@Deprecated
	public void setToken(Place p, int m) {
		this.setToken(p, Token.valueOf(m));
	}

	/**
	 * Adds the given token(s) to a given place.
	 * @param id the place id.
	 * @param m  the number of tokens that get added.
	 * @return A copy of this marking with the given modification.
	 * @throws NoSuchNodeException thrown if the place with the given id don't exists in the net.
	 */
	public Marking addTokenCount(String id, Token m) {
		assert id != null && m != null;
		return addTokenCount(this.net.getPlace(id), m);
	}

	/**
	 * Adds the given token(s) to a given place.
	 * @param id the place id.
	 * @param m  the number of tokens that get added.
	 * @throws NoSuchNodeException thrown if the place with the given id don't exists in the net.
	 * @deprecated Markings should be used immutably. Use {@link addTokenCount} instead.
	 */
	@Deprecated
	public void addToken(String id, Token m) {
		assert id != null && m != null;
		addToken(this.net.getPlace(id), m);
	}

	/**
	 * Adds the given token(s) to a given place.
	 * @param p the place.
	 * @param m the token.
	 * @return A copy of this marking with the given modification.
	 * @throws NoSuchNodeException if the place does not exist in the given net.
	 */
	public Marking addTokenCount(Place p, Token m) {
		assert p != null && m != null;
		Token val = getToken(p);
		Marking result = new Marking(this);
		int idx = result.placesList.indexOf(p);
		result.tokenList.set(idx, val.add(m));
		return result;
	}

	/**
	 * Adds the given token(s) to a given place.
	 * @param p the place.
	 * @param m the token.
	 * @throws NoSuchNodeException if the place does not exist in the given net.
	 * @deprecated Markings should be used immutably. Use {@link addTokenCount} instead.
	 */
	@Deprecated
	public void addToken(Place p, Token m) {
		assert p != null && m != null;
		ensureConsistency();
		Token val = getToken(p);
		int idx = this.placesList.indexOf(p);
		this.tokenList.set(idx, val.add(m));
	}

	/**
	 * Adds the given token(s) to a place with the given id.
	 * @param id the place id.
	 * @param m  the number of tokens that get added.
	 * @return A copy of this marking with the given modification.
	 * @throws IllegalArgumentException if the result of this addition would be less than zero.
	 * @throws NoSuchNodeException      if the place does not exist in the given net.
	 */
	public Marking addTokenCount(String id, int m) {
		assert id != null;
		return addTokenCount(this.net.getPlace(id), m);
	}

	/**
	 * Adds the given token(s) to a place with the given id.
	 * @param id the place id.
	 * @param m  the number of tokens that get added.
	 * @throws IllegalArgumentException if the result of this addition would be less than zero.
	 * @throws NoSuchNodeException      if the place does not exist in the given net.
	 * @deprecated Markings should be used immutably. Use {@link addTokenCount} instead.
	 */
	@Deprecated
	public void addToken(String id, int m) {
		assert id != null;
		addToken(this.net.getPlace(id), m);
	}

	/**
	 * Adds the given token(s) to a given place.
	 * @param p the place.
	 * @param m the number of tokens that get added.
	 * @return A copy of this marking with the given modification.
	 * @throws IllegalArgumentException if the result of this addition would be less than zero.
	 * @throws NoSuchNodeException      if the place does not exist in the given net.
	 */
	public Marking addTokenCount(Place p, int m) {
		assert p != null;
		ensureConsistency();
		Token val = getToken(p);
		Marking result = new Marking(this);
		int idx = result.placesList.indexOf(p);
		result.tokenList.set(idx, val.add(m));
		return result;
	}

	/**
	 * Adds the given token(s) to a given place.
	 * @param p the place.
	 * @param m the number of tokens that get added.
	 * @throws IllegalArgumentException if the result of this addition would be less than zero.
	 * @throws NoSuchNodeException      if the place does not exist in the given net.
	 * @deprecated Markings should be used immutably. Use {@link addTokenCount} instead.
	 */
	@Deprecated
	public void addToken(Place p, int m) {
		assert p != null;
		ensureConsistency();
		Token val = getToken(p);
		int idx = this.placesList.indexOf(p);
		this.tokenList.set(idx, val.add(m));
	}

	/**
	 * Returns the token count of the given place with the given id.
	 * @param id a string containing an id of a place in the corresponding net.
	 * @return a value representing the token count.
	 * @throws NoSuchNodeException thrown if the place with the given id does not belong to the net.
	 */
	public Token getToken(String id) {
		assert id != null;
		return getToken(this.net.getPlace(id));
	}

	/**
	 * Returns the token count of the given place.
	 * @param p a place of the corresponding net.
	 * @return a value representing the token count.
	 * @throws StructureException  thrown if the place belong to an other net.
	 * @throws NoSuchNodeException thrown if the place with the given id does not exists in the net.
	 */
	public Token getToken(Place p) {
		assert p != null;
		if (net != p.getGraph()) {
			throw new StructureException("place '" + p.getId() + "' does not belong to net '"
				+ this.net.getName() + "'.");
		}
		ensureConsistency();
		int idx = this.placesList.indexOf(p);
		if (idx == -1) {
			throw new NoSuchNodeException(net, p.getId());
		}
		return this.tokenList.get(idx);
	}

	/**
	 * Return the tokens that appear in this marking.
	 * @return A collection containing at least once each {@link Token} that appears as a value in this marking.
	 */
	public Collection<Token> values() {
		ensureConsistency();
		return Collections.unmodifiableList(tokenList);
	}

	/**
	 * Used for ensuring the consistency of the marking. The function checks the place revision variable of the net
	 * and incase the marking has an earlier revision, the hashmap of the marking gets updated.
	 */
	final void ensureConsistency() {
		if (placesList != net.getPlacesList()) {
			List<Place> oldPlacesList = placesList;
			List<Token> oldTokenList = tokenList;
			this.placesList = this.net.getPlacesList();
			this.tokenList = new ArrayList<>(this.placesList.size());
			for (Place place : this.placesList) {
				int idx = oldPlacesList.indexOf(place);
				if (idx == -1)
					this.tokenList.add(Token.ZERO);
				else
					this.tokenList.add(oldTokenList.get(idx));
			}
		}
	}

	/**
	 * Check if this object covers the given other marking. If the other marking is covered, a new marking is
	 * returned with has suitable omegas added. Else, null is returned.
	 * @param o The marking that should be covered.
	 * @return A marking with added omegas, or null if this does not cover the other marking.
	 * @author Uli Schlachter, Manuel Gieseking
	 */
	public Marking cover(Marking o) {
		ensureConsistency();
		o.ensureConsistency();
		assert this.placesList == o.placesList;

		Set<Integer> covered = new HashSet<>();
		for (int idx = 0; idx < placesList.size(); idx++) {
			Token own = this.tokenList.get(idx);
			Token other = o.tokenList.get(idx);

			int comp = own.compareTo(other);
			if (comp < 0) {
				return null;
			} else if (comp > 0 && !own.isOmega()) {
				covered.add(idx);
			}
		}
		if (covered.isEmpty()) {
			// Both markings are equal and thus we don't cover anything
			return null;
		}

		// We are covering the other marking, add the suitable omegas
		Marking result = new Marking(this);
		for (int idx : covered) {
			result.tokenList.set(idx, Token.OMEGA);
		}

		return result;
	}

	/**
	 * Check if this object covers the given other marking. If the other marking is covered, suitable omegas are
	 * added to this object.
	 * @param o The marking that should be covered.
	 * @return true if the given marking gets covered
	 * @author Uli Schlachter, Manuel Gieseking
	 * @deprecated Markings should be used immutably. Use {@link cover} instead.
	 */
	@Deprecated
	public boolean covers(Marking o) {
		ensureConsistency();
		o.ensureConsistency();
		assert this.placesList == o.placesList;

		Set<Integer> covered = new HashSet<>();
		for (int idx = 0; idx < placesList.size(); idx++) {
			Token own = this.tokenList.get(idx);
			Token other = o.tokenList.get(idx);

			int comp = own.compareTo(other);
			if (comp < 0) {
				return false;
			} else if (comp > 0 && !own.isOmega()) {
				covered.add(idx);
			}
		}
		if (covered.isEmpty()) {
			// Both markings are equal and thus we don't cover anything
			return false;
		}

		// We are covering the other marking, add the suitable omegas
		for (int index : covered) {
			this.tokenList.set(index, Token.OMEGA);
		}

		return true;
	}

	/**
	 * Check if the marking contains at least one omega.
	 * @return true if the mapping contains at least one omega token.
	 */
	public boolean hasOmega() {
		ensureConsistency();
		for (Token val : tokenList) {
			if (val.isOmega()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		ensureConsistency();

		// Previously this used return this.map.hashCode() which did sum(key.hashCode() ^ value.hashCode).
		// Sadly, we had lots of hash collisions due to this, because all the keys were the same (the IDs of
		// places) and the values are the number of token on a place. Since these are small integers, only the
		// low-order bits of their hash code differed, making hash collisions in the addition easy.

		int hashCode = 0;
		// Loop over all entries in our map and calculate a hash code. For two different Markings that "are
		// logically the same" (according to equals()), this must produce the same hash code. Since the
		// iteration of a map is unstable, this must use an associative operation like plus inside the loop.
		for (int idx = 0; idx < placesList.size(); idx++) {
			// Mix the hash codes more so that hopefully all bits of the resulting hash code are influenced.
			int keyCode = placesList.get(idx).hashCode();
			int valCode = tokenList.get(idx).hashCode();
			hashCode += Integer.rotateLeft(valCode, keyCode);
			hashCode += Integer.rotateLeft(keyCode, valCode);
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		ensureConsistency();
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Marking other = (Marking) obj;
		other.ensureConsistency();
		if (this.net != other.net) {
			return false;
		}
		assert this.placesList == other.placesList;
		return Objects.equals(this.tokenList, other.tokenList);
	}

	@Override
	public String toString() {
		ensureConsistency();
		StringBuilder strBuilder = new StringBuilder("[ ");
		for (Place place : net.getPlaces()) {
			strBuilder.append("[")
				.append(place.getId())
				.append(":")
				.append(getToken(place).toString())
				.append("] ");
		}
		strBuilder.append("]");
		return strBuilder.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
