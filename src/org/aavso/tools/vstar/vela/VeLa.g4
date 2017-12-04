grammar VeLa;

// VeLa: VStar expression Language
//       -     -          -- 

// TODO:
// - let binding of any expression, including functions (HOFs)
//   selection in functions:
//     f <- fun(x:t1,y:t2,z:t3) -> expression-over-x,y,z
//   | fun(x:t1,y:t2,z:t3) -> (boolean-expression : expression-over-x,y,z ...)+
// - internal function representation in Models dialog should use VeLa

// ** Parser rules **

// A VeLa program consists of zero or more bindings or output 
// statements, or expressions. Variable bindings are immutable, 
// as is the operation of functions. VeLa identifiers and keywords
// are case-insensitive.

// The expression production will leave a value on the stack,
// therefore the program rule could be the entry point for 
// VStar filters as well as models. If one wishes to create 
// a filter that is a complete VeLa program that happens to
// end in a boolean expression, then that is permitted.

program
:
	(
		binding
		| namedFundef
		| out
		| expression
	)*
;

// The intention of the semantics are that within a given scope,
// a binding cannot be repeated without error.

binding
:
	symbol BACK_ARROW expression
;

// A named function definition, when invoked, introduces an additional 
// environment and allows all VeLa program elements operating over that 
// environment and its predecessors.

namedFundef
:
	FUN IDENT LPAREN formalParameter?
	(
		comma formalParameter
	)* RPAREN ARROW
	(
		program
	)
;

out
:
	OUT expression
	(
		COMMA expression
	)*
;

expression
:
	selectionExpression
	| booleanExpression
;

// Homage to Haskell/Scala/Erlang functional-style cases

selectionExpression
:
	SELECT
	(
		booleanExpression ARROW booleanExpression
	)+
;

booleanExpression
:
	conjunctiveExpression
	(
		OR conjunctiveExpression
	)*
;

conjunctiveExpression
:
	logicalNegationExpression
	(
		AND logicalNegationExpression
	)*
;

logicalNegationExpression
:
	NOT? relationalExpression
;

relationalExpression
:
	additiveExpression
	(
		(
			EQUAL
			| NOT_EQUAL
			| GREATER_THAN
			| LESS_THAN
			| GREATER_THAN_OR_EQUAL
			| LESS_THAN_OR_EQUAL
			| APPROXIMATELY_EQUAL
			| IN
		) additiveExpression
	)?
;

additiveExpression
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
	sign? exponentiationExpression
;

sign
:
	MINUS
	| PLUS
;

exponentiationExpression
:
// This rule option is right associative.
	< assoc = right > factor
	(
		(
			POW
		) factor
	)*
;

factor
:
	LPAREN expression RPAREN
	| integer
	| real
	| bool
	| string
	| list
	| symbol
	| anonFundef
	| funcall
;

integer
:
	INTEGER
;

real
:
	REAL
;

bool
:
	BOOLEAN
;

string
:
	STRING
;

list
:
	LBRACKET expression?
	(
		comma expression
	)* RBRACKET
;

symbol
:
	IDENT
;

// An anonymous function definition, when invoked, introduces an additional 
// environment and allows all VeLa program elements operating over that environment 
// and its predecessors.

anonFundef
:
	FUN LPAREN formalParameter?
	(
		comma formalParameter
	)* RPAREN ARROW
	(
		program
	)
;

// A formal parameter consists of a name-type pair

formalParameter
:
	IDENT COLON
	(
		INT_T
		| REAL_T
		| BOOL_T
		| STR_T
		| LIST_T
		| FUN
	)
;

// A function call consists of a function object followed 
// by zero or more parameters surrounded by parentheses.

funcall
:
	funobj LPAREN expression?
	(
		comma expression
	)* RPAREN
;

// IDENT corresponds to an explicit function name
// var allows a HOF (let binding or function parameter)
// fundef allows an anonymous function

funobj
:
	(
		IDENT
		| symbol
		| anonFundef
	)
;

comma
:
	COMMA
;

// ** Lexer rules **

SELECT
:
	[Ss] [Ee] [Ll] [Ee] [Cc] [Tt]
;

BACK_ARROW
:
	'<-'
;

OUT
:
	[Oo] [Uu] [Tt]
;

// Used for function definition and parameter type

FUN
:
	[Ff] [Uu] [Nn]
;

INT_T
:
	[Ii] [Nn] [Tt] [Ee] [Gg] [Ee] [Rr]
;

REAL_T
:
	[Rr] [Ee] [Aa] [Ll]
;

BOOL_T
:
	[Bb] [Oo] [Oo] [Ll]
;

STR_T
:
	[Ss] [Tt] [Rr] [Ii] [Nn] [Gg]
;

LIST_T
:
	[Ll] [Ii] [Ss] [Tt]
;

COLON
:
	':'
;

ARROW
:
	'->'
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

POW
:
	'^'
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

// Homage to Perl

APPROXIMATELY_EQUAL
:
	'=~'
;

// Homage to SQL, Python, ...

IN
:
	[Ii] [Nn]
;

LPAREN
:
	'('
;

RPAREN
:
	')'
;

LBRACKET
:
	'['
;

RBRACKET
:
	']'
;

PERIOD
:
	'.'
;

COMMA
:
	','
;

AND
:
	[Aa] [Nn] [Dd]
;

OR
:
	[Oo] [Rr]
;

NOT
:
	[Nn] [Oo] [Tt]
;

INTEGER
:
	DIGIT+
;

REAL
:
	DIGIT+
	(
		POINT DIGIT+
	)?
	(
		EXPONENT_INDICATOR MINUS? DIGIT+
	)?
	| POINT DIGIT+
	(
		EXPONENT_INDICATOR MINUS? DIGIT+
	)?
;

BOOLEAN
:
	TRUE
	| FALSE
;

fragment
TRUE
:
	'T'
	| 't'
;

fragment
FALSE
:
	'F'
	| 'f'
;

fragment
DIGIT
:
	[0-9]
;

fragment
POINT
// Locale-inclusive

:
	PERIOD
	| COMMA
;

fragment
EXPONENT_INDICATOR
:
	'E'
	| 'e'
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

fragment
LETTER
:
	[A-Z]
	| [a-z]
;

UNDERSCORE
:
	'_'
;

STRING
:
	'"'
	(
		~'"'
	)* '"'
;

WS
:
	[ \r\t\n]+ -> skip
;

COMMENT
:
// Could use channel(HIDDEN) instead of skip,
// e.g. https://stackoverflow.com/questions/23976617/parsing-single-line-comments
	'--' ~[\r\n]* -> skip
;