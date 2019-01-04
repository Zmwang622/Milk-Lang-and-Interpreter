package JavaInterpreter.Milk;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
//Imagine the ASTPrinter, but instead of concatenating strings, it computes values.
class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>
{
	final Environment globals = new Environment();
	private Environment environment = globals;
	private final Map<Expr, Integer> locals = new HashMap<>();

	Interpreter()
	{
		globals.define("clock", new MilkCallable(){
			@Override
			public int arity()
			{
				return 0;
			}

			@Override
			public Object call(Interpreter interpreter,
							   List<Object> arguments)
			{
				return (double) System.currentTimeMillis()/1000.0;
			}

			@Override
			public String toString()
			{
				return "<native fn>";
			}
		});
	}
	/***
	 * This is the method that starts up the whole interpretre.
	 * it takes the list of statements and process them one by one. 
	 * Expressions are within Statements.
	 */
	void interpret(List<Stmt> statements)
	{
		try{
			for(Stmt statement : statements)
			{
				execute(statement);
			}
		} catch (RuntimeError error) {
			Milk.runtimeError(error);
		}
	}
	/***
	 
	 Literal: a bit of syntax that produces a value. Number String
	 
	 Literals almost already are values. Luckily, in the scanner we 
	 already stuffed literal tokens with the runtime value, so for the
	 visit we simply pull the value back out.
	
	 @return the literal's value...pretty simple. 
	*/

	@Override
	public Object visitLiteralExpr(Expr.Literal expr)
	{
		return expr.value;
	}	

	@Override 
	public Object visitLogicalExpr(Expr.Logical expr)
	{
		Object left = evaluate(expr.left);

		if(expr.operator.type == TokenType.OR)
		{
			if(isTruthy(left))
				return left;
		}
		else
		{
			if(!isTruthy(left))
				return left;
		}

		return evaluate(expr.right);
	}

	@Override
	public Object visitSetExpr(Expr.Set expr)
	{
		Object object = evaluate(expr.object);

		if(!(object instanceof MilkInstance))
		{
			throw new RuntimeError(expr.name, "Only instances have fields.");
		}

		Object value = evaluate(expr.value);
		((MilkInstance) object).set(expr.name, value);
		return value;
	}

	@Override
	public Object visitSuperExpr(Expr.Super expr)
	{
		int distance = locals.get(expr);
		MilkClass superclass = (MilkClass) environment.getAt(
			distance, "super");
		MilkInstance object = (MilkInstance) environment.getAt(
			distance - 1, "this");
		MilkFunction method = superclass.findMethod(
			object, expr.method.lexeme);

		if(method == null)
		{
			throw new RuntimeError(expr.method,
				"Undefined property '" + expr.method.lexeme + "' .");
		}
		
		return method;
	}

	@Override
	public Object visitThisExpr(Expr.This expr)
	{
		return lookUpVariable(expr.keyword, expr);
	}
	/***

	 Unary expression: -, !
	 
	 First evaluate the operand expression, then apply the unary 
	 operator itself to the result of that. 

	 Evaluation performs a post-order traversal: each node evaluates 
	 its children before doing its own work.

	 @returns either t
	*/
	@Override
	public Object visitUnaryExpr(Expr.Unary expr)
	{
		//
		Object right = evaluate(expr.right);
		//These getters are extremely specific.
		switch(expr.operator.type)
		{
			//if it's a BANG (!) than return its opposite.
			case BANG:
				return !isTruthy(right);
			//If the operator is a - we know it preceds a number
			case MINUS:
				//Need to make sure the right is a number (that way we can cast it as a double) 
				//Dynamic-Casting happens right here!
				checkNumberOperand(expr.operator, right);
				//Cool that we still use the JVM for stuff.
				return -(double)right;
		}
		//Impossible to reach. 
		return null;
	}

	@Override
	public Object visitVariableExpr(Expr.Variable expr)
	{
		return lookUpVariable(expr.name, expr);
	}

	private Object lookUpVariable(Token name, Expr expr)
	{
		Integer distance = locals.get(expr);
		if(distance != null)
		{
			return environment.getAt(distance, name.lexeme);
		}
		else
		{
			return globals.get(name);
		}
	}
	
	/***
	 * The checkNumberOperand(s)() methods are the error-handling of Milk
	 * They are incredibly important.
	 * They all throw RuntimeError, a custom error class.
	 */

	//Validator that ensures operands are correct
	private void checkNumberOperand(Token operator, Object operand)
	{
		if(operand instanceof Double)
			return;
		throw new RuntimeError(operator, "Operand must be a number.");
	}

	//Similar to previous validator, but checks two operands instead
	private void checkNumberOperands(Token operator, 
										Object left, Object right)
	{	
		if(left instanceof Double && right instanceof Double)
			return;
		throw new RuntimeError(operator, "Operands must be numbers");
	}

