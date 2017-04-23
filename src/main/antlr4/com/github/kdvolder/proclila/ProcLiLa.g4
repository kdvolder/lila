grammar ProcLiLa;

ID	: (LETTER|'_') (LETTER|DIGIT|'_')* 
	;
	
OP	: (OP_CHAR)+
	;

STRING
	: '"' (ESC*)? '"' 
	| '\'' (ESC*)? '\''
	;

fragment ESC : . | '\\\\' | '\\"' | '\\\'' ;
fragment LETTER : [a-zA-Z_] ;
fragment DIGIT : [0-9] ;
fragment OP_CHAR: [!#$%&*+\-./:<=?@^|~] ;

INT:[0-9]+ ;
WS: [ \t\n\r]+ -> skip ;

body
	: (def ';')* expr
	;

def	: ID ':' expr
	;

expr 
	: expr expr						# apply
	| expr OP expr					# binOp
	| 'if' expr '{' body '}' ( 'else' '{' body '}')? 	# if
	| ID '->' expr					# lambda
	| ID							# var
	| STRING						# string
	| INT							# int
	| '{' body '}'					# block
	| '(' expr ')'					# parens
	| '(' expr ( ',' expr )+ ')'	# tuple
	;
