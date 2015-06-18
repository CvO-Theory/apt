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

grammar SynetPNFormat;

@header {
	package uniol.apt.io.parser.impl.synet;

	import uniol.apt.io.parser.IPNParserOutput;
	import uniol.apt.io.parser.IParserOutput.Type;
	import java.util.HashMap;
	import java.util.Map;
}

@members {
	private IPNParserOutput<?> out;
	private Map<String, Integer> initMarking = new HashMap<>();
	private boolean suppressWarnings = false;

	public SynetPNFormatParser(CommonTokenStream cts, IPNParserOutput<?> out) {
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
	package uniol.apt.io.parser.impl.synet;
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

start	     : statementlist {out.setType(Type.LPN); out.setInitialMarking(initMarking); };
statementlist: | statement statementlist;
statement    : location | transition | place | flow ;
location     : 'location' locationid;
transition   : 'transition' eventid ('::' locationid)?
                  {
			 Map<String, Object> op = new HashMap<>();
			 if($locationid.text!=null) { op.put("location",$locationid.text);}
                         out.addTransition($eventid.text,op,input);
			};
place        : 'place' placeid (':=' INT {initMarking.put($placeid.text,Integer.parseInt($INT.text));})?
			('::' locationid)?
                   {
			Map<String, Object> op = new HashMap<>();
			if($locationid.text!=null) { op.put("location",$locationid.text);}
			out.addPlace($placeid.text,op,input);
			};
flow         : 'flow' placeid '--' ( INT )? '->' eventid
                    {
			out.addFlow($placeid.text, $eventid.text, ($INT.text != null)? Integer.parseInt($INT.text):1);
			 }
                | 'flow' placeid '<-' ( INT )? '--' eventid
                    {
			out.addFlow($eventid.text, $placeid.text, ($INT.text != null)? Integer.parseInt($INT.text):1);
			 };

eventid      : INT | STR | ID ;
locationid   : INT | STR | ID ;
placeid      : INT | STR | ID ;

INT	     : '0'..'9'+;

ID	     : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;

COMMENT	     : '//' ~('\n'|'\r')* {$channel=HIDDEN;}
		  |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;};

WS	     : ( ' ' | '\n' | '\r' | '\t') {$channel=HIDDEN;};

STR	     : '"' ( ESC_SEQ | ~('\\'|'"') )* '"';

fragment
ESC_SEQ	     : '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\');

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
