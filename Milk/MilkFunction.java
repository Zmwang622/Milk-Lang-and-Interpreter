package JavaInterpreter.Milk;

import java.util.List;
/***
 * Class that is similar to Stmt.Function.
 * We need MilkFunction because:
 * 1. Need to keep track of parameters so we can bind them to argument values.
 * 2. Need a class that implements MilkCallable, so that way we can use MilkCallable's methods.
 */
class MilkFunction implements MilkCallable
{
	private final Stmt.Function declaration;
	private final Environment closure;
	private final boolean isInitializer;

	MilkFunction(Stmt.Function declaration, Environment closure,
				 boolean isInitializer)
	{
		this.isInitializer = isInitializer;
		this.declaration = declaration;
		this.closure = closure;
	}

	MilkFunction bind(MilkInstance instance)
	{
		Environment environment = new Environment(closure);
		environment.define("this", instance);
		return new MilkFunction(declaration, environment, isInitializer);
	}

	/***
	 * Very powerful code.
	 * Create an environment for each function call.
	 * Add all the parameters into the environment.
	 * Finally execute the block. 
	 * @return 
	 */
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
			if(isInitializer)
				return closure.getAt(0,"this");
			return returnValue.value;
		}

		if(isInitializer)
			return closure.getAt(0, "this");
		return null;
	}
	//yeah yeah
	@Override
	public int arity()
	{
		return declaration.params.size();
	}

	//clean toString.
	@Override
	public String toString()
	{
		return "<fn " + declaration.name.lexeme + ">";
	}
}