	/***
	 
	 Binary Expressions: 
	 Two types:
	 Numerical Binary Expressions (+, -, /, *, %, etc.)
	 a. Involve numbers, so must check number operands.
	 b. Return a number value.
	 Boolean Binary Expressions (!=, ==, >,etc.)
	 a. Some, like > and <, involve numbers, so gotta check there. 
	 b. Others like ,== and !=, don't so you just do the thang.
	 c. Returns a boolean value.
	 Special Case
	 a. + can be used to concatenate strings and numbers.
	 b. Returns a string, if an operand is a string. Otherwise, + returns a number.x
	 @return truly depends on the operator, but it can range from a number to a boolean value. enticing.
	*/

	 //Get the left and right, and then evaluate the middle operator.
	 @Override 
	 public Object visitBinaryExpr(Expr.Binary expr)
	 {
	 	Object left = evaluate(expr.left);
	 	Object right = evaluate(expr.right);

	 	switch(expr.operator.type)
	 	{
	 		case GREATER:
	 			checkNumberOperands(expr.operator, left, right);
	 			return (double) left > (double) right;
	 		case GREATER_EQUAL:
	 			checkNumberOperands(expr.operator, left, right);
	 			return (double) left >= (double) right;
	 		case LESS:
	 			checkNumberOperands(expr.operator, left, right);
	 			return (double) left < (double) right;
	 		case LESS_EQUAL:
	 			checkNumberOperands(expr.operator, left, right);
	 			return (double) left <= (double) right;
 	 		case MINUS:
 	 			checkNumberOperands(expr.operator, left, right);
	 			return (double) left - (double) right;
	 		// + could be used to concatenate strings, so must account
	 		// for that
	 		case PLUS:
	 			if(left instanceof Double && right instanceof Double)
	 			{
	 				return (double) left + (double) right;
	 			}
	 			//Also if one is string and the other double, need to account for that.
	 			//Ex: 2+string returns 2string.
	 			if(left instanceof Double && right instanceof String)
	 			{
	 				String stringLeft = left.toString();
	 				return (String) right + (String) stringLeft;
	 			}

	 			if(left instanceof String && right instanceof Double)
	 			{
	 				String stringRight = right.toString();
	 				return (String) stringRight + (String) left;
	 			}

	 			if(left instanceof String && right instanceof String)
	 			{
	 				return (String) left + (String) right;
	 			}
	 			//Doesn't need validator, since it already checks for types
	 			throw new RuntimeError(expr.operator,
	 				"Operands must be two numbers or two strings");

	 		case SLASH:
	 			checkNumberOperands(expr.operator, left, right);
	 			if((double)right == 0)
	 				return 0;
	 			return (double) left / (double) right;
	 		case STAR:
	 			checkNumberOperands(expr.operator, left, right);
	 			return (double) left * (double) right;
	 		case BANG_EQUAL:
	 			return !isEqual(left,right);
	 		case EQUAL_EQUAL:
	 			return isEqual(left, right);
	 	}

	 	// Unreachable but necessary
	 	return null;
	 }

	@Override
	public Object visitCallExpr(Expr.Call expr)
	{
		Object callee = evaluate(expr.callee);

		List<Object> arguments = new ArrayList<>();
		for(Expr argument : expr.arguments)
		{
			arguments.add(evaluate(argument));
		}

		if(!(callee instanceof MilkCallable))
		{
			throw new RuntimeError(expr.paren,
				"Can only call functions and classes.");
		}

		MilkCallable function = (MilkCallable) callee;
		if(arguments.size() != function.arity())
		{
			throw new RuntimeError(expr.paren, "Expected " +
				function.arity() + " arguments but got " +
				arguments.size() + ".");
		}

		return function.call(this, arguments);
	}
	
	@Override
	public Object visitGetExpr(Expr.Get expr)
	{
		Object object = evaluate(expr.object);
		if(object instanceof MilkInstance)
		{
			return ((MilkInstance) object).get(expr.name);
		}

		throw new RuntimeError(expr.name,
			"Only instances have properties.");
	}
	/***
  	 
	 Grouping: Parentheses

	 Grouping node as reference to an inner node for the expr inside 
	 the parentheses. Recursively evaluate the subexpression and 
	 return it.
	 
	 Grouping is easy because in its node, we have the expression. We use evaluate on the inner expression.
	 What evaluate returns is dependent on what type expression is. If it is a literal, it will use visitLiteralExpr(), if its a binary 
	 expression it uses visitBinaryExpr() etc. etc.
	 
	 @return an object, depends on what type expr.expression is.
	*/	
	@Override
	public Object visitGroupingExpr(Expr.Grouping expr)
	{
		return evaluate(expr.expression);
	}
	
	/***
	 * Sends the expression expr back into the visitor pattern.
	 * @param an expression
	 * @return depends on what type expr is.
	 */
	private Object evaluate(Expr expr)
	{
		return expr.accept(this);
	}
	
