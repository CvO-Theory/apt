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

package uniol.apt.io.parser.impl.pnml;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.parser.impl.exception.PNMLParserException;

/**
 * Reads P/T nets in PNML format
 *
 * This parser tries to deal with the quirks of different PNML "dialects,"
 * in particular those emitted by LoLA and PIPE.
 *
 * It is not very efficient and no efforts towards optimisation have been
 * made so far because the focus of APT lies on using the proprietary file
 * format and those of Petrify and Synet.
 *
 * @author Thomas Strathmann
 */
public class PNMLParser {

	public static PetriNet getPetriNet(String path) throws FileNotFoundException, IOException,
	PNMLParserException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new PNMLParserException("While trying to create an XML parser" +
					"object for reading the data you provided," + System.lineSeparator() +
					"the following exception occurred:" + e.getMessage());
		}
		Document dom;
		try {
			dom = dBuilder.parse(path);
		} catch (SAXException e) {
			throw new PNMLParserException("While trying to parse the XML data you provided," + System.lineSeparator() +
					"the following exception occurred:" + e.getMessage());
		}
		dom.getDocumentElement().normalize();
		
		NodeList places = dom.getElementsByTagName("place");
		NodeList transitions = dom.getElementsByTagName("transition");
		NodeList arcs = dom.getElementsByTagName("arc");
		
		PetriNet pn = new PetriNet();
		
		for(int i=0; i<places.getLength(); ++i) {
			Element node = (Element)places.item(i);
			String id = node.getAttribute("id");
			String name = id;
			Element nameElem = getElementByTagName("name", node);
			if(nameElem != null) {
				name = getTextOrValue(nameElem);
			}
			
			int marking = 0;
			Element markingElem  = getElementByTagName("initialMarking", node);
			if(markingElem != null) {
				String val = getTextOrValue(markingElem);
				if(val.contains(",")) {
					// for dealing with PIPE's token classes
					val = val.split(",")[1];
				}
				marking = Integer.parseInt(val);
			}
			
			Place p = pn.createPlace(name);
			p.setInitialToken(marking);
			p.putExtension("id", id);
		}
		
		for(int i=0; i<transitions.getLength(); ++i) {
			Element node = (Element)transitions.item(i);
			String id = node.getAttribute("id");
			String name = id;
			Element nameElem = getElementByTagName("name", node);
			if(nameElem != null) {
				name = getTextOrValue(nameElem);
			}
			
			Transition t = pn.createTransition(name);
			t.putExtension("id", id);
		}
		
		for(int i=0; i<arcs.getLength(); ++i) {
			Element node = (Element)arcs.item(i);
			String sourceId = node.getAttribute("source");
			String targetId = node.getAttribute("target");
			
			
			uniol.apt.adt.pn.Node source = pn.getNodeByExtension("id", sourceId);
			uniol.apt.adt.pn.Node target = pn.getNodeByExtension("id", targetId);
			
			int weight = 1;
			Element weightElem  = getElementByTagName("inscription", node);
			if(weightElem != null) {
				String val = getTextOrValue(weightElem);
				if(val.contains(",")) {
					// for dealing with PIPE's token classes
					val = val.split(",")[1];
				}
				weight = Integer.parseInt(val);
				if(weight == 0) {
					weight = 1;
				}
			}
			
			Flow a = pn.createFlow(source, target);
			a.setWeight(weight);
		}
		
		return pn;
	}
	
	private static String getTextOrValue(Element element) {
		String id = getTagValue("value", element);
		if(id == null)
			id = getTagValue("text", element);
		return id;
	}
	
	private static Element getElementByTagName(String name, Element element) {
		return (Element)element.getElementsByTagName(name).item(0);
	}
	
	private static String getTagValue(String tag, Element element) {
		NodeList tags = element.getElementsByTagName(tag);
		if(tags.getLength() != 1) {
			return null;
		} else {
			NodeList list = tags.item(0).getChildNodes();
			Node value = list.item(0);
			return value.getNodeValue();
		}
	}
	 
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
