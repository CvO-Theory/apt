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

package uniol.apt.analysis.synthesize;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Collection of properties that a (synthesized) Petri Net can/should satisfy.
 * @author Uli Schlachter
 */
public class PNProperties extends AbstractSet<PNProperties.PNProperty> {
	static public KBounded kBounded(int k) {
		return new KBounded(k);
	}

	public final static KBounded SAFE = kBounded(1);
	public final static PNProperty PURE = new PNProperty() {
		@Override
		public String toString() {
			return "PURE";
		}
	};
	public final static PNProperty PLAIN = new PNProperty() {
		@Override
		public String toString() {
			return "PLAIN";
		}
	};
	public final static PNProperty TNET = new PNProperty() {
		@Override
		public String toString() {
			return "TNET";
		}
	};
	public final static PNProperty OUTPUT_NONBRANCHING = new PNProperty() {
		@Override
		public String toString() {
			return "OUTPUT_NONBRANCHING";
		}
	};
	public final static PNProperty CONFLICT_FREE = new PNProperty() {
		@Override
		public String toString() {
			return "CONFLICT_FREE";
		}
	};

	static public interface PNProperty {
	}

	static public class KBounded implements PNProperty {
		final public int k;

		public KBounded(int k) {
			assert k >= 1;
			this.k = k;
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof KBounded && ((KBounded) o).k == k;
		}

		@Override
		public int hashCode() {
			return k;
		}

		@Override
		public String toString() {
			if (k == 1)
				return "SAFE";
			return k + "-BOUNDED";
		}
	}

	// -1 means no k specified
	final static private int KBOUNDED_DEFAULT = -1;
	private int kBounded = KBOUNDED_DEFAULT;
	private boolean pure = false;
	private boolean plain = false;
	private boolean tnet = false;
	private boolean outputNonbranching = false;
	private boolean conflictFree = false;

	/**
	 * Create a new, empty Petri net properties instance.
	 */
	public PNProperties() {
	}

	/**
	 * Create a new Petri net properties instance from the given properties.
	 * @param properties A list of properties to copy
	 */
	public PNProperties(PNProperty... properties) {
		for (PNProperty property : properties)
			add(property);
	}

	/**
	 * Create a new Petri net properties instance from the given collection.
	 * @param properties A collection of properties to copy
	 */
	public PNProperties(Collection<? extends PNProperty> properties) {
		addAll(properties);
	}

	/**
	 * Return true if this property description requires k-boundedness for some k.
	 * @return true if there is some k set for which we require k-boundedness
	 */
	public boolean isKBounded() {
		return kBounded != KBOUNDED_DEFAULT;
	}

	/**
	 * Return true if this property description requires k-boundedness.
	 * @param k The k for k-boundedness
	 * @return true if k-bounded
	 */
	public boolean isKBounded(int k) {
		return isKBounded() && kBounded <= k;
	}

	/**
	 * Return true if this property description requires safeness.
	 * @return true if safe
	 */
	public boolean isSafe() {
		return isKBounded(1);
	}

	/**
	 * Return the k that for k-boundedness. This may only be called if isKBounded() returns true.
	 * @return the k for k-boundedness
	 */
	public int getKForKBoundedness() {
		assert isKBounded();
		return kBounded;
	}

	/**
	 * Return true if this property description requires pureness.
	 * @return true if pure
	 */
	public boolean isPure() {
		return pure;
	}

	/**
	 * Return true if this property description requires plainness.
	 * @return true if plain
	 */
	public boolean isPlain() {
		return plain;
	}

	/**
	 * Return true if this property description requires a T-Net.
	 * @return true if T-net
	 */
	public boolean isTNet() {
		return tnet;
	}

	/**
	 * Return true if this property description requires an output nonbranching PN.
	 * @return true if output nonbranching
	 */
	public boolean isOutputNonbranching() {
		return outputNonbranching;
	}

	/**
	 * Return true if this property description requires a conflict free PN.
	 * @return true if conflict free
	 */
	public boolean isConflictFree() {
		return conflictFree;
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof PNProperty))
			return false;

		PNProperty property = (PNProperty) o;
		if (property instanceof KBounded) {
			return isKBounded(((KBounded) property).k);
		} else if (PURE.equals(property)) {
			return pure;
		} else if (PLAIN.equals(property)) {
			return plain;
		} else if (TNET.equals(property)) {
			return tnet;
		} else if (OUTPUT_NONBRANCHING.equals(property)) {
			return outputNonbranching;
		} else if (CONFLICT_FREE.equals(property)) {
			return conflictFree;
		}
		return false;
	}

	@Override
	public boolean add(PNProperty property) {
		if (contains(property))
			return false;
		if (property instanceof KBounded) {
			int k = ((KBounded) property).k;
			if (!isKBounded(k))
				kBounded = k;
		} else if (PURE.equals(property)) {
			pure = true;
		} else if (PLAIN.equals(property)) {
			plain = true;
		} else if (TNET.equals(property)) {
			tnet = true;
			// Our definition of TNet includes plainness
			plain = true;
			// An impure TNet (while technically possible) makes no sense, because if a transition has a
			// place as a side-condition, the place cannot be connected to anything else and thus never
			// changes its marking
			pure = true;
			// A T-Net is ON + the same condition on the presets.
			outputNonbranching = true;
		} else if (OUTPUT_NONBRANCHING.equals(property)) {
			outputNonbranching = true;
		} else if (CONFLICT_FREE.equals(property)) {
			conflictFree = true;
			// Out definition of CF requires plainness
			plain = true;
		}
		return true;
	}

	@Override
	public int size() {
		int size = 0;
		for (Iterator<?> it = iterator(); it.hasNext(); it.next())
			size++;
		return size;
	}

	@Override
	public Iterator<PNProperty> iterator() {
		return new Iterator<PNProperty>() {
			int state = 0;

			@Override
			public boolean hasNext() {
				if (state == 0 && !isKBounded())
					state++;
				if (state == 1 && !pure)
					state++;
				if (state == 2 && !plain)
					state++;
				if (state == 3 && !tnet)
					state++;
				if (state == 4 && !outputNonbranching)
					state++;
				if (state == 5 && !conflictFree)
					state++;
				return state < 6;
			}

			@Override
			public PNProperty next() {
				// update state member variable
				hasNext();
				switch (state++) {
					case 0:
						return kBounded(kBounded);
					case 1:
						return PURE;
					case 2:
						return PLAIN;
					case 3:
						return TNET;
					case 4:
						return OUTPUT_NONBRANCHING;
					case 5:
						return CONFLICT_FREE;
					default:
						throw new NoSuchElementException();
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
