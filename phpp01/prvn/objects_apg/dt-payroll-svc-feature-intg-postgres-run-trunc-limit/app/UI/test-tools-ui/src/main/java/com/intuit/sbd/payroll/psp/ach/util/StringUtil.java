/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author shivanandad069
 */
public class StringUtil {
    
     /**
     * Determine if the string contains only whitespace.
     */
    public static boolean isWhitespace(String buf) {
        for (int i = 0; i < buf.length(); i++) {
            if (!Character.isWhitespace(buf.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
      /**
     * Converts a Throwable to a printable stack trace
     *
     * @param t any Throwable, or null
	 *
     */
    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);

        if (t != null) {
            t.printStackTrace(w);
        }

        w.flush();	// couldn't hurt...
        return sw.toString();
    }
    
}
