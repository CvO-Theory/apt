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

grammar AptPNFormat;

@header {
	package uniol.apt.io.parser.impl.apt;

	import uniol.apt.io.parser.IPNParserOutput;
	import uniol.apt.io.parser.IParserOutput.Type;
	import uniol.apt.io.parser.impl.MarkingHashMap;
	import java.util.HashMap;
	import java.util.Map;
	import uniol.apt.io.parser.impl.exception.StructureException;
}

@members {

	private IPNParserOutput<?> out;
	private enum State {INIT, FINAL, FLOW_PRE, FLOW_POST};
	private State status;

	// places and transitions
	private String id;
	private Map<String, Object> options = new HashMap<>();

	private String flowTransitionId;

	protected MarkingHashMap initMarking = new MarkingHashMap();
	protected MarkingHashMap finalMarking = new MarkingHashMap();

	private int name, type, description, init;

	private boolean suppressWarnings = false;

	public AptPNFormatParser(CommonTokenStream cts, IPNParserOutput<?> out) {
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

@lexer::header {
	package uniol.apt.io.parser.impl.apt;
}

@rulecatch {
	catch (RecognitionException re) {
		recover(input,re);
		throw re;
	}
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

start throws StructureException: (
		name { if(name == 1) { throw new StructureException("'.name' - identifier is used twice.");} else { ++name;} }
		| type { if(type == 1) { throw new StructureException("'.type' - identifier is used twice.");} else { ++type;} }
		| description { if(description == 1) { throw new StructureException("'.description' - identifier is used twice.");} else { ++description;} }
		| places | transitions | flows
		| initial_marking { if(init == 1) { throw new StructureException("'.initial_marking' - identifier is used twice.");} else { ++init;} }
		| final_markings
		)* EOF {if(type==0) throw new StructureException("'.type' - identifier not specified");};

/* NAME */
name: '.name' string {out.setName($string.val);};

/* TYPE */
type : '.type' typ;
typ  : 'LPN' { out.setType(Type.LPN);}
	| 'PN' {out.setType(Type.PN);};

/* DESCRIPTION */
description : '.description' (txt=string | txt=string_multi) { out.setDescription($txt.val);};

/* PLACES */
places     : '.places' place*;
place      :  idi { id = $idi.text; options = new HashMap<>();} (opts)? {out.addPlace(id, options, input); };

opts : '[' option (',' option)* ']';
option : ID '=' string {options.put($ID.text, $string.val);} | ID {options.put($ID.text, $ID.text);}
        | ID '=' INT {options.put($ID.text,Integer.parseInt($INT.text));};

/* TRANSITIONS */
transitions : '.transitions' transition*;
transition  : idi { id = $idi.text; options = new HashMap<>();} (opts)? {out.addTransition(id, options, input);};

/* FLOWS */
flows : '.flows' flow*;
flow  : idi {flowTransitionId = $idi.text;} ':' {status = State.FLOW_PRE;} set '->' {status = State.FLOW_POST;} set;

/* Sets of INT*ID */
set   : '{' ( | obj (',' obj)*) '}';
obj   : INT '*' idi {switch(status) {
	case INIT: initMarking.put($idi.text, Integer.parseInt($INT.text));
		   break;
	case FINAL:finalMarking.put($idi.text, Integer.parseInt($INT.text));
		   break;
	case FLOW_PRE: out.addFlow($idi.text, flowTransitionId, Integer.parseInt($INT.text));
			break;
	case FLOW_POST:out.addFlow(flowTransitionId, $idi.text, Integer.parseInt($INT.text));
			break; } }
			| idi {switch(status) {
			case INIT: initMarking.put($idi.text, 1);
				  break;
			case FINAL:finalMarking.put($idi.text, 1);
				  break;
			case FLOW_PRE: out.addFlow($idi.text, flowTransitionId, 1);
				      break;
			case FLOW_POST:out.addFlow(flowTransitionId, $idi.text, 1);
				      break; } };

/* INITIAL_MARKING */
initial_marking : '.initial_marking' { status = State.INIT; } (set)? {out.setInitialMarking(initMarking);} ;

/* FINAL_MARKINGS */
final_markings : '.final_markings' {status = State.FINAL;} final_marking*;
final_marking  : {finalMarking = new MarkingHashMap();} set {out.addFinalMarking(finalMarking);};

idi: ID | INT;

INT: '0'..'9'+;
ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;

string returns [String val] : STR { $val = $STR.text.substring(1,$STR.text.length() -1);};
string_multi returns [String val] : STR_MULTI { $val = $STR_MULTI.text.substring(1,$STR_MULTI.text.length() -1);};

COMMENT:   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
	|   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;};

WS  :   ( ' ' | '\n' | '\r' | '\t') {$channel=HIDDEN;} ;

STR: '"' ~('"' | '\n' | '\r' | '\t')*  '"';
STR_MULTI: '"' ~('"' | '\t' )*  '"';

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
