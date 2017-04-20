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

package uniol.apt.tasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * Ant tasks to run our integration tests.
 * @author Uli Schlachter
 */
public class IntegrationTestTask {
	static final private String ARGS_SUFFIX = ".args.txt";
	static final private String STDOUT_SUFFIX = ".out.txt";
	static final private String STDERR_SUFFIX = ".err.txt";
	static final private String EXIT_CODE_SUFFIX = ".exit.txt";

	private IntegrationTestTask() {
	}

	/**
	 * Program entry point. Arguments are program to run and directory to scan.
	 * @param args Program arguments.
	 * @throws Exception In case something goes wrong.
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 2)
			throw new IllegalArgumentException(
					"Need exactly two arguments: program to run, directory to scan");

		String cmdline = args[0];
		File directoryToScan = new File(args[1]);

		Iterator<File> fileIter = FileUtils.iterateFiles(directoryToScan,
					new WildcardFileFilter("*" + ARGS_SUFFIX),
					TrueFileFilter.INSTANCE);
		int count = 0;
		int failed = 0;
		while (fileIter.hasNext()) {
			count++;
			File file = fileIter.next();
			try {
				if (!runTest(cmdline, file)) {
					failed++;
					System.out.println("Failure for " + file.getName());
				}
			} catch (Exception e) {
				System.err.println("Exception occurred while parsing " + file.getAbsolutePath());
				throw e;
			}
		}

		System.out.println("Successfully ran " + count + " integration tests");
		if (failed != 0) {
			System.out.println(failed + " tests failed");
			System.exit(1);
		}
	}

	private static boolean runTest(String commandLine, File file) throws IOException, InterruptedException {
		String commands = readFileWithSuffix(file, ARGS_SUFFIX);
		String expectedStdOut = readFileWithSuffix(file, STDOUT_SUFFIX);
		String expectedStdErr = readFileWithSuffix(file, STDERR_SUFFIX);
		int expectedExitCode = toInteger(readFileWithSuffix(file, EXIT_CODE_SUFFIX));

		Process process = Runtime.getRuntime().exec(commandLine + " " + commands);
		Result result;
		try {
			result = Result.getResult(process);
		} finally {
			process.destroy();
		}

		boolean success = true;

		if (result.exitCode != expectedExitCode) {
			success = false;
			System.err.println(String.format("Unexpected status code %d, expected %d ",
					result.exitCode, expectedExitCode));
		}
		if (!expectedStdErr.equals(result.error)) {
			success = false;
			System.err.println("Wrong error output:");
			System.err.println(findDifference(expectedStdErr, result.error));
		}
		if (!expectedStdOut.equals(result.output)) {
			success = false;
			System.err.println("Wrong output:");
			System.err.println(findDifference(expectedStdOut, result.output));
		}

		return success;
	}

	private static String readFileWithSuffix(File file, String suffix) throws IOException {
		File parent = file.getParentFile();
		String name = file.getName();
		assert name.substring(name.length() - ARGS_SUFFIX.length()).equals(ARGS_SUFFIX);
		name = name.substring(0, name.length() - ARGS_SUFFIX.length()) + suffix;
		File targetFile = new File(parent, name);
		if (!targetFile.exists())
			return "";
		return FileUtils.readFileToString(targetFile, "UTF-8").replace("\n", System.lineSeparator());
	}

	private static int toInteger(String str) {
		str = str.trim();
		if (str.isEmpty())
			return 0;
		return Integer.parseInt(str);
	}

	private static String findDifference(String expected, String actual) {
		// Remove common prefix
		int firstDifference = Math.min(expected.length(), actual.length());
		for (int i = 0; i < Math.min(expected.length(), actual.length()); i++) {
			if (expected.charAt(i) != actual.charAt(i)) {
				firstDifference = i;
				break;
			}
		}
		expected = expected.substring(firstDifference);
		actual = actual.substring(firstDifference);

		// Remove common suffix
		firstDifference = Math.min(expected.length(), actual.length());
		for (int i = 0; i < Math.min(expected.length(), actual.length()); i++) {
			if (expected.charAt(expected.length() - i - 1) != actual.charAt(actual.length() - i - 1)) {
				firstDifference = i;
				break;
			}
		}
		expected = expected.substring(0, expected.length() - firstDifference);
		actual = actual.substring(0, actual.length() - firstDifference);

		// Return result
		return "Got:\n...\n" + actual + "\n...but expected...\n" + expected + "\n...";
	}

	private static class Result {
		public final String output;
		public final String error;
		public final int exitCode;

		private Result(String output, String error, int exitCode) {
			this.output = output;
			this.error = error;
			this.exitCode = exitCode;
		}

		static public Result getResult(Process process) throws IOException, InterruptedException {
			process.getOutputStream().close();
			GatherThread input = new GatherThread(process.getInputStream());
			GatherThread error = new GatherThread(process.getErrorStream());
			input.start();
			error.start();
			int exitCode = process.waitFor();
			input.join();
			error.join();
			return new Result(input.getString(), error.getString(), exitCode);
		}
	}

	private static class GatherThread extends Thread {
		private final InputStream stream;
		private String result;
		private IOException exception;

		public GatherThread(InputStream stream) {
			this.stream = stream;
		}

		@Override
		public void run() {
			try {
				result = IOUtils.toString(stream, "UTF-8");
			} catch (IOException e) {
				this.exception = e;
			}
		}

		public String getString() throws IOException {
			if (this.exception != null)
				throw this.exception;
			return this.result;
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
