/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2017  Members of the project group APT
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

package uniol.apt.io.parser.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.ParserSkipException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.adt.matcher.Matchers.*;

/** @author Uli Schlachter */
@SuppressWarnings("unchecked") // Sigh, generics
public class LoLAPNParserTest {

	@Test
	public void simpleTest() {
		String file = "nets/lola/phils3.llnet";
		PetriNet pn;

		try {
			pn = new LoLAPNParser().parseFile(file);
		} catch (IOException | ParseException e) {
			throw new ParserSkipException(file, LoLAPNParser.class, e);
		}

		assertThat(pn.getPlaces(), containsInAnyOrder(nodeWithID("gabel.1"), nodeWithID("gabel.2"),
					nodeWithID("gabel.3"), nodeWithID("essend.1"), nodeWithID("essend.2"),
					nodeWithID("essend.3"), nodeWithID("denkend.1"), nodeWithID("denkend.2"),
					nodeWithID("denkend.3"), nodeWithID("wartend.1"), nodeWithID("wartend.2"),
					nodeWithID("wartend.3")));
		assertThat(pn.getTransitions(), containsInAnyOrder(nodeWithID("iss.[p=1]"), nodeWithID("iss.[p=2]"),
					nodeWithID("iss.[p=3]"), nodeWithID("nimm.[p=1]"), nodeWithID("nimm.[p=2]"),
					nodeWithID("nimm.[p=3]"), nodeWithID("satt.[p=1]"), nodeWithID("satt.[p=2]"),
					nodeWithID("satt.[p=3]")));

		Map<String, Long> initial = new HashMap<>();
		initial.put("gabel.1", 1l);
		initial.put("gabel.2", 1l);
		initial.put("gabel.3", 1l);
		initial.put("denkend.1", 1l);
		initial.put("denkend.2", 1l);
		initial.put("denkend.3", 1l);
		initial.put("essend.1", 0l);
		initial.put("essend.2", 0l);
		initial.put("essend.3", 0l);
		initial.put("wartend.1", 0l);
		initial.put("wartend.2", 0l);
		initial.put("wartend.3", 0l);
		assertThat(pn.getInitialMarking(), markingThatIs(initial));

		assertThat(pn.getEdges(), containsInAnyOrder(
					flowThatConnects("gabel.2", "iss.[p=1]"),
					flowThatConnects("wartend.1", "iss.[p=1]"),
					flowThatConnects("iss.[p=1]", "essend.1"),
					flowThatConnects("gabel.3", "iss.[p=2]"),
					flowThatConnects("wartend.2", "iss.[p=2]"),
					flowThatConnects("iss.[p=2]", "essend.2"),
					flowThatConnects("gabel.1", "iss.[p=3]"),
					flowThatConnects("wartend.3", "iss.[p=3]"),
					flowThatConnects("iss.[p=3]", "essend.3"),
					flowThatConnects("gabel.1", "nimm.[p=1]"),
					flowThatConnects("denkend.1", "nimm.[p=1]"),
					flowThatConnects("nimm.[p=1]", "wartend.1"),
					flowThatConnects("gabel.2", "nimm.[p=2]"),
					flowThatConnects("denkend.2", "nimm.[p=2]"),
					flowThatConnects("nimm.[p=2]", "wartend.2"),
					flowThatConnects("gabel.3", "nimm.[p=3]"),
					flowThatConnects("denkend.3", "nimm.[p=3]"),
					flowThatConnects("nimm.[p=3]", "wartend.3"),
					flowThatConnects("essend.1", "satt.[p=1]"),
					flowThatConnects("satt.[p=1]", "gabel.1"),
					flowThatConnects("satt.[p=1]", "gabel.2"),
					flowThatConnects("satt.[p=1]", "denkend.1"),
					flowThatConnects("essend.2", "satt.[p=2]"),
					flowThatConnects("satt.[p=2]", "gabel.2"),
					flowThatConnects("satt.[p=2]", "gabel.3"),
					flowThatConnects("satt.[p=2]", "denkend.2"),
					flowThatConnects("essend.3", "satt.[p=3]"),
					flowThatConnects("satt.[p=3]", "gabel.3"),
					flowThatConnects("satt.[p=3]", "gabel.1"),
					flowThatConnects("satt.[p=3]", "denkend.3")));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
