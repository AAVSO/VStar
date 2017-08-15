grammar VeLa;

// VeLa: VStar expression Language
//       -     -          -- 

// TODO:
// - types: integer, set literals
// - set membership: x in [ ... ]; see filter from plot model descriptions
// - exponentiation (use associativity modifier)
// - selection (e.g. in models): Haskell functional-style patterns instead of if-then
// - internal function representation in Models dialog should use VeLa:
//     t -> real-expression-over-t
//   | (boolean-expression : t -> real-expression-over-t ...)+
// - print statement for higher-level use of VeLa with LLVM code generation
// - comments (-- or #)

// ** Parser rules **

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
		) groupedBooleanExpression
	)*
;

groupedBooleanExpression
:
	LPAREN booleanExpression RPAREN
	| expression
;

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
	sign? factor
;

factor
:
	LPAREN expression RPAREN
	| integer
	| real
	| string
	| var
	| func
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

var
:
	IDENT
;

func
:
	IDENT LPAREN expression
	(
		 comma expression
	)* RPAREN
;

sign
:
	MINUS | PLUS
;

comma
:
	COMMA
;

// ** Lexer rules **

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

// Homage to Perl
APPROXIMATELY_EQUAL
:
	'=~'
;

LPAREN
:
	'('
;

RPAREN
:
	')'
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

fragment DIGIT
:
	[0-9]
;

fragment POINT
// Locale-inclusive
:
	PERIOD
	| COMMA
;

fragment EXPONENT_INDICATOR
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

fragment LETTER
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