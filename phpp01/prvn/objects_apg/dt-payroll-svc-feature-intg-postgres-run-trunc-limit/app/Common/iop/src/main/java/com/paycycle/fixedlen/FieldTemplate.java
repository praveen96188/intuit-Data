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

package com.paycycle.fixedlen;

import com.paycycle.gui.Field;
import com.paycycle.util.StringUtil;

import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * The ACH Field template class
 */
public class FieldTemplate extends Field {
    private static final Hashtable<String, String> CTSOverPunchReference = new Hashtable<String, String>();

    protected String mValueType;
    protected String mInclusion;
    protected String mDescription;
    protected StringBuffer mValue;
    protected String mMapping;

    static {
        CTSOverPunchReference.put("+0", "{");
        CTSOverPunchReference.put("+1", "A");
        CTSOverPunchReference.put("+2", "B");
        CTSOverPunchReference.put("+3", "C");
        CTSOverPunchReference.put("+4", "D");
        CTSOverPunchReference.put("+5", "E");
        CTSOverPunchReference.put("+6", "F");
        CTSOverPunchReference.put("+7", "G");
        CTSOverPunchReference.put("+8", "H");
        CTSOverPunchReference.put("+9", "I");
        CTSOverPunchReference.put("-0", "}");
        CTSOverPunchReference.put("-1", "J");
        CTSOverPunchReference.put("-2", "K");
        CTSOverPunchReference.put("-3", "L");
        CTSOverPunchReference.put("-4", "M");
        CTSOverPunchReference.put("-5", "N");
        CTSOverPunchReference.put("-6", "O");
        CTSOverPunchReference.put("-7", "P");
        CTSOverPunchReference.put("-8", "Q");
        CTSOverPunchReference.put("-9", "R");
    }

    public FieldTemplate() {
    }

    protected FieldTemplate(final FieldTemplate pSource) {
        super(pSource);
        mValueType = pSource.mValueType;
        mInclusion = pSource.mInclusion;
        mDescription = pSource.mDescription;
        mValue = (pSource.mValue == null) ? pSource.mValue : new StringBuffer(pSource.mValue);
        mMapping = pSource.mMapping;
    }

    @Override
    public FieldTemplate clone() {
        return new FieldTemplate(this);
    }

    public String getValueType() {
        return mValueType;
    }

    public void setValueType(String pValueType) {
        mValueType = pValueType;
    }

    public String getValue() {
        return mValue == null ? null : mValue.toString();
    }

    public String getFormattedValue() {
        // Default value is all blank or all zero for CTS file
        if (mValue == null || mValue.length() < 0) {
            mValue = new StringBuffer(mSize);
            if (isCTSFormat()) {
                getCTSFormat();
            } else {
                for (int i = 0; i < mSize; i++) {
                    mValue.append(' ');
                }
            }
        } else {
            char sign = '+';
            if (mValueType.equals("signedNumeric") && signedValue()) {
                sign = mValue.charAt(0);
                mValue.deleteCharAt(0);
            }
            // Format data
            if (mValueType.equals("$$$$$$$$CC") || mValueType.equals("$$$$$$$$$$CC") ||
                mValueType.equals("numeric") || mValueType.equals("signedNumeric")) {
                // Pre-fill numbers/amounts with zeros
                for (int i = mValue.length(); i < mSize; i++) {
                    mValue.insert(0, '0');
                }
            } else if (isCTSFormat()) {
                getCTSFormat();
            } else if (mValueType.equals("boolean")) {
                if ((mValue.toString()).equals("X")) {
                    mValue = new StringBuffer("1");
                } else {
                    mValue = new StringBuffer("0");
                }
            } else {
                // Post-fill alphanumeric/alphabetic with blanks.
                for (int i = mValue.length(); i < mSize; i++) {
                    mValue.append(' ');
                }
            }

            if (mValueType.equals("signedNumeric")) {
                mValue.setCharAt(0, sign);
            }
        }
        return mValue.toString();
    }

    private boolean signedValue() {
        return mValue.charAt(0) == '-' || mValue.charAt(0) == '+';
    }

    public boolean isCTSFormat() {
        return (mValueType.equals("S9(11)V99") || mValueType.equals("S9(7)V99") || mValueType.equals("S9(6)") ||
                mValueType.equals("S9(11)") || mValueType.equals("S9(13)") || mValueType.equals("S9(26)") ||
                mValueType.equals("99V9999") || mValueType.equals("9(4)V99") || mValueType.equals("9(2)") ||
                mValueType.equals("9(3)") || mValueType.equals("9(4)") || mValueType.equals("9(5)") ||
                mValueType.equals("9(6)") || mValueType.equals("9(9)"));
    }

