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
	static boolean hadError = false;
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
	*/
	private static void runFile(String path) throws IOException 
	{
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String (bytes, Charset.defaultCharset()));
		if(hadError)
		{
			System.exit(65);
		}
	}
	/***
	* Used when no arguments are given
	* "A more intimate way of using Milk"
	* prints > for each line the user inputs
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
	* Incredibly underwhelmin right now.
	* Can only print on each token because, Scanner and Implementer
	* are not ready.

	 Note on lexical analysis: 
	 Must scan through the list of characterss and group them 
	 into the smalles sequences that still have meaning. 

	 Each blob (like var) that have meaning are called lexeme.
	 Example:
	 [var] [language] [=] ["Milk"] [;]

	 Must learn how to identify each lexeme
	*/
	private static void run(String source)
	{
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();

		for(Token token : tokens)
		{
			System.out.println(token);
		}
	}

	/*** 
	* Debugging is incredibly important, more practical issue than CS.
	* When the code work, user only focuses on their program. When code 
	* breaks down we must give them as much information as possible
	* to guide them and help them understand what they did wrong. 
	*/

	static void error(int line, String message)
	{
		report(line,"",message);
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



}

