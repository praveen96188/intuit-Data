/**
* JspUtil.java
*
* Copyright (c) 1999-2000 PayCycle, Inc. All Rights Reserved.
* 
* This software is the confidential and proprietary information of
* PayCycle, Inc. ("Confidential Information").  You shall not
* disclose such Confidential Information and shall use it only in
* accordance with the terms of the license agreement you entered into
* with PayCycle.
* 
* PAYCYCLE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
* SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
* IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
* PURPOSE, OR NON-INFRINGEMENT. PAYCYCLE SHALL NOT BE LIABLE FOR ANY DAMAGES
* SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
* THIS SOFTWARE OR ITS DERIVATIVES.
* 
* CopyrightVersion 1.0
*/

package com.intuit.sbd.payroll.psp.ach.gui;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.jsp.*;



/**
 * @author  Kin-Hong Wong
 * @version initial
 */
public class JspUtil
{	
	/**
	 * Creates the HTML <select> list based on a TypeMap and a selection.
	 * Emphasis is to minimize string concatenation.
	 */
	public static final void createSelectList (JspWriter out, String name,
					Object selection, TypeMap map) throws IOException
	{
		Enumeration keys = map.keys ();
		
		out.print ("<select name='");
		out.print (name);
		out.print ("'>");
		
		while (keys.hasMoreElements ()) {
			Object	key = keys.nextElement ();
			out.print ("<option value='");
			out.print (key);
			out.print ("'");
			
			if (key.equals (selection))
				out.print (" selected");

			out.print (">");
			out.print (map.getString (key));
			out.print ("</option>");
		}
		out.print ("</select>");
	}

	public static final void createTextField (JspWriter out, String name,
					int size, int maxLen, String value) throws IOException
	{
		out.print ("<input name='");
		out.print (name);
		out.print ("' type='text' size='");
		out.print (size);
		out.print ("' maxLength ='");
		out.print (maxLen);
		
		if (value != null) {
			out.print ("' value='");
			out.print (value);
		}
		
		out.print ("'>");
	}

	public static final void createHiddenField (JspWriter out, String name,
					String value) throws IOException
	{
		out.print ("<input name='");
		out.print (name);
		out.print ("' type='hidden");
		
		if (value != null) {
			out.print ("' value='");
			out.print (value);
		}
		
		out.print ("'>");
	}

	public static final void createCheckBox (JspWriter out, String name,
					boolean checked) throws IOException
	{
		out.print ("<input name='");
		out.print (name);
		out.print ("' type='checkbox' ");

		if (checked)
			out.print (" checked");
			
		out.print (" value='");
		out.print (checked ? "true" : "false");
		out.print ("'>");
	}

	public static final void createFileUpload (JspWriter out, int size, String name)
					throws IOException
	{
		out.print ("<input name='");
		out.print (name);
		out.print ("' type='file' size='");
		out.print (size);
		out.print ("'>");
	}

	
	
	public static String getValue (ServletRequest req, String name)
	{
		String value = req.getParameter (name);
		return (value == null) ? "" : value;
	}
}
