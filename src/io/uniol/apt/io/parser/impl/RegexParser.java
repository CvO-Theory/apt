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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import uniol.apt.adt.automaton.FiniteAutomaton;
import uniol.apt.adt.automaton.Symbol;
import uniol.apt.io.parser.AptParser;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.Parser;
import static uniol.apt.adt.automaton.FiniteAutomatonUtility.*;

/**
 * An antlr4 based parser for regular expressions
 *
 * @author vsp
 */
@AptParser
public class RegexParser extends AbstractParser<FiniteAutomaton> implements Parser<FiniteAutomaton> {
	public final static String FORMAT = "regex";

	private static class AlphabetListener extends RegexFormatParserBaseListener {
		private final Set<Symbol> alphabet;

		private AlphabetListener(Set<Symbol> alphabet) {
			this.alphabet = alphabet;
		}

		public void exitExprIdAtom(RegexFormatParser.ExprIdAtomContext ctx) {
			this.alphabet.add(new Symbol(ctx.ATOM().getText()));
		}

		public void exitExprIdId(RegexFormatParser.ExprIdIdContext ctx) {
			String id = ctx.ID().getText();
			this.alphabet.add(new Symbol(id.substring(1, id.length() - 1)));
		}
	}

	private static class RegexListener extends RegexFormatParserBaseListener {
		private final ParseTreeProperty<FiniteAutomaton> automatons;
		private final Set<Symbol> alphabet;
		private FiniteAutomaton automaton;

		private RegexListener(Set<Symbol> alphabet) {
			this.automatons = new ParseTreeProperty<>();
			this.automaton  = null;
			this.alphabet   = alphabet;
		}

		private FiniteAutomaton getAutomaton() {
			assert this.automaton != null;
			return this.automaton;
		}

		public void exitStart(RegexFormatParser.StartContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.expr());
			assert automaton != null;
			this.automaton = automaton;
		}

