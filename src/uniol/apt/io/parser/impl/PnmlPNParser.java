/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
 * Copyright (C) 2015       Uli Schlachter
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

package uniol.apt.io.parser.impl;

import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.PNParser;
import uniol.apt.io.parser.ParseException;

/**
 * Reads P/T nets in PNML format.
 * @author Uli Schlachter, based on code by Thomas Strathmann
 */
public class PnmlPNParser extends AbstractPNParser implements PNParser {
	private static class Parser {
		final private PetriNet pn;

		// Constructor that gets the parsing started. This only exists so that we have some kind of "fake local
		// variables". The only caller is parsePN().
		public Parser(PetriNet pn, InputStream is) throws ParseException, IOException {
			this.pn = pn;
			Document doc = getDocument(is);

			Element root = doc.getDocumentElement();
			if (!root.getNodeName().equals("pnml"))
				throw new ParseException("Root element isn't <pnml>");

			Element net = nextElement(root.getFirstChild());
			while (net != null && !net.getTagName().equals("net"))
				net = nextElement(net.getNextSibling());
			if (net == null)
				throw new ParseException("Root tag <pnml> doesn't have a child <net>");

			pn.setName(getAttribute(net, "id"));
			createPlacesAndTransitions(net);
			createArcs(net);

			do
				net = nextElement(net.getNextSibling());
			while (net != null && !net.getTagName().equals("net"));
			if (net != null)
				throw new ParseException("Root contains multiple <net> tags");
		}

		// Parse an xml file into a DOM model
		private Document getDocument(InputStream is) throws ParseException, IOException {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new ParseException("Internal error while parsing the document", e);
			}
			builder.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException e) throws SAXException {
					// Silently ignore warnings
				}

				@Override
				public void error(SAXParseException e) throws SAXException {
					throw e;
				}

				@Override
				public void fatalError(SAXParseException e) throws SAXException {
					throw e;
				}
			});
			try {
				return builder.parse(is);
			} catch (SAXException e) {
				throw new ParseException("Could not parse PNML XML file", e);
			}
		}

		// Find the next Element after 'node' (or null if there is none)
		private Element nextElement(Node node) throws ParseException {
			while (node != null && !(node instanceof Element))
				node = node.getNextSibling();
			return (Element) node;
		}

		// Get an attribute of an element or throw an exception if it doesn't have such an attribute
		private String getAttribute(Element elem, String name) throws ParseException {
			String result = elem.getAttribute(name);
			if (result.equals(""))
				throw new ParseException("Element <" + elem.getTagName() + "> does not have attribute " + name);
			return result;
		}

		// Get the text inside of this element
		private String getText(Element element) throws ParseException {
			Node child = element.getFirstChild();
			if (child == null || child.getNextSibling() != null)
				throw new ParseException("Trying to get text inside of <" +
						element.getTagName() + ">, but this element has multiple children");
			if (!(child instanceof Text))
				throw new ParseException("Trying to get text inside of <" +
						element.getTagName() + ">, but child isn't text");
			return child.getNodeValue();
		}

		// Get a (unique!) child node with a tag from "tags"
		private Element getOptionalChildNode(Element element, String... tags) throws ParseException {
			List<String> tagsList = Arrays.asList(tags);
			Element cur = nextElement(element.getFirstChild());
			Element result = null;
			while (cur != null) {
				if (tagsList.contains(cur.getTagName())) {
					if (result != null)
						throw new ParseException("Found multiple children with a tag from" +
								tagsList + ", but only one was expected");
					result = cur;
				}
				cur = nextElement(cur.getNextSibling());
			}
			return result;
		}

		private Element getChildNode(Element element, String... tags) throws ParseException {
			Element result = getOptionalChildNode(element, tags);
			if (result == null)
				throw new ParseException("Did not find any children with a tag from " + Arrays.toString(tags));
			return result;
		}

		// Parse an integer in the format "foo,123", ignoring everything before the first comma
		private int parseIntegerWithComma(String str) throws ParseException {
			// PIPE has values like "Default,5"
			int index = str.indexOf(",");
			if (index != -1)
				str = str.substring(index + 1);
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				throw new ParseException("Cannot parse number " + str, e);
			}
		}

		// Create a place corresponding to the given <place> element
		private void createPlace(Element element) throws ParseException {
			String value = getText(getChildNode(getChildNode(element, "initialMarking"), "value", "text"));
			// PIPE has values like "Default,5" since it supports token classes
			int initialMarking = parseIntegerWithComma(value);
			if (initialMarking < 0)
				throw new ParseException("Negative initial marking");
			pn.createPlace(getAttribute(element, "id")).setInitialToken(initialMarking);
		}

		// Create a transition corresponding to the given <transition> element
		private void createTransition(Element element) throws ParseException {
			pn.createTransition(getAttribute(element, "id"));
		}

		// Create all places and transitions for the given <net> tag
		private void createPlacesAndTransitions(Element net) throws ParseException {
			Element cur = nextElement(net.getFirstChild());
			while (cur != null) {
				switch (cur.getTagName()) {
					case "place":
						createPlace(cur);
						break;
					case "transition":
						createTransition(cur);
						break;
					default:
						break;
				}
				cur = nextElement(cur.getNextSibling());
			}
		}

		// Create an arc for the given <arc> element
		private void createArc(Element element) throws ParseException {
			Flow flow = pn.createFlow(getAttribute(element, "source"), getAttribute(element, "target"));
			Element insc = getOptionalChildNode(element, "inscription");
			if (insc != null) {
				String value = getText(getChildNode(insc, "value", "text"));
				flow.setWeight(parseIntegerWithComma(value));
			}
		}

		// Create all arcs for the given <net> tag
		private void createArcs(Element net) throws ParseException {
			Element cur = nextElement(net.getFirstChild());
			while (cur != null) {
				if (cur.getTagName().equals("arc")) {
					createArc(cur);
				}
				cur = nextElement(cur.getNextSibling());
			}
		}
	}

	@Override
	public PetriNet parsePN(InputStream is) throws ParseException {
		PetriNet pn = new PetriNet();
		try {
			new Parser(pn, is);
		} catch (IOException e) {
			throw new ParseException(e);
		}
		return pn;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
