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

	/***
	 
	 The rule for comparison. 
	 comparison → addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
	
	 Follows the same pattern of equality(), but this time with 
	 addition. The other binary operators follow the same pattern

	*/
	private Expr comparison()
	{
		Expr expr = addition();

		while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL))
		{
			Token operator = previous();
			Expr right = addition();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr addition()
	{
		Expr expr = multiplication();

		while(match(MINUS, PLUS))
		{
			Token operator = previous();
			Expr right = multiplication();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	private Expr multiplication()
	{
		Expr expr = unary();

		while(match(SLASH, STAR))
		{
			Token operator = previous();
			Expr right = unary();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	/***
	 
	 Unary Rule:
	 unary → ( "!" | "-" ) unary | primary ;

	 Code differs from biary operators
	
	 If the token is a ! or -, we have a unary expression.
	 Now recursively call it again until the expression is done.

	*/

	private Expr unary()
	{
	 	if(match(BANG,MINUS))
	 	{
	 		Token operator = previous();
	 		Expr right = unary();
	 		return new Expr.Unary(operator, right);
		}
	 	
		return primary();
	}

	/***
	 
	 Primary Rule:
	 primary → NUMBER | STRING | "false" | "true" | "nil" | "(" expression ")" ;
	  
	 Highest level of precedence.

	*/

	 private Expr primary()
	 {
	 	if(match(FALSE))
	 		return new Expr.Literal(false);
	 	if(match(TRUE))
	 		return new Expr.Literal(true);
	 	if(match(NIL))
	 		return new Expr.Literal(null);

	 	if(match(NUMBER, STRING))
	 	{
	 		return new Expr.Literal(previous().literal);
	 	}

	 	if(match(LEFT_PAREN))
	 	{
	 		Expr expr = expression();
	 		consume(RIGHT_PAREN, "Expect ')' after expression.");
	 		return new Expr.Grouping(expr);
	 	}
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
	
	/***
	 Consumes the current token, and returns it.

	 Just like the scanner's advance().
	*/
	private Token advance()
	{
		if(!isAtEnd())
		{
			current++;
		}
		return previous();
	}

	//Checks if we've run out of tokens to parse
	private boolean isAtEnd()
	{
		return peek().type == EOF;
	}

	//Returns the current token we have yet to consume
	private Token peek()
	{
		return tokens.get(current);
	}

	//Returns most recently consumed token
	private Token previous()
	{
		returns tokens.get(current-1);
	}



}