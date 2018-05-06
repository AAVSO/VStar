grammar VeLa;

// VeLa: VStar expression Language
//       -     -          -- 

// TODO:
// - Internal function representation in Models dialog should use VeLa
// - VeLa could replace or be an alternative to JavaScript for scripting
//   o Need a FFI
// - Generate class files from VeLa ASTs
// - Functions should be properly tail recursive to allow loops
//   o final AST in function is a recursive call, or
//   o final SELECT AST consequent is a recursive call 
//   Detecting tail recursion is easy enough and not pushing VeLa scopes is also easy, 
//   but eliminating recursive calls to eval() is harder; compiling VeLa could do it
// - Add eval() and compile() functions
//   o need to be able to tell whether eval() has left a value on the stack;
//     could do this by returning boolean and having a VeLa pop() function
//   o compile() returns AST as list and/or S-expression
// - Allow S-expressions to be evaluated, e.g. compile_sexpr()
// - Add for, reduce **
// - May still need while loops: while booleanExpression { ... }
// - Add maps; -> as key-value pair delimiter, e.g. m <- [ key -> value, ... ];
//   probably use : actually; we already use -> for select statements; could use 
//   colon for that too
// - Object-based starting with maps; actually structs (object keyword) since
//   keys in maps can be any value at all, not only bindings
//   o Implicit (or explicit) reference to object available to functions in object
//   o A function in an object could have either the non-function contents 
//     of the map added to the current scope or a self/this variable pointed 
//     to the map
// - It would be more type safe to allow a signature instead of "function" 
//   for function parameters, e.g. [real, real] -> real
// - Optional types for let bindings; more useful if mutable **
// - Whitespace is significant in parameter lists because we allow commas in numbers;
//   so, remove commas as parameter list (formal and actual) and list delimiters; use 
//   space or semi-colon ala MATLAB **
// - The interpreter could be used by a compiler for deterministic ASTs
// - Add .. operator as shorthand for creating numeric lists over a range **
//   o Open-ended range: N..
// - A counter style closure would not work since bindings are immutable
// - Y-combinator in VeLa
// - Unicode symbols for vars, e.g. PI, for Fourier models
// - Need a REPL for test model functions

// ** Parser rules **

// A VeLa program consists of zero or more bindings, output 
// statements or expressions. Variable bindings are immutable, 
// as is the operation of functions. VeLa identifiers and keywords
// are case-insensitive.

// The expression production will leave a value on the stack,
// therefore the program rule could be the entry point for 
// VStar filters as well as models. If one wishes to create 
// a filter that is a complete VeLa program that happens to
// end in a boolean expression, then that is permitted.

sequence
:
	(
		binding
		| namedFundef
//		| out
		| expression
	)*
;

// The intention of the semantics are that within a given scope,
// a binding cannot be repeated without error. I note that F# uses
// <- for modifying mutable values

binding
:
	symbol BACK_ARROW expression
;

// A named function definition, when invoked, introduces an additional 
// environment and allows all VeLa program elements operating over that 
// environment and its predecessors. name:type pays homage to Pascal, 
// OCaml/F# and Swift.
// TODO: Should consider using -> vs : for return type ala ML
//       Alternatively, use colons EVERYWHERE; -> could be used, e.g. in select

namedFundef
:
	symbol LPAREN formalParameter?
	(
		formalParameter
	)* RPAREN
	(
		COLON type
	)? LBRACE sequence RBRACE
;

// TODO: add IN/INPUT/READ and change below to WRITE if necessary

//out
//:
//	OUT LPAREN 
//	expression
//	(
//		expression
//	)*
//	RPAREN
//;
 
expression
:
	selectionExpression
	| booleanExpression
;

// Homage to Haskell/Scala/Erlang functional-style cases and Kotlin for name
selectionExpression
:
	WHEN
	(
	// TODO: should be expression on RHS; allows nested select!
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
//	factor
	< assoc = right > factor
	(
		(
			POW
		) factor
	)*
;

factor
:
// Note: funcall must precede symbol to avoid errors
	LPAREN expression RPAREN
	| integer
	| real
	| bool
	| string
	| list
	| funcall
	| symbol
	| anonFundef
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
		expression
	)* RBRACKET
;

symbol
:
	IDENT
;

// An anonymous function definition, when invoked, introduces an additional 
// environment and allows all VeLa program elements operating over that 
// environment and its predecessors.

anonFundef
:
// TODO: call it lambda instead of function? either that or fun.
	FUN LPAREN formalParameter?
	(
		formalParameter
	)* RPAREN
	(
		COLON type
	)? LBRACE sequence RBRACE
;

// A formal parameter consists of a name-type pair

formalParameter
:
	symbol COLON type
;

// TODO: make it int, bool, real ...

type
:
	INT_T
	| REAL_T
	| BOOL_T
	| STR_T
	| LIST_T
	| FUN
;

// A function call consists of a function object followed 
// by zero or more parameters surrounded by parentheses.
 
funcall
:
	funobj LPAREN expression?
	(
		expression
	)* RPAREN
;

// IDENT corresponds to an explicit function name
// var allows a HOF (let binding or function parameter)
// anonFundef allows an anonymous function

funobj
:
	(
		IDENT
		| anonFundef
	)
;

//comma
//:
//	COMMA
//;

// ** Lexer rules **

BACK_ARROW
:
	'<-'
;

COLON
:
	':'
;

ARROW
:
	'->'
;

//OUT
//:
//	[Oo] [Uu] [Tt]
//;

WHEN
:
	[Ww] [Hh] [Ee] [Nn]
;

// Used for function definition and type
// TODO: or define? or just fun as per ML ...

FUN
:
	[Ff] [Uu] [Nn] [Cc] [Tt] [Ii] [Oo] [Nn]
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
	[Bb] [Oo] [Oo] [Ll] [Ee] [Aa] [Nn]
;

STR_T
:
	[Ss] [Tt] [Rr] [Ii] [Nn] [Gg]
;

LIST_T
:
	[Ll] [Ii] [Ss] [Tt]
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

LBRACE
:
	'{'
;

RBRACE
:
	'}'
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

// #t pays homage to (Common) Lisp; #f?

fragment
TRUE
:
	'#T'
	| '#t'
;

fragment
FALSE
:
	'#F'
	| '#f'
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
// TODO: exclude what isn't permitted in an identifier rather than including what can

:
	(
		LETTER
		| UNDERSCORE
		| QUESTION
	)
	(
		LETTER
		| DIGIT
		| UNDERSCORE
		| QUESTION
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

QUESTION
:
	'?'
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
// Homage to SQL
	'--' ~[\r\n]* -> skip
;