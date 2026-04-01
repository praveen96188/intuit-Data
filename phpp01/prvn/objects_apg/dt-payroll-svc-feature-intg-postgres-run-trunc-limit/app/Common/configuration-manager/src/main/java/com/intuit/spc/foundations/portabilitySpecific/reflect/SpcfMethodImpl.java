
package com.intuit.spc.foundations.portabilitySpecific.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.TypeVariable;
import java.util.List;

import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.reflect.SpcfAnnotation;
import com.intuit.spc.foundations.portability.reflect.SpcfIllegalAccessException;
import com.intuit.spc.foundations.portability.reflect.SpcfInvocationTargetException;
import com.intuit.spc.foundations.portability.reflect.SpcfMethod;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * Class implementation of abstract SpcfMethod.
 * @author gwang
 */
public class SpcfMethodImpl extends SpcfMethod 
{
	/**
	 * the encapsulated method object
	 */
	private Method mEncapsulatedMethod;

	/**
	 * Constructor
	 * @param m The method to be encapsulated.
	 */
	public SpcfMethodImpl(Method m)
	{
		if (m != null) {
			m.setAccessible(true);
		}
		mEncapsulatedMethod = m;
	}
	
	/**	 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#equals(Object)
	 */
	public boolean equals(Object obj) 
	{
        //if encapsulated method is null, return false	
		if (mEncapsulatedMethod == null) 
		{
			return false;
		} 
		if (obj == null)
		{
			return false;
		}

		if (obj instanceof SpcfMethodImpl)
		{
			Method objEncapMethod = 
				((SpcfMethodImpl)obj).toSpecific();
			if (objEncapMethod == null)
			{
				return false;
			} else 
			{
				return mEncapsulatedMethod.equals(objEncapMethod);
			}
		}
		return false;
	}
	
	/**
	 * Get the name of the Method
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#getName()
	 */
	public String getName()
    {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");
		return mEncapsulatedMethod.getName();
	}

	/**
	 * Get the array of types of the parameters of this method
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#getParameterTypes()
	 */
	public SpcfClass[] getParameterTypes()
    {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");		
		Class[] paramTypes = mEncapsulatedMethod.getParameterTypes();
		SpcfClass[] spcfParamTypes = new SpcfClassImpl[paramTypes.length];
		for (int i = 0; i < spcfParamTypes.length; i++){
			spcfParamTypes[i] = (SpcfClass)new SpcfClassImpl(paramTypes[i]);
		}
		return spcfParamTypes;
	}
	
	/**
	 * Get the type of the return of this method
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#getReturnType()
	 */
	public SpcfClass getReturnType()
    {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");	
		Class returnType = mEncapsulatedMethod.getReturnType();
		SpcfClass spcfReturnType = new SpcfClassImpl(returnType);
		return spcfReturnType;
	}
  
	/**
	 * Get the type of the declaring class of this method
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#getDeclaringClass()
	 */
	public SpcfClass getDeclaringClass() 
    {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");
		Class declaringClass = mEncapsulatedMethod.getDeclaringClass();
		SpcfClass spcfDeclaringClass = new SpcfClassImpl(declaringClass);
		return spcfDeclaringClass;
	}
 
	/**
	 * Get the hashcode of this method
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#hashCode()
	 */
	public int hashCode()
    {
		if (mEncapsulatedMethod == null) 
			return 0;
		return mEncapsulatedMethod.hashCode();
	}
			
	/**
	 * Determine if this method has an abstract modifier
 	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#isAbstract()
 	 */
	public boolean isAbstract() 
    {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");
		int mod = mEncapsulatedMethod.getModifiers();
		return Modifier.isAbstract(mod);
	}
	
	/**
	 * Determine if this method has a final modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#isFinal()
	 */
	public boolean isFinal()
    {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");
		int mod = mEncapsulatedMethod.getModifiers();
		return Modifier.isFinal(mod);
	}
	
