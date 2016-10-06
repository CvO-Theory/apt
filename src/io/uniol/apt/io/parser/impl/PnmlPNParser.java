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

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.io.parser.AptParser;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.Parser;

/**
 * Reads P/T nets in PNML format.
 *
 * @author Jonas Prellberg
 */
@AptParser
public class PnmlPNParser extends AbstractParser<PetriNet> implements Parser<PetriNet> {
	public final static String FORMAT = "pnml";
	public final static String EXTENSION_KEY_NAME = "pnmlName";

	private static class Parser {

		private static enum Mode {
			ISO("http://www.pnml.org/version-2009/grammar/ptnet", "text"), LOLA(
					"http://www.informatik.hu-berlin.de/top/pntd/ptNetb",
					"text"), PIPE("P/T net", "value");

			private final String netType;
			private final String textElementName;

			private Mode(String netType, String textElementName) {
				this.netType = netType;
				this.textElementName = textElementName;
			}

			public String getNetType() {
				return netType;
			}

			public String getTextElementName() {
				return textElementName;
			}
		}

		private Mode mode;
		private PetriNet pn;
		private Map<String, String> safeIdMap;
		private int idCounter;

		/**
		 * Parses multiple variants of PNML:
		 * <ul>
		 * <li>ISO/IEC 15909
		 * <li>LoLa PNML output
		 * <li>PIPE PNML output
		 * </ul>
		 *
		 * @param is
		 *                input stream to read PNML from
		 * @return the parsed petri net
		 * @throws ParseException
		 * @throws IOException
		 */
		public PetriNet parse(InputStream is) throws ParseException, IOException {
			Element net = getFirstNetElement(is);

			// Try parser variants one after another
			Exception exIso, exPipe, exLola;
			try {
				return parseIsoPNML(net);
			} catch (ParseException e) {
				exIso = e;
			}
			try {
				return parseUnpagedPNML(net, Mode.PIPE);
			} catch (ParseException e) {
				exPipe = e;
			}
			try {
				return parseUnpagedPNML(net, Mode.LOLA);
			} catch (ParseException e) {
				exLola = e;
			}

			String msg = String.format(
					"The PNML format could not be parsed by any variant of the PNML parser.\n"
							+ "\t(ISO)\t %s\n\t(PIPE)\t %s\n\t(LOLA)\t %s",
					exIso.getMessage(), exPipe.getMessage(), exLola.getMessage());
			throw new ParseException(msg);
		}

		/**
		 * Parses PNML that conforms to the standard ISO/IEC 15909.
		 *
		 * @param net
		 *                pnml net element
		 * @return parsed Petri net
		 * @throws ParseException
		 * @throws IOException
		 */
		private PetriNet parseIsoPNML(Element net) throws ParseException, IOException {
			init(Mode.ISO, net);
			parsePagesForNodes(net);
			parsePagesForEdges(net);
			return pn;
		}

		/**
		 * Parses PNML output that has no page tags.
		 *
		 * @param net
		 *                pnml net element
		 * @param mode
		 *                mode that allows to parse different
		 *                tool-specific versions of PNML
		 * @return parsed Petri net
		 * @throws ParseException
		 * @throws IOException
		 */
		private PetriNet parseUnpagedPNML(Element net, Mode mode) throws ParseException, IOException {
			init(mode, net);
			createNodes(net);
			createEdges(net);
			return pn;
		}

		private void init(Mode mode, Element net) throws ParseException {
			String pnName = getAttribute(net, "id");
			this.pn = new PetriNet(pnName);
			this.safeIdMap = new HashMap<>();
			this.idCounter = 0;
			this.mode = mode;
			checkNetType(net, mode.getNetType());
		}

		/**
		 * Returns the first net element found.
		 *
		 * @param is
		 *                input stream to read PNML from
		 * @return first <net> element
		 * @throws IOException
		 * @throws ParseException
		 */
		private Element getFirstNetElement(InputStream is) throws ParseException, IOException {
			Document doc = getDocument(is);
			Element root = getPnmlRoot(doc);

			// Only consider the first net that is encountered
			Element net = getChildElement(root, "net");
			return net;
		}

		/**
		 * Checks that the given net element has an attribute "type"
		 * whose value matches the given expected type. If it does not
		 * match and exception is thrown.
		 *
		 * @param net
		 * @param expectedType
		 * @throws ParseException
		 */
		private void checkNetType(Element net, String expectedType) throws ParseException {
			String type = net.getAttribute("type");
			if (!expectedType.equals(type)) {
				String msg = String.format("Expected net type '%s' but found '%s'", expectedType, type);
				throw new ParseException(msg);
			}
		}

		/**
		 * Parses an xml file into a DOM model.
		 */
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

