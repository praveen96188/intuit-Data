package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.spc.foundations.portability.util.SpcfDecimal;

/**
 * Created by Ankit on 7/13/2015.
 */
public class ReportUtils {

    /**
     * Pads a whole integer with the specified number of digits
     *
     * @param number The number to pad
     * @param size   The number of digits
     * @return The padded number
     */
    public static String getPaddedWholeNumber(int number, int size) {
        return ReportUtils.leftPad(String.valueOf(number), "0", size);
    }

    /**
     * Pads a number with an implied decimal point
     *
     * @param number     The number to pad
     * @param dollarSize The size of dollars to pad
     * @param centsSize  The size of cents to pad
     * @return A number with an implied decimal point
     */
    public static String getPaddedMoney(SpcfDecimal number, int dollarSize, int centsSize) {
        return getPaddedMoney(number, dollarSize, centsSize, false);
    }

    /**
     * Pads a number with a decimal point
     *
     * @param number     The number to pad
     * @param dollarSize The size of dollars to pad
     * @param centsSize  The size of cents to pad
     * @param addPeriod  Whether or not to imply the decimal place
     * @return The padded number
     */
    public static String getPaddedMoney(SpcfDecimal number, int dollarSize, int centsSize, boolean addPeriod) {
        String paddedMoney = ReportUtils.leftPad(String.valueOf(number.getIntegerPart()), "0", dollarSize);

        if (addPeriod) {
            paddedMoney += ".";
        }

        if (centsSize != 0) {
            paddedMoney += ReportUtils.leftPad(String.valueOf(number.getFractionalPart()), "0", centsSize);
        }

        return paddedMoney;
    }

    /**
     * Pads a number with an implied decimal point and a positive/negative sign.  The positive is simple a space " ".
     *
     * @param number     The number to pad
     * @param dollarSize The size of dollars to pad
     * @param centsSize  The size of cents to pad
     * @return A number with an implied decimal point and a positive/negative sign
     */
    public static String getPaddedMoneyWithSign(SpcfDecimal number, int dollarSize, int centsSize) {
        String paddedMoneyWithSign = number.getSign() == -1 ? "-" : " ";
        paddedMoneyWithSign += getPaddedMoney(number, dollarSize, centsSize);
        return paddedMoneyWithSign;
    }

    /**
     * Crops or pads a string depending on the specified string size
     *
     * @param string The string to crop or pad
     * @param size   The maximum string size to crop or pad to
     * @return The cropped or padded string
     */
    public static String cropOrPad(String string, int size) {
        if (string.length() > size) {
            return string.substring(0, size);
        } else {
            return ReportUtils.rightPad(string, " ", size);
        }
    }

    /**
     * Left pads a number with zeros.  If an id is too long, the return value is null
     *
     * @param input The input string to check
     * @param size  The maximum size
     * @return The padded number or null if too long
     */
    public static String padAndSizeCheck(String input, int size) {
        if (input.length() > size) {
            return null;
        } else if (input.length() < size) {
            input = ReportUtils.leftPad(input, "0", size);
        }

        return input;
    }

    /**
     * leftPad("345","0",6) = "000345"
     *
     * @param string
     * @param padCharacter      must be string of length 1
     * @param finalStringLength
     * @return original string if greater than or equal to finalStringLength
     */
    public static String leftPad(String string, String padCharacter, int finalStringLength) {
        int stringLength = string.length();
        if (stringLength < finalStringLength) {
            int padCharacterCount = finalStringLength - stringLength;
            StringBuilder stringBuffer = new StringBuilder(finalStringLength);
            for (int i = 0; i < padCharacterCount; i += 1) {
                stringBuffer.append(padCharacter);
            }
            stringBuffer.append(string);
            return stringBuffer.toString();
        } else {
            return string;
        }
    }

    /**
     * rightPad("345","0",6) = "345000"
     *
     * @param string
     * @param padCharacter      must be string of length 1
     * @param finalStringLength
     * @return original string if greater than or equal to finalStringLength
     */
    public static String rightPad(String string, String padCharacter, int finalStringLength) {
        int stringLength = string.length();
        if (stringLength < finalStringLength) {
            int padCharacterCount = finalStringLength - stringLength;
            StringBuilder stringBuffer = new StringBuilder(finalStringLength);
            stringBuffer.append(string);
            for (int i = 0; i < padCharacterCount; i += 1) {
                stringBuffer.append(padCharacter);
            }
            return stringBuffer.toString();
        } else {
            return string;
        }
    }
}