	/**
	 * Determine if this method has a private modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#isPrivate()
	 */
	public boolean isPrivate()
    {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");
		int mod = mEncapsulatedMethod.getModifiers();
		return Modifier.isPrivate(mod);
	}
	
	/**
	 * Determine if this method has a public modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#isPublic()
	 */
	public boolean isPublic()
    {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");
		int mod = mEncapsulatedMethod.getModifiers();
		return Modifier.isPublic(mod);
	}
	
	/**
	 * Determine if this method has a static modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#isStatic()
	 */
	public boolean isStatic() 
    {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");
		int retModifier = mEncapsulatedMethod.getModifiers();
		return Modifier.isStatic(retModifier);
	}
	
	/**
	 * Invoke this method on the specified object with specified parameters
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#invoke(Object, Object[])
	 */
	public  Object invoke(Object obj, Object[] args)
    {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");
		try {
			//mEncapsulatedMethod.setAccessible(true);
			return mEncapsulatedMethod.invoke(obj, args);
		} catch (IllegalAccessException e1) {
			throw new SpcfIllegalAccessException(e1);
		} catch (InvocationTargetException e2) {
			throw new SpcfInvocationTargetException(e2);
		} catch (NullPointerException e3) {
			throw new SpcfIllegalArgumentException(e3);
		} catch (IllegalArgumentException e4) {
			throw new SpcfIllegalArgumentException(e4);
		}
	}
	
    /**
     * Returns the return value (including generic values) for the
     * current SpcfMethod. 
     * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#getGenericReturnType()
     */
    public String getGenericReturnType()
    {
        //make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");
   	
        String returnType = mEncapsulatedMethod.
        	getGenericReturnType().toString();
        
        // If the type is not generic, then "class " is prepended 
        //to the class name.
        if (returnType.indexOf("class") == 0) 
        {
        	returnType = returnType.substring(6);
        }
        
        return returnType;
    }
    
    /**
     * Returns the list of parameters (including generic values) for the
     * current SpcfMethod.
     * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#getGenericParameterTypes()
     */
    public String[] getGenericParameterTypes()
    {
        //make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");

    	Type[] types = mEncapsulatedMethod.getGenericParameterTypes();
        
        String[] params = new String[types.length];
        for(int i = 0; i < types.length; i++)
        {
            params[i] = types[i].toString();
            // If the type is not generic, then "class " is prepended to the class name.
            if (params[i].indexOf("class") == 0) params[i] = params[i].substring(6);
        }
        
        return params;
    }
    
	/**
     * Return the type parameters associated with this method.
     * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#getTypeParameters()
     */
    public String[] getTypeParameters()
    {    	
    	// make sure we have an encapsulated method
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
                                          "encapsulated method");	
        TypeVariable[] typeArray = mEncapsulatedMethod.getTypeParameters();
        
        String[] strArray = new String[typeArray.length];
        
