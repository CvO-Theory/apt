/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2018  Uli Schlachter
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

package uniol.apt.adt.extension;

import org.testng.annotations.Test;

import uniol.apt.adt.exception.StructureException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uniol.apt.util.matcher.Matchers.pairWith;

/**
 * @author Uli Schlachter
 */
@SuppressWarnings("unchecked")
public class ExtensibleTest {
	private final Object someObjectA = new Object();
	private final Object someObjectB = new Object();
	private final Object someObjectC = new Object();
	private final Object someObjectD = new Object();

	@Test
	public void testCopyExtensions() {
		Extensible toCopy = new Extensible();
		toCopy.putExtension("a", someObjectA);
		toCopy.putExtension("b", someObjectB, ExtensionProperty.NOCOPY);
		toCopy.putExtension("c", someObjectC, ExtensionProperty.WRITE_TO_FILE);
		toCopy.putExtension("d", someObjectD, ExtensionProperty.WRITE_TO_FILE, ExtensionProperty.NOCOPY);

		Extensible copy = new Extensible();
		copy.copyExtensions(toCopy);

		// All properties without NOCOPY are copied
		assertThat(copy.getExtensionsWithoutProperty(ExtensionProperty.NOCOPY),
				containsInAnyOrder(pairWith("a", someObjectA), pairWith("c", someObjectC)));

		// The property "d" with both WRITE_TO_FILE and NOCOPY is not copied
		assertThat(copy.getExtensionsWithoutProperty(ExtensionProperty.WRITE_TO_FILE),
				contains(pairWith("a", someObjectA)));
	}

	@Test(expectedExceptions = { StructureException.class })
	public void testNonExistingExtension() {
		new Extensible().getExtension("DoesNotExist");
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
