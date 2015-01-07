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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import uniol.apt.adt.exception.NoSuchNodeException;
import uniol.apt.adt.exception.StructureException;

/**
 * The Marking class represents the marking of a petri net. It stores the places of the net and the count of tokens on
 * the places in a hashmap.
 * <p/>
 * At each access the consistency of the places of the marking is checked with the petri net.
 * <p/>
 * @author Manuel Gieseking
 */
public class Marking {

	private final HashMap<String, Token> map = new HashMap<>();
	private final PetriNet net;
	private long rev = -1;

	/**
	 * Constructor. Sets the tokencount of every place of the given petri net to zero.
	 * <p/>
	 * @param net - the net for that the marking is created.
	 */
	public Marking(PetriNet net) {
		this.net = net;
		for (Place place : net.getPlaces()) {
			map.put(place.getId(), new Token(0));
		}
		this.rev = net.getPlaceRev();
	}

	/**
	 * Copy-Constructor.
	 * <p/>
	 * @param m - the marking that serves as template for creating the copy.
	 */
	public Marking(Marking m) {
		this.net = m.net;
		this.setMarking(m);
	}
        
        
	/**
	 * Constructor.
	 * <p/>
	 * @param net The net for which the marking instance gets created.
	 * @param m   A marking from which the mapping from places to token is copied.
	 * <p/>
	 * @throws StructureException if the places of the given net and tokenmap do not fit.
	 */
	public Marking(PetriNet net, Marking m) {
		this.net = net;
		this.setMarking(m);
	}

	/**
	 * Constructor.
	 * <p/>
	 * @param net The net for which the marking instance gets created.
	 * @param m   A mapping from Place ids to tokens.
	 * <p/>
	 * @throws StructureException if the places of the given net and tokenmap do not fit.
	 */
	public Marking(PetriNet net, Map<String, Token> m) {
		this.net = net;
		this.setMarking(m);
	}

	/**
	 * Set the Marking for the given net.
	 * <p/>
	 * @param net                - the net the marking belongs to.
	 * @param orderedTokenCounts - list of token count for the places in lexical order.
	 * <p/>
	 * @throws StructureException       thrown if the size of places in the given net and the length of the list of
	 *                                  tokens does not fit.
	 * @throws IllegalArgumentException thrown if a token count is less than zero.
	 */
	public Marking(PetriNet net, int... orderedTokenCounts) {
		this.net = net;
		Set<Place> places = this.net.getPlaces();
		if (orderedTokenCounts.length != places.size()) {
			throw new StructureException("Count of tokencounts does not match the count of"
				+ "places in graph '" + this.net.getName() + "'.");
		}
		int count = -1;
		for (Iterator<Place> it = places.iterator(); it.hasNext();) {
			Place place = it.next();
			this.map.put(place.getId(), new Token(orderedTokenCounts[++count]));
		}
	}

	/**
	 * Copies the marking in the instance.
	 * <p/>
	 * @param m The marking that serves as template for copying the marking.
	 */
	private void setMarking(Marking m) {
		m.ensureConsistency();
		for (Map.Entry<String, Token> entry : m.map.entrySet()) {
			this.map.put(entry.getKey(), new Token(entry.getValue()));
		}
		ensureConsistency();
	}

	/**
	 * Sets the marking of this instance according to the given mapping.
	 * <p/>
	 * @param m a mapping of how many tokens are on the places.
	 * <p/>
	 * @throws StructureException if the places of the given net and tokenmap do not fit.
	 */
	private void setMarking(Map<String, Token> m) {
		for (String id : m.keySet()) {
			if (!this.net.containsPlace(id)) {
				throw new StructureException("place '" + id + "' does not belong to net '"
					+ this.net.getName() + "'.");
			}
		}
		for (Map.Entry<String, Token> entry : m.entrySet()) {
			this.map.put(entry.getKey(), new Token(entry.getValue()));
		}
		ensureConsistency();
	}

