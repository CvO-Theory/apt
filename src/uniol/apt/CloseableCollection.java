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

package uniol.apt;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Object which contains a List of Closeable objects, which can all get closed
 * with one method call.
 *
 * @author Uli Schlachter
 */
class CloseableCollection<T extends Closeable> implements Closeable {
	private final List<T> array = new ArrayList<>();
	private final List<Boolean> needsClose = new ArrayList<>();

	public T get(int idx) {
		return array.get(idx);
	}

	public void add(T val, boolean needsClose) {
		this.array.add(val);
		this.needsClose.add(needsClose);
	}

	@Override
	public void close() throws IOException {
		IOException err = null;
		while (!array.isEmpty()) {
			Closeable c = array.remove(0);
			boolean close = needsClose.remove(0);
			if (close)
				try {
					c.close();
				} catch (IOException e) {
					if (err == null)
						err = e;
					else
						err.addSuppressed(e);
				}
		}
		if (err != null)
			throw err;
	}
}
