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

package uniol.apt.analysis.separation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uniol.apt.util.interrupt.InterrupterRegistry;

/**
 * This class provides functionality with firable sequences which are needed by separation.
 *
 * Note:
 *        This class controls k different nets. So if a transition should be fired
 *        the class checks which net is in the position to execute the transition.
 *        If there is more than one net which can fire this class will save two or
 *        more ways -- one way contains the situation if first possible net was
 *        fired, the next way the next possible net... When the next transition
 *        should be fired all saved ways have to be checked and maybe there will
 *        come more ways.
 *        This class avoids "double" ways -- see example for hashmap.
 *
 * @author Daniel
 */
public class FirableSequences {
	private long k = 0;

	/** will be used to mark used transitions when finding weakly separation */
	private String magicWord = "u_s_e_d";

	/** possible situation of variables by checking strongly:
	 * (weakly works with same principle but uses "used" as mark (order can be switched)
	 * Hashmap:
	 * {0=[[ta, tt, ta], [ta, ta, tt]],
	 * 1=[[tt, ta], [ta, tt]],
	 * 2=[[tt]],
	 * 3=[[]],
	 * 4=[[ta]],
	 * At start ta, tt, ta or ta, ta, tt is possible. For both nets (if k=2) so Array is [0,0]
	 * ta should be fired
	 * than Array will be [1,0] and [0,1] - hashmap 1 contains the situation for ta was fired.
	 * we have two ways. First net or second net fired but we avoid doubles so we just have [1,0]
	 * ta should be fired again
	 * Array will be [2,0] and [1,1]
	 * tt should be fired
	 * Array will be [3,0] and [4,1] and [1,4] - avoid doubles -&gt; [3,0] and [4,1] */
	private HashMap<Integer, ArrayList<ArrayList<String>>> firingSequencesMap = new HashMap<>();

	private int hashMapCounter = 0;

	/** ArrayList for possible ways, ArrayList for k, Integer for FiringSequences (via HashMap)
	 * possible ways are 1 at beginning but will be needed if more than one firingSequence match to wanted fire */
	private ArrayList<ArrayList<Integer>> possibleWaysFiringSequencesArray = new ArrayList<ArrayList<Integer>>(0);

	/** store which of the k-nets are fired to go to position in possibleWaysFiringSequencesArray */
	private ArrayList<String>   possibleWaysSmallNetsOrderArray = new ArrayList<>(0);

	/**
	 *
	 * Constructor.
	 *
	 * @param initFirableSequences firable sequences which we can use k times
	 * @param initK k
	 */
	public FirableSequences(ArrayList<ArrayList<String>> initFirableSequences, long initK) {
		this.k = initK;

		// first possible way
		ArrayList<Integer> possibleFiringSequences = new ArrayList<Integer>(0);

		// initialize hashmap with values of initial situation
		ArrayList<ArrayList<String>> arrayListElement = new ArrayList<>(initFirableSequences);

		firingSequencesMap.put(hashMapCounter, arrayListElement);

		for (int i = 0; i < k; i++) {
			possibleFiringSequences.add(hashMapCounter);
		}

		hashMapCounter++;
		possibleWaysFiringSequencesArray.add(possibleFiringSequences);
	}

	/**
	 * This function return one possible order how separation could be done
	 *
	 * e.g.: Net1, Net1, Net2, Net1...
	 *
	 * @return Order how separation could be done
	 */
	public ArrayList<String> getPossibleWaysSmallNetsOrderArray() {
		return possibleWaysSmallNetsOrderArray;
	}

	/**
	 *
	 * Checks if a single firing sequence is valid for weakly check.
	 *
	 * Note:
	 *        Order in weakly check does not matter so we allow using transition
	 *        from "future" which can be in "preset" after firing other
	 *        transitions. Here we check if in this sequence are transitions
	 *        of "future" are used.
	 *        [t1, used, t2]   - t1 not used, used is in future
	 *                (after t1 fired)
	 *        [used, used, t2] - t2 is after used, everything ok.
	 *
	 * @param firingSequence Firing sequence to check
	 * @return Is a valid weakly firing sequence?
	 */
	public boolean isFiringSequenceValid(ArrayList<String> firingSequence) {
		boolean usedAtBeginning = true;

		for (String singleTransition : firingSequence) {
			if (singleTransition != this.magicWord) {
				usedAtBeginning = false;
			}
			if (!usedAtBeginning && singleTransition.equals(this.magicWord)) {
				return (false);
			}
		}
		return (true);
	}

