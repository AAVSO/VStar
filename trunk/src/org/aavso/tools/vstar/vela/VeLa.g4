grammar VeLa;

// VeLa: VStar expression Language
//       -     -          -- 

// TODO:
// - selection (e.g. in models): Haskell/Scala/Erlang functional-style cases 
//   instead of if-then:
//     (boolean-expression : expression-over-x,y,z ...)+
//   and in functions:
//     f(x,y,z) -> expression-over-x,y,z
//   | f(x,y,z) -> (boolean-expression : expression-over-x,y,z ...)+
// - allow f to be \ or empty string?
// - final "otherwise" or "else" clause
// - internal function representation in Models dialog should use VeLa
// - print statement for higher-level use of VeLa with LLVM/JVM code generation
// - do we need a let statement? (non-mutable variable binding) 
// - comments (-- or #)

// ** Parser rules **

function
:
	IDENT LPAREN IDENT
	(
		comma IDENT
	)* RPAREN
	ARROW
	(
		booleanExpression
		| selectionExpression+	
	)
;

// TODO: change this to expression...
selectionExpression
:
	booleanExpression COLON booleanExpression
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
	groupedBooleanExpression
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
		) groupedBooleanExpression
	)?
;

groupedBooleanExpression
:
	LPAREN booleanExpression RPAREN
	| expression
;

// TODO: ...and this to additiveExpression
expression
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

exponentiationExpression
:
// This whole rule option is right associative.
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
	| string
	| list
	| var
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

funcall
:
	IDENT LPAREN expression
	(
		comma expression
	)* RPAREN
;

sign
:
	MINUS
	| PLUS
;

comma
:
	COMMA
;

// ** Lexer rules **

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
	'IN'
	| 'in'
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
	'AND'
	| 'and'
;

OR
:
	'OR'
	| 'or'
;

NOT
:
	'NOT'
	| 'not'
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