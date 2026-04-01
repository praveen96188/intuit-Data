/**
 * Helper.java
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

package com.paycycle.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Collection of type conversion and number-related methods
 */
public class TypeUtil {
    /**
     * Convert a hex encoded string to a byte array
     */
    public static byte[] hexToByteArray(String s) {
        if (s == null || (s.length() % 2) > 0) {
            return null;
        }

        int i, j;
        byte[] b = new byte[s.length() / 2];
        for (i = 0, j = 0; i < s.length(); i = i + 2, j++) {
            b[j] = (byte) Integer.parseInt(s.substring(i, i + 2), 16);
        }

        return b;
    }


    /**
     * Convert a hex encoded string to a byte array
     */
    public static String byteArrayToHex(byte[] b) {
        StringBuffer sb = new StringBuffer();
        String tmp;
        for (int i = 0; i < b.length; i++) {
            tmp = Integer.toHexString(b[i] & 0xFF).toUpperCase();
            sb.append(tmp.length() < 2 ? "0" + tmp : tmp);
        }

        return sb.toString();
    }

    /**
     * Converts a string to BigDecimal with 2 decimal places.
     */
    public static BigDecimal toMoney(String str) {
        return toMoney(Double.parseDouble(str));
    }

    /**
     * Converts a double to BigDecimal with 2 decimal places.
     */
    public static BigDecimal toMoney(double val) {
        DecimalFormat df = new DecimalFormat("#########.######");
        BigDecimal result = new BigDecimal(df.format(val));
        return toMoney(result);
    }

    /**
     * Converts an int to BigDecimal with 2 decimal places.
     */
    public static BigDecimal toMoney(int val) {
        BigDecimal result = new BigDecimal((new Integer(val)).doubleValue());
        return toMoney(result);
    }

    /**
     * Converts a BigDecimal to 2 decimal places.
     */
    public static BigDecimal toMoney(BigDecimal value) {
        return value.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Converts a double to BigDecimal with 0 decimal places.
     *
     * @param val a double value
     * @return the nearest whole dollar
     */
    public static BigDecimal toDollar(double val) {
        return toDollar(new BigDecimal(val));
    }

    /**
     * Converts an int to BigDecimal with 0 decimal places.
     *
     * @param val an int value
     * @return the nearest whole dollar
     */
    public static BigDecimal toDollar(int val) {
        return toDollar(new BigDecimal((new Integer(val)).doubleValue()));
    }

    /**
     * Set the BigDecimal to the nearest whole dollar.
     *
     * @param val the input value
     * @return the nearest whole dollar
     */
    public static BigDecimal toDollar(BigDecimal val) {
        return val.setScale(0, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Returns the double value of this object.  Typical use in deciphering
     * the double value of a Wddx value.
     */
    public static double doubleValue(Object obj) {
        if (obj == null) {
            return 0;
        } else if (obj instanceof String) {
            return Double.parseDouble((String) obj);
        } else if (obj instanceof Double) {
            return ((Double) obj).doubleValue();
        }

        return 0;
    }

    /**
     * primitive type conversion
     */
    public static boolean toBoolean(Object o) {
        if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue();
        }

        if (Helper.isEmpty(o)) {
            return false;
        }

        final String oString = o.toString();
        if (oString.equalsIgnoreCase("true") || oString.equalsIgnoreCase("yes") || oString.equalsIgnoreCase("1") || oString.equalsIgnoreCase("on")) {
            return true;
        }

        if (oString.equalsIgnoreCase("false") || oString.equalsIgnoreCase("no") || oString.equalsIgnoreCase("0") || oString.equalsIgnoreCase("off")) {
            return false;
        }

        return new Boolean(oString).booleanValue();
    }

    public static byte toByte(Object o) {
        if (o instanceof Number) {
            return ((Number) o).byteValue();
        } else if (Helper.isEmpty(o)) {
            return 0;
        } else {
            return Byte.parseByte(o.toString());
        }
    }

    public static short toShort(Object o) {
        if (o instanceof Number) {
            return ((Number) o).shortValue();
        } else if (Helper.isEmpty(o)) {
            return 0;
        } else {
            return Short.parseShort(o.toString());
        }
    }

    public static Integer toInteger(int value) {
        return new Integer(value);
    }

    public static int toInt(Object o) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        } else if (Helper.isEmpty(o)) {
            return 0;
        } else {
            return Integer.parseInt(o.toString());
        }
    }

    public static long toLong(Object o) {
        if (o instanceof Number) {
            return ((Number) o).longValue();
        } else if (Helper.isEmpty(o)) {
            return 0;
        } else {
            return Long.parseLong(o.toString());
        }
    }

