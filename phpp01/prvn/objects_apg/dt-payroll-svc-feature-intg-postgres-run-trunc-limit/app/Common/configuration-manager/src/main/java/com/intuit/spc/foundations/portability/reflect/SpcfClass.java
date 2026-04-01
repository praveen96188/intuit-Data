package com.intuit.spc.foundations.portability.reflect;

import java.io.Serializable;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfClassCastException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfSecurityException;

import com.intuit.spc.foundations.portability.io.SpcfIOException;
import com.intuit.spc.foundations.portability.text.regularExpressions.SpcfPattern;

/**
 * This class provides wrapped methods for the Java Class object and .Net 
 * Type object.
 * <p>
 * Note about the "library" parameters in SpcfClass methods. The library 
 * parameter is only used on the .Net specific libraries.  The library 
 * can have several forms.  First the assembly name is used in a search for 
 * a Type or assembly.  The assembly name string can be any form from the 
 * name of the dll where the '.dll' is removed before being used in an 
 * Assembly.Load call. A second form of the assembly name can be a simple 
 * assembly name.  A third form of the assembly be the 'full name' of the 
 * assembly which will be formatted with the appropriate version etc. See 
 * the AssemblyName.FullName property to see the format of the string 
 * expected.
 * </p>
 * <p>
 * The method 'CreateAssemblyFullName' will aid in the creation of the 
 * assembly full name.  Use this method when one can garnish the strong name 
 * information from another type. This is useful when composing the full name 
 * for a strong named assembly which must contain the version of the assembly 
 * and the version information is the same as a type available known at 
 * runtime.  An example of code is:
 * </p>
 * <pre>
 * library = SpcfClass.CreateAssemblyFullName(
 * 			"Intuit.Spc.Foundations.Primary.Logging", 
 *          typeof(SpcfLogManager));
 * fullClassName = 
 * 			"Intuit.Spc.Foundations.PrimarySpecific.Logging.SpcfLogManagerImpl";
 * mLogMgr = (SpcfLogManager) SpcfClass.CreateInstanceFromName
 * 			(library, fullClassName);
 * </pre>
 */
public abstract class SpcfClass implements Serializable
{
	/**
	 * serialization constant
	 */
	static final long serialVersionUID = 2850360351680894049L;
	
	/**
     * Instance that is used for static methods
     */
    private static SpcfClass sClass = 
    	SpcfFactory.getInstance().createClass();
	    
    /**
     * Indicator for assembly simple name in SPCF type portable representation 
     * format.
     */
    private static final String sAssemblySimpleNameIndicator = 
    	"AssemblySimpleName=";
    
    /**
     * Indicator for separator between full class name and assembly simple name 
     * in SPCF type portable representation format.
     */
    private static final String sSeparatorIndicator = ", ";
    
    /**
     * Indicator for full class name in SPCF type portable representation 
     * format.
     */
    private static final String sFullClassNameIndicator = "FullClassName=";    

    /**
     * Indicator for generic type in SPCF type portable representation 
     * format.
     */
    private static final String  sGenericTypeIndicator = "`";
   
    /**
     * Indicator for array type in SPCF type portable representation 
     * format.
     */
    private static final String sArrayTypeIndicator = "[]";
    
    /**
     * Indicator for generic type starting symbol in SPCF type portable 
     * representation format.
     */
    private static final String sGenericTypeStartIndicator = "[";
    
    /**
     * Indicator for generic type ending symbol in SPCF type portable 
     * representation format.
     */
    private static final String sGenericTypeEndIndicator = "]";
    
    /**
     * Indicator for generic type's type parameter starting symbol in SPCF type 
     * portable representation format.
     */
    private static final String sGenericTypeParamStartIndicator = "[";
    
    /**
     * Indicator for generic type's type parameter ending symbol in SPCF type 
     * portable representation format.
     */
    private static final String sGenericTypeParamEndIndicator = "]";
    
    /**
     * Indicator for separator between generic type's type parameters in SPCF 
     * type portable representation format.
     */
    private static final String sTypeParameterSeparatorIndicator = ",";
    
    /**
     * Assembly simple name pattern.
     */
    private static final String sAssemblySimpleNamePattern = 
    	"[A-Z][a-zA-Z0-9]*([.][A-Z][a-zA-Z0-9]*)*";
    /**
     * Full class name pattern.
     */
    private static final String sClassFullNamePattern = 
    		//"[a-zA-Z][a-zA-Z0-9]*" + "[.][a-zA-Z][a-zA-Z0-9]*" +
    		"[a-zA-Z][a-zA-Z0-9]*" +
    		"([.][a-zA-Z][a-zA-Z0-9]*)*";
    /**
     * Java and C# common simple type string pattern in portable representation.
     */
    private static final String sSimpleTypeStringPattern =     	
    	sFullClassNameIndicator + sClassFullNamePattern;
        
    /**
     * Assembly version pattern.
     */
    private static final String sAssemblyVersionPattern =
    	"[0-9][0-9]*[.][0-9][0-9]*[.][0-9][0-9]*[.][0-9][0-9]*";
    
    /**
     * Assembly information pattern
     */
    private static final String sAssemblyInfoPattern = 
    	sAssemblySimpleNameIndicator + sAssemblySimpleNamePattern  +
    	sSeparatorIndicator + "Version=" + sAssemblyVersionPattern + 
    	sSeparatorIndicator + "Culture=" + "[a-zA-Z0-9][a-zA-Z0-9]*" +
    	sSeparatorIndicator + "PublicKeyToken=" + "[a-zA-Z0-9][a-zA-Z0-9]*";
    
    /**
     * Regular type string pattern.
     */
    private static final String sRegularTypeStringPattern = 
    	sFullClassNameIndicator + sClassFullNamePattern + 
    	sSeparatorIndicator + sAssemblyInfoPattern;
    
    /**
     * Generic type dimension string pattern.
     */
    private static final String sGenericTypeDimensionPattern = "[0-9]";    

    /**
     * Generic type start indicator pattern
     */
    private static final String sGenericTypeStartIndicatorPattern = "\\[";
    
    /**
     * Generic type end indicator pattern
     */
    private static final String sGenericTypeEndIndicatorPattern = "\\]";
    
    /**
     * Generic type's type parameter start indicator pattern
     */
    private static final String sTypeParamStartIndicatorPattern = "\\[";
    
    /**
     * Generic type 's type parameter end indicator pattern
     */
    private static final String sTypeParamEndIndicatorPattern = "\\]";
    
    /**
     * Array indicator pattern
     */
    private static final String sArrayIndicatorPattern = "\\[\\]" + "(\\[\\])*";
    /**
     * Simple type array type string pattern.
     */
    private static final String sSimpleArrayStringPattern = 
    	//sSimpleTypeStringPattern + "(" + sArrayTypeIndicator + ")"+ 
    	//"(" + sArrayTypeIndicator + ")*";
    	sSimpleTypeStringPattern + sArrayIndicatorPattern;
    
    /**
     * Regular type array type  string pattern.
     */
    private static final String sRegularArrayStringPattern =
       	sFullClassNameIndicator + sClassFullNamePattern + 
       	sArrayIndicatorPattern + sSeparatorIndicator + sAssemblyInfoPattern;    	
    
    /**
     * Basic type parameter string pattern.
     */
    private static final String sBasicParameterPattern =
    	sTypeParamStartIndicatorPattern + sSimpleTypeStringPattern + 
    	"|" + sRegularTypeStringPattern + "|" + sSimpleArrayStringPattern +
    	"|" + sRegularArrayStringPattern +
    	sTypeParamEndIndicatorPattern;
    
    /**
     * Generic type string head pattern.
     */
    private static final String sGenericTypeStringHeadPattern = 
    	sFullClassNameIndicator + sClassFullNamePattern + 
    	sGenericTypeIndicator +  sGenericTypeDimensionPattern +    	
    	sGenericTypeStartIndicatorPattern + sBasicParameterPattern + 
    	"(" + sTypeParameterSeparatorIndicator + sBasicParameterPattern + ")*" +
    	sGenericTypeEndIndicatorPattern;
    
    /**
     * Generic type string pattern.
     */
    private static final String sGenericTypeStringPattern = 
    	sGenericTypeStringHeadPattern + sSeparatorIndicator + 
    	 sAssemblyInfoPattern;
    
    /**
     * Generic type parameter string pattern.
     */
    private static final String sGenericParameterPattern =
    	sTypeParamStartIndicatorPattern + sSimpleTypeStringPattern + 
    	"|" + sGenericTypeStringPattern + sTypeParamEndIndicatorPattern;
    
    /**
     * Nested generic type string head pattern.
     */
    private static final String sNestedGenericTypeStringHeadPattern = 
    	sFullClassNameIndicator + sClassFullNamePattern + 
    	sGenericTypeIndicator +  sGenericTypeDimensionPattern + 
    	sGenericTypeStartIndicatorPattern + sBasicParameterPattern + "|" + 
    	sGenericParameterPattern +
    	"(" + sTypeParameterSeparatorIndicator + sBasicParameterPattern + 
    	"|" + sGenericParameterPattern + ")*" +
    	sGenericTypeEndIndicatorPattern;
    
    /**
     * Nested generic type string pattern.
     */
    private static final String sNestedGenericTypeStringPattern = 
    	sNestedGenericTypeStringHeadPattern + sSeparatorIndicator + 
    	sAssemblyInfoPattern;    
    
    /**
     * Generic type array type  string pattern.
     */
    private static final String sGenericArrayStringPattern =
    	sGenericTypeStringHeadPattern + sArrayIndicatorPattern + 
    	sSeparatorIndicator + sAssemblyInfoPattern;    
    
    /**
     * Nested generic type array type  string pattern.
     */
    private static final String sNestedGenericArrayStringPattern =
    	sNestedGenericTypeStringHeadPattern + sArrayIndicatorPattern + 
    	sSeparatorIndicator + sAssemblyInfoPattern; 
    /**
     * Array type string pattern.
     */
    private static final String sArrayTypeStringPattern =
    	sSimpleArrayStringPattern + "|" + sRegularArrayStringPattern + "|" +
    	sGenericArrayStringPattern + "|" + sNestedGenericArrayStringPattern; 
    	
    /**
     * Type portable string representation pattern.
     */
    private static final String sPortableStringPattern = 
    	sSimpleTypeStringPattern + "|" + sRegularTypeStringPattern + "|" + 
    	sGenericTypeStringPattern + "|" + sNestedGenericTypeStringPattern + 
    	sArrayTypeStringPattern; 
    	    	
    /**
     * SPCF type portable representation string format.
     */
    private static final SpcfPattern sPortableRepresentationPattern =
        SpcfPattern.createInstance('^' + sPortableStringPattern + '$');
    
    /**
     * Determine whether the input type string is a valid SPCF portable 
     * representation for types.
     * @param typeStr a string representing a type.
     * @return true if input typeStr follows the portable representation 
     * pattern, otherwise false.
     */
    private static boolean isVaildPortableRepresentation(String typeStr)
    {
    	return sPortableRepresentationPattern.matcher(typeStr).find();    	
    }
    
    /**
     * Returns the class name for the specified object. 
     * The returned string may be different in C#.
     * @param o an object
     * @return the class name for o
     * @throws SpcfArgumentNullException if o is null
     */
	public static String classNameForObject(Object o)
	{		
		return sClass.getClassNameForObject(o);
	}
	
