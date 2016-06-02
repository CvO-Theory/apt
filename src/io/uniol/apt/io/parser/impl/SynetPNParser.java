/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015  vsp
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import uniol.apt.adt.exception.DatastructureException;
import uniol.apt.adt.extension.Extensible;
import uniol.apt.adt.extension.ExtensionProperty;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.parser.AptParser;
import uniol.apt.io.parser.Parser;
import uniol.apt.io.parser.ParseException;

/**
 * Petri net parser for the synet Petri Net format
 *
 * @author vsp
 */
@AptParser
public class SynetPNParser extends AbstractParser<PetriNet> implements Parser<PetriNet> {
	public final static String FORMAT = "synet";

	private static class LocationListener extends SynetPNFormatBaseListener implements SynetPNFormatListener {
		private final Set<String> locations;

		private LocationListener(Set<String> locations) {
			this.locations = locations;
		}

		@Override
		public void exitLocation(SynetPNFormatParser.LocationContext ctx) {
			String loc = ctx.loc.getText();

			if (!this.locations.add(loc))
				throw new ParseRuntimeException("Location '" + loc + "' already exists");
		}
	}

	private static class PlaceTransitionListener extends SynetPNFormatBaseListener
			implements SynetPNFormatListener {
		private final PetriNet pn;
		private final Set<String> locations;

		private PlaceTransitionListener(PetriNet pn, Set<String> locations) {
			this.pn        = pn;
			this.locations = locations;
		}

		private static String extractPosition(ParserRuleContext ctx) {
			Token token = ctx.getStart();
			return "line " + token.getLine() + " pos " + token.getCharPositionInLine();
		}

		private void handleLocation(Extensible ext, SynetPNFormatParser.IdContext locCtx,
				ParserRuleContext parentCtx) {
			if (locCtx == null) {
				if (!this.locations.isEmpty()) {
					throw new ParseRuntimeException(extractPosition(parentCtx)
							+ " Missing Location");
				} else {
					return;
				}
			}

			String locText = locCtx.getText();
			if (!this.locations.contains(locText))
				throw new ParseRuntimeException(extractPosition(locCtx)
							+ " Unknown Location '" + locText + "'");
			ext.putExtension("location", locText, ExtensionProperty.WRITE_TO_FILE);
		}

		@Override
		public void exitPlace(SynetPNFormatParser.PlaceContext ctx) {
			Place p = this.pn.createPlace(ctx.p.getText());
			handleLocation(p, ctx.loc, ctx);

			if (ctx.init != null) {
				long init = Integer.parseInt(ctx.init.getText());
				p.setInitialToken(init);
			}
		}

		@Override
		public void exitTransition(SynetPNFormatParser.TransitionContext ctx) {
			Transition t = this.pn.createTransition(ctx.t.getText());
			handleLocation(t, ctx.loc, ctx);
		}
	}

	private static class FlowListener extends SynetPNFormatBaseListener implements SynetPNFormatListener {
		private final PetriNet pn;

		private FlowListener(PetriNet pn) {
			this.pn = pn;
		}

		@Override
		public void exitFlowPreset(SynetPNFormatParser.FlowPresetContext ctx) {
			int weight = 1;
			if (ctx.w != null)
				weight = Integer.parseInt(ctx.w.getText());

			this.pn.createFlow(ctx.t.getText(), ctx.p.getText(), weight);
		}

		@Override
		public void exitFlowPostset(SynetPNFormatParser.FlowPostsetContext ctx) {
			int weight = 1;
			if (ctx.w != null)
				weight = Integer.parseInt(ctx.w.getText());

			this.pn.createFlow(ctx.p.getText(), ctx.t.getText(), weight);
		}
	}

	@Override
	public String getFormat() {
		return FORMAT;
	}

	@Override
	public List<String> getFileExtensions() {
		return unmodifiableList(asList("net"));
	}

	@Override
	public PetriNet parse(InputStream is) throws ParseException, IOException {
		CharStream input           = new ANTLRInputStream(is);
		SynetPNFormatLexer lexer   = new SynetPNFormatLexer(input);
		lexer.removeErrorListeners(); // don't spam on stderr
		lexer.addErrorListener(new ThrowingErrorListener());
		CommonTokenStream tokens   = new CommonTokenStream(lexer);
		SynetPNFormatParser parser = new SynetPNFormatParser(tokens);
		parser.removeErrorListeners(); // don't spam on stderr
		parser.addErrorListener(new ThrowingErrorListener());
		parser.setBuildParseTree(true);
		ParseTree tree;
		try {
			tree               = parser.pn();
		} catch (ParseRuntimeException ex) {
			throw ex.getParseException();
		}
		PetriNet pn                = new PetriNet();
		Set<String> locations      = new HashSet<>();
		try {
			ParseTreeWalker.DEFAULT.walk(new LocationListener(locations), tree);
			ParseTreeWalker.DEFAULT.walk(new PlaceTransitionListener(pn, locations), tree);
			ParseTreeWalker.DEFAULT.walk(new FlowListener(pn), tree);
		} catch (ParseRuntimeException ex) {
			throw ex.getParseException();
		} catch (DatastructureException ex) {
			throw new ParseException(ex.getMessage(), ex);
		}

		return pn;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
