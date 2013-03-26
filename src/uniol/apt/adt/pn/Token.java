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

package uniol.apt.adt.pn;

import java.util.Objects;

/**
 * The Token class is used to represent a token on a place. It also implements Omega functionality for the coverability
 * graph.
 * <p/>
 * @author Manuel Gieseking
 */
public class Token implements Comparable<Token> {

	private int v = 0;
	private static final int OMEGA_VALUE = -1;
	public final static Token OMEGA = new Token();

	static {
		OMEGA.setOmega();
	}

	/**
	 * Constructor with count of token 0.
	 */
	public Token() {
	}

	/**
	 * Constructor copying the value of the given token.
	 * <p/>
	 * @param v The token from which it should be copied.
	 * <p/>
	 * @throws IllegalArgumentException if the argument is null.
	 */
	public Token(Token v) {
		if (v == null) {
			throw new IllegalArgumentException("v == null");
		}
		this.v = v.v;
	}

	/**
	 * Constructor initialising a token with a given value.
	 * <p/>
	 * @param v the initial value.
	 * <p/>
	 * @throws IllegalArgumentException if the given value is less than zero.
	 */
	public Token(int v) {
		if (v < 0) {
			throw new IllegalArgumentException("v<0");
		}
		this.v = v;
	}

	/**
	 * Adds a given value to this token. If one value is OMEGA so the value of this token will be OMEGA.
	 * <p/>
	 * @param t The token to add.
	 * <p/>
	 * @throws IllegalArgumentException if the argument is null.
	 */
	void add(Token t) {
		if (t == null) {
			throw new IllegalArgumentException("v == null");
		}
		if (t.isOmega()) {
			this.setOmega();
		} else if (!this.isOmega()) {
			this.v += t.v;
		}
	}

	/**
	 * Adds a given value to this token. If this token is OMEGA so it stays by OMEGA.
	 * <p/>
	 * @param t The value to add.
	 * <p/>
	 * @throws IllegalArgumentException if the result of this addition would be less than zero.
	 */
	void add(int t) {
		if (!this.isOmega()) {
			if (this.v + t < 0) {
				throw new IllegalArgumentException("this.v + v < 0");
			}
			this.v += t;
		}
	}

	/**
	 * Returns the value of this token. That means a natural number or -1 if this token is representing OMEGA.
	 * <p/>
	 * @return if isOmega() == true then -1 else the natural number of token count.
	 */
	public int getValue() {
		return this.v;
	}

	/**
	 * Returns if this token represents OMEGA or not.
	 * <p/>
	 * @return true, if this instance is representing OMEGA.
	 */
	public boolean isOmega() {
		return this.v == Token.OMEGA_VALUE;
	}

	/**
	 * Sets the value of this token to OMEGA.
	 */
	public void setOmega() {
		this.v = Token.OMEGA_VALUE;
	}

	@Override
	public int hashCode() {
		return v;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Token other = (Token) obj;
		if (!Objects.equals(this.v, other.v)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		if (this.isOmega()) {
			return "OMEGA";
		} else {
			return Integer.toString(v);
		}
	}

	@Override
	public int compareTo(Token o) {
		if (this.isOmega()) {
			return (o.isOmega()) ? 0 : 1;
		}
		if (o.isOmega()) {
			return -1;
		}
		return (this.v < o.v) ? -1 : (this.v > o.v) ? 1 : 0;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
