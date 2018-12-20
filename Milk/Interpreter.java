package JavaInterpreter.Milk;

class Interpreter implements Expr.Visitor<Object>
{
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
		Object right = evaluate(expr.)

		switch(expr.operator.type)
		{
			
			case BANG:
				return !isTruthy(right);
			//If the operator is a - we know it preceds a number
			case MINUS:
				checkNumberOperand(espr.operator, right);
				return -(double)right;
		}

		return null;
	}

	private void checkNumberOperand(Token operator, Object operand)
	{
		if(operand instanceof Double)
			return;
		throw new RuntimeError(operator, "Operand must be a number.");
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
	 			return (double) left > (double) right;
	 		case GREATER_EQUAL:
	 			return (double) left >= (double) right;
	 		case LESS:
	 			return (double) left < (double) right;
	 		case LESS_EQUAL:
	 			return (double) left <= (double) right;
 	 		case MINUS:
	 			return (double) left - (double) right;
	 		// + could be used to concatenate strings, so must account
	 		// for that
	 		case PLUS:
	 			if(left instanceof Double && right instanceof Double)
	 			{
	 				return (double) left + (double) right;
	 			}

	 			if(left instanceof String && right instanceof String)
	 			{
	 				return (String) left + (String) right;
	 			}
	 		case SLASH:
	 			return (double) left / (double) right;
	 		case STAR:
	 			return (double) left * (double) right;
	 		case BANG_EQUAL:
	 			return !isEqual(left,right);
	 		case EQUAL_EQUAL:
	 			return isEqual(left, right);
	 	}

	 	// Unreachable but necessary
	 	return null;
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
}