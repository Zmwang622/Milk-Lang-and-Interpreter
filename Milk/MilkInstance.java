package JavaInterpreter.Milk;

import java.util.HashMap;
import java.util.Map;

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

	Object get(Token name)
	{
		if(fields.containsKey(name.lexeme))
		{
			return fields.get(name.lexeme);
		}

		MilkFunction method = klass.findMethod(this, name.lexeme);
		
		if(method!=null)
			return method;

		throw new RuntimeError(name, "Undefined property '"+name.lexeme+"'.");		
	}

	void set(Token name, Object value)
	{
		fields.put(name.lexeme, value);
	}
}