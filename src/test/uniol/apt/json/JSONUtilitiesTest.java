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

import java.io.StringWriter;
import java.util.Map;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.json.JSONObject;
import org.json.JSONWriter;

/** @author Uli Schlachter */
public class JSONUtilitiesTest {
	private void runTest(Throwable t, String outputString) {
		Map<String, Object> expectedOutput = new JSONObject(outputString).toMap();
		Map<String, Object> realOutput = JSONUtilities.toJSONObject(t).toMap();
		assertThat(realOutput, equalTo(expectedOutput));
	}

	@Test
	public void testSimple() {
		Throwable e = new AssertionError("this is a test");

		StringWriter result = new StringWriter();
		new JSONWriter(result)
			.object()
			.key("error").value("this is a test")
			.key("type").value("java.lang.AssertionError")
			.endObject();
		runTest(e, result.toString());
	}


	@Test
	public void testComplicated() {
		Throwable e = new AssertionError("inner");
		e = new Throwable("outer", e);
		e.addSuppressed(new Error("suppressed"));

		StringWriter result = new StringWriter();
		new JSONWriter(result)
			.object()
			.key("error").value("outer")
			.key("type").value("java.lang.Throwable")
			.key("caused_by").object()
				.key("error").value("inner")
				.key("type").value("java.lang.AssertionError")
				.endObject()
			.key("suppressed").array().object()
				.key("error").value("suppressed")
				.key("type").value("java.lang.Error")
				.endObject().endArray()
			.endObject();
		runTest(e, result.toString());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
