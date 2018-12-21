package JavaInterpreter.Milk;

import java.util.HashMap;
import java.util.Map;

class Environment{
	//A map we can map variables to...makes sense
	private final Map<String, Object> values = new HashMap<>();

	void define(String name, Object value)
	{
		values.put(name, value);
	}

	Object get(Token name)
	{
		if(values.containsKey(name.lexeme))
		{
			return values.get(name.lexeme);
		}

		throw new RuntimeError(name, 
			"Undefined variable '" + name.lexeme + "'.");
	}
}
