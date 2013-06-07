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

package uniol.apt.io.parser.impl.exception;

/**
 * An exception that reports an error caused by the machinery
 * of the PNML parser.
 * 
 * Its raison d'etre: Hide the concrete exceptions thrown by
 * any given XML parser and related library code so that changing
 * from one approach for reading XML data to another does not
 * affect the code in the ui package.   
 * 
 * @author Thomas Strathmann
 */
public class PNMLParserException extends FormatException {

	private static final long serialVersionUID = 1L;
	private String message = "Fatal error in PNML parser";

	/**
	 * Creates the exception. The code constructing this object is
	 * responsible for providing a helpful and precise error message.
	 * 
	 * @param message - the error message to report
	 */
	public PNMLParserException(String message) {
		if(message != null && !message.isEmpty()) {
			this.message += ":" + System.lineSeparator();
		}		
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
