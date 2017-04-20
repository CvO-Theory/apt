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

package uniol.apt.tasks.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Base class for individual parser tests.
 * @author Uli Schlachter
 */
public abstract class AbstractParserTester {
	private final PrintWriter output;
	private final String name;

	/**
	 * Construct a new ParserTest.
	 * @param outputDir directory to write output to.
	 * @param name name of this parser
	 * @throws FileNotFoundException When the given file does not exist.
	 * @throws UnsupportedEncodingException If your Java installation is broken and does not support UTF-8.
	 */
	public AbstractParserTester(File outputDir, String name) throws FileNotFoundException, UnsupportedEncodingException {
		this.name = name;
		output = new PrintWriter(new File(outputDir.getPath(), name + ".xml"), "UTF-8");
		output.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		output.println("<testsuite>");
	}

	/**
	 * Close the output.
	 */
	public void close() {
		output.println("</testsuite>");
		output.close();
	}

	/**
	 * Print a successful parse to the file.
	 * @param file The file that could be parsed.
	 */
	public void printSuccess(String file) {
		output.print("  <testcase classname=\"" + name + "\" ");
		output.println("name=\"" + file + "\" />");
	}

	/**
	 * Print a failure to the file.
	 * @param file The file that could not be parsed.
	 * @param type The type of the failure
	 * @param message The long message which provides more details about the error
	 */
	private void printFailure(String file, String type, String message) {
		output.print("  <testcase classname=\"" + name + "\" ");
		output.println("name=\"" + file + "\">");
		output.println("    <failure type=\"" + type + "\">");
		output.print("      <![CDATA[" + message + "]]>");
		output.println("    </failure>");
		output.println("  </testcase>");
	}

	/**
	 * Print an unsuccessful parse to the file.
	 * @param file The file that could not be parsed.
	 * @param e The exception that occurred.
	 */
	public void printException(String file, Exception e) {
		StringBuilder sb = new StringBuilder();
		sb.append(e.getClass());
		sb.append(": ");
		sb.append(e.getMessage());
		sb.append("\n");
		for (StackTraceElement elem : e.getStackTrace()) {
			sb.append("    at ");
			sb.append(elem.getClassName());
			sb.append(".");
			sb.append(elem.getMethodName());
			sb.append("(");
			sb.append(elem.getFileName());
			sb.append(":");
			sb.append(elem.getLineNumber());
			sb.append(")");
			sb.append("\n");
		}
		printFailure(file, "ExceptionThrown", sb.toString());
	}

	/**
	 * Print a file that no parser could parse.
	 * @param file The file that could not be parsed.
	 */
	public void printUnparsable(String file) {
		printFailure(file, "Unparsable", "No parser managed to parse this file");
	}

	/**
	 * Print a file that was not supposed to be parsable.
	 * @param file The file that was parsed.
	 */
	public void printUnexpectedParsable(String file) {
		printFailure(file, "UnexpectedParsable", "This file should be unparsable, but was parsed");
	}

	/**
	 * Print a file that was parsable by more than one parser.
	 * @param file The file that was parsed.
	 */
	public void printMoreThanOneParser(String file) {
		printFailure(file, "MoreThanOneParser", "This file was parsed by more than one parser");
	}

	/**
	 * Try to parse the given file. This should return normally if the file can be parsed successfully. Otherweise,
	 * an exception should be thrown.
	 * @param file The file that should be parsed.
	 * @throws uniol.apt.io.parser.ParseException Thrown when the given file cannot be parsed, because it violates
	 * the supported file format.
	 * @throws Exception Any other kind of exception will be considered an error in the implemenetation.
	 */
	abstract public void parse(File file) throws Exception;
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
