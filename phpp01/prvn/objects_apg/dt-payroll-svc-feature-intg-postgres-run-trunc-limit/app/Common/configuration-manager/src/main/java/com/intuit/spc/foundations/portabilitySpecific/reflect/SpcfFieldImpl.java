package com.intuit.spc.foundations.portabilitySpecific.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

import com.intuit.spc.foundations.portability.reflect.SpcfAnnotation;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;
import com.intuit.spc.foundations.portability.reflect.SpcfField;
import com.intuit.spc.foundations.portability.reflect.SpcfIllegalAccessException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;

/**
 * Class implementation of abstract SpcfField.
 * @author gwang
 */
public class SpcfFieldImpl extends SpcfField {

	/**
	 * the encapsulated field
	 */
	private Field mEncapsulatedField;

	/**
	 * Constructor
	 * @param f The field to be encapsulated.
	 */
	public SpcfFieldImpl(Field f){
		if (f != null) {
			f.setAccessible(true);
		}
		mEncapsulatedField = f;
	}

	/**	 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#equals(Object)
	 */
	public boolean equals(Object obj) 
	{
        //if encapsulated field is null, return false	
		if (mEncapsulatedField == null) 
		{
			return false;
		} 
		if (obj == null)
		{
			return false;
		}

		if (obj instanceof SpcfFieldImpl)
		{
			Field objEncapField = 
				((SpcfFieldImpl)obj).toSpecific();
			if (objEncapField == null)
			{
				return false;
			} else 
			{
				return mEncapsulatedField.equals(objEncapField);
			}
		}
		return false;
	}
	
