/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach;

/**
 *
 * @author shivanandad069
 */
/**
* RoutingNumberEx.java
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


import com.intuit.sbd.payroll.psp.ach.fixedlen.RecordManagerException;
import com.intuit.sbd.payroll.psp.ach.util.AppHelper;

/**
* The "Extended" Routing Number of the ACH Operator or sending point that is
* sending the file, in the bTTTTAAAAC format.
*/
public class RoutingNumberEx extends RoutingNumber
{
	public String getRouting ()	{
		return m_value.substring (1, 5);
	}
	
	public void setRouting (String r) throws RecordManagerException, AchException
	{
		if (! AppHelper.isDigit (r) || r.length () != 4)
			throw new AchException ("Federal Reserve Routing Symbol must be a 4 digit number"); 

		// Validate before storing
		StringBuffer buf = new StringBuffer (getFormattedValue ());
		buf.replace (1, 5, r);
		setValue (buf.toString ());
	}
	
	public String getABAId ()	{
		return m_value.substring (5, 9);
	}
	
	public void setABAId (String id) throws RecordManagerException, AchException
	{
		if (! AppHelper.isDigit (id) || id.length () != 4)
			throw new AchException ("ABA Institution Identifier must be a 4 digit number"); 

		// Validate before storing
		StringBuffer buf = new StringBuffer (getFormattedValue ());
		buf.replace (5, 9, id);
		setValue (buf.toString ());
	}
	
	public char getCheckDigit () {
		return m_value.charAt (9);
	}
	
	/**
	 * Override default implementation.
	 * If the first character is not blank, then prepand a blank char.
	 */
	public void setValue (String val) throws RecordManagerException
	{
		if (val.charAt (0) != ' ' && val.length () < m_size)
			super.setValue (" " + val);
		else
			super.setValue (val);
	}

	/**
	 * Make sure the passed routing number is valid, in the form of bTTTTAAAAC
	 * where:
	 * b    - blank character
	 * TTTT - Federal Reserve Routing Symbol
	 * AAAA - ABA Institution Identifier
	 * C    - CheckDigit
	 */
	public boolean isValid (String val)
	{
		// Make sure the length is correct
		if (val.length () != m_size)
			return false;
		
		// Make sure there is a blank
		if (val.charAt (0) != ' ')
			return false;
		
		// Compute the check digit	
		int accum;	
		accum = (val.charAt (1) - '0') * 3;
		accum += (val.charAt (2) - '0') * 7;
		accum += (val.charAt (3) - '0') * 1;
		accum += (val.charAt (4) - '0') * 3;
		accum += (val.charAt (5) - '0') * 7;
		accum += (val.charAt (6) - '0') * 1;
		accum += (val.charAt (7) - '0') * 3;
		accum += (val.charAt (8) - '0') * 7;
		
		return (10 - accum % 10) == (val.charAt (9) - '0');
	}
}
