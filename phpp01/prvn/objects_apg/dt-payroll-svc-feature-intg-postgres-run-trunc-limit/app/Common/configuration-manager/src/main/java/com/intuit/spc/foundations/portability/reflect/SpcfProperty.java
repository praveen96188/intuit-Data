package com.intuit.spc.foundations.portability.reflect;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;

/**
 * This class provides wrapped methods for the .Net PropertyInfo object 
 * and simulated Property object in Java.
 * 
 * The following rules determine whether getters/setters in Java
 * become properties:
 * 
 * (1) Only getX() in a class that returns a type and take no parameters 
 * will become the accessor of property X of that class; 
 * 
 * (2)  only setX(parameter) that returnd void and take a single parameter 
 * will become the set accessor of property X of that class. 
 * 
 * This mechanism is consistent with the J2C translation rules for getters, 
 * setters and properties.
 * 
 */
public abstract class SpcfProperty {
    
    /**
     * Indicate whether this property is an actual property, or wrapped 
     * from get and set methods in java.  
     * @return true in C#, false in java, if the encapsulated property is not 
     * null.
     * @throws SpcfArgumentNullException if the encapsulated property is null.
     */
    public abstract boolean isActualProperty(); 
    
    /**
	 * Determines whether the encapsulated property is equal to the 
	 * specified object 
	 *
	 * @param obj The object to compare
	 * 
	 * @return true if the encapsulated property of this class is the same 
	 * as the encapsulated property of the specified object obj; return false if 
	 * they are not the same, or obj is null, or obj is not 
	 * an instance of SpcfPropertyImpl, or the encapsulated property is null.
	 * 
	 */
	public abstract boolean equals(Object obj);	

	/**
	 * Determine whether this property can be read.
	 * @return true if this property has a get accessor, false otherwise.
	 */
	public abstract boolean canRead();
	
	/**
	 * Determine whether this property can be written to.
	 * @return true if this property has a set accessor, false otherwise.
	 */
	public abstract boolean canWrite();
	
	
	/**
	 * Get the class that delares this property.
	 * @return the class that delares this property.
     * @throws SpcfArgumentNullException if the encapsulated property is null.
	 */
	public abstract SpcfClass getDeclaringClass();
	
	/**
	 * Get the get accessor of this property.
	 * 
	 * @param includeNonPublic If it is true, it indicates that any private, 
	 * package-private, protected or public get accessor is wanted; if it is false, it 
	 * indicates only public get accessor is wanted.   
	 * @return returns null under any of the following conditions holds:
	 * (1)if this property does not have a get accessor; or 
	 * (2)if includeNonPublic is false and this property has no public get accessor;
	 *  
	 * otherwise returns a SpcfMethod object:
	 * (1) returns the get accessor which may be public or non-public if includeNonPublic is true;
	 * (2) returns the public get accessor if includeNonPublic is false. 
	 * 
	 * @throws SpcfArgumentNullException if the encapsulated property is null.
	 */
	public abstract SpcfMethod getGetMethod(boolean includeNonPublic);
	
	/**
	 * Get the set accessor of this property.
	 * 
	 * @param includeNonPublic If it is true, it indicates that any private, 
	 * package-private, protected or public set accessor is wanted; if it is false, it 
	 * indicates only public set accessor is wanted.   
	 * @return returns null under any of the following conditions holds:
	 * (1)if this property does not have a set accessor; or 
	 * (2)if includeNonPublic is false and this property has no public set accessor;
	 *  
	 * otherwise returns a SpcfMethod object:
	 * (1) returns the set accessor which may be public or non-public if includeNonPublic is true;
	 * (2) returns the public set accessor if includeNonPublic is false. 
	 * 
	 * @throws SpcfArgumentNullException if the encapsulated property is null.
	 */
	public abstract SpcfMethod getSetMethod(boolean includeNonPublic);
	
	/**
	 * Get the name of this field.
	 * @return The name of this field.
	 * @throws SpcfArgumentNullException if the encapsulated property is null.
	 */
	public abstract String getName();
	
	/**
	 * Get the hashcode of this field.
	 * @return the hashcode of this field. Return 0 if the encapsulated 
	 * property is null.
	 */
	public abstract int hashCode();
	
	/**
	 * Get the type of this property.
	 * @return the type of this property.
     * @throws SpcfArgumentNullException if the encapsulated property is null. 
	 */
	public abstract  SpcfClass getPropertyType();
	
	/**
	 * Get the value of this property.
	 * 
	 * @param obj The object whose property value will be obtained. If it is a 
	 * static property, obj can be null.
	 * @param index Optional index values for indexed properties in C#. This value 
	 * should be a null reference for non-indexed properties. In java, this is 
	 * ignored.
	 * @return  The property value for the obj parameter.
	 * @throws SpcfArgumentNullException if encapsulated property is null.
	 * @throws SpcfIllegalAccessException if this property's accessor method 
	 * object enforces access control and the underlying get method is inaccessible.
	 * @throws SpcfInvocationTargetException if the underlying get method 
	 * throws an exception. 
	 * @throws SpcfIllegalArgumentException if the underlying get method is an instance 
	 * method and the specified object argument is not an instance of the 
	 * class or interface declaring the underlying get method (or of a subclass 
	 * or implementor thereof); if the specified object is null but the underlying 
	 * get method is not static. 
	 */
	public abstract Object getValue(Object obj, Object[] index);
	
