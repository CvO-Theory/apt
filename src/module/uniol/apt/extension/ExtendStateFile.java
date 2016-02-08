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

package uniol.apt.extension;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.io.FileUtils;

import uniol.apt.module.exception.ModuleException;

/** @author Renke Grunwald */
public class ExtendStateFile {
	private static final String MINIMAL_CODE_PREFIX = "m:";
	private static final String CURRENT_CODE_PREFIX = "c:";

	private final File file;
	private final int codeLength;

	private BitSet currentCode;
	private List<BitSet> minimalCodes;

	public ExtendStateFile(File file, int codeLength) {
		this.file = file;
		this.codeLength = codeLength;

		setMinimalCodes(new ArrayList<BitSet>());
		currentCode = null;
	}

	private BitSet codeStringToCode(String codeString) {
		BitSet code = new BitSet(codeLength);

		for (int i = 0; i < codeString.length(); i++) {
			if (codeString.charAt(i) == '1') {
				code.set(i);
			}
		}

		return code;
	}

	private String codeToCodeString(BitSet code) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < codeLength; i++) {
			if (code.get(i)) {
				builder.append('1');
			} else {
				builder.append('0');
			}
		}

		return builder.toString();
	}

	public void parse() throws IOException, ModuleException {
		try (InputStream is = FileUtils.openInputStream(this.file);
				Reader isr = new InputStreamReader(is, "UTF-8");
				BufferedReader reader = new BufferedReader(isr)) {
			String line;

			while ((line = reader.readLine()) != null) {
				if (line.length() - MINIMAL_CODE_PREFIX.length() != codeLength) {
					throw new ModuleException("State file was created for different LTS or g. "
							+ "Delete file and try again.");
				}

				if (line.startsWith(MINIMAL_CODE_PREFIX)) {
					String codeString = line.substring(MINIMAL_CODE_PREFIX.length());
					minimalCodes.add(codeStringToCode(codeString));
					continue;
				}

				if (line.startsWith(CURRENT_CODE_PREFIX)) {
					String codeString = line.substring(CURRENT_CODE_PREFIX.length());
					currentCode = codeStringToCode(codeString);
					continue;
				}

				throw new ModuleException(file.getAbsolutePath() + "is not a state file");
			}
		}
	}

	public void render() throws IOException {
		try (OutputStream os = FileUtils.openOutputStream(this.file);
				Writer osw = new OutputStreamWriter(os, "UTF-8");
				Writer writer = new BufferedWriter(osw)) {
			for (BitSet code : minimalCodes) {
				writer.write(MINIMAL_CODE_PREFIX + codeToCodeString(code) + "\n");
			}

			writer.write(CURRENT_CODE_PREFIX + codeToCodeString(currentCode) + "\n");
		}
	}

	public BitSet getCurrentCode() {
		return currentCode;
	}

	public List<BitSet> getMinimalCodes() {
		return minimalCodes;
	}

	public void setCurrentCode(BitSet currentCode) {
		this.currentCode = currentCode;
	}

	public void setMinimalCodes(List<BitSet> minimalCodes) {
		this.minimalCodes = minimalCodes;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
