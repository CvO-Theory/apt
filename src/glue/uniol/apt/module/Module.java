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

package uniol.apt.module;

import uniol.apt.module.exception.ModuleException;

/**
 * A module.
 *
 * @author Renke Grunwald
 */
public interface Module {

	/**
	 * Returns the name of the module. The name is used to uniquely identify a
	 * module.
	 *
	 * It should be all lower-case with words separated by underscores.
	 *
	 * It should also be kept simple as it may be typed by a user in order for
	 * him to use this module. For example: "example_module".
	 *
	 * @return the name of the module
	 */
	public String getName();

	/**
	 * Specify the parameters of the module. To specify a parameter the
	 * {@link ModuleInputSpec#addParameter(String, Class, String, String...)} method can
	 * be used. The order of specified parameters matters.
	 *
	 * @param inputSpec
	 *            specification of parameters (input values)
	 */
	public void require(ModuleInputSpec inputSpec);

	/**
	 * Specify the return values of the module. To specify a return value the
	 * {@link ModuleOutputSpec#addReturnValue(String, Class, String...)} method
	 * can be used. The order of specified return values matters.
	 *
	 * @param outputSpec
	 *            specification of return values (output values)
	 */
	public void provide(ModuleOutputSpec outputSpec);

	/**
	 * Perform some computation on the values stored in the {@link ModuleInput}
	 * object and store the results in the {@link ModuleOutput} object.
	 *
	 * To retrieve values from {@link ModuleInput} object the
	 * {@link ModuleInput#getParameter(String, Class)} method can be used.
	 *
	 * To store values in the {@link ModuleOutput} object the
	 * {@link ModuleOutput#setReturnValue(String, Class, Object)} method can be
	 * used.
	 *
	 * The possible values in both the {@link ModuleInput} and
	 * {@link ModuleOutput} object are determined entirely by the specifications
	 * in the {@link #require(ModuleInputSpec)} and
	 * {@link #provide(ModuleOutputSpec)} method respectively.
	 *
	 * @param input
	 *            storage of the inputs
	 * @param output
	 *            storage of the outputs
	 * @throws ModuleException On various kinds of errors that prevent the module from working.
	 */
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException;

	/**
	 * Returns the title of the module.
	 *
	 * It should be shorter than 40 characters and must not contain control characters like newlines.
	 *
	 * The default implementation in {@link AbstractModule} returns {@link #getName()}.
	 *
	 * @return the title of the module
	 */
	public String getTitle();

	/**
	 * Returns a short description of the module. The short description should explain what the module does without
	 * going into details.
	 *
	 * It should be shorter than 70 characters and must not contain control characters like newlines.
	 *
	 * The default implementation in {@link AbstractModule} returns an empty string.
	 *
	 * @return the short description of the module
	 */
	public String getShortDescription();

	/**
	 * Returns a long description of the module.  The long description should contain a detailed explanation of what
	 * the module does. It might include example invocations, for instance.
	 *
	 * This desription has no length limit and may contain newlines.
	 *
	 * The default implementation in {@link AbstractModule} returns {@link #getShortDescription()}.
	 *
	 * @return the long description of the module
	 */
	public String getLongDescription();

	/**
	 * Returns the categories the module belongs to. The categories are used to
	 * improve the module overview making particular modules faster to find. If
	 * null is returned the module is automatically assigned to the default
	 * category.
	 *
	 * @return an array of categories
	 */
	public Category[] getCategories();
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
