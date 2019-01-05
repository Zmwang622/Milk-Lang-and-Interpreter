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
	 rather than throwing because we want the called decide whether
	 to unwind it or not. 

	*/
	private static class ParseError extends RuntimeException{}
	
	private final List<Token> tokens;
	//Current points at the new token to be used.
	private int current = 0;
	/***
	 * Parser constructor. Consumes a sequence at the token level.
	 * int field current points to the next token.
	 * @param tokens the list of tokens ready to be parsed.
	 */
	Parser(List<Token> tokens)
	{
		this.tokens = tokens;
	}

	/***

	 Now that we have added statements, we can begin work on the method.  
	 Because statements and expressions are so different, we will 
	 relegate them each their own private method. 
	 
	 Start from the highest part of the syntax ladder.
	 
	 program â†’ declaration* EOF ;
	 declaration â†’ varDecl | statement ;
	 Statement syntax:
	 statement â†’ exprStmt | ifStmt | printStmt | block | returnStmt | whileStmt;
	 
	 This is the method that starts it all. Begins the recursive descent.
	 
	 @return the list of parsed statements.
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

	// Statement parser: for all my statement needs
	/***
	 * The granddaddy of all expressions and what not. 
	 * Statements are what make up programs.
	 * 
	 * Checks if the next token is any statement other than an expression. 
	 * 
	 * @return the statement based on the next char.
	 */
	private Stmt statement()
	{
		if(match(FOR))
			return forStatement();
		if(match(IF)) 
			return ifStatement();
		//print statements
		if(match(PRINT))
			return printStatement();
		if(match(RETURN))
			return returnStatement();
		if(match(WHILE))
			return whileStatement();
		// { means a block statement
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
			initializer = varDeclaration();
		}
		else
		{
			initializer = expressionStatement();
		}

		Expr condition = null;
		if(!check(SEMICOLON))
		{
			condition = expression();
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
			body = new Stmt.Block(Arrays.asList(
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

		Stmt thenBranch = statement();
		Stmt elseBranch = null;
		if(match(ELSE))
		{
			elseBranch = statement();
		}

		return new Stmt.If(condition, thenBranch, elseBranch);
	}
	
	/***
	 * Print Syntax:
	 * printStmt â†’ "print" expression ";" ;
	 *
	 * Already consumed the print statement so now we just find the expression and consume the semicolon. 
	 * @return a print statement containing its expression value.
	 */
	private Stmt printStatement()
	{	
		//grab the expression
		Expr value = expression();
		//consume the expected semicolon. This is error-handling.
		consume(SEMICOLON, "Expect ';' after value.");
		return new Stmt.Print(value);
	}

	private Stmt returnStatement()
	{
		Token keyword = previous();
		Expr value = null;

		if(!check(SEMICOLON))
		{
			value = expression();
		}

		consume(SEMICOLON, "Expect ';' after return value.");
		return new Stmt.Return(keyword, value);
	}
	
	/***
	 * Variable Rule:
	 * varDecl â†’ "var" IDENTIFIER ( "=" expression )? ";" 
	 * 
	 * Follow the variable rule. The "var" is already consumed.
	 * Grab the identifier (variable name). 
	 * Depending if there is an = sign or not, do accordingly.
	 * Consume the final semicolon.
	 * @return the Variable packed with its name and initializer which is either null or some expression.
	 */
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
	
	/***
	 *  Expression syntax:
	 *  expression â†’ assignment;
	 *  
	 *  Incredibly simple: Find the expression, and then consume the semicolon.
	 *  @return an expression statement with the expression bundled inside it.
	 */
	private Stmt expressionStatement()
	{	
		Expr expr = expression();
		consume(SEMICOLON, "Expect ';' after expression.");
		return new Stmt.Expression(expr);
	}

	private Stmt.Function function(String kind)
	{
		Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
	
		consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
		List<Token> parameters = new ArrayList<>();
		if(!check(RIGHT_PAREN))
		{
			do
			{
				if(parameters.size() >= 22)
				{
					error(peek(), "Cannot have more than 8 parameters");
				}
			
				parameters.add(consume(IDENTIFIER, "Expect parameter name,"));	
			} while(match(COMMA));
		}
		consume(RIGHT_PAREN,"Expect ')' after parameters.");

		consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
		List<Stmt> body = block();
		return new Stmt.Function(name, parameters, body);
	}
	
	/***
	 * Block Parser.
	 * Creates an empty list and parses all the statements and adds them to the list.
	 * 
	 * @return the list of parsed statements.
	 */
	private List<Stmt> block()
	{
		List<Stmt> statements = new ArrayList<>();

		while(!check(RIGHT_BRACE) && !isAtEnd())
		{
			statements.add(declaration());
		}

		consume(RIGHT_BRACE, "Expect '}' after block.");

		return statements;
	}
	
	/***
	 * Assignment Rule:
	 * assignment → IDENTIFIER "=" assignment | equality ;
	 */
	private Expr assignment()
	{
		//Parse the l-value
		Expr expr = or();
		
		//If we find an = we parse the right side. Otherwise no.
		if(match(EQUAL))
		{
			Token equals = previous();
			//Recursively call assignment to parse the right side.
			Expr value = assignment();

			if(expr instanceof Expr.Variable)
			{
				Token name = ((Expr.Variable)expr).name;
				return new Expr.Assign(name,value);
			}

			else if(expr instanceof Expr.Get)
			{
				Expr.Get get = (Expr.Get) expr;
				return new Expr.Set(get.object, get.name, value);
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
			Token operator = previous();
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
	/***
	 * Expression Rule:
	 * expression â†’ assignment ;
	 * 
	 * Expands the assignment rule.
	 * Start of the chain for any expression statement
	 */
	private Expr expression()
	{
		return assignment();
	}
	/***
	 * The method we call repeatedly when parsing a series of statements.
	 * Parser checks within the try block for Class, Ming, and Var calls. Otherwise it goes to the statement method.
	 * 
	 * @return depends on the statement type.
	 */
	private Stmt declaration()
	{
		try{
			if(match(CLASS))
				return classDeclaration();
			if(match(MING))
				return function("function");
			if(match(VAR))
				return varDeclaration();
			return statement();
		} catch (ParseError e) {
			//If something messes up, we synchronize on go onto the next token.
			synchronize();
			return null;
		}
	}
	private Stmt classDeclaration()
	{
		Token name = consume(IDENTIFIER, "Expect class name.");
		
		Expr.Variable superclass = null;
		if(match(LESS))
		{
			consume(IDENTIFIER, "Expect superclass name.");
			superclass = new Expr.Variable(previous());
		}

		consume(LEFT_BRACE, "Expect '{' before class body");

		List<Stmt.Function> methods = new ArrayList<>();
		while(!check(RIGHT_BRACE) && !isAtEnd())
		{
			methods.add(function("method"));
		}

		consume(RIGHT_BRACE,"Expect '}' after class body.");

		return new Stmt.Class(name, superclass, methods);
	}
	/***
	
	The rule for equality:
	equality â†’ comparison ( ( "!=" | "==" ) comparison )* ;
	
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
			//The previous token was either a != or ==;
			Token operator = previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	/***
	 
	 The rule for comparison. 
	 comparison â†’ addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
	
	 Starts with addition(). Then continuously looks for <, <= , > , >=. 

	 @return the comparison expression..
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
	
	/***
	 * How to parse addition.
	 *
	 * Addition's rule:
	 * addition â†’ multiplication ( ( "-" | "+" ) multiplication )* ; 
	 *
	 * Opens up to multiplication. If the next token is a + or -, it continues to concatenate the addition statement. Otherwise, it returns
	 * the multiplication expression.
	 * 
	 * @return expr the addition expression
	 */
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
	
	/***
	 * Rule for Multiplication
	 * multiplication â†’ unary ( ( "/" | "*" ) unary )* ;
	 * 
	 * Just like addition, but this time we first look for the unary value. Then if a / or * follow we create the multiplication statement.
	 * Otherwise we return the unary expression.
	 */
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
	 unary â†’ ( "!" | "-" ) unary | primary ;

	 Code differs from binary operators
	
	 If the token is a ! or -, we have a unary expression.
	 Now recursively call it again until the expression is done.
	
	 @return 
	*/

	private Expr unary()
	{
	 	if(match(BANG,MINUS))
	 	{
	 		Token operator = previous();
	 		Expr right = unary();
	 		return new Expr.Unary(operator, right);
		}
	 	
		return call();
	}

	private Expr finishCall(Expr callee)
	{
		List<Expr> arguments = new ArrayList<>();
		if(!check(RIGHT_PAREN))
		{
			do
			{
				if(arguments.size() >= 22)
				{
					error(peek(), "Cannot have more than 22 arguments.");
				}
				arguments.add(expression());
			} while(match(COMMA));
		}
		
		Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
	
		return new Expr.Call(callee, paren, arguments);
	}

	/***

	 Call rule:
	 unary â†’ ( "!" | "-" ) unary | call ;
     call  â†’ primary ( "(" arguments? ")" )* ;	 
	 First find the primary value.
	 
	*/
    private Expr call()
    {
    	Expr expr = primary();

     	while(true)
     	{
     		if(match(LEFT_PAREN))
     		{
     			expr = finishCall(expr);
     		}

     		else if (match(DOT))
     		{
     			Token name = consume(IDENTIFIER, 
     				"Expect property name after '.' .");
     			expr = new Expr.Get(expr, name);
     		}

     		else
     		{
     			break;
     		}
    	}

    	return expr;
    }
	/***
	 
	 Primary Rule:
	 primary â†’ "true" | "false" | "nil" | "this" | NUMBER | STRING | IDENTIFIER | "(" expression ")" | "super" "." IDENTIFIER ;
	 Highest level of precedence.
	 
	 @returns the correct expression for the current (well now previous) token
	*/

	 private Expr primary()
	 {
		//If we get a false, true, or nil, we return false, true, or nil, respectively. 
	 	if(match(FALSE))
	 		return new Expr.Literal(false);
	 	if(match(TRUE))
	 		return new Expr.Literal(true);
	 	if(match(NIL))
	 		return new Expr.Literal(null);

	 	//if we match a # or string, we'll return a literal with the value of the previous token.
	 	if(match(NUMBER, STRING))
	 	{
	 		return new Expr.Literal(previous().literal);
	 	}
	 	
	 	//if we match a super, we first find the previous keyword. 
	 	//Then we consume the '.' and find then consume the method name. 
	 	//Now return a super expr with the keyword and method.
	 	if(match(SUPER))
	 	{
	 		Token keyword = previous();
	 		consume(DOT, "Expect '.' after 'super'.");
	 		Token method = consume(IDENTIFIER,
	 			"Expect superclass method name.");
	 		return new Expr.Super(keyword, method);
	 	}
	 	//If we match a this, return a this expression with the previous token.
	 	if(match(THIS))
	 		return new Expr.This(previous());
	 	//If we find a identifier, return a variable expression with the previous token.
	 	if(match(IDENTIFIER))
	 	{
	 		return new Expr.Variable(previous());
	 	}
	 	//If we find a left parentheses, we consume until we find the right one. 
	 	//return a grouping with the expression inside.
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
	/***
	 * 
	 * @param types the given enum of TokenTypes.
	 * @return true if check() is true for a type. False if none of the types given work.
	 */
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
		//If type matches the next token, we continue. Otherwise we will throw an error
		if(check(type))
			return advance();

		throw error(peek(), message);
	}

	//Only looks at the current type and see if it matches. 
	/***
	 * Private helper used for match(). Checks to see if a singular type is equal to the next type in the code.
	 * 
	 * @param type the TokenType being checked.
	 * @return true if the token returned by peek() has the same TokenType as type. False if otherwise.
	 */
	private boolean check(TokenType type)
	{
		if(isAtEnd())
			return false;
		return peek().type == type;
	}
	
	/***
	 * Like the scanner's advance() but on a token level rather than a character level.
	 * Increments current by one.
	 * @return the previous token.
	 */
	private Token advance()
	{
		if(!isAtEnd())
		{
			current++;
		}
		return previous();
	}

	/***
	 * We know we are at the end of the code, if the next token in tokens is an EOF.
	 * @return true if at end, false if not
	 */
	private boolean isAtEnd()
	{
		return peek().type == EOF;
	}

	/***
	 * @return the next token.
	 */
	private Token peek()
	{
		return tokens.get(current);
	}

	/***
	 * @return the most recent token.
	 */
	private Token previous()
	{
		return tokens.get(current-1);
	}

	//Error method, ParseError is a static class in Parser.java
	/***
	 * First calls Milk's error method, which prints out the line of the error and what not. 
	 * 
	 * @return a Parse error, which is a run time error.
	 */
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

	Essentially it clears the JavaSTack after the recursive descent process is over.
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