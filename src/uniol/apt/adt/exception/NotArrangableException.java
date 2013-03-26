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

package uniol.apt.adt.exception;

/**
 * The NotArrangleException is thrown if two object are not able to arrange.
 * <p/>
 * @author Manuel Gieseking
 */
public class NotArrangableException extends RuntimeException {

	public static final long serialVersionUID = 0xdeadbeef00000008l;

	/**
	 * Constructor creates a new NotArrangableException
	 * <p/>
	 * @param msg Name of class which could not be arranged.
	 */
	public NotArrangableException(String msg) {
		super(msg + " not arrangable.");
	}

	/**
	 * Constructor creates a new NotArrangableException with a given cause.
	 * <p/>
	 * @param msg   Name of class which could not be arranged.
	 * @param cause The cause for this exception as Throwable.
	 */
	public NotArrangableException(String msg, Throwable cause) {
		super(msg + " not arrangable.", cause);
	}

	/**
	 * Constructor creates a new NotArrangableException
	 * <p/>
	 * @param msg Name of class which could not be arranged.
	 * @param o1  Object which should be arranged.
	 * @param o2  Object which should be arranged.
	 */
	public NotArrangableException(String msg, Object o1, Object o2) {
		super(msg + " not arrangable: " + o1.toString() + " vs " + o2.toString()
			+ ".");
	}

	/**
	 * Constructor creates a new NotArrangableException with a given cause.
	 * <p/>
	 * @param msg   Name of class which could not be arranged.
	 * @param o1    Object which should be arranged.
	 * @param o2    Object which should be arranged.
	 * @param cause The cause for this exception as Throwable.
	 */
	public NotArrangableException(String msg, Object o1, Object o2, Throwable cause) {
		super(msg + " not arrangable: " + o1.toString() + " vs " + o2.toString()
			+ ".", cause);
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
