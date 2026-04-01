package com.intuit.spc.foundations.portability.reflect;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;

/**
 * This class provides wrapped methods for the Java Constructor object and 
 * .Net ConstructorInfo object.
 */
public abstract class SpcfConstructor {
	
    /**
	 * Determines whether the encapsulated constructor is equal to the 
	 * specified object. 
	 *
	 * @param obj The object to compare.
	 * 
	 * @return true if the encapsulated constructor of this class is the same 
	 * as the encapsulated constructor of the specified object obj, false if 
	 * they are not the same or obj is null or obj is not 
	 * an instance of  SpcfConstructorImpl or the encapsulated constructor is 
	 * null.
	 */
	public abstract boolean equals(Object obj);	

	/**
	 * Get the declaring class of this constructor.
	 * @return the declaring class of this constructor.
	 * @throws SpcfArgumentNullException if the encapsulated constructor is null.
	 */
	public abstract SpcfClass getDeclaringClass();

	/**
	 * Get the hashcode of this field.
	 * @return an integer as the hashcode. Return 0 if the encapsulated 
	 * constructor is null.
	 * 
	 */
	public abstract int hashCode();
		
	/**
	 * Get the name of the constructor.
	 * @return the name of the constructor. Note: for the same constructor,
	 * java and C# return the constructor name in different forms. 
	 * @throws SpcfArgumentNullException if the encapsulated constructor is null.
	 */
	public abstract String getName();

	/**
	 * Get the array of types of the parameters of this constructor.
	 * @return the array of types of the parameters of this constructor. 
	 * Returns an array of length 0 if the underlying constructor takes no parameters. 
	 * @throws SpcfArgumentNullException if the encapsulated constructor is null.
	 */
	public abstract SpcfClass[] getParameterTypes();
	
	/**
	 * Determine if this constructor has an abstract modifier
	 * 
	 * @return true if this constructor has an abstract modifier, false otherwise
	 */
	public abstract boolean isAbstract();
	
	/**
	 * Determine if this constructor has a final modifier
	 * @return true if this constructor has a final modifier, false otherwise
	 */
	public abstract boolean isFinal();
	
	/**
	 * Determine if this constructor has a private modifier
	 * @return true if this constructor has a private modifier, false otherwise
	 */
	public abstract boolean isPrivate();
	
	/**
	 * Determine if this constructor has a public modifier.
	 * @return true if this constructor has a public modifier, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated constructor is null.
	 */
	public abstract  boolean isPublic();
	
	/**
	 * Determine if this constructor has a static modifier.
	 * @return true if this constructor has a static modifier, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated constructor is null.
	 */
	public abstract boolean isStatic();
	
	/**
	 * Create a new instance associated with this constructor by invoking this 
	 * constructor with specified parameters.
	 * 
	 * @param args An array of objects to be passed as arguments to the 
	 * constructor call.
	 * @return An instance of the class associated with this constructor
	 * @throws SpcfIllegalAccessException if the matching Constructor enforces 
	 * Java language access control and the underlying constructor is inaccessible.
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
	 * @throws SpcfArgumentNullException if the encapsulated constructor is null.
	 */
	public abstract Object invoke(Object[] args);
	
	/**
	 * Obtain a string describing this constructor. 
	 * @return a string describing this constructor. Return empty string if 
	 * the encapsulated constructor is null. Note: for the same constructor,
	 * java and C# return in different forms. 
	 */
	public abstract String toString();
	
	/**
	 * Determine whether this constructor is a generic method.
	 * @return Returns true if this constructor contains type parameters,
	 * otherwise false.
	 * @throws SpcfArgumentNullException if the encapsulated constructor is null.
	 */
	public abstract boolean isGenericMethod();
	
	/**
     * Returns the list of generic parameters  for this constructor.
     * @return An array of strings for the full class names for each of 
     * the constructor's generic parameters. Returns an array of length 0 if the 
     * underlying constructor takes no generic parameters. 
     * @throws SpcfArgumentNullException if the encapsulated constructor is null.
     */
    public abstract String[] getGenericParameterTypes();
    
    /**
     * Return the type parameters associated with this constructor.
     * <p>
     * C# .Net 2.0 does not support generic constructors. Type parameters 
     * correspond to generic arguments in C#, calling ConstructorInfo.
     * getGenericArguments() throws NotSupportedException, since this method 
     * from MethodBase is not overridden in ConstructorInfo. So, this method 
     * is currently implemented to always return an empty array in C#.
     * </p>  
     * @return An array of type parameters in string format for this 
     * constructor. Returns an array of length 0 if the underlying constructor 
     * has no type parameters. 
     * @throws SpcfArgumentNullException if the encapsulated constructor is null.
     */
    public abstract String[] getTypeParameters();

	/**
	 * Get all annotations present on this constructor.
	 * @return array of SpcfAnnotations representing all annotations on this 
	 * constructor. Return an empty array if this constructor has no annotations.
	 * @throws SpcfArgumentNullException if encapsulated constructor is null.
	 */
	public abstract SpcfAnnotation[] getAnnotations();
	
	/**
	 * Get the annotation of the specified type present on this constructor.
	 * @return Returns this element's annotation of the specified type if 
	 * such an annotation is present, else null.
	 * @throws SpcfArgumentNullException if encapsulated constructor or type is 
	 * null.
	 */
	public abstract SpcfAnnotation getAnnotation(Class c); 
	
	/**
	 * Determine whether this element has the specified type annotation.
	 * @param type the annotation type to search for.
	 * @return ture if this element has the specified type annotation, 
	 * else false.
	 * @throws SpcfArgumentNullException if encapsulated constructor or type is 
	 * null.
	 */
	public abstract boolean isAnnotationPresent(Class type);
}
