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
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import static org.apache.tools.ant.Project.MSG_VERBOSE;

/**
 * Ant task to verify that the apt.jar file works.
 * @author vsp, Uli Schlachter
 */
public class ServiceVerifyTask extends Task {
	private URL classpathURL = null;
	private final List<LoadElement> toLoad = new ArrayList<>();

	public void setJar(File file) throws MalformedURLException {
		classpathURL = file.toURI().toURL();
	}

	public void addLoad(LoadElement elem) {
		toLoad.add(elem);
	}

	@Override
	public void execute() {
		if (toLoad.isEmpty()) {
			throw new BuildException("No classes to load.");
		}

		URLClassLoader loader = new URLClassLoader(new URL[] { classpathURL });
		String suggestion = " Run 'ant clean' and try again.";
		for (LoadElement elem : toLoad) {
			try {
				elem.load(loader);
				log("Successfully loaded " + elem.toString(), MSG_VERBOSE);
			} catch (ClassNotFoundException e) {
				throw new BuildException("Could not load class '" + elem.klass + "'." + suggestion, e);
			} catch (NoSuchFieldException e) {
				throw new BuildException("Could not find '" + elem.toString() + "'." + suggestion, e);
			} catch (IllegalAccessException e) {
				throw new BuildException("Could not access '" + elem.toString() + "'." + suggestion, e);
			} catch (ServiceConfigurationError | ExceptionInInitializerError e) {
				throw new BuildException("The service configuration for '" + elem.toString()
						+ "' is broken." + suggestion, e);
			}
		}
	}

	static public class LoadElement {
		public String klass;
		public String member;

		public void setClass(String klass) {
			this.klass = klass;
		}

		public void setMember(String member) {
			this.member = member;
		}

		public void load(ClassLoader loader)
				throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
			// We assume that this causes the field to be constructed and as a side-effect a broken setup
			// throws an exception / error.
			loader.loadClass(klass).getField(member).get(null);
		}

		@Override
		public String toString() {
			return klass + "#" + member;
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
