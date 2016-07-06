grammar VeLa;

// VeLa: VStar expression Language

expression
	: real
	;
	
real
	: SIGN? DIGIT+ (POINT DIGIT+)? (EXPONENT_INDICATOR SIGN? DIGIT+)?
	;

SIGN
	: MINUS | PLUS
	;
		
MINUS
	: '-'
	;

PLUS
	: '+'
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