	/**
	 * Checks a whole situation if there is a valid situation.
	 *
	 * Note:
	 *        There can more than one way to get with a firing sequence to a state
	 *                one must be valid (or)
	 *        All k states have to be valid (and)
	 *        If there is more than one state -- one must be valid (or)
	 *
	 * @return Valid firing sequence found?
	 */
	public boolean isThereAValidFiringSequence() {
		boolean isThereAValieFiringSequence = false;
		for (ArrayList<Integer> firingSequenceWay : this.possibleWaysFiringSequencesArray) {
			InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

			boolean thisFiringSequenceWayOk = true;
			for (Integer firingSequencesKey : firingSequenceWay) {
				boolean thisSequencesOk = false;
				for (ArrayList<String> firingSequence
						: this.firingSequencesMap.get(firingSequencesKey)) {
					thisSequencesOk |= isFiringSequenceValid(firingSequence);
				}
				thisFiringSequenceWayOk &= thisSequencesOk;
			}
			isThereAValieFiringSequence |= thisFiringSequenceWayOk;
		}
		return (isThereAValieFiringSequence);
	}

	/**
	 * This is the "main" method of this class. Just tell this function which
	 * transition should be fired and if you check strongly or weakly separable.
	 *
	 * @param wantedFire Name of the transition which should be fired
	 * @param checkStrongly Strongly or weakly check?
	 * @return true: transition can be fired
	 *         false: transition can not be fired.
	 */
	public boolean fire(String wantedFire, boolean checkStrongly) {
		ArrayList<ArrayList<Integer>> possibleWaysFiringSequencesArrayNext =
				new ArrayList<ArrayList<Integer>>(0);
		ArrayList<String> possibleWaysSmallNetsOrderArrayNext = new ArrayList<>(0);
		Set<Integer> alreadyChecked = new HashSet<>();

		int numberOfWays = this.possibleWaysFiringSequencesArray.size();

		for (int way = 0; way < numberOfWays; way++) {
			alreadyChecked.clear();

			for (int kTimes = 0; kTimes < k; kTimes++) {
				InterrupterRegistry.throwIfInterruptRequestedForCurrentThread();

				int hashKeyToCheck = possibleWaysFiringSequencesArray.get(way).get(kTimes);

				ArrayList<Integer> possibleFiringSequences = new ArrayList<Integer>(0);
				if (!alreadyChecked.contains(hashKeyToCheck)) {
					alreadyChecked.add(hashKeyToCheck);

					boolean isThisSequencePossible = checkIfWantedFireIsPossible(
							firingSequencesMap.get(hashKeyToCheck), wantedFire,
							checkStrongly);

					if (isThisSequencePossible) {

						// "fire" this sequence
						ArrayList<ArrayList<String>> nextFiringSequence = fireWanted(
								firingSequencesMap.get(hashKeyToCheck),
								wantedFire, checkStrongly);

						// is this sequence already in hashmap?
						int alreadyInHashMapKey = -1;
						for (Map.Entry<Integer, ArrayList<ArrayList<String>>> entry
								: firingSequencesMap.entrySet()) {
							if (entry.getValue().equals(nextFiringSequence)) {
								alreadyInHashMapKey = entry.getKey();
							}
						}

						int hashMapKey;
						boolean hashMapKeyWritten = false;
						if (alreadyInHashMapKey != -1) {
							hashMapKey = alreadyInHashMapKey;
						} else {
							firingSequencesMap.put(hashMapCounter, nextFiringSequence);
							hashMapKey = hashMapCounter;
							hashMapCounter++;
						}

						// copy all other
						for (int copyK = 0; copyK < k; copyK++) {
							if (copyK != kTimes) {
								int value = possibleWaysFiringSequencesArray
										.get(way).get(copyK);

								// put at "right" position, so data will be sorted.
								if (!hashMapKeyWritten && (hashMapKey > value)) {
									possibleFiringSequences.add(hashMapKey);
									hashMapKeyWritten = true;
								}

								possibleFiringSequences.add(value);
							}
						}
						// was not written yet, write to last position
						if (!hashMapKeyWritten) {
							possibleFiringSequences.add(hashMapKey);
						}


						// is this a new possible state for all data?
						boolean newData = true;
						for (ArrayList<Integer> possibleFiringSequencesNext
								: possibleWaysFiringSequencesArrayNext) {
							if (possibleFiringSequencesNext
									.equals(possibleFiringSequences)) {
								newData = false;
							}
						}

						// save the result
						if (newData) {
							String actualOrderPrefix;
							if (possibleWaysSmallNetsOrderArray.isEmpty()) {
								actualOrderPrefix = "Net" + String.valueOf(kTimes);
							} else {
								actualOrderPrefix =
									possibleWaysSmallNetsOrderArray.get(way);
								actualOrderPrefix += ", Net" + String.valueOf(kTimes);
							}
							possibleWaysSmallNetsOrderArrayNext.add(actualOrderPrefix);

							possibleWaysFiringSequencesArrayNext.add(
								possibleFiringSequences);
						}
					}
				}
			}
		}

		this.possibleWaysSmallNetsOrderArray = possibleWaysSmallNetsOrderArrayNext;
		this.possibleWaysFiringSequencesArray  = possibleWaysFiringSequencesArrayNext;

		if (possibleWaysFiringSequencesArray.isEmpty()) {
			return (false);
		} else {
			return (true);
		}
	}

