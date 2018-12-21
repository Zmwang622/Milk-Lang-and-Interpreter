package JavaInterpreter.Milk;

class RuntimeError extends RuntimeException
{
	//Class tracks the token that identifies where in the user's code the error came from
	final Token token;

	RuntimeError(Token token, String message)
	{
		super(message);
		this.token = token;
	}
}