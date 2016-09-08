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
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import uniol.apt.adt.exception.DatastructureException;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.AptParser;
import uniol.apt.io.parser.Parser;
import uniol.apt.io.parser.ParseException;

/**
 * Petrify LTS format parser
 *
 * @author Uli Schlachter
 */
@AptParser
public class PetrifyLTSParser extends AbstractParser<TransitionSystem> implements Parser<TransitionSystem> {
	public final static String FORMAT = "petrify";

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
					throw new ParseRuntimeException("Duplicate input '" + event.getText() + "'");
		}

		// Create a state if it does not yet exist
		private void createState(String id) {
			if (!lts.containsState(id))
				lts.createState(id);
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
					throw new ParseRuntimeException("Unknown event '" + event + "'");
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
	public String getFormat() {
		return FORMAT;
	}

	@Override
	public List<String> getFileExtensions() {
		return unmodifiableList(asList("g"));
	}

	@Override
	public TransitionSystem parse(InputStream is) throws ParseException, IOException {
		CharStream input = new ANTLRInputStream(is);
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
		} catch (ParseRuntimeException ex) {
			throw ex.getParseException();
		}
		TransitionSystem lts = new TransitionSystem();
		try {
			ParseTreeWalker.DEFAULT.walk(new LTSListener(lts), tree);
		} catch (ParseRuntimeException ex) {
			throw ex.getParseException();
		} catch (DatastructureException ex) {
			throw new ParseException(ex.getMessage(), ex);
		}
		return lts;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
