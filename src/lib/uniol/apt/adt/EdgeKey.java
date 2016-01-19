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

package uniol.apt.adt;

/**
 * An EdgeKey is used to store and find edges by it's primary key: ids of the source node and target node.
 * @author Dennis-Michael Borde, Manuel Gieseking
 */
public class EdgeKey {

	final private String sourceId;
	final private String targetId;

	/**
	 * Constructor for creating a new EdgeKey.
	 * @param sourceId the id of the source node.
	 * @param targetId the id of the target node.
	 */
	public EdgeKey(String sourceId, String targetId) {
		assert sourceId != null && targetId != null;
		this.sourceId = sourceId;
		this.targetId = targetId;
	}

	@Override
	public int hashCode() {
		int hashFirst = sourceId.hashCode();
		int hashSecond = targetId.hashCode();
		return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof EdgeKey) {
			EdgeKey otherKey = (EdgeKey) other;
			return this.sourceId.equals(otherKey.sourceId)
				&& this.targetId.equals(otherKey.targetId);
		}
		return false;
	}

	@Override
	public String toString() {
		return sourceId + " --> " + targetId;
	}

	/**
	 * Gets the source node id.
	 * @return the sourceId.
	 */
	public String getSourceId() {
		return this.sourceId;
	}

	/**
	 * Gets the target node id.
	 * @return the targetId.
	 */
	public String getTargetId() {
		return this.targetId;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
