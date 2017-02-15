/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017  Uli Schlachter
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

package uniol.apt.json;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Helper function for interacting with JSON objects.
 * @author Uli Schlachter
 */
public class JSONUtilities {
	static final private boolean INCLUDE_STACK_TRACE = Boolean.getBoolean("aptjson.includeStackTrace");

	private JSONUtilities() {
	}

	/**
	 * Describe a throwable as a JSONObject. This will include the error message, the type of the exception and
	 * suppressed and causing throwables. If the <pre>aptjson.includeStackTrace</pre> system property is set to
	 * <pre>true</pre>, the stack traces will also be included.
	 * @param t The throwable to transform.
	 * @return A JSON object describing the throwable.
	 */
	static public JSONObject toJSONObject(Throwable t) {
		JSONObject result = new JSONObject();
		result.put("error", t.getMessage());
		result.put("type", t.getClass().getName());
		if (INCLUDE_STACK_TRACE)
			result.put("stacktrace", t.getStackTrace());

		Throwable[] suppressed = t.getSuppressed();
		if (suppressed.length > 0)
			result.put("suppressed", toJSONArray(suppressed));

		Throwable cause = t.getCause();
		if (cause != null)
			result.put("caused_by", toJSONObject(cause));
		return result;
	}

	static private JSONArray toJSONArray(Throwable[] throwables) {
		JSONArray result = new JSONArray();
		for (Throwable t : throwables)
			result.put(toJSONObject(t));
		return result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