		public void exitExpr(RegexFormatParser.ExprContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.exprOr());
			assert automaton != null;
			this.automatons.put(ctx, automaton);
		}

		public void exitExprOr(RegexFormatParser.ExprOrContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.ex1);
			assert automaton != null;
			if (ctx.ex2 != null) {
				FiniteAutomaton automaton2 = this.automatons.get(ctx.ex2);
				assert automaton2 != null;
				automaton = union(automaton, automaton2);
			}
			this.automatons.put(ctx, automaton);
		}

		public void exitExprAnd(RegexFormatParser.ExprAndContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.ex1);
			assert automaton != null;
			if (ctx.ex2 != null) {
				FiniteAutomaton automaton2 = this.automatons.get(ctx.ex2);
				assert automaton2 != null;
				automaton = intersection(automaton, automaton2);
			}
			this.automatons.put(ctx, automaton);
		}

		public void exitExprConcat(RegexFormatParser.ExprConcatContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.ex1);
			assert automaton != null;
			if (ctx.ex2 != null) {
				FiniteAutomaton automaton2 = this.automatons.get(ctx.ex2);
				assert automaton2 != null;
				automaton = concatenate(automaton, automaton2);
			}
			this.automatons.put(ctx, automaton);
		}

		public void exitExprRepeatStar(RegexFormatParser.ExprRepeatStarContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.exprNegatePrefix());
			assert automaton != null;
			this.automatons.put(ctx, kleeneStar(automaton));
		}

		public void exitExprRepeatOpt(RegexFormatParser.ExprRepeatOptContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.exprNegatePrefix());
			assert automaton != null;
			this.automatons.put(ctx, optional(automaton));
		}

		public void exitExprRepeatPlus(RegexFormatParser.ExprRepeatPlusContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.exprNegatePrefix());
			assert automaton != null;
			this.automatons.put(ctx, kleenePlus(automaton));
		}

		public void exitExprRepeatExact(RegexFormatParser.ExprRepeatExactContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.exprNegatePrefix());
			assert automaton != null;
			int x = Integer.parseInt(ctx.x.getText());
			this.automatons.put(ctx, repeat(automaton, x, x));
		}

		public void exitExprRepeatLeast(RegexFormatParser.ExprRepeatLeastContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.exprNegatePrefix());
			assert automaton != null;
			int x = Integer.parseInt(ctx.x.getText());
			this.automatons.put(ctx, concatenate(repeat(automaton, x, x), kleeneStar(automaton)));
		}

		public void exitExprRepeatMinmax(RegexFormatParser.ExprRepeatMinmaxContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.exprNegatePrefix());
			assert automaton != null;
			int x = Integer.parseInt(ctx.x.getText());
			int y = Integer.parseInt(ctx.y.getText());
			try {
				this.automatons.put(ctx, repeat(automaton, x, y));
			} catch (IllegalArgumentException e) {
				throw new ParseRuntimeException("Invalid repetition specification, must satisfy min = "
						+ x + " <= " + y + " = max", e);
			}
		}

		public void exitExprRepeatNothing(RegexFormatParser.ExprRepeatNothingContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.exprNegatePrefix());
			assert automaton != null;
			this.automatons.put(ctx, automaton);
		}

		public void exitExprNPDirect(RegexFormatParser.ExprNPDirectContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.exprId());
			assert automaton != null;
			this.automatons.put(ctx, automaton);
		}

		public void exitExprNPNegate(RegexFormatParser.ExprNPNegateContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.exprId());
			assert automaton != null;
			automaton = negate(automaton, alphabet);
			this.automatons.put(ctx, automaton);
		}

		public void exitExprNPPrefix(RegexFormatParser.ExprNPPrefixContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.exprId());
			assert automaton != null;
			automaton = prefixClosure(automaton);
			this.automatons.put(ctx, automaton);
		}

		public void exitExprIdParentheses(RegexFormatParser.ExprIdParenthesesContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.expr());
			assert automaton != null;
			this.automatons.put(ctx, automaton);
		}

		public void exitExprIdAtom(RegexFormatParser.ExprIdAtomContext ctx) {
			this.automatons.put(ctx, getAtomicLanguage(new Symbol(ctx.ATOM().getText())));
		}

		public void exitExprIdId(RegexFormatParser.ExprIdIdContext ctx) {
			String id = ctx.ID().getText();
			this.automatons.put(ctx, getAtomicLanguage(new Symbol(id.substring(1, id.length() - 1))));
		}

		public void exitExprIdEmpty(RegexFormatParser.ExprIdEmptyContext ctx) {
			this.automatons.put(ctx, getEmptyLanguage());
		}

		public void exitExprIdEpsilon(RegexFormatParser.ExprIdEpsilonContext ctx) {
			this.automatons.put(ctx, getAtomicLanguage(Symbol.EPSILON));
		}
	}

	@Override
	public String getFormat() {
		return FORMAT;
	}

	@Override
	public List<String> getFileExtensions() {
		return Collections.emptyList();
	}

	@Override
	public FiniteAutomaton parse(InputStream is) throws ParseException, IOException {
		CharStream input         = new ANTLRInputStream(is);
		RegexFormatLexer lexer   = new RegexFormatLexer(input);
		lexer.removeErrorListeners(); // don't spam on stderr
		lexer.addErrorListener(new ThrowingErrorListener());
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		RegexFormatParser parser = new RegexFormatParser(tokens);
		parser.removeErrorListeners(); // don't spam on stderr
		parser.addErrorListener(new ThrowingErrorListener());
		parser.setBuildParseTree(true);
		ParseTree tree;
		try {
			tree             = parser.start();
		} catch (ParseRuntimeException ex) {
			throw ex.getParseException();
		}
		ParseTreeWalker walker   = new ParseTreeWalker();
		Set<Symbol> alphabet     = new HashSet<>();
		walker.walk(new AlphabetListener(alphabet), tree);
		RegexListener listener = new RegexListener(alphabet);
		try {
			walker.walk(listener, tree);
		} catch (ParseRuntimeException ex) {
			throw ex.getParseException();
		}

		return listener.getAutomaton();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
