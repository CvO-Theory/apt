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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import uniol.apt.module.AptModuleRegistry;
import uniol.apt.ui.impl.AptParametersTransformer;
import uniol.apt.ui.impl.AptReturnValuesTransformer;

/**
 * @author Uli Schlachter
 */
public class Main {
	/**
	 * Constructor.
	 * @param input Reader to read JSON input from.
	 * @param output Output to write JSON results to.
	 * @param executor Executor used to execute the input received.
	 * @throws IOException When an I/O error occurs.
	 */
	Main(Reader input, Writer output, JSONExecutor executor) throws IOException {
		JSONTokener tokener = new JSONTokener(input);

		// Read commands from the input and execute them until EOF is reached or malformed JSON is read.
		// The dance with nextClean() and back() is needed so that EOF is detected correctly.
		boolean exit = false;
		tokener.nextClean();
		while (!tokener.end() && !exit) {
			tokener.back();

			JSONObject result;
			try {
				JSONObject obj = new JSONObject(tokener);
				result = executor.execute(obj);
			} catch (JSONException e) {
				// Exception in JSON parsing, abort reading
				exit = true;
				result = JSONUtilities.toJSONObject(e);
			}
			result.write(output, 1, 0);
			output.write("\n\n");
			output.flush();

			tokener.nextClean();
		}
	}

	/**
	 * Program entry point.
	 * @param args command line arguments
	 * @throws IOException when reading from standard input or writing to standard output fails
	 */
	public static void main(String[] args) throws IOException {
		JSONExecutor executor = new JSONExecutor(AptModuleRegistry.INSTANCE, AptParametersTransformer.INSTANCE,
				AptReturnValuesTransformer.INSTANCE);
		try (Writer writer = new OutputStreamWriter(System.out, "UTF-8")) {
			new Main(new InputStreamReader(System.in, "UTF-8"), writer, executor);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