	/**
	 * Returns the corresponding net of this marking.
	 * <p/>
	 * @return A PetriNet reference to the net.
	 */
	public PetriNet getNet() {
		return this.net;
	}

	/**
	 * Fires the given transitions by changing this marking.
	 * <p/>
	 * @param t - the transitions that should be fired.
	 * <p/>
	 * @return this marking, changed by firing the transitions.
	 */
	public Marking fire(Transition... t) {
		ensureConsistency();
		for (int i = 0; i < t.length; i++) {
			this.net.fireTransition(t[i].getId(), this);
		}
		return this;
	}

	/**
	 * Sets the given token(s) on a place with the given id.
	 * <p/>
	 * @param placeId the place id.
	 * @param m       the token.
	 * <p/>
	 * @throws StructureException if the places of this net and the given token do not fit.
	 */
	public void setToken(String placeId, Token m) {
		setToken(net.getPlace(placeId), m);
	}

	/**
	 * Sets the given token(s) on a place with the given id.
	 * <p/>
	 * @param placeId the place id.
	 * @param m       the token.
	 * <p/>
	 * @throws StructureException       thrown if the places of this net and the given token do not fit.
	 * @throws IllegalArgumentException thrown if a token count is less than zero.
	 */
	public void setToken(String placeId, int m) {
		setToken(net.getPlace(placeId), m);
	}

	/**
	 * Sets the given token(s) on a given place.
	 * <p/>
	 * @param p the place.
	 * @param m the token.
	 * <p/>
	 * @throws StructureException if the the given place don't belong to the net of this marking.
	 */
	public void setToken(Place p, Token m) {
		assert p != null && m != null;
		ensureConsistency();
		if (net != p.getGraph() || !map.containsKey(p.getId())) {
			throw new StructureException("place '" + p.getId() + "' does not belong to net '"
				+ this.net.getName() + "'.");
		}
		map.put(p.getId(), m);
	}

	/**
	 * Sets the given token(s) on a given place.
	 * <p/>
	 * @param p the place.
	 * @param m the number of tokens on this place.
	 * <p/>
	 * @throws StructureException       if the the given place don't belong to the net of this marking.
	 * @throws IllegalArgumentException thrown if a token count is less than zero.
	 */
	public void setToken(Place p, int m) {
		this.setToken(p, new Token(m));
	}

	/**
	 * Adds the given token(s) to a given place.
	 * <p/>
	 * @param id the place id.
	 * @param m  the number of tokens that get added.
	 * <p/>
	 * @throws NoSuchNodeException thrown if the place with the given id don't exists in the net.
	 */
	public void addToken(String id, Token m) {
		assert id != null && m != null;
		ensureConsistency();
		Token val = getToken(id);
		if (val != null) {
			val.add(m);
		} else {
			throw new NoSuchNodeException(net, id);
		}
	}

	/**
	 * Adds the given token(s) to a given place.
	 * <p/>
	 * @param p the place.
	 * @param m the token.
	 * <p/>
	 * @throws NoSuchNodeException if the place does not exist in the given net.
	 */
	public void addToken(Place p, Token m) {
		assert p != null && m != null;
		addToken(p.getId(), m);
	}

	/**
	 * Adds the given token(s) to a place with the given id.
	 * <p/>
	 * @param id the place id.
	 * @param m  the number of tokens that get added.
	 * <p/>
	 * @throws IllegalArgumentException if the result of this addition would be less than zero.
	 * @throws NoSuchNodeException      if the place does not exist in the given net.
	 */
	public void addToken(String id, int m) {
		assert id != null;
		ensureConsistency();
		Token val = getToken(id);
		if (val != null) {
			val.add(m);
		} else {
			throw new NoSuchNodeException(net, id);
		}
	}

	/**
	 * Adds the given token(s) to a given place.
	 * <p/>
	 * @param p the place.
	 * @param m the number of tokens that get added.
	 * <p/>
	 * @throws IllegalArgumentException if the result of this addition would be less than zero.
	 * @throws NoSuchNodeException      if the place does not exist in the given net.
	 */
	public void addToken(Place p, int m) {
		assert p != null;
		addToken(p.getId(), m);
	}

