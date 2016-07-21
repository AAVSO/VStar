grammar VeLa;

// VeLa: VStar expression Language

// TODO: next: unary negation, functions (start with "now")

realExpression
	: multiplicativeExpression ((PLUS | MINUS) multiplicativeExpression)*
	;
	
multiplicativeExpression
	: numericFactor ((MULT | DIV) numericFactor)*
	;
	
numericFactor
	: LPAREN realExpression RPAREN
	| real
	;
	
real
	: sign? DIGIT+ (POINT DIGIT+)? (EXPONENT_INDICATOR MINUS? DIGIT+)?
	| sign? POINT DIGIT+? (EXPONENT_INDICATOR MINUS? DIGIT+)?
	
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

DIGIT
	: [0-9]
	;

EXPONENT_INDICATOR
	: [Ee]
	;
		 
WS 	
	: [ \r\t\n]+ -> skip
	;