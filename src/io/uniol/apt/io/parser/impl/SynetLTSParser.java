/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2015       vsp
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
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import uniol.apt.adt.exception.DatastructureException;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.AptParser;
import uniol.apt.io.parser.Parser;
import uniol.apt.io.parser.ParseException;

/**
 * synet lts format parser
 *
 * @author vsp
 */
@AptParser
public class SynetLTSParser extends AbstractParser<TransitionSystem> implements Parser<TransitionSystem> {
	public final static String FORMAT = "synet";

	private static class StateListener extends SynetLTSFormatBaseListener implements SynetLTSFormatListener {
		private final TransitionSystem ts;
		private final Set<String> states;

		private StateListener(TransitionSystem ts) {
			this.ts     = ts;
			this.states = new HashSet<>();
		}

		@Override
		public void exitTs(SynetLTSFormatParser.TsContext ctx) {
			for (String state : states) {
				this.ts.createState(state);
			}
			this.ts.setInitialState(ctx.init.getText());
		}

		@Override
		public void exitTrans(SynetLTSFormatParser.TransContext ctx) {
			this.states.add(ctx.from.getText());
			this.states.add(ctx.to.getText());
		}
	}

	private static class ArcListener extends SynetLTSFormatBaseListener implements SynetLTSFormatListener {
		private final TransitionSystem ts;

		private ArcListener(TransitionSystem ts) {
			this.ts = ts;
		}

		@Override
		public void exitTrans(SynetLTSFormatParser.TransContext ctx) {
			this.ts.createArc(ctx.from.getText(), ctx.to.getText(), ctx.event.getText());
		}
	}

	@Override
	public String getFormat() {
		return FORMAT;
	}

	@Override
	public List<String> getFileExtensions() {
		return unmodifiableList(asList("aut"));
	}

	@Override
	public TransitionSystem parse(InputStream is) throws ParseException, IOException {
		CharStream input            = new ANTLRInputStream(is);
		SynetLTSFormatLexer lexer   = new SynetLTSFormatLexer(input);
		lexer.removeErrorListeners(); // don't spam on stderr
		lexer.addErrorListener(new ThrowingErrorListener());
		CommonTokenStream tokens    = new CommonTokenStream(lexer);
		SynetLTSFormatParser parser = new SynetLTSFormatParser(tokens);
		parser.removeErrorListeners(); // don't spam on stderr
		parser.addErrorListener(new ThrowingErrorListener());
		parser.setBuildParseTree(true);
		ParseTree tree;
		try {
			tree                = parser.ts();
		} catch (ParseRuntimeException ex) {
			throw ex.getParseException();
		}
		TransitionSystem ts         = new TransitionSystem();
		try {
			ParseTreeWalker.DEFAULT.walk(new StateListener(ts), tree);
			ParseTreeWalker.DEFAULT.walk(new ArcListener(ts), tree);
		} catch (DatastructureException ex) {
			throw new ParseException(ex.getMessage(), ex);
		}

		return ts;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
