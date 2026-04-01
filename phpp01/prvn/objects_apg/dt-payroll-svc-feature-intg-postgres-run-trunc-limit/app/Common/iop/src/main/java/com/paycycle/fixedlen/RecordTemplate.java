/**
 * RecordTemplate.java
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
package com.paycycle.fixedlen;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.paycycle.util.Helper;
import com.paycycle.util.Identity;
import com.paycycle.util.StringUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * The fixed length record template class.  These are templates used by application
 * specific classes to generate data entries.  Each record is made up of a number
 * of Fields.
 */
public class RecordTemplate extends Identity {
    protected boolean mIncludeCarriageReturn = false;
    protected Vector<FieldTemplate> mFields = new Vector<FieldTemplate>();

    public RecordTemplate() {
    }

    protected RecordTemplate(final RecordTemplate pSource) {
        super(pSource);

        mIncludeCarriageReturn = pSource.mIncludeCarriageReturn;

        for (FieldTemplate fieldTemplate : pSource.mFields) {
            mFields.add(fieldTemplate.clone());
        }
    }

    @Override
    public RecordTemplate clone() {
        return new RecordTemplate(this);
    }

    /**
     * Append the field to the end of the list.  Note that the order of fields
     * is important in records because the fields will be written out according
     * to the order.
     */
    public void add(FieldTemplate f) {
        mFields.add(f);
    }

    /**
     * Clears all fields.
     */
    public void reset() throws RecordManagerException {
        for (FieldTemplate field : mFields) {
            field.clearValue();
        }
    }

    /**
     * Determines if the data in the record contains only zero values.
     */
    public boolean isZero() {
        boolean allZeroes = false;

        for (FieldTemplate field : mFields) {
            if (field.getValueType().equals("numeric")) {
                if (Helper.isEmpty(field.getValue()) || (Float.parseFloat(field.getValue()) == 0)) {
                    allZeroes = true;
                } else {
                    return false;
                }
            }
        }

        return allZeroes;
    }

    public void setIncludeCarriageReturn(boolean pIncludeCarriageReturn) {
        mIncludeCarriageReturn = pIncludeCarriageReturn;
    }

    public Iterator getFieldIterator() {
        return mFields.iterator();
    }

    public List<FieldTemplate> getFieldList() {
        return mFields;
    }

    /**
     * Returns the field whose id matches the passed id.
     */
    public FieldTemplate getField(int pFieldId) {
        for (FieldTemplate field : mFields) {
            if (field.getId() == pFieldId) {
                return field;
            }
        }

        return null; // not found
    }

    public String getFieldValue(int pFieldId) {
        return getField(pFieldId).getValue();
    }

    public int getFieldInt(int pFieldId) {
        return Integer.parseInt(getField(pFieldId).getValue());
    }

    public BigDecimal getFieldBigDecimal(int pFieldId) {
        BigDecimal result = null;
        String value = getField(pFieldId).getValue();

        if (!Helper.isEmpty(value) && !StringUtil.isWhitespace(value)) {
            try {
                result = new BigDecimal(new BigInteger(value), 2);
            } catch (RuntimeException ex) {
                System.out.println("Enable to parse BigDecimal: " + ex);
            }
        }

        return (result == null) ? new BigDecimal("0.00") : result;
    }

    public void setFieldValue(int pFieldId, String pValue) throws RecordManagerException {
        FieldTemplate f = getField(pFieldId);

        if (f != null) {
            f.setValue(pValue);
        }
    }

    public void setFieldValue(int pFieldId, BigDecimal pValue) throws RecordManagerException {
        FieldTemplate f = getField(pFieldId);

        if (f != null) {
            f.setValue(pValue.movePointRight(2).toString());
        }
    }

    public void setFieldValue(int pFieldId, BigInteger pValue) throws RecordManagerException {
        FieldTemplate f = getField(pFieldId);

        if (f != null) {
            f.setValue(pValue.toString());
        }
    }

    public void setFieldValue(int pId, int pValue) throws RecordManagerException {
        FieldTemplate f = getField(pId);

        if (f != null) {
            f.setValue(String.valueOf(pValue));
        }
    }

    /**
     * Reads the field contents from the reader.
     */
    public int read(Reader pReader) throws IOException, RecordManagerException {
        int count = 0;

        for (FieldTemplate field : mFields) {
            char[] buf = new char[field.getSize()];
            count += pReader.read(buf);
            field.setValue(String.valueOf(buf));
        }

        return count;
    }

    /**
     * Writes the field contents to the writer.
     */
    public int write(Writer pWriter) throws IOException, RecordManagerException {
        if (pWriter == null) {
            return 0;
        }

        int count = 0;

        for (FieldTemplate field : mFields) {
            String buf = field.getFormattedValue();
            pWriter.write(buf);
            count += buf.length();
        }

        // include carriage return for each record.
        if (mIncludeCarriageReturn) {
            pWriter.write(EftpsUtil.NEWLINE);
        }

        return count;
    }

    /**
     * append the record contents to the strBuilder.
     */
    public int appendRecord(StringBuilder pBuffer) {
        if (pBuffer == null) {
            return 0;
        }

        int count = 0;

        for (FieldTemplate field : mFields) {
            String buf = field.getFormattedValue();
            pBuffer.append(buf);
            count += buf.length();
        }

        // include carriage return for each record.
        if (mIncludeCarriageReturn) {
            pBuffer.append(EftpsUtil.NEWLINE);
        }

        return count;
    }

    /**
     * Given the passed hashtable, attempt to set the field values to the field mapping
     */
    public void mapFields(Map pData, int pIndex) throws RecordManagerException {
        mapFields(pData, pIndex, null, false);
    }

    public void mapFields(Map pData, int pIndex, Map pNumericFieldAccumulator, boolean pPrependFieldNameToValue) throws RecordManagerException {
        for (FieldTemplate field : mFields) {
            field.map(pData, pIndex, pNumericFieldAccumulator, pPrependFieldNameToValue);
        }
    }

    /**
     * Given the passed hashtable, attempt to set the field values to the field mapping
     */
    public void mapFields(Map pData) throws RecordManagerException {
        for (FieldTemplate field : mFields) {
            field.map(pData);
        }
    }

    /**
     * Return a hashtable containing the data for fields with mapping attributes
     */
    public Hashtable mapFields() throws RecordManagerException {
        Hashtable data = new Hashtable();

        for (FieldTemplate field : mFields) {
            field.mapTo(data);
        }

        return data;
    }

    /**
     * Return the fields count
     */
    public int getFieldCount() {
        return mFields.size();
    }

    /**
     * Get the record templates size. This is size of one record template, before binding the actual data.
     */
    public int getRecordTemplateSize() {
        int width = 0;

        for (FieldTemplate field : mFields) {
            width += field.getSize();
        }

        return width;
    }

    @Override
    public String toString() {
        try {
            StringWriter writer = new StringWriter();
            write(writer);
            return writer.toString();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
