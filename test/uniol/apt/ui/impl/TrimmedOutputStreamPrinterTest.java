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

package uniol.apt.ui.impl;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.testng.annotations.Test;

/** @author Renke Grunwald */
public class TrimmedOutputStreamPrinterTest {

	@Test
	public void testEmpty() {
		StringOutputStream stringStream = new StringOutputStream();
		PrintStream printStream = new PrintStream(stringStream);
		TrimmedOutputStreamPrinter printer = new TrimmedOutputStreamPrinter(printStream);

		printer.show();

		assertEquals(stringStream.toString(), "");
	}

	@Test
	public void testTrimSpaces() {
		StringOutputStream stringStream = new StringOutputStream();
		PrintStream printStream = new PrintStream(stringStream);
		TrimmedOutputStreamPrinter printer = new TrimmedOutputStreamPrinter(printStream);

		printer.print("  test  ");
		printer.show();

		assertEquals(stringStream.toString(), "test" + System.lineSeparator());
	}

	@Test
	public void testTrimSpacesAndNewLineAfter() {
		StringOutputStream stringStream = new StringOutputStream();
		PrintStream printStream = new PrintStream(stringStream);
		TrimmedOutputStreamPrinter printer = new TrimmedOutputStreamPrinter(printStream);

		printer.print("test" + System.lineSeparator() + System.lineSeparator() + "  ");
		printer.show();

		assertEquals(stringStream.toString(), "test" + System.lineSeparator());
	}

	@Test
	public void testTrimSpacesAndNewLineAfter2() {
		StringOutputStream stringStream = new StringOutputStream();
		PrintStream printStream = new PrintStream(stringStream);
		TrimmedOutputStreamPrinter printer = new TrimmedOutputStreamPrinter(printStream);

		printer.print("test");
		printer.println(" ");
		printer.println(" ");
		printer.show();

		assertEquals(stringStream.toString(), "test" + System.lineSeparator());
	}

	@Test
	public void testTrimSpacesAndNewLineBeforeAndAfter() {
		StringOutputStream stringStream = new StringOutputStream();
		PrintStream printStream = new PrintStream(stringStream);
		TrimmedOutputStreamPrinter printer = new TrimmedOutputStreamPrinter(printStream);

		printer.println(" ");
		printer.println("just");
		printer.println(" a ");
		printer.print("test");
		printer.println(" ");
		printer.println(" ");
		printer.show();

		assertEquals(stringStream.toString(),
				"just" + System.lineSeparator()
				+ " a " + System.lineSeparator()
				+ "test" + System.lineSeparator());
	}
}

class StringOutputStream extends OutputStream {
	private final StringBuilder builder = new StringBuilder();

	@Override
	public void write(int b) throws IOException {
		this.builder.append((char) b);
	}

	@Override
	public String toString() {
		return builder.toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
