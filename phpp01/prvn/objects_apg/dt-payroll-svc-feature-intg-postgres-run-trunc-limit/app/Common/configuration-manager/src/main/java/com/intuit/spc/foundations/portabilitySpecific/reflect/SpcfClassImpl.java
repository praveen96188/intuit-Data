package com.intuit.spc.foundations.portabilitySpecific.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarFile;
import java.io.IOException;
import java.io.Serializable;
import java.io.File;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfArgumentOutOfRangeException;
import com.intuit.spc.foundations.portability.SpcfClassCastException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfSecurityException;
import com.intuit.spc.foundations.portability.SpcfSystem;
import com.intuit.spc.foundations.portability.collections.SpcfHashMap;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfPair;
import com.intuit.spc.foundations.portability.io.SpcfDirectory;
import com.intuit.spc.foundations.portability.io.SpcfFile;
import com.intuit.spc.foundations.portability.io.SpcfIOException;
import com.intuit.spc.foundations.portability.io.SpcfStream;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipFile;
import com.intuit.spc.foundations.portability.reflect.SpcfAnnotation;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;
import com.intuit.spc.foundations.portability.reflect.SpcfClassNoSuchFieldException;
import com.intuit.spc.foundations.portability.reflect.SpcfConstructor;
import com.intuit.spc.foundations.portability.reflect.SpcfInstantiationException;
import com.intuit.spc.foundations.portability.reflect.SpcfMethod;
import com.intuit.spc.foundations.portability.reflect.SpcfField;
import com.intuit.spc.foundations.portability.reflect.SpcfPortabilityResolver;
import com.intuit.spc.foundations.portability.reflect.SpcfProperty;
import com.intuit.spc.foundations.portability.reflect.SpcfClassNotFoundException;
import com.intuit.spc.foundations.portability.reflect.SpcfClassNoSuchMethodException; 

/**
 * Class implementation of abstract SpcfClass
 */
public class SpcfClassImpl extends SpcfClass {
	
	/**
	 * serialization constant
	 */
	private static final long serialVersionUID = 162527803052891057L;

	/**
	 * @See com.intuit.spc.foundations.portability.reflect.SpcfClass#
	 * getEncapsulatedClassType()
	 */
	@SuppressWarnings("unchecked")
	private Class mEncapsulatedType;
	
    /**
     * The element type of an array class.
     * 
     */
    private SpcfClass sElementType =  (mEncapsulatedType == null)? 
    		null: SpcfClass.createInstance(mEncapsulatedType);  
    
	/**
	 * Type parameters specified by customers for portable serialization.
	 */
	private SpcfClass[] mSpcfTypeParameters;
	
