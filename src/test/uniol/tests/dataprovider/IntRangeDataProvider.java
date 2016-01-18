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

package uniol.tests.dataprovider;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.testng.annotations.DataProvider;

import uniol.tests.dataprovider.annotations.IntRangeParameter;

/**
 * A class to provide a data provider method for testng returning parameter sets containing an integer parameter with
 * values out of the range specified by the {@link uniol.tests.dataprovider.annotations.IntRangeParameter} annotation.
 *
 * Example use:
 * <pre>
 * {@literal @Test(dataProvider = "IntRange", dataProviderClass = IntRangeDataProvider.class) }
 * {@literal @IntRangeParameter(start = 1, end = 30) }
 * </pre>
 * @author vsp
 */
public class IntRangeDataProvider {
	/**
	 * Iterator used by the getIntRange method.
	 */
	private static class IntRangeIterator implements Iterator<Object[]> {
		private final int end;
		private int cur;

		/**
		 * Constructor, this constructs a iterator out of the parameter annotation.
		 *
		 * @param range the parameter annotation
		 */
		private IntRangeIterator(IntRangeParameter range) {
			this.end = range.end();
			this.cur = range.start();
		}

		@Override
		public boolean hasNext() {
			return cur <= end;
		}

		@Override
		public Object[] next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			return new Object[]{cur++};
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Dummy constructor to ensure that this class cannot get instantiated.
	 */
	private IntRangeDataProvider() { /* empty */ }

	/**
	 * Method to provide parameter sets for testng.
	 *
	 * This method get called by testng.
	 *
	 * @param method the method which should get called with the parameter sets provided by this method.
	 * @return an array of parameter sets (also in an array)
	 */
	@DataProvider(name = "IntRange", parallel = true)
	public static Iterator<Object[]> getIntRange(Method method) {
		if (method == null) {
			throw new IllegalArgumentException("Test Method context cannot be null.");
		}

		// get annotation from method
		IntRangeParameter range = method.getAnnotation(IntRangeParameter.class);
		if (range == null) {
			// get annotation from the class to which the method belongs
			range = method.getDeclaringClass().getAnnotation(IntRangeParameter.class);
		}
		if (range == null) {
			throw new IllegalArgumentException("Test Method context needs a IntRange annotation.");
		}

		return new IntRangeIterator(range);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
