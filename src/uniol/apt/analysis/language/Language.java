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

package uniol.apt.analysis.language;

import static java.util.Collections.unmodifiableList;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;

public class Language {

	private final TransitionSystem lts;

	/**
	 * Constructor.
	 * @param pn The Petri net whose prefix language should be analyzed.
	 * @throws UnboundedException 
	 */
	public Language(PetriNet pn) throws UnboundedException {		
		this.lts = new CoverabilityGraph(pn).toReachabilityLTS();
		// in addition to boundedness we should also check for cycles
		// if the reachability graph contains cycles, then we cannot compute
		// its prefix language using this method!
	}

	Set<Word> language() {
		return language(lts.getInitialState(), new HashSet<Word>(), new LinkedList<String>());
	}
	
	private Set<Word> language(State s, Set<Word> words, List<String> w) {
		if(s.getPostsetEdges().size() == 0) {
			words.add(new Word(unmodifiableList(w)));
			//w.clear();
		}
		
		for(Arc e : s.getPostsetEdges()) {
			LinkedList<String> w2 = new LinkedList<String>(w);
			w2.add(e.getLabel());
			language(e.getTarget(), words, w2);
		}
		
		return words;
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
