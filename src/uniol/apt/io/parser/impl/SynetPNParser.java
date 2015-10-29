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
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import uniol.apt.adt.exception.DatastructureException;
import uniol.apt.adt.extension.Extensible;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.parser.PNParser;
import uniol.apt.io.parser.ParseException;

/**
 * Petri net parser for the synet Petri Net format
 *
 * @author vsp
 */
public class SynetPNParser extends AbstractPNParser implements PNParser {
	private static class LocationListener extends SynetPNFormatBaseListener implements SynetPNFormatListener {
		private final Set<String> locations;

		private LocationListener(Set<String> locations) {
			this.locations = locations;
		}

		@Override
		public void exitLocation(SynetPNFormatParser.LocationContext ctx) {
			String loc = ctx.loc.getText();

			if (!this.locations.add(loc))
				throw new ParseCancellationException(new ParseException("Location already exists."));
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
					throw new ParseCancellationException(extractPosition(parentCtx)
							+ " Missing Location");
				} else {
					return;
				}
			}

			String locText = locCtx.getText();
			if (!this.locations.contains(locText))
				throw new ParseCancellationException(new ParseException(extractPosition(locCtx)
							+ " Unknown Location"));
			ext.putExtension("location", locText);
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
	public PetriNet parsePN(InputStream is) throws ParseException {
		CharStream input;
		try {
			input              = new ANTLRInputStream(is);
		} catch (IOException ex) {
			throw new ParseException(ex.getMessage(), ex);
		}
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
		} catch (ParseCancellationException ex) {
			throw new ParseException(ex.getMessage(), ex);
		}
		PetriNet pn                = new PetriNet();
		Set<String> locations      = new HashSet<>();
		try {
			ParseTreeWalker.DEFAULT.walk(new LocationListener(locations), tree);
			ParseTreeWalker.DEFAULT.walk(new PlaceTransitionListener(pn, locations), tree);
			ParseTreeWalker.DEFAULT.walk(new FlowListener(pn), tree);
		} catch (DatastructureException | ParseCancellationException ex) {
			throw new ParseException(ex.getMessage(), ex);
		}

		return pn;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120