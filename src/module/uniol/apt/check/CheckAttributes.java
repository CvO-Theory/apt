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

import java.util.HashSet;
import java.util.Set;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import uniol.apt.analysis.bounded.Bounded;
import uniol.apt.analysis.exception.PreconditionFailedException;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.analysis.fc.FreeChoice;
import uniol.apt.analysis.isolated.Isolated;
import uniol.apt.analysis.live.Live;
import uniol.apt.analysis.persistent.PersistentNet;
import uniol.apt.analysis.plain.Plain;
import uniol.apt.analysis.reversible.ReversibleNet;
import uniol.apt.analysis.separation.LargestK;
import uniol.apt.analysis.separation.SeparationLogic;
import uniol.apt.analysis.sideconditions.Pure;
import uniol.apt.analysis.snet.SNet;
import uniol.apt.analysis.tnet.TNet;

/**
 * This class manages the attributes for check
 *
 * @author Daniel
 *
 */
public class CheckAttributes {

	private PetriNet petriNet;
	private Set<String> attributes;
	private int score;
	private int markingFactor;
	private boolean finished;

	private int bestScore;
	private String bestMatch;

	/**
	 * Class constructor
	 */
	CheckAttributes() {
		this.attributes = new HashSet<>();

		markingFactor = 1;
		bestScore = 0;
		bestMatch = null;
	}


	/**
	 * Set net
	 *
	 * @param petriNet net
	 */
	public void setPetriNet(PetriNet petriNet) {
		this.petriNet = petriNet;
	}

	/**
	 * Check actual net
	 * @throws AttributeFormatException Attribute format exception
	 * @throws UnsupportedAttributeException Unsupported attribute exception
	 */
	public void check() throws AttributeFormatException,
		UnsupportedAttributeException {

		StringBuilder match = new StringBuilder("\n");
		int tempScore = 0;

		this.score = 0;
		this.markingFactor = 1;

		for (String attribute : this.attributes) {

			if (attribute.equals("snet")) {
				if (checkSnet()) {
					this.score++;
				}
			} else if (attribute.equals("!snet")) {
				if (!checkSnet()) {
					this.score++;
				}
			} else if (attribute.equals("tnet")) {
				if (checkTnet()) {
					this.score++;
				}
			} else if (attribute.equals("!tnet")) {
				if (!checkTnet()) {
					this.score++;
				}
			} else if (attribute.equals("freeChoice")) {
				if (checkFreeChoice()) {
					this.score++;
				}
			} else if (attribute.equals("!freeChoice")) {
				if (!checkFreeChoice()) {
					this.score++;
				}
			} else if (attribute.equals("pure")) {
				if (checkPure()) {
					this.score++;
				}
			} else if (attribute.equals("!pure")) {
				if (!checkPure()) {
					this.score++;
				}
			} else if (attribute.equals("isolated")) {
				if (checkIsolated()) {
					this.score++;
				}
			} else if (attribute.equals("!isolated")) {
				if (!checkIsolated()) {
					this.score++;
				}
			} else if (attribute.equals("plain")) {
				if (checkPlain()) {
					this.score++;
				}
			} else if (attribute.equals("!plain")) {
				if (!checkPlain()) {
					this.score++;
				}
			} else if (attribute.equals("bounded")) {
				if (checkBounded()) {
					this.score++;
				}
			} else if (attribute.equals("!bounded")) {
				if (!checkBounded()) {
					this.score++;
				}
			} else if (attribute.equals("stronglyLive")) {
				if (checkStronglyLive()) {
					this.score++;
				}
			} else if (attribute.equals("!stronglyLive")) {
				if (!checkStronglyLive()) {
					this.score++;
				}
			} else if (attribute.equals("reversible")) {
				if (checkReversible()) {
					this.score++;
				}
			} else if (attribute.equals("!reversible")) {
				if (!checkReversible()) {
					this.score++;
				}
			} else if (attribute.equals("persistent")) {
				if (checkPersistent()) {
					this.score++;
				}
			} else if (attribute.equals("!persistent")) {
				if (!checkPersistent()) {
					this.score++;
				}
			} else if (attribute.endsWith("-marking")) {
				int numberStartIndex = 0;
				if (attribute.charAt(0) == '!') {
					numberStartIndex++;
				}
				int numberEndIndex = attribute.indexOf('-');

				try {
					int k = Integer.parseInt(attribute.substring(numberStartIndex, numberEndIndex));

					if (attribute.charAt(0) == '!') {
						if (!checkKMarking(k)) {
							this.score++;
						}
					} else {
						if (markingFactor % k != 0) {
							markingFactor *= k;
						}

						if (checkKMarking(k)) {
							this.score++;
						}
					}

				} catch (NumberFormatException e) {
					throw new AttributeFormatException(attribute);
				}
			} else if (attribute.endsWith("-separable")) {
				boolean stronglyCheck;

				if (attribute.startsWith("!strongly_")) {
					stronglyCheck = true;
				} else if (attribute.startsWith("!weakly_")) {
					stronglyCheck = false;
				} else {
					throw new AttributeFormatException(attribute);
				}

				int numberStartIndex = attribute.indexOf('_') + 1;
				int numberEndIndex = attribute.indexOf('-');

				try {
					int k = Integer.parseInt(attribute.substring(numberStartIndex, numberEndIndex));

					if (markingFactor % k != 0) {
						markingFactor *= k;
					}

					// Opt: set separation test length
					if (checkNotkSeparable(stronglyCheck, k, 5)) {
						this.score++;
					}

				} catch (NumberFormatException e) {
					throw new AttributeFormatException(attribute);
				}
			} else {
				throw new UnsupportedAttributeException(attribute);
			}

			if (this.score > tempScore) {
				match.append(attribute + " ");
				tempScore = this.score;
			}
		}

		if (this.score == this.attributes.size()) {
			this.finished = true;
		} else {
			this.finished = false;
		}

		if (this.score > this.bestScore) {
			bestMatch = match.toString();
			this.bestScore = this.score;
		}
	}

