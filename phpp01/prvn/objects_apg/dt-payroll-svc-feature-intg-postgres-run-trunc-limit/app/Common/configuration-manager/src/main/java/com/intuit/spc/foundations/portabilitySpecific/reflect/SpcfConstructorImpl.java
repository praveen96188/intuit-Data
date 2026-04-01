
package com.intuit.spc.foundations.portabilitySpecific.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.reflect.SpcfConstructor;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

import com.intuit.spc.foundations.portability.reflect.SpcfAnnotation;
import com.intuit.spc.foundations.portability.reflect.SpcfIllegalAccessException;
import com.intuit.spc.foundations.portability.reflect.SpcfInvocationTargetException;
import com.intuit.spc.foundations.portability.reflect.SpcfInstantiationException;

/**
 * Class implementation of abstract SpcfConstructor.
 * @author gwang
 */
public class SpcfConstructorImpl extends SpcfConstructor {
	
	//the encapsulated constructor object
	private Constructor mEncapsulatedConstructor;

	/**
	 * Constructor
	 * @param c The constructor to be encapsulated.
	 */
	public SpcfConstructorImpl(Constructor c)
	{
		if (c != null)
		{
			c.setAccessible(true);
		}
		mEncapsulatedConstructor = c;
	}
	
	/**	 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#equals(Object)
	 */
	public boolean equals(Object obj) 
	{
        //if encapsulated constructor is null, return false	
		if (mEncapsulatedConstructor == null) 
		{
			return false;
		} 
		if (obj == null)
		{
			return false;
		}

		if (obj instanceof SpcfConstructorImpl)
		{
			Constructor objEncapConstructor = 
				((SpcfConstructorImpl)obj).toSpecific();
			if (objEncapConstructor == null)
			{
				return false;
			} else 
			{
				return mEncapsulatedConstructor.equals(objEncapConstructor);
			}
		}
		return false;
	}
	/**
	 * get a string containing the name of the constructor
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#getName()
	 */
	public String getName(){
		// make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
				                          "encapsulated constructor");
		String name = mEncapsulatedConstructor.getName();
		//System.out.println("encapConstructor name = " + name);
		if (name != null)
		{
			int index = name.lastIndexOf(".");
			if ((index != -1) && (index + 1 < name.length()))
			{
				name = name.substring(index + 1);
			}
		}
		return name;
	}
	/**
	 * Get the array of types of the parameters of this constructor
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#getParameterTypes()
	 */
	public SpcfClass[] getParameterTypes(){
		// make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
				                          "encapsulated constructor");
		Class[] paramTypes = mEncapsulatedConstructor.getParameterTypes();
		SpcfClass[] spcfParamTypes = new SpcfClassImpl[paramTypes.length];
		for (int i = 0; i < spcfParamTypes.length; i++){
			spcfParamTypes[i] = (SpcfClass)new SpcfClassImpl(paramTypes[i]);
		}
		return spcfParamTypes;
	}
	
	/**
	 * Get the type of the declaring class of this constructor
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#getDeclaringClass()
	 */
	public SpcfClass getDeclaringClass(){
		// make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
				                          "encapsulated constructor");
		Class dClass = mEncapsulatedConstructor.getDeclaringClass();
		return (SpcfClass)new SpcfClassImpl(dClass);
	}

	/**
	 * Get the hashcode of this field
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#hashCode()
	 */
	public int hashCode(){
		if (mEncapsulatedConstructor == null)
			return 0;
		return mEncapsulatedConstructor.hashCode();
	}
			
	/**
	 * Determine if this constructor has an abstract modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#isAbstract()
	 */
	public boolean isAbstract() {
		// make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
				                          "encapsulated constructor");
		int mod = mEncapsulatedConstructor.getModifiers();
		return Modifier.isAbstract(mod);
	}
	
	/**
	 * Determine if this constructor has a final modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#isFinal()
	 */
	public  boolean isFinal(){
		// make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
				                          "encapsulated constructor");
		int mod = mEncapsulatedConstructor.getModifiers();
		return Modifier.isFinal(mod);
	}
	
	/**
	 * Determine if this constructor has a private modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#isPrivate()
	 */
	public  boolean isPrivate(){
		// make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
				                          "encapsulated constructor");
		int mod = mEncapsulatedConstructor.getModifiers();
		return Modifier.isPrivate(mod);
	}
	
	/**
	 * Determine if this constructor has a public modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#isPublic()
	 */
	public   boolean isPublic(){
		// make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
				                          "encapsulated constructor");
		int mod = mEncapsulatedConstructor.getModifiers();
		return Modifier.isPublic(mod);
	}
	
	/**
	 * Determine if this constructor has a static modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#isStatic()
	 */
	public  boolean isStatic() {
		// make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
				                          "encapsulated constructor");
		int mod = mEncapsulatedConstructor.getModifiers();
		return Modifier.isStatic(mod);
	}
	
	/**
	 * Create a new instance associated with this conctructor by invoking 
	 * this constructor with specified parameters
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#invoke(Object[])
	 */
	public  Object invoke(Object[] args){
		// make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
				                          "encapsulated constructor");	
		Object obj = null;
		try {
			obj = mEncapsulatedConstructor.newInstance(args);
		} catch (IllegalAccessException e1) {
			throw new SpcfIllegalAccessException(e1);
		} catch (InvocationTargetException e2) {
			throw new SpcfInvocationTargetException(e2);
		} catch (InstantiationException e3){
			throw new SpcfInstantiationException(e3);
		} catch (IllegalArgumentException e4){
			throw new SpcfIllegalArgumentException(e4);
		}	
		return obj;
	}
	
	/**
	 * Obtain a string describing this constructor 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#toString()
	 */
	public  String toString() {
		if (mEncapsulatedConstructor == null)
			return "";	
		return mEncapsulatedConstructor.toString();
	}	
	/**
	* Returns the encapsulated third party runtime object 
	* 
	* @return The encapsulated constructor
	*/
	public Constructor toSpecific()
	{
		return mEncapsulatedConstructor;
	}
	/**
	 * Determine whether this constructor is a generic constructor.
	 *  @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#isGenericMethod()
	 */
	public boolean isGenericMethod() {
		// make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
				                          "encapsulated constructor");	
		Type[] types = mEncapsulatedConstructor.getTypeParameters();
		if (types != null && types.length > 0) 
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the list of generic parameters  for this constructor.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#getGenericParameterTypes()
	 */
    public String[] getGenericParameterTypes()
    {		
    	// make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
        "encapsulated constructor");	
        Type[] types = mEncapsulatedConstructor.getGenericParameterTypes();
   
        String[] params = new String[types.length];
        for(int i = 0; i < types.length; i++)
        {
            params[i] = types[i].toString();
            // If the type is not generic, then "class " is prepended 
            //to the class name.
            if (params[i].indexOf("class") == 0) 
            {
            	params[i] = params[i].substring(6);
            }
        }        
        return params;   
    }	
    
    /**
     * Return the type parameters associated with this constructor.
     * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#getTypeParameters()
     */
    public String[] getTypeParameters()
    {    	
    	// make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
        								  "encapsulated constructor");	
		TypeVariable[] typeArray = mEncapsulatedConstructor.getTypeParameters();        
    	String[] strArray = new String[typeArray.length];
    
        for(int i = 0; i < typeArray.length; i++)
        {
            strArray[i] = typeArray[i].toString();
        }          
        return strArray;        
    }
    
	/**
	 * Get all annotations present on this constructor.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#getAnnotations()
	 */
	public SpcfAnnotation[] getAnnotations() 
	{
		//make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
										  "encapsulated constructor");
		Annotation annos[] = mEncapsulatedConstructor.getAnnotations(); 
	    SpcfAnnotation[] sAnnos = new SpcfAnnotation[annos.length];             
        for(int i = 0; i < annos.length; i++) 
        { 	        	
        	sAnnos[i] = new SpcfAnnotationImpl(annos[i]);        	
        }	
        return sAnnos;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Get the annotation of the specified type present on this constructor.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#getAnnotation(Class)
	 */	
	public SpcfAnnotation getAnnotation(Class c) 
	{
		//make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
										  "encapsulated constructor");
		//make sure type is not null
		SpcfParamValidator.checkIsNotNull(c, "c");
		Annotation annot = mEncapsulatedConstructor.getAnnotation(c);
		if (annot == null) 
		{
			return null;
		}
	    
        return	new SpcfAnnotationImpl(annot);        	        
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Determine whether this element has the specified type annotation.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfConstructor#isAnnotationPresent(Class)
	 */	
	public boolean isAnnotationPresent(Class type) 
	{
		//make sure we have an encapsulated constructor
		SpcfParamValidator.checkIsNotNull(mEncapsulatedConstructor, 
										  "encapsulated constructor");
//		//make sure type is not null
		SpcfParamValidator.checkIsNotNull(type, "type");
		return mEncapsulatedConstructor.isAnnotationPresent(type);		
	}
}
