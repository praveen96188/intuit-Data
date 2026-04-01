/**
 * EDIRecordTemplate.java
 *
 * Copyright (c) 2007 PayCycle, Inc. All Rights Reserved.
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

package com.paycycle.eftpsBp;

import com.paycycle.delimlen.DelimRecordTemplate;
import com.paycycle.fixedlen.FieldTemplate;
import com.paycycle.fixedlen.RecordManagerException;

import java.io.IOException;
import java.io.Reader;

/**
 * This class is similar to a DelimRecordTemplate, except that EDI files have
 * "segments" instead of lines.  An EDI file also has newlines, but they're
 * completely irrelevant to the data, they simply break the file at every
 * 80 characters.  Except files meant for testing, which have newlines after
 * each segment also.  So... the best thing to do with newlines when reading
 * is to completely ignore/discard them.  That way you don't have to worry
 * about whether your file is a test or production file.  The segment separators
 * are significant though.  These are usually a backslash.  It's necessary to
 * find them, because unlike a DelimFieldRecord, you can end a segment with
 * a backslash and that means that there will be no more fields in that segment.
 * <p/>
 * So the functionality of the reading for an EDI file is similar to a
 * DelimFieldRecord except that we discard all newlines, and we end segments
 * with a backslash and then tokenize based on the field delimiter.
 */
public class EDIRecordTemplate extends DelimRecordTemplate {
    protected StringBuilder mRecordBuffer = new StringBuilder();

    public EDIRecordTemplate() {
    }

    protected EDIRecordTemplate(final EDIRecordTemplate pSource) {
        super(pSource);

        // Fields will be cloned in base class, so just copy source record buffer (no need to parse into fields)
        mRecordBuffer = new StringBuilder(pSource.mRecordBuffer);
    }

    @Override
    public EDIRecordTemplate clone() {
        return new EDIRecordTemplate(this);
    }

    public StringBuilder getRecordBuffer() {
        return mRecordBuffer;
    }

    public void setRecordBuffer(StringBuilder pRecordBuffer) {
        if (pRecordBuffer == mRecordBuffer) {
            return;
        }

        mRecordBuffer = pRecordBuffer;

        parseRecord();
    }

    public void setRecordBuffer(EDIRecordTemplate pSourceRecord) {
        if (pSourceRecord == this) {
            return;
        }

        setRecordBuffer(new StringBuilder(pSourceRecord.getRecordBuffer()));
    }

    protected void parseRecord() {
        //
        // Assign the delimited field values to their corresponding Field objects
        //
        String[] tokens = mRecordBuffer.toString().replace(RecordId.EDI_SEGMENT_SEP, "").split(getDelimiter());

        for (int fieldIndex = 0; fieldIndex < mFields.size(); ++fieldIndex) {
            FieldTemplate field = mFields.elementAt(fieldIndex);

            //
            // Don't want to assign data to segment separator field
            //
            if (field.getId() != FieldId.SEGMENT_SEPARATOR) {
                //
                // While we have them, assign the token to the field (empty tokens are represented as "" from split)
                //
                field.setValue((tokens.length > fieldIndex) ? tokens[fieldIndex] : "");
            }
        }
    }

    public int read(Reader pReader) throws IOException, RecordManagerException {
        return read(pReader, 1024); // default read limit to 1k
    }

    public int read(Reader pReader, int pRecordReadLimit) throws IOException, RecordManagerException {
        char buf[] = new char[1];
        int readLimit = pRecordReadLimit;

        //
        // Create a new record buffer for each read operation
        // (allows others to cache previously read records via getRecordBuffer, if desired)
        //
        StringBuilder buffer = new StringBuilder();

        //
        // Hopefully the Reader is a BufferedReader for efficiency.
        //
        while (pReader.read(buf, 0, 1) > 0) {
            //
            // Skip newline chars and continue reading
            //
            if ("\r\n".indexOf(buf[0]) < 0) {
                buffer.append(buf[0]);

                //
                // If we hit the end of the segment, stop reading
                //
                if (RecordId.EDI_SEGMENT_SEP.indexOf(buf[0]) >= 0) {
                    break;
                } else if (--readLimit < 0) {
                    //
                    // Assert a read limit as a defensive measure to stop junk files from overwhelming the system.
                    //
                    throw new RuntimeException(String.format("Record read limit exceeded (%d)", pRecordReadLimit));
                }
            }
        }

        //
        // Populate the fields with the new record data (trimming any whitespace)
        //
        setRecordBuffer(new StringBuilder(buffer.toString().trim()));

        return mRecordBuffer.length();
    }

    public static String peekNextSegCode(Reader pReader) throws IOException {
        // Do a quick read-ahead to determine if there's more data and what segment it is.
        final int BUF_SZ = 10;
        char buf[] = new char[BUF_SZ];

        pReader.mark(BUF_SZ);
        int amtRead = pReader.read(buf);
        pReader.reset();

        if (amtRead < 0) {
            return null; // reached EOF
        }

        // It's possible that the segment code got split by the line break. Remove those.
        String retVal = new String(buf).trim().replace("\r", "").replace("\n", "");

        if (retVal.length() < 1) {
            return null; // nothing but white space left
        } else {
            return retVal;
        }
    }
}
