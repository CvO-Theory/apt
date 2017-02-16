/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016 Jonas Prellberg
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

package uniol.apt.analysis.presynthesis.pps;

import java.util.HashMap;
import java.util.Map;

import uniol.apt.adt.ts.State;

/**
 * Result class for {@link PpsPropertyChecker} that holds information about the
 * offending state(s) that can be displayed to the user.
 *
 * @author Jonas Prellberg
 */
public class PpsPropertyResult {

	private final String violatedProperty;
	private final Map<String, State> offendingStates;
	private final Map<String, String> transitions;

	/**
	 * Initializes a new result for the given violated property. The string
	 * is not interpreted and only displayed to the user.
	 *
	 * @param violatedProperty
	 *                name of the violated property shown to the user
	 */
	public PpsPropertyResult(String violatedProperty) {
		this.violatedProperty = violatedProperty;
		this.offendingStates = new HashMap<>();
		this.transitions = new HashMap<>();
	}

	public String getViolatedProperty() {
		return violatedProperty;
	}

	public Map<String, State> getOffendingStates() {
		return offendingStates;
	}

	public Map<String, String> getTransitions() {
		return transitions;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("violatedProperty: ");
		sb.append(violatedProperty);
		sb.append(System.lineSeparator());
		sb.append("offendingStates:");
		sb.append(System.lineSeparator());
		for (Map.Entry<String, State> entry : offendingStates.entrySet()) {
			sb.append("  ");
			sb.append(entry.getKey());
			sb.append(" = ");
			sb.append(entry.getValue().getId());
			sb.append(System.lineSeparator());
		}
		sb.append("offendingTransitions:");
		sb.append(System.lineSeparator());
		for (Map.Entry<String, String> entry : transitions.entrySet()) {
			sb.append("  ");
			sb.append(entry.getKey());
			sb.append(" = ");
			sb.append(entry.getValue());
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
