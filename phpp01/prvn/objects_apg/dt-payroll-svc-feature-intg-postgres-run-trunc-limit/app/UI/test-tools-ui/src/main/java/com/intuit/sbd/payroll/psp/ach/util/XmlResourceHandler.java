/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach.util;

/**
 *
 * @author shivanandad069
 */
/**
* XmlResourceHandler.java
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


import java.util.Vector;

import org.xml.sax.*;

/**
 * This class implements a resource handler for XML resource files.
 */
public class XmlResourceHandler extends ResourceHandler implements XmlConstants
{
	protected Vector m_listeners;

	/** Convenience methods */
	protected final Object createInstance (String className) {
		return Helper.createInstance (className);
	}
	
	protected final Object invokeSetMethod (Object target, String name, Object[] args)
	{
		Object result = null;
		try {
			result = Helper.invoke (target, "set" + name, args);  //$NON-NLS-L$ 
		}
		catch (Exception ex) {
			System.out.println ("Unable to invoke set" + name);
		}
		return result;
	}

	protected final Object invokeAddMethod (Object target, String name, Object[] args)
	{
		Object result = null;
		try {
			result = Helper.invoke (target, "add" + name, args);  //$NON-NLS-L$ 
		}
		catch (Exception ex) {
			System.out.println ("Unable to invoke add" + name);
		}
		return result;
	}

	/**
	 * Build an argument object to be used when setting a property value.
	 */
	protected final Object createArg (Attributes atts)
	{
		String	value = atts.getValue (XML_VALUE);
		String	className = atts.getValue (XML_CLASS);
		String	constantPath = atts.getValue (XML_CONSTANT);
		String	idref = atts.getValue (XML_IDREF);
		Object	arg = value;
	
		if (className != null)
			arg = Helper.getClass (className);
		else if (constantPath != null)
			arg = Helper.getConstant (constantPath);
		else if (idref != null) {
			arg = retrieve (idref);
		} else if (value != null) {
			if (value.startsWith ("####"))
				arg = Helper.parseRectangle (value.substring (4), ", ");
			else if (value.startsWith ("##"))
				arg = Helper.parseDimension (value.substring (2), ", ");
			else if (value.startsWith ("#"))
				arg = new Integer (value.substring (1));
		}
		return arg;
	}

	protected Object retrieve (String id) {
		Object res = null;
		
		// Query all listeners if any can supply the specified resource
		for (int i = 0; i < m_listeners.size(); i++) {
			res = ((ResourceListener)m_listeners.get (i)).itemRequested (id);
			if (res != null)
				break;
		}
		
		return res;
	}

	/** Implement ResourceHandler */
	public void addResourceListener (ResourceListener l) {
		if (m_listeners == null)
			m_listeners = new Vector ();
			
		m_listeners.add (l);
	}
	
	public void removeResourceListener (ResourceListener l) {
		if (m_listeners != null && m_listeners.contains (l)) {
			m_listeners.remove (l);
		}
	}

	public void startElement (String namespaceURI,
                         String localName,
                         String qName,
						 Attributes atts) throws SAXException
	{
		if (qName.equals (XML_RESOURCE)) {
			String handlerClass = atts.getValue (XML_HANDLER);
			ResourceHandler handler = (ResourceHandler)createInstance (handlerClass);
			if (handler != null)
			{
				// Notify all listeners of the start resource event
				for (int i = 0; i < m_listeners.size (); i++)
					((ResourceListener)m_listeners.get (i)).handlerChanged (handler);
			}
		}
	}
        
      

}

