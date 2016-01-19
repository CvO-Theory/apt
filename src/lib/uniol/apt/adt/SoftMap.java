/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  Uli Schlachter
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

package uniol.apt.adt;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A soft reference to a map. This keeps a soft reference to a map and re-creates the map if the reference is
 * invalidated. In other words, this is a map which sometimes loses all of its entries.
 * @param <K> The type of keys in the map.
 * @param <V> The type of values in the map.
 * @author Uli Schlachter
 */
public class SoftMap<K, V> implements Map<K, V> {

	private Reference<Map<K, V>> map;

	/**
	 * Construct a new, empty map.
	 */
	public SoftMap() {
		map = new SoftReference<>(null);
	}

	/**
	 * Constructs a new SoftMap with the same mappings as the specified map.
	 * @param m the map whose mappings should be copied
	 */
	public SoftMap(Map<? extends K, ? extends V> m) {
		map = new SoftReference<Map<K, V>>(new HashMap<>(m));
	}

	// Get or create the underlying map of this SoftMap.
	private Map<K, V> getMap() {
		Map<K, V> result = map.get();
		if (result == null) {
			result = new HashMap<>();
			map = new SoftReference<>(result);
		}
		return result;
	}

	@Override
	public V get(Object o) {
		Map<K, V> result = map.get();
		if (result == null)
			return null;
		return result.get(o);
	}

	@Override
	public V put(K key, V value) {
		return getMap().put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		getMap().putAll(m);
	}

	@Override
	public V remove(Object key) {
		Map<K, V> result = map.get();
		if (result == null)
			return null;
		return result.remove(key);
	}

	@Override
	public void clear() {
		Map<K, V> result = map.get();
		if (result != null)
			result.clear();
	}

	@Override
	public boolean isEmpty() {
		Map<K, V> result = map.get();
		if (result == null)
			return true;
		return result.isEmpty();
	}

	@Override
	public int size() {
		Map<K, V> result = map.get();
		if (result == null)
			return 0;
		return result.size();
	}

	@Override
	public Collection<V> values() {
		Map<K, V> result = map.get();
		if (result == null)
			return Collections.emptySet();
		return result.values();
	}

	@Override
	public boolean containsKey(Object o) {
		Map<K, V> result = map.get();
		if (result == null)
			return false;
		return result.containsKey(o);
	}

	@Override
	public boolean containsValue(Object o) {
		Map<K, V> result = map.get();
		if (result == null)
			return false;
		return result.containsValue(o);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		Map<K, V> result = map.get();
		if (result == null)
			return Collections.emptySet();
		return result.entrySet();
	}

	@Override
	public Set<K> keySet() {
		Map<K, V> result = map.get();
		if (result == null)
			return Collections.emptySet();
		return result.keySet();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SoftMap) {
			SoftMap<?, ?> oMap = (SoftMap<?, ?>) o;
			return getMap().equals(oMap.getMap());
		}
		if (o instanceof Map) {
			Map<?, ?> oMap = (Map<?, ?>) o;
			return getMap().equals(oMap);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getMap().hashCode();
	}

	@Override
	public String toString() {
		return getMap().toString();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