	/**
	 * Get the class that declares this field
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#getDeclaringClass()
	 */
	public  SpcfClass getDeclaringClass(){
		// make sure we have an encapsulated field
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
				                          "encapsulated field");
		Class dClass = mEncapsulatedField.getDeclaringClass();
		return (SpcfClass)new SpcfClassImpl(dClass);
	}
	
	/**
	 * Get the type of this field
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#getFieldType()
	 */
	public   SpcfClass getFieldType(){
		// make sure we have an encapsulated field
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
				                          "encapsulated field");
		Class fClass = mEncapsulatedField.getType();
		return (SpcfClass)new SpcfClassImpl(fClass);
	}
	
	/**
	 * Get the name of this field
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#getName()
	 */
	public  String getName(){
		// make sure we have an encapsulated field
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
				                          "encapsulated field");
		String name = mEncapsulatedField.getName();
		if (name != null){
			int index = name.lastIndexOf(".");
			if ((index != -1) && (index + 1 < name.length())){
				name = name.substring(index + 1);
			}
		}
		return name;
	}
	
	/**
	 * Get the hashcode of this field
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#hashCode()
	 */
	public  int hashCode(){
		if (mEncapsulatedField == null)
			return 0;
		return mEncapsulatedField.hashCode();
	}
	
	/**
	 * Get the value of this field
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#getValue(Object)
	 */
	public  Object getValue(Object obj) {
		// make sure we have an encapsulated field
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
				                          "encapsulated field");
		try {
			//mEncapsulatedField.setAccessible(true);
			return mEncapsulatedField.get(obj);
		} catch (IllegalArgumentException e1) {
			throw new SpcfIllegalArgumentException(e1);
		} catch (IllegalAccessException e2) {
			throw new SpcfIllegalAccessException(e2);
		}
	}
	
	/**
	 * Determine if this field has a final modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#isFinal()
	 */
	public  boolean isFinal() {
		// make sure we have an encapsulated field
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
				                          "encapsulated field");
		int mod = mEncapsulatedField.getModifiers();
		return Modifier.isFinal(mod);
	}
	
	/**
	 * Determine if this field has a private modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#isPrivate()
	 */
	public  boolean isPrivate(){
		// make sure we have an encapsulated field
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
				                          "encapsulated field");
		int mod = mEncapsulatedField.getModifiers();
		return Modifier.isPrivate(mod);
	}
	
	/**
	 * Determine if this field has a public modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#isPublic()
	 */
	public   boolean isPublic(){
		// make sure we have an encapsulated field
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
				                          "encapsulated field");
		int mod = mEncapsulatedField.getModifiers();
		return Modifier.isPublic(mod);
	}
	
	/**
	 * Determine if this field has a static modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#isStatic()
	 */
	public   boolean isStatic() {
		// make sure we have an encapsulated field
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
				                          "encapsulated field");
		int mod = mEncapsulatedField.getModifiers();
		return Modifier.isStatic(mod);
	}
	
	/**
	 * Set this field on the specified object argument to the specified new 
	 * value. The new value is automatically unwrapped if the underlying 
	 * field has a primitive type
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#setValue(Object, Object)
	 */
	public void setValue(Object obj, Object newValue) {
		// make sure we have an encapsulated field
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
				                          "encapsulated field");
		try {
			//mEncapsulatedField.setAccessible(true);
			mEncapsulatedField.set(obj, newValue);
		} catch (IllegalArgumentException e1) {
			throw new SpcfIllegalArgumentException(e1);	
		} catch (IllegalAccessException e2) {
			throw new SpcfIllegalAccessException(e2);
		}
	}
	
	/**
	 * Obtain a string describing this field 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#toString()
	 */
	public  String toString(){
		if (mEncapsulatedField == null) 
			return "";
		return mEncapsulatedField.toString();
	}
	/**
	* Returns the encapsulated third party runtime object 
	* 
	* @return The encapsulated field
	*/
	public Field toSpecific()
	{
		return mEncapsulatedField;
	}	
	
    /**
     * Returns a string that represents the declared type for this field. 
     *  
     * @return A string that represents the declared type for this field.
     */
    public String getGenericType()
    {
		// make sure we have an encapsulated field
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
				                          "encapsulated field");
		Type type = mEncapsulatedField.getGenericType();
		return type.toString();
		
    }
    
    /**
     * Determine whether the specified list has a field that shares the name 
     * and type with this field.
     * @return  Returns true if there is a matching field, otherwise returns 
     * false. 
     */
    protected boolean matchName(List<SpcfField> fields) {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
				                          "encapsulated field");
		if (fields == null || fields.size() == 0) 
		{
			return false;
		}
		String name = getName();
		//SpcfClass type = getFieldType();
		for (int i = 0; i < fields.size(); i++) 
		{
			SpcfField field = (SpcfField)fields.get(i);
			
			//same name field is found
			if (field.getName().equals(name)) 
			{
				return true;
				/*
				 SpcfClass fType = field.getFieldType();				
				//same name and same type field is found
				if (type.equals(fType)) 
				{					
					return true;
				}*/
			}
		} 
		return false;
    }  
    
    /**
     * Determine whether this field is inherited by the specified class or 
     * interface.
     * @return true if this method is inherited, false otherwise. 
     */
    protected boolean inheritedBy(SpcfClass c) {
		// make sure we have an encapsulated field
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
				                          "encapsulated field");
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
				if (Modifier.isProtected(mEncapsulatedField.getModifiers())) 
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
	 * Get all annotations present on this field.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#getAnnotations()
	 */
	public SpcfAnnotation[] getAnnotations() 
	{
		// make sure we have an encapsulated field
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
				                          "encapsulated field");
		Annotation annos[] = mEncapsulatedField.getAnnotations(); 
	    SpcfAnnotation[] sAnnos = new SpcfAnnotation[annos.length];             
        for(int i = 0; i < annos.length; i++) 
        { 	        	
        	sAnnos[i] = new SpcfAnnotationImpl(annos[i]);        	
        }	
        return sAnnos;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Get the annotation of the specified type present on this field.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#getAnnotation(Class)
	 */
	public SpcfAnnotation getAnnotation(Class c) 
	{
		//make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
										  "encapsulated field");
		//make sure type is not null
		SpcfParamValidator.checkIsNotNull(c, "c");
		Annotation annot = mEncapsulatedField.getAnnotation(c);
		if (annot == null) 
		{
			return null;
		}	    
        return	new SpcfAnnotationImpl(annot);        	        
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Determine whether this element has the specified type annotation.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfField#isAnnotationPresent(Class)
	 */	
	public boolean isAnnotationPresent(Class type) 
	{
		//make sure we have an encapsulated field
		SpcfParamValidator.checkIsNotNull(mEncapsulatedField, 
										  "encapsulated field");
		//make sure type is not null
		SpcfParamValidator.checkIsNotNull(type, "type");
		return mEncapsulatedField.isAnnotationPresent(type);		
	}
}
