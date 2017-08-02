/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014  Uli Schlachter
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

package uniol.apt.analysis.synthesize.separation;

import static uniol.apt.util.DebugUtil.debug;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import uniol.apt.adt.exception.StructureException;
import uniol.apt.adt.ts.Event;
import uniol.apt.adt.ts.State;
import uniol.apt.analysis.synthesize.MissingLocationException;
import uniol.apt.analysis.synthesize.PNProperties;
import uniol.apt.analysis.synthesize.Region;
import uniol.apt.analysis.synthesize.RegionUtility;
import uniol.apt.analysis.synthesize.UnreachableException;

/**
 * Helper functions for solving separation problems.
 * @author Uli Schlachter
 */
public final class SeparationUtility {
	private SeparationUtility() {
	}

	/**
	 * Test if there exists an outgoing arc labelled with the given event.
	 * @param state The state to examine.
	 * @param event The event that should fire.
	 * @return True if a suitable arc exists, else false.
	 */
	static public boolean isEventEnabled(State state, String event) {
		return !state.getPostsetNodesByLabel(event).isEmpty();
	}

	/**
	 * Check if the given region separates the two given states.
	 * @param region The region to examine.
	 * @param state The first state of the separation problem
	 * @param otherState The second state of the separation problem
	 * @return A separating region or null.
	 */
	static public boolean isSeparatingRegion(Region region, State state, State otherState) {
		try {
			// We need a region which assigns different values to these two states.
			BigInteger stateValue = region.getMarkingForState(state);
			BigInteger otherStateValue = region.getMarkingForState(otherState);
			return !stateValue.equals(otherStateValue);
		} catch (UnreachableException e) {
			return false;
		}
	}

	/**
	 * Check if the given region separates the state from the event.
	 * @param region The region to examine.
	 * @param state The state of the separation problem
	 * @param event The event of the separation problem
	 * @return A separating region or null.
	 */
	static public boolean isSeparatingRegion(Region region, State state, String event) {
		try {
			// We need r(state) to be smaller than the event's backward weight in some region.
			return region.getMarkingForState(state).compareTo(region.getBackwardWeight(event)) < 0;
		} catch (UnreachableException e) {
			return false;
		}
	}

	/**
	 * Calculate a mapping from events to their location. Note that this also handles output-nonbranching and will
	 * unset output-nonbranching in properties, if it is specified.
	 * @param utility The region utility that describes the events.
	 * @param properties Properties that may influence the location map.
	 * @return An array containing the location for each event.
	 * @throws MissingLocationException if the transition system for the utility has locations for only some events
	 */
	static public String[] getLocationMap(RegionUtility utility, PNProperties properties)
			throws MissingLocationException {
		// Build a mapping from events to locations. Yaaay. Need to iterate over all arcs...
		String[] locationMap = new String[utility.getNumberOfEvents()];
		boolean hadEventWithLocation = false;

		// If there are no events, there is nothing to do
		if (locationMap.length == 0)
			return locationMap;

		for (Event eventObj : utility.getTransitionSystem().getAlphabetEvents()) {
			String location;
			try {
				location = eventObj.getExtension("location").toString();
			} catch (StructureException e) {
				// Because just returning "null" is too easy...
				continue;
			}

			int event = utility.getEventIndex(eventObj.getLabel());
			String oldLocation = locationMap[event];
			locationMap[event] = location;
			hadEventWithLocation = true;

			// The parser makes sure that this assertion always holds. If something constructs a PN which
			// breaks this assumption, then the bug is in that code.
			assert oldLocation == null || oldLocation.equals(location);
		}

		if (hadEventWithLocation) {
			// Do all events have a location?
			if (Arrays.asList(locationMap).contains(null))
				throw new MissingLocationException("Trying to synthesize a Petri Net where some events "
						+ " have a location and others do not. Either all or no event must"
						+ " have a location.");
		}

		// We used the above as sanity checks, now handle output-nonbranching, if specified
		if (properties.isOutputNonbranching()) {
			for (int i = 0; i < locationMap.length; i++)
				locationMap[i] = String.valueOf(i);
		}

		// Do all events have the same location?
		if (Collections.frequency(Arrays.asList(locationMap), locationMap[0]) == locationMap.length) {
			// No location handling needed, discard the map
			locationMap = new String[locationMap.length];
		}

		return locationMap;
	}

