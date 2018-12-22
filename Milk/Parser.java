package JavaInterpreter.Milk;

import java.util.ArrayList;
import java.util.Arrays;
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
	/***
	 
	 Sentinel class used to unwind parser. error() returns it 
	 rather than thwoing because we want the called decide whether
	 to unwind it or not. 

	*/
	private static class ParseError extends RuntimeException{}

	private final List<Token> tokens;
	//Current points at the new token to be used.
	private int current = 0;

	Parser(List<Token> tokens)
	{
		this.tokens = tokens;
	}

	/***

	 Now that we have added statements, we can begin work on the method.  
	 Because statements and expressions are so different, we will 
	 relegate them each their own private method. 
	 
	 Start from the highest part of the syntax ladder.
	 
	 program → declaration* EOF ;
	 declaration → varDecl | statement ;
	 Statement syntax:
	 statement → exprStmt | ifStmt | printStmt | block;
	
	*/
	List<Stmt> parse()
	{
		List<Stmt> statements = new ArrayList<>();
		while(!isAtEnd())
		{
			statements.add(declaration());
		}

		return statements;
	}

	//Parsing statements: either print or use expression

	private Stmt statement()
	{
		if(match(FOR))
			return forStatement();
		if(match(IF)) 
			return ifStatement();
		if(match(PRINT))
			return printStatement();
		if(match(WHILE))
			return whileStatement();
		if(match(LEFT_BRACE))
			return new Stmt.Block(block());

		return expressionStatement();
	}


	private Stmt forStatement()
	{
		consume(LEFT_PAREN,"Expect '(' after 'for' .");

		Stmt initializer;
		if(match(SEMICOLON))
		{
			initializer = null;
		}
		else if(match(VAR))
		{
			intializer = varDeclaration();
		}
		else
		{
			initializer = expressionStatement();
		}

		Expr condition = null;
		if(!check(SEMICOLON))
		{
			condition = expressionm();
		}
		consume(SEMICOLON, "Expect ';' after loop condition.");

		Expr increment = null;
		if(!check(RIGHT_PAREN))
		{
			increment = expression();
		}

		consume(RIGHT_PAREN,"Expect ')' after for clauses.");

		Stmt body = statement();

		if(increment != null)
		{
			body = new Stmt.Blcok(Arrays.asList(
				body, new Stmt.Expression(increment)));
		}

		if(condition == null)
			condition = new Expr.Literal(true);
		body = new Stmt.While(condition, body);

		if(initializer != null)
		{
			body = new Stmt.Block(Arrays.asList(initializer, body));
		}
		return body;
	}
	private Stmt ifStatement()
	{
		consume(LEFT_PAREN, "Expect '(' after 'if'.");
		Expr condition = expression();
		consume(RIGHT_PAREN, "Expect ')' after if condition,");

		Stmt thenBrench = statement();
		Stmt elseBranch = null;
		if(match(ELSE))
		{
			elseBranch = statement();
		}

		return new Stmt.If(condition, thenBranch, elseBranch);
	}
	private Stmt printStatement()
	{
		Expr value = expression();
		consume(SEMICOLON, "Expect ';' after value.");
		return new Stmt.Print(value);
	}

	private Stmt varDeclaration()
	{
		Token name = consume(IDENTIFIER, "Expect variable name.");

		Expr initializer = null;

		//if there is an '=' it knows there is an initializer expression.
		if(match(EQUAL))
		{
			initializer = expression();
		}

		consume(SEMICOLON, "Expect ';' after variable declaration.");
		return new Stmt.Var(name, initializer);
	}

	private Stmt whileStatement()
	{
		consume(LEFT_PAREN, "Expect '(' after 'while'.");
		Expr condition = expression();
		consume(RIGHT_PAREN, "Expect ')' after condition.");
		Stmt body = statement();

		return new Stmt.While(condition, body);
	}

	private Stmt expressionStatement()
	{	
		Expr expr = expression();
		consume(SEMICOLON, "Expect ';' after expression.");
		return new Stmt.Expression(expr);
	}

	private List<Stm> block()
	{
		List<Stmt> statements = new ArrayList<>();

		while(!check(RIGHT_BRACE) && !isAtEnd())
		{
			statements.add(declaration());
		}

		consume(RIGHT_BRACE, "Expect '}' after block.");

		return statements;
	}

	private Expr assignment()
	{
		Expr expr = or();

		if(match(EQUAL))
		{
			Token equals = previous();
			Expr value = assignment();

			if(expr instanceof Expr.Variable)
			{
				Token name = ((Expr.Variable)expr).name;
				return new Expr.Assign(name,value);
			}

			error(equals, "Invalid assignment target.");
		}
		return expr;
	}

	private Expr or()
	{
		Expr expr = and();

		while(match(OR))
		{
			Token operator = previous;
			Expr right = and();
			expr = new Expr.Logical(expr, operator, right);
		}

		return expr;
	}

	//And has higher precedence over or
	private Expr and()
	{
		Expr expr = equality();

		while(match(AND))
		{
			Token operator = previous();
			Expr right = equality();
			expr = new Expr.Logical(expr,operator,right);
		}

		return expr;
	}
	//First grammar rule, expression, expands to the equality rule.
	private Expr expression()
	{
		return assignment();
	}

	private Stmt declaration()
	{
		try{
			if(match(VAR))
				return varDeclaration();
			return statement();
		} catch (ParseError e) {
			synchronize();
			return null;
		}
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

	 	if(match(IDENTIFIER))
	 	{
	 		return new Expr.Variable(previous());
	 	}
	 	if(match(LEFT_PAREN))
	 	{
	 		Expr expr = expression();
	 		consume(RIGHT_PAREN, "Expect ')' after expression.");
	 		return new Expr.Grouping(expr);
	 	}

	 	//If there is a token that doesn't match a single case
	 	throw error(peek(), "Expect expression.");
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

	/***
	 Error Handler will be using the "panic mode" technique
	 As soon as the parser detects an error, it enters panic mode. It
	 knows at least one token is incorrect in the current stack of
	 grammar productions. Must synchronize.

	 Synchronization - Get the parser's states and the sequence 
	 of forthcoming tokens aligned such that the next token does 
	 match the rule being parsed.

	 Select a rule in the grammar that marks the synchronization 
	 piont. Parser jumps out of it currents production and 
	 "synchronizes" by discarding tokens until it reaches one that
	 can appear at that point in the rule.
	 
	 Need to develop statements first, so I'll finish out the rest of
	 the parser first.
	
	 consume() - Checks to see next token is expected. If it isn't 
	 throw an error and freak out. 
	*/
	private Token consume(TokenType type, String message)
	{	
		if(check(type))
			return advance();

		throw error(peek(), message);
	}

	//Only looks at the current type and see if it matches. 
	private boolean check(TokenType type)
	{
		if(isAtEnd())
			return false;
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
		return tokens.get(current-1);
	}

	//Error method, ParseError is a static class in Parser.java
	private ParseError error(Token token, String message)
	{
		Milk.error(token, message);
		return new ParseError();
	}

	/***
	 
	 Method that synchronizes. 

	 Because we use recursive descent, the praser's stae is not 
	 stored explicitly in fields. Instead we use Java's own callstack
	 to track what the parser is doing. 

	 Instead, we use Java exceptions. When we want to synchronize, 
	 we THROW  the ParseObject. We are synchronizing on statement
	 boundaries, so we'll catch there. 

	 Once the expection is caught, we synchronize the tokens. 

	 Lets discard tokens until we're right at the beginning of the 
	 next statement. To determine that, assume the statements after the
	 next semicolon is the beginning of the next statement.

	Call this after we catch a ParseError on the statement level, and
	we'll hopefully be back on track.

	*/
	private void synchronize()
	{
		advance();

		while(!isAtEnd())
		{	
			//If last type was a ; we can assume we're at a new stmnt
			if(previous().type == SEMICOLON)
				return;

			switch(peek().type)
			{
				//New statements can also start with these.
				//Especially: for, if, return, and var
				case CLASS:
				case MING:
				case VAR:
				case FOR:
				case IF:
				case WHILE:
				case PRINT:
				case RETURN:
					return;
			}
			
			//Keep ignoring tokens until it might have found a new statement
			advance();
		}
	}
}

