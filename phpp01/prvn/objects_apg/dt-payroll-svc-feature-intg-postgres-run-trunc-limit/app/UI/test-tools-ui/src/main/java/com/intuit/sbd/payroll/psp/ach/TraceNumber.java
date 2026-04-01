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
* TraceNumber.java
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


import com.intuit.sbd.payroll.psp.ach.fixedlen.FieldTemplate;
import com.intuit.sbd.payroll.psp.ach.fixedlen.RecordManagerException;
import com.intuit.sbd.payroll.psp.ach.util.AppHelper;

/**
* The Trace Number in the ACH Entry Detail.
*/
public class TraceNumber extends FieldTemplate
{
	public String getRoutingNumber ()	{
		return m_value.substring (0, 8);
	}
	
	public void setRoutingNumber (String r) throws RecordManagerException, AchException
	{
		if (! AppHelper.isDigit (r) || r.length () != 8)
			throw new AchException ("Routing Number must be a 8 digit number"); 

		// Validate before storing
		StringBuffer buf = new StringBuffer (getFormattedValue ());
		buf.replace (0, 8, r);		
		setValue (buf.toString ());
	}
	
	public int getSequence () {
		return Integer.parseInt (m_value.substring (8, 15));
	}
	
	public void setSequence (int seq) throws RecordManagerException, AchException
	{
		StringBuffer seqbuf = new StringBuffer ("0000000");
		String seqval = String.valueOf (seq);
		seqbuf.replace (7 - seqval.length (), 7, seqval);

		StringBuffer buf = new StringBuffer (getFormattedValue ());
		buf.replace (8, 15, seqbuf.toString ());		
		setValue (buf.toString ());
	}
}

