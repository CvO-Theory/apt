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

package uniol.apt.generator.isolated;

import java.util.Iterator;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import static uniol.apt.TestNetCollection.*;

import uniol.tests.dataprovider.IntRangeDataProvider;
import uniol.tests.dataprovider.annotations.IntRangeParameter;

import uniol.apt.adt.pn.PetriNet;

/** @author Uli Schlachter */
public class IsolatedTransitionsGeneratorTest {
	private void testNet(PetriNet pn, int maxSize) {
		int transitions = pn.getTransitions().size();
		Iterator<PetriNet> iter = new IsolatedTransitionsGenerator(pn, maxSize).iterator();

		for (; transitions <= maxSize; transitions++)
			assertEquals(iter.next().getTransitions().size(), transitions);
		assertFalse(iter.hasNext());
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 0, end = 30)
	public void testEmptyNet(int size) {
		testNet(getEmptyNet(), size);
	}

	@Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class)
	@IntRangeParameter(start = 0, end = 30)
	public void testABCLanguageNet(int size) {
		testNet(getABCLanguageNet(), size);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