/***

 Notes on Error Handling:

 Parse has two jobs:
 1. Take a valid sequence of tokens, and produce a corresponding 
 syntax tree.
 2. Given an invalid sequence of tokens, detect any errors and yell at
 the users for messing up.

 2nd is much much much more important that the first one.
 -When the user doesn't realize they messed up, the parser has to
 lead them back on track

 Requirements and Recommendations for Parser:
 Req:
 1. Detect and Report the Error. 
  If the parser doesn't react to the error and simply go on to the
  interpreter...hell will ensue.
 2. Can't Crash or Hang
  Parser can't get stuck in an infinite loop at any point because 
  though code may be invalid, it's still a valid input to the parser.
  Users use the parser to learn what syntax is allowed.
 Rec:
  1. Be Fast
   "Faster than fast, quicker than quick, I am lightning"
    	-- My parser, probably
  2. Report as many distinct errors as there are
   This way the user can fix all of their issues in one run, rather
   than repeated running for each error.
  3. Minimize cascaded errors.
   Don't you hate it when you forget one bracket and you suddenly 
   have 35 errors? Let's try to minimize that from happening.

 Error Recovery- how the parser responds to an error and keeps going
 back to look for later errors. Heavily researched in the 60s when 
 code was still submitted via stacks of punch cards.
*/