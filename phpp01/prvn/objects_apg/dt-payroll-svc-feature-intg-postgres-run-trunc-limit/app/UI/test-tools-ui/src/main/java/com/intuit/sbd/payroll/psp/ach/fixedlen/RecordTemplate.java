package com.intuit.sbd.payroll.psp.ach.fixedlen;

import com.intuit.sbd.payroll.psp.ach.util.StringUtil;
import com.intuit.sbd.payroll.psp.ach.util.Helper;
import com.intuit.sbd.payroll.psp.ach.util.Identity;
import java.math.*;
import java.io.*;
import java.util.*;


/**
 * The fixed length record template class.
 * These are templates used by application-specific classes to generate data entries.
 * Each record is made up of a number of Fields.
 */
public class RecordTemplate extends Identity {
	protected Vector<FieldTemplate> m_fields = new Vector();
	protected boolean shouldRecordsWithAllZeroNumericFieldsNotBeWritten = true;
	public static final String CarriageReturnLineFeed = "\r\n";
	private boolean includeCarriageReturn = false;
	/**
	 * Append the field to the end of the list.
	 * Note that the order of fields is important in records because the fields will be written out according to the order.
	 */
	public void add(FieldTemplate fieldTemplate) {
		m_fields.add(fieldTemplate);
	}
	/**
	 * Clears all fields.
	 */
	public void reset() throws RecordManagerException {
		for (FieldTemplate fieldTemplate : m_fields) {
			fieldTemplate.clearValue();
		}
	}
	/**
	 * Determines if the all the numeric fields in the record contain only zero values.
	 */
	public boolean isZero() {
		boolean allZeroes = false;
		if (shouldRecordsWithAllZeroNumericFieldsNotBeWritten) { // Why is this check at this low level?
			for (FieldTemplate fieldTemplate : m_fields) {
				if (fieldTemplate.getValueType().equals("numeric")) {
					if (Helper.isEmpty(fieldTemplate.getValue()) || Float.parseFloat(fieldTemplate.getValue()) == 0) {
						allZeroes = true;
					}
					else {
						return false;
					}
				}
			}
		}
		return allZeroes;	
	}
	public void setIncludeCarriageReturn(boolean includeCarriageReturn) {
		this.includeCarriageReturn = includeCarriageReturn;
	}
	public Enumeration getFields() {
		return m_fields.elements();
	}
	public Iterator getFieldIterator() {
		return m_fields.iterator();
	}
	public FieldTemplate getField(int id) {
		for (FieldTemplate fieldTemplate : m_fields) {
			if (fieldTemplate.getId() == id) {
				return fieldTemplate;
			}
		}
		return null;
	}
	public String getFieldValue(int id) {
		return getField(id).getValue();
	}
	public int getFieldInt(int id) {
		return Integer.parseInt(getField(id).getValue());
	}
	public BigDecimal getFieldBigDecimal (int id) {
		BigDecimal result = RecordManager.ZERO_AMOUNT;
		String value = getField (id).getValue ();
		if (!Helper.isEmpty(value) && !StringUtil.isWhitespace(value))
		{
			try {
				result = new BigDecimal(new BigInteger (value), 2);
			}
			catch (RuntimeException ex) {
				System.out.println ("Enable to parse BigDecimal: " + ex);
			}
		}
		return result;
	}

	public void setFieldValue (int id, String value) throws RecordManagerException {
		FieldTemplate f = getField (id);
		if (f != null)
			f.setValue (value);
	}

	public void setFieldValue (int id, BigDecimal value) throws RecordManagerException {
		FieldTemplate f = getField (id);
		if (f != null)
			f.setValue (value.movePointRight (2).toString ());
	}

	public void setFieldValue (int id, BigInteger value) throws RecordManagerException {
		FieldTemplate f = getField (id);
		if (f != null)
			f.setValue (value.toString ());
	}

	public void setFieldValue (int id, int value) throws RecordManagerException {
		FieldTemplate f = getField (id);
		if (f != null)
			f.setValue (String.valueOf (value));
	}

