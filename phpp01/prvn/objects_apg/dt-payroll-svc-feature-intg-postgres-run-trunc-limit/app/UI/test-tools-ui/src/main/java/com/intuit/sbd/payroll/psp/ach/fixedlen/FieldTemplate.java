package com.intuit.sbd.payroll.psp.ach.fixedlen;

import java.math.BigDecimal;
import java.util.Map;

import com.intuit.sbd.payroll.psp.ach.gui.Field;

public class FieldTemplate extends Field
{
	protected String m_valueType;
	protected String m_inclusion;
	protected String m_description;
	protected StringBuffer m_value;
	protected String m_mapping;
	
	public String getValueType () {
		return m_valueType;
	}

	public void setValueType (String vt) {
		m_valueType = vt;
	}
	
	public String getValue () {
		return m_value == null ? null : m_value.toString ();
	}
	
	public String getFormattedValue ()
	{
		// Default value is all blank or all zero for CTS file
		if (m_value == null || m_value.length () < 0) {
			m_value = new StringBuffer (m_size);
			for (int i = 0; i < m_size; i++) {
				m_value.append (' ');
			}
		}
		else {
            char sign = '+';
            if (m_valueType.equals("signedNumeric") && signedValue()) {
                sign = m_value.charAt(0);
                m_value.deleteCharAt(0);
            }
			// Format data
			if (m_valueType.equals ("$$$$$$$$CC")
				|| m_valueType.equals ("$$$$$$$$$$CC")
				|| m_valueType.equals ("numeric")
                || m_valueType.equals("signedNumeric"))
			{
				// Pre-fill numbers/amounts with zeros
				for (int i = m_value.length (); i < m_size; i++)
					m_value.insert (0, '0');
			}
			else if (m_valueType.equals("boolean"))
			{
				if ((m_value.toString()).equals("X"))
					m_value = new StringBuffer("1");
				else
					m_value = new StringBuffer("0");	
			}
			else
			{
				// Post-fill alphanumeric/alphabetic with blanks.
				for (int i = m_value.length (); i < m_size; i++)
					m_value.append (' ');
			}

            if (m_valueType.equals("signedNumeric")) {
                m_value.setCharAt(0, sign);
            }
		}
		return m_value.toString ();
	}

    private boolean signedValue() {
        return m_value.charAt(0) == '-' || m_value.charAt(0) == '+';
    }

	public void setValue (String val) throws RecordManagerException
	{
		// Make sure subclass has a chance to validate the input
		if (! isValid (val))
			throw new RecordManagerException ("Invalid value (" + val + ") for field " + m_name);
		
		// Truncate the value if needed
		// Depending on the type, we truncate on either the left or the right.
		if ((m_size != -1) && (val.length() > m_size)) {
			if (m_valueType.equals("alphanumericTruncateLeft")) {
				m_value = new StringBuffer(val.substring(val.length()-m_size, val.length()));
			} else {
				m_value = new StringBuffer(val.substring(0, m_size));
			}
		} else {
			m_value = new StringBuffer(val);
		}
	}

	public void clearValue() throws RecordManagerException {
		if (m_valueType.equals("numeric") || m_valueType.equals("signedNumeric")) {
            setValue("0");
		}
	}

	/** Values can be:
	 * mandatory - always required to be written
	 * optional  - values can be empty, but still needs to be written
	 * skipEmpty - do not write empty values
	 */
	public String getInclusion () {
		return m_inclusion;
	}
	public void setInclusion (String inc) {
		m_inclusion = inc;
	}
	
	public String getDescription () {
		return m_description;
	}

	public void setDescription (String desc) {
		m_description = desc;
	}
	
	public void setMapping (String map) {
		m_mapping = map;
	}

	public String getMapping () {
		return m_mapping;
	}

	/**
	 * Subclass should override this method to supply specific validation.
	 */
	public boolean isValid (String val) {
		return val != null;
	}
	
	/**
	 * Attempt to populate field
	 * Replace index variable in mapping property
	 */
	public void map(Map data, int index) throws RecordManagerException
	{
		map(data, index, null, false);
	}
	/**
	 * There is a requirement of the source data value when using the accumulator
	 * 1. The source data value string must be a valid constructor value for the BigDecimal type
	 * @param data
	 * @param index
	 * @param numericFieldAccumulator - only numeric fields will be accumulated
	 * @throws com.intuit.sbd.payroll.psp.ach.fixedlen.RecordManagerException
	 */
	public void map(Map data, int index, Map numericFieldAccumulator, boolean prependFieldNameToValue) throws RecordManagerException   
	{
		if (data != null && m_mapping != null) {
			String key = m_mapping.replaceAll("\\{i\\}",String.valueOf(index));
			Object valueObj = data.get(key);
			if (valueObj == null) {
				throw new RecordManagerException("key " + key + " not found");
			}
			String value = valueObj.toString();
			if (value == null) {
				throw new RecordManagerException("value not found");
			}
			else {
				if (numericFieldAccumulator != null && m_valueType.equalsIgnoreCase("numeric")) {
					if (numericFieldAccumulator.containsKey(m_name))
						numericFieldAccumulator.put(m_name, ((BigDecimal)numericFieldAccumulator.get(m_name)).add(new BigDecimal(value.equals("") ? "0.0" : value)));
					else
						numericFieldAccumulator.put(m_name, new BigDecimal(value.equals("") ? "0.0" : value));
				}
				if (prependFieldNameToValue)
					value = m_name + value;
				setValue(value.trim());
			}
		}	
	}

	/**
	 * Attempt to map field mapping specification with hashtable data.
	 */
	public void map(Map data) throws RecordManagerException
	{
		String v;
		if (data != null && m_mapping != null)
		{
			if ((v = (String)data.get(m_mapping)) == null)
				return;

			setValue(v.trim());
		}	
	}

	/**
	 * Attempt to map field mapping specification to hashtable data.
	 */
	public void mapTo(Map data) throws RecordManagerException
	{
		String v;
		if (data != null && m_mapping != null)
		{
			if ((v = getValue()) == null)
				return;

			data.put(m_mapping, v.trim());
		}	
	}
}