    /**
     * Returns the class name string for the specified class. 
     * The returned string may be different in C#.
     * @param c a Class (java) or Type (.Net)
     * @return the class name string for c. Note: for the same class,
	 * java and C# return in different forms. 
     * @throws SpcfArgumentNullException if c is null
     */
	@SuppressWarnings("unchecked")
	public static String getFullName(Class c)
	{
		return sClass.doGetFullName(c);
	}
	
	/**
	 * Instance implementation called by getFullName()
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getFullName()
	 * @param c a Class (java) or Type (.Net)
     * @return the class name string for c
     * @throws SpcfArgumentNullException if c is null
	 */
	@SuppressWarnings("unchecked")
	protected abstract String doGetFullName(Class c);
	
	/**
	 * Invokes a static method on the specified class. C# needs the name of the 
	 * library, Java does not.
	 * <p>
	 * Note: currently the method only works for non primitive Java types.
	 * </p>
	 * @param library	the name of the library (not used in Java).
	 * @param fullClassName	the fully qualified class name.
	 * @param method the name of the method.
	 * @param args	the arguments to pass to the specified method.
	 * @param usePortabilityResolver whether to use SpcfPortabilityResolver.
	 * @return returns the return object from the specified method.
	 * 
	 * @throws SpcfArgumentNullException if library, fullClassName or method is 
	 * null, or if args is not null but contains a null element.
	 * @throws SpcfInvocationTargetException if the matching method throws 
	 * an exception. 
	 * @throws SpcfIllegalAccessException if the matching method object 
	 * enforces Java language access control and the underlying method is 
	 * inaccessible. 
	 * @throws SpcfClassNoSuchMethodException if there is no matching method.
	 * @throws SpcfClassNotFoundException if class cannot be located.
	 * 
	 */
	public static Object invokeMethod(String library, 
			String fullClassName, String method, Object[] args, 
			boolean usePortabilityResolver)
	{
	    return sClass.doInvokeMethod(library, fullClassName, method, 
	    		args, usePortabilityResolver);
	}
	
	/**
	 * Invokes a static method on the specified class. C# needs the name of the
	 * library, Java does not.
	 * <p>
	 * Note: currently the method only works for non primitive Java types.
	 * </p>
	 * @param library the name of the library (not used in Java).
	 * @param fullClassName	the fully qualified class name.
	 * @param method the name of the method.
	 * @param args	the arguments to pass to the specified method.
	 * @return returns the return object from the specified method.
	 * @throws SpcfArgumentNullException if library, fullClassName and/or method 
	 * are null, or if args is not null but contains a null element.
	 * @throws SpcfInvocationTargetException if the matching method throws 
	 * an exception. 
	 * @throws SpcfIllegalAccessException if the matching method object 
	 * enforces Java language access control and the underlying method is 
	 * inaccessible. 
	 * @throws SpcfClassNoSuchMethodException if there is no matching method.
	 * @throws SpcfClassNotFoundException if class cannot be located.
	 * 
	 */
	public static Object invokeMethod(String library, 
			String fullClassName, String method, Object[] args)
	{
	    return sClass.doInvokeMethod(library, fullClassName, method, 
	    		args, false);
	}
	
	/**
	 * Invokes a static method on the specified class. 
	 * <p>
	 * Note: currently the method only works for non primitive Java types.
	 * </p>
	 * 
	 * @param type	the class or type that has the specified method.
	 * @param method the name of the method.
	 * @param args	the arguments to pass to the specified method.
	 * @return returns the return object from the specified method.
	 * @throws SpcfArgumentNullException if type and/or method are null, or 
	 * if args is not null but contains a null element.
	 * @throws SpcfInvocationTargetException if the matching method throws 
	 * an exception. 
	 * @throws SpcfIllegalAccessException if the matching method object 
	 * enforces Java language access control and the underlying method is 
	 * inaccessible. 
	 * @throws SpcfClassNoSuchMethodException if there is no matching method.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static Object invokeMethod(Class type, String method, Object[] args)
	{
	    return sClass.doInvokeMethod(type, method, args, false);
	}
	
	/**
	 * Invokes a static method on the specified class. 
	 * <p>
	 * Note: currently the method only works for non primitive Java types.
	 * </p>
	 * 
	 * @param type	the class or type that has the specified method.
	 * @param method the name of the method.
	 * @param args	the arguments to pass to the specified method.
	 * @param usePortabilityResolver whether to use SpcfPortabilityResolver
	 * @return returns the return object from the specified method.
	 * @throws SpcfArgumentNullException if type and/or method are null, or 
	 * if args is not null but contains a null element.
	 * @throws SpcfInvocationTargetException if the matching method throws 
	 * an exception. 
	 * @throws SpcfIllegalAccessException if the matching method object 
	 * enforces Java language access control and the underlying method is 
	 * inaccessible. 
	 * @throws SpcfClassNoSuchMethodException if there is no matching method.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static Object invokeMethod(Class type, String method, Object[] args,
			boolean usePortabilityResolver)
	{
	    return sClass.doInvokeMethod(type, method, args, usePortabilityResolver);
	}
	
	/**
	 * Invokes a static method on the specified class. 
	 * <p>
	 * Note: currently the method only works for non primitive Java types.
	 * </p>
	 * 
	 * @param type	the class or type that has the specified method.
	 * @param method the name of the method.
	 * @param args	the arguments to pass to the specified method.
	 * @param argTypes	the arguments types to to find the specified method.
	 * @param usePortabilityResolver whether to use SpcfPortabilityResolver
	 * @return returns the return object from the specified method.
	 * @throws SpcfArgumentNullException if type, method and/or any argTypes 
	 * element are null.
	 * @throws SpcfInvocationTargetException if the matching method throws 
	 * an exception. 
	 * @throws SpcfIllegalAccessException if the matching method object 
	 * enforces Java language access control and the underlying method is 
	 * inaccessible. 
	 * @throws SpcfClassNoSuchMethodException if there is no matching method.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static Object invokeMethod(Class type, String method, Object[] args,
			Class[] argTypes, boolean usePortabilityResolver)
	{
	    return sClass.doInvokeMethod(type, method, args, argTypes, usePortabilityResolver);
	}
	
	/**
	 * Invokes a static method on the specified class. 
	 * <p>
	 * Note: currently the method only works for non primitive Java types.
	 * </p>
	 * 
	 * @param type	the class or type that has the specified method.
	 * @param method the name of the method.
	 * @param args	the arguments to pass to the specified method.
	 * @param argTypes	the arguments types to to find the specified method.
	 * @return returns the return object from the specified method.
	 * @throws SpcfArgumentNullException if type, method and/or any argTypes 
	 * element are null.
	 * @throws SpcfInvocationTargetException if the matching method throws 
	 * an exception. 
	 * @throws SpcfIllegalAccessException if the matching method object 
	 * enforces Java language access control and the underlying method is 
	 * inaccessible. 
	 * @throws SpcfClassNoSuchMethodException if there is no matching method.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static Object invokeMethod(Class type, String method, Object[] args,
			Class[] argTypes)
	{
	    return sClass.doInvokeMethod(type, method, args, argTypes, false);
	}
	/**
	 * Invokes a static method on the specified class, allowing to 
	 * explicitly specify the argument types.
	 * 
	 * C# needs the name of the library, Java does not.
	 * 
	 * If library is not neccesary on C#, for example, loaded types, 
	 * just provide as "", in order to avoid potential naming problems. 
	 * 
	 * @param library the name of the library (not used in Java).
	 * @param fullClassName	the fully qualified class name.
	 * @param method the name of the method.
	 * @param args	the arguments to pass to the specified method.
	 * @param argsTypes	the types of the arguments to pass to the specified 
	 * method.
	 * @param usePortabilityResolver whether to use SpcfPortabilityResolver.
	 * @return returns the return object from the specified method.
	 * @throws SpcfArgumentNullException if fullClassName, method and/or any 
	 * argTypes element are null.
	 * @throws SpcfInvocationTargetException if the matching method throws 
	 * an exception. 
	 * @throws SpcfIllegalAccessException if the matching method object 
	 * enforces Java language access control and the underlying method is 
	 * inaccessible. 
	 * @throws SpcfClassNoSuchMethodException if there is no matching method.
	 * @throws SpcfClassNotFoundException if class cannot be located.
	 * 
	 */
	public static Object invokeMethod(
			String library, String fullClassName, String method, 
			Object[] args, String[] argsTypes, boolean usePortabilityResolver)
	{
	    return sClass.doInvokeMethod(library, fullClassName, 
	    		method, args, argsTypes, usePortabilityResolver);
	}
	
	/**
	 * Invoke a static method, allowing to explicitly specify the argument types.
	 * 
	 * Invokes a static method on the specified class. 
	 * C# needs the name of the library, Java does not.
	 * 
	 * @param library the name of the library (not used in Java).
	 * @param fullClassName	the fully qualified class name.
	 * @param method the name of the method.
	 * @param args	the arguments to pass to the specified method.
	 * @param argsTypes	the types of the arguments to pass to the specified 
	 * method.
	 * @return returns the return object from the specified method.
	 * @throws SpcfArgumentNullException if fullClassName, method and/or any 
	 * argTypes element are null.
	 * @throws SpcfInvocationTargetException if the matching method throws 
	 * an exception. 
	 * @throws SpcfIllegalAccessException if the matching method object 
	 * enforces Java language access control and the underlying method is 
	 * inaccessible. 
	 * @throws SpcfClassNoSuchMethodException if there is no matching method.
	 * @throws SpcfClassNotFoundException if class cannot be located.
	 * 
	 */
	public static Object invokeMethod(
			String library, String fullClassName, String method, 
			Object[] args, String[] argsTypes)
	{
	    return sClass.doInvokeMethod(library, fullClassName, 
	    		method, args, argsTypes, false);
	}
	
	/**
	 * Looks in library for the type with the specified name. In java, library is ignored. 
	 * In .Net, if library is empty, this method searches in the calling 
	 * object's assembly, then in the mscorlib.dll assembly; if library is 
	 * fully qualified with the partial or complete assembly name, this 
	 * method searches in the calling object's assembly, then in the 
	 * mscorlib.dll assembly, finally in the specified assembly.
	 * 
	 * If library is not neccesary on C#, for example, loaded types, 
	 * just provide as "", in order to avoid potential naming problems. 
	 * @param library The library in which to look for.
	 * @param fullClassName The full name of the class, including namespace.
	 * For array type names, e.g., for String[], "[Ljava.lang.String;" is 
	 * expecetd in java, while "System.String[]" is expected in C#. 
	 * In SpcfClass.getType(string, string, boolean), SpcfPortabilityResolver 
	 * will be used to translate type names if usePortabilty is specified as 
	 * true, then either valid java or C# array type naming convention will 
	 * be accepted. 
	 * @return The class with the specified full class name in the given library. 
	 * @throws SpcfClassNotFoundException If the type is an primitive/value type
	 *  array or library couldn't be located. E.g., if the type is not loaded, 
	 *  and library is empty, or library is not empty but does not have a valid 
	 *  file name after suffix .dll is removed; or if the library does not exist.
	 * @throws SpcfArgumentNullException If library is null.
	 */
	@SuppressWarnings("unchecked")
	public static Class getType(String library, String fullClassName)
	{
		return sClass.doGetType(library, fullClassName, false);
	}
	
