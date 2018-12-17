package JavaInterpreter.Milk;

/*** 
 The parser needs to know not just that it has a lexeme
 for some word, but that it has a reserved word, and 
 which keyword it is.
 
 Recognize a lexeme --> remember which kind of lexeme it represents.
 
 Below is a list of all the lexemes starring in Milk
*/ 
 enum TokenType
 {
 	// Single-charcater tokens.
 	LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
 	COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

 	// One or two character tokens.
 	BANG, BANG_EQUAL,
 	EQUAL, EQUAL_EQUAL,
 	GREATER, GREATER_EQUAL,
 	LESS, LESS_EQUAL,

 	// Literals.
 	IDENTIFIER, STRING, NUMBER,

 	// Keywords.
 	AND, CLASS, ELSE, FALSE, FOR, IF, MING, NIL, OR,
 	PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

 	EOF
 }

 /***
  There are lexemes for literal values.
  "Since a scanner has to walk each character in the literal to 
  correctly identify it, it can also convert it to the real
  runtime value that will be used by the interpreter later."
   -- CraftingInterpretres.com 4.2.2
 */

//Test