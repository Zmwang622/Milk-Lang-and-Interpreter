package JavaInterpreter.Milk;

import java.util.HashMap;
import java.util.Map;
/***
 * Instance for when a class is called.
 * Its to string is so simple I'm not even making javadoc for it.
 */
class MilkInstance
{
	private MilkClass klass;
	private final Map<String, Object> fields = new HashMap<>();

	MilkInstance(MilkClass klass)
	{
		this.klass = klass;
	}

	@Override
	public String toString()
	{
		return klass.name + " instance";
	}

	/***
	 * Used to look up a property of an instance.
	 */
	Object get(Token name)
	{
		if(fields.containsKey(name.lexeme))
		{
			return fields.get(name.lexeme);
		}
		
		//Look for a method with the name of the instance's class.
		MilkFunction method = klass.findMethod(this, name.lexeme);
		
		if(method!=null)
			return method;

		throw new RuntimeError(name, "Undefined property '"+name.lexeme+"'.");		
	}

	//Breathtaking how crazy this method is.
	void set(Token name, Object value)
	{
		fields.put(name.lexeme, value);
	}
}