/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2014-2015  vsp
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import uniol.apt.adt.exception.DatastructureException;
import uniol.apt.adt.extension.ExtensionProperty;
import uniol.apt.adt.extension.IExtensible;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.parser.AptParser;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.Parser;

/**
 * apt format parser
 *
 * @author vsp
 */
@AptParser
public class AptPNParser extends AbstractParser<PetriNet> implements Parser<PetriNet> {
	public final static String FORMAT = "apt";

	private static void handleOption(Map<String, Object> curOpts, AptPNFormatParser.OptionContext ctx) {
		Object val = ctx.ID().getText();

		if (ctx.STR() != null) {
			String str = ctx.STR().getText();
			val = str.substring(1, str.length() - 1);
		} else if (ctx.INT() != null) {
			val = Integer.parseInt(ctx.INT().getText());
		}

		curOpts.put(ctx.ID().getText(), val);
	}

	private static void putExtensions(IExtensible extensible, Map<String, Object> options) {
		if (options == null)
			return;

		// Extensible really needs a putExtensions method ...
		for (Map.Entry<String, Object> entry : options.entrySet()) {
			extensible.putExtension(entry.getKey(), entry.getValue(), ExtensionProperty.WRITE_TO_FILE);
		}
	}

	private static class NameDescPlaceTransitionListener extends AptPNFormatBaseListener
			implements AptPNFormatListener {
		private final PetriNet pn;
		private Map<String, Object> curOpts;

		private NameDescPlaceTransitionListener(PetriNet pn) {
			this.pn = pn;
		}

		@Override
		public void exitName(AptPNFormatParser.NameContext ctx) {
			String str = ctx.STR().getText();
			this.pn.setName(str.substring(1, str.length() - 1));
		}

		@Override
		public void exitDescription(AptPNFormatParser.DescriptionContext ctx) {
			String str = ctx.txt.getText();
			this.pn.putExtension("description", str.substring(1, str.length() - 1));
		}

		@Override
		public void enterNetOptions(AptPNFormatParser.NetOptionsContext ctx) {
			this.curOpts = new HashMap<>();
		}

		@Override
		public void exitNetOptions(AptPNFormatParser.NetOptionsContext ctx) {
			putExtensions(this.pn, curOpts);
			this.curOpts = null;
		}

		@Override
		public void enterPlace(AptPNFormatParser.PlaceContext ctx) {
			this.curOpts = new HashMap<>();
		}

		@Override
		public void enterTransition(AptPNFormatParser.TransitionContext ctx) {
			this.curOpts = new HashMap<>();
		}

		@Override
		public void exitOption(AptPNFormatParser.OptionContext ctx) {
			if (this.curOpts != null)
				handleOption(this.curOpts, ctx);
		}

		@Override
		public void exitPlace(AptPNFormatParser.PlaceContext ctx) {
			putExtensions(this.pn.createPlace(ctx.id.getText()), curOpts);
			this.curOpts = null;
		}

		@Override
		public void exitTransition(AptPNFormatParser.TransitionContext ctx) {
			Transition t = this.pn.createTransition(ctx.id.getText());

			if (this.curOpts == null)
				return;

			// Extensible really needs a putExtensions method ...
			for (Map.Entry<String, Object> entry : this.curOpts.entrySet()) {
				if ("label".equals(entry.getKey())) {
					// Why do we need this case? :-(
					t.setLabel(entry.getValue().toString());
				} else {
					t.putExtension(entry.getKey(), entry.getValue(),
							ExtensionProperty.WRITE_TO_FILE);
				}
			}

			this.curOpts = null;
		}
	}

	private static class FlowMarkingsListener extends AptPNFormatBaseListener implements AptPNFormatListener {
		private final PetriNet pn;
		private ParseTreeProperty<Map<String, Integer>> sets = new ParseTreeProperty<>();
		private MarkingHashMap curSet;
		private Map<String, Object> curOpts;

		private FlowMarkingsListener(PetriNet pn) {
			this.pn = pn;
		}

		@Override
		public void enterSet(AptPNFormatParser.SetContext ctx) {
			this.curSet = new MarkingHashMap();
			this.sets.put(ctx, this.curSet);
		}

		@Override
		public void exitSet(AptPNFormatParser.SetContext ctx) {
			this.curSet = null;
		}

		@Override
		public void exitObj(AptPNFormatParser.ObjContext ctx) {
			assert this.curSet != null;

			int mult = 1;
			if (ctx.mult != null) {
				mult = Integer.parseInt(ctx.mult.getText());
			}
			this.curSet.put(ctx.id.getText(), mult);
		}

		@Override
		public void enterFlow(AptPNFormatParser.FlowContext ctx) {
			this.curOpts = new HashMap<>();
		}

		@Override
		public void exitOption(AptPNFormatParser.OptionContext ctx) {
			if (this.curOpts != null)
				handleOption(this.curOpts, ctx);
		}

		@Override
		public void exitFlow(AptPNFormatParser.FlowContext ctx) {
			Map<String, Integer> preset  = this.sets.get(ctx.preset);
			Map<String, Integer> postset = this.sets.get(ctx.postset);

			for (Map.Entry<String, Integer> entry : preset.entrySet()) {
				putExtensions(this.pn.createFlow(entry.getKey(), ctx.id.getText(), entry.getValue()),
						curOpts);
			}

			for (Map.Entry<String, Integer> entry : postset.entrySet()) {
				putExtensions(this.pn.createFlow(ctx.id.getText(), entry.getKey(), entry.getValue()),
						curOpts);
			}

			this.curOpts = null;
		}

		@Override
		public void exitInitialMarking(AptPNFormatParser.InitialMarkingContext ctx) {
			if (ctx.set() == null)
				return;

			Map<String, Integer> marking = this.sets.get(ctx.set());
			this.pn.setInitialMarking(new Marking(this.pn, marking));
		}

		@Override
		public void exitFinalMarkings(AptPNFormatParser.FinalMarkingsContext ctx) {
			if (ctx.set() == null)
				return;

			for (AptPNFormatParser.SetContext setCtx : ctx.set()) {
				Map<String, Integer> marking = this.sets.get(setCtx);
				this.pn.addFinalMarking(new Marking(this.pn, marking));
			}
		}
	}

	@Override
	public String getFormat() {
		return FORMAT;
	}

	@Override
	public List<String> getFileExtensions() {
		return unmodifiableList(asList("apn", "apt"));
	}

	@Override
	public PetriNet parse(InputStream is) throws ParseException, IOException {
		CharStream input         = new ANTLRInputStream(is);
		AptPNFormatLexer lexer   = new AptPNFormatLexer(input);
		lexer.removeErrorListeners(); // don't spam on stderr
		lexer.addErrorListener(new ThrowingErrorListener());
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		AptPNFormatParser parser = new AptPNFormatParser(tokens);
		parser.removeErrorListeners(); // don't spam on stderr
		parser.addErrorListener(new ThrowingErrorListener());
		parser.setBuildParseTree(true);
		ParseTree tree;
		try {
			tree             = parser.pn();
		} catch (ParseRuntimeException ex) {
			throw ex.getParseException();
		}
		PetriNet pn = new PetriNet();
		try {
			ParseTreeWalker.DEFAULT.walk(new NameDescPlaceTransitionListener(pn), tree);
			ParseTreeWalker.DEFAULT.walk(new FlowMarkingsListener(pn), tree);
		} catch (DatastructureException ex) {
			throw new ParseException(ex.getMessage(), ex);
		}

		return pn;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
