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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import uniol.apt.ui.impl.AptParametersTransformer;
import uniol.apt.ui.impl.AptReturnValuesTransformer;

/** @author Uli Schlachter */
public class JSONExecutorTest {
	private JSONExecutor executor;

	@BeforeMethod
	public void prepare() {
		executor = new JSONExecutor(new TestModuleRegistry(), AptParametersTransformer.INSTANCE,
				AptReturnValuesTransformer.INSTANCE);
	}

	private void runTest(String inputString, String outputString) {
		JSONObject inputObj = new JSONObject(inputString);
		Map<String, Object> expectedOutput = new JSONObject(outputString).toMap();
		Map<String, Object> realOutput = executor.execute(inputObj).toMap();
		assertThat(realOutput, equalTo(expectedOutput));
	}

	@Test
	public void testEmpty() {
		StringWriter result = new StringWriter();
		new JSONWriter(result)
			.object()
			.key("error").value("JSONObject[\"command\"] not found.")
			.key("type").value("org.json.JSONException")
			.endObject();
		runTest("{}", result.toString());
	}

	@Test
	public void listModules() {
		StringWriter command = new StringWriter();
		new JSONWriter(command)
			.object()
			.key("command").value("list_modules")
			.endObject();

		StringWriter result = new StringWriter();
		new JSONWriter(result)
			.object()
			.key("modules").array()
				.object()
				.key("name").value("example_module")
				.key("description").value("Lowercase a string")
				.key("description_long").value("Lowercase a string")
				.key("categories").array()
					.value("Miscellaneous")
					.endArray()
				.endObject()
			.endArray()
			.endObject();
		runTest(command.toString(), result.toString());
	}

	@Test
	public void describeModule() {
		StringWriter command = new StringWriter();
		new JSONWriter(command)
			.object()
			.key("command").value("describe_module")
			.key("module").value("example")
			.endObject();

		StringWriter result = new StringWriter();
		new JSONWriter(result)
			.object()
			.key("name").value("example_module")
			.key("description").value("Lowercase a string")
			.key("description_long").value("Lowercase a string")
			.key("categories").array()
				.value("Miscellaneous")
			.endArray()
			.key("parameters").array()
				.object()
					.key("name").value("string")
					.key("description").value("Some string")
					.key("optional").value(false)
					.key("type").value("java.lang.String")
					.key("properties").array()
						.value("foo")
					.endArray()
				.endObject()
				.object()
					.key("name").value("error")
					.key("description").value("If true, the module will fail")
					.key("optional").value(true)
					.key("default").value("false")
					.key("type").value("java.lang.Boolean")
					.key("properties").array()
						.value("bar")
					.endArray()
				.endObject()
			.endArray()
			.key("return_values").array()
				.object()
					.key("name").value("lower_case_string")
					.key("type").value("java.lang.String")
				.endObject()
			.endArray()
			.endObject();
		runTest(command.toString(), result.toString());
	}

	@Test
	public void callModuleSuccess() {
		StringWriter command = new StringWriter();
		new JSONWriter(command)
			.object()
			.key("command").value("run_module")
			.key("module").value("example")
			.key("arguments").object()
				.key("string").value("iNpUt")
				.endObject()
			.endObject();

		StringWriter result = new StringWriter();
		new JSONWriter(result)
			.object().key("return_values").object()
				.key("lower_case_string").value("input")
			.endObject().endObject();
		runTest(command.toString(), result.toString());
	}

	@Test
	public void callModuleSuccessWithError() {
		StringWriter command = new StringWriter();
		new JSONWriter(command)
			.object()
			.key("command").value("run_module")
			.key("module").value("example")
			.key("arguments").object()
				.key("string").value("iNpUt")
				.key("error").value("true")
				.endObject()
			.endObject();

		StringWriter result = new StringWriter();
		new JSONWriter(result)
			.object()
			.key("error").value("This module failed: iNpUt")
			.key("type").value("uniol.apt.module.exception.ModuleException")
			.endObject();
		runTest(command.toString(), result.toString());
	}

	@Test
	public void callModuleSuccessWithTransformError() {
		StringWriter command = new StringWriter();
		new JSONWriter(command)
			.object()
			.key("command").value("run_module")
			.key("module").value("example")
			.key("arguments").object()
				.key("string").value("iNpUt")
				.key("error").value("42")
				.endObject()
			.endObject();

		StringWriter result = new StringWriter();
		new JSONWriter(result)
			.object()
			.key("error").value("Not a valid boolean: 42")
			.key("type").value("uniol.apt.module.exception.ModuleException")
			.endObject();
		runTest(command.toString(), result.toString());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
