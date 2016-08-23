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

package uniol.apt.tasks;

import static org.apache.tools.ant.Project.MSG_ERR;
import static org.apache.tools.ant.Project.MSG_WARN;
import static uniol.apt.tasks.modules.ModuleParameterVerifyMethodVisitor.DIFFERENT_TYPES_DETECTED_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import uniol.apt.tasks.modules.ModuleParameterVerifyClassVisitor;

/**
 * Ant task to verify if the modules parameter use is correct.
 * @author vsp
 */
public class ModuleParameterVerifyTask extends Task {
	private final List<FileSet> filesets = new ArrayList<>();

	/**
	 * Method which get called by ant when a nested fileset element is parsed.
	 * @param fileset the new fileset
	 */
	public void addFileset(FileSet fileset) {
		filesets.add(fileset);
	}

	/** Execute the task. */
	public void execute() {
		if (filesets.isEmpty()) {
			throw new BuildException("No nested fileset element found.");
		}

		Map<String, ModuleParameterVerifyClassVisitor> classes = new HashMap<>();
		for (FileSet fs : filesets) {
			DirectoryScanner ds = fs.getDirectoryScanner(getProject());
			File baseDir        = ds.getBasedir();
			for (String className : ds.getIncludedFiles()) {
				File classFile = new File(baseDir, className);
				ClassReader reader;
				try {
					reader = new ClassReader(FileUtils.openInputStream(classFile));
				} catch(IOException ex) {
					throw new BuildException("Error accessing class: " + className, ex);
				}
				ModuleParameterVerifyClassVisitor cv = new ModuleParameterVerifyClassVisitor();
				reader.accept(cv, 0);

				cv = classes.put(cv.getClassName(), cv);
				if (cv != null)
					throw new BuildException("Multiple definitions for class: " + cv.getClassName());
			}
		}

		for (ModuleParameterVerifyClassVisitor cv : classes.values()) {
			analyseClass(cv, classes);
		}
	}

	/** Get class information */
	private void getClassInformation(ModuleParameterVerifyClassVisitor cv,
			Map<String, ModuleParameterVerifyClassVisitor> classes, Map<String, Type> requestedInputs,
			Map<String, Type> optionalInputs, Map<String, Type> usedInputs,
			Map<String, Type> announcedOutputs, Map<String, Type> providedOutputs) {
		requestedInputs .putAll(cv.getRequestedInputs());
		optionalInputs  .putAll(cv.getOptionalInputs());
		usedInputs      .putAll(cv.getUsedInputs());
		announcedOutputs.putAll(cv.getAnnouncedOutputs());
		providedOutputs .putAll(cv.getProvidedOuputs());

		cv = classes.get(cv.getSuperclassName());
		if (cv != null)
			getClassInformation(cv, classes, requestedInputs, optionalInputs, usedInputs,
					announcedOutputs, providedOutputs);
	}

	/** Do the work */
	private void analyseClass(ModuleParameterVerifyClassVisitor cv, Map<String, ModuleParameterVerifyClassVisitor> classes) {
		String className = cv.getClassName();

		Map<String, Type> requestedInputs  = new HashMap<>();
		Map<String, Type> optionalInputs   = new HashMap<>();
		Map<String, Type> usedInputs       = new HashMap<>();
		Map<String, Type> announcedOutputs = new HashMap<>();
		Map<String, Type> providedOutputs  = new HashMap<>();

		getClassInformation(cv, classes, requestedInputs, optionalInputs, usedInputs,
				announcedOutputs, providedOutputs);

		boolean fail = false;

		Set<String> requiredOptionalInputs = new HashSet<>(requestedInputs.keySet());
		requiredOptionalInputs.retainAll(optionalInputs.keySet());

		for (String parameter : requiredOptionalInputs) {
			log("error: " + className + ": Parameter specified as required and also as optional: " + parameter, MSG_ERR);
			fail = true;
		}

		for (String parameter : requestedInputs.keySet()) {
			Type usedClass      = usedInputs.get(parameter);
			Type requestedClass = requestedInputs.get(parameter);

			if (requestedClass == DIFFERENT_TYPES_DETECTED_TYPE) {
				log("error: " + className + ": Parameter required with multiple types: " + parameter, MSG_ERR);
				fail = true;
			} else if (usedClass == null) {
				log("warning: " + className + ": Never used parameter: " + parameter, MSG_WARN);
			} else if (usedClass == DIFFERENT_TYPES_DETECTED_TYPE) {
				log("error: " + className + ": Parameter used with multiple types: " + parameter, MSG_ERR);
				fail = true;
			} else if (!usedClass.equals(requestedClass)) {
				log("error: " + className + ": Parameter used with other type as requested: " + parameter, MSG_ERR);
				fail = true;
			}
		}

		for (String parameter : optionalInputs.keySet()) {
			Type usedClass      = usedInputs.get(parameter);
			Type requestedClass = optionalInputs.get(parameter);

			if (requestedClass == DIFFERENT_TYPES_DETECTED_TYPE) {
				log("error: " + className + ": Parameter required with multiple types: " + parameter, MSG_ERR);
				fail = true;
			} else if (usedClass == null) {
				log("warning: " + className + ": Never used parameter: " + parameter, MSG_WARN);
			} else if (usedClass == DIFFERENT_TYPES_DETECTED_TYPE) {
				log("error: " + className + ": Parameter used with multiple types: " + parameter, MSG_ERR);
				fail = true;
			} else if (!usedClass.equals(requestedClass)) {
				log("error: " + className + ": Parameter used with other type as requested: " + parameter, MSG_ERR);
				fail = true;
			}
		}

		Set<String> unknownInputs = new HashSet<>(usedInputs.keySet());
		unknownInputs.removeAll(requestedInputs.keySet());
		unknownInputs.removeAll(optionalInputs.keySet());
		for (String parameter : unknownInputs) {
			log("error: " + className + ": Used but not requested parameter: " + parameter, MSG_ERR);
			fail = true;
		}

		for (String parameter : announcedOutputs.keySet()) {
			Type providedClass = providedOutputs.get(parameter);
			Type announcedClass = announcedOutputs.get(parameter);

			if (announcedClass == DIFFERENT_TYPES_DETECTED_TYPE) {
				log("error: " + className + ": Return value announced with multiple types: " + parameter, MSG_ERR);
				fail = true;
			} else if (providedClass == null) {
				log("warning: " + className + ": Return value gets never set: " + parameter, MSG_WARN);
			} else if (providedClass == DIFFERENT_TYPES_DETECTED_TYPE) {
				log("error: " + className + ": Return value provided with multiple types: " + parameter, MSG_ERR);
				fail = true;
			} else if (!providedClass.equals(announcedClass)) {
				log("error: " + className + ": Return value gets set with other type as announced: " + parameter, MSG_ERR);
				fail = true;
			}
		}

		Set<String> unknownOutputs = new HashSet<>(providedOutputs.keySet());
		unknownOutputs.removeAll(announcedOutputs.keySet());
		for (String parameter : unknownOutputs) {
			log("error: " + className + ": Unannounced return value: " + parameter, MSG_ERR);
			fail = true;
		}

		if (fail) {
			throw new BuildException("Module parameter or return value use is incorrect; see above messages.", new Location(className));
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
