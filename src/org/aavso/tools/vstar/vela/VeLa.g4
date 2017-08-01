grammar VeLa;

// VeLa: VStar expression Language
//       -     -          -- 

// TODO:
// - add exponentiation (use associativity modifier)
// - set membership: x in [ ... ]; see filter from plot descriptions
// - math functions: by reflection from Math class or a targeted selection?
// - selection (e.g. in models): Haskell functional-style patterns instead of if-then
// - internal function representation in Models dialog should use VeLa:
//     t -> real-expression-over-t
//   | (boolean-expression : t -> real-expression-over-t ...)+
// - add print statement for higher-level use of VeLa with LLVM code generation

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

// TODO: rename realExpression as expression
expression
:
	realExpression
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

// TODO: change to factor
numericFactor
:
	LPAREN realExpression RPAREN
	| real
	| string
	| var
	| func
;

real
:
	REAL
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

// In homage to Perl
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