package com.intuit.spc.foundations.portability.reflect;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;

/**
 * This class provides wrapped methods for the Java Method object 
 * and .Net MethodInfo object.
 */
public abstract class SpcfMethod {
	  
	
    /**
	 * Determines whether the encapsulated method is equal to the 
	 * specified object. 
	 *
	 * @param obj The object to compare.
	 * 
	 * @return true if the encapsulated method of this class is the same 
	 * as the encapsulated method of the specified object obj, false if 
	 * they are not the same or obj is null or obj is not 
	 * an instance of SpcfMethodImpl or the encapsulated methos is null.
	 */
	public abstract boolean equals(Object obj);	

	/**
	 * Get the class that declares this method.
	 * 
	 * @return the class that declares this method.
	 * @throws SpcfArgumentNullException if the encapsulated method is null.
	 */
	public abstract SpcfClass getDeclaringClass();

	/**
	 * Get the hashcode of this method.
	 * @return an integer as the hashcode of this method. Return 0 if the 
	 * encapsulated method is null.
	 */
	public abstract int hashCode();
	
	/**
	 * Get the name of the Method.
	 * @return the name of the Method. Note: for the same method,
	 * java and C# may return the method name in different forms. 
	 * @throws SpcfArgumentNullException if the encapsulated method is null.
	 */
	public abstract String getName();

	/**
	 * Get the array of types of the parameters of this method.
	 * @return the array of types of the parameters of this method.
	 * @throws SpcfArgumentNullException if the encapsulated method is null.
	 */
	public abstract SpcfClass[] getParameterTypes();
	
	/**
	 * Get the type of the return of this method.
	 * @return the type of the return of this method.
	 * @throws SpcfArgumentNullException if the encapsulated method is null.
	 */
	public abstract SpcfClass getReturnType();

	/**
	 * Determine if this method has an abstract modifier.
	 * @return true if this method has an abstract modifier, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated method is null.
	 */
	public abstract boolean isAbstract();
	
	/**
	 * Determine if this method has a final modifier.
	 * @return true if this method has a final modifier, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated method is null.
	 */
	public abstract boolean isFinal();
	
	/**
	 * Determine if this method has a private modifier.
	 * @return true if this method has a private modifier, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated method is null.
	 */
	public abstract boolean isPrivate();
	
	/**
	 * Determine if this method has a public modifier.
	 * @return true if this method has a public modifier, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated method is null.
	 */
	public abstract  boolean isPublic();
	
	/**
	 * Determine if this method has a static modifier.
	 * @return true if this method has a static modifier, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated method is null.
	 */
	public abstract boolean isStatic();
	
	/**
	 * Invoke this method with specified parameters on the specified onject.
	 * @param obj The object on which this method is to be invoked. 
	 * If the underlying method is static, then the specified obj argument 
	 * is ignored. It may be null if the method is static.
	 * @param args An array of objects to be passed as arguments to the method 
	 * call.
	 * @return  the result of dispatching this method on obj with parameters args
	 * @throws SpcfIllegalAccessException if this Method object enforces 
	 * access control and the underlying method is inaccessible.
	 * @throws SpcfInvocationTargetException if the underlying method 
	 * throws an exception. 
	 * @throws SpcfIllegalArgumentException if the method is an instance 
	 * method and the specified object argument is not an instance of the 
	 * class or interface declaring the underlying method (or of a subclass 
	 * or implementor thereof); if the specified object is null but the 
	 * method is not static; if the number of actual and formal parameters 
	 * differ; if an unwrapping conversion for primitive arguments fails; 
	 * or if, after possible unwrapping, a parameter value cannot be converted 
	 * to the corresponding formal parameter type by a method invocation 
	 * conversion. 
	 * @throws SpcfArgumentNullException if the encapsulated method is null. 
	 */
	public abstract Object invoke(Object obj, Object[] args);
	
	/**
	 * Obtain a string describing this method. 
	 * @return A string describing this method. Return empty string  if 
	 * the encapsulated method is null. Note: for the same method,
	 * java and C# return in different forms. 
	 */
	public abstract String toString();
    
    /**
     * Returns the return value (including generic values) for the
     * current SpcfMethod. 
     * @return The full classname for the method's return value.
     * @throws SpcfArgumentNullException if the encapsulated method is null.
     */
    public abstract String getGenericReturnType();
    
    /**
     * Returns the list of generic parameters  for this method.
     * @return An array of strings for the full class names for each of 
     * the method's generic parameters.
     * @throws SpcfArgumentNullException if the encapsulated method is null.
     */
    public abstract String[] getGenericParameterTypes();
    
	/**
	 * Determine whether this method is a generic method.
	 * @return Returns true if this method contains type parameters,
	 * otherwise false.
	 * @throws SpcfArgumentNullException if the encapsulated method is null.
	 */
	public abstract boolean isGenericMethod();
	
    /**
     * Return the type parameters associated with this method.
     * @return An array of type parameters in string format for this method.
     * @throws SpcfArgumentNullException if the encapsulated method is null.
     */
    public abstract String[] getTypeParameters();
    
    /**
     * Determine whether this method is a property accessor.
     * @return In C# .Net, if the encapsulated method is a property accessor, returns true, 
     * otherwise returns false; in java, always return false.
     * @throws SpcfArgumentNullException if the encapsulated method is null.
     */
    public abstract boolean isPropertyAccessor();  
     
	/**
	 * Get all annotations present on this method.
	 * @return array of SpcfAnnotations representing all annotations on this 
	 * method. Return an empty array if this method has no annotations.
	 * @throws SpcfArgumentNullException if encapsulated method is null.
	 */
	public abstract SpcfAnnotation[] getAnnotations(); 
	
	/**
	 * Get the annotation of the specified type present on this method.
	 * @return Returns this element's annotation of the specified type if 
	 * such an annotation is present.
	 * @throws SpcfArgumentNullException if encapsulated method or type is 
	 * null.
	 */
	public abstract SpcfAnnotation getAnnotation(Class c);
	
	/**
	 * Determine whether this element has the specified type annotation.
	 * @param type the annotation type to search for.
	 * @return ture if this element has the specified type annotation, 
	 * else false.
	 * @throws SpcfArgumentNullException if encapsulated method or type is 
	 * null.
	 */
	public abstract boolean isAnnotationPresent(Class type);
}
