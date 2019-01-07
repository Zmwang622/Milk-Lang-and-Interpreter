package JavaInterpreter.Milk;

import java.util.List;
import java.util.Map;
/***
 * Runtime Representation of a Class.
 */
class MilkClass implements MilkCallable
{
	final String name;
	final MilkClass superclass;
	private final Map<String, MilkFunction> methods;

	MilkClass(String name, MilkClass superclass,
		Map<String, MilkFunction> methods)
	{
		this.name = name;
		this.methods = methods;
		this.superclass = superclass;
	}	
	/***
	 * 
	 */
	MilkFunction findMethod(MilkInstance instance, String name)
	{
		if(methods.containsKey(name))
			return methods.get(name).bind(instance);
		if(superclass != null)
		{
			return superclass.findMethod(instance, name);
		}
		return null;
	}

	@Override
	public String toString()
	{
		return name;
	
	}
	
	/***
	 * When a class is called, it instantiates a new class and returns it.
	 */
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
	
	/***
	 * Classes don't have arguments, in the function sense.
	 */
	@Override
	public int arity()
	{
		 MilkFunction initializer = methods.get("init");
		 if(initializer == null)
		 	return 0;
		 return initializer.arity();
	}
}