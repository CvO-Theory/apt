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

package uniol.apt.analysis.snet;

import java.util.HashSet;
import java.util.Set;

/**
 * Class to save snet informations
 *
 * @author Daniel
 *
 */
public class SNetResult {
	private Set<String> synchronizationLabels;
	private Set<String> splittingLabels;

	private boolean isSNet;

	/**
	 * Class constructor.
	 *
	 */
	public SNetResult() {
		this.isSNet = true;

		synchronizationLabels = new HashSet<String>();
		splittingLabels = new HashSet<String>();
	}

	/**
	 * Add a label to splitting set
	 *
	 * @param label label to add
	 */
	public void addSplittingLabel(String label) {
		splittingLabels.add(label);
	}

	/**
	 * Returns splitting labels
	 *
	 * @return splitting labels
	 */
	public Set<String> getSplittingLabels() {
		return splittingLabels;
	}

	/**
	 * Add a label to synchronization set
	 *
	 * @param label label to add
	 */
	public void addSynchronizationLabel(String label) {
		synchronizationLabels.add(label);
	}

	/**
	 * Returns synchronization labels
	 *
	 * @return synchronization labels
	 */
	public Set<String> getSynchronizationLabels() {
		return synchronizationLabels;
	}

	/**
	 * Is net an s net?
	 *
	 * @return isSNet
	 */
	public boolean isSNet() {
		return isSNet;
	}

	/**
	 * Set snet property
	 *
	 * @param setIsSNet set snet property
	 */
	public void setSNet(boolean setIsSNet) {
		this.isSNet = setIsSNet;
	}



}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
