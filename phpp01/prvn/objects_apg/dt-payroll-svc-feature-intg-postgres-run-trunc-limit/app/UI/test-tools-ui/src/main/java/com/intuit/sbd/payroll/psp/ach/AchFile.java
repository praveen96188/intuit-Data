/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.ach;

/**
 *
 * @author shivanandad069
 */


import com.intuit.sbd.payroll.psp.ach.fixedlen.RecordTemplate;
import com.intuit.sbd.payroll.psp.ach.fixedlen.RecordManagerException;
import com.intuit.sbd.payroll.psp.ach.fixedlen.RecordListener;
import com.intuit.sbd.payroll.psp.ach.util.Helper;


import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import com.intuit.sbd.payroll.psp.ach.util.XmlResourcePool;


/**
 * The AchFile class represents an ACH file.
 */ 
public class AchFile extends AchEnvelope
{
	protected Date m_createDate;
	int m_totalEntryAddenda;
	int m_totalBatch;

	public AchFile ()
	{
		m_createDate = new Date();
	}

	public void setResourcePool (XmlResourcePool pool)
	{
		m_pool = pool;
		m_header = (RecordTemplate)m_pool.get (RecordId.FILE_HEADER, false);
		m_control = (RecordTemplate)m_pool.get (RecordId.FILE_CONTROL, false);
	}
	
	public void reset ()
	{
		super.reset ();
		m_totalEntryAddenda = 0;
		m_totalBatch = 0;
	}

	public Date getCreateDate () {
		return m_createDate;
	}

	/**
	 * Reads and input the records and fields from the reader.
	 */
	public int read (Reader r) throws IOException, RecordManagerException
	{
		// Read the header
		int count = m_header.read (r);
		notifyRecordCreated (m_header);
		
		// Set up the record handler if necessary
		if (m_handler == null)
		{
			m_handler = new AchBatch ();
			((AchBatch)m_handler).setResourcePool (m_pool);

			// Establish communication link
			for (int i = 0; i < m_listeners.size (); i++)
				m_handler.addRecordListener ((RecordListener)m_listeners.get (i));
		}
		
		while (true)
		{
			// Look ahead and determine end of file
			r.mark (RECORD_LENGTH);
			char buf[] = new char[m_control.getField (FieldId.RECORD_TYPE_CODE).getSize ()];
			r.read (buf);
			r.reset ();
			if (String.valueOf (buf).equals (m_control.getFieldValue (FieldId.RECORD_TYPE_CODE))) {
				break;
			}
		
			// Read the batches
			m_handler.reset ();
			count += m_handler.read (r);
		}
			
		// Read the control
		count += m_control.read (r);
		notifyRecordCreated (m_control);
		return count;
	}

	/**
	 * Generate and output the records and fields to the writer.
	 */
	public int write (Writer w)
						throws IOException, RecordManagerException
	{
		reset ();

		// Accumulators
		int totalRecord = 0;
		int totalBlock = 0;
		
		// Get the file creation data and time
		SimpleDateFormat df = new SimpleDateFormat ();
		df.applyPattern ("yyMMdd");
		m_header.setFieldValue (FieldId.FILE_CREATION_DATE, df.format (m_createDate));
		df.applyPattern ("HHmm");
		m_header.setFieldValue (FieldId.FILE_CREATION_TIME, df.format (m_createDate));
		
		// Output the header
		int count = m_header.write (w);
		notifyRecordCreated (m_header, m_credit, m_debit);
		
		// Output the batches
		if (m_handler != null)
			count += m_handler.write (w);

		// Prepare the File Control Record content
		m_control.setFieldValue (FieldId.BATCH_COUNT, Integer.toString (m_totalBatch));
								
		// Compute the block count
		int blockingFactor = Integer.parseInt (
				m_header.getFieldValue (FieldId.BLOCKING_FACTOR));

		totalRecord = m_totalEntryAddenda + m_totalBatch * 2 + 2;
		totalBlock = totalRecord/blockingFactor;
		int tobeFilled = blockingFactor - totalRecord % blockingFactor;
		if (tobeFilled == blockingFactor)
			tobeFilled = 0;
		else
			totalBlock++;
		
		m_control.setFieldValue (FieldId.BLOCK_COUNT, Integer.toString (totalBlock));
		m_control.setFieldValue (FieldId.ENTRY_ADDENDA_COUNT, Integer.toString (m_totalEntryAddenda));
		m_control.setFieldValue (FieldId.ENTRY_HASH, m_entryHash);
		m_control.setFieldValue (FieldId.TOTAL_DEBIT_ENTRY_DOLLAR_AMOUNT_IN_FILE, m_debit);
		m_control.setFieldValue (FieldId.TOTAL_CREDIT_ENTRY_DOLLAR_AMOUNT_IN_FILE, m_credit);
		
		// Write out the control record
		count += m_control.write (w);
		notifyRecordCreated (m_control, m_credit, m_debit);
		
		// Fill the rest with all nines
		if (w != null)
		{
			boolean isAchFtpEnabled = Helper.isAchFtpEnabled();
			boolean isChase = Helper.getAchProcessor().equalsIgnoreCase("chase");
			for (int i = 0; i < tobeFilled; i++) {
				if (!isChase || isAchFtpEnabled) {
				    w.write (m_94nines_ftp);
				    count += m_94nines_ftp.length ();
				} else {
					w.write (m_94nines);
					count += m_94nines.length ();
				}
			}
		}

		return count;
	}

	/**
	 * Implement RecordListener
	 */
	public void recordCreated (RecordTemplate template, BigDecimal debit, BigDecimal credit)
	{
		if (template.getId () != RecordId.BATCH_CONTROL)
			return;

		m_totalEntryAddenda += Integer.parseInt (template.getFieldValue (FieldId.ENTRY_ADDENDA_COUNT));
		m_totalBatch++;
		m_debit = m_debit.add (debit);
		m_credit = m_credit.add (credit);
		
		// Build the entry hash
		m_entryHash = computeEntryHash (m_entryHash,
			template.getFieldValue (FieldId.ENTRY_HASH),
			m_control.getField (FieldId.ENTRY_HASH).getSize ());
	}
	
	static final String m_94nines_ftp =
		"9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\r\n";
	static final String m_94nines =
		"9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999";
}	
