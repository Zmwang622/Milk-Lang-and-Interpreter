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

	 Unary expression: -, !, arithmetic operations.
	 
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
				return -(double)right;
		}

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


}