grammar VeLa;

// VeLa: VStar expression Language

booleanExpression
:
	expression
	(
		(
			EQUAL
			| NOT_EQUAL
			| GREATER_THAN
			| LESS_THAN
			| GREATER_THAN_OR_EQUAL
			| LESS_THAN_OR_EQUAL
		) expression
	)* 
;

expression
:
	realExpression
	| stringExpression
;

realExpression
:
	multiplicativeExpression
	(
		(
			PLUS
			| MINUS
		) multiplicativeExpression
	)*
;

multiplicativeExpression
:
	unaryExpression
	(
		(
			MULT
			| DIV
		) unaryExpression
	)*
;

unaryExpression
:
	sign? numericFactor
;

numericFactor
:
	LPAREN realExpression RPAREN
	| real
	| func
;

stringExpression
:
	stringFactor (PLUS stringFactor)*
;

stringFactor
:
	string
	| func
;

func
:
	IDENT
	(
		LPAREN RPAREN
	)?
	| IDENT
	(
		LPAREN realExpression
		(
			COMMA realExpression
		)* RPAREN
	)?
;

// TODO: This rule needs to move to the lexer because it allows whitespace and 
// there's also a conflict with comma here and in the real number rule (may be 
// able to handle this via a non-greedy directive)

real
:
	DIGIT+
	(
		POINT DIGIT+
	)?
	(
		exponentIndicator MINUS? DIGIT+
	)?
	| POINT DIGIT+?
	(
		exponentIndicator MINUS? DIGIT+
	)?
;

// TODO: should be in lexer; this avoids [Ee] being treated as a subset of 
// LETTER, such that it would never be matched; unsatisfying! There must be 
// a better approach

exponentIndicator
:
	'E'
	| 'e'
;

sign
:
	MINUS
	| PLUS
;

string
:
	STRING
;

MINUS
:
	'-'
;

PLUS
:
	'+'
;

MULT
:
	'*'
;

DIV
:
	'/'
;

EQUAL
:
	'='
;

NOT_EQUAL
:
	'<>'
;

GREATER_THAN
:
	'>'
;

LESS_THAN
:
	'<'
;

GREATER_THAN_OR_EQUAL
:
	'>='
;

LESS_THAN_OR_EQUAL
:
	'<='
;

LPAREN
:
	'('
;

RPAREN
:
	')'
;

POINT
// Locale-inclusive

:
	'.'
	| ','
;

COMMA
:
	','
;

//QUOTE
//:
//	'\''
//;

DIGIT
:
	[0-9]
;

LETTER
:
	[A-Z]
	| [a-z]
;

UNDERSCORE
:
	'_'
;

IDENT
:
	(
		LETTER
		| UNDERSCORE
	)
	(
		LETTER
		| DIGIT
		| UNDERSCORE
	)*
;

STRING
:
	'"' ( ~'"' | '""' )* '"'
;

WS
:
	[ \r\t\n]+ -> skip
;