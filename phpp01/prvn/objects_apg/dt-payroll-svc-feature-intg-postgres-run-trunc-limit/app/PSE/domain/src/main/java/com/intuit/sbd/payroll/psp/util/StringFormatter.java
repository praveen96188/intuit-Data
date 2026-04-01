/*
 * $Id: //psp/dev/PSE/Domain/src/com/intuit/sbd/payroll/psp/util/StringFormatter.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.util;



import com.google.common.base.CharMatcher;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.portability.text.SpcfDateFormat;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.text.SpcfDateFormatImpl;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.math.BigDecimal;

public class StringFormatter {
    private static SpcfLogger logger = Application.getLogger(StringFormatter.class);

    public static String formatString(String inputStr, int fieldLength) {
        if (inputStr == null) {
            inputStr = "";
        }
        int strLength = inputStr.length();
        if (strLength > fieldLength) {
            return inputStr.substring(0, fieldLength).toUpperCase();
        } else {
            for (int i = strLength; i < fieldLength; i++) {
                inputStr += " ";
            }
            return inputStr.toUpperCase();
        }
    }

    public static String formatCurrencyNoDecimalPoint(BigDecimal value, int fieldLength) {
        // use round to correct inconsist double math...
        value = value.multiply(new BigDecimal(100));
        String inputStr = String.valueOf(value.longValue());

        int strLength = inputStr.length();
        if (strLength > fieldLength) {
            throw new RuntimeException("Currency amount greater than given field length," + " amount is: <" + value + ">");
        } else {
            for (int i = strLength; i < fieldLength; i++) {
                inputStr = "0" + inputStr;
            }
            return inputStr;
        }
    }

    public static String formatCurrency(BigDecimal value, int fieldLength) {
        String inputStr = String.valueOf(value);

        int strLength = inputStr.length();
        if (strLength > fieldLength) {
            throw new RuntimeException("Currency amount greater than given field length," + " amount is: <" + value + ">");
        } else {
            for (int i = strLength; i < fieldLength; i++) {
                inputStr = "0" + inputStr;
            }
            return inputStr;
        }
    }

    /**
     * Pads the value to the given fieldLength with 0s to the left
     *
     * @param value       Value to be formatted
     * @param fieldLength Length the value must have
     * @return Formatted long
     */
    public static String formatLong(long value, int fieldLength) {
        String inputStr = String.valueOf(value);

        int strLength = inputStr.length();
        if (strLength > fieldLength) {
            return inputStr.substring(0, fieldLength);
        } else {
            for (int i = strLength; i < fieldLength; i++) {
                inputStr = "0" + inputStr;
            }
            return inputStr;


        }
    }

    public static String formatString(String pInputStr, int pFieldLength, char pPaddingChar, boolean pPaddingLeft) {
        StringBuffer str = new StringBuffer(pInputStr);
        int strLength = str.length();
        if (pFieldLength > 0 && pFieldLength > strLength) {
            for (int i = 0; i <= pFieldLength; i++) {
                if (pPaddingLeft) {
                    if (i < pFieldLength - strLength) str.insert(0, pPaddingChar);
                } else {
                    if (i > strLength) str.append(pPaddingChar);
                }
            }
        }
        return str.toString();
    }
    
    public static String formatDate(SpcfCalendar date, String format) {
        SpcfDateFormat dateFormat = new SpcfDateFormatImpl(null);
        dateFormat.setPattern(format);
        return dateFormat.format(date);
    }

    public static String prependLeadingZeros(String str, int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length - str.length(); i++) {
            sb.append("0");
        }
        sb.append(str);
        return (sb.toString());
    }

    public static String removeNonAsciiCharacters(String input) {
        if (input == null)
            return input;
        try {
            return CharMatcher.ascii().retainFrom(input);
        } catch (Exception e) {
            logger.error("Exception occured while removing non ascii characters", e);
        }
        return input;
    }
}
