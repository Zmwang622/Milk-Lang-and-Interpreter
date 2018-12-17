package JavaInterpreter.Milk;
/***
 Tracking where the error occurs starts here
 
 In simple interperter: only note which line the token appears on
*/
class Token
{
	final TokenType type;
	final String lexeme;
	final Object literal;
	final int line;

	Token(TokenType type, String lexeme, Object literal, int line)
	{
		this.type = type;
		this.lexeme = lexeme;
		this.literal = literal;
		this.line = line;
	}

	public String toString()
	{
		return type + " " + lexeme + " " + literal;
	}
}

/***
 A token: a bundle containing the raw lexeme along with the other things
 the scanner learned about it.

*/