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
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import uniol.apt.adt.automaton.FiniteAutomaton;
import uniol.apt.adt.automaton.Symbol;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.Parser;
import static uniol.apt.adt.automaton.FiniteAutomatonUtility.*;

/**
 * An antlr4 based parser for regular expressions
 *
 * @author vsp
 */
public class RegexParser extends AbstractParser<FiniteAutomaton> implements Parser<FiniteAutomaton> {
	private static class AlphabetListener extends RegexFormatParserBaseListener {
		private final Set<Symbol> alphabet;

		private AlphabetListener(Set<Symbol> alphabet) {
			this.alphabet = alphabet;
		}

		public void exitExpr_id_atom(RegexFormatParser.Expr_id_atomContext ctx) {
			this.alphabet.add(new Symbol(ctx.ATOM().getText()));
		}

		public void exitExpr_id_id(RegexFormatParser.Expr_id_idContext ctx) {
			String id = ctx.ID().getText();
			this.alphabet.add(new Symbol(id.substring(1, id.length() - 1)));
		}
	}

	private static class RegexListener extends RegexFormatParserBaseListener {
		private final ParseTreeProperty<FiniteAutomaton> automatons;
		private final Set<Symbol> alphabet;
		FiniteAutomaton automaton;

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
			FiniteAutomaton automaton = this.automatons.get(ctx.expr_or());
			assert automaton != null;
			this.automatons.put(ctx, automaton);
		}

		public void exitExpr_or(RegexFormatParser.Expr_orContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.ex1);
			assert automaton != null;
			if (ctx.ex2 != null) {
				FiniteAutomaton automaton2 = this.automatons.get(ctx.ex2);
				assert automaton2 != null;
				automaton = union(automaton, automaton2);
			}
			this.automatons.put(ctx, automaton);
		}

		public void exitExpr_and(RegexFormatParser.Expr_andContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.ex1);
			assert automaton != null;
			if (ctx.ex2 != null) {
				FiniteAutomaton automaton2 = this.automatons.get(ctx.ex2);
				assert automaton2 != null;
				automaton = intersection(automaton, automaton2);
			}
			this.automatons.put(ctx, automaton);
		}

		public void exitExpr_concat(RegexFormatParser.Expr_concatContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.ex1);
			assert automaton != null;
			if (ctx.ex2 != null) {
				FiniteAutomaton automaton2 = this.automatons.get(ctx.ex2);
				assert automaton2 != null;
				automaton = concatenate(automaton, automaton2);
			}
			this.automatons.put(ctx, automaton);
		}

		public void exitExpr_repeat_star(RegexFormatParser.Expr_repeat_starContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.expr_negate());
			assert automaton != null;
			this.automatons.put(ctx, kleeneStar(automaton));
		}

		public void exitExpr_repeat_opt(RegexFormatParser.Expr_repeat_optContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.expr_negate());
			assert automaton != null;
			this.automatons.put(ctx, optional(automaton));
		}

		public void exitExpr_repeat_plus(RegexFormatParser.Expr_repeat_plusContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.expr_negate());
			assert automaton != null;
			this.automatons.put(ctx, kleenePlus(automaton));
		}

		public void exitExpr_repeat_exact(RegexFormatParser.Expr_repeat_exactContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.expr_negate());
			assert automaton != null;
			int x = Integer.parseInt(ctx.x.getText());
			this.automatons.put(ctx, repeat(automaton, x, x));
		}

		public void exitExpr_repeat_least(RegexFormatParser.Expr_repeat_leastContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.expr_negate());
			assert automaton != null;
			int x = Integer.parseInt(ctx.x.getText());
			this.automatons.put(ctx, concatenate(repeat(automaton, x, x), kleeneStar(automaton)));
		}

		public void exitExpr_repeat_minmax(RegexFormatParser.Expr_repeat_minmaxContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.expr_negate());
			assert automaton != null;
			int x = Integer.parseInt(ctx.x.getText());
			int y = Integer.parseInt(ctx.y.getText());
			this.automatons.put(ctx, repeat(automaton, x, y));
		}

		public void exitExpr_repeat_nothing(RegexFormatParser.Expr_repeat_nothingContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.expr_negate());
			assert automaton != null;
			this.automatons.put(ctx, automaton);
		}

		public void exitExpr_negate(RegexFormatParser.Expr_negateContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.expr_id());
			assert automaton != null;
			if (ctx.NEGATE() != null) {
				automaton = negate(automaton, alphabet);
			}
			this.automatons.put(ctx, automaton);
		}

		public void exitExpr_id_parentheses(RegexFormatParser.Expr_id_parenthesesContext ctx) {
			FiniteAutomaton automaton = this.automatons.get(ctx.expr());
			assert automaton != null;
			this.automatons.put(ctx, automaton);
		}

		public void exitExpr_id_atom(RegexFormatParser.Expr_id_atomContext ctx) {
			this.automatons.put(ctx, getAtomicLanguage(new Symbol(ctx.ATOM().getText())));
		}

		public void exitExpr_id_id(RegexFormatParser.Expr_id_idContext ctx) {
			String id = ctx.ID().getText();
			this.automatons.put(ctx, getAtomicLanguage(new Symbol(id.substring(1, id.length() - 1))));
		}

		public void exitExpr_id_empty(RegexFormatParser.Expr_id_emptyContext ctx) {
			this.automatons.put(ctx, getEmptyLanguage());
		}

		public void exitExpr_id_epsilon(RegexFormatParser.Expr_id_epsilonContext ctx) {
			this.automatons.put(ctx, getAtomicLanguage(Symbol.EPSILON));
		}
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
		} catch (ParseCancellationException ex) {
			throw new ParseException(ex.getMessage(), ex);
		}
		ParseTreeWalker walker   = new ParseTreeWalker();
		Set<Symbol> alphabet     = new HashSet<>();
		walker.walk(new AlphabetListener(alphabet), tree);
		RegexListener listener = new RegexListener(alphabet);
		walker.walk(listener, tree);

		return listener.getAutomaton();
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
