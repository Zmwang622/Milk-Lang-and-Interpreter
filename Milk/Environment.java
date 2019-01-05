package JavaInterpreter.Milk;

import java.util.HashMap;
import java.util.Map;
/***
 * Hash-table based Environment
 * Keys are Variable names
 * Values are the variable's values.
 */
class Environment{
	//Environment reference to the environment that encloses it. Allows traversal
	//Each environment has a reference to its enclosing environment.
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
	
	/***
	 * Method for variable defining.
	 * Variables name is name, its values is value. Wow who'd thought.
	 */
	void define(String name, Object value)
	{
		values.put(name, value);
	}

	Environment ancestor(int distance)
	{
		Environment environment = this;
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

	void assignAt(int distance, Token name, Object value)
	{
		ancestor(distance).values.put(name.lexeme, value);
	}
	
	/***
	 * Variable Lookup Method.
	 * 
	 * @throws RuntimeError if variable name doesn't exist
	 * @return The variable's data. Or a Runtime error.
	 */
	Object get(Token name)
	{
		if(values.containsKey(name.lexeme))
		{
			return values.get(name.lexeme);
		}
		//If the value isn't in this scope, we try its enclosing scope.
		if(enclosing!=null)
			return enclosing.get(name);
		throw new RuntimeError(name, 
			"Undefined variable '" + name.lexeme + "'.");
	}
	
	/***
	 * Changes the value in the environment to value.
	 * @param name The token being changed
	 * @param value the new value for name.
	 */
	void assign(Token name, Object value)
	{
		if(values.containsKey(name.lexeme))
		{
			values.put(name.lexeme, value);
			return;
		}
		//If the variable isn't in the environment, check the outer ones.
		if(enclosing != null)
		{
			enclosing.assign(name,value);
			return;
		}
		throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
	}
}
