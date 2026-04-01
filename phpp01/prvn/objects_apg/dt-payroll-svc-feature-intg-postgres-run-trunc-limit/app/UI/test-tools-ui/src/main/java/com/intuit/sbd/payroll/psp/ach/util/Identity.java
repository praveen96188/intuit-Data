/**
* Identity.java
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

package com.intuit.sbd.payroll.psp.ach.util;

/**
 * The ACH Field base class
 */
public class Identity
{
	protected int m_id;
	protected String m_name;
	protected String m_delimiter;
	
	public int getId () {
		return m_id;		
	}

	public void setId (int id) {
		m_id = id;
	}
	
	public String getName () {
		return m_name;		
	}

	public void setName (String name) {
		m_name = name;
	}
	
	public String getDelimiter () {
		return m_delimiter;		
	}

	public void setDelimiter (String delim) {
		m_delimiter = delim;
	}
	
}	
