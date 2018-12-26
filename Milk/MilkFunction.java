package JavaInterpreter.Milk;

import java.util.List;

class MilkFunction implements MilkCallable
{
	private final Stmt.Function declaration;
	private final Environment closure;

	MilkFunction(Stmt.Function declaration, Environment closure)
	{
		this.declaration = declaration;
		this.closure = closure;
	}

	MilkFunction bind(MilkInstance instance)
	{
		Environment environment = new Environment(closure);
		environment.define("this", instance);
		return new MilkFunction(declartaion, environment);
	}
	
	@Override
	public Object call(Interpreter interpreter, List<Object> arguments)
	{
		Environment environment = new Environment(closure);
		for(int i = 0; i < declaration.params.size(); i++)
		{
			environment.define(declaration.params.get(i).lexeme, arguments.get(i)); 
		}
		
	
		try{
			interpreter.executeBlock(declaration.body, environment);
		} catch(Return returnValue) {
			return returnValue.value;
		}

		return null;
	}

	@Override
	public int arity()
	{
		return declaration.params.size();
	}

	@Override
	public String toString()
	{
		return "<fn " + declaration.name.lexeme + ">";
	}
}