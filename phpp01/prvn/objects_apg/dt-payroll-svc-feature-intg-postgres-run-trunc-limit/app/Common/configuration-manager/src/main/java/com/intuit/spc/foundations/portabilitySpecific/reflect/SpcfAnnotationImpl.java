package com.intuit.spc.foundations.portabilitySpecific.reflect;

import java.lang.annotation.Annotation;

import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;
import com.intuit.spc.foundations.portability.reflect.SpcfAnnotation;
/**
 * Class implementation of abstract SpcfAnnotation.
 * @author gwang
 *
 */
public class SpcfAnnotationImpl extends SpcfAnnotation 
{
	private Annotation mEncapsulatedAnnotation;
	
	/**
	 * Constructor.
	 * @param a the annotation to be encapsulated. 
	 */
	public SpcfAnnotationImpl(Annotation a)
	{
		mEncapsulatedAnnotation = a;
	}
	
	/**
	 * Return the annotation type of this annotation.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfAnnotation#annotationType()
	 */
	public SpcfClass annotationType() 
	{
		// make sure we have an encapsulated annotation
		SpcfParamValidator.checkIsNotNull(mEncapsulatedAnnotation, 
				                          "encapsulated annotation");
		return new SpcfClassImpl(mEncapsulatedAnnotation.annotationType());
	} 
	
	 /**
	  * Compare this annotation with the specified object.
	  * @see com.intuit.spc.foundations.portability.reflect.SpcfAnnotation#equals(Object)
	  */
	public boolean equals(Object obj)
	{		
		if (mEncapsulatedAnnotation == null) 
		{
			return false;
		} 
		
		if (obj == null)
		{
			return false;
		}

		if (obj instanceof SpcfAnnotationImpl)
		{
			Annotation objEncapAnn = ((SpcfAnnotationImpl)obj).getEncapsulatedAnnotation();
			if (objEncapAnn == null)
			{
				return false;
			} else 
			{
				return mEncapsulatedAnnotation.equals(objEncapAnn);
			}
		}
		else if (obj instanceof Annotation)
		{
			return mEncapsulatedAnnotation.equals((Class)obj);
		}
		return false;
	}

	/**
	 * Get the hash code for this annotation.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfAnnotation#hashCode()
	 */
 	public int hashCode()
 	{
		if(mEncapsulatedAnnotation == null) 
		{
			return 0; 
		}	
 		return mEncapsulatedAnnotation.hashCode();
 	} 

 	/**
 	 * Return string representation of this annotation.
 	 *@see com.intuit.spc.foundations.portability.reflect.SpcfAnnotation#toString()
 	 */
 	public String toString() 
 	{
 		if (mEncapsulatedAnnotation == null) 
 		{ 
			return "";
		} 
 		return mEncapsulatedAnnotation.toString();
 	} 

 	/**
 	 * Return the encapsulated annotation.
 	 * @see com.intuit.spc.foundations.portability.reflect.SpcfAnnotation#getEncapsulatedAnnotation()
 	 */
 	public Annotation getEncapsulatedAnnotation() 
 	{ 		
 		return mEncapsulatedAnnotation;
 	} 
}
