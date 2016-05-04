/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014-2015  vsp
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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import uniol.apt.adt.exception.DatastructureException;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.io.parser.AptParser;
import uniol.apt.io.parser.Parser;
import uniol.apt.io.parser.ParseException;
import uniol.apt.util.Pair;

/**
 * Petrify PN format parser
 *
 * @author Uli Schlachter
 */
@AptParser
public class PetrifyPNParser extends AbstractParser<PetriNet> implements Parser<PetriNet> {
	public final static String FORMAT = "petrify";

	private static class PNListener extends PetrifyPNFormatParserBaseListener
			implements PetrifyPNFormatParserListener {
		private final PetriNet pn;
		private final Map<Pair<String, String>, Place> implicitPlaces = new HashMap<>();
		private final ParseTreeProperty<String> nodeIds = new ParseTreeProperty<>();

		private PNListener(PetriNet pn) {
			this.pn = pn;
		}

		@Override
		public void exitModel(PetrifyPNFormatParser.ModelContext ctx) {
			pn.setName(ctx.NAME().getText());
		}

		@Override
		public void exitTransitions(PetrifyPNFormatParser.TransitionsContext ctx) {
			for (TerminalNode event : ctx.ID())
				pn.createTransition(event.getText());
		}

		@Override
		public void exitFlow(PetrifyPNFormatParser.FlowContext ctx) {
			String source = nodeIds.get(ctx.source);
			boolean sourceIsTransition = pn.containsTransition(source);
			if (!sourceIsTransition && !pn.containsPlace(source))
				pn.createPlace(source);

			for (PetrifyPNFormatParser.Flow_targetContext targetCtx : ctx.flow_target()) {
				int weight = 1;
				if (targetCtx.INT() != null) {
					weight = Integer.parseInt(targetCtx.INT().getText());
				}
				String target = nodeIds.get(targetCtx.event());
				if (sourceIsTransition && pn.containsTransition(target)) {
					// Create an implicit place
					Pair<String, String> pair = new Pair<>(source, target);
					String placeName = "<" + source + "," + target + ">";
					implicitPlaces.put(pair, pn.createPlace(placeName));
					pn.createFlow(source, placeName, weight);
					pn.createFlow(placeName, target, weight);
				} else {
					// This is an explicit place, figure out which ID is the transition
					if (sourceIsTransition) {
						if (!pn.containsPlace(target))
							pn.createPlace(target);
					} else if (!pn.containsTransition(target))
						throw new ParseRuntimeException(
								"Tried to create arc between two places '"
								+ source + "' and '" + target + "'");
					pn.createFlow(source, target, weight);
				}
			}
		}

		@Override
		public void exitTokenExplicitPlace(PetrifyPNFormatParser.TokenExplicitPlaceContext ctx) {
			Place p = pn.getPlace(ctx.ID().getText());
			if (p.getInitialToken().getValue() != 0)
				throw new ParseRuntimeException("Duplicate initial marking for place '"
						+ p.getId() + "'");
			int initial = 1;
			if (ctx.INT() != null) {
				initial = Integer.parseInt(ctx.INT().getText());
			}
			p.setInitialToken(initial);
		}

		@Override
		public void exitTokenImplicitPlace(PetrifyPNFormatParser.TokenImplicitPlaceContext ctx) {
			String source = ctx.source.getText();
			String target = ctx.target.getText();
			Place p = implicitPlaces.get(new Pair<>(source, target));
			if (p == null)
				throw new ParseRuntimeException("There is no implicit place"
							+ " between '" + source + "' and '" + target
							+ "' whose initial marking can be set");
			if (p.getInitialToken().getValue() != 0)
				throw new ParseRuntimeException(
						"Duplicate initial marking for place '" + p.getId() + "'");
			int initial = 1;
			if (ctx.INT() != null) {
				initial = Integer.parseInt(ctx.INT().getText());
			}
			p.setInitialToken(initial);
		}

		@Override
		public void exitEventUnsplit(PetrifyPNFormatParser.EventUnsplitContext ctx) {
			nodeIds.put(ctx, ctx.ID().getText());
		}

		@Override
		public void exitEventSplit(PetrifyPNFormatParser.EventSplitContext ctx) {
			String id = ctx.EVENT().getText();

			// An event a can be split into a/1, a/2 etc. a/0 is equivalent to a
			int index = id.indexOf('/');
			String label = id.substring(0, index);
			int number = Integer.parseInt(id.substring(index + 1));

			if (number == 0)
				nodeIds.put(ctx, label);
			else {
				if (!pn.containsTransition(label))
					throw new ParseRuntimeException("A non-existent event was split in '" + id
							+ "'");
				if (!pn.containsTransition(id))
					pn.createTransition(id).setLabel(label);
				nodeIds.put(ctx, id);
			}
		}
	}

	@Override
	public String getFormat() {
		return FORMAT;
	}

	@Override
	public List<String> getFileExtensions() {
		return unmodifiableList(asList("g"));
	}

	@Override
	public PetriNet parse(InputStream is) throws ParseException, IOException {
		CharStream input = new ANTLRInputStream(is);
		PetrifyPNFormatLexer lexer = new PetrifyPNFormatLexer(input);
		lexer.removeErrorListeners(); // don't spam on stderr
		lexer.addErrorListener(new ThrowingErrorListener());
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		PetrifyPNFormatParser parser = new PetrifyPNFormatParser(tokens);
		parser.removeErrorListeners(); // don't spam on stderr
		parser.addErrorListener(new ThrowingErrorListener());
		parser.setBuildParseTree(true);
		ParseTree tree;
		try {
			tree = parser.pn();
		} catch (ParseRuntimeException ex) {
			throw ex.getParseException();
		}
		PetriNet pn = new PetriNet();
		try {
			ParseTreeWalker.DEFAULT.walk(new PNListener(pn), tree);
		} catch (ParseRuntimeException ex) {
			throw ex.getParseException();
		} catch (DatastructureException ex) {
			throw new ParseException(ex.getMessage(), ex);
		}

		return pn;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
