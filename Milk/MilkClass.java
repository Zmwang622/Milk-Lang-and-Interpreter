package JavaInterpreter.Milk;

import java.util.List;
import java.util.Map;

class MilkClass implements MilkCallable
{
	final String name;
	private final Map<String, MilkFunction> methods;

	MilkClass(String name, Map<String, MilkFunction> methods)
	{
		this.name = name;
		this.methods = methods;
	}	

	MilkFunction findMethod(MilkInstance instance, String name)
	{
		if(methods.containsKey(name))
			return methods.get(name);

		return null;
	}

	@Override
	public String toString()
	{
		return name;
	
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments)
	{
		MilkInstance instance = new MilkInstance(this);
		return instance;
	}

	@Override
	public int arity()
	{
		return 0;
	}
}