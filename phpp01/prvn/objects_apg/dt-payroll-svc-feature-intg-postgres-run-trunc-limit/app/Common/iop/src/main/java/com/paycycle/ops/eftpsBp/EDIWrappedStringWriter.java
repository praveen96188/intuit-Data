/**
 * EDIWrappedStringWriter.java
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

package com.paycycle.ops.eftpsBp;

import java.io.IOException;
import java.io.Writer;


/**
 * Extends Writer and is similar to StringWriter, except that it formats
 * its output in the EDI "80-byte wrapped" format, which just means that
 * it inserts line breaks every 80 characters.  If a line is already broken
 * then it resets its count so that the next line will be a max of 80
 * chars.
 * <p/>
 * You can also create one with a different wrap width and newline sequence.
 */
public class EDIWrappedStringWriter extends Writer {
    private StringBuffer mBuf;
    private String mWrapSequence;
    private int mWidth;
    private int mWrapCount = 0;

    public EDIWrappedStringWriter() {
        this("\r\n", 80);
    }

    public EDIWrappedStringWriter(int width) {
        this("\r\n", width);
    }

    public EDIWrappedStringWriter(String wrapSequence, int width) {
        mBuf = new StringBuffer();
        lock = mBuf;
        mWrapSequence = wrapSequence;
        mWidth = width;
    }

    private void appendWrap() {
        mBuf.append(mWrapSequence);
        mWrapCount = 0;
    }

    private boolean nextCharsAreWrapSequence(char cbuf[], int start, int end) {
        for (int i = 0; ((i < mWrapSequence.length()) && ((start + i) < end)); i++) {
            if (mWrapSequence.charAt(i) != cbuf[start + i]) {
                return false;
            }
        }
        return true;
    }

    public void write(char cbuf[], int off, int len) {
        if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        int last = off + len;
        for (int cbufIndex = off; cbufIndex < last; cbufIndex++) {
            if (nextCharsAreWrapSequence(cbuf, cbufIndex, last)) {
                appendWrap();
                cbufIndex += mWrapSequence.length();
            } else {
                mBuf.append(cbuf[cbufIndex]);
                mWrapCount++;
                if (mWrapCount >= mWidth) {
                    appendWrap();
                }
            }
        }
    }

    /**
     * Return the buffer's current value as a string.
     */
    public String toString() {
        return mBuf.toString();
    }

    public void flush() {
    }

    public void close() throws IOException {
    }
}
