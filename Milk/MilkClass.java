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
			return methods.get(name).bind(instance);

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
		MilkFunction intializer = methods.get("init");
		if(intializer != null)
		{
			intializer.bind(instance).call(interpreter, arguments);
		}

		return instance;
	}

	@Override
	public int arity()
	{
		 MilkFunction intializer = methods.get("init");
		 if(intializer == null)
		 	return 0;
		 return initializer.arity();
	}
}