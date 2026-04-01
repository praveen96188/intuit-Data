/**
 * FieldTemplate.java
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

import com.paycycle.fixedlen.FieldTemplate;
import com.paycycle.util.Helper;
import com.paycycle.util.StringUtil;
import com.paycycle.util.TypeUtil;

/**
 * The variable length Field template class
 */
public class DelimFieldTemplate extends FieldTemplate {
    protected boolean mDelimiterReqd = false;
    protected String mValueReplace;
    protected String mValueReplaceWith;
    protected boolean mStripLeadingZeros;
    protected String mConstantValue;
    protected boolean mTrimWhiteSpaces = false;
    protected boolean mEnableFormatting = false;

    public DelimFieldTemplate() {
        mSize = -1;
    }

    protected DelimFieldTemplate(final DelimFieldTemplate pSource) {
        super(pSource);
        mDelimiterReqd = pSource.mDelimiterReqd;
        mValueReplace = pSource.mValueReplace;
        mValueReplaceWith = pSource.mValueReplaceWith;
        mStripLeadingZeros = pSource.mStripLeadingZeros;
        mConstantValue = pSource.mConstantValue;
    }

    @Override
    public DelimFieldTemplate clone() {
        return new DelimFieldTemplate(this);
    }

    public void setDelimiterReqd(String d) {
        mDelimiterReqd = TypeUtil.toBoolean(d);
    }

    public boolean getDelimiterReqd() {
        return mDelimiterReqd;
    }

    public String getFormattedValue() {
        if (mConstantValue != null) {
            return mConstantValue;
        }

        // Default value is all blank
        if (mValue == null || mValue.length() < 0) {
            mValue = new StringBuffer(1);
            mValue.append("");
        } else if (mValueType.equals("boolean")) {
            if ((mValue.toString()).equals("X")) {
                mValue = new StringBuffer("1");
            } else {
                mValue = new StringBuffer("0");
            }
        }

        String result;

        if (mEnableFormatting) {
            result = super.getFormattedValue();

            if(mTrimWhiteSpaces){
                result = result.trim();
            }
            // Ed - I tried to do a generic number format but we're using ResourceHandler which has special meanings for values that start with #
            if (mValueType.equals("numeric") && mStripLeadingZeros) {
                result = StringUtil.leftTrim(result, '0');
                if (result.compareTo("") == 0) {
                    result = "0";
                }
            }
        } else {
            result = mValue.toString();
        }

        if (Helper.isNotEmpty(mValueReplace)) {
            result = result.replaceAll(mValueReplace, mValueReplaceWith);
        }

        return result;
    }

    public String getValueReplace() {
        return mValueReplace;
    }

    public void setValueReplace(String pString) {
        mValueReplace = pString;
    }

    public String getValueReplaceWith() {
        return mValueReplaceWith;
    }

    public void setValueReplaceWith(String pString) {
        mValueReplaceWith = pString;
    }

    public boolean getStripLeadingZeros() {
        return mStripLeadingZeros;
    }

    public void setStripLeadingZeros(String pString) {
        mStripLeadingZeros = TypeUtil.toBoolean(pString);
    }

    public String getConstantValue() {
        return mConstantValue;
    }

    public void setConstantValue(String pValue) {
        mConstantValue = pValue;
    }

    public boolean getTrimWhiteSpaces() {
        return mTrimWhiteSpaces;
    }

    public boolean getEnableFormatting() {
        return mEnableFormatting;
    }

    public void setTrimWhiteSpaces(String pString) {
        this.mTrimWhiteSpaces = TypeUtil.toBoolean(pString);
    }

    public void setEnableFormatting(String pString) {
        this.mEnableFormatting = TypeUtil.toBoolean(pString);
    }
}
