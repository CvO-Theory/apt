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
import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import uniol.apt.adt.exception.DatastructureException;
import uniol.apt.adt.exception.NodeExistsException;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.LTSParser;
import uniol.apt.io.parser.ParseException;

/**
 * Petrify LTS format parser
 *
 * @author Uli Schlachter
 */
public class PetrifyLTSParser extends AbstractLTSParser implements LTSParser {
	private static class LTSListener extends PetrifyLTSFormatParserBaseListener
			implements PetrifyLTSFormatParserListener {
		private final TransitionSystem lts;
		private final Set<String> alphabet = new HashSet<>();

		private LTSListener(TransitionSystem lts) {
			this.lts = lts;
		}

		@Override
		public void exitModel(PetrifyLTSFormatParser.ModelContext ctx) {
			lts.setName(ctx.NAME().getText());
		}

		@Override
		public void exitInputs(PetrifyLTSFormatParser.InputsContext ctx) {
			for (TerminalNode event : ctx.ID())
				if (!this.alphabet.add(event.getText()))
					throw new ParseCancellationException(new ParseException(
								"Duplicate input " + event.getText()));
		}

		// Create a state if it does not yet exist
		private void createState(String id) {
			try {
				lts.createState(id);
			} catch (NodeExistsException e) {
				// TODO: Add TransitionSystem#containsState()
			}
		}

		@Override
		public void exitTransition(PetrifyLTSFormatParser.TransitionContext ctx) {
			String source = ctx.first.getText();
			createState(source);

			for (PetrifyLTSFormatParser.TargetContext targetCtx : ctx.target()) {
				String event = targetCtx.event.getText();
				String target = targetCtx.next.getText();
				createState(target);

				if (!this.alphabet.contains(event))
					throw new ParseCancellationException(new ParseException(
								"Unknown event " + event));
				lts.createArc(source, target, event);

				source = target;
			}
		}

		@Override
		public void exitMarking(PetrifyLTSFormatParser.MarkingContext ctx) {
			lts.setInitialState(ctx.ID().getText());
		}
	}

	@Override
	public TransitionSystem parseLTS(InputStream is) throws ParseException {
		CharStream input;
		try {
			input = new ANTLRInputStream(is);
		} catch (IOException ex) {
			throw new ParseException(ex);
		}
		PetrifyLTSFormatLexer lexer = new PetrifyLTSFormatLexer(input);
		lexer.removeErrorListeners(); // don't spam on stderr
		lexer.addErrorListener(new ThrowingErrorListener());
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		PetrifyLTSFormatParser parser = new PetrifyLTSFormatParser(tokens);
		parser.removeErrorListeners(); // don't spam on stderr
		parser.addErrorListener(new ThrowingErrorListener());
		parser.setBuildParseTree(true);
		ParseTree tree;
		try {
			tree = parser.ts();
		} catch (ParseCancellationException ex) {
			throw new ParseException(ex);
		}
		TransitionSystem lts = new TransitionSystem();
		try {
			ParseTreeWalker.DEFAULT.walk(new LTSListener(lts), tree);
		} catch (DatastructureException | ParseCancellationException ex) {
			throw new ParseException(ex);
		}
		return lts;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
