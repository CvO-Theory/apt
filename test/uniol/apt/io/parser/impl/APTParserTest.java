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

package uniol.apt.io.parser.impl;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import uniol.apt.CrashCourseNets;
import uniol.apt.io.parser.impl.apt.APTParser;

/**
 * @author Manuel Gieseking
 * <p/>
 */
public class APTParserTest {

	@Test
	public void testAPTParser() throws Exception {
		APTParser parser = new APTParser();
		parser.parse("nets/crashkurs-cc1-net.apt");
		assertNotNull(parser.getPn());
		assertNull(parser.getTs());
		parser.parse("nets/crashkurs-cc1-aut.apt");
		assertNotNull(parser.getTs());
		assertNull(parser.getPn());
	}

	@Test
	public void testLTSandPN() throws Exception {
		APTParser parser = new APTParser();
		parser.parse("nets/crashkurs-cc1-aut.apt");
		assertNull(parser.getPn());
		assertNotNull(parser.getTs());
		parser = new APTParser();
		parser.parse("nets/crashkurs-cc1-net.apt");
		assertNotNull(parser.getPn());
		assertNull(parser.getTs());
	}

	@Test
	public void testCrashCourseNets() throws Exception {
		assertNotNull(CrashCourseNets.getCCNet1());
		assertNotNull(CrashCourseNets.getCCNet2());
		assertNotNull(CrashCourseNets.getCCNet2inf());
		assertNotNull(CrashCourseNets.getCCNet3());
		assertNotNull(CrashCourseNets.getCCNet4());
		assertNotNull(CrashCourseNets.getCCNet5());
		assertNotNull(CrashCourseNets.getCCNet6());
		assertNotNull(CrashCourseNets.getCCNet7());
		assertNotNull(CrashCourseNets.getCCNet8());
		assertNotNull(CrashCourseNets.getCCNet9());
		assertNotNull(CrashCourseNets.getCCNet10());
		assertNotNull(CrashCourseNets.getCCNet11());
		assertNotNull(CrashCourseNets.getCCNet12());
		assertNotNull(CrashCourseNets.getCCNet13());
		assertNotNull(CrashCourseNets.getCCNet14());
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
