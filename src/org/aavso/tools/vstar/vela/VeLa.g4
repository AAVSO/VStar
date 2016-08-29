grammar VeLa;

// VeLa: VStar expression Language

realExpression
	: multiplicativeExpression ((PLUS | MINUS) multiplicativeExpression)*
	;
	
multiplicativeExpression
	: unaryExpression ((MULT | DIV) unaryExpression)*
	;

unaryExpression
	: sign? numericFactor
	;
		
numericFactor
	: LPAREN realExpression RPAREN
	| real
	| func
	;

func
	: identifier ( LPAREN RPAREN )?
//	| identifier ( LPAREN realExpression ( COMMA realExpression )* RPAREN )?
	;

// TODO: These 2 rules need to move to the lexer because they allow whitespace and 
// there's also a conflict with comma here and in the real number rule (may be 
// able to handle this via a non-greedy directive)
identifier
	: LETTER ( LETTER | DIGIT )*
	;
	
real
	: DIGIT+ (POINT DIGIT+)? (exponentIndicator MINUS? DIGIT+)?
	| POINT DIGIT+? (exponentIndicator MINUS? DIGIT+)?
	;

// TODO: this is to avoid [Ee] being treated as a subset of LETTER, such that it
// would never be matched; unsatisfying! There must be a better approach
exponentIndicator
	: 'E' | 'e'
	;

sign
	: MINUS | PLUS
	;
		
MINUS
	: '-'
	;

PLUS
	: '+'
	;

MULT
	: '*'
	;

DIV
	: '/'
	;

LPAREN
	: '('
	;

RPAREN
	: ')'
	;
	
POINT
	// Locale-inclusive
	: '.' | ','
	;

COMMA
	: ','
	;

DIGIT
	: [0-9]
	;

LETTER
	: [A-Z] | [a-z]
	;
	 
WS 	
	: [ \r\t\n]+ -> skip
	;