	/**
	 * Get the type represented by the given type portable string representation. 
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getType(String)
	 * @param portableTypeStr The portable string representation of a class.
	 * @return The class with the full class name in the library in 
	 * the given portable string representation. 
	 * @throws SpcfInvalidTypeStringException if the input portableTypeStr is 
	 * not a valid portable representation for types.
	 * @throws SpcfClassNotFoundException If the type is an primitive/value type
	 *  array or library or type couldn't be located. E.g., if type is not 
	 *  loaded and library is empty string in C# .Net.
	 * @throws SpcfArgumentNullException if input portableTypeStr is null.	 
	 */
	public static SpcfClass getSpcfType(String portableTypeStr) 
	{
		if (!isVaildPortableRepresentation(portableTypeStr)) 
		{
			throw new SpcfInvalidTypeStringException(portableTypeStr);
		}
		return sClass.doGetSpcfType(portableTypeStr);
	}
	
	/**
	 * Get the type represented by the given type portable string representation. 
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getType(String)
	 * @param portableTypeStr The portable string representation of a class.
	 * @return The class with the full class name in the library in 
	 * the given portable string representation. 
	 * @throws SpcfClassNotFoundException if library or type couldn't be located. 
	 * E.g., if type is not loaded and library is empty string in C# .Net.
	 * @throws SpcfArgumentNullException if input portableTypeStr is null.	 
	 */
	protected abstract SpcfClass doGetSpcfType(String portableTypeStr); 	
	

	
	/**
	 * Get the type represented by the given type portable string representation. 
	 * SpcfPortabilityResolver is used to translate the FullClassName part into
     * platform specific. In java, assembly information is ignored. 
	 * In .Net, if assembly information is empty, this method searches in the 
	 * calling	object's assembly, then in the mscorlib.dll assembly; if 
	 * assembly simple name is provided  and/or complete assembly name with 
	 * version, culture and public key token are all provided, this 
	 * method searches in the calling object's assembly, then in the 
	 * mscorlib.dll assembly, finally in the specified assembly.
	 * 
	 * @param portableTypeStr The portable string representation of a class, 
	 * including the library in which to look for.
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#portableRepresentation()
	 *  
	 *  
	 * Regarding the FullClassName part, for array type names, e.g., String[], in 
	 * SpcfClass.getType(String library, String fullClassName), "[Ljava.lang.String;" 
	 * is expecetd in java, while "System.String[]" is expected in C#. But in this 
	 * method and getType(String library, String fullClassName, boolean usePorTabilityResolver), 
	 * SpcfPortabilityResolver is used to translate type names into platform specific 
	 * type names, so either valid java array type name, like "[Ljava.lang.String;" 
	 * or names in C# array type naming convention, like "System.String[]" or 
	 * "jave.lang.String[]" will be accepted in both platforms. 
	 * 
	 * @return The class with the full class name in the library in 
	 * the given canonical string representation. 
	 * @throws SpcfInvalidTypeStringException if the input portableTypeStr is 
	 * not a valid portable representation for types.
	 * @throws SpcfClassNotFoundException if library or type couldn't be located. 
	 * E.g., if type is not loaded and library is empty string in C# .Net.
	 * @throws SpcfArgumentNullException if input portableTypeStr is null.	 
	 */
	@SuppressWarnings("unchecked")
	public static Class getType(String portableTypeStr)
	{
	    //make sure the input is not null
		SpcfParamValidator.checkIsNotNull(portableTypeStr, "portableTypeStr");
	
		if (!isVaildPortableRepresentation(portableTypeStr)) 
		{
			throw new SpcfInvalidTypeStringException(portableTypeStr);
		}
		
		try 
		{
			//parse the canonical string into two parts: library and fullClassName 
			//"FullClassName=" should exist and should not be at the end			          
			String classIndicator = SpcfClass.getFullClassNameIndicator();
			int index = portableTypeStr.indexOf(classIndicator);
			
			int offset = 1; //index starts from 0
            if (index < 0 || index >= portableTypeStr.length() - 
            		classIndicator.length() - offset)
            {
                throw new SpcfClassNotFoundException
                ("The given class' canonical string representation " +
                		portableTypeStr + "is invalid");
            }
            String assemIndicator = SpcfClass.getSeparatorIndicator() + 
            	SpcfClass.getAssemblySimpleNameIndicator();      
            String fullClassName = "";
            String library = "";
            int index2 = portableTypeStr.lastIndexOf(assemIndicator);
            if (index2 > 0) 
            {
	            String tail = portableTypeStr.substring(index2);
	            //library string starts after ", AssemblySimpleName="
	            library = tail.substring(assemIndicator.length());
	
	            String head = portableTypeStr.substring(0, index2);
	            //fullClassName string starts after "FullClassName="
	            fullClassName = head.substring(index + 
	            		classIndicator.length());
            }
            else             
            {
            	fullClassName = portableTypeStr.substring(
            			classIndicator.length());
            }
            
			return sClass.doGetType(library, fullClassName, true);
		}
        catch (Exception e)
        {
            throw new SpcfClassNotFoundException(portableTypeStr, portableTypeStr, e);
        }     
	}
	

	
	
	/**
	 * Creates a platform neutral string representation for this class, 
	 * which can be used to reconstitute the class in both Java and C# .Net 
	 * by using SpcfClass.getType(String portableTypeString).
	 *  
	 *  For instances, String[] type's portable representation may look:
	 *  
	 *  "FullClassName=java.lang.String[]" (if generated in java)
	 *   
	 *   or
	 *   
	 *  "FullClassName=System.String, AssemblySimpleName=mscorlib, Version=2.0.0.0, 
	 *  Culture=neutral, PublicKeyToken=b77a5c561934e089" (if generated in C# .Net)
	 *  
	 *  SpcfClassImpl type's portable representation may look like:
	 *  
	 *  "FullClassName=Intuit.Spc.Foundations.PortabilitySpecific.Reflect.
	 *  SpcfClassImpl, AssemblySimpleName=Intuit.Spc.Foundations.PortabilitySpecific, 
	 *  Version=2.0.5.16, Culture=neutral, PublicKeyToken=7ce6deabcb36a8ea" 
	 *  
	 *  SpcfArrayList[SpcfArrayList[String]] type's portable representation 
	 *  may look like:
	 *  
	 *  "FullClassName=Intuit.Spc.Foundations.Portability.Collections.SpcfArrayList`1[[
	 *  FullClassName=Intuit.Spc.Foundations.Portability.Collections.SpcfArrayList`1[[
	 *  FullClassName=System.String, AssemblySimpleName=mscorlib, Version=2.0.0.0, 
	 *  Culture=neutral, PublicKeyToken=b77a5c561934e089]], AssemblySimpleName=
	 *  Intuit.Spc.Foundations.Portability, Version=2.2.3.0, Culture=neutral, 
	 *  PublicKeyToken=540d4816ead86321]], AssemblySimpleName=Intuit.Spc.
	 *  Foundations.Portability, Version=2.2.3.0, Culture=neutral, 
	 *  PublicKeyToken=540d4816ead86321"
	 *  
	 * @return a platform neutral string representation for this class, 
	 * which can be used to reconstitute the class in both Java and C# .Net.
	 * 
	 * @throws SpcfArgumentNullException if the encapsulated class is null.
	 * @throws SpcfIOException if an I/O error has occurred, or the jar 
	 * file cannot be located, or the found jar manifest does not contain 
	 * assembly information about version, culture and public key token. 
	 * 
	 */
	public abstract String portableRepresentation();
	
	/**
	 * Combine the input full class name and assmbly info into the format 
	 * of portable representation for types.  
	 * 
	 * @param fullClassName The fully qualified class name for a type.
	 * @param assemblyInfo The assmbly name including the assembly simple name,
	 * Version, Culture and PublickeyToken. The class with name as fullClassName
	 * is defined in the assembly specified by assemblyInfo.
	 * @return a string in the format of portable representation for types, 
	 * which can be used to reconstitute the type specified by the input strings 
	 * in both Java and C# .Net.
	 *
	 */
	public static String portableRepresentation(String fullClassName, 
			String assemblyInfo) 
	{
		
		return sFullClassNameIndicator + fullClassName + sSeparatorIndicator + 
			sAssemblySimpleNameIndicator + assemblyInfo;
	}
	/**
	 * Looks in library for the given type. In java, library is ignored. 
	 * In .Net, if library is empty, this method searches in the calling 
	 * object's assembly, then in the mscorlib.dll assembly; if library is 
	 * fully qualified with the partial or complete assembly name, this 
	 * method searches in the calling object's assembly, then in the 
	 * mscorlib.dll assembly, finally in the specified assembly.
	 * @param library The library in which to look for.
	 * @param fullClassName The full name of the class, including namespace.
	 * For array type names, e.g., for String[], "[Ljava.lang.String;" is 
	 * expecetd in java, while "System.String[]" is expected in C#. 
	 * If usePortabilityResolver is true, then either valid java or C# array type 
	 * naming convention will be accepted. 
	 * @param usePortabilityResolver Whether to use SpcfPortabilityResolver 
	 * for resolving platform discrepancies on namespaces.
	 * @return The class with the specified full class name in the given library. 
	 * @throws SpcfClassNotFoundException If library couldn't be located.
	 * @throws SpcfArgumentNullException If library is null.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static Class getType(String library, String fullClassName, 
			boolean usePortabilityResolver)
	{
		return sClass.doGetType(library, fullClassName, usePortabilityResolver);
	}
	
	/**
	 * Instance implementation called by getType().
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getType(String, String, boolean)
	 * @param library The library in which to look for.
	 * @param fullClassName The full name of the class, including namespace.
	 * @param usePortabilityResolver Whether to use SpcfPortabilityResolver. 
	 * for resolving platform discrepancies on namespaces.
	 * @return The class with the specified full class name in the given library 
	 * @throws SpcfClassNotFoundException If library couldn't be located.
	 * @throws SpcfArgumentNullException if usePortabilityResolver is true and 
	 * fullClassName and/or method are null.
	 */
	@SuppressWarnings("unchecked")
	protected abstract Class doGetType(String library, String fullClassName, 
			boolean usePortabilityResolver);
	
	/**
	 * Creates an instance of the specified class. C# needs the name of the
	 * library, Java does not.
	 * 
	 * @param library the name of the library (not used in Java).
	 * @param fullClassName	the fully qualified class name.
	 * @return the instance of the specified class.
	 * @throws SpcfClassNotFoundException if the class cannot be located.
	 * @throws SpcfClassNoSuchMethodException if no matching constructor without 
	 * any parameter is found.
	 * @throws SpcfIllegalAccessException if the class or its nullary 
	 * constructor is not accessible.
	 * @throws SpcfInvocationTargetException if the matching constructor throws 
	 * an exception.
	 * @throws SpcfInstantiationException if this Class represents an abstract 
	 * class, an interface, an array class, a primitive type, or void; or 
	 * if the class has no nullary constructor; or if the instantiation fails 
	 * for some other reason. 
	 * @throws SpcfSecurityException if the caller does not have the necessary 
	 * code access permission in .Net.
	 */
	public static Object createInstanceFromName(String library,
			                                    String fullClassName)
	{
	    return sClass.doCreateInstanceFromName(library, fullClassName, false);
	}
	
