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

package uniol.apt.ui.impl.parameter;

import uniol.apt.analysis.language.Word;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.ui.ParameterTransformation;

/**
 * Transform a string into an instance of Word.
 * @author Uli Schlachter, Daniel
 */
public class WordParameterTransformation implements ParameterTransformation<Word> {
	@Override
	public Word transform(String arg) throws ModuleException {
		String[] tokens;
		if (arg.contains(";")) {
			// with ; --> "a;b;c"/ "a; b ; c" possible
			tokens = arg.split(";", -1);
		} else if (arg.contains(",")) {
			tokens = arg.split(",", -1);
		} else {
			tokens = arg.split(" ", -1);
		}

		Word word = new Word();
		for (String t : tokens) {
			t = t.trim();
			if (t.isEmpty())
				throw new ModuleException("Empty label found while parsing word '" + arg + "' after " +
						word + ".");
			word.add(t);
		}
		return word;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
