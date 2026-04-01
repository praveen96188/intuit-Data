/**
 * DelimRecordTemplate.java
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

package com.paycycle.delimlen;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.paycycle.fixedlen.FieldTemplate;
import com.paycycle.fixedlen.RecordManagerException;
import com.paycycle.fixedlen.RecordTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.StringTokenizer;

/**
 * The variable length record template class with specified delimiter in definition XML file.
 * These are templates used by application specific classes to generate data entries.
 * Each record is made up of a number of Fields.
 */
public class DelimRecordTemplate extends RecordTemplate {
    public DelimRecordTemplate() {
    }

    protected DelimRecordTemplate(final DelimRecordTemplate pSource) {
        super(pSource);
    }

    @Override
    public DelimRecordTemplate clone() {
        return new DelimRecordTemplate(this);
    }

    /**
     * Reads the field contents from the reader.
     */
    public int read(Reader pReader) throws IOException, RecordManagerException {
        int count = 0;
        // tokenizing the record based on delimiter
        StringTokenizer record = new StringTokenizer(((BufferedReader) pReader).readLine(), getDelimiter());

        for (FieldTemplate field : getFieldList()) {
            if (record.hasMoreTokens()) {
                field.setValue(record.nextToken());
            }

            count++;
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

        for (FieldTemplate field : getFieldList()) {
            String buf = field.getFormattedValue();

            // If delimiter is required then it is appended to the field.
            if (((DelimFieldTemplate) field).getDelimiterReqd()) {
                buf += getDelimiter();
            }

            pWriter.write(buf);

            count += buf.length();
        }

        // include carriage return for each record.
        if ((count > 0) && mIncludeCarriageReturn) {
            pWriter.write(EftpsUtil.NEWLINE);
        }

        return count;
    }

    /**
     * Get the record templates size. This is size of one record template, before binding the actual data.
     */
    public int getRecordTemplateSize() {
        //add delimeters count to the original record width.
        return super.getRecordTemplateSize() + getFieldCount();
    }
}	
