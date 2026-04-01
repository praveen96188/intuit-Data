/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach.util;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 *
 * @author shivanandad069
 */
public class Helper {
    
    public static boolean isEmpty (Object o)
	{
		return o == null ? true : (String.valueOf(o) == null ? true : (String.valueOf(o).length() == 0 ? true : false));
	}
    
    /**
	 * Create a new instance of the class specified by the className
	 * parameter.  The class must supply a default no argument public
	 * constructor.
	 */
	public static final Object createInstance (String className) {
		Object	result = null;
		try {
			result = Class.forName (className).newInstance ();
		} catch (Exception ex) {
			//AppMgr.getLogger().error("Unable to create new instance of the class (" + className + "): ", ex);
		}
		return result;
	}
        
        /**
	 * Invoke a single argument method on the target object using reflection.
	 */
	public static final Object invoke (Object target, String methodName, Object[] args)
							throws Exception
	{
		Method m = getMethod (target.getClass(), methodName, args);
		try {
		    return m.invoke (target, args);
		} 
		catch (IllegalAccessException ex) 
		{
			/* There is a known bug in the JDK where reflection does not work on some objects 
			 * because the object's interface does not return the most accessible method; here's 
			 * a workaround so we don't unintentionally break forms 
			 */
			return invokeInnerMethodOrInterface(m, target, methodName, args);
		}
	}
        
        public static final Method getMethod (Class clazz, String name, Object[] args)
								throws NoSuchMethodException
	{
		// Do short cut for methods with no arguments
		if (args == null)
		{
			return clazz.getMethod(name, (Class []) null);
		}
			
		ArrayList filter = new ArrayList();
		Method[] methods = clazz.getMethods();

		// Filter out methods that don't match name
		for (int i = 0; i < methods.length; i++)
		{
			Method m = methods[i];
			if (m.getName().equals(name)) {
				filter.add(m);
			}
		}
		
		// Filter out methods that don't match the parameters
		for (Iterator it = filter.iterator(); it.hasNext(); )
		{
			Method m = (Method)it.next();
			Class[] params = m.getParameterTypes();
			
			// Check for param count
			if (params.length != args.length) {
				it.remove();
			} else
			{
				// Check for param type match
				for (int i = 0; i < args.length; i++)
				{
					boolean match = false;
					Object arg = args[i];
					if (arg == null) {
						continue;
					}
	
					Class c = arg.getClass();
					Class p = params[i];

					// Check for class hierarchy on argument
					do
					{
						if (p.isAssignableFrom(c)) {
							match = true;
						}
					} while ((c = c.getSuperclass()) != null && !match);
					
					if (!match && !checkPrimitive(args[i].getClass()).equals(p))
					{
						it.remove();
						break;
					}
				}
			}
		}

		if (filter.size() == 0)
		{
			String list = "";
			for (int i = 0; i < args.length; i++)
			{
				Object arg = args[i];
				if (i > 0) {
					list += ", ";
				}
				list += (arg == null) ? "null" : arg.getClass().getName();
			}
			throw new NoSuchMethodException("Cannot find " + name + "(" + list + ") in " + clazz);  //$NON-NLS-L$
		}
		return (Method)filter.get(0);
	}
        
        /**
	 * Invokes a inner method or interface.  Used when regular invoke fails.
	 */
	public static final Object invokeInnerMethodOrInterface (Method m, Object target, String methodName, Object...args)
		throws InvocationTargetException, NoSuchMethodException, IllegalAccessException
	{
		/* There is a known bug in the JDK where reflection does not work on some objects 
		 * because the object's interface does not return the most accessible method; here's 
		 * a workaround so we don't unintentionally break forms 
		 */
	    Class currentClass = target.getClass();
	    while(currentClass != null) 
	    {
            Method innerMethod = currentClass.getMethod(m.getName(), m.getParameterTypes());

            if (!m.equals(innerMethod))
                return innerMethod.invoke(target, args);

            // try invoking the method for this class's interfaces
        	Class intfaces[] = currentClass.getInterfaces();
        	if (intfaces.length > 0) 
        	{
        	    Method interfaceMethod = intfaces[0].getMethod(m.getName(), m.getParameterTypes());
        	    return interfaceMethod.invoke(target, args);
        	}

        	// we haven't found an accessible method yet, so now try any super classes
        	currentClass = currentClass.getSuperclass();
	    }
	    
	    return null;
	}
	
        /**
	 * Check if the class representing the passed argument is assignable to
	 * primitive types.  If it is true, than the primitive class type is
	 * returned, else the original class is returned.
	 */
	public static Class checkPrimitive (Class clazz) {
		Class[][]	primitives = { {Integer.class, Integer.TYPE},
									{Boolean.class, Boolean.TYPE},
									{Byte.class, Byte.TYPE},
									{Short.class, Short.TYPE},
									{Character.class, Character.TYPE},
									{Long.class, Long.TYPE},
									{Float.class, Float.TYPE},
									{Double.class, Double.TYPE} };

		for (int i = 0; i < primitives.length; i++) {
			if (clazz.isAssignableFrom (primitives [i][0])) {
				return primitives [i][1];
			}
		}
		
		return clazz;
	}
	
	
	
/****************************************************************************
The following methods are copies of methods in AppHelper (ops), but since it doesn't inherit from
the public Helper class, I'm copying them here - both places would need to be changed if changes
are made.  (ykb)
*****************************************************************************/ 

	public static final Class getClass (String className) {
		Class	res = null;
		try {
			res = Class.forName (className);
		} catch (Exception ex) {
			System.out.println ("Class " + className + " not found");
		}
		return res;
	}
	
        public static final Object getConstant (String constantPath) {
		Object	res = null;
		int		i;
		/** Split the path into class path and field name */
		if ((i = constantPath.lastIndexOf ('.')) != -1) {
			try {
				Class clazz = Class.forName (constantPath.substring (0, i));
				return clazz.getField (constantPath.substring (i+1)).get (null);
			} catch (Exception ex) {
			}
		}

		if (res == null) {
			System.out.println ("Constant " + constantPath + " not found");
		}
		return res;
	}
        
          /**
	 * Breaks a string and parse into a rectangle representing coordinates. The
	 * string should be in the form of {left, top, width, height}
	 * @return 	A rectangle containing the encoded coordinates
	 */
	public static Rectangle parseRectangle (String toParse, String separator) {
		Rectangle		res = new Rectangle ();
		if (toParse == null) {
			return res;
		}

		StringTokenizer	tokenizer = new StringTokenizer (toParse, separator);
		int 				c [] = new int [4];

		for (int i = 0; i < 4 && tokenizer.hasMoreTokens (); i++) {
			c[i] = Integer.parseInt (tokenizer.nextToken ());
		}
			
		res.setBounds (c[0], c[1], c[2], c[3]);
		return res;
	}
        
        /**
	 * Breaks a string and parse into a Dimension representing width & height.
	 * @return 	A Dimension object
	 */
	public static Dimension parseDimension (String toParse, String separator) {
		if (toParse == null || separator == null) {
			return null;
		}

		StringTokenizer	tokenizer = new StringTokenizer (toParse, separator);
		if (tokenizer.countTokens () != 2) {
			return null;
		}
		
		Dimension	res = new Dimension ();
		res.width = Integer.parseInt (tokenizer.nextToken ());
		res.height = Integer.parseInt (tokenizer.nextToken ());
		return res;
	}
	
        public static boolean isAchFtpEnabled(){
            return true;
        }
        
        
        public static String getAchProcessor(){
            return "chase";
        }
        
}
