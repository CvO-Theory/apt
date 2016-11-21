/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016  Uli Schlachter
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

package uniol.apt.ui.impl.parameter;

import java.util.Arrays;

import uniol.apt.analysis.language.Word;
import uniol.apt.analysis.language.WordList;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.ui.AptParameterTransformation;
import uniol.apt.ui.ParameterTransformation;

/**
 * Transform a string into an instance of Word.
 * @author Uli Schlachter
 */
@AptParameterTransformation(value = WordList.class, fileSource = true)
public class WordListParameterTransformation implements ParameterTransformation<WordList> {
	@Override
	public WordList transform(String arg) throws ModuleException {
		WordList result = new WordList();
		String[] lines = arg.split("\\r?\\n");
		for (int index = 0; index < lines.length; index++) {
			String[] tokens = lines[index].split("");
			// I don't know why, but apparently some Java versions generate an empty first entry
			if (tokens.length > 0 && tokens[0].isEmpty())
				tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
			result.add(new Word(Arrays.asList(tokens)));
		}

		return result;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
