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
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * The default ACH entry class.
 */
public class AchEntry extends Ach
{
	protected RecordTemplate m_entryDetail;
	protected RecordTemplate m_addenda;
	protected RecordTemplate m_returnAddenda;

	private boolean isReturn = false;

	/**
	 * The header and control record templates.
	 */
	public RecordTemplate getEntryDetail () {
		return m_entryDetail;
	}

	public void setEntryDetail (RecordTemplate r) {
		m_entryDetail = r;
	}

	public RecordTemplate getAddenda () {
		return m_addenda;
	}

	public void setAddenda (RecordTemplate r) {
		m_addenda = r;
	}

	public RecordTemplate getReturnAddenda () {
		return m_returnAddenda;
	}

	public void setReturnAddenda (RecordTemplate r) {
		m_returnAddenda = r;
	}

	public int read (Reader r) throws IOException, RecordManagerException
	{
		int count = 0;

		// Pre-fetch record type code
		char buf[] = new char[m_entryDetail.getField (FieldId.RECORD_TYPE_CODE).getSize ()];
		char transactionCodeBuf[] = new char[m_entryDetail.getField (FieldId.TRANSACTION_CODE).getSize ()];
		while (true)
		{
			// Look ahead and determine end of entries
			r.mark (RECORD_LENGTH);
			r.read (buf);
			r.read (transactionCodeBuf);
			r.reset ();
			if (String.valueOf (buf).equals (Codes.RECORD_ENTRY_DETAIL))
			{
				// Indicate which addenda handler to use
				isReturn = Codes.isReturn(String.valueOf(transactionCodeBuf));

				if (m_entryDetail != null) {
					count += m_entryDetail.read (r);
					notifyRecordCreated (m_entryDetail);
				} else
					r.skip (RECORD_LENGTH);

			}
			else if (String.valueOf (buf).equals (Codes.RECORD_ADDENDA))
			{
				// use different addenda handler for returns
				if (isReturn) {
					if (m_returnAddenda != null) {
						count += m_returnAddenda.read (r);
						notifyRecordCreated (m_returnAddenda);
					} else
						r.skip (RECORD_LENGTH);
				} else {
					if (m_addenda != null) {
						count += m_addenda.read (r);
						notifyRecordCreated (m_addenda);
					} else
						r.skip (RECORD_LENGTH);
				}
			} else {
				break;
			}
		}
		return count;
	}

	public int write (Writer w)	throws IOException, RecordManagerException
	{
		int count = 0;
		count += write (w, m_entryDetail);
		count += write (w, m_addenda);
		return count;
	}

	/**
	 * All fields should be already set by batch before this is called.
	 */
	protected int write (Writer w, RecordTemplate entry)
					throws IOException, RecordManagerException
	{
		if (entry == null)
			return 0;

		reset ();
		int count = entry.write (w);

		//if addenda record then return.
		if(entry.getId() == RecordId.ADDENDA)
			notifyRecordCreated (entry);
		else
		{
			// Determine if this is a credit or a debit entry and notify listeners
			String value = entry.getFieldValue (FieldId.AMOUNT);
			if (value == null)
			{
				notifyRecordCreated (entry);
			}
			else
			{
				String transCode = entry.getFieldValue (FieldId.TRANSACTION_CODE);
				BigDecimal amount = new BigDecimal (new BigInteger (value), 2);

				notifyRecordCreated (entry,
						Codes.isDebit (transCode) ? amount : ZERO_AMOUNT,
						Codes.isCredit (transCode) ? amount : ZERO_AMOUNT);
			}
		}
		return count;
	}

	/**
	 * All fields should be already set by batch before this is called.
	 */
	protected boolean isValid (RecordTemplate entry)
	{
		if (entry == null)
			return false;
		return entry.isValid();
	}
}