	/**
	 * Creates an instance of the specified class. C# needs the name of the
	 * library, Java does not.
	 * 
	 * @param library the name of the library (not used in Java)
	 * @param fullClassName	the fully qualified class name
	 * @param usePortabilityResolver whether to use SpcfPortabilityResolver
	 * @return the instance of the specified class
	 * @throws SpcfArgumentNullException if usePortabilityResolver is true 
	 * and fullClassName is null.
	 * @throws SpcfClassNotFoundException if the class cannot be located.
	 * @throws SpcfClassNoSuchMethodException if no matching constructor without 
	 * any parameter is found.
	 * @throws SpcfIllegalAccessException if the class or its nullary 
	 * constructor is not accessible.
	 * @throws SpcfInvocationTargetException if the matching constructor throws 
	 * an exception.
	 * @throws SpcfInstantiationException if this Class represents an abstract 
	 * class, an interface, an array class, a primitive type, or void; or 
	 * if the class has no nullary constructor; or if the instantiation fails 
	 * for some other reason.
	 * @throws SpcfSecurityException if the caller does not have the necessary 
	 * code access permission in .Net.
	 */
	public static Object createInstanceFromName(String library,
			                                    String fullClassName,
			                                    boolean usePortabilityResolver)
	{
	    return sClass.doCreateInstanceFromName(library, fullClassName, 
	    		usePortabilityResolver);
	}
	
	/**
	 * Creates an instance of the class represented by the specified portable 
	 * representation string.
	 * 
	 * @param portableTypeStr A string descrbing a type full name and its 
	 * corresponding C# type's assembly information in the portable 
	 * representation format.
	 * @return the instance of the type specified by the input string.
	 * @throws SpcfInvalidTypeStringException if the input portableTypeStr is 
	 * not a valid portable representation for types.
	 * @throws SpcfArgumentNullException if input portableTypeStr is null.
	 * @throws SpcfClassNotFoundException if the specified class cannot be located.
	 * @throws SpcfClassNoSuchMethodException if no matching constructor without 
	 * any parameter is found.
	 * @throws SpcfIllegalAccessException if the specified  class or its nullary 
	 * constructor is not accessible.
	 * @throws SpcfInvocationTargetException if the matching constructor throws 
	 * an exception.
	 * @throws SpcfInstantiationException if the specified Class represents an 
	 * abstract class, an interface, an array class, a primitive type, or void; or 
	 * if the class has no nullary constructor; or if the instantiation fails 
	 * for some other reason.
	 * @throws SpcfSecurityException if the caller does not have the necessary 
	 * code access permission in .Net.
	 */
	public static Object createInstanceFromName(String portableTypeStr)
	{
	    return sClass.doCreateInstanceFromName(portableTypeStr);
	}
	/**
	 * Creates an instance of the class represented by the specified portable 
	 * representation string.
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#createInstanceFromName(String)
	 * 
	 * @param portableTypeStr A string descrbing a type full name and its 
	 * corresponding C# type's assembly information in the portable 
	 * representation format.
	 * @return the instance of the type specified by the input string.
	 * 
	 */
	protected abstract Object doCreateInstanceFromName(String portableTypeStr);
	
	/**
	 * Instance implementation called by createInstanceFromName()
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#createInstanceFromName(String, String, boolean)
	 * @param library the name of the library (not used in Java)
	 * @param fullClassName	the fully qualified class name
	 * @param usePortabilityResolver whether to use SpcfPortabilityResolver
	 * @return the instance of the specified class
	 * @throws SpcfArgumentNullException if usePortabilityResolver is true 
	 * and fullClassName is null.
	 * @throws SpcfClassNotFoundException if the class cannot be located.
	 * @throws SpcfIllegalAccessException if the class or its nullary 
	 * constructor is not accessible.
	 * @throws SpcfInstantiationException if this Class represents an abstract 
	 * class, an interface, an array class, a primitive type, or void; or 
	 * if the class has no nullary constructor; or if the instantiation fails 
	 * for some other reason.
	 */
	protected abstract Object doCreateInstanceFromName(String library, 
											  String fullClassName,													
											  boolean usePortabilityResolver);
	
    /**
	 * 
     * Instance implementation called by classNameForObject(). 
	 * @param o an object
	 * @return the class name for o
	 * @throws SpcfArgumentNullException if o is null
	 */
	protected abstract String getClassNameForObject(Object o);
	
	/**
	 * Instance implementation called by invokeMethod().
	 * 
	 * @param library the name of the library (not used in Java).
	 * @param fullClassName	the fully qualified class name.
	 * @param method the name of the method.
	 * @param args	the arguments to pass to the specified method.
	 * @param usePortabilityResolver whether to use SpcfPortabilityResolver
	 * @return returns the return object from the specified method.
	 * @throws SpcfArgumentNullException if library name, fullClassName and/or 
	 * method are null.
	 * @throws SpcfInvocationTargetException if the matching method throws 
	 * an exception. 
	 * @throws SpcfIllegalAccessException if the matching method object 
	 * enforces Java language access control and the underlying method is 
	 * inaccessible. 
	 * @throws SpcfClassNoSuchMethodException if there is no matching method.
	 * 
	 */
	protected abstract Object doInvokeMethod(String library, 
			String fullClassName, String method, Object[] args, 
			boolean usePortabilityResolver);
	
	/**
	 * Instance implementation called by invokeMethod(Class, String, Object[]).
	 * 
	 * @param type	the class or type that has the specified method.
	 * @param method the name of the method.
	 * @param args	the arguments to pass to the specified method.
	 * @param usePortabilityResolver whether to use SpcfPortabilityResolver
	 * @return returns the return object from the specified method.
	 * @throws SpcfArgumentNullException if type and/or method are null.
	 * @throws SpcfInvocationTargetException if the matching method throws 
	 * an exception. 
	 * @throws SpcfIllegalAccessException if the matching method object 
	 * enforces Java language access control and the underlying method is 
	 * inaccessible. 
	 * @throws SpcfClassNoSuchMethodException if there is no matching method.
	 * 
	 */
	@SuppressWarnings("unchecked")
	protected abstract Object doInvokeMethod(Class type, String method, Object[] args, 
			boolean usePortabilityResolver);
	
	/**
	 * Instance implementation called by invokeMethod(String, String, 
	 * String, Object[], String[]).
	 * 
	 * @param library the name of the library (not used in Java).
	 * @param fullClassName	the fully qualified class name.
	 * @param method	the name of the method.
	 * @param args	the arguments to pass to the specified method.
	 * @param argTypes	the arguments types to to find the specified method.
	 * @param usePortabilityResolver whether to use SpcfPortabilityResolver.
	 * @return returns the return object from the specified method.
	 * @throws SpcfArgumentNullException if library, fullClassName and/or method 
	 * are null.
	 * @throws SpcfInvocationTargetException if the matching method throws 
	 * an exception. 
	 * @throws SpcfIllegalAccessException if the matching method object 
	 * enforces Java language access control and the underlying method is 
	 * inaccessible. 
	 * @throws SpcfClassNoSuchMethodException if there is no matching method.
	 * 
	 */
	protected abstract Object doInvokeMethod(String library, 
			String fullClassName, String method, Object[] args, 
			String[] argTypes, boolean usePortabilityResolver);
	
	/**
	 * Instance implementation called by invokeMethod(Class, String, Object[], 
	 * Class[]).
	 * 
	 * @param type the class or type that has the specified method.
	 * @param method the name of the method.
	 * @param args	the arguments to pass to the specified method.
	 * @param argTypes	the arguments types to to find the specified method.
	 * @param usePortabilityResolver whether to use SpcfPortabilityResolver.
	 * @return returns the return object from the specified method.
	 * @throws SpcfArgumentNullException if type and/or method are null.
	 * @throws SpcfInvocationTargetException if the matching method throws 
	 * an exception. 
	 * @throws SpcfIllegalAccessException if the matching method object 
	 * enforces Java language access control and the underlying method is 
	 * inaccessible. 
	 * @throws SpcfClassNoSuchMethodException if there is no matching method.
	 * 
	 */
	@SuppressWarnings("unchecked")
	protected abstract Object doInvokeMethod(Class type, String method, Object[] args, 
			Class[] argTypes, boolean usePortabilityResolver);
	/**
	 * Creates an instance of the class encapsulated by type. For example,
	 * if it contains "System.String" you'll get one of those.
	 *
	 * @param args the args for the constructor.
	 * @param paramTypes the types of the args.
	 * @return the instance of the encapsulated class.
	 * @throws SpcfArgumentNullException if paramTypes is null.
	 * @throws SpcfIllegalArgumentException if the number of arguments is wrong, or
	 * types don't match.
	 * @throws SpcfClassNoSuchMethodException if no matching constructor is found.
	 * @throws SpcfIllegalAccessException if the matching Constructor enforces 
	 * Java language access control and the underlying constructor is inaccessible; or 
	 * or in .Net if the encapsulated type contains generic arguments T, U, etc., 
     * which are not intantiated real types, e.g., typeof(GenericClass&lt;&gt;) 
     * translated from GenericClassA.class in java by J2C.
	 * @throws SpcfIllegalArgumentException if the number of actual and formal 
	 * parameters differ; if an unwrapping conversion for primitive arguments 
	 * fails; or if, after possible unwrapping, a parameter value cannot be 
	 * converted to the corresponding formal parameter type by a method invocation 
	 * conversion; if the matching constructor pertains to an enum type.
	 * @throws SpcfInstantiationException if the class that declares the matching 
	 * constructor represents an abstract class.
	 * @throws SpcfInvocationTargetException if the matching constructor throws 
	 * an exception.
	 * @throws SpcfSecurityException if the caller does not have the necessary 
	 * code access permission in .Net.
 
	 */
	@SuppressWarnings("unchecked")
	public abstract Object createInstanceOfEncapsulatedClass
						(Object[] args, Class[] paramTypes);



	/**
	 * Creates an instance of the specified class. 
	 *
	 * @param type the type to create an instance of.
	 * @return the instance of the specified type.
	 * @throws SpcfArgumentNullException if type is null.
	 * @throws SpcfClassNoSuchMethodException if no matching constructor without 
	 * any parameter is found.
	 * @throws SpcfIllegalAccessException if the matching Constructor enforces 
	 * Java language access control and the underlying constructor is inaccessible, 
	 * or in .Net if the input type contains generic arguments T, U, etc., 
     * which are not intantiated real types, e.g., typeof(GenericClass&lt;&gt;) 
     * translated from GenericClassA.class in java by J2C.
	 * @throws SpcfInstantiationException if the class that declares the matching 
	 * constructor represents an abstract class.
	 * @throws SpcfInvocationTargetException if the matching constructor throws 
	 * an exception.
	 * @throws SpcfSecurityException if the caller does not have the necessary 
	 * code access permission in .Net.
	 */
	@SuppressWarnings("unchecked")
	public static Object createInstanceFromType(Class type) 
	{
		return sClass.doCreateInstanceFromType(type);
	}
	
	/**
     * Method called by createInstanceFromType(Class).
     * @param type The type to create an instance from.
     * @return An instance of the input type.
     */
	@SuppressWarnings("unchecked")
    protected abstract Object doCreateInstanceFromType(Class type);
    
