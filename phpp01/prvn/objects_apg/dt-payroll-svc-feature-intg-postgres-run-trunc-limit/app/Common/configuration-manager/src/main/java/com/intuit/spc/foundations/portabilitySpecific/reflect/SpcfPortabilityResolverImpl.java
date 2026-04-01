package com.intuit.spc.foundations.portabilitySpecific.reflect;

import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.reflect.SpcfPortabilityResolver;

import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Class implementation of abstract SpcfPortabilityResolver
 *  
 * @author gwang
 *
 */
public class SpcfPortabilityResolverImpl extends SpcfPortabilityResolver 
{

	/**
	 * The mapping between common types in java and C#.
	 */
	protected static Hashtable commonTypeMap = createCommonTypeMap();
	
	/**
	 * Constructor.
	 * 
	 */
	public SpcfPortabilityResolverImpl(){}
	
	/**
	 * Translate a method name to a platform specific name.
	 * Make sure the method name starts with a lowercase letter. 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfPortabilityResolver#doTranslateMethodName(String)
	 */
	protected String doTranslateMethodName(String name)
	{
		// make sure name is not null
		SpcfParamValidator.checkIsNotNull(name, 
				                          "method name");
		if (name.length()>=1)
		{
			return name.substring(0, 1).toLowerCase() + name.substring(1);
		}
		return name;
	}
	/**
	 * Translate a type full name to a platform specific name.
	 *  
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfPortabilityResolver#doTranslateTypeFullName(String) 
	 */
	protected String doTranslateTypeFullName(String name)
	{
		// make sure name is not null
		SpcfParamValidator.checkIsNotNull(name, 
				                          "type full name");
		
		String elemName = name;
		String arrayTail = "";
		String arrayHead = "";
		
		//handle array types name differences in java and C#.		
		// for array type name following java convention, i.e., containing  
		//"[L" at front and ending with ";" 
		if (name.contains("[L") && name.endsWith(";")) 
		{            
            int index = name.indexOf("[L");
            int fIndex = name.indexOf("[");
        
            arrayHead = name.substring(fIndex, index+2);
            
            //Console.WriteLine("name length = " + name.Length);
            //Console.WriteLine("[L index = " + index);
            //Console.WriteLine(" 1st [ index = " + fIndex);                

            //get rid of [L, [[L, etc., at front and ";" at end to get element 
            //type name
            elemName = name.substring(index+2, name.length()-1);
            
            //Console.WriteLine("arrayTail = " + arrayTail);
            //Console.WriteLine("elemName = " + elemName);
            			
		} 
		// for array type name following C# convention, 
		//e.g. java.lang.String[], or System.String[] 
		if (!name.contains("[L") && 
				name.endsWith("[]") && name.length() > 2) 
		{
			int index = name.indexOf("[]");
			arrayTail = name.substring(index, name.length());
			elemName = name.substring(0, index);
			//System.out.println("arrayTail = " + arrayTail);
			//System.out.println("elemName = " + elemName);
		}
		
		//search among common types
		String translatedElemName = (String)commonTypeMap.get(elemName);
		
		//assume portable class (in portable library or written in the SPCF 
		//Portability Language), name starts with either "com." 
		//in java convention or uppercase letter, like "Intuit." or "PayRoll." 
		//in C# convention
		if (translatedElemName == null) 
		{			
			//if (elemName.startsWith("com.intuit."))
			if (elemName.length() != 0 &&  
					Character.isLowerCase(elemName.charAt(0)))
			{
				translatedElemName =  elemName; // no change needed
				
			//} else if (elemName.startsWith("Intuit."))
			} else if (elemName.length() != 0 &&  
					Character.isUpperCase(elemName.charAt(0)))
			{
				int lastDot = elemName.lastIndexOf("."); 
				String simpleName = elemName.substring(lastDot+1);
				String packageName = elemName.substring(0, lastDot);
				StringTokenizer st = new StringTokenizer(packageName, ".");
				StringBuffer strBuf = new StringBuffer("com.");
				while (st.hasMoreTokens()) 
				{
					String word = st.nextToken();
					String newWord = word.substring(0, 1).toLowerCase() + 
						word.substring(1);
					strBuf = strBuf.append(newWord + ".");
			    }
				translatedElemName = strBuf.toString() + simpleName;
			}
		}
							
		if (translatedElemName == null) 
		{
			return name;
		}
		if (arrayTail.length() > 0) 
		{
			for (int i = 0; i < arrayTail.length(); i = i+2) 
			{
				arrayHead = arrayHead + "[";
			}
			arrayHead = arrayHead + "L";
			
			//translated array type name
			return arrayHead + translatedElemName + ";";
		}
		
		if (arrayHead.length() > 0) 
		{
			//translated array type name
			return arrayHead + translatedElemName + ";";
		}
		return translatedElemName;
	}
	
