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

import java.util.List;

import uniol.apt.module.impl.ExitStatus;
import uniol.apt.module.impl.ModuleUtils;
import uniol.apt.module.impl.ReturnValue;

/**
 * @author Renke Grunwald
 *
 */
public class PropertyModuleExitStatusChecker implements ModuleExitStatusChecker {

	@Override
	public ExitStatus check(Module module, List<Object> values) {
		List<ReturnValue> returnValues = ModuleUtils.getReturnValues(module);

		for (int i = 0; i < returnValues.size(); i++) {
			if (isSuccessProperty(returnValues.get(i))) {
				Boolean successValue = (Boolean) values.get(i);
				if (successValue != null && !successValue) return ExitStatus.FAILURE;
			}
		}

		return ExitStatus.SUCCESS;
	}

	/**
	 *
	 * @param returnValue The return value to check
	 * @return true if this return value indicates success
	 */
	public boolean isSuccessProperty(ReturnValue returnValue) {
		if (!returnValue.getKlass().equals(Boolean.class)) {
			return false;
		}

		if (!returnValue.hasProperty(ModuleOutputSpec.PROPERTY_SUCCESS)) {
			return false;
		}

		return true;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
