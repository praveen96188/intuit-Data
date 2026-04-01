package com.intuit.spc.foundations.portability.reflect;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;

/**
 * This class provides utilities for resolving reflection portability 
 * discrepancies caused by different naming conventions in different 
 * platforms, including method names, name spaces, type names, etc.
 *  
 * @author gwang
 *
 */
public abstract class SpcfPortabilityResolver 
{

	/**
     * Instance that is used for static methods
     */
    protected static SpcfPortabilityResolver  sResolver = 
    	SpcfFactory.getInstance().createPortabilityResolver(); 
    
	/**
	 * Translate a method name to a platform specific name. 
	 * Assume the java and C# naming conventions for methods are followed, 
	 * i.e., java method name starts with a lowercase letter, while C# 
	 * method name starts with an uppercase letter. This utiliy is mainly 
	 * for tranlating portable classes' method names, where the above method 
	 * naming conventions are forced by J2C or C2J tranlations.
	 *  
	 * @param name The method name to be translated. 
	 * @return A platform appropriate method name in String
	 * @throws SpcfArgumentNullException if name is null.
	 */
	public static String translateMethodName(String name)
	{
		return sResolver.doTranslateMethodName(name);
	}
	
	/**	 
	 * Instance implementation called by translateMethodName(String).
	 *
	 * @param name The method name to be resolved.
	 * @return A platform specific method name. 
	 * @throws SpcfArgumentNullException if name is null.
	 */
	protected abstract String doTranslateMethodName(String name);
	
	/**
	 * Translate a full name of a portable class and any common type supported 
	 * by the SPCF Portability Language to a platform specific full name. 
	 * 
	 * <p>It is assumed that a portable class full name is always in this pattern: 
	 * "com.abc...xyz.ClassA" starting with "com." in java and 
	 * "Abc...Xyz.ClassA" with "com." removed and each word's first letter 
	 * in upper case in C#. The translation rules for common type names across 
	 * java and C# supported by the SPCF Portable Language are the same 
	 * as the type translation rules in J2C or C2J. 
	 * 
	 * <p>Note: Currently, we assume that any portable class full name starts 
	 * with "com.intuit." in java, and starts with "Intuit." in C#. This will 
	 * be extended to allow portable class name to start with just "com." in 
	 * java, and start with any word as long as the first letter is uppercase 
	 * in C#. 
	 * 
	 * @param name The type name to be translated.
	 * @return A platform appropriate full name in String
	 * @throws SpcfArgumentNullException if name is null.
	 */
	public static String translateTypeFullName(String name)
	{
		return sResolver.doTranslateTypeFullName(name);
	}
	
	/**	 
	 * Instance implementation called by translateTypeFullName(String).
	 *
	 * @param name The type full name to be resolved.
	 * @return A platform specific type full name. 
	 * @throws SpcfArgumentNullException if name is null.
	 */
	protected abstract String doTranslateTypeFullName(String name);
	
//	/**
//	 * For testing getMethod and invokeMethod for package protected static 
//	 * methods. Will be removed after figuring out Jim's issue on invoking
//	 * private or package/internal methods.
//	 *
//	static String test(String name) 
//	{
//		if (name == null)
//		{
//			return "Hi, your name?";
//		} else 
//		{
//			return "Hi, " + name;
//		}		
//	}*/
}
