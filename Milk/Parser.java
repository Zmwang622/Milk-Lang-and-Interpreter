package JavaInterpreter.Milk;

import java.util.List;

import static JavaInterpreter.Milk.TokenType.*;
/***
 A recursive descent parser: meaning we start from lowest priority
 and move up.  Also called a top-down parser.

 Priority(from lowest to highest)
 5.Equality
 4.Comparison
 3.Addition
 2.Multiplication
 1.Unary

 Recursive Descent parser translates a grammar's rules straight 
 into code.

 Like the scanner this parser consumes sequences but now it works at
 the level of entire tokens.
*/
class Parser
{
	private final List<Token> tokens;
	//Current points at the new token to be used.
	private int current = 0;

	Parser(List<Token> tokens)
	{
		this.tokens = tokens;
	}

	//First grammar rule, expression, expands to the equality rule.
	private Expr expression()
	{
		return equality();
	}

	/***
	
	The rule for equality:
	equality → comparison ( ( "!=" | "==" ) comparison )* ;
	
	The left comparison nonterminal in the body is translated to the
	first call to comparison() and store that in the local variable, 
	expr. 

	Then the (...)* statements loops in the while loop. If we don't 
	see a != or == token, we know he have finished the equality 
	operator statement.

	*/
	private Expr equality()
	{
		Expr expr = comparison();

		while(match(BANG_EQUAL,EQUAL_EQUAL))
		{
			Token operator = previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	//Checks to see if the current token is any of the given types.
	private boolean match(TokenType... types)
	{
		for(TokenType type: types)
		{
			if(check(type))
			{
				advance();
				return true;
			}
		}

		return false;
	}

	//Only looks at the current type and see if it matches. 
	private boolean check(TokenType type)
	{
		if(istAtEnd())
			return false
		return peek().type == type;
	}
	
	private Token advance()
	{
		if(!isAtEnd())
		{
			current++;
		}
		return previous();
	}
}