	/**
	 * Constructor
	 */
	public SpcfClassImpl(){
		mEncapsulatedType = null;
	}
	/**
	 * Constructor
	 * @param c A class to be encapsulated.
	 */
	@SuppressWarnings("unchecked")
	public SpcfClassImpl(Class c){
		//SpcfParamValidator.checkIsNotNull(c, "c");	
		mEncapsulatedType = c;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getClassNameForObject(Object)
	 */
	@Override
	protected String getClassNameForObject(Object o){
		SpcfParamValidator.checkIsNotNull(o, "o");
		return o.getClass().getName();
	} 
	 
	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doInvokeMethod(Class, String, Object[], Class[], boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Object doInvokeMethod(Class type,  
			String method, 
			Object[] args, 
			Class[] argTypes,
			boolean usePortabilityResolver)
	{
		return doInvokeMethod(type, "", "", method, args, 
				argTypes, true, false, usePortabilityResolver); 
	}


	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doInvokeMethod(String, String, String, Object[], String[], boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Object doInvokeMethod(String library, 
			String fullClassName, 
			String method, 
			Object[] args, 
			String[] argTypes,
			boolean usePortabilityResolver)
	{  
		//make sure argTypes is not null
		SpcfParamValidator.checkIsNotNull(argTypes, "argTypes"); 
		for(String c: argTypes) 
		{
			SpcfParamValidator.checkIsNotNull(c, "argTypes array element");
		} 
		Class[] argTypesClass = convertTypes(argTypes, usePortabilityResolver);

		return doInvokeMethod(null, library, fullClassName, method, args, 
				argTypesClass, true, true, usePortabilityResolver);

	}

	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doGetType(String, String, boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Class doGetType(String library, 
			String fullClassName, 
			boolean usePortabilityResolver)
	{
		SpcfParamValidator.checkIsNotNull(library, "library");
		SpcfParamValidator.checkIsNotNull(fullClassName, "full class name");
		
		String name = fullClassName;
		
		//remove generic type's type parameters
		int index = fullClassName.indexOf(SpcfClass.getGenericTypeIndicator());
		if (index > 0) 
		{
			name = fullClassName.substring(0, index);			
			int index1 = fullClassName.indexOf(SpcfClass.getArrayTypeIndicator());
			if (index1 > 0) 
			{
				String arrayTail = fullClassName.substring(index1);
				name = name + arrayTail;
			}			
		}
		String newName = name;
		if (usePortabilityResolver)
		{
			newName = SpcfPortabilityResolver.translateTypeFullName(name);			
		}
		//System.out.println("name = " + fullClassName + " newName = " + newName);
		try 
		{
			Class type = getPrimitiveType(newName); 
			if (type != null)
			{
				return type;
			} else 
			{
				return Class.forName(newName);
				//return Class.forName(newName, false, this.getClass().getClassLoader());
			}
		} catch (ClassNotFoundException ex)
		{
			throw new SpcfClassNotFoundException(library, newName, ex);
            // When testing using JUnit from command line with an invalid full 
			//class name, NoClassDefFoundError is thrown, instead of 
			//ClassNotFoundException as running from maven or eclipse. 
        } catch(java.lang.NoClassDefFoundError e) 
        {
           	throw new SpcfClassNotFoundException(e.getMessage());
        }
	}

	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doInvokeMethod(String, String, String, Object[], boolean)
	 */
	@Override
	protected Object doInvokeMethod(String library, String fullClassName, String method, 
			Object[] args, boolean usePortabilityResolver)
	{
		return doInvokeMethod(null, library, fullClassName, method, args, 
				null, false, true, usePortabilityResolver); 
	}

	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doInvokeMethod(Class, String, Object[], boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Object doInvokeMethod(Class type, String method, 
			Object[] args, boolean usePortabilityResolver)
	{
		return doInvokeMethod(type, "", "", method, args, 
				null, false, false, usePortabilityResolver); 
	}
	
	/**
	 * Creates an instance of the class represented by the specified portable 
	 * representation string.
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doCreateInstanceFromName(String)
	 * 
	 * @param portableTypeStr A string descrbing a type full name and its 
	 * corresponding C# type's assembly information in the portable 
	 * representation format.
	 * @return the instance of the type specified by the input string.
	 * 
	 */
	@Override
	protected Object doCreateInstanceFromName(String portableTypeStr) 
	{
		try 
		{
			SpcfClass sc = getSpcfType(portableTypeStr);
			return sc.createInstanceOfEncapsulatedClass(new Object[0], new Class[0]);
		}
        catch(SpcfClassNotFoundException e)
        {
            throw new SpcfClassNotFoundException(e.getMessage()); 
            // When testing using JUnit from command line with an invalid full class name, 
            // NoClassDefFoundError is thrown, instead of ClassNotFoundException as 
            // running maven or eclipse. 
        } catch(java.lang.NoClassDefFoundError e) {
           	throw new SpcfClassNotFoundException(e.getMessage());
        }

	}
	
	/**
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doCreateInstanceFromName(String, String, boolean)
     */
	@Override
    protected Object doCreateInstanceFromName(String library, 
    										  String fullClassName,
    										  boolean usePortabilityResolver)
    {
        //make sure fullClassName is not null
		SpcfParamValidator.checkIsNotNull(fullClassName, 
				                          "full class name");
        try {        	
        	String newName = fullClassName;
        	if (usePortabilityResolver){
        		newName = SpcfPortabilityResolver.translateTypeFullName(fullClassName);
    		}
            SpcfClass sc = new SpcfClassImpl(Class.forName(newName));
            return sc.createInstanceOfEncapsulatedClass(new Object[0], new Class[0]);
        } 
        catch(ClassNotFoundException e)
        {
            throw new SpcfClassNotFoundException(e.getMessage()); 
            // When testing using JUnit from command line with an invalid full class name, 
            // NoClassDefFoundError is thrown, instead of ClassNotFoundException as 
            // running maven or eclipse. 
        } catch(java.lang.NoClassDefFoundError e) {
           	throw new SpcfClassNotFoundException(e.getMessage());
        }
    }
   
	/**
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doCreateInstanceFromType(Class)
     */
	@Override
	@SuppressWarnings("unchecked")
    protected Object doCreateInstanceFromType(Class type)
    {
        //make sure fullClassName is not null
		SpcfParamValidator.checkIsNotNull(type, 
				                          "type");
        
        SpcfClass sc = new SpcfClassImpl(type);
        return sc.createInstanceOfEncapsulatedClass(new Object[0], new Class[0]);        
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doCreateInstanceFromType(Class, Object[], Class[])
     */
	@Override
	@SuppressWarnings("unchecked")
    protected Object doCreateInstanceFromType(Class type, Object[] args, Class[] argTypes)
    {
        //make sure fullClassName is not null
		SpcfParamValidator.checkIsNotNull(type, 
				                          "type");
        
        SpcfClass sc = new SpcfClassImpl(type);
        return sc.createInstanceOfEncapsulatedClass(args, argTypes);        
    }
    
    /**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getEncapsulatedClassType() 
	 */
	@Override
	public Object getEncapsulatedClassType(){
		return mEncapsulatedType;
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getEncapsulatedType() 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Class getEncapsulatedType(){
		return mEncapsulatedType;
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#setEncapsulatedType(Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setEncapsulatedType(Class value) {
		mEncapsulatedType = value;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#setEncapsulatedClassType(Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setEncapsulatedClassType(Object value) {
		try 
		{
			mEncapsulatedType = (Class)value; 
		} catch (ClassCastException e) 
		{
			throw new SpcfClassCastException(e);
		}
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getFullName()
	 */
	@Override
	public  String getFullName(){
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		return mEncapsulatedType.getName();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isAssignableFrom(SpcfClass)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public  boolean isAssignableFrom(SpcfClass toCompare){
		if (mEncapsulatedType == null || toCompare == null)
		{
			return false;
		}

		if (!(toCompare instanceof SpcfClassImpl)){
			return false;
		}
		
		SpcfClassImpl toCompareImpl = (SpcfClassImpl)toCompare;
		Class t = (Class)toCompareImpl.getEncapsulatedClassType();
		//make sure input class t is not null, otherwise java throws exception
		SpcfParamValidator.checkIsNotNull(t, 
			      "encapsulated type of" + toCompare.toString());
		
		// take care of primitive types to match return from .net
		// we will return true if the types match like "int" and "Integer".
		boolean bPrimitiveType = false;
		Class primitiveClass = null;
		Class otherClass = null;
		if (t.isPrimitive())
		{
			bPrimitiveType = true;
			primitiveClass = t;
			otherClass = mEncapsulatedType;
		} else if (mEncapsulatedType.isPrimitive())
		{
			bPrimitiveType = true;
			primitiveClass = mEncapsulatedType;
			otherClass = t;
		}
		if (bPrimitiveType)
		{
			String primitiveTypeString = primitiveClass.getName();
			String otherTypeString = otherClass.getName();
			if (primitiveTypeString.equals("int"))
			{
				if (otherTypeString.equals("java.lang.Integer"))
					return true;
			} else if (primitiveTypeString.equals("boolean"))
			{
				if (otherTypeString.equals("java.lang.Boolean"))
					return true;
			} else if (primitiveTypeString.equals("long"))
			{
				if (otherTypeString.equals("java.lang.Long"))
					return true;
			} else if (primitiveTypeString.equals("byte"))
			{
				if (otherTypeString.equals("java.lang.Byte"))
					return true;
			} else if (primitiveTypeString.equals("short"))
			{
				if (otherTypeString.equals("java.lang.Short"))
					return true;
			} else if (primitiveTypeString.equals("float"))
			{
				if (otherTypeString.equals("java.lang.Float"))
					return true;
			} else if (primitiveTypeString.equals("double"))
			{
				if (otherTypeString.equals("java.lang.Double"))
					return true;
			} else if (primitiveTypeString.equals("char"))
			{
				if (otherTypeString.equals("java.lang.Character"))
					return true;
			}
		}
		
		return mEncapsulatedType.isAssignableFrom(t);
	}
    
	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doAreAssignable(Class, Class)
	 */
    @SuppressWarnings("unchecked")    
	@Override
    protected boolean doAreAssignable(Class c1, Class c2) {
        if (c1 == null || c2 == null)
        {
            return false;
        } else         
        {
        	return c1.isAssignableFrom(c2);
        }        
    }

	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#createInstanceOfEncapsulatedClass(Object[], Class[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object createInstanceOfEncapsulatedClass(Object[] args, 
			                                        Class[] paramTypes){
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType,
										  "encapsulated type");
		if (this.isPrimitive()) 
		{
			return this.createInstanceOfEncapsulatedPrimitiveType();
		}
		
        if (isAbstract() || isInterface()) 
        {
        	throw new SpcfInstantiationException(new InstantiationException());
        }
		//try {
		//make sure paramTypes is not null, consistent with C#
		SpcfParamValidator.checkIsNotNull(paramTypes, 
				                          "parameter types");
		SpcfConstructor constructor = getDeclaredConstructor(paramTypes);			
		return constructor.invoke(args);
		/*} catch (NoSuchMethodException e){
			throw new SpcfClassNoSuchMethodException(e);
		} catch (IllegalAccessException e){
			throw new SpcfIllegalAccessException(e);
		} catch (InvocationTargetException e){
			throw new SpcfInvocationTargetException(e);
		} catch (InstantiationException e){
			throw new SpcfInstantiationException(e);
		} catch (IllegalArgumentException e){
			throw new SpcfIllegalArgumentException(e);
		}*/	
	}
	
	/**
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doAreEqual(Object, Object)
     */
	@Override
	@SuppressWarnings("unchecked")
    protected boolean doAreEqual(Object o1, Object o2) {
    	if ((o1 == null) || (o2 == null))
    		return false;
    	Class c1 = o1.getClass();
    	Class c2 = o2.getClass();
        return c1.equals(c2);
    }
  
    /**
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#equals(Object)
     */
	@Override
	@SuppressWarnings("unchecked")
    public boolean equals(Object obj)
    {
		if (mEncapsulatedType == null) 
		{
			return false;
		} 
		
		if (obj == null)
		{
			return false;
		}

		if (obj instanceof SpcfClassImpl)
		{
			Class objEncapClass = ((SpcfClassImpl)obj).toSpecific();
			if (objEncapClass == null)
			{
				return false;
			} else 
			{
				return mEncapsulatedType.equals(objEncapClass);
			}
		}
		else if (obj instanceof Class)
		{
			return mEncapsulatedType.equals(obj);
		}
		return false;
    }
    /**
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doCreateAssemblyFullName(String, Class)
     */
	@Override
	@SuppressWarnings("unchecked")
    protected String doCreateAssemblyFullName(String assemblyName, 
    										  Class typeToUse){
    	// this method exists to do work on the .Net side
        return "";
    }

    /**
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doGetTypeAssemblyQualifiedName(Class t)
     */
	@Override
	@SuppressWarnings("unchecked")
    protected  String doGetTypeAssemblyQualifiedName(Class t){
    	// make sure input class c is not null
		SpcfParamValidator.checkIsNotNull(t, "t");
        return "";
    }

    /**
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doGetFullName(Class)
     */
	@Override
	@SuppressWarnings("unchecked")
    protected  String doGetFullName(Class c){
		// make sure input class c is not null
		SpcfParamValidator.checkIsNotNull(c, "c");
		return c.getName();
    }

	/**
	 * Get the array of public methods for this class
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getMethods()
	 */
	@Override
	public SpcfMethod[] getMethods() 
	{
		return getMethods(false, false); 
	} 
	
	/**
	 * Get the array of inherited and declared methods for this class,
	 * including both public and non-public methods if includeNonPublic 
	 * is true, otherwise inlcuding only public methods.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getMethods(boolean)
	 */
	@Override
	public SpcfMethod[] getMethods(boolean includeNonPublic) 
	{
		return getMethods(includeNonPublic, false);   
	}

	/**
	 * Get the array of all public and non-public methods declared by this class
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getDeclaredMethods()
	 */
	@Override
	public  SpcfMethod[] getDeclaredMethods() 
	{
		return getMethods(false, true); 
	}
	/**	
	 * Get the array of public constructors for this class
	 *
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getConstructors()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public  SpcfConstructor[] getConstructors(){
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
										  "encapsulated type");

		Constructor[] constructorArray = mEncapsulatedType.getConstructors();
		if (constructorArray == null || constructorArray.length == 0) 
		{
			return new SpcfConstructor[0];
		}
		SpcfConstructor[] portableConstructorArray = 
			new SpcfConstructorImpl[constructorArray.length];
		for (int i = 0; i < constructorArray.length; i++){
			portableConstructorArray[i] = 
				new SpcfConstructorImpl(constructorArray[i]);
		}
		return portableConstructorArray;
	}
	
	/**	
	 * Get the array of all public and non-public constructors declared by this class,
	 * excluding those inherited from super classes.
	 *
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getDeclaredConstructors()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public  SpcfConstructor[] getDeclaredConstructors() {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
										  "encapsulated type");

		Constructor[] constructorArray = 
			mEncapsulatedType.getDeclaredConstructors();
		if (constructorArray == null || constructorArray.length == 0) {
			return new SpcfConstructor[0];
		}
		SpcfConstructor[] portableConstructorArray = 
			new SpcfConstructorImpl[constructorArray.length];
		for (int i = 0; i < constructorArray.length; i++){
			portableConstructorArray[i] = 
				new SpcfConstructorImpl(constructorArray[i]);
		}
		return portableConstructorArray;
	}
	
	/**
	 * Get the interfaces implemented or inherited by the current class.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getInterfaces()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SpcfClass[] getInterfaces(){
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		
		Class[] interfaceTypes = mEncapsulatedType.getInterfaces();
		if (interfaceTypes == null || interfaceTypes.length == 0) {
			return new SpcfClass[0];
		}
		
		SpcfClass[] spcfInterfaceTypes = 
			new SpcfClassImpl[interfaceTypes.length];

		for (int i = 0; i < spcfInterfaceTypes.length; i++){
			spcfInterfaceTypes[i] = new SpcfClassImpl(interfaceTypes[i]);
		}
		
		HashSet<SpcfClass> set = new HashSet<SpcfClass>();
		
		for (SpcfClass type : spcfInterfaceTypes) 
		{
			set.add(type);
			SpcfClass[] types = type.getInterfaces();
			for (SpcfClass t : types) 
			{
				set.add(t);
			}
		}		
		
		SpcfClass[] interfaceArray = set.toArray(spcfInterfaceTypes);
		return interfaceArray;
	}
	
	/**
	 * Get the array of all public and non-public properties declared 
	 * by this class, excluding inherited ones. 
	 *
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getDeclaredProperties()
	 */
	@Override
	public SpcfProperty[] getDeclaredProperties(){
		SpcfMethod[] methodArray = getDeclaredMethods();
		if (methodArray == null || methodArray.length == 0) {
			return new SpcfProperty[0];
		}			
		return extractProperties(methodArray, true, false,null);
	}
	
	/**
	 * Get the array of properties for this class, simulating properties 
	 * in C# .Net. 
	 *   
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getProperties()
	 */
	@Override
	public SpcfProperty[] getProperties()
	{
		SpcfMethod[] methodArray = getMethods(true);
		return extractProperties(methodArray, false, false,null);	
	}
	
	/**
	 * Get the array of properties, declared and inherited by this type. If 
	 * includeNonPublic is false, return only public properties; otherwise 
	 * return all public and non-public properties.
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getProperties(boolean)
	 */
	@Override
	public SpcfProperty[] getProperties(boolean includeNonPublic) 
	{
		SpcfMethod[] methods = getMethods(includeNonPublic);			
		return extractProperties(methods, includeNonPublic, false,null);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#hasProperty(String, boolean)
	 */
	@Override
	public boolean hasProperty(String name, boolean includeNonPublic) 
	{
		return hasProperty(name, includeNonPublic, false, false);
	}
	
	/**
	 * Get the property with the specified name, declared or inherited by 
	 * this type. If includeNonPublic is false, search among only public properties; otherwise 
	 * search all public and non-public properties.
	 * @param includeNonPublic  Search both public and non-public 
	 * properties if includeNonPublic is true, otherwise search only public 
	 * properties.	 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getProperty(String, boolean)
	 */
	@Override
	public SpcfProperty getProperty(String name, boolean includeNonPublic) 
	{
        return getProperty(name, includeNonPublic, false, false, true);  
	} 
	
	/** 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getAncestorPropertiesWithPrivateAccessor()
	 */
	@Override
	public SpcfProperty[] getAncestorPropertiesWithPrivateAccessor() 
	{ 			
		return getProperties(true, false, true);
	}
	
	/** 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getAncestorPropertyWithPrivateAccessor(String)
	 */
	@Override
	public SpcfProperty getAncestorPropertyWithPrivateAccessor(String name) 
	{
		return getProperty(name, true, false, true, true);
	} 
	
	/** 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#hasAncestorPropertyWithPrivateAccessor(String)
	 */
	@Override
	public boolean hasAncestorPropertyWithPrivateAccessor(String name) 
	{
		return hasProperty(name, true, false, true);
	}
	
	/**
	 * Extract properties from an array of methods, i.e., wrapping getters
	 * and setters into properties in C#.
	 * @param methodArray The array of methods to extract properties from.
	 * @param includeNonPublic if true, return all public and non-public 
	 * properties, otherwise return only public properties.
	 * @param onlyPrivateAncestor if true, will require that either the get
	 * or set method has private accessor.
	 * @param inheritedMethods
	 * @return An array of SpcfProperty objects. 
	 */
	private SpcfProperty[] extractProperties(SpcfMethod[] methodArray,
			boolean includeNonPublic, boolean onlyPrivateAncestor, List<SpcfMethod> inheritedMethods)
	{
		if (methodArray == null || methodArray.length == 0) {
			return new SpcfProperty[0];
		} 
		
		SpcfHashMap<String, SpcfPropertyImpl> propertyTable = SpcfHashMap.<String, SpcfPropertyImpl>createInstance();
		 
		for (int i=0; i<methodArray.length; i++) 
		{
			SpcfMethod method = methodArray[i];
			String name = method.getName(); 
			
			//Determine if it is a eligible getter/setter
			boolean isGet = name.startsWith("get");
			boolean isSet = name.startsWith("set");
		 
			if (isGet || isSet) 
			{
				SpcfClass [] paraTypes = method.getParameterTypes(); 
				SpcfClass returnType = method.getReturnType();
				boolean returnVoid =  (returnType.getSimpleName().equals("void")); 
				String propertyName = name.substring(3); 
				
				Method setMethod = null; 
				Method getMethod = null;  
				if (isGet && !returnVoid &&  (paraTypes == null || paraTypes.length == 0)) 
				{ 
					getMethod = ((SpcfMethodImpl)methodArray[i]).toSpecific(); 
				} 
				else if (isSet && returnVoid &&  paraTypes != null && paraTypes.length == 1) 
				{			 
					setMethod = ((SpcfMethodImpl)methodArray[i]).toSpecific();  
				}
				
				if (getMethod != null || setMethod != null)
				{   
					// found a property method so add it as a property to hashmap.
					// if already there, readd the property with matching get/set method.
					SpcfPropertyImpl propertyMapped;
					SpcfPropertyImpl propertyNew;  
					if (propertyTable.containsKey(propertyName))
					{ 
						propertyMapped = propertyTable.getItem(propertyName);
						if (propertyMapped != null)
						{
							// found matching property, but double check parameter type if set.
							//boolean remove = true; 
							if (setMethod != null)
							{
								Method mExist = propertyMapped.getEncapsulatedSetMethod();
								if (mExist == null) // would be non null if diff param types
								{  
									getMethod = propertyMapped.getEncapsulatedGetMethod();
									propertyTable.remove(propertyName);
								} 
							}
							else
							{
								setMethod = propertyMapped.getEncapsulatedSetMethod();
								propertyTable.remove(propertyName);
							} 
						}
					}
					propertyNew = new SpcfPropertyImpl(getMethod, setMethod);
					propertyTable.add(propertyName, propertyNew);  
				}
			} 
		}
		
		List<SpcfProperty> properties = new ArrayList<SpcfProperty>();
		 
		ISpcfIterator<SpcfPair<String, SpcfPropertyImpl>> iterator = propertyTable.getIterator();
		while (iterator.hasNext())
		{
			SpcfPair<String, SpcfPropertyImpl> pair = iterator.next(); 
			SpcfPropertyImpl p = pair.getValueItem();
			if (onlyPrivateAncestor)
			{
				SpcfMethod setMethod = p.getSetMethod(true);
				SpcfMethod getMethod = p.getGetMethod(true);
				//Method getMethod = p.getEncapsulatedSetMethod();
				
				boolean privateFound = false;
                if (getMethod != null)
                {
                	//package protected fix
                    //privateFound = getMethod.isPrivate();
                    privateFound = !((SpcfMethodImpl)getMethod).matchNameAndTypes(inheritedMethods);
    				
                }
                if (!privateFound && (setMethod != null))
                {
                	//package protected fix
                	//privateFound = setMethod.isPrivate();
                    privateFound = !((SpcfMethodImpl)setMethod).matchNameAndTypes(inheritedMethods);
                }
				
				if (privateFound)
				{
					properties.add(p); 
				} 
			}
			else if (includeNonPublic) 
			{
				properties.add(p); 
			}
			else if (p.isPublic()) 
			{
				properties.add(p);	 
			}  
		}   
		 
		SpcfProperty[] spcfPropertyArray = new SpcfProperty[properties.size()];
		for (int i=0; i< properties.size(); i++) 
		{
			 //System.out.println("property[" + i + "] = " + properties.get(i).getName());
			 //System.out.println("mEncapGetMethod = " + properties.get(i).getGetMethod(true));
			 //System.out.println("mEncapSetMethod = " + properties.get(i).getSetMethod(true)); 
			 spcfPropertyArray[i] = properties.get(i); 	
		}
		return spcfPropertyArray;
	}
	
	/**
	 * Get a spefic property, public or non-public, declared by this 
	 * class with the specified name 
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getDeclaredProperty(String)
	 */
	@Override
	public SpcfProperty getDeclaredProperty(String name)
	{
        // make sure we have an encapsulated type
        SpcfParamValidator.checkIsNotNull(mEncapsulatedType, "encapsulated type");
        SpcfParamValidator.checkIsNotNull(name, "property name");
		SpcfProperty ret = null;
		SpcfProperty[] properties = getDeclaredProperties();
		for (int i=0; i<properties.length; i++) 
		{
			if (properties[i].getName().equals(name)) 
			{
					//properties[i].mIsActualProperty = false;
					ret = properties[i];
					break;
			}
		}
		if (ret == null) 
		{
			throw new SpcfClassNoSuchMethodException("No such property: " + name);
		} else 
		{
			return ret;
		}
	}
	/**
	 * Get a specific public property for this class with the 
	 * specified name.
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getProperty(String)
	 */
	@Override
	public SpcfProperty getProperty(String name)
	{
		return this.getProperty(name, false, false, false, true); 
	}
	
	/**
	 * Get the super class extended by this class
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getSuperClass()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SpcfClass getSuperClass(){
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");		
		Class superClass = mEncapsulatedType.getSuperclass();
		if (superClass == null) 
		{
			return null;
		}
		SpcfClass spcfSuperClass = new SpcfClassImpl(superClass);
		return spcfSuperClass;
	}
	/**
	 * A string containing the name of the Type, excluding 
	 * the namespace of the Type 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getSimpleName()
	 */
	@Override
	public String getSimpleName(){
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		return mEncapsulatedType.getSimpleName();
	}
	/**
	 * A string containing the name of the package this class 
	 * belongs to.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getPackageName()
	 */	
	@Override
	public String getPackageName(){
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		
		Package pkg = mEncapsulatedType.getPackage();
		if (pkg == null)
			return null;
		return pkg.getName();
	}
	
	/**
	 * Get a public constructor for this class with the specified parameters
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getConstructor(Class[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SpcfConstructor getConstructor(Class[] parameterTypes) {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		 // make sure input parameterTypes is not null 
        SpcfParamValidator.checkIsNotNull(parameterTypes,
                                          "constructor parameter types");
        for(Class c: parameterTypes) 
        {
        	SpcfParamValidator.checkIsNotNull(c, 
        			"parameterTypes array element");
        }
		try {
			Constructor c = mEncapsulatedType.getConstructor(parameterTypes);				
			return new SpcfConstructorImpl(c); 
		} catch (NoSuchMethodException e) {
			throw new SpcfClassNoSuchMethodException(e);
		} catch (SecurityException e) {
			throw new SpcfSecurityException(e);
		}
	}
	
	/**
	 * Get a constructor declared by this class with the specified parameters.
	 * @param parameterTypes The parameter array. 
	 * @return A constrcutor declared by this class, which matches the 
	 * specified parameterTypes.
	 * @throws SpcfClassNoSuchMethodException if no matching constructor is found.
	 * @throws SpcfSecurityException if a security manager is set, and the 
	 * access to this constructor or the package of this class is denied. 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SpcfConstructor getDeclaredConstructor(Class[] parameterTypes) {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
        // make sure input parameterTypes is not null 
        SpcfParamValidator.checkIsNotNull(parameterTypes,
                                          "constructor parameter types");
        for(Class c: parameterTypes) 
        {
        	SpcfParamValidator.checkIsNotNull(c, 
        			"parameterTypes array element");
        }
		try {
			Constructor c = mEncapsulatedType.getDeclaredConstructor(parameterTypes);			
			return new SpcfConstructorImpl(c); 
		} catch (NoSuchMethodException e) {
			throw new SpcfClassNoSuchMethodException(e);
		} catch (SecurityException e) {
			throw new SpcfSecurityException(e);
		}
	}

	
	/**
	 * Get the class that declares this member
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getDeclaringClass()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SpcfClass getDeclaringClass(){
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");

		Class dClass = mEncapsulatedType.getDeclaringClass();
		if (dClass == null) 
		{
			return null;
		}
		return new SpcfClassImpl(dClass);
	} 
	
	/**
	 * Get a field, declared or inherited, for this class with the specified 
	 * name.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getField(String, boolean)
	 */
	@Override
	public SpcfField getField(String name, boolean includeNonPublic) 
	{
		 return getField(name, includeNonPublic, false);	
	}
	
	/**
     * Get a field with the specified name for this class
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getField(String)
     */
	@Override
	public SpcfField getField(String name) 
	{
		 return getField(name, false, false);	
	} 
	
	/**
     * Get a field with the specified name declared by this class
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getDeclaredField(String)
     */
	@Override
	public SpcfField getDeclaredField(String name) 
	{
		 return getField(name, false, true);	
	}  
	
	/**
	 * Get the array of fields, declared and inherited. If ncludeNonPublic 
	 * is false, return only public fields; otherwise return all public and 
	 * non-public fields.
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getFields(boolean)
	 */
	@Override
	public SpcfField[] getFields(boolean includeNonPublic) 
	{
		return getFields(includeNonPublic, false); 
	}
	
	/**
	 * Get the array of public fields for this class
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getFields()
	 */
	@Override
	public SpcfField[] getFields()
	{
		return getFields(false, false);  
	}
	
	/**
	 * Get the array of all public and non-public fields declared by this class
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getDeclaredFields()
	 */
	@Override
	public SpcfField[] getDeclaredFields()
	{
		return getFields(false, true);
	}
	
	/**
	 * Get the hashcode of this class
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#hashCode()
	 */
	@Override
	public int hashCode(){
		// make sure we have an encapsulated type
		if(mEncapsulatedType == null) 
		{
			return 0; 
		}				     
		return mEncapsulatedType.hashCode();
	}

	/**
	 * Get an interface implemented by this class with the specified name
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getInterface(String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SpcfClass getInterface(String name){
		// make sure we have an encapsulated type and interface name is not null
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");	
		SpcfParamValidator.checkIsNotNull(name, "interface name");		
		
		Class[] interfaces = mEncapsulatedType.getInterfaces();
		
		if (interfaces == null || interfaces.length == 0) 
		{
			throw new SpcfClassNotFoundException("No such interface " + name);
		}
		for (int i=0; i<interfaces.length; i++){
			if (interfaces[i].getName().equals(name)) 
			{				
				return new SpcfClassImpl(interfaces[i]);				
			}			
		}
		throw new SpcfClassNotFoundException("No such interface " + name);					
	}

	
	
	/**
	 * Get a public method for this class with the specified name and parameters
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getMethod(String, Class[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SpcfMethod getMethod(String name, Class[] parameterTypes)
	{ 
		return getMethod(name, false, false, parameterTypes, false); 
	}

	/**
	 * Get a method declared by this class with the specified name and parameters
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getDeclaredMethod(String, Class[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SpcfMethod getDeclaredMethod(String name, Class[] parameterTypes)
	{ 
		return getMethod(name, false, true, parameterTypes, false);		
	}
	
	/**
	 * Get a public method for this class with the specified name and parameters
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getMethod(String, Class[], boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SpcfMethod getMethod(String name, Class[] parameterTypes, 
			                    boolean usePortabilityResolver)
	{ 
		return getMethod(name, false, false, parameterTypes, usePortabilityResolver);		
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getMethod(String, boolean, Class[], boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SpcfMethod getMethod( String name, boolean includeNonPublic,
			Class[] parameterTypes, boolean usePortabilityResolver)
	{  
		return	getMethod(name, includeNonPublic, false, parameterTypes, usePortabilityResolver);			
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getMethod(String, boolean, Class[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SpcfMethod getMethod( String name, boolean includeNonPublic,
			Class[] parameterTypes)		
	{
		 return getMethod(name, includeNonPublic, false, parameterTypes, false);    
	}
	
	/**
	 * Get a method declared by this class with the specified name and parameters
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getDeclaredMethod(String, Class[], boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SpcfMethod getDeclaredMethod(String name, Class[] parameterTypes, 
			                            boolean usePortabilityResolver)
	{  
		return getMethod(name, false, true, parameterTypes, usePortabilityResolver);		
	} 
	
	/**
	 * Determine if this constructor has an abstract modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isAbstract()
	 */
	@Override
	public boolean isAbstract() {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		int mod = mEncapsulatedType.getModifiers();
		return Modifier.isAbstract(mod);
	}
	
	/**
	 * Determine if this object represents an array
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isArray()
	 */
	@Override
	public boolean isArray() {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		return mEncapsulatedType.isArray();
	}
	
	/**
	 * Determine if this object represents an enumeration
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isEnum()
	 */
	@Override
	public boolean isEnum() {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		return mEncapsulatedType.isEnum();
	}

	/**
	 * Determine if the specified object is an instance of this class
	 *
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isInstance(Object)
	 */
	@Override
	public boolean isInstance(Object obj) {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		return mEncapsulatedType.isInstance(obj);
	}

	/**
	 * Determine if this object represents an interface
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isInterface()
	 */
	@Override
	public boolean isInterface(){
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		return mEncapsulatedType.isInterface();
	}

	/**
	 * Determine if this constructor has a final modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isFinal()
	 */
	@Override
	public  boolean isFinal(){
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		int mod = mEncapsulatedType.getModifiers();
		return Modifier.isFinal(mod);
	}
	/**  
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isPrimitive()
	 */
	@Override
	public boolean isPrimitive(){
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		
		if (mEncapsulatedType.isPrimitive())
		{
			return true;
		} else {
			//check if it is one of the predefined Class objects for 
			//those primitive types or void, simiulates C# Type.IsPrimitive
			String name = mEncapsulatedType.getSimpleName();
			if (name.equals("Short") || name.equals("Integer") || 
			    name.equals("Byte") || name.equals("Long") ||
				name.equals("Float") || name.equals("Double") ||
				name.equals("Boolean") || name.equals("Char") || 
				name.equals("Void")) 
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Determine if this constructor has a public modifier
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isPublic()
	 */
	@Override
	public   boolean isPublic(){
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		int mod = mEncapsulatedType.getModifiers();
		return Modifier.isPublic(mod);
	}
	
	/**
	 * Determine if this class is serializable
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isSerializable()
	 */
	@Override
	public boolean isSerializable() {		
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		if (Serializable.class.isAssignableFrom(mEncapsulatedType)){
		     return true;
		}
		return false;
	}
	
    /**
     * Determine if this class is a generic type
     * @return True if this class is a generic type, false otherwise
     * @see SpcfClass#isGenericType()
     */
	@Override
	@SuppressWarnings("unchecked")
    public boolean isGenericType()
    {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
        TypeVariable[] typeArray = mEncapsulatedType.getTypeParameters();
        
        return (typeArray.length > 0);
    }
    
   
    /**
     * Return the type parameters associated with this SpcfClass.
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getTypeParameters()
     */
	@Override
	@SuppressWarnings("unchecked")
    public String[] getTypeParameters()
    {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
        TypeVariable[] typeArray = mEncapsulatedType.getTypeParameters();
        if (typeArray == null) 
        {
        	return new String[0];
        }
        String[] strArray = new String[typeArray.length];
        
        for(int i = 0; i < typeArray.length; i++)
        {
            strArray[i] = typeArray[i].toString();
        }
            
        return (strArray);
    }
    
    /**
     * Return the type parameters specified by customers for this SpcfClass 
     * instance.
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getSpcfTypeParameters()
     */
    @Override
    public SpcfClass[] getSpcfTypeParameters()
    {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");            
        return mSpcfTypeParameters;
    }
    
    /**
     * Set the type parameters specified by customers for this SpcfClass 
     * instance.
     * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#setSpcfTypeParameters(SpcfClass[])
     */
    @Override
    public void setSpcfTypeParameters(SpcfClass[] typeParams)
    {
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");  
		//make sure input is not null
		SpcfParamValidator.checkIsNotNull(typeParams, 
        		                          "type parameters");
		//make sure none of typeParams elements is null
		for (int i = 0; i < typeParams.length; i++) 
		{
			SpcfClass sc = typeParams[i];
			SpcfParamValidator.checkIsNotNull(sc, 
            	"typeParams[" + i + "]");
			SpcfParamValidator.checkIsNotNull(sc.getEncapsulatedType(), 
        		"Encapsulated type of typeParams[" + i + "]");			
		}

        this.mSpcfTypeParameters = typeParams;
    }
	/**
	 * Obtain a string describing this class
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#toString() 
	 */
	@Override
	public  String toString() {
		if (mEncapsulatedType == null) 
			return ""; 
		return mEncapsulatedType.toString();
	}
	
	/**
	* Returns the encapsulated third party runtime object 
	* 
	* @return The encapsulated type
	*/
	@SuppressWarnings("unchecked")
	public Class toSpecific()
	{
		return mEncapsulatedType;
	}
	
	/**
	 * Determines whether this class is the same as or derives from the 
	 * specified class.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isSameOrSubclassOf(Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean isSameOrSubclassOf(Class c)
	{
		return equals(c)||isSubclassOf(c);
	}

	/**
	 * Determines whether this class is the same as or derives from the 
	 * specified spcfclass' encapsulated type.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isSameOrSubclassOf(SpcfClass)
	 */
	@Override
	public boolean isSameOrSubclassOf(SpcfClass c)
	{
		return equals(c)||isSubclassOf(c);
	}
	
	/**
	 * Determines whether this class derives from the specified class.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isSubclassOf(Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean isSubclassOf(Class c)
	{
        //make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");		
        // make sure input class c is not null
        SpcfParamValidator.checkIsNotNull(c,
                                          "c");
		if (isInterface() || c.isInterface())
		{
			return false;
		}
		
		SpcfClass superClass = getSuperClass();
	    if (superClass == null || superClass.getEncapsulatedClassType() == null) 
	    {
	    	return false;
    	} else 
    	{
    		if (superClass.equals(c))
    		{
    			return true;
    		} else 
    		{
    			return superClass.isSubclassOf(c);
    		}
    	}	
	}
	
	/**
	 * Determines whether this class derives from the encapsulated type by the 
	 * specified spcfclass' encapsulated type.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isSubclassOf(SpcfClass)
	 */
	@Override
	public boolean isSubclassOf(SpcfClass c)
	{
        //make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");		
        // make sure input class c is not null
        SpcfParamValidator.checkIsNotNull(c,
                                          "c");
		return isSubclassOf(c.getEncapsulatedType());
	}
	
	/**
	 * Locates the jar file which the encapsulated class by this SpcfClass 
	 * instance belongs to. 
	 * 
	 * <p>Note: In this method we search for the jar that contains the 
	 * encapsulated class until reaching the direct subdirectory of the root 
	 * directory of the path where the encapsulated class is located. This 
	 * does not work for any class that is a class (not in a jar) in the 
	 * .classpath and the corresponding jar is located outside of the path 
	 * tree of the direct subdirectory of the root 
	 * directory of the path where the class is located. The normal scenario 
	 * in which this method is used is that the jar will be found immediately at the 
	 * class' CodeResource location. In this situation this method behaves the same as
	 * locateSource(). This method will be deleted once we get a jar for testing
	 * locateSource() and portableRepresentation().  
	 * </p>
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#locateJar()
	 *
	protected String locateJar() 	
	{
	    //make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");	
		Class c = getEncapsulatedType();
		try {
			JarFile jf = null;
			URL url = null;
			ProtectionDomain pd = c.getProtectionDomain();	
			//System.out.println("pd = " + pd.toString());
			if (pd != null) 
			{
				CodeSource cs = pd.getCodeSource();
				//System.out.println("cs = " + cs.toString());
				if (cs != null)
				{
					url = pd.getCodeSource().getLocation();
				}
			} 
			
			if (url == null) 
			{
				throw new SpcfIOException("Cannot locate " + 
						c.getSimpleName() + ".class ");
			}
			//System.out.println("url: " + url);
			
			//get rid of "file:/" at the beginning of url string
			//String path = urlStr.substring(6, urlStr.length());
			//System.out.println("path = " + path);
			File currentDir = new File(url.toURI());
			//System.out.println("current dir = " + currentDir);
			
			//search until direct subdirectory of root directory is reached
			while (currentDir != null && currentDir.getParentFile() != null) 
			{							
				jf = searchJar(c, currentDir);
				if (jf == null) 
				{
					currentDir = currentDir.getParentFile();
					//System.out.println("currentDir = " + currentDir);
				} else
				{
					break;
				}
			}
			
			if (jf == null) 
			{
				throw new SpcfIOException("Cannot find a jar file that defines " + 
						c.getSimpleName() + " class");
			}
			//System.out.println("Class to search for jar = " + c.getName());
			//System.out.println("jar file = " + jf.getName());
			
			return jf.getName();		
		} catch (URISyntaxException e) 
		{
			throw new SpcfIOException(e);
		}	
	}*/
	
	/**
	 * Copy the resource with the specified name from 
	 * the library, i.e., jar file in java, or assembly file in .Net, 
	 * which contains the encapsulated class by this SpcfClass instance, into the
	 * specified directory with the specified file name.
	 * 
	 * This method on .net side handles only manifest resources, but on 
	 * java side, it handles any entry in the jar that contains the encapsulated 
	 * class.
	 * 
	 * @param resourceName The name of the resource from the library to get as 
	 * input stream. If it is contained in a sub-directory in the library, the 
	 * path needs to be specified. For example, MyType.class is contained its 
	 * corresponding jar under directory structure corresponding to its name 
	 * space. The input resourceName is expected as 
	 * "com/intuit/spc/foundations/test/MyType.class" on java side.  
	 * But on .net side, this method does not handle classes, handles only manifest 
	 * resources.
	 * 
	 * @param targetDir The directory in file system for copying the resource to.
	 * @param fileName The name for the resource file to copy the resource into. 
	 * @return true if the jar or assembly file that contains the encapsulated 
	 * class is found, the resource is found and copied successfully; otherwise 
	 * return false. 
	 * 
	 * @throws SpcfArgumentNullException if the encapsulated class, the input 
	 * targetDir, resourceName or fileName is null.
	 * @throws SpcfArgumentOutOfRangeException if the input resourceName or 
	 * fileName is an empty string.
	 * @throws SpcfIllegalArgumentException if the input targetDir does not exist in 
	 * the file system.
	 *  
	 */
	public boolean copyResource(String resourceName, 
			SpcfDirectory targetDir, String fileName) 	
	{
        // make sure we have an encapsulated type
        SpcfParamValidator.checkIsNotNull(mEncapsulatedType,
                                          "encapsulated type");
        
        SpcfParamValidator.checkIsNotNullOrBlankString(fileName, "fileName");
        SpcfParamValidator.checkIsNotNullOrBlankString(resourceName, 
        		"resourceName");
        SpcfParamValidator.checkIsNotNull(targetDir, "targetDir");

        if (!targetDir.exists()) 
        {
            throw new SpcfIllegalArgumentException(targetDir.getPath() + 
            		" does not exist in the file system.");
        }

		try 
		{
	        String libraryPath = locateSource();
	        SpcfZipFile zip = SpcfZipFile.createInstance(libraryPath);
	        SpcfZipEntry zipEntry = zip.getEntry(resourceName);        
	        SpcfStream inputStream = zip.getInputStream(zipEntry);
	        String targetPath = targetDir.getPath() + 
	        	SpcfSystem.getFileSeparator() + fileName;
	        SpcfStream outStream = SpcfFile.openForBinaryWriting(targetPath);
	        for(int j = 0; j < zipEntry.getSize(); j++)
	        {
	        	byte b = inputStream.readByte();                     		
	        	outStream.write(b);	        
			}
	        outStream.flush();
	        outStream.close();
	        inputStream.close();
	        return true;
		} catch (Exception e) 
		{
			return false;
		}
	}
	/**
	 * Locates the library, i.e., jar file in java, or assembly file in .Net, 
	 * which contains the encapsulated class by this SpcfClass instance.
	 * 
	 * @return String The jar file or assembly file name with full path.
	 * @throws SpcfIOException if an I/O error has occurred, or the jar 
	 * file cannot be located.
	 */	
	@SuppressWarnings("unchecked")
	public String locateSource() 	
	{
	    //make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");	
		Class c = getEncapsulatedType();
		URL url = null;
		ProtectionDomain pd = c.getProtectionDomain();	
		//System.out.println("pd = " + pd.toString());
		if (pd != null) 
		{
			CodeSource cs = pd.getCodeSource();
			//System.out.println("cs = " + cs.toString());
			if (cs != null)
			{
				url = cs.getLocation();
			}
		} 
		
		if (url == null) 
		{
			throw new SpcfIOException("Cannot locate " + 
					c.getSimpleName() + ".class ");
		}
		String path = url.getPath();
		if (path.endsWith(".jar"))
		{
			try {
				JarFile f = new JarFile(new File(url.toURI()));
				//e.g., "/C:/gwang1/.maven/repository/junit/jars/junit-3.8.1.jar"	
			
				//remove "/" on the front
				//System.out.println("jar = " + path.substring(1));
				//return path.substring(1); 
				//url path is encoded, any space in the path is replaced by %20
				
				return f.getName();
			} catch (URISyntaxException e) {
				throw new SpcfIOException(e);
			} catch (IOException e1) {
				throw new SpcfIOException(e1);
			}									
		} else 
			{
				throw new SpcfIOException(c.getSimpleName() + 
						".class is not loaded from a jar file");
			}			
	}
	
	/**
	 * Determine whether the given type is a common type in both java and C# 
	 * that is allowed in SPCF portable language, such as String/string, 
	 * boolean/bool.
	 * 
	 * @param type The type to be checked.
	 * 
	 * @return true if the given type is a common tye in both java and C# that 
	 * is allowed in SPCF portable language, otherwise false.
	 */
	@SuppressWarnings("unchecked")
	protected boolean isCommonType(Class type)
	{
		String name = type.getName();
		if (SpcfPortabilityResolverImpl.commonTypeMap.get(name) != null)
		{
			return true;
		}
		return false;
	}

	
    /**
     * Return the number of dimensions of an array class.
     * @return the number of dimensions of the encapsulated array class. 
     * @throws SpcfIllegalArgumentException if the encapsulated class is not an 
     * array type.
     */
	@Override
    public int getArrayRank () 
    {
    	if (!this.isArray()) 
    	{
    		throw new SpcfIllegalArgumentException("The encapsulated type " + 
    				getFullName() + " is not an array type");
    	}
		String className = this.getFullName();
		//Array class name starts with "[L" for 1-D arrays, "[[L" for 2-D arrays
		int index = className.indexOf("[L");
		//Offset from last index "[L" to the number or dimensions of the array 
		int offset = 1;
		return index + offset;	
    }
	
	/**
	 * Search a jar file that contains the specified class from the root 
	 * directory of the class location and all its sub directories.
	 * 
	 * @param c a Class instance to locate the its corresponding jar file. 
	 * @param file the file or directory to search for the jar file that 
	 * contains the specified class.
	 * @return a matching JarFile instance if found, null otherwise.
	 * @throws SpcfArgumentNullException if c or file is null.
	 * @throws SpcfIOException if an I/O error has occurred. 
	 *
	protected JarFile searchJar(Class c, File file) 
	{
        //make sure input class c is not null
		SpcfParamValidator.checkIsNotNull(c, 
				                          "class");		
        // make sure input file is not null
        SpcfParamValidator.checkIsNotNull(file,
                                         "file");		
		JarFile jf = null;
		if (file.isFile()) 
		{			
			if (file.getName().endsWith(".jar"))
			{
				//System.out.println("jar to open = " + file.getName());
				try 
				{
					jf = new JarFile(file);
					Enumeration<JarEntry> e = jf.entries();
					while (e.hasMoreElements()) 
					{
						JarEntry entry = e.nextElement();										
						if (entry.getName().endsWith(c.getSimpleName()+ ".class")) 
						{
							//System.out.println("class to search for jar= " + c.getName());
							//System.out.println("matching jar entry = " + entry.getName() + "\n");								
							return jf;													
						}
					}
				} catch (IOException e) {
					//some third party jars might not be able to open, just 
					//continue searching
					//throw new SpcfIOException(e);
					System.out.println("Error: " + e.getMessage());
				}
			} 
		} else if (file.isDirectory()) 
		{	//search all files and sub directories  
						
			//System.out.println(file + " is dir");
			File[] files = file.listFiles();
			//System.out.println("files has " + files.length);
			for (int i = 0; i < files.length; i++) 
			{
				File f = files[i];
				//System.out.println("file = " + f);
				jf = searchJar(c, f);
				if (jf != null) 
				{					
					return jf;
				}
			}
		} 		
		return null;
	}*/
	
	/**
	 * Merge a list of methods with an array of methods, does not allow two 
	 * methods have the same name and parameter types.
	 *  
	 * @param mList the list of methods to be merged.
	 * @param mArray the array of methods to be merged.
	 * @return the merged list of methods. 
	 */
	private List<SpcfMethod> mergeMethods(List<SpcfMethod> mList, 
			SpcfMethod[] mArray) 
	{
		//make sure input list is not null
		SpcfParamValidator.checkIsNotNull(mList, 
				                          "mList - list of methods");		
		if (mArray == null || mArray.length == 0) 
		{
			return mList;
		}
        for (int i = 0; i < mArray.length; i++) 
        {
        	if (((SpcfMethodImpl)mArray[i]).matchNameAndTypes(mList) == false)
        	//Class[] argTypes = ((SpcfMethodImpl)mArray[i]).toSpecific().
        	//	getParameterTypes();
        	//if (searchMethod(mArray[i].getName(), argTypes, mList) != null)
        	{
        		mList.add(mArray[i]);
        	}        	
        }
        return mList;
	}
	
	/**
	 * Get all annotations present on this class.
	 * @return array of SpcfAnnotations representing all annotations on this 
	 * class. Return an empty array if this class has no annotations.
	 */
	@Override
	public SpcfAnnotation[] getAnnotations() 
	{
		//make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
										  "encapsulated type");
		Annotation annos[] = mEncapsulatedType.getAnnotations(); 
	    SpcfAnnotation[] sAnnos = new SpcfAnnotation[annos.length];             
        for(int i = 0; i < annos.length; i++) 
        { 	        	
        	sAnnos[i] = new SpcfAnnotationImpl(annos[i]);        	
        }	
        return sAnnos;
	}
	
	/**
	 * Get the annotation of the specified type present on this class.
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getAnnotation(Class)
	 *
	 */
	@SuppressWarnings("unchecked")
	@Override
	public SpcfAnnotation getAnnotation(Class c) 
	{
		//make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
										  "encapsulated type");
		//make sure type is not null
		SpcfParamValidator.checkIsNotNull(c, "c");
		
	
		Annotation annot = mEncapsulatedType.getAnnotation(c);
		if (annot == null) 
		{
			return null;
		}	    
        return new SpcfAnnotationImpl(annot);        	        
	}
	
	/**
	 * Determine whether this element has the specified type annotation.
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#isAnnotationPresent(Class)
	 */	
	@SuppressWarnings("unchecked")
	@Override
	public boolean isAnnotationPresent(Class type) 
	{
		//make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
										  "encapsulated type");
		//make sure type is not null
		SpcfParamValidator.checkIsNotNull(type, "type");
		return mEncapsulatedType.isAnnotationPresent(type);		
	}
	
	/**
	 * Helper method for argTypes conversion
	 * @param argTypes an array of string representations of Class names
	 * @param usePortabilityResolver If true then SpcfPortabilityResolver is 
	 * used to translate method name in order to make the method name 
	 * platform appropriate, otherwise users takes care of the method naming 
	 * differences in platforms.
	 * @return an array of Class types for the argTypes
	 */
	@SuppressWarnings("unchecked")
	private Class[] convertTypes(String[] argTypes, boolean usePortabilityResolver)
	{ 
		Class[] argTypesClass = null;
		if (argTypes == null)
		{
			argTypesClass = new Class[0];
		}
		else
		{
			try
			{
				String argName; 
				int numberArgs = argTypes.length;  
				argTypesClass = new Class[numberArgs];
				for (int i = 0; i < numberArgs; i++) 
				{ 
					if (usePortabilityResolver)
					{
						argName = SpcfPortabilityResolver.
						translateTypeFullName(argTypes[i]);
					}
					else
					{
						argName = argTypes[i];	
					}
					argTypesClass[i] = Class.forName(argName);
				}

			} 
			catch(ClassNotFoundException e)
			{
				throw new SpcfClassNotFoundException(e.getMessage());
	            // When testing using JUnit from command line with an invalid full class name, 
	            // NoClassDefFoundError is thrown, instead of ClassNotFoundException as 
	            // running maven or eclipse. 
	        } catch(java.lang.NoClassDefFoundError e) {
	           	throw new SpcfClassNotFoundException(e.getMessage());
	        }
		}  
		return argTypesClass; 
	}
	
	/**
	 * Handles all overloads for hasProperty
	 * 
	 * @param name name of the property to check 
	 * @param includeNonPublic if true, will check all public and non-public 
	 * properties, otherwise will check only public properties.
	 * @param declaredOnly if true, will check only properties declared by
	 * the encapsulated class.  
	 * @param onlyPrivateAncestor if true, will check only properties that 
	 * are in the class hierarchy but not declared by the encapsulated class
	 * and that have a get or set method with a private accessor.
	 * @return true if this type has a matching property, otherwise false.	 
	 */
	protected boolean hasProperty(String name, boolean includeNonPublic, 
			boolean declaredOnly, boolean onlyPrivateAncestor) 
	{ 
		try 
		{
			SpcfProperty p = getProperty(name, includeNonPublic, declaredOnly, onlyPrivateAncestor, false);
			return (p != null);
			
		} 
		catch (Exception e) 
		{
			return false;
		}
	} 
	
	/**
	 * Handles all overloads for getProperty
	 * 
	 * @param name name of the property to return 
	 * @param includeNonPublic if true, will check all public and non-public 
	 * properties, otherwise will check only public properties.
	 * @param declaredOnly if true, will check only properties declared by
	 * the encapsulated class.  
	 * @param onlyPrivateAncestor if true, will check only properties that 
	 * are in the class hierarchy but not declared by the encapsulated class
	 * and that have a get or set method with a private accessor.
	 * @param throwException if true, an exception will be thrown if property
	 * is not found, else return null.
	 * @return SpcfProperty the property that matches the criteria or null
	 * if not found.
	 */
	protected SpcfProperty getProperty(String name, boolean includeNonPublic, 
			boolean declaredOnly, boolean onlyPrivateAncestor, boolean throwException) 
	{
        // make sure we have an encapsulated type
		if (throwException)
		{
			SpcfParamValidator.checkIsNotNull(mEncapsulatedType, "encapsulated type");
			SpcfParamValidator.checkIsNotNull(name, "property name");
		}  
		else // just check to avoid exception
        {
            if (mEncapsulatedType == null)
                return null;

            if (name == null)
                return null;
        }
		
		SpcfProperty[] props = getProperties(includeNonPublic, declaredOnly, onlyPrivateAncestor);
		if (props != null ) 
		{ 
			for (int i = 0; i < props.length; i++) 
			{			
				if (props[i] != null)
				{
					if (props[i].getName() != null && props[i].getName().equals(name)) 
					{
						return props[i];
					}
				}
			}
		}
		
		if (throwException)
		{
			throw new SpcfClassNoSuchMethodException("No such property: " + name);
		}
		else // dont make hasProperty catch exception to return true/false
		{
			return null;
		}
	} 
	
	/**
	 * Handles all overloads for getProperties
	 *  
	 * @param includeNonPublic if true, will return all public and non-public 
	 * properties, otherwise will return only public properties.
	 * @param declaredOnly if true, will return only properties declared by
	 * the encapsulated class.  
	 * @param onlyPrivateAncestor if true, will return only properties that 
	 * are in the class hierarchy but not declared by the encapsulated class
	 * and that have a get or set method with a private accessor. 
	 * @return SpcfProperty[] the array of properties that matches the criteria 
	 */
	protected SpcfProperty[] getProperties(boolean includeNonPublic, boolean declaredOnly, 
			boolean onlyPrivateAncestor)
	{
		SpcfMethod[] methods;
		List<SpcfMethod> inheritedMethods = null;
		if (onlyPrivateAncestor)
		{
			methods = getMethods(includeNonPublic, declaredOnly);
			inheritedMethods = new ArrayList<SpcfMethod>();   
	        for (int i = 0; i <  methods.length; i++) 
	        {    
	        	inheritedMethods.add(methods[i]);     	
	        } 
			methods = getAncestorMethods(); 
		}
		else
		{
			methods = getMethods(includeNonPublic, declaredOnly);
		} 
		return extractProperties(methods, includeNonPublic, onlyPrivateAncestor, inheritedMethods);
	}

	
	/**
	 * Handles all overloads for getField. 
	 * @param name The name of the field.
	 * @param includeNonPublic if false, searching among only public fields;
	 * otherwise searching among all public and non-public fields.
	 * @param declaredOnly only get fields which are declared for the class
	 * @return The field for this class with the specified name.
	 */
	protected SpcfField getField(String name, boolean includeNonPublic,
			boolean declaredOnly) 
	{
		//make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, "encapsulated type");	
		// make sure name is not null
		SpcfParamValidator.checkIsNotNull(name, "field name");
		
		 SpcfField[] fields = this.getFields(includeNonPublic, declaredOnly);
		 if (fields != null ) 
		 { 
			 for (int i = 0; i < fields.length; i++) 
			 {			
				 if (fields[i] != null)
				 {
					 if( fields[i].getName() != null && fields[i].getName().equals(name)) 
					 {
						 return fields[i];
					 }
				 }
			 }
		 }
		 throw new SpcfClassNoSuchFieldException(name);  
	}
	
	/**
	 * Handles all overloads for getFields
	 * @param includeNonPublic if false, return only public fields;
	 * otherwise return all public and non-public fields.
	 * @param declaredOnly only get fields which are declared for the class
	 * @return The array of public fields for this class.
	 */
	protected SpcfField[] getFields(boolean includeNonPublic, boolean declaredOnly) 
	{
		// make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, "encapsulated type");

		Field[] fields;
		if (includeNonPublic == false && declaredOnly == false) 
		{
			fields = mEncapsulatedType.getFields();
		}
		else
		{
			fields = mEncapsulatedType.getDeclaredFields(); 
		}
		
		SpcfField[] portableFields; 
		if (fields == null) 
		{
			portableFields = new SpcfField[0];
		}
		else
		{
			portableFields = new SpcfField[fields.length];
			for (int i = 0; i < fields.length; i++) 
			{
				portableFields[i] = new SpcfFieldImpl(fields[i]);
			} 
		} 
		
		SpcfClass currentType = this.getSuperClass(); 
		
		//return what was found if no base class or not searching inherited
		if (currentType == null || !includeNonPublic )  
		{
			return portableFields;
		}  
		
		// add the declared fields to master list
		List<SpcfField> fieldList = new ArrayList<SpcfField>();        
        for (int i = 0; i <  portableFields.length; i++) 
        {        	
        	fieldList.add(portableFields[i]);        	
        }
        
        //get inherited fields from superclasses      
		while (currentType != null)
        { 	 
			SpcfFieldImpl fieldImpl;
			fields = currentType.getEncapsulatedType().getDeclaredFields();  
            for (int i = 0; i < fields.length; i++) 
            {
            	fieldImpl = new SpcfFieldImpl(fields[i]); 
            	if (fieldImpl.inheritedBy(this) && !fieldImpl.matchName(fieldList)) 
                {
                	fieldList.add(fieldImpl);
                } 		                
            }	
            currentType = currentType.getSuperClass();   
        }
		
		// return all that were found
		SpcfField[] fieldArray = new SpcfField[fieldList.size()];
		for (int i = 0; i < fieldList.size(); i++) 
		{
			fieldArray[i] = fieldList.get(i);
		}		
		return fieldArray;				
	}
	
	/**
	 * handles all overloads for getMethod. 
	 * @param name The name of the method to obtain.
	 * @param includeNonPublic  Searching among both public and non-public 
	 * methods if includeNonPublic is true, otherwise inlcuding only public 
	 * methods.
	 * @param parameterTypes The parameter type array. If the method to get does not
	 * have any parameter, pass in an empty array of Class, i.e, new Class[0].
	 * For a generic parameter GenericClassA&lt;T, U&gt; to get
	 * its type by using  GenericClassA.class in Java and 
	 * typeof(GenericClassA&lt;&gt;) in C#.  For a generic parameter 
	 * GenericClassA&lt;T, U&gt;, to get its type by using  GenericClassA.class 
	 * in Java and typeof(GenericClassA&lt;, &gt;) in C#.
	 * @param usePortabilityResolver If true then SpcfPortabilityResolver is 
	 * used to translate method name in order to make the method name 
	 * platform appropriate, otherwise users takes care of the method naming 
	 * differences in platforms.
	 * @param declaredOnly only get methods which are declared for the class
	 * @return A matching method. 
	 */
	@SuppressWarnings("unchecked")
	protected SpcfMethod getMethod( String name, 
			boolean includeNonPublic,
			boolean declaredOnly, Class[] parameterTypes, 
			boolean usePortabilityResolver)		
	{
		// make sure we have an encapsulated type, input name and parameterTypes 
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, "encapsulated type");	
		SpcfParamValidator.checkIsNotNull(name, "method name");
		
		SpcfParamValidator.checkIsNotNull(parameterTypes, "parameter types");
		for(Class c: parameterTypes) 
		{
			SpcfParamValidator.checkIsNotNull(c, "parameterTypes array element");
		}
		String useName = name;
		if (usePortabilityResolver)
		{
			useName = SpcfPortabilityResolver.translateMethodName(name);			
		}
		Method method = null;
		try
		{
			if (!includeNonPublic && !declaredOnly)
			{
				method = mEncapsulatedType.getMethod(useName, parameterTypes);			
			}
			else
			{
				method = mEncapsulatedType.getDeclaredMethod(useName, parameterTypes);
			}
		}
		catch(NoSuchMethodException e) 
		{
			// this is left intentially blank
		}
		catch (SecurityException e) 
		{
			throw new SpcfSecurityException(e);
		}
		
		SpcfMethodImpl methodImpl = null;
		if (method != null)
		{
			methodImpl = new SpcfMethodImpl(method); 
		}
		
		if (includeNonPublic && methodImpl == null)
		{
			SpcfClass currentType = this.getSuperClass();      
			while (currentType != null)
	        {
	            try
	            {
	                method = currentType.getEncapsulatedType().getDeclaredMethod
	                	(useName, parameterTypes);
	                 
	                methodImpl = new SpcfMethodImpl(method);
	                 
	                //get all declared methods by this class and inherited
	                if (currentType.equals(this) || methodImpl.inheritedBy(this)) 	                		
	                {
	                	break;
	                }   
	            }
	            catch(NoSuchMethodException e) 
	            {
	            	// this is left intentially blank
	            }
        		catch (SecurityException e) 
        		{
        			throw new SpcfSecurityException(e);
        		} 
        		methodImpl = null;
        		currentType = currentType.getSuperClass();
	        } 
		}
		
		if (methodImpl == null)
		{
			throw new SpcfClassNoSuchMethodException(useName);  
		}
		
		return methodImpl;   
	} 
	
	/**
	 * Returns an array of methods that match criteria
	 *  
	 * @param includeNonPublic if true, will return all public and non-public 
	 * methods, otherwise will return only public methods.
	 * @param declaredOnly if true, will return only methods declared by
	 * the class.    
	 * @return SpcfMethod[] array of methods that matches the criteria. 
	 */
	protected SpcfMethod[] getMethods(boolean includeNonPublic, boolean declaredOnly) 
	{ 
		//make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, "encapsulated type"); 
		
		SpcfClass currentType = this.getSuperClass(); 
		  
		Method[] nativeArray;
		if (includeNonPublic == false && declaredOnly == false) 
		{
			nativeArray = mEncapsulatedType.getMethods();
		}
		else
		{
			nativeArray = mEncapsulatedType.getDeclaredMethods(); 
		}
		
		SpcfMethod[] portableArray; 
		if (nativeArray == null) 
		{
			portableArray = new SpcfMethod[0];
		}
		else
		{
			portableArray = new SpcfMethod[nativeArray.length];
			for (int i = 0; i < nativeArray.length; i++) 
			{
				portableArray[i] = new SpcfMethodImpl(nativeArray[i]);
			} 
		}  
		
		//return what was found if no base class or not searching inherited
		if (currentType == null || !includeNonPublic )  
		{
			return portableArray; 
		}  
		  
		//add the declared methods to master list  
		List<SpcfMethod> masterList = new ArrayList<SpcfMethod>();   
        for (int i = 0; i <  portableArray.length; i++) 
        {        	
        	masterList.add(portableArray[i]);        	
        }
        
        //get inherited methods from super interfaces 
		//all inherited methods from interfaces are public, so just merge the 
		//return from getMethods() with the above methodList including declared
		//and inherited from superclasses. 
		masterList = mergeMethods(masterList, getMethods());   
        
        //get inherited methods from superclasses      
		while (currentType != null)
        { 	 
			SpcfMethodImpl methodImpl;
			nativeArray = currentType.getEncapsulatedType().getDeclaredMethods();  
            for (int i = 0; i < nativeArray.length; i++) 
            {
            	methodImpl = new SpcfMethodImpl(nativeArray[i]);  
            	
            	//check if already in the master list
            	if (!methodImpl.matchNameAndTypes(masterList))
               	//Class[] argTypes = methodImpl.toSpecific().getParameterTypes();
               	//String methodName = methodImpl.getName();
               	//if (searchMethod(methodName, argTypes, masterList) == null)               	     
            	{
            		//check whether inherited by this type
            		if (methodImpl.inheritedBy(this))
            		{
            			//if so, add to master list
            			masterList.add(methodImpl); 
            		} 
            	}  
            }	
            currentType = currentType.getSuperClass();   
        }  
		
		//return each method that was found
		SpcfMethod[] methodArray = new SpcfMethod[masterList.size()];
		for (int i = 0; i < masterList.size(); i++) 
		{
			methodArray[i] = masterList.get(i);
		}
		return methodArray;	 
	}
	
	/**
	 * Returns array of methods that are declared by ancestors.  
	 * @return array of methods that are declared by ancestors
	 */
	protected SpcfMethod[] getAncestorMethods() 
	{ 
		//make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, "encapsulated type"); 
		
		SpcfClass currentType = this.getSuperClass(); 
		 
		if (currentType == null)
		{
			// return empty array if no base class
			return new SpcfMethod[0];
		}
		 
		Method[] nativeArray;
		nativeArray = mEncapsulatedType.getDeclaredMethods(); 
		  
		SpcfMethod[] portableArray; 
		if (nativeArray == null) 
		{
			portableArray = new SpcfMethod[0];
		}
		else
		{
			portableArray = new SpcfMethod[nativeArray.length];
			for (int i = 0; i < nativeArray.length; i++) 
			{
				portableArray[i] = new SpcfMethodImpl(nativeArray[i]);  
			} 
		}  
		  
		//if onlyPrivateAncestor methods are requested,
		List<SpcfMethod> privateList = new ArrayList<SpcfMethod>();        
        
		//add the declared methods to master list  
		List<SpcfMethod> masterList = new ArrayList<SpcfMethod>();   
        for (int i = 0; i <  portableArray.length; i++) 
        {        	
        	masterList.add(portableArray[i]);        	
        }
        	  
        //get methods declared by superclasses      
		while (currentType != null)
        { 	 
			SpcfMethodImpl methodImpl;
			nativeArray = currentType.getEncapsulatedType().getDeclaredMethods();  
            for (int i = 0; i < nativeArray.length; i++) 
            {
            	methodImpl = new SpcfMethodImpl(nativeArray[i]);  
            	
            	//check if already in the master list
            	
            	if (!methodImpl.matchNameAndTypes(masterList))
              	//Class[] argTypes = methodImpl.toSpecific().getParameterTypes();
              	//String methodName = methodImpl.getName();
              	//if (searchMethod(methodName, argTypes, masterList) == null)               	     
              	{            	  
            		if (!methodImpl.matchNameAndTypes(privateList)) 
              		//if (searchMethod(methodName, argTypes, privateList) == null)
            		{   
            			privateList.add(methodImpl);
            		}
            	}  
            }	
            currentType = currentType.getSuperClass();   
        }    
		 
		//return each method that was found
		SpcfMethod[] methodArray = new SpcfMethod[privateList.size()];
		for (int i = 0; i < privateList.size(); i++) 
		{
			methodArray[i] = privateList.get(i);
		}
		return methodArray;			 
	} 
	 
	/**
	 * Handles all overloads for doInvokeMethod
	 * @param type the class or type that has the specified method.
	 * @param library the name of the library (not used in Java).
	 * @param fullClassName	the fully qualified class name.
	 * @param method	the name of the method.
	 * @param args	the arguments to pass to the specified method.
	 * @param argTypes	the arguments types to to find the specified method.
	 * @param checkTypes whether to check the argument types
	 * @param resolveClass use the portabilityResolver to get the correct class string
	 * @param usePortabilityResolver whether to use SpcfPortabilityResolver.
	 * @return returns the return object from the specified method.
	 */ 
	@SuppressWarnings("unchecked")
	protected Object doInvokeMethod(
			Class type, 
			String library, 
			String fullClassName, 
			String method, 
			Object[] args, 
			Class[] argTypes,
			boolean checkTypes,
			boolean resolveClass,
			boolean usePortabilityResolver)
	{  

		Class impl = type;
		if (resolveClass) // validate necessary arguments
		{  
			//make sure library is not null
			SpcfParamValidator.checkIsNotNull(library, "library name");

			//make sure fullClassName is not null
			SpcfParamValidator.checkIsNotNull(fullClassName, "full class name"); 

			if (fullClassName == "") 
			{
				throw new SpcfClassNotFoundException("full class name should not be an empty string");
			}
		}
		else
		{
			//make sure type is not null
			SpcfParamValidator.checkIsNotNull(type, "class"); 
		}

		if (checkTypes) // validate necessary arguments
		{ 
			SpcfParamValidator.checkIsNotNull(argTypes,  "argTypes"); 

			for(Class c: argTypes) 
			{
				SpcfParamValidator.checkIsNotNull(c, "argTypes array element");
			}
		}

		//make sure method name is not null
		SpcfParamValidator.checkIsNotNull(method,  "method name");

		try 
		{
			if (resolveClass)  
			{
				String newName = fullClassName;
				if (usePortabilityResolver)
				{
					newName = SpcfPortabilityResolver.
					translateTypeFullName(fullClassName); 
				} 
				impl = Class.forName(newName); 
			}  

			String newMethod = method; 
			if (usePortabilityResolver)
			{ 
				newMethod = SpcfPortabilityResolver.translateMethodName(method);
			}

			// resolve argTypes
			Class[] useArgTypes = null;
			if (checkTypes)
			{
				useArgTypes = argTypes;	
			}
			else if (args != null)
			{
				int numberArgs = args.length;
				useArgTypes = new Class[numberArgs];
				for (int i = 0; i < numberArgs; i++)
				{
				    Object arg = args[i];
	                //make sure each args[i] is not null
	                SpcfParamValidator.checkIsNotNull(
	                    arg, "cannot determine type of argument " + i + " because it is null");
					useArgTypes[i] = arg.getClass();
				}
			} 
			else 
			{
				useArgTypes = new Class[0];
			}

			SpcfClass spcfType = new SpcfClassImpl(impl);
			SpcfMethod initializeMethod = spcfType.getMethod(newMethod, true, useArgTypes, usePortabilityResolver);
			return initializeMethod.invoke(null, args);
		} 
		catch(ClassNotFoundException e)
		{
			throw new SpcfClassNotFoundException(e.getMessage());
		}
		catch(java.lang.NoClassDefFoundError e) 
		{
           	throw new SpcfClassNotFoundException(e.getMessage());
        }  
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#getMethodInHierarchy(String, Class[], boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SpcfMethod getMethodInHierarchy(String name, 
			Class[] parameterTypes, 
			boolean usePortabilityResolver) 
	{		
	    SpcfMethod method = null;
	    try
	    {
            method = getMethod(name, true,
                true, parameterTypes, usePortabilityResolver);
	    }
	    catch(SpcfClassNoSuchMethodException e) 
	    {
	    	// left intentially blank
	    }		    			    
		
        // not found from this class, go up the class hierarchy
	    if (method == null)
	    {
		    SpcfClass currentType = this.getSuperClass();   
		    //System.Console.Out.WriteLine("current type = " + currentType.FullName);
		    while (currentType != null)
            {
                try
                {
                    method = ((SpcfClassImpl)currentType).getMethod(name, true,
                        true, parameterTypes, usePortabilityResolver);                	    	                    
                    //found a matching method 	                    
                    return method;	               	                   
                }
                catch(SpcfClassNoSuchMethodException e) 
                {
                	// this is left intentially blank
                }        		            		    
    		    currentType = currentType.getSuperClass();
            } 
	    }
		
	    if (method == null)
	    {
		    throw new SpcfClassNoSuchMethodException(name);  
	    }    		
	    return method; 
	}
	
	/**
	 * Get all methods declared in the class hierarchy from this class or 
	 * interface till the root.
	 *
	protected SpcfMethod[] getMethodsInHierarchy() 
	{
		//get declared methods by this class or interface
		SpcfMethod[] thisClassMethods = getDeclaredMethods();
		List<SpcfMethod> thisClassMethodList = new ArrayList<SpcfMethod>();
		for (SpcfMethod m : thisClassMethods) 
		{
			thisClassMethodList.add(m);
		}
		
		//get methods declared by ancestor classes or interfaces
		SpcfMethod[] ancestorMethods = getAncestorMethods();
		List<SpcfMethod> allMethods = mergeMethods (thisClassMethodList, 
				ancestorMethods);
		SpcfMethod[] allMethodArray = new SpcfMethod[allMethods.size()];
		int i = 0;
		for (SpcfMethod m : allMethods)
		{
			allMethodArray[i] = m;
			i++;
		}
		return allMethodArray;
	}*/
	
    /**
     * Search a method with the specified name and parameter types among the 
     * specified method list.
     * @return  Returns the matching method if found, otherwise returns 
     * null. 
     *
    protected SpcfMethod searchMethod(String name, Class[] argTypes,
    		List<SpcfMethod> methods) {

		if (methods == null || methods.size() == 0) 
		{
			return null;
		}

		for (int i = 0; i < methods.size(); i++) 
		{
			SpcfMethod method = (SpcfMethod)methods.get(i);
			if (method.getName().equals(name)) 
			{
				boolean typesMatched = true;
				SpcfClass[] pTypes = method.getParameterTypes();
				if (argTypes == null || argTypes.length == 0) {
					if (pTypes == null || pTypes.length == 0) 
					{
						return method;
					} 
				} else if (argTypes != null && argTypes.length > 0)
				{
					if (pTypes != null && pTypes.length == argTypes.length) 
					{
					
						for (int j = 0; j < argTypes.length; j++) 
						{
							if (!argTypes[j].equals(pTypes[j])) 
							{
								typesMatched = false;
							}
						}
						if (typesMatched == true) return method;
					}
				}
			}
		} 
		return null;
    } */ 
	
	/**
	 * Get a portable string representation for a generic type by embedding its 
	 * given innerTypeStr into its given outerTypeStr as a part of its class 
	 * name. 
	 * @param innerTypeStr The portable string representation for the inner type  
	 * which may be a generic type or a non-generic type.
	 * 
	 * @return A string for the nested type portable representation.
	 * @throws SpcfInvalidTypeStringException if the given innerTypeStr is 
	 * invalid portable representation.
	 *
	public String nestPortableRepresentation(String innerTypeStr)
	{
        //make sure input is not null		
		SpcfParamValidator.checkIsNotNull(innerTypeStr, 
        								  "innerTypeStr");
		//check whether input is valid			 		
		//SpcfClass.isValidPortableRepresentation(innerTypeStr);
		
		String outerTypeStr = portableRepresentation();						
		int index2 = outerTypeStr.indexOf(", AssemblySimpleName=");
		String head = outerTypeStr.substring(0, index2);
		String tail = outerTypeStr.substring(index2);		
		String[] typeParamArray = getTypeParameters(); 
		String nestedStr = head + "'" + typeParamArray.length + "[[" + 
			innerTypeStr + "]]" + tail; 
		return nestedStr;
	}*/

	/**
     * Return the element type if this class is an array.
     * @return the element type of the array class. If the enclosed class is not 
     * an array, return null.
     */
	@Override
    public SpcfClass getElementType () 
    {
    	return sElementType;
    }	
	
    /**
     * Set the element type of an array type.
     * @param elementType the element type of the array class. 
     */
	@Override
    public void setElementType (SpcfClass elementType) 
    {
    	sElementType = elementType;
    } 
    
	/**
	 * Create portable representation for the encapsulated generic type.
	 * @param library the string describing the assembly including assembly simple name,
	 * culture, version and public key token, which contains the encapsulated 
	 * type on .net side.
	 * @return A string for the portable representation of the encapsulated 
	 * generic type.
	 * @throws SpcfIllegalArgumentException if the encapsulated type is not a
	 * generic type.
	 */
	protected String genericTypePortableRepresentation(String library)
	{
		if (!this.isGenericType()) 
		{
			throw new SpcfIllegalArgumentException("The encapsulated type " + 
					this.getFullName() + " is not a generic type");
		}
		SpcfClass[] typeParams = this.getSpcfTypeParameters();
		int dimension = typeParams.length;
		
		//get the portable representations for all type parameters and 
		//append them together in the original order
		String typeParamsStr = SpcfClass.getGenericTypeStartIndicator();
		for (int i = 0; i < typeParams.length; i++) 
		{
			SpcfClass typeParam = typeParams[i];
			String typeParamStr = typeParam.portableRepresentation();			
			typeParamsStr = typeParamsStr + 
				SpcfClass.getGenericTypeParamStartIndicator() + 
				typeParamStr + SpcfClass.getGenericTypeParamEndIndicator();					
			
			//add TypeParameterSeparatorIndicator
			if (i < typeParams.length -1) 
			{
				typeParamsStr = typeParamsStr + 
					SpcfClass.getTypeParameterSeparatorIndicator();
			}				
		}
		typeParamsStr = typeParamsStr + SpcfClass.getGenericTypeEndIndicator();
		String fullClassName = SpcfClass.getFullClassNameIndicator() + 
			this.getFullName() + SpcfClass.getGenericTypeIndicator() + 
			dimension + typeParamsStr;
		if (library.length() == 0) 
		{ 
			return fullClassName;
		} 
		else
		{ 
			return fullClassName + library;
		}
	}
	
	/**
	 * Create string representation for the encapsulated generic type without 
	 * the assembly information for the encapsulated type, but assembly 
	 * information for type parameters is included. 
	 * 
	 * @return A string representing the encapsulated generic type without 
	 * assembly information.
	 * @throws SpcfIllegalArgumentException if the encapsulated type is not a
	 * generic type.
	 */
	protected String genericTypePortableRepresentation()
	{
		String library = "";
		return genericTypePortableRepresentation(library);
	}
	
	/**
	 * Create portable representation for the encapsulated array type.
	 * @param library the string describing the assembly including assembly simple name,
	 * culture, version and public key token, which contains the encapsulated 
	 * type on .net side.
	 * @return A string for the portable representation of the encapsulated 
	 * array type.
	 * @throws SpcfIllegalArgumentException if the encapsulated type is not a
	 * array type.
	 */
	protected String arrayTypePortableRepresentation(String library)
	{
		if (library == null)
		{
			library = "";
		}
		if (!this.isArray()) 
		{
			throw new SpcfIllegalArgumentException("The encapsulated type " + 
					this.getFullName() + " is not an array type");
		}
		int rank = this.getArrayRank();
		String arraySymbol = ""; 
		for (int i = 0; i < rank; i++)
		{
			arraySymbol = arraySymbol + SpcfClass.getArrayTypeIndicator();
		}		
		
		SpcfClass elementType = this.getElementType();
		if (!elementType.isGenericType()) 
		{	
			String str = SpcfClass.getFullClassNameIndicator() + 
				elementType.getFullName() +	arraySymbol + library;			
			return str;
		} 
		else
		{
			String elementTypeStr = 
				((SpcfClassImpl)elementType).genericTypePortableRepresentation();
			return elementTypeStr + arraySymbol + library;		
		}
	}

	/**
	 * Create portable representation for common types (including arrays of the 
	 * common types) across java and C# .Net alloweded in SPCF Portable Language.   
	 * @return A string in the type portable representation formart for the 
	 * encapsulated type. If the encapsulated type is not a common type or a 
	 * common type array, return null.
	 * @throws SpcfArgumentNullException if the encapsulate type is null.
	 */
	@SuppressWarnings("unchecked")
	private String commonTypeAndArrayPortableRepresentation()
	{
		//make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
			                          "encapsulated type");
		Class c = getEncapsulatedType();
		if (!this.isArray())
		{
			if (isCommonType(c)) 			
			{				
				return SpcfClass.getFullClassNameIndicator() + c.getName();
			}  
		}
		else 
		{
			Class elemType = c.getComponentType();
			if (isCommonType(elemType)) 
			{
				String str = SpcfClass.getFullClassNameIndicator() + 
					elemType.getName();
				for (int i=0; i<this.getArrayRank(); i++)
				{
					str = str + SpcfClass.getArrayTypeIndicator();
				}
				return str;
			}			
		}	
		return null;
	}
	
	/**
	 * Get assembly information from the manifest file in the jar that contains
	 * the type. The assembly information includes assembly simple name, 
	 * version, culture and public ket token that are necessary for loading an 
	 * assembly for reconstructing a type from name. The assembly information 
	 * is obtained from the corresponding dll file and can be pushed into the 
	 * jar manefest file by using SPCF Jar Updater or by using "jar umf" 
	 * command manually. 
	 *  
	 * @return a string describing the assembly that contains the encapsulated 
	 * type on .Net Side.
	 * @throws SpcfArgumentNullException if the encapsulated class is null.
	 * @throws SpcfIOException if an I/O error has occurred, or the jar 
	 * file cannot be located, or the found jar manifest does not contain 
	 * all necessary assembly information: version, culture and public key 
	 * token. 
	 */
	private String getAssemblyInfo ()
	{
		String library = "";
		//Use locateJar() for positive testing before we get a testing
		//jar with assembly info used in both eclipse and maven
		//String jarFileName = locateJar();
		
		String jarFileName = locateSource();
		//System.out.println("jarFileName = " + jarFileName);
		try {
			JarFile jf = new JarFile(jarFileName);			
			java.util.jar.Manifest mf = jf.getManifest();  
			if (mf == null) 
			{
				throw new SpcfIOException("Cannot find jar manifest in " + 
						jarFileName);
			}
			java.util.jar.Attributes mainAttrs = mf.getMainAttributes();
			java.util.jar.Attributes.Name name = 
				new java.util.jar.Attributes.Name("Assembly");
			boolean hasAllAssemblyInfo = true;
			if (mainAttrs.containsKey(name)) 
			{
				String asName = mainAttrs.getValue("Assembly");
				library = SpcfClass.getSeparatorIndicator() + 
					SpcfClass.getAssemblySimpleNameIndicator() + asName;
				//System.out.println("assembly simple name = " + asName);
				name = new java.util.jar.Attributes.Name("Assembly-Version");
				if (mainAttrs.containsKey(name)) 
				{
					asName = mainAttrs.getValue("Assembly-Version");
					library = library + "," + asName;
					//System.out.println("assembly version = " + asName);
				} else 
				{
					hasAllAssemblyInfo = false;
				}
				name = new java.util.jar.Attributes.Name("Assembly-Culture");
				if (mainAttrs.containsKey(name)) 
				{
					asName = mainAttrs.getValue("Assembly-Culture");
					library = library + "," + asName;
					//System.out.println("assembly culture = " + asName);
				}else 
				{
					hasAllAssemblyInfo = false;
				}
				name = new java.util.jar.Attributes.Name("Assembly-PublicKeyToken");
				if (mainAttrs.containsKey(name)) 
				{
					asName = mainAttrs.getValue("Assembly-PublicKeyToken");
					library = library + "," + asName;
					//System.out.println("assembly key = " + asName);
				} else 
				{
					hasAllAssemblyInfo = false;
				}								
			} else 
			{
				hasAllAssemblyInfo = false;
			}	
			if (hasAllAssemblyInfo == false) 
			{
				throw new SpcfIOException("The found jar file " + jf.getName() + 
						" does not contain all required assembly information");
			}
		} catch (IOException e) 
		{
			throw new SpcfIOException(e);
		}
		return library;
	}
	/**
	 * Creates a platform neutral string representation for this type including 
	 * information of the assembly that contains this type, which can be used to 
	 * reconsitute the class in both Java and C# .Net by using 
	 * SpcfClass.getType(String canonicalString).
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#portableRepresentation()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public String portableRepresentation() 
	{
	    //make sure we have an encapsulated type
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType, 
				                          "encapsulated type");
		//handle common type and common type array, no assembly information is
		//needed for resconstuting them on .Net side
		String str = this.commonTypeAndArrayPortableRepresentation();
		if (str != null) 
		{
			return str;
		}
		
		//get assembly information
		String library = getAssemblyInfo();
		
		Class c = getEncapsulatedType();
		
		//for generic type
		if (this.isGenericType())
		{
			return this.genericTypePortableRepresentation(library);

		}
		//for array type
		else if (this.isArray()) 
		{
			return this.arrayTypePortableRepresentation(library);			 
		}
		//for regular type
		else 
		{			
			//if (!this.isArray() && !this.isGenericType()) 	
			StringBuilder builder = new StringBuilder();
			builder.append(SpcfClass.getFullClassNameIndicator());
			builder.append(c.getName());
			if (library == null || !library.startsWith(","))
			{
				builder.append(SpcfClass.getSeparatorIndicator());
			}
			builder.append(library);
			return builder.toString();
//			return SpcfClass.getFullClassNameIndicator() 
//			+ c.getName() 
//			+ library!=null&&library.startsWith(",") ? "": SpcfClass.getSeparatorIndicator() 
//			+ library;					
		
		}
	}
	
	/**
	 * Get the type represented by the given type portable string representation. 
	 * 
	 * @see com.intuit.spc.foundations.portability.reflect.SpcfClass#doGetSpcfType(String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected SpcfClass doGetSpcfType(String portableTypeStr) 
	{
		Class c = SpcfClass.getType(portableTypeStr);
		SpcfClass sc = SpcfClass.createInstance(c);
		
		if (isArray(portableTypeStr)) 
		{
			String elemTypeStr = getElementTypeStr(portableTypeStr);
			SpcfClass elemType = getSpcfType(elemTypeStr);
			sc.setElementType(elemType);
		}
		else if (isGenericType(portableTypeStr)) 
		{			
			String[] typeParamStrs= getTypeParameterStrs(portableTypeStr);			
			
			SpcfClass[] typeParams = new SpcfClassImpl[typeParamStrs.length];
			for (int i = 0; i < typeParamStrs.length; i++) 
			{
				typeParams[i] = getSpcfType(typeParamStrs[i]);
			}
			sc.setSpcfTypeParameters(typeParams);			
		}		
		return sc;
	}
	
	/**
	 * Return the portable string representations of type parameters of the type 
	 * represented by the input portable representation.
	 * 
	 * @param portableTypeStr A string representing a type in portable 
	 * representation format.
	 * @return an array of string representations for type parameters of the type 
	 * represented by the input portable representation. Return null if the type 
	 * represented by the input portable representation is not a generic type.
	 * 	 
	 */
	private static String[] getTypeParameterStrs(String portableTypeStr)
	{	
		if(portableTypeStr == null || !isGenericType(portableTypeStr)) 
		{			
			return null;
		}
		
		//remove top type name 
		int index = portableTypeStr.indexOf(SpcfClass.getGenericTypeIndicator());
		String str = portableTypeStr.substring(index);
		
		int dim = str.charAt(1) - '0';
		String typeParamStrs[] = new String[dim];
		//remove top generic type indicator, dimension, and its start and end 
		//indicators
		int offset = SpcfClass.getGenericTypeEndIndicator().length() + 
			SpcfClass.getGenericTypeDimIndicatorLength() + 
			SpcfClass.getGenericTypeStartIndicator().length();		
		
		//remove top generic type's portable representation generic type end 
		//indicator, separator indicator and top type assembly info		
		int assemIndex = str.lastIndexOf(SpcfClass.getGenericTypeEndIndicator() + 
				SpcfClass.getSeparatorIndicator() + 
				SpcfClass.getAssemblySimpleNameIndicator());
		//for simple type string without assembly info, or array element type 
		//string whose assembly info got removed already, that just remove top 
		//generic type's generic type end indicator
		String typeParamsStr = str.substring(offset, str.length()-SpcfClass.
				getGenericTypeEndIndicator().length());
		if (assemIndex > 0 && !str.endsWith(getGenericTypeEndIndicator()))
		{
			typeParamsStr= str.substring(offset, assemIndex);		
		}		
		
		//extract type parameter string one by one	
		int i = 0;
		while(typeParamsStr.length() > 0)
		{								
			//look for matching generic type paramter start and end indicators
			int index1 = getFirstTypeParamEnd(typeParamsStr);
			if (index1 > 0) 
			{
				String indRemoved = typeParamsStr.substring(SpcfClass.
						getGenericTypeParamStartIndicator().length(), index1);
				typeParamStrs[i] = indRemoved;				
				int indexOffset = 1;
				int nextParamStartIndex = index1 + indexOffset +
						SpcfClass.getTypeParameterSeparatorIndicator().length();
				if (nextParamStartIndex < typeParamsStr.length())
				{	
					String leftTypeParamsStr = 
						typeParamsStr.substring(nextParamStartIndex);				
					typeParamsStr = leftTypeParamsStr;
				} 
				else 
				{
					break;
				}
			}
			i++;
		}
		return typeParamStrs;
	}
	
	/**
	 * Find the first matching type parameter end indicator from the given type
	 * parameters string and return its position index in the given string.
	 * @param typeParamsStr a string describing a list of type parameters in 
	 * portable representaion format. 
	 * @return the first matching type parameter end indicator index in the 
	 * given string.
	 */
	private static int getFirstTypeParamEnd(String typeParamsStr)
	{ 
		int index1 = 
			typeParamsStr.indexOf(SpcfClass.getGenericTypeParamEndIndicator());
		int index2 = 
			typeParamsStr.indexOf(SpcfClass.getGenericTypeParamStartIndicator(),
					SpcfClass.getGenericTypeParamStartIndicator().length());
			
		//if there is no generic type start inidcator inside,
		//then the matching generic type end indicator is found
		while (index1 > 0)
		{
			if (!(index2 > 0) || index2 > index1) 		
			{
				return index1;			
			} else // look further 
			{
				index1 = typeParamsStr.indexOf(
						SpcfClass.getGenericTypeParamEndIndicator(), index1+1);				
				index2 = typeParamsStr.indexOf(
						SpcfClass.getGenericTypeParamStartIndicator(), index2+1);				
			}			
		}
		return -1;
	}
	
	/**
	 * Determine the type represented by the input portable representation is an
	 * generic type or not.
	 * 
	 * @param portableTypeStr A string representing a type in portable 
	 * representation format.
	 * @return true if the represented type is genric type, otherwise false.
	 * 	 
	 */
	private static boolean isGenericType(String portableTypeStr)
	{	
		if(portableTypeStr == null) 
		{
			return false;
		}
		return portableTypeStr.contains(SpcfClass.getGenericTypeIndicator());
	}
	
	/**
	 * Determine the type represented by the input portable representation is an
	 * array type or not.
	 * 
	 * @param portableTypeStr A string representing a type in portable 
	 * representation format.
	 * @return true if the represented type is array type, otherwise false.
	 * 	 
	 */
	private static boolean isArray(String portableTypeStr)
	{
		if(portableTypeStr == null) 
		{
			return false;
		}
		
		String head = portableTypeStr;
		int index = portableTypeStr.lastIndexOf(SpcfClass.getSeparatorIndicator() 
				+ SpcfClass.getAssemblySimpleNameIndicator());
		if (index > 0) 
		{
			head = portableTypeStr.substring(0, index);
		}
		if (head.endsWith(SpcfClass.getArrayTypeIndicator())) 
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Extract and return the string representation for the element type of the 
	 * type represented by the input type representation.
	 * 
	 * @param portableTypeStr a string representing a type in portable 
	 * representation format.
	 * @return the string representation for the element type of the 
	 * type represented by the input type representation. Return null if the 
	 * input portableTypeStr does not represent an array type.
	 * @throws SpcfArgumentNullException if input portableTypeStr is null.	 
	 */
	private static String getElementTypeStr(String portableTypeStr) 
	{
	    //make sure the input is not null
		SpcfParamValidator.checkIsNotNull(portableTypeStr, "portableTypeStr");

		if (isArray(portableTypeStr)) 
		{	
			String head = portableTypeStr;
			int index = portableTypeStr.lastIndexOf(SpcfClass.getSeparatorIndicator() 
					+ SpcfClass.getAssemblySimpleNameIndicator());
			if (index > 0) 
			{
				head = portableTypeStr.substring(0, index);
			}
			String arrayInd = SpcfClass.getArrayTypeIndicator();
			while (head.endsWith(arrayInd)) 
			{
				head = head.substring(0, head.length() - arrayInd.length());
			}
			return head;			 
		}
		return null;
	}
	
	/**
	 * Get primitive java type from name.
	 *  @param name class name.
	 *  @return java primitive type as the name specified. Return null if the
	 *  input type name is not a java primitive type name allowed in SPCF 
	 *  portable language.
	 */
	@SuppressWarnings("unchecked")
	private static Class getPrimitiveType(String name)
	{
		if (name.equals("int"))
		{
			return int.class;
		}
		else if (name.equals("short")) 
		{
			return short.class;
		}
		else if (name.equals("long")) 
		{
			return long.class;
		}
		else if (name.equals("boolean")) 
		{
			return boolean.class;
		}
		else if (name.equals("double")) 
		{
			return double.class;
		}
		else if (name.equals("float")) 
		{
			return float.class;
		}
		else if (name.equals("char")) 
		{
			return char.class;
		}
		else if (name.equals("byte")) 
		{
			return byte.class;
		} 
		else 
		{
			return null;
		}
	}
	
	/**
	 * Create an instance of the encapsulated java primitive type or 
	 * the corresponding simple class.
	 * @return an instance of the encapsulated java primitive types or 
	 * the java primitive types' corresponding classes. Return null if the 
	 * encapsulated class is not a primitive or a primitive type's 
	 * corresponding class.
	 * @throws SpcfArgumentNullException if the encapsulated type is null.
	 */
	private Object createInstanceOfEncapsulatedPrimitiveType() 
	{
		SpcfParamValidator.checkIsNotNull(mEncapsulatedType,
		  								"encapsulated type");
		if (mEncapsulatedType.equals(int.class))
		{
			return (int)0;
		}
		else if (mEncapsulatedType.equals(Integer.class)) 		 
		{
			Integer integer = 0;
			return integer;
		}
		else if (mEncapsulatedType.equals(short.class)) 		 
		{
			return (short)0;
		}
		else if (mEncapsulatedType.equals(Short.class)) 		 
		{
			return new Short((short)0);
		}
		else if (mEncapsulatedType.equals(long.class)) 		 
		{
			return (long)0;
		}
		else if (mEncapsulatedType.equals(Long.class)) 		 
		{
			Long x = new Long(0);			
			return x;
		}
		else if (mEncapsulatedType.equals(float.class)) 		 
		{
			return (float)0.0;
		}
		else if (mEncapsulatedType.equals(Float.class)) 		 
		{
			Float x = new Float(0.0);			
			return x;
		}
		else if (mEncapsulatedType.equals(Double.class)) 		 
		{
			Double x = new Double(0.0);			
			return x;
		}
		else if (mEncapsulatedType.equals(double.class)) 		 
		{
			return (double)0.0;
		}
		else if (mEncapsulatedType.equals(byte.class)) 		 
		{
			return (byte)0;
		}
		else if (mEncapsulatedType.equals(Byte.class)) 		 
		{
			return new Byte((byte)0);
		}
		else if (mEncapsulatedType.equals(boolean.class)) 		 
		{
			return false;
		}
		else if (mEncapsulatedType.equals(Boolean.class)) 		 
		{
			Boolean x = new Boolean(false);
			return x;
		}
		else if (mEncapsulatedType.equals(char.class)) 		 
		{
			return '0';
		}
		else if (mEncapsulatedType.equals(Character.class)) 		 
		{
			Character x = new Character('0');
			return x;
		}
		else  		 
		{			
			return null;
		}
	}
}
