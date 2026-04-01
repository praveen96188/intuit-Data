package com.intuit.spc.foundations.portability.reflect;

import java.lang.annotation.Annotation;

/**
 * This class wraps java.lang.Annotation and C# System.Attribute.
 *
 */
public abstract class SpcfAnnotation 
{
	/**
	 * Return the annotation type of this annotation.
	 * @return the annotation type of this annotation.
	 * @throws SpcfArgumentNullException if the encapsulated annotation is null.  
	 */
	public abstract SpcfClass annotationType(); 
	
	/**
	 * Compare this annotation with the specified object.
	 * @return true if the specified object represents an annotation that is 
	 * logically equivalent to this one; return false if the
 	 * encapsulated annotation or obj is null, or the specified object does 
 	 * not represents an annotation that is logically equivalent to the 
 	 * encapsulated annotation. 
	 */
	public abstract boolean equals(Object obj); 

	/**
	 * Get the hash code for this annotation.
	 * @return the hash code for this annotation. Return 0 if the
 	 * encapsulated annotation is null.  
	 */
 	public abstract int hashCode(); 

 	/**
 	 * Return string representation of this annotation.
 	 * @return string representation of this annotation. Return "" if the
 	 * encapsulated annotation is null.  
 	 */
 	public abstract String toString(); 
	
 	/**
 	 * Return the encapsulated annotation.
 	 * @return the encapsulated annotation object.
 	 */
 	public abstract Annotation getEncapsulatedAnnotation(); 
}