	/***
	 * The statement version of evaluate().
	 * 
	 * @return depends on what type statement is.
	 */
	private void execute(Stmt stmt)
	{
		stmt.accept(this);
	}

	void resolve(Expr expr, int depth)
	{
		locals.put(expr, depth);
	}

	void executeBlock(List<Stmt> statements, Environment environment)
	{
		Environment previous = this.environment;
		try
		{
			this.environment = environment;

			for(Stmt statement : statements)
			{
				execute(statement);
			}
		}
		finally
		{
			this.environment = previous;
		}
	}
	@Override
	public Void visitBlockStmt(Stmt.Block stmt)
	{
		executeBlock(stmt.statements, new Environment(environment));
		return null;
	}

	@Override
	public Void visitClassStmt(Stmt.Class stmt)
	{
		Object superclass = null;
		if(stmt.superclass != null)
		{
			superclass = evaluate(stmt.superclass);
			if(!(superclass instanceof MilkClass))
			{
				throw new RuntimeError(stmt.superclass.name,
					"Superclass must be a class.");
			}
		}
		environment.define(stmt.name.lexeme, null);

		if(stmt.superclass != null)
		{
			environment = new Environment(environment);
			environment.define("super", superclass);
		}
		
		Map<String, MilkFunction> methods = new HashMap<>();
		for(Stmt.Function method : stmt.methods)
		{
			MilkFunction function = new MilkFunction(method,environment,
				method.name.lexeme.equals("init"));
			methods.put(method.name.lexeme, function);
		}

		MilkClass klass = new MilkClass(stmt.name.lexeme, 
			(MilkClass) superclass, methods);
		
		if(superclass != null)
		{
			environment = environment.enclosing;
		}

		environment.assign(stmt.name, klass);
		return null;
	}

	@Override
	public Void visitExpressionStmt(Stmt.Expression stmt)
	{
		evaluate(stmt.expression);
		return null;
	}


	@Override
	public Void visitFunctionStmt(Stmt.Function stmt)
	{
		MilkFunction function = new MilkFunction(stmt, environment, 
			false);
		environment.define(stmt.name.lexeme, function);
		return null;
	}

	public Void visitIfStmt(Stmt.If stmt)
	{
		if(isTruthy(evaluate(stmt.condition)))
		{
			execute(stmt.thenBranch);
		}
		else if(stmt.elseBranch!=null)
		{
			execute(stmt.elseBranch);
		}

		return null;
	}

	@Override
	public Void visitPrintStmt(Stmt.Print stmt)
	{
		Object value = evaluate(stmt.expression);
		System.out.println(stringify(value));
		return null;
	}

	@Override
	public Void visitReturnStmt(Stmt.Return stmt)
	{
		Object value = null;
		if(stmt.value != null)
			value = evaluate(stmt.value);

		throw new Return(value);
	}

	@Override
	public Void visitVarStmt(Stmt.Var stmt)
	{
		Object value = null;
		if(stmt.initializer != null)
		{
			value = evaluate(stmt.initializer);
		}

		environment.define(stmt.name.lexeme, value);
		return null;
	}

	@Override
	public Void visitWhileStmt(Stmt.While stmt)
	{
		while(isTruthy(evaluate(stmt.condition)))
		{
			execute(stmt.body);
		}

		return null;
	}

	@Override
	public Object visitAssignExpr(Expr.Assign expr)
	{
		Object value = evaluate(expr.value);

		Integer distance = locals.get(expr);
		if(distance != null)
		{
			environment.assign(expr.name,value);
		} 
		else
		{
			globals.assign(expr.name, value);
		}
		return value;
	}
	/***

	What is truth (in Milk)?
	Must determine what logic operations mean in different 
	situations 

	Truthy: A set of all "true" types
	Falsey: A set of all "false" types

	Extremely arbitrary.

	Milk's rules: false and nil are falsey, everything else
    is truthy.
	
	@return false if the object is null, if its boolean return it. Otherwise, if its not null or boolean, return true.
	*/

	private boolean isTruthy(Object object)
	{
		if(object == null)
			return false;
		if(object instanceof Boolean)
			return (boolean ) object;
		return true;
	}         

	/***
	 * Checks to see if values are equal.
	 * 
	 * @return if both are null, true. If one is null, false. All other cases use .equals()
	 */
	private boolean isEqual(Object a, Object b)
	{
		if(a==null && b == null)
			return true;
		if(a== null)
			return false;
		return a.equals(b);
	}
	
	/***
	 * Originally used in interpret(), before expressions were added.
	 * Turns any object to a string.
	 * 
	 * @return the string version of inputted object.
	 */
	private String stringify(Object object)
	{
		if(object == null)
			return "nil";

		//Hack. Work around Java adding ".0" to integer-valued doubles.
		if(object instanceof Double)
		{
			String text = object.toString();
			if(text.endsWith(".0"))
			{
				text = text.substring(0,text.length()-2);
			}
			return text;
		}

		return object.toString();
	}
}