/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

package uniol.apt.analysis.synthesize;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static uniol.apt.analysis.synthesize.Matchers.*;

/** @author Uli Schlachter */
@Test
@SuppressWarnings("unchecked")
public class PNPropertiesTest {
	private PNProperties properties;

	@BeforeMethod
	public void setup() {
		properties = new PNProperties();
	}

	@Test
	public void emptyPropertiesTest() {
		assertThat(properties.size(), equalTo(0));
		assertThat(properties.iterator().hasNext(), equalTo(false));
	}

	@Test(expectedExceptions = NoSuchElementException.class)
	public void emptyPropertiesIterationTest() {
		properties.iterator().next();
	}

	@DataProvider(name = "properties")
	private Object[][] createProperties() {
		return new Object[][] {
			{ PNProperties.kBounded(42) },
			{ PNProperties.SAFE },
			{ PNProperties.PURE },
			{ PNProperties.PLAIN },
			{ PNProperties.TNET },
			{ PNProperties.OUTPUT_NONBRANCHING },
			{ PNProperties.CONFLICT_FREE },
		};
	}

	@Test
	public void addAllTest() {
		properties.addAll(Arrays.asList(PNProperties.SAFE, PNProperties.kBounded(7), PNProperties.PURE,
					PNProperties.PLAIN, PNProperties.TNET, PNProperties.OUTPUT_NONBRANCHING,
					PNProperties.CONFLICT_FREE));
		assertThat(properties, containsInAnyOrder(PNProperties.SAFE, PNProperties.PURE, PNProperties.PLAIN,
					PNProperties.TNET, PNProperties.OUTPUT_NONBRANCHING,
					PNProperties.CONFLICT_FREE));
	}

	@Test(dataProvider = "properties")
	public void notContainsTest(PNProperties.PNProperty property) {
		assertThat(properties.contains(property), equalTo(false));
	}

	@Test(dataProvider = "properties")
	public void addTest(PNProperties.PNProperty property) {
		assertThat(properties.add(property), equalTo(true));
	}

	@Test
	public static class SingletonListTest {
		private Object[] makeSimpleProperty(PNProperties.PNProperty prop) {
			return new Object[] {
				new PNProperties(prop), new PNProperties.PNProperty[] { prop }
			};
		}

		@DataProvider(name = "propertiesAndMatchers")
		private Object[][] createProperties() {
			return new Object[][] {
				makeSimpleProperty(PNProperties.kBounded(42)),
				makeSimpleProperty(PNProperties.SAFE),
				makeSimpleProperty(PNProperties.PURE),
				makeSimpleProperty(PNProperties.PLAIN),
				{ new PNProperties(PNProperties.TNET),
					new PNProperties.PNProperty[] { PNProperties.TNET, PNProperties.PLAIN,
						PNProperties.PURE, PNProperties.OUTPUT_NONBRANCHING } },
				makeSimpleProperty(PNProperties.OUTPUT_NONBRANCHING),
				{ new PNProperties(PNProperties.CONFLICT_FREE),
					new PNProperties.PNProperty[] { PNProperties.CONFLICT_FREE, PNProperties.PLAIN } },
			};
		}

		@Test(dataProvider = "propertiesAndMatchers")
		public void containsTest(PNProperties properties, PNProperties.PNProperty[] property) {
			assertThat(properties.contains(property[0]), equalTo(true));
		}

		@Test(dataProvider = "propertiesAndMatchers")
		public void iterateTest(PNProperties properties, PNProperties.PNProperty[] property) {
			assertThat(properties, containsInAnyOrder(property));
		}

		@Test(dataProvider = "propertiesAndMatchers")
		public void sizeTest(PNProperties properties, PNProperties.PNProperty[] property) {
			assertThat(properties.size(), equalTo(property.length));
		}
	}

	@Test
	public void addKBoundedAndSafeTest() {
		assertThat(properties.add(PNProperties.kBounded(7)), equalTo(true));
		assertThat(properties.add(PNProperties.SAFE), equalTo(true));
	}

	@Test
	public void addSafeAndKBoundedTest() {
		assertThat(properties.add(PNProperties.SAFE), equalTo(true));
		assertThat(properties.add(PNProperties.kBounded(7)), equalTo(false));
	}

	@Test
	public void testWithout() {
		properties.add(PNProperties.SAFE);
		properties.add(PNProperties.PLAIN);
		assertThat(properties.without(PNProperties.PLAIN), equalTo(new PNProperties(PNProperties.SAFE)));
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
