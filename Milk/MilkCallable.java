package JavaInterpreter.Milk;

import java.util.List;

interface MilkCallable
{
	int arity();
	Object call(Interpreter interpreter, List<Object> arguments);
}