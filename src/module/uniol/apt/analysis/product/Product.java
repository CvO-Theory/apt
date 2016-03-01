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

package uniol.apt.analysis.product;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uniol.apt.adt.ts.TransitionSystem;

public class Product {
	
	private final TransitionSystem ts1;
	private final TransitionSystem ts2;
	
	public Product(TransitionSystem ts1, TransitionSystem ts2) {
		this.ts1 = ts1;
		this.ts2 = ts2;
	}
	
	public TransitionSystem getSyncProduct() {
		throw new NotImplementedException();
	}
	
	public TransitionSystem getAsyncProduct() {
		throw new NotImplementedException();
	}

}
