/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

package uniol.apt.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * Ant task to prepare a TestNG XML file.
 * @author Uli Schlachter
 */
public class WriteTestsXML {
	private WriteTestsXML() {
	}

	/**
	 * Program entry point. Arguments are output directory, directory to scan and a file name pattern.
	 * @param args Program arguments.
	 */
	public static void main(String[] args) {
		if (args.length != 3)
			throw new IllegalArgumentException(
					"Need exactly three arguments: output file, directory to scan, file name pattern");

		File outputFile = new File(args[0]);
		File directoryToScan = new File(args[1]);
		String namePattern = args[2];

		Worker worker = new Worker();
		Iterator<File> fileIter = FileUtils.iterateFiles(directoryToScan,
					new WildcardFileFilter(namePattern),
					TrueFileFilter.INSTANCE);
		while (fileIter.hasNext()) {
			File file = fileIter.next();
			// The worker wants a relative path to the base directory
			String relativeFile = directoryToScan.toURI().relativize(file.toURI()).getPath();
			try {
				worker.handleFile(relativeFile);
			} catch (UnhandledException e) {
				System.err.println("Could not handle file " + file + ": " + e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}

		try {
			worker.write(outputFile);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Helper exception that is thrown when some file cannot be handled.
	 */
	public static class UnhandledException extends Exception {
		public static final long serialVersionUID = 0L;

		/**
		 * Constructor.
		 * @param message Description of the error.
		 */
		public UnhandledException(String message) {
			super(message);
		}
	}

	private static class Worker {
		private static final String FILE_HEADER = "<!DOCTYPE suite SYSTEM \"http://testng.org/testng-1.0.dtd\">%n"
			+ "<suite name=\"Suite\">%n";
		private static final String FILE_FOOTER = "</suite>%n";
		private static final String TEST_HEADER_FORMAT = "    <test name=\"%s\">%n"
			+ "        <classes>%n";
		private static final String TEST_FOOTER = "        </classes>%n"
			+ "    </test>%n";
		private static final String CLASS_TEMPLATE = "            <class name=\"%s\" />%n";
		private static final String ENDING = ".class";

		private final Map<String, Set<String>> testsToClasses = new HashMap<>();

		public void handleFile(String fileName) throws UnhandledException {
			// Given foo/bar/baz.class, we want foo.bar (packageName) and baz (className)

			// Construct the class name
			File file = new File(fileName);
			String baseName = file.getName();
			if (!baseName.endsWith(ENDING))
				throw new UnhandledException("File name does not end with " + ENDING);
			String className = baseName.substring(0, baseName.length() - ENDING.length());

			// Build the package name
			file = file.getParentFile();
			String separator = "";
			StringBuilder result = new StringBuilder();
			while (file != null) {
				result.insert(0, separator);
				result.insert(0, file.getName());
				file = file.getParentFile();
				separator = ".";
			}
			String packageName = result.toString();

			Set<String> set = testsToClasses.get(packageName);
			if (set == null) {
				set = new HashSet<>();
				testsToClasses.put(packageName, set);
			}

			set.add(packageName + "." + className);
		}

		public void write(File file) throws FileNotFoundException, UnsupportedEncodingException {
			Formatter format = null;
			try {
				format = new Formatter(file, "UTF-8");
				format.format(FILE_HEADER);
				for (Map.Entry<String, Set<String>> entry : testsToClasses.entrySet()) {
					format.format(TEST_HEADER_FORMAT, entry.getKey());
					for (String klass : entry.getValue())
						format.format(CLASS_TEMPLATE, klass);
					format.format(TEST_FOOTER);
				}
				format.format(FILE_FOOTER);
			} finally {
				if (format != null)
					format.close();
			}
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
