/**
* StartWithBlankNumber.java
*/

package com.intuit.sbd.payroll.psp.ach;

import com.intuit.sbd.payroll.psp.ach.fixedlen.FieldTemplate;
import com.intuit.sbd.payroll.psp.ach.fixedlen.RecordManagerException;

/**
* Number starts with Blank, "bNNNNNNNNN", where "b" indicates a blank space.
*/
public class StartWithBlankNumber extends FieldTemplate
{
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
}
