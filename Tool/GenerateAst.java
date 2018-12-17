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
		 Expr is the name of the calss and the name of the file it outputs.
		*/
		defineAst(outputDir, "Expr", Arrays.asList(          
	      "Binary   : Expr left, Token operator, Expr right",
	      "Grouping : Expr expression",                      
	      "Literal  : Object value",                         
	      "Unary    : Token operator, Expr right"            
    	));
	}
    private static void defineAst(
    String outputDir, String baseName, List<String> types)
    	throws IOException
    {
    	String path = outputDir + "/" + baseName + ".java";
    	PrintWriter writer = new PrintWriter(path, "UTF-8");
    	//Use a printWriter to write the classes for us
    	writer.println("package JavaInterpreter.Milk;");
    	writer.println();
    	writer.println("import java.util.List;");
    	writer.println();
    	writer.println("abstract class " + baseName + " {");

    	for(String type : types)
    	{
    		String className = type.split(":")[0].trim();
    		String fields = type.split(":")[1].trim();
    		defineType(writer, baseName, className, fields);
    	}
    	writer.println("}");
    	writer.close();
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
   	}	
}