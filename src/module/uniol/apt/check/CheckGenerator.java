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

package uniol.apt.check;

import java.util.Iterator;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.check.CheckParameters.ParameterModifyType;
import uniol.apt.generator.bitnet.SimpleBitNetGenerator;
import uniol.apt.generator.cycle.CycleGenerator;
import uniol.apt.generator.marking.MarkingIterable;
import uniol.apt.generator.philnet.QuadstatePhilNetGenerator;
import uniol.apt.generator.philnet.TristatePhilNetGenerator;
import uniol.apt.generator.tnet.TNetGenerator;

/**
 * This class manages the generators for check
 *
 * @author Daniel, Chris
 *
 */
public class CheckGenerator {

	private String generator_;
	private CheckParameters parameters_;

	private ChanceGenerator chanceGenerator;
	private SmartChanceGenerator smartChanceGenerator;

	private MarkingIterable markingNetGenerator;
	private Iterator<PetriNet> petriNetIterator;

	/**
	 * Class constructor
	 */
	CheckGenerator() {
		chanceGenerator      = new ChanceGenerator();
		smartChanceGenerator = new SmartChanceGenerator(0);
		markingNetGenerator  = null;
		petriNetIterator     = null;
	}

	/**
	 * Set generator which should be used
	 *
	 * @param name
	 *            generator name
	 */
	public void setGenerator(String name) {
		generator_ = name;
	}

	/**
	 * Generate a net with generator
	 *
	 * @return generated net
	 */
	public PetriNet getPNs() {
		PetriNet returnNet = null;

		if (generator_.equals("cycle")) {
			CycleGenerator generator = new CycleGenerator();
			returnNet = generator.generateNet(parameters_.get(0));
		} else if (generator_.equals("bitnet")) {
			SimpleBitNetGenerator generator = new SimpleBitNetGenerator();
			returnNet = generator.generateNet(parameters_.get(0));
		} else if (generator_.equals("triPhilgen")) {
			TristatePhilNetGenerator generator = new TristatePhilNetGenerator();
			returnNet = generator.generateNet(parameters_.get(0));
		} else if (generator_.equals("quadPhilgen")) {
			QuadstatePhilNetGenerator generator = new QuadstatePhilNetGenerator();
			returnNet = generator.generateNet(parameters_.get(0));
		} else if (generator_.equals("chance")) {
			returnNet = chanceGenerator.generateNet(parameters_.get(0), parameters_.get(1));
		} else if (generator_.equals("smartchance")) {
			returnNet = smartChanceGenerator.generateNet(parameters_.get(0), parameters_.get(1));
		} else if (generator_.equals("tnetgen2")) {
			if (petriNetIterator == null) {
				markingNetGenerator = new MarkingIterable(
						new TNetGenerator(parameters_.get(0)), parameters_.get(1));
				petriNetIterator = markingNetGenerator.iterator();
			}

			if (petriNetIterator.hasNext()) {
				returnNet = petriNetIterator.next();
				parameters_.setActive(false);
			} else {
				petriNetIterator = null;
				parameters_.setActive(true);
			}
		} else if (generator_.equals("tnetgen3")) {
			if (petriNetIterator == null) {
				markingNetGenerator = new MarkingIterable(
						new TNetGenerator(parameters_.get(0), parameters_.get(1)),
						parameters_.get(2));
				petriNetIterator = markingNetGenerator.iterator();
			}

			if (petriNetIterator.hasNext()) {
				returnNet = petriNetIterator.next();
				parameters_.setActive(false);
			} else {
				petriNetIterator = null;
				parameters_.setActive(true);
			}
		}

		return returnNet;
	}

	/**
	 * Get parameters
	 *
	 * @return actual parameter
	 */
	public CheckParameters getParameters() {
		return parameters_;
	}

	/**
	 * Set initial parameters
	 *
	 * @param parameters
	 *            Parameter
	 * @throws UnsupportedGeneratorException
	 *             unsupported generator
	 */
	public void setInitialParameters(CheckParameters parameters) throws UnsupportedGeneratorException {
		parameters_ = parameters;

		if (generator_.equals("cycle")) {
			parameters_.setMinValue(1);
			parameters_.setNumberOfParameters(1);
			parameters_.setModifyMode(ParameterModifyType.BruteForce);
		} else if (generator_.equals("bitnet")) {
			parameters_.setMinValue(1);
			parameters_.setNumberOfParameters(1);
			parameters_.setModifyMode(ParameterModifyType.BruteForce);
		} else if (generator_.equals("triPhilgen")) {
			parameters_.setMinValue(2);
			parameters_.setNumberOfParameters(1);
			parameters_.setModifyMode(ParameterModifyType.BruteForce);
		} else if (generator_.equals("quadPhilgen")) {
			parameters_.setMinValue(2);
			parameters_.setNumberOfParameters(1);
			parameters_.setModifyMode(ParameterModifyType.BruteForce);
		} else if (generator_.equals("chance")) {
			parameters_.setMinValue(-1);
			parameters_.setNumberOfParameters(2);
			parameters_.setModifyMode(ParameterModifyType.Score);
		} else if (generator_.equals("smartchance")) {
			parameters_.setMinValue(-1);
			parameters_.setNumberOfParameters(2);
			parameters_.setModifyMode(ParameterModifyType.Score);
		} else if (generator_.equals("tnetgen2")) {
			parameters_.setMinValue(1);
			parameters_.setNumberOfParameters(2);
			parameters_.setModifyMode(ParameterModifyType.BruteForce);
		} else if (generator_.equals("tnetgen3")) {
			parameters_.setMinValue(1);
			parameters_.setNumberOfParameters(3);
			parameters_.setModifyMode(ParameterModifyType.BruteForce);
		} else {
			throw new UnsupportedGeneratorException(generator_);
		}

	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
