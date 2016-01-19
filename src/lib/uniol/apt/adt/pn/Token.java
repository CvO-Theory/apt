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
 * @author Manuel Gieseking
 */
public class Token implements Comparable<Token> {

	private final long v;
	public final static Token OMEGA = new Token(-1, false);
	public final static Token ZERO = new Token(0);

	private static Token[] tokenCache;
	static {
		tokenCache = new Token[256];
		tokenCache[0] = ZERO;
		for (int i = 1; i < tokenCache.length; i++)
			tokenCache[i] = new Token(i);
	}

	/**
	 * Returns a Token instance for the given value.
	 * @param v The token's value
	 * @return A token instance for that value
	 */
	public static Token valueOf(long v) {
		if (v >= 0 && v < tokenCache.length)
			return tokenCache[(int) v];
		return new Token(v);
	}

	private Token(long v, boolean checkValue) {
		if (checkValue && v < 0) {
			throw new IllegalArgumentException("v<0");
		}
		this.v = v;
	}

	/**
	 * Constructor initialising a token with a given value.
	 * @param v the initial value.
	 * @throws IllegalArgumentException if the given value is less than zero.
	 * @deprecated Use {@link valueOf} instead.
	 */
	@Deprecated
	public Token(long v) {
		this(v, true);
	}

	/**
	 * Adds a given value to this token. If one value is OMEGA so the value of this token will be OMEGA.
	 * @param t The token to add.
	 * @throws IllegalArgumentException if the argument is null.
	 * @return The sum of both token instances.
	 */
	Token add(Token t) {
		if (t == null) {
			throw new IllegalArgumentException("v == null");
		}
		if (this.isOmega())
			return this;
		if (t.isOmega())
			return t;
		assert t.v >= 0;
		return add(t.v);
	}

	/**
	 * Adds a given value to this token. If this token is OMEGA so it stays by OMEGA.
	 * @param t The value to add.
	 * @throws IllegalArgumentException if the result of this addition would be less than zero.
	 * @return The sum of this token instance and the given value.
	 */
	Token add(long t) {
		if (this.isOmega())
			return this;
		if (this.v + t < 0) {
			throw new IllegalArgumentException("this.v + v < 0");
		}
		return valueOf(this.v + t);
	}

	/**
	 * Returns the value of this token. That means a natural number or -1 if this token is representing OMEGA.
	 * @return if isOmega() == true then -1 else the natural number of token count.
	 */
	public long getValue() {
		return this.v;
	}

	/**
	 * Returns if this token represents OMEGA or not.
	 * @return true, if this instance is representing OMEGA.
	 */
	public boolean isOmega() {
		return this.equals(OMEGA);
	}

	@Override
	public int hashCode() {
		return (int) v;
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
			return Long.toString(v);
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