	/**
	 * Creates an instance of the specified type using the constructor that takes the given 
	 * paramter types using the specified auguments.
	 *
	 * @param type the type to create an instance of. 
	 * @param args the arguments for the constructor.
	 * @param paramTypes the parameter types for the constructor to use.
	 * @return the instance of the encapsulated class.
	 * @throws SpcfArgumentNullException if type and/or paramTypes are null.
	 * @throws SpcfIllegalArgumentException if the number of arguments is wrong, or
	 * types don't match.
	 * @throws SpcfClassNoSuchMethodException if no matching constructor is found.
	 * @throws SpcfIllegalAccessException if the matching Constructor enforces 
	 * Java language access control and the underlying constructor is inaccessible; or 
	 * or in .Net if the encapsulated type contains generic arguments T, U, etc., 
     * which are not intantiated real types, e.g., typeof(GenericClass&lt;&gt;) 
     * translated from GenericClassA.class in java by J2C.
	 * @throws SpcfIllegalArgumentException if the number of actual and formal 
	 * parameters differ; if an unwrapping conversion for primitive arguments 
	 * fails; or if, after possible unwrapping, a parameter value cannot be 
	 * converted to the corresponding formal parameter type by a method invocation 
	 * conversion; if the matching constructor pertains to an enum type.
	 * @throws SpcfInstantiationException if the class that declares the matching 
	 * constructor represents an abstract class.
	 * @throws SpcfInvocationTargetException if the matching constructor throws 
	 * an exception.
	 * @throws SpcfSecurityException if the caller does not have the necessary 
	 * code access permission in .Net.
	 */
	@SuppressWarnings("unchecked")
	public static Object createInstanceFromType(Class type,
						Object[] args, Class[] paramTypes) 
	{
		return sClass.doCreateInstanceFromType(type, args, paramTypes); 
	}
	
	/**
	 * Instance implementation called by areAssignable().
	 * @param type the type to create an instance of. 
	 * @param args the arguments for the constructor.
	 * @param paramTypes the parameter types for the constructor to use.
	 * @return the instance of the encapsulated class.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#createInstanceFromType(Class, Object[], Class[])
	 */
	@SuppressWarnings("unchecked")
	protected abstract Object doCreateInstanceFromType(Class type,
			Object[] args, Class[] paramTypes);
	
    /**
     * Instance implementation called by areAssignable()
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#areAssignable(Class, Class)
	 * 
     * @param c1 The Type to compare with the c2.
     * @param c2 The Type to compare with the c1.
     * @return true if c1 and c2 represent the same type, or if c1 is a 
     * superclass or superinterface of c2, false if none of these conditions 
     * are the case, or if c1 is a null reference. 
     * @throws SpcfArgumentNullException is c2 is null.
     */   
	@SuppressWarnings("unchecked")
    protected abstract boolean doAreAssignable(Class c1, Class c2);    
    
    /**
     * Determines whether an instance of c1 can be assigned from an instance of c2. 
     * Return true if c1 and c2 represent the same type, or if c1 is a 
     * superclass or superinterface of c2, false if none of these conditions 
     * are the case, or if c1 is a null reference.     
     * @param c1 The Type to compare with the c2.
     * @param c2 The Type to compare with the c1.
     * @return true if c1 and c2 represent the same type, or if c1 is a 
     * superclass or superinterface of c2, false if none of these conditions 
     * are the case, or if c1 or c2 is a null reference. 
     */   
	@SuppressWarnings("unchecked")
    public static boolean areAssignable(Class c1, Class c2){
        return sClass.doAreAssignable(c1, c2);
    }

	/**
	 * A string containing the fully qualified name of the encapsulated
	 * type, including its namespace 
	 * 
	 * @return the full name of the encapsulated type
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract String getFullName();

	/**
	 * Get the actual type encapsulated by this class.
	 * <p>
	 * This method was written before J2C supports Class. It is kept in this 
	 * version, in order to be backward compatible with Version 2.0. Please 
	 * use getEncapsulatedType() that returns Class instance.
	 * </p> 
	 * @return the actual type encapsulated by this class as an Object 
	 * instance. 
	 */
	public abstract Object getEncapsulatedClassType();

	/**
	 * Get the actual type encapsulated by this class.
	 * 
	 * @return the actual type encapsulated by this class.
	 */
	@SuppressWarnings("unchecked")
	public abstract Class getEncapsulatedType();
	
	/**
	 * Determines whether two instances are of the same Class (java) or Type (.Net)
	 *
	 * @param o1 The first object to compare
	 * @param o2 The second object to compare
	 * @return true if the o1 and o2 parameters are of the same Class (java) 
	 * or Type (.Net), false if these are two different classes or one or both objects are null. 
	 */
	public static boolean areEqual(Object o1, Object o2)
	{
		return sClass.doAreEqual(o1, o2);
	}

	/**
	 * Instance implementation called by areEqual()
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#areEqual(Object, Object)
	 * @param o1 The first object to compare
	 * @param o2 The second object to compare
	 * @return true if the o1 and o2 parameters are of the same Class (java) 
	 * or Type (.Net), false if these are two different classes or one or both objects are null. 
	 */
	protected abstract boolean doAreEqual(Object o1, Object o2);

	/**
	 * Determines whether the encapsulated type is equal to the 
	 * specified object or its encapsulated type
	 *
	 * @param obj The object to compare
	 * 
	 * @return true if the encapsulated type of this class is the same 
	 * as the specified object obj, or its encapsulated type, false if
	 * any of the following conditions are satisfied: 
	 * 
	 * (1) obj is null
	 * 
	 * (2) if obj is not an instance of  SpcfClassImpl and the encapsulated
	 *  type of this class is not the same as obj
	 *  
	 * (3) if obj is an instance of SpcfClassImpl and the encapsulated
	 *  type of this class is not the same as the encapsulated type of obj
	 */
	@Override
	public abstract boolean equals(Object obj);	
	
	/**
	 * Construct assembly full name using an assembly simple name and other 
	 * info garnished from the type. The java implementation returns an empty
	 *  string. This helper method is only implemented on the .Net side.  
	 *  The .Net implementation will take all of the assembly information 
	 *  from the "typeToUse" parameter and then replace the "assemblyName"
	 * parameter for the simple assembly name.  The return of this method 
	 * will be the full name of the AssemblyName class constructed as 
	 * described in the previous sentences.
	 * @param assemblyName the simple name of the assembly
	 * @param typeToUse the type of use to garnish the assembly info from
	 * @return the string containing assembly full name
	 */
	@SuppressWarnings("unchecked")
    public static String createAssemblyFullName(String assemblyName, 
    		                                    Class typeToUse)
    {
        return sClass.doCreateAssemblyFullName(assemblyName, typeToUse);
    }

    /**
     * Instance implementation called by createAssemblyFullName().
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#createAssemblyFullName(String, Class)
     * @param assemblyName the simple name of the assembly.
	 * @param typeToUse the type of use to garnish the assembly info from.
	 * @return the string containing assembly full name.
	 */
	@SuppressWarnings("unchecked")
    protected abstract String doCreateAssemblyFullName(String assemblyName, 
    		                                           Class typeToUse);

    /**
     * Return the full assembly qualified type name of the object passed in.
     * The java implementation returns an empty string. This helper method 
     * only has implementation code on the .Net side.  The .Net 
     * implementation will return the type name qualified with the assembly 
     * information which contains the type.
     * @param t the Type to get the name of.
     * @return the string containing assembly qualified full name of the type.
     * @throws SpcfArgumentNullException if t is null.
     * 
     */
	@SuppressWarnings("unchecked")
    public static String getTypeAssemblyQualifiedName(Class t)
    {
        return sClass.doGetTypeAssemblyQualifiedName(t);
    }

    /**
     * Instance implementation called by getTypeAssemblyQualifiedName.
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getTypeAssemblyQualifiedName(Class)
     * @param t the Type to get the name of.
     * @return the string containing assembly qualified full name of the type.
     * @throws SpcfArgumentNullException if t is null.    
     */
	@SuppressWarnings("unchecked")
    protected abstract String doGetTypeAssemblyQualifiedName(Class t);

	/**
	 * Creates a new SpcfClass instance
	 * @return an SpcfClass instance object
	 */
	public static SpcfClass createInstance()
	{
		return SpcfFactory.getInstance().createClass();
	}
	/**
	 * Creates a new SpcfClass instance.
	 * @param c an instance of Class to encapsulate.
	 * @return an SpcfClass instance object.
	 */
	@SuppressWarnings("unchecked")
	public static SpcfClass createInstance(Class c)
	{
		return SpcfFactory.getInstance().createClass(c);
	}

	
	/**
	 * Creates a new SpcfClass instance.
	 *
	 * @param classInstance an instance of the type desired.
	 * @return an SpcfClass instance object.
	 */
	public static SpcfClass createInstance(Object classInstance)
	{
		
	    return SpcfFactory.getInstance().createClassFromInstance(classInstance);	
	}
	
	/**
	 * Creates a new SpcfClass instance with the given type parameters. 
	 * <p>
	 * This method is for specifying a type's type parameters that are erased at 
	 * java runtime, but needed for portable serialization.
	 * <p>
	 * The input classInstance should not be java.lang.Class or System.Type 
	 * object.
	 *
	 * @param classInstance an instance of the type desired.
	 * @param typeParams type parameters.
	 * @return an SpcfClass instance object.
	 * @throws SpcfArgumentNullException if classInstance is null, or typeParams
	 * is null, or any element of typeParams is null.
	 * @throws SpcfIllegalArgumentException if the input classInstance is 
	 * java.lang.Class or System.Type object. 
	 */
	public static SpcfClass createInstance(Object classInstance, 
			SpcfClass[] typeParams)
	{
		if (classInstance instanceof Class) 
		{
			throw new SpcfIllegalArgumentException("Input classInstance should " +
					"not be java.lang.Class or System.Type object.");
		}
	    return SpcfFactory.getInstance().createClassFromInstance(
	    		classInstance, typeParams);	
	}
	
	/**
	 * Get a constructor declared by this class with the specified parameters.
	 * @param parameterTypes The parameter array. If the constructor to get 
	 * does not have any parameter, pass in an empty array of Class, i.e, 
	 * new Class[0]. For a generic parameter GenericClassA&lt;T, U&gt; to get
	 * its type by using  GenericClassA.class in Java and 
	 * typeof(GenericClassA&lt;&gt;) in C#.  For a generic parameter 
	 * GenericClassA&lt;T, U&gt;, to get its type by using  GenericClassA.class 
	 * in Java and typeof(GenericClassA&lt;, &gt;) in C#.
	 * @return A constrcutor declared by this class, which matches the 
	 * specified parameterTypes.
	 * @throws SpcfArgumentNullException if the encapsulated type, paramTypes 
	 * and/or any parameterTypes element are null.
	 * @throws SpcfClassNoSuchMethodException if no matching constructor is 
	 * found.
	 * @throws SpcfSecurityException if a security manager is set, and the 
	 * access to this constructor or the package of this class is denied 
	 */
	@SuppressWarnings("unchecked")
	public abstract SpcfConstructor getDeclaredConstructor(Class[] parameterTypes);
	
