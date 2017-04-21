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

import static uniol.apt.tasks.modules.ModuleParameterVerifyMethodVisitor.DIFFERENT_TYPES_DETECTED_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import uniol.apt.tasks.modules.ModuleParameterVerifyClassVisitor;

/**
 * Ant task to verify if the modules parameter use is correct.
 * @author vsp
 */
public class ModuleParameterVerifyTask {
	private ModuleParameterVerifyTask() {
	}

	/**
	 * Program entry point. Arguments are a list of base directories and wildcards.
	 * @param args Program arguments.
	 */
	public static void main(String[] args) {
		if (args.length % 2 != 0)
			throw new IllegalArgumentException(
					"Need base dir and wildcard pairs as arguments");

		try {
			Map<String, ModuleParameterVerifyClassVisitor> classes = new HashMap<>();
			for (int i = 0; i < args.length; i += 2) {
				String baseDir = args[i];
				File baseFile = new File(baseDir);
				String wildcard = args[i + 1];

				Iterator<File> fileIter = FileUtils.iterateFiles(baseFile,
							new WildcardFileFilter(wildcard),
							TrueFileFilter.INSTANCE);
				while (fileIter.hasNext()) {
					File classFile = fileIter.next();
					ClassReader reader;
					try {
						reader = new ClassReader(FileUtils.openInputStream(classFile));
					} catch (IOException ex) {
						throw new FailureException("Error accessing file: " + classFile, ex);
					}
					ModuleParameterVerifyClassVisitor cv = new ModuleParameterVerifyClassVisitor();
					reader.accept(cv, 0);

					cv = classes.put(cv.getClassName(), cv);
					if (cv != null)
						throw new FailureException("Multiple definitions for class: " +
								cv.getClassName());
				}
			}

			boolean fail = false;
			for (ModuleParameterVerifyClassVisitor cv : classes.values()) {
				fail |= analyseClass(cv, classes);
			}
			if (fail) {
				throw new FailureException("Module parameter or return value use is incorrect; see above messages.");
			}
		} catch (FailureException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	/** Get class information */
	private static void getClassInformation(ModuleParameterVerifyClassVisitor cv,
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
	private static boolean analyseClass(ModuleParameterVerifyClassVisitor cv, Map<String, ModuleParameterVerifyClassVisitor> classes) {
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
			System.err.println("error: " + className + ": Parameter specified as required and also as optional: " + parameter);
			fail = true;
		}

		for (Map.Entry<String, Type> entry : requestedInputs.entrySet()) {
			String parameter    = entry.getKey();
			Type requestedClass = entry.getValue();
			Type usedClass      = usedInputs.get(parameter);

			if (requestedClass == DIFFERENT_TYPES_DETECTED_TYPE) {
				System.err.println("error: " + className + ": Parameter required with multiple types: " + parameter);
				fail = true;
			} else if (usedClass == null) {
				System.err.println("warning: " + className + ": Never used parameter: " + parameter);
			} else if (usedClass == DIFFERENT_TYPES_DETECTED_TYPE) {
				System.err.println("error: " + className + ": Parameter used with multiple types: " + parameter);
				fail = true;
			} else if (!usedClass.equals(requestedClass)) {
				System.err.println("error: " + className + ": Parameter used with other type as requested: " + parameter);
				fail = true;
			}
		}

		for (Map.Entry<String, Type> entry : optionalInputs.entrySet()) {
			String parameter    = entry.getKey();
			Type usedClass      = usedInputs.get(parameter);
			Type requestedClass = entry.getValue();

			if (requestedClass == DIFFERENT_TYPES_DETECTED_TYPE) {
				System.err.println("error: " + className + ": Parameter required with multiple types: " + parameter);
				fail = true;
			} else if (usedClass == null) {
				System.err.println("warning: " + className + ": Never used parameter: " + parameter);
			} else if (usedClass == DIFFERENT_TYPES_DETECTED_TYPE) {
				System.err.println("error: " + className + ": Parameter used with multiple types: " + parameter);
				fail = true;
			} else if (!usedClass.equals(requestedClass)) {
				System.err.println("error: " + className + ": Parameter used with other type as requested: " + parameter);
				fail = true;
			}
		}

		Set<String> unknownInputs = new HashSet<>(usedInputs.keySet());
		unknownInputs.removeAll(requestedInputs.keySet());
		unknownInputs.removeAll(optionalInputs.keySet());
		for (String parameter : unknownInputs) {
			System.err.println("error: " + className + ": Used but not requested parameter: " + parameter);
			fail = true;
		}

		for (Map.Entry<String, Type> entry : announcedOutputs.entrySet()) {
			String parameter = entry.getKey();
			Type providedClass = providedOutputs.get(parameter);
			Type announcedClass = entry.getValue();

			if (announcedClass == DIFFERENT_TYPES_DETECTED_TYPE) {
				System.err.println("error: " + className + ": Return value announced with multiple types: " + parameter);
				fail = true;
			} else if (providedClass == null) {
				System.err.println("warning: " + className + ": Return value gets never set: " + parameter);
			} else if (providedClass == DIFFERENT_TYPES_DETECTED_TYPE) {
				System.err.println("error: " + className + ": Return value provided with multiple types: " + parameter);
				fail = true;
			} else if (!providedClass.equals(announcedClass)) {
				System.err.println("error: " + className + ": Return value gets set with other type as announced: " + parameter);
				fail = true;
			}
		}

		Set<String> unknownOutputs = new HashSet<>(providedOutputs.keySet());
		unknownOutputs.removeAll(announcedOutputs.keySet());
		for (String parameter : unknownOutputs) {
			System.err.println("error: " + className + ": Unannounced return value: " + parameter);
			fail = true;
		}

		return fail;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
