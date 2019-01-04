package JavaInterpreter.Milk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
/*** 
 The interpreter, currently in its baby phase.
 Contains processing and error handling.
*/
public class Milk
{   
	// hadError ensures that we don't try excuting code when there is a known error
	private static final Interpreter interpreter = new Interpreter();
	//Used to system.exit in runFile()
	static boolean hadError = false;
	static boolean hadRuntimeError = false;
	/*** 
	* Baby steps for now.
	* Main method
	* What else to say. Look below for more deets lol.
	*/
	public static void main(String[] args) throws IOException
	{
		if(args.length>1)
		{
			System.out.println("Usage: jmilk [script]");
			System.exit(64);
		}
		else if(args.length == 1)
		{
			runFile(args[0]);
		}
		else
		{
			runPrompt();
		}
	}

	/*** 
	* Creates an array of byte that is based off a file path, path.; 
	* Then calls run on the newly created array.
	* If there is an error, exit ASAP.
	* 
	* @param path User-given path to the text file that contains the Milk code.
	*/
	private static void runFile(String path) throws IOException 
	{
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String (bytes, Charset.defaultCharset()));
		if(hadError)
		{
			System.exit(65);
		}

		if(hadRuntimeError)
		{
			System.exit(70);
		}
	}
	/***
	* Used when no arguments are given
	* "A more intimate way of using Milk"
	* prints > for each line the user inputs
	* 
	* @throws IOException if input is illegal code.
	*/
	private static void runPrompt() throws IOException
	{
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		for(;;)
		{
			System.out.print("> ");
			run(reader.readLine());
			hadError = false;
		}
	}

	/*** 
	 Note on lexical analysis: 
	 Must scan through the list of characterss and group them 
	 into the smalles sequences that still have meaning. 

	 Each blob (like var) that have meaning are called lexeme.
	 Example:
	 [var] [language] [=] ["Milk"] [;]

	 Must learn how to identify each lexeme
	 
	 @param source is the code given.
	*/
	private static void run(String source)
	{
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
		Parser parser = new Parser(tokens);
		List<Stmt> statements = parser.parse();

		//Stop if there's a syntax error.

		if(hadError)
			return;

		Resolver resolver =new Resolver(interpreter);
		resolver.resolve(statements);

		if(hadError)
			return;
		
		interpreter.interpret(statements);
	}

	/*** 
	* Debugging is incredibly important, more practical issue than CS.
	* When the code work, user only focuses on their program. When code 
	* breaks down we must give them as much information as possible
	* to guide them and help them understand what they did wrong. 
	* 
	* @param line the line #
	* @param message The error message given from where the error occurred.
	*/

	static void error(int line, String message)
	{
		report(line," ",message);
	}
	
	/***
	 * Error-handling for runtime errors.
	 * Prints the line # and error message.
	 */
	static void runtimeError(RuntimeError error)
	{
		System.err.println(error.getMessage() + 
			"\n[line " + error.token.line + "]");
		hadRuntimeError = true;
	}
	/*** 
	 Gives user basic information on the error. 
	 Info : given line, the location, and addt'l message.
	 Good practice to separate the code that generates the errosr from the code that 
	 reports them.
	*/
	private static void report(int line, String where, String message)
	{
		System.err.println("[line " + line + "] Error" + where + ":" + message);
		hadError = true;
	}

	/***
	 Reports an error at a given token. shows token's location and 
	 the token itself. 
	*/
	static void error(Token token, String message)
	{
		if(token.type == TokenType.EOF)
		{
			report(token.line, " at end ", message);
		}

		else
		{
			report(token.line, " at '" + token.lexeme + "'", 
				message);
		}
	}

}

