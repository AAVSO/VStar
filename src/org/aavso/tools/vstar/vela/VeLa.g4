grammar VeLa;

// VeLa: VStar expression Language
//       -     -          -- 

// TODO:
// - IF antecedent { consequent1 } ELSE { consequent2 } 
// - Add .. operator as shorthand for creating numeric lists over a range
//   o open-ended range: N.. => generator
// - It would be more type safe to allow a signature instead of "function" **
//   for function parameters, e.g. (real real) : real 
//   or function (real real) : real
// - Add compile() function **
//   o compile() returns AST as list and/or S-expression string
// - Allow S-expressions to be converted into ASTs, e.g. compile_sexpr() 
//   => AST internally
// - Generate Java class files from VeLa ASTs
// - Functions should be properly tail recursive to allow loops
//   o final AST in function is a recursive call, or
//   o final SELECT AST consequent is a recursive call 
//   o Detecting tail recursion is easy enough and not pushing VeLa 
//     scopes is also easy, but eliminating recursive calls to eval() is harder; 
//     compiling VeLa could do it; could we have an iteration within the loop call 
//     Java code that handled this?
//   o could continuation passing vs direct style combined with iteration help here?
// - Add maps; -> as key-value pair delimiter, e.g. m <- [ key -> value, ... ];
//   probably use ":" actually; we already use -> for select statements; could use 
//   colon for that too
// - Consider a typed vs heterogenous tuple type, e.g. record in homage to Pascal
// - An object could just be created from a closure with multiple functions 
//   accessible via an instance (function call) with "class", ".", "this"
// - Object-based starting with maps; actually structs (object keyword) since
//   keys in maps can be any value at all, not only bindings
//   o Implicit (or explicit) reference to object available to functions in object
//   o A function in an object could have either the non-function contents 
//     of the map added to the current scope or a self/this variable pointed 
//     to the map.
//   o Indeed, an object is arguably just a scope, such that x.f() or
//     x.a would involve object x being created (as a VeLaScope) and pushed
//     onto the stack with look-ups for 'f' and 'a' then proceeding as 
//     normal. VeLaScope's addAll() would permit multiple super classes; 
//     to disambiguate function arguments from class instance variables, a
//     'this' or 'self' or 'me' argument could be added to a function's actual 
//     parameter list at invocation time. The object scope must be popped 
//     when a function exits or by a method invocation handler in eval().
//     A function could be marked as a method, for example.
// - Consider omitting "function" prefix ala Java etc anonymous functions
// - Consider a list subscript operator vs nth() **
// - Require list elements to be all the same?
//   o realistic & good for API calls
//   o type checker must determine whether a lists's elements can be 
//     coerced to the same value; perhaps declared as list<T> or checked
//     upon each element addition
// - Y-combinator in VeLa **
// - Allow a type called ANY or variant types such as (real | string | list) **
// - Refinement types ala Wadler's complement to blame, e.g. f(n:real{n >= 0})
// - Doc strings for functions; use ;; rather than -- ?

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
:   '\u2C96' | '\u2C97'
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