	/**
	 * This method executes a transition.
	 *
	 * @param possibleFiringSequence A situation of a firing sequence
	 * @param wantedFire Name of transition which should be fired
	 * @param checkStrongly Do execution of transition strongly or weakly
	 *
	 * @return Situation of the firing sequence after fire
	 */
	public ArrayList<ArrayList<String>> fireWanted(ArrayList<ArrayList<String>> possibleFiringSequence,
			String wantedFire, boolean checkStrongly) {
		// not possible sequences will not be delete, possible will be copied.
		ArrayList<ArrayList<String>> possibleFiringSequenceAfterWantedFire = new ArrayList<>();

		for (ArrayList<String> firingSequence : possibleFiringSequence) {
			if (checkStrongly) {
				// fire possible at first position?
				if (firingSequence.get(0).equals(wantedFire)) {
					if (firingSequence.size() > 1) {
						// copy - otherwise it will change original data
						// which can be used by other elements.
						ArrayList<String> newFiringSequence =
								new ArrayList<String>(firingSequence);
						// delete first transition -> it was fired
						newFiringSequence.remove(0);
						possibleFiringSequenceAfterWantedFire.add(newFiringSequence);
					}
				}
			} else { // weakly
				// fire possible any time? By weakly order does not matter.
				if (firingSequence.contains(wantedFire)) {
					// copy - otherwise it will change original data
					ArrayList<String> newFiringSequence = new ArrayList<String>(firingSequence);
					// delete matching transition -> it was fired
					int index = firingSequence.indexOf(wantedFire);
					newFiringSequence.set(index, this.magicWord);

					possibleFiringSequenceAfterWantedFire.add(newFiringSequence);
				}
			}
		}
		return (possibleFiringSequenceAfterWantedFire);
	}

	/**
	 * This method check if a transition can be fired if you would like to fire.
	 * But it does not fire it.
	 *
	 * @param possibleFiringSequence A situation of a firing sequence
	 * @param wantedFire Name of transition which should be checked
	 * @param checkStrongly Do execution of transition strongly or weakly
	 *
	 * @return Is fire possible?
	 */
	public static boolean checkIfWantedFireIsPossible(ArrayList<ArrayList<String>> possibleFiringSequence,
			String wantedFire, boolean checkStrongly) {
		for (ArrayList<String> firingSequence : possibleFiringSequence) {
			if (checkStrongly) {
				// fire possible at first position?
				if (firingSequence.get(0).equals(wantedFire)) {
					return (true);
				}
			} else { // weakly
				// fire possible any time? By weakly order does not matter.
				if (firingSequence.contains(wantedFire)) {
					return (true);
				}
			}
		}
		return (false);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