	/**
	 * Construct a new Separation instance.
	 * @param utility The region utility to use.
	 * @param properties Properties that the calculated region should satisfy.
	 * @return A suitable Separation instance
	 * @throws MissingLocationException if the transition system for the utility has locations for only some events
	 */
	static public Separation createSeparationInstance(RegionUtility utility, PNProperties properties)
			throws MissingLocationException {
		String[] locationMap = getLocationMap(utility, properties);
		Separation result = null;

		// getLocationMap() handled ON
		properties = properties.setOutputNonbranching(false);

		// Should a specific implementation of Separation be used?
		String forcedSeparationImplementation = System.getProperty("apt.separationImplementation");
		if (forcedSeparationImplementation != null)
			try {
				// Find the class to use
				Class<?> klass;
				String pkg = SeparationUtility.class.getPackage().getName();
				try {
					klass = Class.forName(pkg + "." + forcedSeparationImplementation);
				} catch (ClassNotFoundException e) {
					klass = Class.forName(forcedSeparationImplementation);
				}
				// Construct an instance
				Constructor<?> constructor = klass.getConstructor(RegionUtility.class,
						PNProperties.class, String[].class);
				return (Separation) constructor.newInstance(utility, properties, locationMap);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Failed to instantiate " + forcedSeparationImplementation +
						" as an implementation of " + Separation.class, e.getTargetException());
			} catch (Exception e) {
				throw new RuntimeException("Failed to instantiate " + forcedSeparationImplementation +
						" as an implementation of " + Separation.class, e);
			}

		try {
			if (result == null)
				result = new ElementarySeparation(utility, properties, locationMap);
		} catch (UnsupportedPNPropertiesException e) {
			// Ignore, try the other implementations
		}
		try {
			if (result == null)
				result = new KBoundedSeparation(utility, properties, locationMap);
		} catch (UnsupportedPNPropertiesException e) {
			// Ignore, try the other implementations
		}
		try {
			if (result == null)
				result = new MarkedGraphSeparation(utility, properties, locationMap);
		} catch (UnsupportedPNPropertiesException e) {
			// Ignore, try the other implementations
		}
		try {
			if (result == null)
				result = new BasicPureSeparation(utility, properties, locationMap);
		} catch (UnsupportedPNPropertiesException e) {
			// Ignore, try the other implementations
		}
		try {
			if (result == null)
				result = new BasicImpureSeparation(utility, properties, locationMap);
		} catch (UnsupportedPNPropertiesException e) {
			// Ignore, try the other implementations
		}
		try {
			if (result == null)
				result = new PlainPureSeparation(utility, properties, locationMap);
		} catch (UnsupportedPNPropertiesException e) {
			// Ignore, try the other implementations
		}
		if (result == null)
			result = new InequalitySystemSeparation(utility, properties, locationMap);

		debug("Created Separation instance from class ", result.getClass().getName());
		return result;
	}

	/**
	 * Construct a new Synthesizer instance.
	 * @param utility The region utility to use.
	 * @param properties Properties that the calculated region should satisfy.
	 * @param onlyEventSeparation A flag indicating that state separation should be ignored.
	 * @param quickFail If true, stop the calculation as soon as it is known that it won't be successful. If false,
	 * try to solve all separation problems. Only if true will the list of failed problems be fully filled.
	 * @return A suitable Separation instance
	 * @throws MissingLocationException if the transition system for the utility has locations for only some events
	 */
	static public Synthesizer createSynthesizerInstance(RegionUtility utility, PNProperties properties,
			boolean onlyEventSeparation, boolean quickFail) throws MissingLocationException {
		boolean tryToFactorize = !Boolean.getBoolean("apt.separation.skipFactorisation");
		return createSynthesizerInstance(utility, properties, onlyEventSeparation, quickFail, tryToFactorize);
	}

	/**
	 * Construct a new Synthesizer instance.
	 * @param utility The region utility to use.
	 * @param properties Properties that the calculated region should satisfy.
	 * @param onlyEventSeparation A flag indicating that state separation should be ignored.
	 * @param quickFail If true, stop the calculation as soon as it is known that it won't be successful. If false,
	 * try to solve all separation problems. Only if true will the list of failed problems be fully filled.
	 * @param tryToFactorize Try to factorize the input before actual synthesis begins.
	 * @return A suitable Separation instance
	 * @throws MissingLocationException if the transition system for the utility has locations for only some events
	 */
	static public Synthesizer createSynthesizerInstance(RegionUtility utility, PNProperties properties,
			boolean onlyEventSeparation, boolean quickFail, boolean tryToFactorize)
			throws MissingLocationException {
		if (quickFail && tryToFactorize) {
			// Try to factorize the input
			Synthesizer result = new FactorisationSynthesizer().createSynthesizer(utility,
					properties, onlyEventSeparation);
			if (result != null)
				return result;
		}
		Separation result = createSeparationInstance(utility, properties);
		if (result instanceof Synthesizer)
			return (Synthesizer) result;
		return new SeparationSynthesizer(utility.getTransitionSystem(), result, onlyEventSeparation, quickFail);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