	private static Hashtable createCommonTypeMap()
	{
		Hashtable table = new Hashtable();
		
		//from c# type name to get corresponding java type name 
		table.put("System.String", 	"java.lang.String");	
		table.put("System.SByte",  	"java.lang.Byte");		 
		table.put("System.Int16",  	"java.lang.Short");	   
		table.put("System.Int32",  	"java.lang.Integer");	   
		table.put("System.Int64",  	"java.lang.Long");	   
		table.put("System.Single", 	"java.lang.Float");	   
		table.put("System.Double", 	"java.lang.Double");	   
		table.put("System.Char",   	"java.lang.Character");	   
		table.put("System.Boolean",	"java.lang.Boolean");	   
		table.put("System.Exception", "java.lang.Exception");
		table.put("System.object",	"java.lang.Object");
		//table.put("System.Exception", "java.lang.Throwable");	   
		table.put("System.Type", "java.lang.Class");	   
		table.put("System.ArithmeticException",	"java.lang.ArithmeticException");	   
		table.put("System.NullReferenceException", 
				"java.lang.NullPointerException");		
		table.put("System.InvalidCastException", "java.lang.ClassCastException");	   
		table.put("System.IndexOutOfRangeException", 
				"java.lang.IndexOutOfBoundsException");	   
		table.put("System.ArrayTypeMismatchException", 
				"java.lang.ArrayStoreException");	   
		table.put("Nunit.Framework.Assert",	"junit.framework.Assert");	 

		//from java type name to get java type name itself, make sure users can
		//use both java and c# type name to get the teh corresponding java name
		table.put("java.lang.String", "java.lang.String");	
		table.put("java.lang.Byte", "java.lang.Byte");		 
		table.put("java.lang.Short", "java.lang.Short");	   
		table.put("java.lang.Integer", "java.lang.Integer");	   
		table.put("java.lang.Long", "java.lang.Long");	   
		table.put("java.lang.Float", "java.lang.Float");	   
		table.put("java.lang.Double", "java.lang.Double");	   
		table.put("java.lang.Character", "java.lang.Character");	   
		table.put("java.lang.Boolean",	"java.lang.Boolean");	   
		table.put("java.lang.Exception", "java.lang.Exception");
		table.put("java.lang.Throwable", "java.lang.Throwable");	   
		table.put("java.lang.Class", "java.lang.Class");
		table.put("java.lang.Object", "java.lang.Object");
		table.put("java.lang.ArithmeticException",	"java.lang.ArithmeticException");	   
		table.put("java.lang.NullPointerException", "java.lang.NullPointerException");		
		table.put("java.lang.ClassCastException", "java.lang.ClassCastException");	   
		table.put("java.lang.IndexOutOfBoundsException", 
				"java.lang.IndexOutOfBoundsException");	   
		table.put("java.lang.ArrayStoreException", "java.lang.ArrayStoreException");	   
		table.put("junit.framework.Assert",	"junit.framework.Assert");	 

		
		//for simple types: java name -> java name
		table.put("double", "double");
		table.put("float", "float");
		table.put("char", "char");
		table.put("byte", "byte");
		table.put("boolean", "boolean");
		table.put("int", "int");
		table.put("short", "short");
		table.put("long", "long");
		
		//for simple types: C# name -> java name
		table.put("bool", "boolean");
		return table;
		
	}
}
