/**
* Field.java
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

import java.awt.Rectangle;
import java.awt.Dimension;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.servlet.jsp.JspWriter;

import com.intuit.sbd.payroll.psp.ach.util.Identity;

/**
 *
 * @author  Kin-Hong Wong
 * @version initial
 */
public abstract class Field extends Identity implements FieldType
{
	// HTML forms
	protected int m_inputType = INPUT_NONE;
	protected int m_size = 10;
	protected String m_maxLength;
	protected Dimension	m_grid;
	protected Rectangle	m_bounds;
  
	protected Class m_dataType = String.class;
	protected TypeMap m_typeMap;
	
	static DateFormat	m_dateFormat = new SimpleDateFormat ("MM/dd/yy");
	
	public void setInputType (int type) {
		m_inputType = type;
	}
	
	public int getInputType () {
		return m_inputType;
	}
	
	public void setSize (String size) {
		try {
			m_size = Integer.parseInt (size);
		} catch (NumberFormatException ex) {
			System.err.println ("Cannot convert " + size + " to integer");
		}
	}
	
	public int getSize () {
		return m_size;
	}
	
	public void setMaxLength (String maxLength) {
		m_maxLength = maxLength;
	}
	
	public String getMaxLength () {
		return m_maxLength;
	}
	
	public void setGrid (Dimension grid) {
		m_grid = grid;
	}
	
	public Dimension getGrid () {
		return m_grid;
	}

	public void setBounds (Rectangle bounds) {
		m_bounds = bounds;
	}
	
	public Rectangle getBounds () {
		return m_bounds;
	}
	
	public void setDataType (Class clazz) {
		m_dataType = clazz;
	}
	
	public Class getDataType () {
		return m_dataType;
	}

	public void setTypeMap (TypeMap map) {
		m_typeMap = map;
	}
	
	public TypeMap getTypeMap () {
		return m_typeMap;
	}

	/**
	 * Emphasis is on minimizing string concatenation
	 */
	public void getHtmlInput (JspWriter out, Object target)
								throws java.io.IOException
	{
		switch (m_inputType)
		{
			case INPUT_TEXT:
				JspUtil.createTextField (out, m_name, m_size, m_size, getString (target));
				break;
			
			case INPUT_SELECT:
				if (m_typeMap != null)
					JspUtil.createSelectList (out, m_name, target, m_typeMap);
				break;

			case INPUT_CHECKBOX:
				boolean checked = false;
				String strValue = getString (target);
				if (strValue != null && Boolean.valueOf (strValue) == Boolean.TRUE)
					checked = true;

				JspUtil.createCheckBox (out, m_name, checked);
				break;
				
			case INPUT_FILE:
				JspUtil.createFileUpload (out, m_size, m_name);
				break;
				
			case INPUT_HIDDEN:
				JspUtil.createHiddenField (out, m_name, getString (target));
				break;
		}
	}

	/**
	 * Returns the string value of the passed field id.  Uses the field
	 * binding to determine what field(s) to get from the record.
	 */
	 public String getString (Object value) {
	 	return value == null ? null : valueToString (value);
	 }

	/**
	 * Returns the long value of the passed field id.  Uses the field
	 * binding to determine what field(s) to get from the record.
	 */
	 public long getLong (Object value) {
	 	if (value != null) {
	 		if (value instanceof Long)
	 			return ((Long)value).longValue ();
	 		else if (value instanceof String)
	 			return Long.parseLong ((String)value);
	 	}
	 	return 0;
	 }

	/** Conversion */
	protected Object stringToValue (String str) {
		Object	res = str;
		if (str == null)
			return null;

		try {
			if (m_dataType == BigDecimal.class)
				res = new BigDecimal (str);
			else if (m_dataType == java.util.Date.class)
				res = m_dateFormat.parse (str);
			else if (m_dataType == Long.class)
				res = new Long (str);
			else if (m_dataType == Integer.class)
				res = new Integer (str);
		} catch (Exception e) {
			if (str.length () != 0)
				System.out.println ("Cannot convert " + str + "to value: " + e);

			res = null;
		}
		return res;
	}

	protected String valueToString (Object value) {
		if (value == null)
			return null;

		try {
			if (m_dataType == java.util.Date.class)
				return m_dateFormat.format ((java.util.Date)value);
			else if (m_dataType == Long.class) {
				if (m_typeMap != null)
					return m_typeMap.getString (value);
			}
		} catch (Exception e) {
			System.out.println ("Cannot convert " + value + " to string: " + e);
		}
		return value.toString ();
	}
	
}
