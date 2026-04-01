package com.intuit.sbd.payroll.psp.ach.fixedlen;

import java.io.*;
import java.math.*;
import java.util.*;

public abstract class RecordManager
{
	public static final int DEFAULT_RECORD_LENGTH = 94;
	public static final BigDecimal ZERO_AMOUNT = new BigDecimal ("0.00");
	protected Vector<RecordListener> m_listeners;

	public void reset () throws RecordManagerException {
	}
	
	/**
	 * Reads and input the records and fields from the reader.
	 */
	public abstract int read (Reader r) throws IOException, RecordManagerException;

	/**
	 * Generate and output the records and fields to the writer.
	 */
	public abstract int write (Writer w) throws IOException, RecordManagerException;

	public int write (Writer writer, Writer writer2) throws IOException, RecordManagerException
	{
		return write(writer, null);
	}
	
	public int write (Writer writer, Writer writer2, Writer writer3, Writer writer4) throws IOException, RecordManagerException
	{
		return write(writer, null, null, null);
	}
	
	public int write (Writer writer, Writer writer2, List<Long> efileTransactionIDs) throws IOException, RecordManagerException
	{
		return write(writer, null, null);
	}
	
	/**
	 * Return the entry detail handler and remove callback.
	 */
	public RecordManager removeRecordListener (RecordManager l) {
		if (m_listeners != null && m_listeners.contains (l))
			m_listeners.remove (l);

		return l;
	}

	public void addRecordListener (RecordListener l) {
		if (m_listeners == null)
			m_listeners = new Vector ();
			
		if (! m_listeners.contains (l))
			m_listeners.add (l);
	}
	
	public void notifyRecordCreated (RecordTemplate template)
	{
		if (m_listeners == null)
			return;

		for (int i = 0; i < m_listeners.size (); i++)
			((RecordListener)m_listeners.get (i)).recordCreated (template);
	}

	public void notifyRecordCreated (RecordTemplate template,
									BigDecimal debit, BigDecimal credit)
	{
		if (m_listeners == null)
			return;

		for (int i = 0; i < m_listeners.size (); i++)
			((RecordListener)m_listeners.get (i)).recordCreated (
									template, debit, credit);
	}

	public void notifyRecordCreated (RecordTemplate template, Hashtable fields)
	{
		if (m_listeners == null)
			return;

		for (int i = 0; i < m_listeners.size (); i++)
			((RecordListener)m_listeners.get (i)).recordCreated (
									template, fields);
	}
}
