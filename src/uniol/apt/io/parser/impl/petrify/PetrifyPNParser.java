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

package uniol.apt.io.parser.impl.petrify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Set;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.module.exception.NetIsNotParsableException;

/**
 * Parse the petrify string into the APT-data structure.
 * 
 * @author Sören Dierkes
 * 
 */
public class PetrifyPNParser {

	private PetriNet pn_;
	private final int SOURCE = 0;
	private final int TARGET = 1;

	/**
	 * Parse the petrify string into the APT-data structure.
	 * 
	 * @param petrifyFormat
	 *            Petri net as Petrify string.
	 * @throws IOException
	 * @throws NetIsNotParseableException
	 */
	public void parse(String petrifyFormat) throws IOException, NetIsNotParsableException {

		BufferedReader br = new BufferedReader(new StringReader(petrifyFormat));
		
		int places = 0;
		boolean sawTransitions = false;
		pn_ = new PetriNet();

		String line = "";
		while ((line = br.readLine()) != null) {
			if (line.contains("/")) {
				line = line.replace("/", "_");
			}

			if (line.contains("#") || line.contains(".graph") || line.contains(".end")) {
				continue;
			} else if (line.contains(".model")) {
				pn_.setName("" + line.subSequence(".model".length() + 1, line.length()));
			} else if (line.contains(".inputs")) {
				line = line.replace(".inputs", "");
				line = line.replaceAll(" +", " ");
				line = line.trim();
				for (String s : line.split(" ")) {
					if (!s.isEmpty())
						pn_.createTransition(s);
				}
				sawTransitions = true;
			} else if (line.contains(".marking")) {
				line = line.replace(".marking", "");
				line = line.replace("{", "");
				line = line.replace("}", "");
				line = line.replaceAll("<", " ");
				line = line.replaceAll(">", " ");
				line = line.replaceAll(" +", " ");
				line = line.trim();
				String[] pairs = line.split(" ");
				for (String s : pairs) {
					if (s.isEmpty())
						continue;
					if (s.contains("p")) {
						pn_.getPlace(s).setInitialToken(1);
					} else {
						Set<Place> pl = pn_.getTransition(s.split(",")[SOURCE]).getPostset();
						Iterator<Place> itr = pl.iterator();

						boolean rdy = true;
						while (itr.hasNext() && rdy) {
							Place p = itr.next();
							for (Transition t : p.getPostset()) {
								if (t.getId().equals(s.split(",")[TARGET])) {
									p.setInitialToken(1);
									rdy = false;
									break;
								}
							}
						}
					}
				}
			} else if (!line.contains(".") && sawTransitions) {
				String[] tmpTrans = line.split(" ");

				if (tmpTrans.length == 0) {
					throw new NetIsNotParsableException();
				}
				
				if (tmpTrans[SOURCE].contains("_") && !pn_.containsTransition(tmpTrans[SOURCE])) {
					Transition t = pn_.createTransition(tmpTrans[SOURCE]);
					if (tmpTrans[SOURCE].split("_").length == 0) {
						throw new NetIsNotParsableException();
					}
					t.setLabel(tmpTrans[SOURCE].split("_")[0]);
				}

				for (int i = 1; i < tmpTrans.length; i++) {
					
					if (tmpTrans[i].contains("_") && !pn_.containsTransition(tmpTrans[i])) {
						Transition t = pn_.createTransition(tmpTrans[i]);
						if (tmpTrans[i].split("_").length == 0) {
							throw new NetIsNotParsableException();
						}
						t.setLabel(tmpTrans[i].split("_")[0]);
					}

					if ((!transitionExist(tmpTrans[i]) && !tmpTrans[i].contains("p") || !transitionExist(tmpTrans[SOURCE]))
							&& !tmpTrans[SOURCE].contains("p")) {
						throw new NetIsNotParsableException();
					}

					if (tmpTrans[SOURCE].contains("p")) {
						if (!placeExist(tmpTrans[SOURCE])) {
							pn_.createPlace("" + tmpTrans[SOURCE]);
						}
						pn_.createFlow(tmpTrans[SOURCE], tmpTrans[i]);

					} else if (tmpTrans[i].contains("p")) {
						if (!placeExist(tmpTrans[i])) {
							pn_.createPlace("" + tmpTrans[i]);
						}
						pn_.createFlow(tmpTrans[SOURCE], tmpTrans[i]);
					} else {
						Place p = pn_.createPlace("place" + places++);
						pn_.createFlow(tmpTrans[SOURCE], p.getId());
						pn_.createFlow(p.getId(), tmpTrans[i]);
					}
				}

				if (tmpTrans.length == 1) {
					if (tmpTrans[0].contains("p")) {
						pn_.createPlace("" + tmpTrans[0]);
					} else {
						pn_.createPlace("place" + places++);
					}
				}
			} else {
				throw new NetIsNotParsableException();
			}
		}
	}

	private boolean transitionExist(String t) {
		return pn_.containsTransition(t);
	}

	private boolean placeExist(String t) {
		return pn_.containsPlace(t);
	}

	public PetriNet getPN() {
		return pn_;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