	/**
	 * Does petri-net fulfilled attribute: snet?
	 *
	 * @return bool
	 */
	private boolean checkSnet() {
		SNet sNet = new SNet(this.petriNet);
		try {
			return sNet.testPlainSNet();
		} catch (PreconditionFailedException e) {
			return false;
		}
	}

	/**
	 * Does petri-net fulfilled attribute: tnet?
	 *
	 * @return bool
	 */
	private boolean checkTnet() {
		TNet tNet = new TNet(this.petriNet);
		try {
			return tNet.testPlainTNet();
		} catch (PreconditionFailedException e) {
			return false;

		}
	}

	/**
	 * Does petri-net fulfilled attribute: free choice?
	 *
	 * @return bool
	 */
	private boolean checkFreeChoice() {
		try {
			FreeChoice fc = new FreeChoice();
			return fc.check(this.petriNet);
		} catch (PreconditionFailedException ex) {
			return false;
		}
	}

	/**
	 * Does petri-net fulfilled attribute: pure?
	 *
	 * @return bool
	 */
	private boolean checkPure() {
		return Pure.checkPure(this.petriNet);
	}

	/**
	 * Does petri-net fulfilled attribute: isolated?
	 *
	 * @return bool
	 */
	private boolean checkIsolated() {
		return Isolated.checkIsolated(this.petriNet);
	}

	/**
	 * Does petri-net fulfilled attribute: plain?
	 *
	 * @return bool
	 */
	private boolean checkPlain() {
		Plain plain = new Plain();
		return plain.checkPlain(this.petriNet);
	}

	/**
	 * Does petri-net fulfilled attribute: bounded?
	 *
	 * @return bool
	 */
	private boolean checkBounded() {
		return Bounded.isBounded(this.petriNet);
	}

	/**
	 * Does petri-net fulfilled attribute: strongly live?
	 *
	 * Note: whole net
	 *
	 * @return bool
	 */
	private boolean checkStronglyLive() {
		boolean boolLive = true;
		for (Transition t : this.petriNet.getTransitions()) {
			try {
				boolLive &= Live.checkStronglyLive(this.petriNet, t);
			} catch (UnboundedException e) {
				return false;
			}
		}

		return boolLive;
	}

	/**
	 * Does petri-net fulfilled attribute: reversible?
	 *
	 * @return bool
	 */
	private boolean checkReversible() {
		ReversibleNet reversible = new ReversibleNet(this.petriNet);
		try {
			reversible.check();
		} catch (UnboundedException e) {
			return false;
		}
		return reversible.isReversible();
	}

	/**
	 * Does petri-net fulfilled attribute: persitent?
	 *
	 * @return bool
	 */
	private boolean checkPersistent() {
		PersistentNet persistent = new PersistentNet(this.petriNet);
		try {
			persistent.check();
		} catch (UnboundedException e) {
			return false;
		}
		return persistent.isPersistent();
	}

	/**
	 * Does petri-net fulfilled attribute: k-marking?
	 *
	 * @param k k
	 * @return bool
	 */
	private boolean checkKMarking(long k) {
		LargestK largestK = new LargestK(this.petriNet);
		long maxK = largestK.computeLargestK();

		if ((maxK % k) == 0) {
			return true;
		}
		return false;
	}

	/**
	 * Does petri-net fulfilled attribute: NOT k-separable?
	 *
	 * @param stronglyCheck stronglyCheck
	 * @param k k
	 * @param maxLength TODO: Figure out what this parameter does
	 * @return bool
	 *              true: is not separable
	 *              false: is separable OR unbounded OR k not ok OR do not know
	 */
	private boolean checkNotkSeparable(boolean stronglyCheck, long k, int maxLength) {
		try {
			LargestK largestK = new LargestK(this.petriNet);
			long maxK = largestK.computeLargestK();

			// is k "ok"?
			if ((maxK % k) != 0) {
				return false;
			}

			// direct call only allowed if k is "ok"
			SeparationLogic separationLogic = new SeparationLogic(
					this.petriNet, stronglyCheck, k, null, maxLength, false);

			return (!separationLogic.getResult().isSeparable());
		} catch (UnboundedException e) {
			return false;
		}
	}

	/**
	 * Get attributes
	 *
	 * @return attributes
	 */
	public Set<String> getAttributes() {
		return attributes;
	}

	/**
	 * Add attribute
	 *
	 * @param attribute attribute
	 */
	public void addAttribute(String attribute) {
		this.attributes.add(attribute);
	}

	/**
	 * Get actual score
	 *
	 * @return actual score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * search finished?
	 *
	 * @return true: actual net fulfills all attributes
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * Get marking factor
	 *
	 * @return marking factor
	 */
	public int getMarkingFactor() {
		return markingFactor;
	}

	/**
	 * set marking factor
	 *
	 * Note: normally not used, will be calculated by check
	 *
	 * @param markingFactor marking factor
	 */
	public void setMarkingFactor(int markingFactor) {
		this.markingFactor = markingFactor;
	}

	/**
	 * Get best match
	 * Useful if no complete match was found
	 *
	 * @return best founded match
	 */
	public String getBestMatch() {
		return bestMatch;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
