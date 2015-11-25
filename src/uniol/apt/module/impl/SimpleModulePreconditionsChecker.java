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

package uniol.apt.module.impl;

import java.util.ArrayList;
import java.util.List;

import uniol.apt.module.Module;
import uniol.apt.module.ModulePreconditionsChecker;
import uniol.apt.module.ModuleRegistry;
import uniol.apt.module.exception.ModuleException;

/**
 * @author Renke Grunwald
 *
 */
public class SimpleModulePreconditionsChecker implements ModulePreconditionsChecker {
	@Override
	public List<Parameter> check(ModuleRegistry reg, Module mod, Object... args) {
		List<Parameter> params = ModuleUtils.getParameters(mod);
		List<Parameter> invalidParams = new ArrayList<Parameter>();

		for (Parameter param : params) {
			for (String prop : param.getProperties()) {
				Module checkingMod = findCheckingModule(reg, param, prop);

				if (checkingMod == null) continue;

				ReturnValue matchingRetVal = getMatchingReturnValue(checkingMod, prop);
				boolean valid = checkProperty(mod, checkingMod, param, matchingRetVal, args);

				if (!valid) {
					invalidParams.add(param);
					break;
				}
			}
		}

		return invalidParams;
	}

	public boolean isCheckingModule(Module mod, Parameter checkedParam, String prop) {
		if (getMatchingReturnValue(mod, prop) == null) return false;
		List<Parameter> params = ModuleUtils.getParameters(mod);
		if (params.size() != 1) return false;
		if (!params.get(0).getKlass().equals(checkedParam.getKlass())) return false;
		return true;
	}

	public Module findCheckingModule(ModuleRegistry reg, Parameter checkedParam, String prop) {
		for (Module mod : reg.getModules()) {
			if (isCheckingModule(mod, checkedParam, prop)) {
				return mod;
			}
		}
		return null;
	}

	public ReturnValue getMatchingReturnValue(Module checkingMod, String prop) {
		for (ReturnValue retVal : ModuleUtils.getReturnValues(checkingMod)) {
			if (retVal.getKlass().equals(Boolean.class)) {
				if (retVal.hasProperty(prop)) {
					return retVal;
				}
			}
		}
		return null;
	}

	public boolean checkProperty(Module checkedMod, Module checkingMod, Parameter checkedParam,
			ReturnValue checkingRetVal, Object... args) {
		List<Parameter> params = ModuleUtils.getParameters(checkedMod);
		Object arg = args[params.indexOf(checkedParam)];
		ModuleInvoker invoker = new ModuleInvoker();

		try {
			List<Object> results = invoker.invoke(checkingMod, arg);
			List<ReturnValue> retVals = ModuleUtils.getReturnValues(checkingMod);
			Boolean met = (Boolean) results.get(retVals.indexOf(checkingRetVal));
			return met;
		} catch (ModuleException e) {
			return false;
		}
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
