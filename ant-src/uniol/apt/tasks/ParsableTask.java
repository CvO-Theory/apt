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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import static org.apache.tools.ant.Project.MSG_ERR;

import uniol.apt.tasks.parsers.*;

/**
 * Ant task to verify that a list of files is parsable.
 * @author vsp, Uli Schlachter
 */
public class ParsableTask extends Task {
	private final List<FileSet> filesets = new ArrayList<>();
	private final List<FileSet> excludeFilesets = new ArrayList<>();
	private File outputdir;

	/**
	 * Method which get called by ant when a nested fileset element is parsed.
	 * @param fileset the new fileset
	 */
	public void addFileset(FileSet fileset) {
		filesets.add(fileset);
	}

	/**
	 * Method which get called by ant when a nested fileset element is parsed.
	 * @param fileset the new fileset
	 */
	public void addExclude(FileSet fileset) {
		excludeFilesets.add(fileset);
	}

	public void setOutputdir(File dir) {
		outputdir = dir;
	}

	/** Execute the task. */
	@Override
	public void execute() {
		if (filesets.isEmpty()) {
			throw new BuildException("No nested fileset element found.");
		}
		if (outputdir == null) {
			throw new BuildException("No output directory set.");
		}

		if (!outputdir.exists())
			outputdir.mkdirs();
		if (!outputdir.isDirectory())
			throw new BuildException("Output directory is not a directory: " + outputdir);

		Set<File> excludedFiles = new HashSet<>();
		for (FileSet fs : excludeFilesets) {
			DirectoryScanner ds = fs.getDirectoryScanner(getProject());
			File baseDir        = ds.getBasedir();
			for (String fileName : ds.getIncludedFiles()) {
				excludedFiles.add(new File(baseDir, fileName));
			}
		}

		ParserTester[] testers = null;
		try {
			try {
				testers = new ParserTester[] {
					new APTPNParserTester(outputdir),
					new APTLTSParserTester(outputdir),
					new PetrifyPNParserTester(outputdir),
					new SynetLTSParserTester(outputdir),
					new SynetPNParserTester(outputdir),
					new APTRegexParserTester(outputdir),
				};
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				throw new BuildException(e);
			}

			for (FileSet fs : filesets) {
				DirectoryScanner ds = fs.getDirectoryScanner(getProject());
				File baseDir        = ds.getBasedir();
				for (String fileName : ds.getIncludedFiles()) {
					File file = new File(baseDir, fileName);
					parseFile(testers, file, fileName, excludedFiles.contains(file));
				}
			}
		} finally {
			if (testers != null)
				for (ParserTester tester : testers)
					tester.close();
		}
	}

	/** Do the work */
	private boolean parseFile(ParserTester[] testers, File file, String fileName, boolean expectUnparsable) {
		// Try various parsers and hope that one of them works, but always check all parsers to make sure they
		// can handle unparsable files correctly.
		boolean fail = false;
		List<ParserTester> successful = new ArrayList<>();

		for (ParserTester tester : testers) {
			try {
				tester.parse(file);
				successful.add(tester);
			} catch (UnparsableException e) {
				// Ignore
			} catch (Exception e) {
				fail = true;
				tester.printException(fileName, e);
				log("Error while parsing file " + file.getPath(), MSG_ERR);
				e.printStackTrace();
			}
		}

		if (successful.isEmpty() && !expectUnparsable) {
			fail = true;
			log("No parser managed to parse " + file.getPath(), MSG_ERR);

			for (ParserTester tester : testers) {
				tester.printUnparsable(fileName);
			}
		}
		if (expectUnparsable && !successful.isEmpty()) {
			log("Some parser unexpectedly managed to parse " + file.getPath(), MSG_ERR);
			fail = true;
		} else if (successful.size() > 1) {
			fail = true;
			log("More than one parser managed to parse " + file.getPath(), MSG_ERR);
		}
		for (ParserTester tester : successful) {
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
