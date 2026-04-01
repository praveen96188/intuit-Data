/**
* RoutingNumber.java
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

package com.intuit.sbd.payroll.psp.ach;

import com.intuit.sbd.payroll.psp.ach.fixedlen.FieldTemplate;
import com.intuit.sbd.payroll.psp.ach.fixedlen.RecordManagerException;
import com.intuit.sbd.payroll.psp.ach.util.AppHelper;

/**
* The Routing Number of the ACH Operator or sending point that is sending the file.
*/
public class RoutingNumber extends FieldTemplate
{
	public String getRouting ()	{
		return m_value.substring (0, 4);
	}
	
	public void setRouting (String r) throws RecordManagerException, AchException
	{
		if (! AppHelper.isDigit (r) || r.length () != 4)
			throw new AchException ("Federal Reserve Routing Symbol must be a 4 digit number"); 

		// Validate before storing
		StringBuffer buf = new StringBuffer (getFormattedValue ());
		buf.replace (0, 4, r);		
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
		buf.replace (4, 8, id);
		setValue (buf.toString ());
	}
	
	/**
	 * Given the passed string in the format TTTTAAAA, where:
	 * TTTT - Federal Reserve Routing Symbol
	 * AAAA - ABA Institution Identifier,
	 * compute the check digit
	 */
	public static String computeCheckDigit (String n)
	{
		int accum;
	
		// Compute the check digit
		accum = (n.charAt (0) - '0') * 3;
		accum += (n.charAt (1) - '0') * 7;
		accum += (n.charAt (2) - '0') * 1;
		accum += (n.charAt (3) - '0') * 3;
		accum += (n.charAt (4) - '0') * 7;
		accum += (n.charAt (5) - '0') * 1;
		accum += (n.charAt (6) - '0') * 3;
		accum += (n.charAt (7) - '0') * 7;
		
		return Integer.toString (10 - accum % 10, 10);
	}
	
}