	/**
	 * Returns the token count of the given place with the given id.
	 * <p/>
	 * @param id a string containing an id of a place in the corresponding net.
	 * <p/>
	 * @return a value representing the token count.
	 * <p/>
	 * @throws NoSuchNodeException thrown if the place with the given id does not belong to the net.
	 */
	public Token getToken(String id) {
		assert id != null;
		ensureConsistency();
		Token val = map.get(id);
		if (val == null) {
			throw new NoSuchNodeException(net, id);
		}
		return val;
	}

	/**
	 * Returns the token count of the given place.
	 * <p/>
	 * @param p a place of the corresponding net.
	 * <p/>
	 * @return a value representing the token count.
	 * <p/>
	 * @throws StructureException  thrown if the place belong to an other net.
	 * @throws NoSuchNodeException thrown if the place with the given id does not exists in the net.
	 */
	public Token getToken(Place p) {
		assert p != null;
		if (net != p.getGraph()) {
			throw new StructureException("place '" + p.getId() + "' does not belong to net '"
				+ this.net.getName() + "'.");
		}
		return getToken(p.getId());
	}

	/**
	 * Used for ensuring the consistency of the marking. The function checks the place revision variable of the net
	 * and incase the marking has an earlier revision, the hashmap of the marking gets updated.
	 */
	final void ensureConsistency() {
		if (rev != net.getPlaceRev()) {
			Collection<String> toRemove = null;
			for (String key : this.map.keySet()) {
				if (!this.net.containsPlace(key)) {
					if (toRemove == null) {
						toRemove = new LinkedList<>();
					}
					toRemove.add(key);
				}
			}
			if (toRemove != null) {
				for (String key : toRemove) {
					this.map.remove(key);
				}
			}
			Collection<Place> places = this.net.getPlaces();
			for (Place p : places) {
				if (!this.map.containsKey(p.getId())) {
					map.put(p.getId(), new Token(0));
				}
			}
			rev = net.getPlaceRev();
		}
	}

	/**
	 * Check if this object covers the given other marking. If the other marking is covered, suitable omegas are
	 * added to this object.
	 * <p/>
	 * @param o The marking that should be covered.
	 * <p/>
	 * @return true if the given marking gets covered
	 * <p/>
	 * @author Uli Schlachter, Manuel Gieseking
	 */
	public boolean covers(Marking o) {
		ensureConsistency();
		o.ensureConsistency();
		assert map.keySet().equals(o.map.keySet());

		Set<Map.Entry<String, Token>> covered = new HashSet<>();
		for (Map.Entry<String, Token> e : map.entrySet()) {
			Token own = e.getValue();
			Token other = o.map.get(e.getKey());

			int comp = own.compareTo(other);
			if (comp < 0) {
				return false;
			} else if (comp > 0 && !own.isOmega()) {
				covered.add(e);
			}
		}
		if (covered.isEmpty()) {
			// Both markings are equal and thus we don't cover anything
			return false;
		}

		// We are covering the other marking, add the suitable omegas
		for (Map.Entry<String, Token> e : covered) {
			e.getValue().setOmega();
		}

		return true;
	}

	/**
	 * Check if the marking contains at least one omega.
	 * <p/>
	 * @return true if the mapping contains at least one omega token.
	 */
	public boolean hasOmega() {
		ensureConsistency();
		for (Token val : map.values()) {
			if (val.isOmega()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		ensureConsistency();
		return this.map.hashCode();
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
		if (!Objects.equals(this.map, other.map)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		ensureConsistency();
		StringBuilder strBuilder = new StringBuilder("[ ");
		for (String pid : map.keySet()) {
			strBuilder.append("[").append(pid).append(":").append(map.get(pid).toString()).append("] ");
		}
		strBuilder.append("]");
		return strBuilder.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