	/**
	 * Set the value of the property. 
	 * 
	 * @param obj The object whose property value will be set. 
	 * @param newValue The new value to be set for this property.
	 * 	 * @throws SpcfArgumentNullException if encapsulated property is null.
	 * @throws SpcfIllegalAccessException if this property's accessor method 
	 * object enforces access control and the underlying set method is inaccessible.
	 * @throws SpcfInvocationTargetException if the underlying set method 
	 * throws an exception. 
	 * @throws SpcfIllegalArgumentException if the underlying set method is an instance 
	 * method and the specified object argument is not an instance of the 
	 * class or interface declaring the underlying set method (or of a subclass 
	 * or implementor thereof); if the specified object is null but the underlying 
	 * set method is not static; if the number of actual and formal parameters 
	 * differ; if an unwrapping conversion for primitive arguments fails; 
	 * or if, after possible unwrapping, a parameter value cannot be converted 
	 * to the corresponding formal parameter type by a method invocation 
	 * conversion. 
	 */
	public abstract void setValue(Object obj, Object newValue);
	
	/**
	 * Obtain a string describing this property. 
	 * @return A string describing this property. Return empty string if 
	 * the encapsulated property is null. Note: for the same property,
	 * java and C# return in different forms, due to the different type 
	 * names of the property in java and C#. 
	 */
	public abstract String toString();

    
    /**
     * Returns the return value (including generic values) for the
     * current SpcfProperty. 
     * @return The full classname for the property's return value.
     * @throws SpcfArgumentNullException if the encapsulated property is null.
     */
    public abstract String getGenericType();
    
	/**
	 * Get all annotations present on this property.
	 * @return array of SpcfAnnotations representing all annotations on this 
	 * property. Return an empty array if this property has no annotations. 
	 * 
	 * <p>In the current implementation, on java platform a wrapped property's 
	 * annotations include all annotations on its get method and all annotations 
	 * on its set method that are different from any annotation on the get 
	 * method. On .Net platform, a property's annotations are those applied to 
	 * this property directly. Implementation will be adjusted as requirements 
	 * and use cases come up. 
	 * </p>   
	 * @throws SpcfArgumentNullException if encapsulated property is null.
	 */
	public abstract SpcfAnnotation[] getAnnotations(); 
	
	/**
	 * Get the annotation of the specified type present on this property.
	 * @return Returns this element's annotation of the specified type if 
	 * such an annotation is present, else null.
	 * 
	 * <p>In the current implementation, on java platform search a wrapped 
	 * property getter's annotations and setter's annotations, while on .Net 
	 * platform, search those annotations applied to this property directly. 
	 * Implementation will be adjusted as requirements and use cases come up.
	 * </p>   
	 * @throws SpcfArgumentNullException if encapsulated property or type is 
	 * null.
	 */
	public abstract SpcfAnnotation getAnnotation(Class c); 
	
	/**
	 * Get the annotation of the specified type present on the getter of this 
	 * property.
	 * @return Returns the annotation of the specified type if this property 
	 * has a getter and the getter has such an annotation, else null.
	 * @throws SpcfArgumentNullException if encapsulated property or type is 
	 * null.
	 */
	public abstract SpcfAnnotation getGetterAnnotation(Class c); 
	
	/**
	 * Get the annotation of the specified type present on the setter of this 
	 * property.
	 * @return Returns the annotation of the specified type if this property 
	 * has a setter and the setter has such an annotation, else null.
	 * @throws SpcfArgumentNullException if encapsulated property or type is 
	 * null.
	 */
	public abstract SpcfAnnotation getSetterAnnotation(Class c); 
	
	/**
	 * Determine whether this element has the specified type annotation.
	 * 
	 * <p>In the current implementation, on java platform search a wrapped 
	 * property getter's annotations and setter's annotations, while on .Net 
	 * platform, search those annotations applied to this property directly. 
	 * Implementation will be adjusted as requirements and use cases come up.
	 * </p>   
	 * @param type the annotation type to search for.
	 * @return ture if this element has the specified type annotation, 
	 * else false.
	 * @throws SpcfArgumentNullException if encapsulated property or type is 
	 * null.
	 */
	public abstract boolean isAnnotationPresent(Class type);
	
	/**
	 * Determine whether this property has a getter and the getter has the 
	 * specified type annotation.
	 * @param type the annotation type to search for.
	 * @return ture if this property has a getter and the getter has the 
	 * specified type annotation, else false.
	 * @throws SpcfArgumentNullException if encapsulated property or type is 
	 * null.
	 */
	public abstract boolean isGetterAnnotationPresent(Class type);
	
	/**
	 * Determine whether this property has a setter and the setter has the 
	 * specified type annotation.
	 * @param type the annotation type to search for.
	 * @return ture if this property has a setter and the setter has the 
	 * specified type annotation, else false.
	 * @throws SpcfArgumentNullException if encapsulated property or type is 
	 * null.
	 */
	public abstract boolean isSetterAnnotationPresent(Class type);
}
