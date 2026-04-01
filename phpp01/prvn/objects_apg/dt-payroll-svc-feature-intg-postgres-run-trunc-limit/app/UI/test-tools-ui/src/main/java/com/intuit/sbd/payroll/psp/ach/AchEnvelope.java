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
import com.intuit.sbd.payroll.psp.ach.fixedlen.RecordListener;
import com.intuit.sbd.payroll.psp.ach.fixedlen.RecordManagerException;
import java.math.*;
import java.util.*;

import com.intuit.sbd.payroll.psp.ach.util.XmlResourcePool;

/**
 * The base class of Ach envelope.
 */ 
public abstract class AchEnvelope extends Ach implements RecordListener
{
	protected String m_entryHash;
	protected BigDecimal m_credit; 
	protected BigDecimal m_debit;
	protected RecordTemplate m_header;
	protected RecordTemplate m_control;
	protected XmlResourcePool m_pool;
	protected Ach m_handler;

	public void setRecordHandler (Ach handler) {
		m_handler = handler;
		handler.addRecordListener (this);
		handler.setEnvelope (this);
	}
			
	public Ach getRecordHandler () {
		return m_handler;
	}
			
	/**
	 * The header and control record templates.
	 */
	public RecordTemplate getHeader () {
		return m_header;
	}
	
	public void setHeader (RecordTemplate r) {
		m_header = r;
	}
	
	public RecordTemplate getControl () {
		return m_control;
	}
	
	public void setControl (RecordTemplate r) {
		m_control = r;
	}
	
	/**
	 * Convenience method to set a field value in both the header and control templates.
	 */
	public void setFieldValue (int id, String val) throws RecordManagerException
	{
		m_header.setFieldValue (id, val);
		m_control.setFieldValue (id, val);
	}

	/**
	 * Default RecordListener implementation
	 */
	public void recordCreated (RecordTemplate template)
	{
	}

	public void recordCreated (RecordTemplate template, BigDecimal debit, BigDecimal credit)
	{
	}

	public void recordCreated (RecordTemplate template, Hashtable attributes)
	{
	}

	/**
	 * Helper method that computes the next entry hash
	 */
	public static String computeEntryHash (String hash, String entry, int len)
	{
		String newHash = String.valueOf (
					new BigInteger (hash).add (new BigInteger (entry)));
		int newLen = newHash.length ();
		return newLen > len ? newHash.substring (newLen - len) : newHash;
	}
	
	public void reset () {
		m_entryHash = "0";
		m_credit = new BigDecimal ("0.00");
		m_debit = new BigDecimal ("0.00");
	}
	
	public BigDecimal getDebit()
	{
		return m_debit;
	}
}