	/**
	 * Get a public constructor  for this class with the specified parameters.
	 * @param parameterTypes The parameter array. If the constructor to get does not
	 * have any parameter, pass in an empty array of Class, i.e, new Class[0].
	 * For a generic parameter GenericClassA&lt;T, U&gt; to get
	 * its type by using  GenericClassA.class in Java and 
	 * typeof(GenericClassA&lt;&gt;) in C#.  For a generic parameter 
	 * GenericClassA&lt;T, U&gt;, to get its type by using  GenericClassA.class 
	 * in Java and typeof(GenericClassA&lt;, &gt;) in C#.
	 * @return A public constrcutor for this class, which matches the specified 
	 * parameterTypes.
	 * @throws SpcfArgumentNullException if the encapsulated type, 
	 * paramTypes and/or any paramTypes element are null.
	 * @throws SpcfClassNoSuchMethodException if no matching constructor is found.
	 * @throws SpcfSecurityException if a security manager is set, and the 
	 * access to this constructor or the package of this class is denied.
	 *  
	 */
	@SuppressWarnings("unchecked")
	public abstract SpcfConstructor getConstructor(Class[] parameterTypes);
	/**
	 * Get the array of public constructors for this class.
	 * @return The array of public constructors for this class.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract SpcfConstructor[] getConstructors();
	
	/**
	 * Get the array of all public and non-public constructors declared 
	 * by this class.
	 * @return The array of all constructors declared this by class.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract SpcfConstructor[] getDeclaredConstructors();
	
	/**
	 * Get the class that declares this member.
	 * @return The class that declares this member. If it is not declared 
	 * as a member by any class, return null.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract SpcfClass getDeclaringClass();

	/**
	 * Get a public field for this class with the specified name.
	 * @param name The name of the field.
	 * @return The field for this class with the specified name.
	 * @throws SpcfArgumentNullException if the encapsulated type and/or name 
	 * are null.
	 * @throws SpcfClassNoSuchFieldException if no matching field is found.
	 * @throws SpcfSecurityException if a security manager is set, and the 
	 * access to this field or the package of this class is denied. 
	 */
	public abstract SpcfField getField(String name);
	
	/**
	 * Get a field, declared or inherited, for this class with the specified 
	 * name.
	 * @param name The name of the field.
	 * @param includeNonPublic if false, searching among only public fields;
	 * otherwise searching among all public and non-public fields.
	 * @return The field for this class with the specified name.
	 * @throws SpcfArgumentNullException if the encapsulated type and/or name 
	 * are null.
	 * @throws SpcfClassNoSuchFieldException if no matching field is found.
	 * @throws SpcfSecurityException if a security manager is set, and the 
	 * access to this field or the package of this class is denied. 
	 */
	public abstract SpcfField getField(String name, boolean includeNonPublic);
	
	/**
	 * Get a field declared this class with the specified field name.
	 * @param name The name of the field.
	 * @return The field declared by this class with the specified name. 
	 * @throws SpcfArgumentNullException if the encapsulated type and/or name 
	 * are null. 
	 * @throws SpcfClassNoSuchFieldException if no matching field is found.
	 * @throws SpcfSecurityException if a security manager is set, and the 
	 * access to this field or the package of this class is denied. 
	 */
	public abstract SpcfField getDeclaredField(String name);
	
	/**
	 * Get the array of fields, declared and inherited. If includeNonPublic 
	 * is false, return only public fields; otherwise return all public and 
	 * non-public fields.
	 * 
	 * @param includeNonPublic if false, return only public fields;
	 * otherwise return all public and non-public fields.
	 * @return The array of public fields for this class.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.	 
	 */
	public abstract SpcfField[] getFields(boolean includeNonPublic);
	
	/**
	 * Get the array of public fields for this class.
	 * @return The array of public fields for this class.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.	 
	 */
	public abstract SpcfField[] getFields();
	
	/**
	 * Get the array of public and non-public fields declared by this class.
	 * @return The array of fields declared for this class.	 
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract SpcfField[] getDeclaredFields();
	
	/**
	 * Get the array of public properties for this class, simulating properties 
	 * in .net. The following rules determine whether getters/setters 
	 * become properties:
	 * 
	 * (1) Only getX() in a class that returns a type and take no parameters 
	 * will become the accessor of property X of that class; 
	 * 
	 * (2)  only setX(parameter) that returnd void and take a single parameter will 
	 * become the set accessor of property X of that class. 
	 * 
	 * This mechanism is consistent with the J2C translation rules for getters, 
	 * setters and properties.
     *
     * Property access modifier rules taken from the C# spec: ECMA-334,
     * pg. 297.
     *
     * The accessor-modifier shall declare an accessibility that is 
     * strictly more restrictive than the declared accessibility of the 
     * property or indexer itself. To be precise:
     * 
     * 1) If the property or indexer has a declared accessibility of 
     *    public, any accessor-modifier can be used.
     * 2) If the property or indexer has a declared accessibility of 
     *    protected internal, the accessor-modifier can be internal, 
     *    protected, or private.
     * 3) If the property or indexer has a declared accessibility of 
     *    internal or protected, the accessor-modifier shall be private.
     * 4) If the property or indexer has a declared accessibility of 
     *    private, no accessor-modifier shall be used.     
     *
     * Trying to be as consistent with C# spec and the current J2C translation 
     * rules as possible, currently we determine a wrapped property's access 
     * modifier in java as the same as that of property's most accessible 
     * accessor. This may beed to be changed in the future once J2C supports 
     * different visibilities for get and set accesors of the same property.
     *   
	 * @return The array of public properties for this class, including 
	 * inherited properties from super classes.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract SpcfProperty[] getProperties();
	
	/**
	 * Get the array of properties, declared and inherited by this type. If 
	 * includeNonPublic is false, return only public properties; otherwise 
	 * return all public and non-public properties.
	 * @param includeNonPublic  Include both public and non-public 
	 * properties if includeNonPublic is true, otherwise inlcuding only public 
	 * properties.
	 * @return An array of matching properties for this class.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract SpcfProperty[] getProperties(boolean includeNonPublic);

	/**
	 * Determine whether this type has a private property, 
	 * with the specified name, that is not declared by this type, 
	 * but exists within the class hierarchy.  A property that has at 
	 * least one set or get method not inherited by this type is considered to 
	 * be private.  Any corresponding get or set method that is inherited by 
	 * this type will be available in the property.  A superclass' method is not 
	 * inherited by a this subtype if the method is private, or if it is package 
	 * proteted but this subtype is defined in a different package in java or it 
	 * is internal but this type is defined in a different assembly. If the 
	 * property has been redeclared by this type the return value will be false.
	 * @param name the name of the property that is being searched.  
	 * @return true if this type has a matching property, otherwise false.	 
	 */
	public abstract boolean hasAncestorPropertyWithPrivateAccessor(String name);
	
	/**
	 * Get the array of private properties that are not declared by
	 * this type, but exist within the class hierarchy.  A property that has at 
	 * least one set or get method
     * not inherited by this type is considered to be private.  Any
     * corresponding get or set method that is inherited by this type will be
     * available in the property.  A superclass' method is not inherited by a 
     * this subtype if the method is private, or if it is package proteted but 
     * this subtype is defined in a different package in java or it is internal 
     * but this type is defined in a different assembly. If the property has 
     * been redeclared by this type, it will not be returned. 
	 * @return An array of matching properties for this class.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
    public abstract SpcfProperty[] getAncestorPropertiesWithPrivateAccessor();
   
    /**
     * Get the private property, with the specified name, that is
     * not declared by this type, but exists within the class
     * hierarchy.  A property that has at least one set or get method
     * not inherited by this type is considered to be private.  Any
     * corresponding get or set method that is inherited by this type will be
     * available in the property.  A superclass' method is not inherited by a 
     * this subtype if the method is private, or if it is package proteted but 
     * this subtype is defined in a different package in java or it is internal 
     * but this type is defined in a different assembly. If the property has been redeclared
     * by this type, it will not be returned. 
	 * @param name the name of the property that is being searched. 
	 * @return A matching property for this class.
	 * @throws SpcfArgumentNullException if the encapsulated type and/or name 
	 * are null.
	 * @throws SpcfClassNoSuchMethodException if there is no matching property.
	 */
    public abstract SpcfProperty getAncestorPropertyWithPrivateAccessor(String name);
     
	/**
	 * Get the property with the specified name, declared or inherited by 
	 * this type. If includeNonPublic is false, search among only public properties; otherwise 
	 * search all public and non-public properties.
	 * @param name the name of the property that is being searched.
	 * @param includeNonPublic  Search both public and non-public 
	 * properties if includeNonPublic is true, otherwise search only public 
	 * properties.
	 * @return A matching property for this class.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 * @throws SpcfClassNoSuchMethodException if there is no matching property.
	 */
	public abstract SpcfProperty getProperty(String name, boolean includeNonPublic);
	
	/**
	 * Determine whether this type has a property with the specified name, declared or inherited. 
	 * If includeNonPublic is false, search among only public properties; otherwise 
	 * search all public and non-public properties.
	 * @param name the name of the property that is being searched.
	 * @param includeNonPublic  Search both public and non-public 
	 * properties if includeNonPublic is true, otherwise search only public 
	 * properties.
	 * @return true if this type has a matching property, otherwise false.	 
	 */
	public abstract boolean hasProperty(String name, boolean includeNonPublic);
	/**
	 * Get the array of all public and non-public properties declared 
	 * by this class, excluding inherited ones. 
	 * 
	 * @return The array of properties declared by this class.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract SpcfProperty[] getDeclaredProperties();

	/**
	 * Get a specific property, public or non-public, declared by this 
	 * class with the specified name 
	 * @param name the name of the property that is being searched
	 * @return a specific property declared by this class with the 
	 * specified name.
	 * @throws SpcfArgumentNullException if the encapsulated type and/or name 
	 * are null.
	 * @throws SpcfClassNoSuchMethodException if there is no matching property. 
	 */
	public abstract SpcfProperty getDeclaredProperty(String name);
	
	/**
	 * Get a specific public property for this class with the specified name.
	 * @param name the name of the property that is being searched.
	 * @return a specific property for this class with the specified name.
	 * @throws SpcfArgumentNullException if the encapsulated type and/or name 
	 * are null.
	 * @throws SpcfClassNoSuchMethodException if there is no matching property. 
	 */
	public abstract SpcfProperty getProperty(String name);
	
	/**
	 * Get the hashcode of this class.
	 * @return an integer as the hashcode. Return 0 if the encapsulated type is null.
	 */
	@Override
	public abstract int hashCode();

	/**
	 * Get an interface implemented by this class with the specified name.
	 * @param name The name of the field.
	 * @return  An interface implemented by this class with the specified name.
	 * @throws SpcfArgumentNullException if the encapsulated type and/or name 
	 * are null.
	 * @throws SpcfClassNotFoundException if there is no matching interface.
	 */
	public abstract SpcfClass getInterface(String name);
	
	/**
	 * Get the interfaces implemented or inherited by the current class.
	 * @return The array of interfaces implemented or inherited by this class.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract SpcfClass[] getInterfaces();
	
	/**
	 * Get a public method for this class or interface with the specified name 
	 * and parameters.
	 * @param name The name of the method to obtain.
	 * @param parameterTypes The parameter array. If the method to get does not
	 * have any parameter, pass in an empty array of Class, i.e, new Class[0].
	 * For a generic parameter GenericClassA&lt;T, U&gt; to get
	 * its type by using  GenericClassA.class in Java and 
	 * typeof(GenericClassA&lt;&gt;) in C#.  For a generic parameter 
	 * GenericClassA&lt;T, U&gt;, to get its type by using  GenericClassA.class 
	 * in Java and typeof(GenericClassA&lt;, &gt;) in C#.
	 * @return A public method that matches the specified parameterTypes. 
	 * @throws SpcfClassNoSuchMethodException if no matching method is found.
	 * @throws SpcfArgumentNullException if the encapsulated type, name, 
	 * parameterTypes and/or any parameterTypes element are null.
	 * @throws SpcfSecurityException if a security manager is set, and the 
	 * access to this method or the package of this class is denied.
	 */
	@SuppressWarnings("unchecked")
	public abstract SpcfMethod getMethod(String name, Class[] parameterTypes);
	
