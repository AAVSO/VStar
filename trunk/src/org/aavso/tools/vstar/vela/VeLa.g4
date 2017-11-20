grammar VeLa;

// VeLa: VStar expression Language
//       -     -          -- 

// TODO:
// - selection (e.g. in models): Haskell/Scala/Erlang functional-style cases 
//   instead of if-then:
//     (boolean-expression : expression-over-x,y,z ...)+
//   and in functions:
//     f <- fun(x:t1,y:t2,z:t3) -> expression-over-x,y,z
//   | fun(x:t1,y:t2,z:t3) -> (boolean-expression : expression-over-x,y,z ...)+
// - final "else" clause
// - variable binding of any expression, including functions (HOFs)
// - print statement for higher-level use of VeLa with LLVM/JVM
// - internal function representation in Models dialog should use VeLa
// - comments (-- or #)

// ** Parser rules **

// A VeLa program consists of zero or more bindings or output 
// statements, or expressions.

// The expression production will leave a value on the stack,
// therefore the program rule could be the entry point for 
// VStar filters as well as models. If one wishes to create 
// a filter that is a complete VeLa program that happens to
// end in a boolean expression, then that should be allowed.
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
	booleanExpression
	| selectionExpression+
;

selectionExpression
:
	booleanExpression COLON booleanExpression
	(
		ELSE COLON booleanExpression
	)?
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
	| additiveExpression
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
	LPAREN additiveExpression RPAREN
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
	LBRACKET additiveExpression?
	(
		comma additiveExpression
	)* RBRACKET
;

var
:
	IDENT
;

funcall
:
	IDENT LPAREN additiveExpression
	(
		comma additiveExpression
	)* RPAREN
;

fundef
:
	FUN LPAREN formalParameter?
	(
		comma formalParameter
	)* RPAREN ARROW
	(
		booleanExpression
		| expression
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

comma
:
	COMMA
;

// ** Lexer rules **

BACK_ARROW
:
	'<-'
;

OUT
:
	'out'
	| 'OUT'
;

ELSE
:
	'else'
	| 'ELSE'
;

FUN
:
	'fun'
	| 'FUN'
;

INT_T
:
	'integer'
	| 'INTEGER'
;

REAL_T
:
	'real'
	| 'REAL'
;

STR_T
:
	'string'
	| 'STRING'
;

LIST_T
:
	'list'
	| 'LIST'
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