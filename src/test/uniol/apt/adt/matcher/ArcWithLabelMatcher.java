/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2016       vsp
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

package uniol.apt.adt.matcher;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import uniol.apt.adt.ts.Arc;

/**
 * Matcher to verify that an arc has a matching label.
 *
 * @author Uli Schlachter, vsp
 */
public class ArcWithLabelMatcher extends FeatureMatcher<Arc, String> {
	private ArcWithLabelMatcher(Matcher<? super String> matcher) {
		super(matcher, "Arc with Label", "Label");
	}

	@Override
	protected String featureValueOf(Arc arc) {
		return arc.getLabel();
	}

	public static <T> Matcher<Arc> arcWithLabel(Matcher<? super String> matcher) {
		return new ArcWithLabelMatcher(matcher);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
