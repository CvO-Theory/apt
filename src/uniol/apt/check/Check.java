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

import uniol.apt.adt.pn.PetriNet;

/**
 * Check main class
 *
 * @author Daniel
 *
 */
public class Check {

	private PetriNet pn;
	private int counter;

	private CheckGenerator generator;
	private CheckAttributes attributes;
	private CheckParameters parameters;

	/**
	 * Class constructor
	 */
	public Check() {
		generator = new CheckGenerator();
		attributes = new CheckAttributes();
		parameters = new CheckParameters();

		pn = null;
	}

	/**
	 * Add an attribute
	 *
	 * @param attribute attribute
	 * @throws UnsupportedAttributeException unsupported attribute
	 */
	public void addAttribute(String attribute) throws UnsupportedAttributeException {
		this.attributes.addAttribute(attribute);
	}

	/**
	 * Set which generator will be used
	 *
	 * @param name name of generator
	 * @throws UnsupportedGeneratorException unsupported generator
	 */
	public void setGenerator(String name) throws UnsupportedGeneratorException {
		generator.setGenerator(name);
		generator.setInitialParameters(parameters);
	}

	/**
	 * Search for a net which fulfills all attributes
	 *
	 * @param maxSeconds stop after maxSeconds or if petri net was found
	 *
	 * @return net which fulfills all attributes, null: no net was found
	 *
	 * @throws AttributeFormatException Attribute format exception
	 * @throws UnsupportedAttributeException Unsupported attribute exception
	 */
	public PetriNet search(int maxSeconds) throws AttributeFormatException, UnsupportedAttributeException {
		boolean found = false;
		long startTimestamp, timestamp;

		startTimestamp = System.currentTimeMillis();
		counter = 0;

		while (!found) {

			pn = generator.getPNs();

			// stop after maxSecondS
			timestamp = System.currentTimeMillis();
			if ((timestamp - startTimestamp) > (maxSeconds * 1000)) {
				return null;
			}

			if (pn != null) {
				attributes.setPetriNet(pn);
				attributes.check();
			}

			// modify for next run
			parameters.modify(attributes.getScore(), attributes.getMarkingFactor());

			found = attributes.isFinished();

			counter++;
		}

		return pn;
	}

	/**
	 *
	 * @return get net
	 */
	public PetriNet getPn() {
		return pn;
	}

	/**
	 * Set net
	 *
	 * @param pn net
	 */
	public void setPn(PetriNet pn) {
		this.pn = pn;
	}

	/**
	 * Get best match
	 * Useful if no complete match was found
	 *
	 * @return best founded match
	 */
	public String getBestMatch() {
		return (attributes.getBestMatch());
	}

	/**
	 * Get counter
	 *
	 * @return counter value
	 */
	public int getCounter() {
		return counter;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