    public void getCTSFormat() {
        StringTokenizer st = new StringTokenizer(StringUtil.strip(mValue.toString(), "-"), ".");
        String ctsFormatValue = new String("");
        while (st.hasMoreTokens()) {
            ctsFormatValue += st.nextToken();
        }

        // prefill with zeros
        int strLength = ctsFormatValue.trim().length();
        for (int i = strLength; i < mSize; i++) {
            ctsFormatValue = "0" + ctsFormatValue;
        }

        if (mValueType.equals("S9(11)V99") || mValueType.equals("S9(6)") || mValueType.equals("S9(11)") ||
            mValueType.equals("S9(7)V99") || mValueType.equals("S9(13)") || mValueType.equals("S9(26)")) {
            BigDecimal val = new BigDecimal(mValue.toString().trim().equals("") ? "0" : mValue.toString());
            String lastChar = "" + ctsFormatValue.charAt(ctsFormatValue.length() - 1);
            ctsFormatValue = ctsFormatValue.substring(0, ctsFormatValue.length() - 1);
            if (val.doubleValue() < 0) {
                lastChar = "-" + lastChar;
            } else {
                lastChar = "+" + lastChar;
            }
            ctsFormatValue += FieldTemplate.CTSOverPunchReference.get(lastChar);
        }
        mValue = new StringBuffer(ctsFormatValue);
    }

    public void setValue(String pValue) throws RecordManagerException {
        if (pValue == null) {
            throw new RecordManagerException("Invalid value (" + pValue + ") for field " + mName);
        }

        // Truncate the value if needed
        // Depending on the type, we truncate on either the left or the right.
        if ((mSize != -1) && (pValue.length() > mSize)) {
            if (mValueType.equals("alphanumericTruncateLeft")) {
                mValue = new StringBuffer(pValue.substring(pValue.length() - mSize, pValue.length()));
            } else {
                mValue = new StringBuffer(pValue.substring(0, mSize));
            }
        } else {
            mValue = new StringBuffer(pValue);
        }
    }

    public void clearValue() throws RecordManagerException {
        if (mValueType.equals("numeric") || mValueType.equals("signedNumeric") || isCTSFormat()) {
            setValue("0");
        }
    }

    public String getInclusion() {
        return mInclusion;
    }

    public void setInclusion(String pInclusion) {
        mInclusion = pInclusion;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String pDescription) {
        mDescription = pDescription;
    }

    public void setMapping(String pMapping) {
        mMapping = pMapping;
    }

    public String getMapping() {
        return mMapping;
    }

    /**
     * Attempt to populate field
     * Replace index variable in mapping property
     */
    public void map(Map pData, int pIndex) throws RecordManagerException {
        map(pData, pIndex, null, false);
    }

    /**
     * There is a requirement of the source data value when using the accumulator
     * 1. The source data value string must be a valid constructor value for the BigDecimal type
     *
     * @param pData
     * @param pIndex
     * @param pNumericFieldAccumulator - only numeric fields will be accumulated
     * @throws RecordManagerException
     */
    public void map(Map pData, int pIndex, Map pNumericFieldAccumulator, boolean pPrependFieldNameToValue) throws RecordManagerException {
        if (pData != null && mMapping != null) {
            String key = mMapping.replaceAll("\\{i\\}", String.valueOf(pIndex));
            String value = pData.get(key).toString();
            if (value == null) {
                throw new RecordManagerException("value not found");
            } else {
                if (pNumericFieldAccumulator != null && mValueType.equalsIgnoreCase("numeric")) {
                    if (pNumericFieldAccumulator.containsKey(mName)) {
                        pNumericFieldAccumulator.put(mName, ((BigDecimal) pNumericFieldAccumulator.get(mName)).add(new BigDecimal(value.equals("") ? "0.0" : value)));
                    } else {
                        pNumericFieldAccumulator.put(mName, new BigDecimal(value.equals("") ? "0.0" : value));
                    }
                }
                if (pPrependFieldNameToValue) {
                    value = mName + value;
                }
                setValue(value.trim());
            }
        }
    }

    /**
     * Attempt to map field mapping specification with hashtable data.
     */
    public void map(Map pData) throws RecordManagerException {
        String v;
        if (pData != null && mMapping != null) {
            if ((v = (String) pData.get(mMapping)) == null) {
                return;
            }

            setValue(v.trim());
        }
    }

    /**
     * Attempt to map field mapping specification to hashtable data.
     */
    public void mapTo(Map pData) throws RecordManagerException {
        String v;
        if (pData != null && mMapping != null) {
            if ((v = getValue()) == null) {
                return;
            }

            pData.put(mMapping, v.trim());
        }
    }
}	
