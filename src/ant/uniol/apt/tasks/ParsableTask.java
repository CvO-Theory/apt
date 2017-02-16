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

package uniol.apt.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import uniol.apt.io.parser.LTSParsers;
import uniol.apt.io.parser.PNParsers;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.ParserNotFoundException;
import uniol.apt.io.parser.Parsers;
import uniol.apt.io.parser.impl.RegexParser;
import uniol.apt.tasks.parsers.AbstractParserTester;
import uniol.apt.tasks.parsers.ParserTester;

/**
 * Ant task to verify that a list of files is parsable.
 * @author vsp, Uli Schlachter
 */
public class ParsableTask {
	private ParsableTask() {
	}

	/**
	 * Program entry point. Arguments are output directory and directory to scan.
	 * @param args Program arguments.
	 */
	public static void main(String[] args) {
		if (args.length < 2)
			throw new IllegalArgumentException("Need at least two arguments: Output directory, directory to scan");

		File outputdir = new File(args[0]);
		File baseFile = new File(args[1]);

		if (!outputdir.exists())
			outputdir.mkdirs();
		if (!outputdir.isDirectory()) {
			System.err.println("Output directory is not a directory: " + outputdir);
			System.exit(1);
		}

		// Parse patterns given on the argument. A leading ! negates the pattern.
		AndFileFilter filter = new AndFileFilter();
		IOFileFilter negatedFilter = new NotFileFilter(filter);
		for (int i = 2; i < args.length; i++) {
			String arg = args[i];
			String wildcard = arg;
			boolean negate = wildcard.startsWith("!");

			if (negate)
				wildcard = arg.substring(1);
			IOFileFilter newFilter = new WildcardFileFilter(wildcard);
			if (negate)
				newFilter = new NotFileFilter(newFilter);
			filter.addFileFilter(newFilter);
		}

		// Get all excluded files
		Collection<File> excludedFiles = FileUtils.listFiles(baseFile, negatedFilter, TrueFileFilter.INSTANCE);

		// Now do the job
		boolean fail = false;
		List<AbstractParserTester> testers = new ArrayList<>();
		try {
			try {
				testers.add(new ParserTester<>(new RegexParser(), outputdir));
				testers.addAll(constructParserTesters(outputdir, PNParsers.INSTANCE, "genet"));
				testers.addAll(constructParserTesters(outputdir, LTSParsers.INSTANCE));
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}

			for (File file : FileUtils.listFiles(baseFile, TrueFileFilter.INSTANCE,
						TrueFileFilter.INSTANCE)) {
				String relativePath = baseFile.toURI().relativize(file.toURI()).getPath();
				boolean excluded = excludedFiles.contains(file);
				if (!parseFile(testers, file, relativePath, excluded))
					fail = true;
			}
		} finally {
			if (testers != null)
				for (AbstractParserTester tester : testers)
					tester.close();
		}

		if (!fail)
			return;
		System.err.println("Errors found; see above messages.");
		System.exit(1);
	}

	private static <G> List<AbstractParserTester> constructParserTesters(File outputdir, Parsers<G> parsers, String... skip)
			throws FileNotFoundException, UnsupportedEncodingException {
		List<AbstractParserTester> ret = new ArrayList<>();
		for (String format : parsers.getSupportedFormats()) {
			if (Arrays.asList(skip).contains(format))
				continue;
			try {
				ret.add(new ParserTester<>(parsers.getParser(format), outputdir));
			} catch (ParserNotFoundException ex) {
				throw new RuntimeException("Internal error: Parsers class claims to support a format, "
						+ "but can't provide a parser for it");
			}
		}
		return ret;
	}

	/** Do the work */
	private static boolean parseFile(Collection<AbstractParserTester> testers, File file, String fileName,
			boolean expectUnparsable) {
		// Try various parsers and hope that one of them works, but always check all parsers to make sure they
		// can handle unparsable files correctly.
		boolean fail = false;
		List<AbstractParserTester> successful = new ArrayList<>();

		for (AbstractParserTester tester : testers) {
			try {
				tester.parse(file);
				successful.add(tester);
			} catch (ParseException e) {
				// Ignore
			} catch (Exception e) {
				fail = true;
				tester.printException(fileName, e);
				System.err.println("Error while parsing file " + file.getPath());
				e.printStackTrace();
			}
		}

		if (successful.isEmpty() && !expectUnparsable) {
			fail = true;
			System.err.println("No parser managed to parse " + file.getPath());

			for (AbstractParserTester tester : testers) {
				tester.printUnparsable(fileName);
			}
		}
		if (expectUnparsable && !successful.isEmpty()) {
			System.err.println("Some parser unexpectedly managed to parse " + file.getPath());
			fail = true;
		} else if (successful.size() > 1) {
			fail = true;
			System.err.println("More than one parser managed to parse " + file.getPath());
		}
		for (AbstractParserTester tester : successful) {
			if (expectUnparsable) {
				tester.printUnexpectedParsable(fileName);
			} else if (successful.size() > 1) {
				tester.printMoreThanOneParser(fileName);
			} else {
				tester.printSuccess(fileName);
			}
		}

		return !fail;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
