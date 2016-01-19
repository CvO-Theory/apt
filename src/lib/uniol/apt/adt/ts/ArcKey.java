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

package uniol.apt.adt.ts;

import java.util.Objects;
import uniol.apt.adt.EdgeKey;

/**
 * This class serves for storing the key of an Arc.
 * @author Manuel Gieseking
 */
public class ArcKey extends EdgeKey {

	private final String label;

	/**
	 * Constructor.
	 * @param sourceId the source node id
	 * @param targetId the target node it
	 * @param label    the label of the arc
	 */
	public ArcKey(String sourceId, String targetId, String label) {
		super(sourceId, targetId);
		this.label = label;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ArcKey other = (ArcKey) obj;
		if (!Objects.equals(this.label, other.label)) {
			return false;
		}
		if (!Objects.equals(this.getSourceId(), other.getSourceId())) {
			return false;
		}
		if (!Objects.equals(this.getTargetId(), other.getTargetId())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 89 * hash + Objects.hashCode(this.label);
		hash = 89 * hash + Objects.hashCode(this.getSourceId());
		hash = 89 * hash + Objects.hashCode(this.getTargetId());
		return hash;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * Gets the label of this ArcKey.
	 * @return the label.
	 */
	public String getLabel() {
		return label;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
