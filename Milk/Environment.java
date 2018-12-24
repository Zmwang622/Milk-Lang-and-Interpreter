package JavaInterpreter.Milk;

import java.util.HashMap;
import java.util.Map;

class Environment{
	//Environment reference to the environment that encloses it. Allows traversal
	final Environment enclosing;

	//A map we can map variables to...makes sense
	private final Map<String, Object> values = new HashMap<>();

	Environment()
	{
		enclosing = null;
	}

	Environment(Environment enclosing)
	{
		this.enclosing = enclosing;
	}

	void define(String name, Object value)
	{
		values.put(name, value);
	}

	Environment ancestor(int distance)
	{
		Enivronment environment = this;
		for(int i = 0; i<distance; i++)
		{
			environment = environment.enclosing;
		}

		return environment;
	}
	Object getAt(int distance, String name)
	{
		return ancestor(distance).values.get(name);
	}

	Object get(Token name)
	{
		if(values.containsKey(name.lexeme))
		{
			return values.get(name.lexeme);
		}

		if(enclosing!=null)
			return enclosing.get(name);
		throw new RuntimeError(name, 
			"Undefined variable '" + name.lexeme + "'.");
	}

	void assign(Token name, Object value)
	{
		if(values.containsKey(name.lexeme))
		{
			values.put(name.lexeme, value);
			return;
		}

		if(enclosing != null)
		{
			enclosing.assign(name,value);
			return;
		}
		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}
}
