package JavaInterpreter.Milk;
/***
 Scanner Workflow:
 Starts from first character of source code, figures out which lexeme it belongs to, and consumes it and any following charcaters 
 that are part of that lexeme. It emits a token after process the lexeme. 

 Imagine an alligator.  

 The workflow loops over again until it runs out of characters.


 Regular Expressions: Search pattern for strings. Abbreviated is regex.
 Regex rules of Milk match the rules of C. 

*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static JavaInterpreter.Milk.TokenType.*;

class Scanner
{
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	
	//These data values keep track of where we are in the source code.
	private int start = 0;
	private int current = 0;
	private int line = 1;

	//Map of all alphanumeric keywords
	private static final Map<String, TokenType> keywords;

	static
	{
	 keywords = new HashMap<>();
	 keywords.put("and", AND);
	 keywords.put("class", CLASS);
	 keywords.put("else", ELSE);
	 keywords.put("false", FALSE);
	 keywords.put("for", FOR);
	 keywords.put("if", IF);
	 keywords.put("ming", MING);
	 keywords.put("nil", NIL);
	 keywords.put("or", OR);
	 keywords.put("print", PRINT);
	 keywords.put("return", RETURN);
	 keywords.put("super", SUPER);
	 keywords.put("this", THIS);
	 keywords.put("true", TRUE);
	 keywords.put("var", VAR);
	 keywords.put("while", WHILE);
	}
	/***
	 * The Scanner's constructor. 
	 * @param source the given code
	 */
	Scanner(String source)
	{
		this.source = source;
	}

	/***
	 * Scanner method. 
	 * <p>
	 * The scanner works its way through the code and emits tokens until it reaches the end. 
	 */
	List<Token> scanTokens()
	{
		while(!isAtEnd())
		{
			start = current;
			scanToken();
		}
		//Token(TokenType type, String lexeme, Object literal, int line)
		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	/***
	 * Simple helper method that determines if the scanner has reached the end of the code or not. 
	 */
	private boolean isAtEnd()
	{
		return current >= source.length();
	}

	/*** 
	 * Scans each token and processes it accordingly.
	 * <p>
	 * Calls a bunch of helper methods, because why not. 
	 * Each call is based off the current token.
	 */
	private void scanToken()
	{
		char c = advance();
		switch(c)
		{
			//Single Character Lexemes in Milk
			case '(':
				addToken(LEFT_PAREN);
				break;
			case ')':
				addToken(RIGHT_PAREN);
				break;
			case '{':
				addToken(LEFT_BRACE);
				break;
			case '}':
				addToken(RIGHT_BRACE);
				break;
			case ',':
				addToken(COMMA);
				break;
			case '.':
				addToken(DOT);
				break;
			case '-':
				addToken(MINUS);
				break;
			case '+':
				addToken(PLUS);
				break;
			case ';':
				addToken(SEMICOLON);
				break;
			case '*':
				addToken(STAR);
				break;
			//Double-char lexeme. Where is '/' ??
			case '!':
				addToken(match('=')? BANG_EQUAL : BANG);
				break;
			case '=':
				addToken(match('=')? EQUAL_EQUAL : EQUAL);
				break;
			case '<':
				addToken(match('=')? LESS_EQUAL : LESS);
				break;
			case '>':
				addToken(match('=')? GREATER_EQUAL : GREATER);
				break;
			/*** 
			 If we find a '/' after a '/', we dont end the token, because
			 it signifies a comment. Instead keep consuming characters until
			 we reach the end of the line.
			*/
			case '/':
				if(match('/'))
				{
					while(peek()!= '\n' && !isAtEnd())
					{
						advance();
					}
				}
				else
				{
					addToken(SLASH);
				}
				break;
			/***
			 If white space is encountered, we go back to the beginning
			 of the scan loop, starting a new lexeme after the whitespace
			 character.
			*/
			case ' ':                                    
      		case '\r':                                   
      		case '\t':                                   
        		// Ignore whitespace.                      
        		break;

      		case '\n':                                   
        		line++;                                    
        		break;   
        	case '"' :
        		string();
        		break; 
			/***
			 Need to account for when user decides to 
			 throw illegal lexemes. 
			 Despite an error the scanner will still advance(), that 
			 way we don't get stuck in an infinite loop. Also
			 allows us to detect as many errors in one go as possible

			 Thanks to hadError in Milk, we won't execute the code, but
			 will still scan the rest of it.
			*/
			default:
				if(isDigit(c))
				{
					number();
				}

			  	/***
				 Need to account for maximal munch, when two lexical grammar
				 rules can both match a chunk of code that the scanner
				 is looking at.
				
				ex: orchid could be an "or" keyword or a "orchid" 
				identifier.
				
				Because of maximal munch, we can't detect a reserved word
				until the end of what might be an identifier.

				We assume any lexeme w/ a letter or underscore is an
				identifier.
			  	*/
				else if(isAlpha(c))
				{
					identifier();
				}
				else
				{
					Milk.error(line, "Unexpected character.");
				}
				break;
		}
	}

	/***
	 * Method that deals with strings
	 * It keeps processing until it reaches a " or EOF (which is an error).
	 * By calling advance() in the while loop, the scanner updates current while retaining the start position.
	 * Multi lined strings are allowed, but line must get that ++.
	 * When adding the token we add the actual string value as well. It's for the interpreter.
	 */
	private void string()
	{
		while(peek() != '"' && !isAtEnd())
		{
			if(peek()=='\n')
			{
				//Multiline strings are permitted, requiring line updates
				line++;
			}
			advance();
		}

		//Unterminated string
		if(isAtEnd())
		{
			Milk.error(line,"Unterminated string.");
			return;
		}

		//The closing " in the string statement.
		advance();

		//Cut off the "" in the string statemen
		//When creating the token, we also produce the actual string value
		//that will be used later.
		String value = source.substring(start + 1, current-1);
		addToken(STRING, value);
	}
	/***
	 Helper method that processes numbers.
	*/
	private void number()
	{	
		// Find the full digit
		while(isDigit(peek()))
		{
			advance();
		}

		// After finding the full digit, we look for the decimal portion
		// followed by at least one digit
		if(peek() == '.' && isDigit(peekNext()))
		{
			advance();

			//If it is a decimal, get as many #s as possible
			while(isDigit(peek()))
			{
				advance();
			}
		}

		addToken(NUMBER, 
			Double.parseDouble(source.substring(start,current)));
	}

	private void identifier()
	{
		/***
		 Assuming a letter/_ is an identifier we collect 
		 all the alphanumerics. Now we check it with a map of keywords.
 		*/
		while(isAlphaNumeric(peek()))
		{
			advance();
		}

		String text = source.substring(start,current);

		TokenType type = keywords.get(text);
		if(type==null)
		{
			type = IDENTIFIER;
		}
		addToken(type);
	}

	/***
	 * Private helper method that increments the scanner to the next char. 
	 * @return the current character.
	 */
	private char advance()
	{
		current++;
		return source.charAt(current-1);
	}

	//Helper method.
	/***
	 * Private helper method called in scanToken()
	 * <p>
	 * Passes the TokenType type to the overloaded addToken() method.
	 * 
	 * @param type the TokenType given from the scanToken() method.
	 */
	private void addToken(TokenType type)
	{
		addToken(type,null);
	}

	//Adds the token to the list
	//Grabs text of the current lexeme and creates a new token for it
	/***
	 * 
	 * @param type the TokenType
	 * @param literal 
	 */
	private void addToken(TokenType type, Object literal)
	{
		String text = source.substring(start,current);
		tokens.add(new Token(type, text, literal, line));
	}

	/***
	 * Private helper method called in scanToken() that determines the operator
	 * <p>
	 * If the scanner is at the end of the code... it can't match to anything
	 * If the char doesn't match the character at the scanner's current position, returns false;
	 * If they do match, the scanner advances and returns true.
	 * @return true if they match, false if they don't.
	 */
	private boolean match(char expected)
	{
		if(isAtEnd())
		{
			return false;
		}
		if(source.charAt(current) != expected) 
		{
			return false;
		}

		current++;
		return true;
	}
	/***
	 Lookahead method, that only looks at the current character, rather
	 than consume the character. 
	 
	 @return the next character.
	*/
	private char peek()
	{
		if(isAtEnd())
		{
			return '\0';
		}

		return source.charAt(current);
	}
	/***
	 Lookahead method that checks the character after current. Used to 
	 determine whether there is at least one digit after a decimal point.

	 Alternate: Could've had peek take a parameter, but this is more 
	 readable.
	*/
	private char peekNext()
	{
		if(current + 1 >= source.length())
		{
			return '\0';
		}

		return source.charAt(current+1);
	}

	private boolean isAlpha(char c)
	{
		return (c >= 'a' && c <= 'z') ||      
           (c >= 'A' && c <= 'Z') ||      
            c == '_';   
	}
	private boolean isAlphaNumeric(char c)
	{
		return isAlpha(c) || isDigit(c);
	}
	private boolean isDigit(char c)
	{
		return c >= '0' && c <= '9';
	}
}