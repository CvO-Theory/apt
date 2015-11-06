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

package uniol.apt.analysis.tnet;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to save tnet informations
 *
 * @author Daniel
 *
 */
public class TNetResult {
	private Set<String> mergeIDs;
	private Set<String> conflictIDs;

	private boolean isTNet;

	/**
	 * Class constructor.
	 *
	 */
	public TNetResult() {
		this.isTNet = true;

		mergeIDs = new HashSet<String>();
		conflictIDs = new HashSet<String>();
	}

	/**
	 * Add a id to merge set
	 *
	 * @param id id to add
	 */
	public void addMergeID(String id) {
		mergeIDs.add(id);
	}

	/**
	 * Returns merge ids
	 *
	 * @return merge ids
	 */
	public Set<String> getMergeIDs() {
		return mergeIDs;
	}

	/**
	 * Add a id to conflict ids
	 *
	 * @param id id to add
	 */
	public void addConflictID(String id) {
		conflictIDs.add(id);
	}

	/**
	 * Returns conflict ids
	 *
	 * @return conflict ids
	 */
	public Set<String> getConflictIDs() {
		return conflictIDs;
	}

	/**
	 * Is net a t net?
	 *
	 * @return isTNet
	 */
	public boolean isTNet() {
		return isTNet;
	}

	/**
	 * Set tnet property
	 *
	 * @param setIsTNet set tnet property
	 */
	public void setTNet(boolean setIsTNet) {
		this.isTNet = setIsTNet;
	}



}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
