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

package uniol.apt.ui;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation marking {@link ParameterTransformation} implementations.
 *
 * @author Uli Schlachter
 * @author Jonas Prellberg
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AptParameterTransformation {

	/**
	 * @return The type of parameters that is supported.
	 */
	Class<?>[] value();

	/**
	 * Marks {@link ParameterTransformation} implementations that transform
	 * parameter values typically saved in files. This value will be
	 * detected by calling code to decide if the parameter value is
	 * interpreted as a file path and needs to be read first before passing
	 * the value string to the transformer.
	 *
	 * @return true if this parameter usually is read from a file
	 */
	boolean fileSource() default false;

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
