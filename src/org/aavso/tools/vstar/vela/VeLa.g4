grammar VeLa;

// VeLa: VStar expression Language
//       -     -          -- 

// TODO:
//   selection in functions:
//     f <- fun(x:t1,y:t2,z:t3) -> expression-over-x,y,z
//   | fun(x:t1,y:t2,z:t3) -> (boolean-expression : expression-over-x,y,z ...)+
// - let binding of any expression, including functions (HOFs)
// - print statement for higher-level use of VeLa with LLVM/JVM
// - internal function representation in Models dialog should use VeLa
// - comments (-- or #)

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
	(binding | out | expression)*
;

// The intention of the semantics are that within a given scope,
// a binding cannot be repeated without error.
binding
:
	IDENT BACK_ARROW expression
;

out
:
	OUT expression (COMMA expression)*
;

expression
:
	selectionExpression
	| booleanExpression
;

// Homage to Haskell/Scala/Erlang functional-style cases
selectionExpression
:
	SELECT (booleanExpression ARROW booleanExpression)+
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
	| var
	| fundef
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

var
:
	IDENT
;

fundef
:
	FUN LPAREN formalParameter?
	(
		comma formalParameter
	)* RPAREN ARROW
	(
		expression
	)
;

formalParameter
:
	IDENT COLON
	(
		INT_T
		| REAL_T
		| STR_T
		| LIST_T
		| FUN
	)
;

funcall
:
	IDENT LPAREN expression
	(
		comma expression
	)* RPAREN
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
	TRUE | FALSE
;

fragment
TRUE
:
	'T' | 't'
;

fragment
FALSE
:
	'F' | 'f'
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