		/**
		 * Returns the root pnml element.
		 */
		private Element getPnmlRoot(Document doc) throws ParseException {
			Element root = doc.getDocumentElement();
			if (!root.getNodeName().equals("pnml")) {
				throw new ParseException("Root element isn't <pnml>");
			}
			return root;
		}

		/**
		 * Recursively parses all pages contained in the given element
		 * and creates nodes for each place or transition encountered.
		 *
		 * @param parent
		 *                net or (sub-)page element
		 * @throws ParseException
		 */
		private void parsePagesForNodes(Element parent) throws ParseException {
			List<Element> childPages = getChildElements(parent, "page");
			for (Element page : childPages) {
				createNodes(page);
				parsePagesForNodes(page);
			}
		}

		/**
		 * Recursively parses all pages contained in the given element
		 * and creates edges for each arc encountered.
		 *
		 * @param parent
		 *                net or (sub-)page element
		 * @throws ParseException
		 */
		private void parsePagesForEdges(Element parent) throws ParseException {
			List<Element> childPages = getChildElements(parent, "page");
			for (Element page : childPages) {
				createEdges(page);
				parsePagesForEdges(page);
			}
		}

		/**
		 * Returns a list of all child elements with the given tag name.
		 *
		 * @param parent
		 *                parent element
		 * @param tagName
		 *                child tag name
		 * @return list of child elements
		 */
		private List<Element> getChildElements(Element parent, String tagName) {
			List<Element> elements = new ArrayList<>();
			NodeList children = parent.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(tagName)) {
					elements.add((Element) child);
				}
			}
			return elements;
		}

		/**
		 * Returns a single child element with the given name.
		 *
		 * @param parent
		 *                parent element
		 * @param tagName
		 *                child tag name
		 * @return the child element
		 * @throws ParseException
		 *                 thrown when not exactly one child with the
		 *                 given name is found
		 */
		private Element getChildElement(Element parent, String tagName) throws ParseException {
			List<Element> elements = getChildElements(parent, tagName);
			if (elements.size() == 1) {
				return elements.get(0);
			} else {
				throw new ParseException(
						String.format("Expected single child <%s> of parent <%s> but found %d",
								tagName, parent.getTagName(), elements.size()));
			}
		}

		/**
		 * Returns a single child element with the given name or null if
		 * it does not exist.
		 *
		 * @param parent
		 *                parent element
		 * @param tagName
		 *                child tag name
		 * @return the child element
		 */
		private Element getOptionalChildElement(Element parent, String tagName) {
			List<Element> elements = getChildElements(parent, tagName);
			if (elements.size() == 1) {
				return elements.get(0);
			} else {
				return null;
			}
		}

		/**
		 * Returns an attribute's value.
		 *
		 * @param elem
		 *                element of which the attribute is to be
		 *                returned
		 * @param attrName
		 *                attribute name
		 * @return attribute value
		 * @throws ParseException
		 *                 thrown when the attribute does not exist
		 */
		private String getAttribute(Element elem, String attrName) throws ParseException {
			if (!elem.hasAttribute(attrName)) {
				throw new ParseException("Element <" + elem.getTagName() + "> does not have attribute "
						+ attrName);
			}
			return elem.getAttribute(attrName);
		}

		/**
		 * Returns the text contents of an element.
		 *
		 * @param element
		 *                parent element
		 * @return text enclosed by parent element
		 * @throws ParseException
		 *                 thrown when there are other elements inside
		 *                 the parent element
		 */
		private String getText(Element element) throws ParseException {
			Node child = element.getFirstChild();
			if (child == null || child.getNextSibling() != null)
				throw new ParseException("Trying to get text inside of <" + element.getTagName()
						+ ">, but this element has multiple children");
			if (!(child instanceof Text))
				throw new ParseException("Trying to get text inside of <" + element.getTagName()
						+ ">, but child isn't text");
			return child.getNodeValue();
		}

		/**
		 * Creates places and transitions for all elements found in the
		 * given page.
		 *
		 * @param page
		 *                page element
		 * @throws ParseException
		 */
		private void createNodes(Element page) throws ParseException {
			List<Element> places = getChildElements(page, "place");
			for (Element place : places) {
				createPlace(place);
			}

			List<Element> transitions = getChildElements(page, "transition");
			for (Element transition : transitions) {
				createTransition(transition);
			}
		}

		/**
		 * Creates a place corresponding to the given <place> element.
		 *
		 * @param place
		 *                place element
		 * @throws ParseException
		 */
		private void createPlace(Element place) throws ParseException {
			String id = toSafeIdentifier(getAttribute(place, "id"));
			String name = parseName(place);
			long initialMarking = parseInitialMarking(place);
			Place pnPlace = pn.createPlace(id);
			pnPlace.setInitialToken(initialMarking);
			if (name != null) {
				pnPlace.putExtension(EXTENSION_KEY_NAME, name);
			}
		}

		/**
		 * Parses the initial marking of a place.
		 *
		 * @param place
		 *                place element
		 * @return initial marking or default value of 0
		 * @throws ParseException
		 */
		private long parseInitialMarking(Element place) throws ParseException {
			Element initMarkElem = getOptionalChildElement(place, "initialMarking");
			if (initMarkElem == null) {
				return 0;
			}
			Element textElem = getChildElement(initMarkElem, mode.getTextElementName());
			String textValue = getText(textElem);
			long initialMarking = parseLong(textValue);
			if (initialMarking < 0) {
				throw new ParseException("Negative initial marking");
			} else {
				return initialMarking;
			}
		}

		/**
		 * Creates a transition corresponding to the given
		 * <transition> element
		 *
		 * @param transition
		 *                transition element
		 * @throws ParseException
		 */
		private void createTransition(Element transition) throws ParseException {
			String id = toSafeIdentifier(getAttribute(transition, "id"));
			String name = parseName(transition);
			if (name != null) {
				pn.createTransition(id, name);
			} else {
				pn.createTransition(id);
			}
		}

		/**
		 * Creates arcs for all elements found in the given page.
		 *
		 * @param page
		 *                page element
		 * @throws ParseException
		 */
		private void createEdges(Element page) throws ParseException {
			List<Element> arcs = getChildElements(page, "arc");
			for (Element arc : arcs) {
				createArc(arc);
			}
		}

		/**
		 * Creates an arc for the given <arc> element.
		 *
		 * @param arc
		 * @throws ParseException
		 */
		private void createArc(Element arc) throws ParseException {
			String sourceId = toSafeIdentifier(getAttribute(arc, "source"));
			String targetId = toSafeIdentifier(getAttribute(arc, "target"));

			Flow flow = pn.createFlow(sourceId, targetId);

			String name = parseName(arc);
			if (name != null) {
				flow.putExtension(EXTENSION_KEY_NAME, name);
			}

			Element insc = getOptionalChildElement(arc, "inscription");
			if (insc != null) {
				Element textElem = getChildElement(insc, mode.getTextElementName());
				String textValue = getText(textElem);
				long weight = parseLong(textValue);
				if (weight > Integer.MAX_VALUE) {
					throw new ParseException(
							"Enountered arc weight > 2^31 - 1 which APT does not support");
				}
				flow.setWeight((int)weight);
			}
		}

		/**
		 * Parses the name of a "basicobject" element like place,
		 * transition or arc.
		 *
		 * @param elem
		 *                "basicobject" element
		 * @return the name or null if none exists
		 * @throws ParseException
		 */
		private String parseName(Element elem) throws ParseException {
			Element nameElem = getOptionalChildElement(elem, "name");
			if (nameElem == null) {
				return null;
			} else {
				Element textElem = getChildElement(nameElem, mode.getTextElementName());
				return getText(textElem);
			}
		}

		/**
		 * Parses a long.
		 *
		 * @param str
		 *                string to parse
		 * @return long value
		 * @throws ParseException
		 */
		private long parseLong(String str) throws ParseException {
			if (mode == Mode.PIPE) {
				// PIPE has values like "Default,5"
				int index = str.indexOf(",");
				if (index != -1) {
					str = str.substring(index + 1);
				}
			}
			try {
				return Long.parseLong(str);
			} catch (NumberFormatException e) {
				throw new ParseException("Cannot parse number " + str, e);
			}
		}

		/**
		 * Transforms the given input string to a string that is safe to
		 * use as an identifier in APT.
		 *
		 * @param input
		 *                input string
		 * @return input string with all illegal characters replaced by
		 *         "_"
		 */
		private String toSafeIdentifier(String input) {
			if (safeIdMap.containsKey(input)) {
				return safeIdMap.get(input);
			}

			String key = input;
			// Replace all generally illegal characters
			input = input.replaceAll("[^a-zA-Z0-9_]", "_");
			// Make sure first character is non-numeric
			input = input.replaceAll("^[0-9]", "_");
			// Guarantee uniqueness
			String unique = input;
			while (safeIdMap.containsKey(unique)) {
				unique = input + "_" + idCounter;
				idCounter += 1;
			}

			safeIdMap.put(key, unique);
			return input;
		}

	}

	@Override
	public String getFormat() {
		return FORMAT;
	}

	@Override
	public List<String> getFileExtensions() {
		return unmodifiableList(asList("pnml", "xml"));
	}

	@Override
	public PetriNet parse(InputStream is) throws ParseException, IOException {
		Parser parser = new Parser();
		return parser.parse(is);
	}

}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
