/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach;

/**
 *
 * @author shivanandad069
 */


import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;

import com.intuit.sbd.payroll.psp.ach.code.StandardEntryClassCodes;
import com.intuit.sbd.payroll.psp.ach.fixedlen.RecordListener;
import com.intuit.sbd.payroll.psp.ach.fixedlen.RecordManagerException;
import com.intuit.sbd.payroll.psp.ach.fixedlen.RecordTemplate;
import com.intuit.sbd.payroll.psp.ach.util.XmlResourcePool;

/**
 * The AchBatch class represents an ACH Company/Batch.
 */ 
public class AchBatch extends AchEnvelope
{
	int m_numEntryAddenda;
	
	public void setResourcePool (XmlResourcePool pool) {
		m_pool = pool;
		m_header = (RecordTemplate)m_pool.get (RecordId.BATCH_HEADER, true);
		m_control = (RecordTemplate)m_pool.get (RecordId.BATCH_CONTROL, true);
	}
	
	public void reset () {
		super.reset ();
		m_numEntryAddenda = 0;
	}

	/**
	 * Reads and input the records and fields from the reader.
	 */
	public int read (Reader r) throws IOException, RecordManagerException
	{
		// Read the header
		int count = m_header.read (r);
		notifyRecordCreated (m_header);

		// Determine and create the handler to read the entries.
		m_handler = createEntryHandler (m_header.getFieldValue (
							FieldId.STANDARD_ENTRY_CLASS_CODE));
		
		while (true)
		{
			// Look ahead and determine end of batches
			r.mark (RECORD_LENGTH);
			char buf[] = new char[m_control.getField (FieldId.RECORD_TYPE_CODE).getSize()];
			r.read (buf);
			r.reset ();

			if (String.valueOf (buf).equals (
					m_control.getFieldValue (FieldId.RECORD_TYPE_CODE))) {
				break;
			}
		
			// Read the entries
			int entryCount = m_handler.read (r);
			if (entryCount > 0) {
				count += entryCount;
			} else {
				throw new RecordManagerException("Invalid ACH entry.");
			}
		}
			
		// Read the control
		count += m_control.read (r);
		notifyRecordCreated (m_control);
		return count;
	}

	/**
	 * Generate and output the records and fields to the writer.
	 * This method will update the m_numEntryAddenda field.
	 */
	public int write (Writer w)	throws IOException, RecordManagerException
	{
		reset ();

		// Write the header record out
		int count = m_header.write (w);
		notifyRecordCreated (m_header, m_debit, m_credit);

		// Gather all entry details and write them out
		if (m_handler != null) 
			count += m_handler.write (w); // Entry.write(w)
		
		// Prepare to write control record
		m_control.setFieldValue (FieldId.ENTRY_ADDENDA_COUNT, Integer.toString (m_numEntryAddenda));
		m_control.setFieldValue (FieldId.ENTRY_HASH, m_entryHash);
		m_control.setFieldValue (FieldId.TOTAL_DEBIT_ENTRY_DOLLAR_AMOUNT, m_debit);
		m_control.setFieldValue (FieldId.TOTAL_CREDIT_ENTRY_DOLLAR_AMOUNT, m_credit);
					
		// Write the control record out.
		count += m_control.write (w);
		notifyRecordCreated (m_control, m_debit, m_credit);
		return count;
	}
	
	/**
	 * Returns the appropriate record handler based on SEC code
	 */
	protected AchEntry createEntryHandler (String secCode)
	{
		AchEntry handler = null;
		handler = new AchEntry ();
		if (secCode.equals (StandardEntryClassCodes.STANDARD_PPD))
		{
			handler.setEntryDetail ((RecordTemplate)m_pool.get (RecordId.PPD_ENTRY_DETAIL, false));
			handler.setAddenda ((RecordTemplate)m_pool.get (RecordId.ADDENDA, false));
			handler.setReturnAddenda ((RecordTemplate)m_pool.get (RecordId.RETURN_ADDENDA, false));
		}
		else if (secCode.equals (StandardEntryClassCodes.STANDARD_CCD))		
		{
			handler.setEntryDetail ((RecordTemplate)m_pool.get (RecordId.CCD_ENTRY_DETAIL, false));
			handler.setAddenda ((RecordTemplate)m_pool.get (RecordId.ADDENDA, false));
			handler.setReturnAddenda ((RecordTemplate)m_pool.get (RecordId.RETURN_ADDENDA, false));
		}
		// Notification of Change entry
		else if (secCode.equals (StandardEntryClassCodes.STANDARD_COR))		
		{
			handler.setEntryDetail ((RecordTemplate)m_pool.get (RecordId.COR_ENTRY_DETAIL, false));
			handler.setAddenda ((RecordTemplate)m_pool.get (RecordId.COR_ADDENDA, false));
			handler.setReturnAddenda ((RecordTemplate)m_pool.get (RecordId.COR_ADDENDA, false));
		}

		// Establish communication link
		for (int i = 0; i < m_listeners.size (); i++) 
			handler.addRecordListener ((RecordListener)m_listeners.get (i));

		return handler;
	}

	/**
	 * Implement RecordListener
	 */
	public void recordCreated (RecordTemplate template, BigDecimal debit, BigDecimal credit)
	{
		m_credit = m_credit.add (credit);
		m_debit = m_debit.add (debit);
		m_numEntryAddenda++;

		// Build the entry hash
		m_entryHash = computeEntryHash (m_entryHash,
			template.getFieldValue (FieldId.RECEIVING_DFI_IDENTIFICATION),
			m_control.getField (FieldId.ENTRY_HASH).getSize ());
	}

	/**
	 * Implement RecordListener
	 */
	public void recordCreated (RecordTemplate template)
	{
		//only for addenda records add the count
		if(template.getId() == RecordId.ADDENDA)		
			m_numEntryAddenda++;
	}

}	

