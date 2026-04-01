package com.intuit.spc.foundations.portabilitySpecific.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.reflect.SpcfAnnotation;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;
import com.intuit.spc.foundations.portability.reflect.SpcfClassNoSuchMethodException;
import com.intuit.spc.foundations.portability.reflect.SpcfIllegalAccessException;
import com.intuit.spc.foundations.portability.reflect.SpcfInvocationTargetException;
import com.intuit.spc.foundations.portability.reflect.SpcfMethod;
import com.intuit.spc.foundations.portability.reflect.SpcfProperty;

/**
 * Class implementation of abstract SpcfProperty.
 * @author gwang
 */
public class SpcfPropertyImpl extends SpcfProperty 
{

	/**
	 * Encapsulated get method
	 */
	protected Method mEncapsulatedGetMethod;
	
	/**
	 * Encapsulated set method
	 */
	protected Method mEncapsulatedSetMethod;
	
	/**
	 * name of the simulated property from getter/setter
	 */
	protected String mName;
	
	   
	/**
     * Indicate whether this property is an actual property, or wrapped from 
     * get and set methods in java. In java, a property is always not actual.  
     */
    protected boolean mIsActualProperty = false; 
	
	/**
	 * constructor
	 *
	 */
	public SpcfPropertyImpl()
    {
		mEncapsulatedGetMethod = null;
		mEncapsulatedSetMethod = null;
	}

	/**
	 * Constructor.
	 * @param getMethod The get method for this property to encapsulate.
	 * @param setMethod The set method for this property to encapsulate.
	 */
	public SpcfPropertyImpl(Method getMethod, Method setMethod)
    {		
		Class getRetType = null;
		Class setRetType = null;
		Class setParamType = null;
		
		//only encapsulate a get method that has a return type, no parameter
		if (getMethod != null) 
        {
			getRetType = getMethod.getReturnType();
			//System.out.println("getmethod returntype=" + getRetType);
			Class[] paramTypes = getMethod.getParameterTypes(); 
			if (getRetType != null && 
				(paramTypes == null || paramTypes.length == 0))
            {
				getMethod.setAccessible(true);
				mEncapsulatedGetMethod = getMethod;
			}
			//remove "get" from the name of the getMethod
			mName = getMethod.getName().substring(3);
		}
		
		//only encapsulate a set method that has no return type, 
		// takes a single parameter  
		if (setMethod != null) 
        {
			Class[] paramTypes = setMethod.getParameterTypes();
			setRetType = setMethod.getReturnType();
			if (paramTypes != null && paramTypes.length == 1 && 
				(setRetType == null || setRetType.getSimpleName().equals("void")))
            {
				setParamType = paramTypes[0];
				//System.out.println("set method paramType = " + setParamType.toString());
				setMethod.setAccessible(true);
				mEncapsulatedSetMethod = setMethod;
				if (mName == null) 
                {
					//remove "get" from the name of the getMethod
					mName = setMethod.getName().substring(3);
				}
			}
		}
		
		//System.out.println("get method returntype=" + getRetType);
		//System.out.println("set method paramType = " + setParamType.toString());
		//System.out.println("value = " + getRetType.toString().
		//equals(setParamType.toString()));
		
		//if both get method and set method exist, getter return type must be 
		//the same as the setter's single parameter type
		if (getRetType != null && setParamType != null && 
				!(getRetType.toString().equals(setParamType.toString())))
        {
			mEncapsulatedSetMethod = null;
			mEncapsulatedGetMethod = null;
			mName = null;
			String message = "setMethod argument does not match getMethod return type";
			throw new SpcfIllegalArgumentException(message);
		} 		
	}

	/**	 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#isActualProperty()
	 */
	public boolean isActualProperty()
	{
		return mIsActualProperty;
	}
	
	/**	 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#equals(Object)
	 */
	public boolean equals(Object obj) 
	{
		if (obj == null)
		{
			return false;
		}

		if (obj instanceof SpcfPropertyImpl)
		{
			Method objEncapGetMethod = 
				((SpcfPropertyImpl)obj).getEncapsulatedGetMethod();
			Method objEncapSetMethod = 
				((SpcfPropertyImpl)obj).getEncapsulatedSetMethod();
			if (mEncapsulatedGetMethod != null && 
					mEncapsulatedSetMethod != null)
			{
				return mEncapsulatedGetMethod.equals(objEncapGetMethod) &&
				       mEncapsulatedSetMethod.equals(objEncapSetMethod);
			} 
            else if (mEncapsulatedGetMethod != null && 
            		 mEncapsulatedSetMethod == null)
			{
				if (objEncapSetMethod != null)
				{
					return false;
				} 
                else
				{
					return mEncapsulatedGetMethod.equals(objEncapGetMethod); 
				}
			} 
            else if (mEncapsulatedGetMethod == null && 
            		 mEncapsulatedSetMethod != null)
			{
				if (objEncapGetMethod != null)
				{
					return false;
				} 
                else
				{
					return mEncapsulatedSetMethod.equals(objEncapSetMethod); 
				}					
			}
            else if (mEncapsulatedGetMethod == null && 
           		 mEncapsulatedSetMethod == null) 
            {
            	return false;
            }
		}
		return false;
	}
	
