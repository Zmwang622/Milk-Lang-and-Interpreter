package JavaInterpreter.Milk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>
{
	private final Interpreter interpreter;
	private final Stack<Map<String,Boolean>> scopes = new Stack<>();
	private FunctionType currentFunction = FunctionType.NONE;

	Resolver(Interpreter interpreter)	
	{
		this.interpreter = interpreter;
	}

	private enum FunctionType{
		NONE,
		FUNCTION,
		INITIALIZER,
		METHOD
	}

	private enum ClassType{
		NONE,
		CLASS,
		SUBCLASS
	}

	private ClassType currentClass = ClassType.NONE;
	
	/***
	 * Runs through a list and resolves each one.
	 */
	void resolve(List<Stmt> statements)
	{
		for(Stmt statement : statements)
		{
			resolve(statement);
		}
	}

	/***
	 * Function resolver Pt.2 
	 * We declare a new scope for the funciton body, and add its parameters.
	 */
	private void resolveFunction(
		Stmt.Function function, FunctionType type) 
	{
		FunctionType enclosingFunction = currentFunction;
		currentFunction = type;

		beginScope();
		for(Token param: function.params)
		{
			declare(param);
			define(param);
		}
		resolve(function.body);
		endScope();

		currentFunction = enclosingFunction;
	}
	
	/***
	 * For both resolve() methods:
	 * We give the visitor pattern to the given syntax tree node.
	 */
	private void resolve(Stmt stmt)
	{
		stmt.accept(this);
	}

	private void resolve(Expr expr)
	{
		expr.accept(this);
	}
	
	/***
	 * A new block scope is created by pushing a HashMap 
	 * The map represents a single scope.
	 */
	private void beginScope()
	{
		scopes.push(new HashMap<String, Boolean>());
	}

	/***
	 * To exit a scope, pop the most recent one off and you good.
	 * AKA pop from scopes.
	 */
	private void endScope()
	{
		scopes.pop();
	}

	/***
	 * Method that adds variable to the innermost scope.
	 * Variable shadows any outer one. 
	 * 
	 */
	private void declare(Token name)
	{
		if(scopes.isEmpty())
			return;
		Map<String, Boolean> scope = scopes.peek();
		//Error handling for duplicates
		if(scope.containsKey(name.lexeme))
		{
			Milk.error(name, 
				"Variable with this name already declared in this scope.");
		}
		//Put it in as false to signify "not ready yet"
		scopes.put(name.lexeme, false);
	}

	/***
	 * Set the variable's value in the scope as true to mark that its ready!
	 */
	private void define(Token name)
	{
		if(scopes.isEmpty())
			return;
		scopes.peek().put(name.lexeme,true);
	}
	
	/***
	 * Start at the innermost scope and start looking for the variable.
	 */
	private void resolveLocal(Expr expr, Token name)
	{
		for(int i = scopes.size()-1 ; i>= 0; i--)
		{
			if(scopes.get(i).containsKey(name.lexeme))
			{
				//If variable is found we pass in the # of scopes and resolve it yeet.
				interpreter.resolve(expr,scopes.size()- 1 - i);
				return;
			}
		}
	}
	/***
	 * Block Resolver.
	 * Begins a scope, traverses into the statements within the block, and then discards the scope.
	 */
	@Override
	public Void visitBlockStmt(Stmt.Block stmt)
	{
		beginScope();
		resolve(stmt.statements);
		endScope();
		return null;
	}
	
	/***
	 * Class Visitor.
	 */
	@Override
	public Void visitClassStmt(Stmt.Class stmt)
	{	
		ClassType enclosingClass = currentClass;
		currentClass = ClassType.CLASS;
		declare(stmt.name);

		if(stmt.superclass != null)
		{
			currentClass = ClassType.SUBCLASS;
			resolve(stmt.superclass);
		}

		define(stmt.name);

		if(stmt.superclass != null)
		{
			beginScope();
			scopes.peek().put("super", true);
		}

		beginScope();
		scopes.peek().put("this", true);

		//Iterate through each method and call resolveFunction on it.
		for(Stmt.Function method : stmt.methods)
		{
			FunctionType declaration = FunctionType.METHOD;
			//For constructors.
			if(method.name.lexeme.equals("init"))
			{
				declaration = FunctionType.INITIALIZER;
			}
			resolveFunction(method, declaration);
		}

		endScope();

		if(stmt.superclass != null)
			endScope();

		currentClass = enclosingClass;
		return null;	
	}

	/***
	 * Expression resolver
	 * Expression statements have 1 expression to traverse.
	 */
	@Override
	public Void visitExpressionStmt(Stmt.Expression stmt)
	{
		resolve(stmt.expression);
		return null;
	}
	
	/***
	 * Function Resolver.
	 * First we resolve the function name, then its parameters.
	 */
	@Override
	public Void visitFunctionStmt(Stmt.Function stmt)
	{
		declare(stmt.name);
		define(stmt.name);

		resolveFunction(stmt, FunctionType.FUNCTION);
		return null;
	}

	/***
	 * If Resolver
	 * Gotta resolve all branches possible.
	 */
	@Override
	public Void visitIfStmt(Stmt.If stmt)
	{
		resolve(stmt.condition);
		resolve(stmt.thenBranch);
		if(stmt.elseBranch != null)
			resolve(stmt.elseBranch);
		return null;
	}
	/***
	 * Print Resolver
	 * Print only has one expression to resolve.
	 */
	@Override
	public Void visitPrintStmt(Stmt.Print stmt)
	{
		resolve(stmt.expression);
		return null;
	}
	/***
	 * Return resolver
	 */
	public Void visitReturnStmt(Stmt.Return stmt)
	{
		if(currentFunction == FunctionType.NONE)
		{
			Milk.error(stmt.keyword, "Cannot return from top-level code.");
		}

		if(stmt.value != null)
		{
			if(currentFunction == FunctionType.INITIALIZER)
			{
				Milk.error(stmt.keyword, 
					"Cannot return a value from an initializer.");
			}
			resolve(stmt.value);
		}

		return null;
	}
	
	/***
	 * Var Statement Resolver.
	 * 
	 * Notes are in the helper. 
	 * We first declare it into the environment (doesn't mean var is ready), then we resolve it and finally we define it. 
	 * At this point the var becomes ready.
	 */
	@Override
	public Void visitVarStmt(Stmt.Var stmt)
	{
		declare(stmt.name);
		if(stmt.initializer != null)
		{
			resolve(stmt.initializer);
		}
		define(stmt.name);
		return null;
	}

	/***
	 * While Resolver
	 * Resolve everything
	 */
	@Override
	public Void visitWhileStmt(Stmt.While stmt)
	{
		resolve(stmt.condition);
		resolve(stmt.body);
		return null;
	}
	
	/***
	 * Resolve the expression for the assigned value in case it references other variables.
	 * Resolve the variable that's being assigned to.
	 */
	@Override
	public Void visitAssignExpr(Expr.Assign expr)
	{
		resolve(expr.value);
		resolveLocal(expr, expr.name);
		return null;
	}

	/***
	 * Binary Resolver
	 * Resolve both operands.
	 */
	@Override
	public Void visitBinaryExpr(Expr.Binary expr)
	{
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	/***
	 * Call Resolver
	 * Resolve the callee, and its arguments.
	 */
	@Override
	public Void visitCallExpr(Expr.Call expr)
	{
		resolve(expr.callee);

		for(Expr argument : expr.arguments)
		{
			resolve(argument);
		}

		return null;
	}
	
	/***
	 * Resolve the expression's object
	 */
	@Override
	public Void visitGetExpr(Expr.Get expr)
	{
		resolve(expr.object);
		return null;
	}
	
	/***
	 * Parentheses Resolver
	 * Resolve the inside expression.
	 */
	@Override
	public Void visitGroupingExpr(Expr.Grouping expr)
	{
		resolve(expr.expression);
		return null;
	}
	
	/***
	 * Literal Resolver
	 * Literally nothing
	 */
	@Override
	public Void visitLiteralExpr(Expr.Literal expr)
	{
		return null;
	}

	/***
	 * Logical Resolver
	 * Literally the same as Binary.
	 */
	@Override
	public Void visitLogicalExpr(Expr.Logical expr)
	{
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	/***
	 * Set Resolver
	 * Resolve both the object and its new value.
	 */
	@Override
	public Void visitSetExpr(Expr.Set expr)
	{
		resolve(expr.value);
		resolve(expr.object);
		return null;
	}

	@Override
	public Void visitSuperExpr(Expr.Super expr)
	{
		if(currentClass == ClassType.NONE)
		{
			Milk.error(expr.keyword,
				"Cannot use 'super' outside of a class.");
		} 

		else if (currentClass != ClassType.SUBCLASS)
		{
			Milk.error(expr.keyword,
				"Cannot use 'super' in a class with no superclass");
		}
		resolveLocal(expr, expr.keyword);
		return null;
	}

	@Override
	public Void visitThisExpr(Expr.This expr)
	{
		if(currentClass == ClassType.NONE)
		{
			Milk.error(expr.keyword,
				"Cannot use 'this' outside of a class.");
			return null;
		}
		resolveLocal(expr, expr.keyword);
		return null;
	}
	
	/***
	 * Unary Resolver
	 * Resolve the operand
	 */
	@Override
	public Void visitUnaryExpr(Expr.Unary expr)
	{
		resolve(expr.right);
		return null;
	}
	
	/***
	 * 	Variable Expression Resolver
	 */
	@Override
	public Void visitVariableExpr(Expr.Variable expr)
	{
		if(!scopes.isEmpty() &&
			scopes.peek().get(expr.name.lexeme)==Boolean.FALSE)
		{
			//Occurs if the variable is declared, but not defined.
			Milk.error(expr.name,
				"Cannot read local variable in its own initializer.");
		}

		resolveLocal(expr,expr.name);
		return null;
	}
}