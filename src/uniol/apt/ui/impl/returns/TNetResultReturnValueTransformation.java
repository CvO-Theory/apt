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

package uniol.apt.ui.impl.returns;

import uniol.apt.analysis.tnet.TNetResult;
import uniol.apt.ui.ReturnValueTransformation;

/**
 * Transform t net result into a string.
 *
 * @author Daniel
 */
public class TNetResultReturnValueTransformation implements ReturnValueTransformation<TNetResult> {
	@Override
	public String transform(TNetResult result) {
		StringBuilder sb = new StringBuilder("\n");

		if (result.getMergeIDs().isEmpty() && result.getConflictIDs().isEmpty()) {
			sb.append("  No merge and no conflict was found.\n");
		}

		for (String id : result.getMergeIDs()) {
			sb.append("  There is a merge before " + id + ".\n");
		}

		for (String id : result.getConflictIDs()) {
			sb.append("  There is a conflict after " + id + ".\n");
		}

		return sb.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
