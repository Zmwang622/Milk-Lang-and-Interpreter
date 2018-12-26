package JavaInterpreter.Tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/***
 Use this to automate generating the syntax tree classes for jMilk
 A script that writes each class deifinition, field declaration, constructor,
 and initializer. Prints out the Java code needed to define said class.
*/
public class GenerateAst
{
	public static void main(String[] args) throws IOException
	{
		if(args.length != 1)
		{
			System.err.println("Usage: generate_ast <output directory>");
			System.exit(1);
		}

		String outputDir = args[0];
		/***
		 Needs to have description of each type and its fields.	
		 Expr is the name of the class and the name of the file it outputs.
		*/
		defineAst(outputDir, "Expr", Arrays.asList(          
	      "Assign   : Token name, Expr value",
        "Binary   : Expr left, Token operator, Expr right",
        "Call     : Expr callee, Token paren, List<Expr> arguments",
	      "Get      : Expr object, Token name",
        "Grouping : Expr expression",                      
	      "Literal  : Object value",                         
	      "Logical  : Expr left, Token operator, Expr right",
        "Set      : Expr object, Token name, Expr value",
        "This     : Token keyword",
        "Unary    : Token operator, Expr right",
        "Variable : Token name"            
    	));

    defineAst(outputDir, "Stmt", Arrays.asList(
        "Block      : List<Stmt> statements",
        "Class      : Token name, List<Stmt.Function> methods",
        "Expression : Expr expression",
        "Function   : Token name, List<Token> params, List<Stmt> body",
        "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
        "Print      : Expr expression",
        "Return     : Token keyword, Expr value",
        "Var        : Token name, Expr initializer",
        "While      : Expr condition, Stmt body"
      ));
	}
  private static void defineAst(String outputDir, 
    String baseName, List<String> types) throws IOException
    {
    	String path = outputDir + "/" + baseName + ".java";
    	PrintWriter writer = new PrintWriter(path, "UTF-8");
    	//Use a printWriter to write the classes for us
    	writer.println("package JavaInterpreter.Milk;");
    	writer.println();
    	writer.println("import java.util.List;");
    	writer.println();
    	writer.println("abstract class " + baseName + " {");

    	/***
		 Define the Visitor Interface
		 
		 Visitor pattern: define all of the behavior for a new operation on
		 a set of types in one place, without having to touch the types
		 themselves. 

		 Allows us to define new operations to all ASTs without having to
		 add a method to each class every time
    	*/
    	defineVisitor(writer, baseName, types);

    	//The AST Classes
    	for(String type : types)
    	{
    		String className = type.split(":")[0].trim();
    		String fields = type.split(":")[1].trim();
    		defineType(writer, baseName, className, fields);
    	}
    	
    	writer.println();
    	writer.println("  abstract <R> R accept(Visitor<R> visitor);");
    	
    	writer.println("}");
    	writer.close();
    } 

    private static void defineVisitor(
    	PrintWriter writer, String baseName, List<String> types)
    {
    	writer.println("  interface Visitor<R> {");
    	
 		/***
		 Iterates through all of the subclasses and declares a visit
		 method for each one.
 		*/
    	for(String type : types)
    	{
    		String typeName = type.split(":")[0].trim();
    		writer.println("    R visit" + typeName + baseName + "(" +
    			typeName + " " + baseName.toLowerCase() + ");");
    	}

    	writer.println("  }");
    }
    private static void defineType(
    	PrintWriter writer, String baseName,
    	String className, String fieldList) 
    {
    	writer.println("  static class " + className + " extends " +
    		baseName + " {");
    	
    	//Build the constructor
    	writer.println("    " + className + "(" + fieldList + ") {");

   		//Store parameters in fields.
   		String[] fields = fieldList.split(", ");
   		for(String field : fields)
   		{
   			String name = field.split(" ")[1];
   			writer.println("      this." + name + " = " + name + ";");
   		}
   		writer.println("    }");

   		writer.println();
   		for(String field : fields)
   		{
   			writer.println("    final " + field + ";");
   		}
   		writer.println();
   		writer.println("    <R> R accept(Visitor<R> visitor) {");
   		writer.println("      return visitor.visit" + 
    		className + baseName + "(this);");
   		writer.println("    }");
   		writer.println("  }");
   	}	
}
