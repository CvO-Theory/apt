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

grammar AptLTSFormat;

@header {
	package uniol.apt.io.parser.impl.apt;

	import uniol.apt.io.parser.ILTSParserOutput;
	import uniol.apt.io.parser.IParserOutput.Type;
	import java.util.HashMap;
	import java.util.Map;
	import uniol.apt.io.parser.impl.exception.StructureException;
}

@members {
	private ILTSParserOutput<?> out;

	private String id;
	private Map<String, Object> options = new HashMap<>();

	private int name, type, description;

	private boolean suppressWarnings = false;

	public AptLTSFormatParser(CommonTokenStream cts, ILTSParserOutput<?> out) {
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
	package uniol.apt.io.parser.impl.apt;
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
		 name {
				if(name == 1) {
					throw new StructureException("'.name' - identifier is used twice.");
				} else {
					 ++name;
				}
			}
		| type {
				if(type == 1) {
					throw new StructureException("'.type' - identifier is used twice.");
				} else {
					 ++type;
				}
			}
		| description {
				if(description == 1) {
					throw new StructureException("'.description' - identifier is used twice.");
				} else {
					++description;
				}
			}
		| states | labels | arcs
	)* EOF {
		if(type==0) {
			throw new StructureException("'.type' - identifier not specified");
		}
	};


/* NAME */
name		: '.name' string {out.setName($string.val);} ;

/* TYPE */
type		: '.type' 'LTS' {out.setType(Type.LTS);};

/* DESCRIPTION */
description	: '.description' (txt=string | txt=string_multi) { out.setDescription($txt.val);};

/* STATES */
states		: '.states' state*;
state		: idi {
				id = $idi.text; options = new HashMap<>();
				}
		(opts)? {
				out.addState(id, options, input);
				 };

opts		: '[' option (',' option)* ']';
option		: ID '=' string {
				options.put($ID.text, $string.val);
				}		 
                   | ID {
                                options.put($ID.text, "true");
                                }
                    | ID '=' INT {
				options.put($ID.text, Integer.parseInt($INT.text));
				};

/* LABELS */
labels		: '.labels' label*;
label		: idi {
				id =  $idi.text; options = new HashMap<>();
				}
		(opts)? {
				out.addLabel(id, options);
				};

/* ARCS */
arcs		: '.arcs' arc*;
arc		: id1=idi id2=idi id3=idi {
						out.addArc($id1.text, $id2.text, $id3.text);
						};

idi		: ID | INT;
string returns [String val]: STR {$val = $STR.text.substring(1,$STR.text.length() -1);};
string_multi returns [String val]: STR_MULTI {$val = $STR_MULTI.text.substring(1,$STR_MULTI.text.length() -1);};

INT		: '0'..'9'+;
ID		: ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;
COMMENT		: '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
			|   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;};
WS		: ( ' ' | '\n' | '\r' | '\t')+ {$channel=HIDDEN;};
STR		: '"' ~('"' | '\n' | '\r' | '\t')*  '"';
STR_MULTI	: '"' ~('"' | '\t' )*  '"';

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
