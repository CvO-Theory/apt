/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015 Uli Schlachter
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

package uniol.apt.tasks;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceConfigurationError;

/**
 * Task to verify that the apt.jar file works.
 * @author vsp, Uli Schlachter
 */
public class ServiceVerifyTask {
	private ServiceVerifyTask() {
	}

	/**
	 * Program entry point. Arguments are a path to a JAR file, the class to load from there and the name of a
	 * static member that should be instantiated.
	 * @param args Program arguments.
	 */
	public static void main(String[] args) {
		if (args.length != 3)
			throw new IllegalArgumentException(
					"Need exactly three arguments: Path to jar, class to load, static member to instantiate");

		try {
			load(args[0], args[1], args[2]);
		} catch (FailureException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	private static void load(String classpath, String klass, String member) throws FailureException {
		String suggestion = " Run 'ant clean' and try again.";
		URL classpathURL;
		try {
			classpathURL = new File(classpath).toURI().toURL();
		} catch (MalformedURLException e) {
			throw new FailureException("The path argument is invalid", e);
		}
		URLClassLoader loader = new URLClassLoader(new URL[] { classpathURL });

		try {
			loader.loadClass(klass).getField(member).get(null);
		} catch (ClassNotFoundException e) {
			throw new FailureException("Could not load class '" + klass + "'." + suggestion, e);
		} catch (NoSuchFieldException e) {
			throw new FailureException("Could not find '" + klass + "#" + member + "'." + suggestion, e);
		} catch (IllegalAccessException e) {
			throw new FailureException("Could not access '" + klass + "#" + member + "'." + suggestion, e);
		} catch (ServiceConfigurationError | ExceptionInInitializerError e) {
			throw new FailureException("The service configuration for '" + klass + "#" + member
					+ "' is broken." + suggestion, e);
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