	/**
	 * Reads the field contents from the reader.
	 */
	public int read (Reader r) throws IOException, RecordManagerException
	{
		char buf[];
		int count = 0;
		for (FieldTemplate fieldTemplate : m_fields) {
			buf = new char[fieldTemplate.getSize()];
			count += r.read(buf);
			fieldTemplate.setValue(String.valueOf(buf));
		}
		return count;
	}

	/**
	 * Writes the field contents to the writer.
	 */
	public int write (Writer writer) throws IOException, RecordManagerException {
		if (writer == null) {
			return 0;
		}
		int count = 0;
		for (FieldTemplate fieldTemplate : m_fields) {
			String formattedFieldValue = fieldTemplate.getFormattedValue();
			writer.write(formattedFieldValue);
			count += formattedFieldValue.length();
		}
		if (includeCarriageReturn) {
			writer.write(CarriageReturnLineFeed);
		}
		return count;
	}
	
	/**
	 * append the record contents to the strBuilder.
	 */
	public int appendRecord(StringBuilder stringBuilder) {
		if (stringBuilder == null) {
			return 0;
		}
		int count = 0;
		for (FieldTemplate fieldTemplate : m_fields) {
			String formattedFieldValue = fieldTemplate.getFormattedValue();
			stringBuilder.append(formattedFieldValue);
			count += formattedFieldValue.length();
		}
		if (includeCarriageReturn) {
			stringBuilder.append(CarriageReturnLineFeed);
		}
		return count;
	}
	
	/**
	 * Attempt to set the field values to the field mapping
	 */
	public void mapFields (Map data, int index) throws RecordManagerException
	{
		mapFields(data, index, null, false);
	}
	public void mapFields (Map data, int index, Map numericFieldAccumulator, boolean prependFieldNameToValue) throws RecordManagerException {
		for (FieldTemplate fieldTemplate : m_fields) {
			fieldTemplate.map(data, index, numericFieldAccumulator, prependFieldNameToValue);
		}
	}

	/**
	 * Attempt to set the field values to the field mapping
	 */
	public void mapFields (Map data) throws RecordManagerException
	{
		for (FieldTemplate fieldTemplate : m_fields) {
			fieldTemplate.map(data);
		}
	}
	/**
	 * Return a hashtable containing the data for fields with mapping attributes
	 */
	public Hashtable mapFields() throws RecordManagerException {
		Hashtable data = new Hashtable();
		for (FieldTemplate fieldTemplate : m_fields) {
			fieldTemplate.mapTo(data);
		}
		return data;
	}
	
	/**
	 * Return the fields count
	 */
	public int getFieldCount()
	{
		return m_fields.size();
	}
	
	/**
	 * Get the record templates size. This is size of one record template, before binding the actual data.
	 */
	public int getRecordTemplateSize()
	{
		int width = 0;
		for (FieldTemplate fieldTemplate : m_fields) {
			width += fieldTemplate.getSize();
		}
		return width;
	}
	
	/**
	 * validate record fields
	 */
	public boolean isValid()
	{
		boolean valid = true;
		try {
			for (FieldTemplate fieldTemplate : m_fields) {
				fieldTemplate.getFormattedValue();
			}
		}
		catch (Exception e) {
			valid = false;
		}
		return valid;
	}
	
	/**
	 * @return Returns the shouldRecordsWithAllZeroNumericFieldsNotBeWritten.
	 */
	public boolean shouldRecordsWithAllZeroNumericFieldsNotBeWritten() {
		return shouldRecordsWithAllZeroNumericFieldsNotBeWritten;
	}
	/**
	 * @param shouldRecordsWithAllZeroNumericFieldsNotBeWritten The shouldRecordsWithAllZeroNumericFieldsNotBeWritten to set.
	 */
	public void setShouldRecordsWithAllZeroNumericFieldsNotBeWritten(boolean value) {
		shouldRecordsWithAllZeroNumericFieldsNotBeWritten = value;
	}
}	
