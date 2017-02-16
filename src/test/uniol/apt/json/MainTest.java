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

import java.io.StringReader;
import java.io.StringWriter;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.json.JSONObject;

/** @author Uli Schlachter */
public class MainTest {
	private void runTest(String input, String output, int numberOfExecutions) throws Exception {
		JSONExecutor executor = mock(JSONExecutor.class);
		when(executor.execute((JSONObject) anyObject())).thenReturn(new JSONObject());

		StringWriter result = new StringWriter();
		new Main(new StringReader(input), result, executor);

		assertThat(result.toString(), equalTo(output));
		verify(executor, times(numberOfExecutions)).execute((JSONObject) anyObject());
	}

	@Test
	public void testSimple() throws Exception {
		runTest("{}", "{}\n\n", 1);
	}

	@Test
	public void testInARow() throws Exception {
		runTest("{}{}{}{}", "{}\n\n{}\n\n{}\n\n{}\n\n", 4);
	}

	@Test
	public void testSyntaxError() throws Exception {
		runTest("banana", "{\n \"error\": \"A JSONObject text must begin with '{' at 1 [character 2 line 1]" +
				"\",\n \"type\": \"org.json.JSONException\"\n}\n\n", 0);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