	/**
	 * Get a method for this class or interface with the specified name and 
	 * parameter types, 
	 * searching among inherited and declared, including public and non-public if 
	 * includeNonPublic is true, otherwise inlcuding only public.
	 * @param name The name of the method to obtain.
	 * @param includeNonPublic  Searching among both public and non-public 
	 * methods if includeNonPublic is true, otherwise inlcuding only public 
	 * methods.
	 * @param parameterTypes The parameter type array. If the method to get does not
	 * have any parameter, pass in an empty array of Class, i.e, new Class[0].
	 * For a generic parameter GenericClassA&lt;T, U&gt; to get
	 * its type by using  GenericClassA.class in Java and 
	 * typeof(GenericClassA&lt;&gt;) in C#.  For a generic parameter 
	 * GenericClassA&lt;T, U&gt;, to get its type by using  GenericClassA.class 
	 * in Java and typeof(GenericClassA&lt;, &gt;) in C#.
	 * @return A matching method. 
	 * @throws SpcfClassNoSuchMethodException if no matching method is found.
	 * @throws SpcfArgumentNullException if the encapsulated type, name, 
	 * parameterTypes and/or any parameterTypes element are null.
	 * @throws SpcfSecurityException if a security manager is set, and the 
	 * access to this method or the package of this class is denied.
	 */
	@SuppressWarnings("unchecked")
	public abstract SpcfMethod getMethod(
			String name,
			boolean includeNonPublic,
			Class[] parameterTypes);
	
	/**
	 * Get a method for this class or interface with the specified name and 
	 * parameter types, searching among inherited and declared, including 
	 * public and non-public methods if includeNonPublic is true, otherwise 
	 * inlcuding only public.
	 * @param name The name of the method to obtain.
	 * @param includeNonPublic  Searching among both public and non-public 
	 * methods if includeNonPublic is true, otherwise inlcuding only public 
	 * methods.
	 * @param parameterTypes The parameter type array. If the method to get does not
	 * have any parameter, pass in an empty array of Class, i.e, new Class[0].
	 * For a generic parameter GenericClassA&lt;T, U&gt; to get
	 * its type by using  GenericClassA.class in Java and 
	 * typeof(GenericClassA&lt;&gt;) in C#.  For a generic parameter 
	 * GenericClassA&lt;T, U&gt;, to get its type by using  GenericClassA.class 
	 * in Java and typeof(GenericClassA&lt;, &gt;) in C#.
	 * @param usePortabilityResolver If true then SpcfPortabilityResolver is 
	 * used to translate method name in order to make the method name 
	 * platform appropriate, otherwise users takes care of the method naming 
	 * differences in platforms.
	 * @return A matching method. 
	 * @throws SpcfClassNoSuchMethodException if no matching method is found.
	 * @throws SpcfArgumentNullException if the encapsulated type, name, 
	 * parameterTypes and/or any parameterTypes element are null.
	 * @throws SpcfSecurityException if a security manager is set, and the 
	 * access to this method or the package of this class is denied.
	 */
	@SuppressWarnings("unchecked")
	public abstract SpcfMethod getMethod(
			String name,
			boolean includeNonPublic,
			Class[] parameterTypes,
			boolean usePortabilityResolver);
	
	/**
	 * Get a method declared by this class or interface with the specified name 
	 * and parameters.
	 * @param name The name of the method to obtain. 
	 * @param parameterTypes The parameter array. If the method to get does not
	 * have any parameter, pass in an empty array of Class, i.e, new Class[0].
	 * For a generic parameter GenericClassA&lt;T, U&gt; to get
	 * its type by using  GenericClassA.class in Java and 
	 * typeof(GenericClassA&lt;&gt;) in C#.  For a generic parameter 
	 * GenericClassA&lt;T, U&gt;, to get its type by using  GenericClassA.class 
	 * in Java and typeof(GenericClassA&lt;, &gt;) in C#.
	 * @return A method that is declared by this class and matches the 
	 * specified parameterTypes. 
	 * @throws SpcfClassNoSuchMethodException if no matching method is found.
	 * @throws SpcfArgumentNullException if the encapsulated type,  name, 
	 * parameterTypes and/or any parameterTypes element are null.
	 * @throws SpcfSecurityException if a security manager is set, and the 
	 * access to this method or the package of this class is denied.
	 */
	@SuppressWarnings("unchecked")
	public abstract SpcfMethod getDeclaredMethod(String name, Class[] parameterTypes);
	
	/**
	 * Get a public method for this class or interface with the specified name 
	 * and parameters.
	 * @param name The name of the method to obtain. 
	 * @param parameterTypes The parameter array. If the method to get does not
	 * have any parameter, pass in an empty array of Class, i.e, new Class[0].
	 * For a generic parameter GenericClassA&lt;T, U&gt; to get
	 * its type by using  GenericClassA.class in Java and 
	 * typeof(GenericClassA&lt;&gt;) in C#.  For a generic parameter 
	 * GenericClassA&lt;T, U&gt;, to get its type by using  GenericClassA.class 
	 * in Java and typeof(GenericClassA&lt;, &gt;) in C#.
	 * @param usePortabilityResolver If true then SpcfPortabilityResolver is 
	 * used to translate method name in order to make the method name. 
	 * platform appropriate, otherwise users takes care of the method naming 
	 * differences in platforms.
	 * @return A public method that matches the specified parameterTypes.
	 * @throws SpcfArgumentNullException if the encapsulated type, name, 
	 * parameterTypes and/or any parameterTypes element are null.
	 * @throws SpcfSecurityException if a security manager is set, and the 
	 * access to this method or the package of this class is denied.
	 */
	@SuppressWarnings("unchecked")
	public abstract SpcfMethod getMethod(String name, Class[] parameterTypes, 
			boolean usePortabilityResolver);
	
	/**
	 * Get a method declared by this class or interface with the specified name 
	 * and parameters.
	 * @param name The name of the method to obtain. 
	 * @param parameterTypes The parameter array. If the method to get does not
	 * have any parameter, pass in an empty array of Class, i.e, new Class[0].
	 * For a generic parameter GenericClassA&lt;T, U&gt; to get
	 * its type by using  GenericClassA.class in Java and 
	 * typeof(GenericClassA&lt;&gt;) in C#.  For a generic parameter 
	 * GenericClassA&lt;T, U&gt;, to get its type by using  GenericClassA.class 
	 * in Java and typeof(GenericClassA&lt;, &gt;) in C#.
	 * @param usePortabilityResolver If true then SpcfPortabilityResolver is 
	 * used to translate method  name in order to make the method name. 
	 * platform appropriate, otherwise users takes care of the method naming 
	 * differences in platforms.
	 * @return A method that is declared by this class and matches the 
	 * specified parameterTypes.
	 * @throws SpcfArgumentNullException if the encapsulated type, name, 
	 * parameterTypes and/or any parameterTypes element are null.
	 * @throws SpcfSecurityException if a security manager is set, and the 
	 * access to this method or the package of this class is denied.
	 */
	@SuppressWarnings("unchecked")
	public abstract SpcfMethod getDeclaredMethod(String name, 
			Class[] parameterTypes, boolean usePortabilityResolver);
	
	/**
	 * Get the array of public methods for this class or interface, including those 
	 * declared by the class or interface and those inherited from superclasses and 
	 * superinterfaces.
	 * @return An array of public methods for this class.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract SpcfMethod[] getMethods(); 
	
	/**
	 * Get the array of inherited and declared methods for this class or interface,
	 * including both public and non-public methods if includeNonPublic 
	 * is true, otherwise inlcuding only public methods.
	 * 
	 * @param includeNonPublic  Include both public and non-public 
	 * methods if includeNonPublic is true, otherwise inlcuding only public 
	 * methods.
	 * @return An array of matching methods for this class.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract SpcfMethod[] getMethods(boolean includeNonPublic); 

	/**
	 * Get the array of all public and nonpublic methods declared by this class, 
	 * excluding inherited methods from superclasses. 
	 * @return An array of methods declared by this class.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract SpcfMethod[] getDeclaredMethods();
	
	/**
	 * Get the super class extended by this class
	 * @return The super class extended by this class. If this class does not
	 * have a superclass, return null, i.e., if this Class represents either 
	 * the Object class, an interface, a primitive type, or void, then null is 
	 * returned. If this object represents an array class then the Class object 
	 * representing the Object class is returned. 
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract SpcfClass getSuperClass();

	/**
	 * Get a string containing the name of the Type, excluding 
	 * the namespace of the Type.
	 * @return  The name of the Type, excluding 
	 * the namespace of the Type.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract String getSimpleName();

	/**
	 * A string containing the name of the package this class 
	 * belongs to.
	 * @return the name of of the package this class belongs to.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract String getPackageName();
	
	/**
	 * Determine if this class has an abstract modifier.
	 * @return true if this class has an abstract modifier, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract boolean isAbstract();

	/**
	 * Determine if this object represents an array
	 * @return true if this object represents an array, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract boolean isArray();
	
	/**
	 * Determines whether an instance of the current Type can be assigned 
	 * from an instance of the specified Type; i.e., if the class or interface
	 * represented by this SPcfClass object is either the same as, or is a 
	 * superclass or superinterface of the class or interface represented 
	 * by the specified SpcfClass parameter c.
	 *
	 * @param c instance of SpcfClass encapsulating the class in question
	 * @return true if the c parameter and the current type represent the 
	 * same type, or if the current type is a superclass or superinterface of 
	 * c; false if none of the above conditions are the case, or if the 
	 * encapsulated type or c is a null reference. 
	 */
	public abstract boolean isAssignableFrom(SpcfClass c);  
	
	/**
	 * Determine if this object represents an enumeration.
	 * @return true if this object represents an enumeration, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract boolean isEnum();

	/**
	 * Determine if the specified object is an instance of this class.
	 *
	 * @param obj The object to check
	 * @return true if the specified object is an instance of this class, 
	 * false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract boolean isInstance(Object obj);

	/**
	 * Determine if this object represents an interface.
	 * @return true if this object represents an interface, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract boolean isInterface();


	/**
	 * Determine if this class has a final modifier.
	 * @return true if this class has a final modifier, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract boolean isFinal();
	
	/**
	 * Determine if this object is void or one of the primitive types, or 
	 * their corresponding value types. This method simulates C# Type.IsPrimitive,
	 * but behaves differently from java Class.isPrimitive().
	 * 
	 * @return This method returns true only if 
	 * 1) in Java, this object is void or one of the primitive types,
	 *  namely boolean, byte, char, short, int, long, float, and double,
	 *  or their corresponding predefined Class objects, Void, Boolean, 
	 *  Byte, Char, Short, Integer, Long, Float, and Double; or
	 *  
	 * 2) in C# .Net, if this object is one of the primitive types, namely 
	 * Boolean, Byte, SByte, Int16, UInt16, Int32, UInt32, Int64, UInt64, 
	 * Char, Double, and Single, or their corresponding value types, 
	 * e.g., uint32 for UInt32, bool for Boolean, etc.
	 *  
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract boolean isPrimitive();
	
	/**
	 * Determine if this class has a public modifier.
	 * @return true if this class has a public modifier, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract  boolean isPublic();
	
	/**
	 * Determine if this class is serializable.
	 * @return true if this class is serializable, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	public abstract boolean isSerializable();
    
    /**
     * Determine if this class is a generic type.
     * @return True if this class is a generic type, false otherwise.
     * @throws SpcfArgumentNullException if the encapsulated type is null.
     */
    public abstract boolean isGenericType();
	  