    public static float toFloat(Object o) {
        if (o instanceof Number) {
            return ((Number) o).floatValue();
        } else if (Helper.isEmpty(o)) {
            return 0;
        } else {
            return Float.parseFloat(o.toString());
        }
    }

    public static double toDouble(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else if (Helper.isEmpty(o)) {
            return 0;
        } else {
            return Double.parseDouble(o.toString());
        }
    }

    public static BigDecimal toBigDecimal(Object o) {
        if (Helper.isEmpty(o)) {
            return new BigDecimal(0);
        } else if (o instanceof BigDecimal) {
            return (BigDecimal) o;
        } else {
            return new BigDecimal(o.toString());
        }
    }

    public static Date toDate(Object o) throws ParseException {
        if (o instanceof Date) {
            return (Date) o;
        } else {
            return DateUtil.parseDate(o.toString());
        }
    }

    /**
     * Set the BigDecimal to the nearest whole amount, rounding down.
     *
     * @param val the input value
     * @return the nearest whole amount, rounding down
     */
    public static BigDecimal roundDown(BigDecimal val) {
        return val.setScale(0, BigDecimal.ROUND_FLOOR);
    }

    /**
     * Set the double to the nearest whole amount, rounding down.
     *
     * @param doubleVal the input value
     * @return the nearest whole amount, rounding down
     */
    public static double roundDown(double doubleVal) {
        return new BigDecimal(doubleVal).setScale(0, BigDecimal.ROUND_FLOOR).doubleValue();
    }

    /**
     * Set the BigDecimal to the nearest whole amount, rounding up.
     *
     * @param val the input value
     * @return the nearest whole amount, rounding up
     */
    public static BigDecimal roundUp(BigDecimal val) {
        return val.setScale(0, BigDecimal.ROUND_CEILING);
    }

    /**
     * Set the BigDecimal to the nearest whole amount.
     *
     * @param val the input value
     * @return the nearest whole amount
     */
    public static BigDecimal roundNearest(BigDecimal val) {
        return val.setScale(0, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Converts a double to BigDecimal with 0 decimal places.
     *
     * @param val a double value
     * @return the nearest whole amount
     */
    public static BigDecimal roundUp(double val) {
        return roundUp(new BigDecimal(val));
    }

    /**
     * Converts an integer to BigDecimal with 0 decimal places.
     * This method is useful if you are calculating on the fly and don't
     * want to have to convert a value before passing to roundUp.
     *
     * @param val an integer value
     * @return the nearest whole amount
     */
    public static BigDecimal roundUp(int val) {
        return roundUp(new BigDecimal(val));
    }

    public static BigDecimal roundDown(int val) {
        return roundDown(new BigDecimal(val));
    }

    /**
     * Converts a BigDecimal to 3 decimal places...anything more than 4 decimal places and our database will display hours as "1E-4" which causes problem in our code
     */
    public static BigDecimal toHour(BigDecimal value) {
        return value.setScale(3, BigDecimal.ROUND_FLOOR);
    }

    /*
      * Returns the negative number, taking care not to return -0.00.
      */

    public static Double flipDouble(double amount) {
        double result;

        if (amount == 0.00) {
            // I know this looks crazy, but it solves the -0.00 problem.
            result = 0.00;
        } else {
            result = -amount;
        }

        return new Double(result);
    }


    public static int roundUpToInt(BigDecimal amount) {
        return roundUp(amount).intValue();
    }

    public static String toEnglish(int nonNegativeInteger) {
        final String[] toTwenty = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty"};
        final String[] tens = {"zero", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety", "one-hundred"};

        if (nonNegativeInteger >= 0 && nonNegativeInteger <= 20) {
            return toTwenty[nonNegativeInteger];
        } else if (nonNegativeInteger <= 100) {
            if (nonNegativeInteger % 10 == 0) {
                return tens[nonNegativeInteger / 10];
            } else {
                return tens[nonNegativeInteger / 10] + "-" + toTwenty[nonNegativeInteger % 10];
            }
        } else {
            throw new RuntimeException("Only non-negative integers less than or equal to 100 are supported.");
        }
    }

    /**
     * Create a map from a list where the key for each map entry is a function of the list element and the value is the list element
     *
     * @param list
     * @param keyFunctionOfListElement function that returns key as a function of the list element
     * @return
     */
    public static Map toMap(List list, CollectionUtil.OneArgumentFunction keyFunctionOfListElement) {
        Map result = new HashMap();
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            Object key = keyFunctionOfListElement.call(element);
            result.put(key, element);
        }
        return result;
    }
}
