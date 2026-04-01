/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach.util;

/**
 *
 * @author shivanandad069
 */
public class AppHelper {
    
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
	 * Determines if the passed string contains all digits
	 */
	public static boolean isDigit (String s) 
	{
		for (int i = 0; i < s.length (); i++) {
			if (! Character.isDigit (s.charAt (i)))
				return false;
		}
		return true;
	}
}