    /**
     * Return the type parameters associated with this SpcfClass.
     * @return An array of type parameters in string format for this SpcfClass 
     * for the enclosed generic type, if it is available.
     * @throws SpcfArgumentNullException if the encapsulated type is null.    
     */
    public abstract String[] getTypeParameters();

    /**
     * Return the type parameters associated with this SpcfClass instance.
     * @return An array of type parameters as SpcfClass instances for this 
     * SpcfClass for the enclosed generic type, if it is available.
     * @throws SpcfArgumentNullException if the encapsulated type is null.    
     */
    public abstract SpcfClass[] getSpcfTypeParameters();
    
    /**
     * Set the type parameters associated with this SpcfClass instance.
     * @param typeParams An array of type parameters as SpcfClass instances for 
     * this SpcfClass for the enclosed generic type.
     * @throws SpcfArgumentNullException if the encapsulated type is null, or 
     * the input typeParams is null or any of its elements is null.    
     */
    public abstract void setSpcfTypeParameters(SpcfClass[] typeParams);
    
	/**
	 * Set the actual type encapsulated by this SpcfClass object. 
	 * 
	 * <p>This method was written before J2C supports Class. It is kept in this 
	 * version, in order to be backward compatible with Version 2.0. Please 
	 * use setEncapsulatedType(Class) that takes a Class instance as input.
	 * </p>
	 * @param c The class to be encapsulated.
	 * @throws SpcfClassCastException if input is not a Class instance.
	 */
	public abstract void setEncapsulatedClassType(Object c);
	
	/**
	 * Set the actual type encapsulated by this SpcfClass object.
	 * @param c The class to be encapsulated.
	 */
	@SuppressWarnings("unchecked")
	public abstract void setEncapsulatedType(Class c);
	
	/**
	 * Obtain a string describing this class.
	 * @return A string that describes this class. Return empty string if the 
	 * encapsulated type is null. Note: for the same class, java and C# return 
	 * in different forms. 
	 */
	@Override
	public abstract String toString();
	
	/**
	 * Determines whether this class derives from the specified class.
	 * @param c A possible super class.
	 * @return true if this class and the specified class c are classes 
	 * (not interfaces) and this class derives from the specified class, 
	 * otherwise false.
	 * @throws SpcfArgumentNullException if the encapsulated type and/or c are null.
	 */
	@SuppressWarnings("unchecked")
	public abstract boolean isSubclassOf(Class c);	

	/**
	 * Determines whether this class is the same as or derives from the 
	 * specified class.
	 * @param c A possible super class.
	 * @return true if this class and c are classes (not interfaces) and 
	 * this class is the same as or derives from the specified class, 
	 * otherwise false.
	 * @throws SpcfArgumentNullException if the encapsulated type and/or c are null.
	 */
	@SuppressWarnings("unchecked")
	public abstract boolean isSameOrSubclassOf(Class c);	
	
	/**
	 * Determines whether this class derives from the type encapsulated 
	 * by the specified spcfclass' encapsulated type.
	 * @param c A possible super class.
	 * @return true if this class and the encapsulated type of c are classes 
	 * (not interfaces) and this class derives from the encapsulated type of 
	 * the specified spcfclass.
	 * @throws SpcfArgumentNullException if the encapsulated type, c and/or the 
	 * encapsulated type of c are null.
	 */
	public abstract boolean isSubclassOf(SpcfClass c);	
	
	/**
	 * Determines whether this class is the same as or derives from the 
	 * type encapsulated by the specified spcfclass' encapsulated type.
	 * @param c A class to compare.
	 * @return true if this class and the encapsulated type of c are classes 
	 * (not interfaces) 
	 * and this class is the same as or derives from the encapsulated type of 
	 * the specified spcfclass.
	 * @throws SpcfArgumentNullException if the encapsulated type, c and/or the 
	 * encapsulated type of c are null.
	 */
	public abstract boolean isSameOrSubclassOf(SpcfClass c);
	
	/**
	 * Get all annotations present on this class.
	 * @return array of SpcfAnnotations representing all annotations on this 
	 * class. Return an empty array if this class has no annotations.
	 * @throws SpcfArgumentNullException if encapsulated type is null.
	 */
	public abstract SpcfAnnotation[] getAnnotations();
	
	/**
	 * Get the annotation of the specified type present on this class.
	 * @param type The annotation type to be searched for.
	 * @return Returns this element's annotation of the specified type if 
	 * such an annotation is present, else null.
	 * @throws SpcfArgumentNullException if encapsulated type or type is 
	 * null.	 
	 */
	@SuppressWarnings("unchecked")
	public abstract SpcfAnnotation getAnnotation(Class type);
	
	/**
	 * Determine whether this element has the specified type annotation.
	 * @param type the annotation type to search for.
	 * @return ture if this element has the specified type annotation, 
	 * else false.
	 * @throws SpcfArgumentNullException if encapsulated type or type is 
	 * null.
	 */
	@SuppressWarnings("unchecked")
	public abstract boolean isAnnotationPresent(Class type);
	
	/**
	 * Get a method declared in this class or interface hierarchy with the 
	 * specified name and parameters. It searches for the matching method from 
	 * all methods declared by this class and then all methods by its super 
	 * classes up all the way till the root in the class hierarchy.
	 * @param name The name of the method to obtain. 
	 * @param parameterTypes The parameter array. If the method to get does not
	 * have any parameter, pass in an empty array of Class, i.e, new Class[0].
	 * For a generic parameter GenericClassA&lt;T, U&gt; to get
	 * its type by using  GenericClassA.class in Java and 
	 * typeof(GenericClassA&lt;&gt;) in C#.  For a generic parameter 
	 * GenericClassA&lt;T, U&gt;, to get its type by using  GenericClassA.class 
	 * in Java and typeof(GenericClassA&lt;, &gt;) in C#.
	 * @param usePortabilityResolver If true then SpcfPortabilityResolver is 
	 * used to translate method name in order to make the method name 
	 * platform appropriate, otherwise users takes care of the method naming 
	 * differences in platforms.
	 * @return A method that matches the specified name and parameterTypes.
	 * @throws SpcfArgumentNullException if the encapsulated type, name, 
	 * parameterTypes and/or any parameterTypes element are null.
	 * @throws SpcfSecurityException if a security manager is set, and the 
	 * access to this method or the package of this class is denied.
	 */
	@SuppressWarnings("unchecked")
	public abstract SpcfMethod getMethodInHierarchy(String name, 
			Class[] parameterTypes, 
			boolean usePortabilityResolver);
	
	/**
	 * Get a portable string representation for a generic type by embedding its 
	 * given innerTypeStr into its given outerTypeStr as a part of its class 
	 * name. 
	 * @param innerTypeStr The portable string representation for the inner type  
	 * which may be a generic type or a non-generic type.
	 * 
	 * @return A string for the nested type portable representation.
	 * @throws SpcfInvalidTypeStringException if the given innerTypeStr is 
	 * invalid portable representation.
	 *
	protected abstract String nestPortableRepresentation(String innerTypeStr);
	*/
	/**
	 * Append two portable type representaion strings for two type parameters of 
	 * a generic type. This method needs to be used multiple times for 
	 * appending more than two type parameters' portable representation together.
	 * 
	 * @param str1 The portable type representaion string for a type parameter 
	 * whose position is before another type parameter.
	 * @param str2 The portable type representaion string for a type parameter 
	 * whose position is after another type parameter.
	 *
	 * @return A string that is the concatenation of str1, "," and str2. This is 
	 * the C# way of embedding type parameters in its generic type string 
	 * representation.
	 *
	protected static String appendPortableRepresentation(String str1, String str2) 
	{
		return str1 + "," + str2;
	}
	*/
	/**
	 * Return the full class name indicator in portable representation format.
	 * @return A string for the full class name indicator in portable 
	 * representation format.
	 */
	protected static String getFullClassNameIndicator() 
	{
		return sFullClassNameIndicator;
	}
	
	/**
	 * Return the assembly simple name indicator in portable representation 
	 * format.
	 * @return A string for the assembly simple name indicator in portable 
	 * representation format.
	 */
	protected static String getAssemblySimpleNameIndicator() 
	{
		return sAssemblySimpleNameIndicator;
	}
	
	/**
	 * Return the separator between the full class name and the assembly simple 
	 * name in the portable representation format.
	 * @return A string for the separator between the full class name and the assembly simple 
	 * name in the portable representation format.
	 */
	protected static String getSeparatorIndicator() 
	{
		return sSeparatorIndicator;
	}
	
	/**
	 * Return the generic type indicator in the portable representation format.
	 * @return A string for the generic type indicator in the portable 
	 * representation format. 
	 * 
	 */
	protected static String getGenericTypeIndicator() 
	{
		return sGenericTypeIndicator;
	}
	
	/**
	 * Return the array type indicator in the portable representation format.
	 * @return A string for the array type indicator in the portable 
	 * representation format. 
	 * 
	 */
	protected static String getArrayTypeIndicator() 
	{
		return sArrayTypeIndicator;
	}
	
	/**
     * Return the indicator for separator between generic type's type parameters 
     * in SPCF type portable representation format.
     * @return the indicator for separator between generic type's type parameters 
     * in SPCF type portable representation format.
     */
    protected static String getTypeParameterSeparatorIndicator()
    {
    	return sTypeParameterSeparatorIndicator;
    }
    
    /**
     * Return the indicator for generic type starting symbol in SPCF type 
     * portable representation format.
     * @return the indicator for generic type starting symbol in SPCF type 
     * portable representation format.
     */
    protected static String getGenericTypeStartIndicator()
    {
    	return sGenericTypeStartIndicator;
    }
    
    /**
     * Return the indicator for generic type ending symbol in SPCF type portable 
     * representation format.
     * @return the indicator for generic type ending symbol in SPCF type portable 
     * representation format.
     */
    protected static String getGenericTypeEndIndicator()
    {
    	return sGenericTypeEndIndicator;
    }
    
    /**
     * Return the indicator for generic type's type parameter starting symbol in 
     * SPCF type portable representation format.
     * @return the indicator for generic type's type parameter starting symbol 
     * in SPCF type portable representation format.
     */
    protected static String getGenericTypeParamStartIndicator ()
    {
    	return sGenericTypeParamStartIndicator;
    }
    
    /**
     * Return the indicator for generic type's type parameter ending symbol in 
     * SPCF type portable representation format.
     * @return the indicator for generic type's type parameter ending symbol in 
     * SPCF type portable representation format.
     */
    protected static String getGenericTypeParamEndIndicator ()
    {
    	return sGenericTypeParamEndIndicator;
    }
    /**
     * Return the length of indicator for generic type's dimension in 
     * SPCF type portable representation format.
     * @return the length of indicator for generic type's dimension in 
     * SPCF type portable representation format.
     */
    protected static int getGenericTypeDimIndicatorLength()
    {
    	//assume the number of generic types is represented a single digit 
    	//number, i.e., 1-9
    	return 1;
    }
    
    /**
     * Return the element type if this class is an array.
     * @return the element type of the array class. If the enclosed class is not 
     * an array, return null.
     */
    public abstract SpcfClass getElementType ();
    
    /**
     * Set the element type of an array type.
     * @param elementType the element type of the array class. 
     */
    public abstract void setElementType (SpcfClass elementType); 
    
    /**
     * Return the number of dimensions of an array class.
     * @return the number of dimensions of the encapsulated array class. 
     * @throws SpcfIllegalArgumentException if the encapsulated class is not an 
     * array type.
     */
    public abstract int getArrayRank ();
 
}