	/**
	 * Determine whether this property can be read
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#canRead()
	 */
	public boolean canRead() 
    {
        // make sure we have an encapsulated property		
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			throw new SpcfArgumentNullException("encapsulated property");
		}        

		if (mEncapsulatedGetMethod != null)
        {
			return true;
		}
		return false;
	}
	
	/**
	 * Determine whether this property can be written to
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#canWrite()
	 */
	public boolean canWrite()	
    {
	    // make sure we have an encapsulated property		
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			throw new SpcfArgumentNullException("encapsulated property");
		}    
		if (mEncapsulatedSetMethod != null)
        {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Get the class that delares this property
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#getDeclaringClass()
	 */
	public SpcfClass getDeclaringClass()
    {
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			throw new SpcfArgumentNullException("encapsulated property");
		}    
		
		Class dClass = null;
		if (mEncapsulatedGetMethod != null) 
        {
			dClass = mEncapsulatedGetMethod.getDeclaringClass();
		} 
        else if (mEncapsulatedSetMethod != null) 
        {
			dClass = mEncapsulatedSetMethod.getDeclaringClass();
		}
		if (dClass != null) 
        {
			return (SpcfClass)new SpcfClassImpl(dClass);
		}
		return null; 
	}

	/**
	 * Get the encapsulated get method 
	 * @return the encapsulated get method 
	 */
	public Method getEncapsulatedGetMethod() 
    {
		return mEncapsulatedGetMethod;
	}
	
	/**
	 * Get the encapsulated set method 
	 * @return the encapsulated set method 
	 */
	public Method getEncapsulatedSetMethod() 
    {
		return mEncapsulatedSetMethod;
	}	
	
	/**
	 * A string containing the name of this field
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#getName()
	 */
	public String getName()
    {
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			throw new SpcfArgumentNullException("encapsulated property");
		}    
		return mName;
	}
	
	/**
	 * Get the hashcode of this field
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#hashCode()
	 */
	public int hashCode()
    {
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			return 0;
		}    
		int hash = 0;		
		if (mEncapsulatedGetMethod != null) 
        {
			hash = hash + mEncapsulatedGetMethod.hashCode();		
		}		
		if (mEncapsulatedSetMethod != null) 
        {
			hash = 31 * hash + mEncapsulatedSetMethod.hashCode();	
		}
		return hash;
	}
	
	/**
	 * Get the type of this property
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#getPropertyType()
	 */
	public SpcfClass getPropertyType()
    {
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			throw new SpcfArgumentNullException("encapsulated property");
		}   
		if (mEncapsulatedGetMethod != null) 
        {
			Class pType = mEncapsulatedGetMethod.getReturnType();
			return (SpcfClass)new SpcfClassImpl(pType);
		} 
        else if (mEncapsulatedSetMethod != null) 
        {
			Class[] paramTypes = mEncapsulatedSetMethod.getParameterTypes();
			if (paramTypes != null && paramTypes.length == 1)
            {
				return (SpcfClass)new SpcfClassImpl(paramTypes[0]);
			}
		}
		return null;		
	}
	
	/**
	 * Get the value of this property
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#getValue(Object, Object[])
	 */
	public Object getValue(Object obj, Object[] index)
    {
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			throw new SpcfArgumentNullException("encapsulate property");
		}  
		if (mEncapsulatedGetMethod == null)
		{
			throw new SpcfClassNoSuchMethodException("Property Get method was not found");
		}   
	
		Object[] args = null;
		try {
			//mEncapsulatedGetMethod.setAccessible(true);
			return mEncapsulatedGetMethod.invoke(obj, args);
		} catch (IllegalArgumentException e1){
			throw new SpcfIllegalArgumentException(e1);
		} catch (InvocationTargetException e2) {
			throw new SpcfInvocationTargetException(e2);
		} catch (IllegalAccessException e3){
			throw new SpcfIllegalAccessException(e3);
		}		
	}
	
	/**
	 * Set the value of the property. 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#setValue(Object, Object)
	 */
	public void setValue(Object obj, Object newValue)
    {
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			throw new SpcfArgumentNullException("encapsulate property");
		}  
		if (mEncapsulatedSetMethod == null)
		{
			throw new SpcfClassNoSuchMethodException("Property set method not found");
		}   		
		Object[] args = new Object[1];
		args[0] = newValue;
		try 
		{
			//mEncapsulatedSetMethod.setAccessible(true);
			mEncapsulatedSetMethod.invoke(obj, args);
		}
		catch (NullPointerException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
		catch (IllegalArgumentException e1){
			throw new SpcfIllegalArgumentException(e1);
		} catch (InvocationTargetException e2) {
			throw new SpcfInvocationTargetException(e2);
		} catch (IllegalAccessException e3){
			throw new SpcfIllegalAccessException(e3);
		}
	}
	
	/**
	 * Obtain a string describing this property 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#toString()
	 */
	public String toString()
    {
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			return "";
		}   
	    String str = null;
	    SpcfClass pType = getPropertyType();  
		if (pType != null && mName != null)
        {
			str = pType.getFullName() + " " + mName;
		}
		return str;
	}

    /**
     * Returns the return value (including generic values) for the
     * current SpcfMethod. 
     * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#getGenericType()
     */
    public String getGenericType()
    {
        if (mEncapsulatedGetMethod != null)
        {
            return mEncapsulatedGetMethod.getGenericReturnType().toString();
        }
        else if (mEncapsulatedSetMethod != null)
        {
            Type[] types = mEncapsulatedSetMethod.getGenericParameterTypes();
            return types[0].toString();
        }
        if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
        {        
        	throw new SpcfArgumentNullException("encapulated property");        	
        }
    	return null;
    }
    
    /** Get the get accessor of this property
     * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#getGetMethod(boolean)
	 */
	public SpcfMethod getGetMethod(boolean nonPublic)
	{
	    // make sure we have an encapsulated property		
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			throw new SpcfArgumentNullException("No encapsulated property");
		}    
		if (mEncapsulatedGetMethod != null)
		{
			//mEncapsulatedGetMethod.setAccessible(true);
			SpcfMethod spcfM = 
				(SpcfMethod)new SpcfMethodImpl(mEncapsulatedGetMethod);
			if (spcfM.isPublic() || nonPublic)
			{
				return spcfM;
			} 
		}
		return null;
	}
	
    /**
     * Get the set accessor of this property
     * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#getSetMethod(boolean)
	 */
	public SpcfMethod getSetMethod(boolean nonPublic)
	{
	    // make sure we have an encapsulated property		
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			throw new SpcfArgumentNullException("No encapsulated property");
		}    
		if (mEncapsulatedSetMethod != null)
		{
			//mEncapsulatedSetMethod.setAccessible(true);
			SpcfMethod spcfM = 
				(SpcfMethod)new SpcfMethodImpl(mEncapsulatedSetMethod);
			if (spcfM.isPublic() || nonPublic)
			{
				return spcfM;
			} 
		}
		return null;
	}		
	
	/**
	 * Determine whether this property is public or not.
	 * 
	 * Trying to be as consistent with C# spec and the current J2C translation 
     * rules as possible, currently we determine a wrapped property's access 
     * modifier in java as the same as that of the property's most accessible 
     * accessor. This may beed to be changed in the future once J2C supports 
     * different visibilities for get and set accesors of the same property.
     *   
	 * @return true if this property has a public accessor, otherwise false.
	 */
	protected boolean isPublic() 
	{
		//return true if a public get method exists
		if (getGetMethod(false) != null) 
		{
			return true;
		} else if (getSetMethod(false) != null) 
		{
			return true;
		} else 
		{
		return false;
		}
	}
	
	/**
	 * Get all annotations present on this property.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#getAnnotations()
	 */
	public SpcfAnnotation[] getAnnotations() 
	{
	    // make sure we have an encapsulated property		
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			throw new SpcfArgumentNullException("No encapsulated property");
		}    
			
		HashSet<Annotation> annoSet = new HashSet<Annotation>();
		if (mEncapsulatedGetMethod != null)
		{
			Annotation annosGet[] = mEncapsulatedGetMethod.getAnnotations();
			for (Annotation a : annosGet) 
			{
				annoSet.add(a);
			}
			//SpcfAnnotation[] sAnnosGet = new SpcfAnnotation[annosGet.length];             
	        /*for(int i = 0; i < annos.length; i++) 
	        { 	        	
	        	sAnnos[i] = new SpcfAnnotationImpl(annos[i]);        	
	        }*/	
	        //return sAnnos;
		}
		
		if (mEncapsulatedSetMethod != null)
		{
			Annotation annosSet[] = mEncapsulatedSetMethod.getAnnotations();
			for (Annotation a : annosSet) 
			{
				annoSet.add(a);
			}
			//SpcfAnnotation[] sAnnosSet = new SpcfAnnotation[annos.length];
			/*for(int i = 0; i < annosSet.length; i++) 
	        { 	        	
	        	sAnnos[i] = new SpcfAnnotationImpl(annos[i]);        	
	        }*/
			
	        //return sAnnos;
		}
		
		if (!annoSet.isEmpty()) 
		{
			SpcfAnnotation[] sAnnos = new SpcfAnnotation[annoSet.size()];
			int iCnt = 0;
			for(Annotation a : annoSet) 
	        { 	        	
	        	sAnnos[iCnt++] = new SpcfAnnotationImpl(a);        	
	        }
			return sAnnos;
		} else 
		{
			return new SpcfAnnotation[0];
		}	    
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Get the annotation of the specified type present on this property.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#getAnnotation(Class)
	 */
	public SpcfAnnotation getAnnotation(Class c) 
	{
		// make sure we have an encapsulated property		
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			throw new SpcfArgumentNullException("No encapsulated property");
		}
		//make sure type is not null
		SpcfParamValidator.checkIsNotNull(c, "c");
		
		Annotation annot = null;	
		if (mEncapsulatedGetMethod != null)
		{
			annot = mEncapsulatedGetMethod.getAnnotation(c);
		} else if (mEncapsulatedSetMethod != null)
		{
			annot = mEncapsulatedSetMethod.getAnnotation(c);
		}
		if (annot == null) 
		{
			return null;
		}	    
        return	new SpcfAnnotationImpl(annot);        	        
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#getGetterAnnotation(Class)
	 */
	public SpcfAnnotation getGetterAnnotation(Class c) 
	{
		// make sure we have an encapsulated property		
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			throw new SpcfArgumentNullException("No encapsulated property");
		}		
		//make sure type is not null
		SpcfParamValidator.checkIsNotNull(c, "c");
		
		if (mEncapsulatedGetMethod == null) 
		{
			return null;
		} else {
			Annotation annot = mEncapsulatedGetMethod.getAnnotation(c);
			if (annot == null) 
			{
				return null;
			} else 
			{	    
				return	new SpcfAnnotationImpl(annot);
			}  
		}      	        
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#getSetterAnnotation(Class)
	 */
	public SpcfAnnotation getSetterAnnotation(Class c) 
	{
		// make sure we have an encapsulated property		
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)
		{
			throw new SpcfArgumentNullException("No encapsulated property");
		}		
		//make sure type is not null
		SpcfParamValidator.checkIsNotNull(c, "c");
		
		if (mEncapsulatedSetMethod == null) 
		{
			return null;
		} else {
			Annotation annot = mEncapsulatedSetMethod.getAnnotation(c);
			if (annot == null) 
			{
				return null;
			} else 
			{	    
				return	new SpcfAnnotationImpl(annot);
			}  
		}      	        
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Determine whether this element has the specified type annotation.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#isAnnotationPresent(Class)
	 */
	public boolean isAnnotationPresent(Class type) 
	{
		//make sure type is not null
		SpcfParamValidator.checkIsNotNull(type, "type");
		
		// make sure we have an encapsulated property		
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)						
		{
			throw new SpcfArgumentNullException("No encapsulated property");
		}    
		if (getAnnotation(type) != null) 
		{
			return true;
		} else 
		{
			return false;
		}		
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#isGetterAnnotationPresent(Class)
	 */
	public boolean isGetterAnnotationPresent(Class type) 
	{
		//make sure type is not null
		SpcfParamValidator.checkIsNotNull(type, "type");
		
		// make sure we have an encapsulated property		
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)						
		{
			throw new SpcfArgumentNullException("No encapsulated property");
		}    
		if (getGetterAnnotation(type) != null) 
		{
			return true;
		} else 
		{
			return false;
		}		
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfProperty#isSetterAnnotationPresent(Class)
	 */
	public boolean isSetterAnnotationPresent(Class type) 
	{
		//make sure type is not null
		SpcfParamValidator.checkIsNotNull(type, "type");
		
		// make sure we have an encapsulated property		
		if (mEncapsulatedGetMethod == null && mEncapsulatedSetMethod == null)						
		{
			throw new SpcfArgumentNullException("No encapsulated property");
		}    
		if (getSetterAnnotation(type) != null) 
		{
			return true;
		} else 
		{
			return false;
		}		
	}
}