        for(int i = 0; i < typeArray.length; i++)
        {
            strArray[i] = typeArray[i].toString();
        }          
        return strArray;
    }
    
	/**
	 * Determine whether this method is a generic method.
	 *  @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#isGenericMethod()
	 */
	public boolean isGenericMethod() {
		// make sure we have an encapsulated method
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");	
		Type[] types = mEncapsulatedMethod.getTypeParameters();
		if (types != null && types.length > 0) 
		{
			return true;
		}
		return false;
	}
    
	/**
	 * Obtain a string describing this method
     * @return The String representation
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#toString() 
	 */
	public String toString() 
    {
		if (mEncapsulatedMethod == null) 
			return "";
		return mEncapsulatedMethod.toString();
	}
    
	/**
	* Returns the encapsulated third party runtime object 
	* @return The encapsulated method
	*/
	public Method toSpecific()
	{
		return mEncapsulatedMethod;
	}	
    /**
     * Determine whether this method is a property accessor.
     * @return If this method is a property accessor, returns true, 
     * otherwise returns false. 
     */
    public boolean isPropertyAccessor() {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");
    	return false;
    }  
    
    /**
     * Determine whether the specified list has a method that shares the name 
     * and parameter types with this method.
     * @return  Returns true if there is a matching method, otherwise returns 
     * false. 
     */
    protected boolean matchNameAndTypes(List<SpcfMethod> methods) {
		// make sure we have an encapsulated method
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");
		if (methods == null || methods.size() == 0) 
		{
			return false;
		}
		String name = getName();
		SpcfClass[] types = getParameterTypes();
		for (int i = 0; i < methods.size(); i++) 
		{
			SpcfMethod method = (SpcfMethod)methods.get(i);
			if (method.getName().equals(name)) 
			{
				boolean typesMatched = true;
				SpcfClass[] pTypes = method.getParameterTypes();
				if (types == null || types.length == 0) {
					if (pTypes == null || pTypes.length == 0) 
					{
						return true;
					} 
				} else if (types != null && types.length > 0)
				{
					if (pTypes != null && pTypes.length == types.length) 
					{
					
						for (int j = 0; j < types.length; j++) 
						{
							if (!types[j].equals(pTypes[j])) 
							{
								typesMatched = false;
							}
						}
						if (typesMatched == true) return true;
					}
				}
			}
		} 
		return false;
    } 
    
    /**
     * Determine whether this method is inherited by the specified class or 
     * interface.
     * @return true if this method is inherited, false otherwise. 
     */
    protected boolean inheritedBy(SpcfClass c) {
		// make sure we have an encapsulated method
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
				                          "encapsulated method");
		SpcfClass decClass = getDeclaringClass();
		//if this method is not from a superclass or interface of c
		if (!c.isSubclassOf(decClass) || 
				(decClass.isInterface() && 
						c.getInterface(decClass.getFullName()) == null)) 
		{
			return false;
		} else 
		{
			//private methods are not inherited by subclass
			if (isPrivate())  
			{
				return false;
			} else if (isPublic()) // public methods are inherited by subclass 
			{
				return true;
			} else if (!isPublic() && !isPrivate())
			{ 
				if (Modifier.isProtected(mEncapsulatedMethod.getModifiers())) 
				{
					return true; // protected methods are inherited by subclass
				} else 
				{
					//default access (package-private) methods are inherited 
					//by only subclasses within the same package.
					if (c.getPackageName().equals(decClass.getPackageName())) 
					{
						return true;
					} else 
					{
						return false;
					}
				}							
			}
		}
		return false;
	}
    
	/**
	 * Get all annotations present on this method.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#getAnnotations()
	 */
	public SpcfAnnotation[] getAnnotations() 
	{
		//make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
										  "encapsulated method");
		Annotation annos[] = mEncapsulatedMethod.getAnnotations(); 
	    SpcfAnnotation[] sAnnos = new SpcfAnnotation[annos.length];             
        for(int i = 0; i < annos.length; i++) 
        { 	        	
        	sAnnos[i] = new SpcfAnnotationImpl(annos[i]);        	
        }	
        return sAnnos;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Get the annotation of the specified type present on this method.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#getAnnotation(Class)
	 */
	public SpcfAnnotation getAnnotation(Class c) 
	{
		//make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
										  "encapsulated method");
		//make sure type is not null
		SpcfParamValidator.checkIsNotNull(c, "c");
		
		Annotation annot = mEncapsulatedMethod.getAnnotation(c);
		if (annot == null) 
		{
			return null;
		}
	    
        return	new SpcfAnnotationImpl(annot);        	        
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Determine whether this element has the specified type annotation.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfMethod#isAnnotationPresent(Class)
	 */	
	public boolean isAnnotationPresent(Class type) 
	{
		//make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedMethod, 
										  "encapsulated method");
		//make sure type is not null
		SpcfParamValidator.checkIsNotNull(type, "type");
		return mEncapsulatedMethod.isAnnotationPresent(type);		
	}
}
