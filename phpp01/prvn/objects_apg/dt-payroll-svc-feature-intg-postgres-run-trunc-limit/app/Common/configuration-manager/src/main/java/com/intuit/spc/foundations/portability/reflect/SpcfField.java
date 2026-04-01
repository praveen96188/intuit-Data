package com.intuit.spc.foundations.portability.reflect;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException; 

/**
 * This class provides wrapped methods for the Java Field object and 
 * .Net FieldInfo object.
 */
public abstract class SpcfField {
	  
    /**
	 * Determines whether the encapsulated field is equal to the 
	 * specified object. 
	 *
	 * @param obj The object to compare.
	 * 
	 * @return true if the encapsulated field of this class is the same 
	 * as the encapsulated field of the specified object obj; return false if 
	 * they are not the same, or obj is null, or obj is not an instance of 
	 * SpcfFieldImpl, or the encapsulated field is null.
	 */
	public abstract boolean equals(Object obj);	

	/**
	 * Get the class that declares this field.
	 * @return the class that declares this field.
	 * @throws SpcfArgumentNullException if the encapsulated field is null.
	 */
	public abstract SpcfClass getDeclaringClass();
	
	/**
	 * Get the type of this field.
	 * @return the type of this field.
	 * @throws SpcfArgumentNullException if the encapsulated field is null.
	 */
	public abstract  SpcfClass getFieldType();
	
	/**
	 * Get the name of this field.
	 * @return the name of this field.
	 * @throws SpcfArgumentNullException if the encapsulated field is null.
	 */
	public abstract String getName();
	
	/**
	 * Get the hashcode of this field.
	 * @return an interger as the hashcode. Return 0 if the encapsulated 
	 * field is null.
	 */
	public abstract int hashCode();
	
	/**
	 * Get the value of this field.
	 * @param obj The object to get the value of this field from.
	 * @return the value of this field.
	 * @throws SpcfIllegalAccessException if the underlying field is 
	 * inaccessible. 
	 * @throws SpcfIllegalArgumentException  if the specified object is not 
	 * an instance of the class or interface declaring the underlying field 
	 * (or a subclass or implementor thereof), or if an unwrapping conversion 
	 * fails.
	 * @throws SpcfArgumentNullException if the encapsulated field is null.
	 */
	public abstract Object getValue(Object obj);
	
	/**
	 * Determine if this field has a final modifier.
	 * @return true if this field has a final modifier in java or is a const in 
	 * C#, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated field is null.
	 */
	public abstract boolean isFinal();
	
	/**
	 * Determine if this field has a private modifier.
	 * @return true if this field has a final modifier, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated field is null.
	 */
	public abstract boolean isPrivate();
	
	/**
	 * Determine if this field has a public modifier.
	 * @return true if this field has a final modifier, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated field is null.
	 */
	public abstract  boolean isPublic();
	
	/**
	 * Determine if this field has a static modifier.
	 * @return true if this field has a final modifier, false otherwise.
	 * @throws SpcfArgumentNullException if the encapsulated field is null.
	 */
	public abstract  boolean isStatic();
	
	/**
	 * Set this field on the specified object argument to the specified new 
	 * value. The new value is automatically unwrapped if the underlying 
	 * field has a primitive type.
	 * 
	 * @param obj An object that has this field.
	 * @param newValue The new value to be set for this field.
	 * 
	 * @throws SpcfIllegalAccessException if the underlying field is 
	 * inaccessible.
	 * @throws SpcfIllegalArgumentException  if the specified object is not 
	 * an instance of the class or interface declaring the underlying field 
	 * (or a subclass or implementor thereof), or if an unwrapping conversion 
	 * fails. 
	 * @throws SpcfArgumentNullException if the encapsulated field is null.
	 */
	public abstract void setValue(Object obj, Object newValue);
	
	/**
	 * Obtain a string describing this field.
	 * @return  a string describing this field. Note: for the same field,
	 * java and C# return in different forms, due to the different type 
	 * names of the field in java and C#.  
	 * @throws SpcfArgumentNullException if the encapsulated field is null.
	 */
	public abstract String toString();

    /**
     * Returns a string that represents the declared type for this field. 
     *  
     * @return The full classname of the declared type for this field. 
     * Return empty string if the encapsulated field is null.
     */
    public abstract String getGenericType();
    
	/**
	 * Get all annotations present on this field.
	 * @return array of SpcfAnnotations representing all annotations on this 
	 * field. Return an empty array if this field has no annotations.
	 * @throws SpcfArgumentNullException if encapsulated field is null.
	 */
	public abstract SpcfAnnotation[] getAnnotations(); 
	
	/**
	 * Get the annotation of the specified type present on this field.
	 * @return Returns this element's annotation of the specified type if 
	 * such an annotation is present, else null.
	 * @throws SpcfArgumentNullException if encapsulated field or type is 
	 * null.
	 */
	public abstract SpcfAnnotation getAnnotation(Class c); 
	
	/**
	 * Determine whether this element has the specified type annotation.
	 * @param type the annotation type to search for.
	 * @return ture if this element has the specified type annotation, 
	 * else false.
	 * @throws SpcfArgumentNullException if encapsulated field or type is 
	 * null.
	 */
	public abstract boolean isAnnotationPresent(Class type);
}
