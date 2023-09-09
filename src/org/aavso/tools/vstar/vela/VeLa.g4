grammar VeLa;

// VeLa: VStar expression Language
//       -     -          -- 

// TODO:
// - Doc strings for functions; use ;; rather than -- ?
// - Generate Java class files from VeLa ASTs

// ** Parser rules **

// A VeLa program consists of zero or more bindings, function 
// definitions, while loops or expressions. Bindings may be 
// variable (mutable) or constant. VeLa identifiers and keywords
// are case-insensitive.

// The expression production will leave a value on the stack,
// therefore the sequence rule could be the entry point for 
// VStar filters as well as models, i.e. f one wishes to create 
// a filter that is a complete VeLa program that happens to
// end in a boolean expression, then that is permitted.

sequence
:
    (
        binding
        | whileLoop
        | namedFundef
        | expression
    )*
;

// F# (+ OCaml, ML?) uses <- for modifying mutable values while 
// R uses it for regular assignment.

binding
:
    symbol
    (
        BACK_ARROW
        | IS
    ) expression
;

// A named function definition, when invoked, introduces an additional 
// environment and allows all VeLa program elements operating over that 
// environment and its predecessors. name:type pays homage to Pascal, 
// OCaml/F# and Swift.

namedFundef
:
    symbol LPAREN formalParameter?
    (
        formalParameter
    )* RPAREN
    (
        COLON type
    )? block
;

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
        booleanExpression ARROW consequent
    )+
;

consequent
:
    expression
    | block
;

whileLoop
:
    WHILE booleanExpression block
;

booleanExpression
:
    exclusiveOrExpression
    (
        OR exclusiveOrExpression
    )*
;


exclusiveOrExpression
:
    conjunctiveExpression
    (
        XOR conjunctiveExpression
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
    shiftExpression
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
        ) shiftExpression
    )?
;

shiftExpression
:
    additiveExpression
    (
        (
            SHIFT_LEFT
            | SHIFT_RIGHT
            
        ) additiveExpression
    )*
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
    MINUS? exponentiationExpression
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
    (FUN | LAMBDA) LPAREN formalParameter?
    (
        formalParameter
    )* RPAREN
    (
        COLON type
    )? block
;

// A formal parameter consists of a name-type pair

formalParameter
:
    symbol COLON type
;

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

block
:
    LBRACE sequence RBRACE
;

// ** Lexer rules **

BACK_ARROW
:
    '<-'
;

IS
:
    [Ii] [Ss]
;

COLON
:
    ':'
;

ARROW
:
    '->'
;

WHEN
:
    [Ww] [Hh] [Ee] [Nn]
;

WHILE
:
    [Ww] [Hh] [Ii] [Ll] [Ee]
;

// Used for function definition and type
// TODO: or define? or just fun as per ML ...

FUN
:
    [Ff] [Uu] [Nn] [Cc] [Tt] [Ii] [Oo] [Nn]
;

LAMBDA
:   '\u039B' | '\u03BB'
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

SHIFT_LEFT
:
    '<<'
;

SHIFT_RIGHT
:
    '>>'
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

XOR
:
    [Xx] [Oo] [Rr]
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
    DEC_DIGIT+
    | ([0] [Xx] HEX_DIGIT+)
    | ([0] [Bb] BIN_DIGIT+)
;

REAL
:
    DEC_DIGIT+
    (
        POINT DEC_DIGIT+
    )?
    (
        EXPONENT_INDICATOR MINUS? DEC_DIGIT+
    )?
    | POINT DEC_DIGIT+
    (
        EXPONENT_INDICATOR MINUS? DEC_DIGIT+
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
    [Tt] [Rr] [Uu] [Ee]
;

fragment
FALSE
:
    [Ff] [Aa] [Ll] [Ss] [Ee]
;

fragment
DEC_DIGIT
:
    [0-9]
;

fragment
HEX_DIGIT
:
    DEC_DIGIT | [a-z] | [A-Z]
;

fragment
BIN_DIGIT
:
    [0-1]
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
        | DEC_DIGIT
        | SYMBOL
    )*
;

fragment
LETTER
:
    [A-Z]
    | [a-z]
    | [\u0080-\uFFFF]
;

fragment
UNDERSCORE
:
    '_'
;

fragment
SYMBOL
:
    UNDERSCORE
    | '?'
    | '!'
    | '&'
    | '%'
    | '#'
    | '$'
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
// The first pays homage to SQL. The second is a concession to the shebang mechanism.

    (
        '--'
        | '#'
    ) ~[\r\n]* -> skip
;