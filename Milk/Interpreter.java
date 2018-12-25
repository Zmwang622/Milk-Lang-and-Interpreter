package JavaInterpreter.Milk;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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
	/***

	 Unary expression: -, !
	 
	 First evaluate the operand expression, then apply the unary 
	 operator itself to the result of that. 

	 Evaluation performs a post-order traversal: each node evaluates 
	 its children before doing its own work.

	*/
	@Override
	public Object visitUnaryExpr(Expr.Unary expr)
	{
		Object right = evaluate(expr.right);

		switch(expr.operator.type)
		{
			
			case BANG:
				return !isTruthy(right);
			//If the operator is a - we know it preceds a number
			case MINUS:
				checkNumberOperand(expr.operator, right);
				return -(double)right;
		}

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
	 
	 Binary Expressions: + - / * % etc.

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

	*/	
	@Override
	public Object visitGroupingExpr(Expr.Grouping expr)
	{
		return evaluate(expr.expression);
	}

	private Object evaluate(Expr expr)
	{
		return expr.accept(this);
	}

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
		environment.define(stmt.name.lexeme, null);

		Map<String, MilkFunction> methods = new HashMap<>();
		for(Stmt.Function method : stmt.methods)
		{
			MilkFunction function = new MilkFunction(method,environment);
			methods.put(method.name.lexeme, function);
		}

		MilkClass klass = new MilkClass(stmt.name.lexeme, methods);
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
		MilkFunction function = new MilkFunction(stmt, environment);
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
	
	*/

	private boolean isTruthy(Object object)
	{
		if(object == null)
			return false;
		if(object instanceof Boolean)
			return (boolean ) object;
		return true;
	}         

	private boolean isEqual(Object a, Object b)
	{
		if(a==null && b == null)
			return true;
		if(a== null)
			return false;
		return a.equals(b);
	}

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