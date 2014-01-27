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

// @author Manuel Gieseking

grammar PetrifyLTSFormat;

@header {
	package uniol.apt.io.parser.impl.petrify;

	import uniol.apt.io.parser.ILTSParserOutput;
	import uniol.apt.io.parser.IParserOutput.Type;
	import java.util.HashMap;
	import java.util.Map;
}

@members {
	private ILTSParserOutput<?> out;
	private Map<String, String> options = new HashMap<>();
	private boolean suppressWarnings = false;

	public PetrifyLTSFormatParser(CommonTokenStream cts, ILTSParserOutput<?> out) {
		this(cts);
		this.out = out;
	}


	public void suppressWarnings(boolean flag) {
		suppressWarnings = flag;
	}

	@Override
	public void emitErrorMessage(String msg) {
		if(!suppressWarnings) {
			System.out.println("[WARNING] Parser: " + msg);
		}
	}
}

@rulecatch {
	catch (RecognitionException re) {
		recover(input,re);
		throw re;
	}
}

@lexer::header {
	package uniol.apt.io.parser.impl.petrify;
}

@lexer::members {
	private boolean suppressWarnings = false;

	public void suppressWarnings(boolean flag) {
		suppressWarnings = flag;
	}

	@Override
	public void emitErrorMessage(String msg) {
		if(!suppressWarnings) {
			System.out.println("[WARNING] Lexer: " + msg);
		}
	}
}

start    : model inputs state translist marking end {out.setType(Type.LTS);};
model    : '.model' NAME=(INT|STR|ID)* {out.setName($NAME.text);};
inputs   : '.inputs' (INT|ID)*;
state    : '.state graph';
translist: trans*;
trans    : from=stateid eventid to=stateid {out.addArc($from.text, $eventid.text, $to.text);};
stateid  : INT {out.addState($INT.text, options, input);} | STR {out.addState($STR.text, options, input);}
		 | ID {out.addState($ID.text, options, input);};
eventid  : INT {out.addLabel($INT.text, options);} | STR {out.addLabel($STR.text, options);}
		 | ID {out.addLabel($ID.text, options);};
marking  : '.marking' '{' st=(INT|STR|ID) {out.setInitialState($st.text);} '}';
end      : '.end';

ID	 : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;

INT	 : '0'..'9'+;

COMMENT  : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
		|   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;};

WS	 : ( ' ' | '\n' | '\r' | '\t') {$channel=HIDDEN;} ;

STR	 : '"' ( ESC_SEQ | ~('\\'|'"') )* '"';

fragment
ESC_SEQ	 : '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\') ;

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
