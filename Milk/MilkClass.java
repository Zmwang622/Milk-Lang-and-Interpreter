package JavaInterpreter.Milk;

import java.util.List;
import java.util.Map;

class MilkClass 
{
	final String name;

	MilkClass(String name)
	{
		this.name = name;
	}	

	@Override
	public String toString()
	{
		return name;
	}
}