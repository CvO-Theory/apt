/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015       vsp
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

package uniol.apt.io.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/**
 * Class which provides the correct parser for a given format.
 *
 * @author vsp
 *
 * @param <T> The class which the parsers generate.
 */
public class AbstractParsers<T> implements Parsers<T> {
	private Map<String, Parser<T>> parsers;

	/**
	 * Constructor
	 *
	 * @param clazz Class object of the which the parsers should generate
	 */
	@SuppressWarnings("unchecked") // I hate type erasure and other java things ...
	protected AbstractParsers(Class<T> clazz) {
		this.parsers     = new HashMap<>();

		String className = clazz.getCanonicalName();

		ClassLoader cl   = getClass().getClassLoader();
		try {
			Enumeration<URL> parserNames = cl.getResources("META-INF/uniol/apt/compiler/"
					+ Parser.class.getCanonicalName() + "/" + className);

			while (parserNames.hasMoreElements()) {
				try (InputStream is = parserNames.nextElement().openStream()) {
					LineIterator lIter = IOUtils.lineIterator(is, "UTF-8");
					while (lIter.hasNext()) {
						String parserName = lIter.next();
						Class<? extends Parser<T>> parserClass;
						try {
							parserClass =
								(Class<? extends Parser<T>>) cl.loadClass(parserName);
						} catch (ClassNotFoundException ex) {
							throw new RuntimeException(String.format(
									"Could not load class %s",
									parserName), ex);
						}
						Parser<T> parser;
						try {
							parser = parserClass.newInstance();
						} catch (ClassCastException
								| IllegalAccessException
								| InstantiationException ex) {
							throw new RuntimeException(String.format(
									"Could not instantiate %s",
									parserName), ex);
						}
						String format = parser.getFormat();
						if (format == null || format.equals("")
								|| !format.equals(format.toLowerCase())) {
							throw new RuntimeException(String.format(
									"Parser %s reports an invalid format: %s",
									parserName, format));
						}
						Parser<T> oldParser = this.parsers.get(format);
						if (oldParser != null && !oldParser.getClass().equals(parserClass)) {
							throw new RuntimeException(String.format(
								"Different parsers claim, to interpret format %s:"
								+ " %s and %s", format,
								oldParser.getClass().getCanonicalName(),
								parserName));
						}
						this.parsers.put(format, parser);
					}
				}
			}
		} catch (IOException ex) {
			throw new RuntimeException("Failed to discover parsers", ex);
		}

	}

	@Override
	public Parser<T> getParser(String format) throws ParserNotFoundException {
		Parser<T> parser = this.parsers.get(format.toLowerCase());
		if (parser == null) {
			throw new ParserNotFoundException(String.format("Parser for format %s not found", format));
		}
		return parser;
	}

	@Override
	public Set<String> getSupportedFormats() {
		return this.parsers.keySet();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
