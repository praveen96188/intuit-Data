/**
 * StringUtil.java
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

import com.ibm.numberspeller.NumberSpeller;
import com.paycycle.data.KeyedRecordModel;
import com.paycycle.model.DateModel;
import com.paycycle.model.ModelHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.CharacterIterator;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of String manipulation functions
 */
public class StringUtil {
    /**
     * the line separator for this OS
     */
    public static final String LINE_SEP = System.getProperty("line.separator");
    private static final char[] ALL_ALPHABETIC_CHARS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final char[] ALL_NUMERIC_CHARS = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
    private static final char[] ALL_ALPHANUMERIC_CHARS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
    // certain characters (such as "1," "l" and "I") are confusingly similar
    // in certain fonts, and are not suitable for random passwords.
    private static final char[] NON_AMBIGUOUS_ALPHANUMERIC_CHARS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '2', '3', '4', '5', '6', '7', '8', '9'};
    protected static String punctuation = "~!@#$%^&*()_-+={}[]:;<>,./?\\|'\"";
    protected static String STANDARD_ASCII = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    private final static Map<Integer, Character> NON_STANDARD_ASCII_MAPPING = new HashMap<Integer, Character>();

    static {
        NON_STANDARD_ASCII_MAPPING.put(138, 'S');
        NON_STANDARD_ASCII_MAPPING.put(142, 'Z');
        NON_STANDARD_ASCII_MAPPING.put(154, 's');
        NON_STANDARD_ASCII_MAPPING.put(158, 'z');
        NON_STANDARD_ASCII_MAPPING.put(159, 'Y');
        NON_STANDARD_ASCII_MAPPING.put(192, 'A');
        NON_STANDARD_ASCII_MAPPING.put(193, 'A');
        NON_STANDARD_ASCII_MAPPING.put(194, 'A');
        NON_STANDARD_ASCII_MAPPING.put(195, 'A');
        NON_STANDARD_ASCII_MAPPING.put(196, 'A');
        NON_STANDARD_ASCII_MAPPING.put(197, 'A');
        NON_STANDARD_ASCII_MAPPING.put(199, 'C');
        NON_STANDARD_ASCII_MAPPING.put(200, 'E');
        NON_STANDARD_ASCII_MAPPING.put(201, 'E');
        NON_STANDARD_ASCII_MAPPING.put(202, 'E');
        NON_STANDARD_ASCII_MAPPING.put(203, 'E');
        NON_STANDARD_ASCII_MAPPING.put(204, 'I');
        NON_STANDARD_ASCII_MAPPING.put(205, 'I');
        NON_STANDARD_ASCII_MAPPING.put(206, 'I');
        NON_STANDARD_ASCII_MAPPING.put(207, 'I');
        NON_STANDARD_ASCII_MAPPING.put(209, 'N');
        NON_STANDARD_ASCII_MAPPING.put(210, 'O');
        NON_STANDARD_ASCII_MAPPING.put(211, 'O');
        NON_STANDARD_ASCII_MAPPING.put(212, 'O');
        NON_STANDARD_ASCII_MAPPING.put(213, 'O');
        NON_STANDARD_ASCII_MAPPING.put(214, 'O');
        NON_STANDARD_ASCII_MAPPING.put(216, 'O');
        NON_STANDARD_ASCII_MAPPING.put(217, 'U');
        NON_STANDARD_ASCII_MAPPING.put(218, 'U');
        NON_STANDARD_ASCII_MAPPING.put(219, 'U');
        NON_STANDARD_ASCII_MAPPING.put(220, 'U');
        NON_STANDARD_ASCII_MAPPING.put(221, 'Y');
        NON_STANDARD_ASCII_MAPPING.put(224, 'a');
        NON_STANDARD_ASCII_MAPPING.put(225, 'a');
        NON_STANDARD_ASCII_MAPPING.put(226, 'a');
        NON_STANDARD_ASCII_MAPPING.put(227, 'a');
        NON_STANDARD_ASCII_MAPPING.put(228, 'a');
        NON_STANDARD_ASCII_MAPPING.put(229, 'a');
        NON_STANDARD_ASCII_MAPPING.put(232, 'e');
        NON_STANDARD_ASCII_MAPPING.put(233, 'e');
        NON_STANDARD_ASCII_MAPPING.put(234, 'e');
        NON_STANDARD_ASCII_MAPPING.put(235, 'e');
        NON_STANDARD_ASCII_MAPPING.put(236, 'i');
        NON_STANDARD_ASCII_MAPPING.put(237, 'i');
        NON_STANDARD_ASCII_MAPPING.put(238, 'i');
        NON_STANDARD_ASCII_MAPPING.put(239, 'i');
        NON_STANDARD_ASCII_MAPPING.put(241, 'n');
        NON_STANDARD_ASCII_MAPPING.put(242, 'o');
        NON_STANDARD_ASCII_MAPPING.put(243, 'o');
        NON_STANDARD_ASCII_MAPPING.put(244, 'o');
        NON_STANDARD_ASCII_MAPPING.put(245, 'o');
        NON_STANDARD_ASCII_MAPPING.put(246, 'o');
        NON_STANDARD_ASCII_MAPPING.put(249, 'u');
        NON_STANDARD_ASCII_MAPPING.put(250, 'u');
        NON_STANDARD_ASCII_MAPPING.put(251, 'u');
        NON_STANDARD_ASCII_MAPPING.put(252, 'u');
        NON_STANDARD_ASCII_MAPPING.put(253, 'y');
        NON_STANDARD_ASCII_MAPPING.put(255, 'y');
    }

    //----------------------------- Lists ------------------------------//

    /**
     * Turns a collection of objects into their string versions separated by separator.
     *
     * @return a string.
     */
    public static String concatenate(Collection collection, String separator) {
        if (collection == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        Iterator iter = collection.iterator();
        if (iter.hasNext()) {
            builder.append(iter.next().toString());
        }
        while (iter.hasNext()) {
            builder.append(separator);
            builder.append(iter.next().toString());
        }
        return builder.toString();
    }

    /**
     * Turns a collection of objects into their string versions separated by separator.
     *
     * @return a string.
     */
    public static String concatenate(Object[] objects, String separator) {
        if (objects == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();

        if (objects.length > 0) {
            buf.append(objects[0].toString());
        }

        for (int index = 1; index < objects.length; index++) {
            buf.append(separator);
            buf.append(objects[index].toString());
        }

        return buf.toString();
    }

    /**
     * Turns an array of string to a list delimited by commas.
     *
     * @return a string list delimited by commas.
     */
    public static String getList(String[] strArray) {
        if (strArray == null || strArray.length <= 0) {
            return null;
        }

        StringBuilder buf = new StringBuilder(strArray[0]);
        for (int i = 1; i < strArray.length; i++) {
            buf.append(",");
            buf.append(strArray[i]);
        }
        return buf.toString();
    }

    /**
     * Turns a list delimited by commas, semicolon, etc. to an array of string.
     *
     * @return a string array.
     */
    public static String[] getArray(String list, String delim) {
        if (Helper.isEmpty(list)) {
            return null;
        }

        StringTokenizer token = new StringTokenizer(list, delim);
        String[] strArray = new String[token.countTokens()];
        int i = 0;
        while (token.hasMoreElements()) {
            String str = (String) token.nextElement();
            if (!Helper.isEmpty(str)) {
                strArray[i++] = str;
            }
        }
        return strArray;
    }

    /**
     * Retreive the first item from a delimited list of items
     *
     * @param list  the source
     * @param delim the delimiter (can contain multiple delimiters)
     */
    public static String listFirst(String list, String delim) {
        return listGetAt(list, delim, 1);
    }

    /**
     * Retreive the indexed item from a delimited list of items
     *
     * @param list     the source
     * @param delim    the delimiter (can contain multiple delimiters)
     * @param position the 1-based index
     * @return the item, null if not found
     */
    public static String listGetAt(String list, String delim, int position) {
        StringTokenizer tok = new StringTokenizer(list, delim);

        int count = tok.countTokens();
        if (position <= 0 || position > count) {
            return null;
        }

        for (int i = 1; i <= count; i++) {
            String t = tok.nextToken();
            if (position == i) {
                return t;
            }
        }
        return null;
    }

    /**
     * Construct an ID list.
     *
     * @param list a list of KeyedRecordModel elements
     * @return a comma separated list of keyedRecord IDs
     */
    public static String getIdList(List list) {
        if (list == null || list.size() == 0) {
            return "";
        }

        StringBuilder answerList = new StringBuilder();
        int i, secondToLast;
        KeyedRecordModel aRecord;

        secondToLast = list.size() - 1;

        for (i = 0; i < secondToLast; i++) {
            aRecord = (KeyedRecordModel) list.get(i);
            answerList.append(aRecord.getId());
            answerList.append(",");
        }
        // last item without a trailing comma
        aRecord = (KeyedRecordModel) list.get(i);
        answerList.append(aRecord.getId());

        return answerList.toString();
    }

    public static boolean REMatch(String expr, String search, int startIdx) {
        return Pattern.matches(expr, search.substring(startIdx));
    }

    public static String REReplace(String expr, String str, String substr) {
        if (str == null) {
            return "";
        }
        if (expr == null || substr == null) {
            return str;
        }

        Pattern p = Pattern.compile(expr);
        Matcher m = p.matcher(str);
        return m.replaceAll(substr);
    }

    /**
     * Returns the list of matched tokens. Support parenthesized subexpression also.
     */
    public static Vector RETokens(String expr, String search, int g) {
        Vector tokens = new Vector();

        Pattern p = Pattern.compile(expr);
        Matcher m = p.matcher(search);
        int startIdx = 0;

        while (m.find(startIdx)) {
            if (m.groupCount() >= g) {
                tokens.add(m.group(m.groupCount()));
            }

            // Prepare for next match
            startIdx = m.start(m.groupCount());
        }
        return tokens;
    }

    /**
     * Determine if a string contains all digits (0-9)
     */
    public static boolean isAllDigits(String number) {
        if (Helper.isEmpty(number)) {
            return false;
        }

        for (int i = 0; i < number.length(); i++) {
            if (!Character.isDigit(number.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine if a string contains at least one letter
     */
    public static boolean containsLetters(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if a string can be parsed as a number
     */
    public static boolean isNumeric(String number) {
        try {
            Double.parseDouble(number);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    /**
     * Determine if a string is numeric with two decimal places. To limit
     * ambiguity, this requires that the string be all-numeric except for
     * the decimal point, and returns false if there is a dollar sign at the
     * beginning.
     * <p/>
     * This does consider strings like ".27" to be valid.
     */
    public static boolean isMoney(String s) {
        if (s.length() < 3) {
            return false;
        }
        if (!isNumeric(s)) {
            return false;
        }
        if (s.charAt(s.length() - 3) != '.') {
            return false;
        }
        return true;
    }

    /**
     * Determine if the passed string represents a sensitized value
     */
    public static boolean isSensitized(String str) {
        if (Helper.isEmpty(str)) {
            return true;
        }

        return str.startsWith("....");
    }


    /**
     * Sensitize the passed clear text.  Returns a string in the form of
     * ....XXXX
     */
    public static String sensitize(String clear) {
        if (isSensitized(clear)) {
            return clear;
        }

        int idx = clear.length() - 4;
        if (idx < 0) {
            idx = 0;
        }

        return "...." + clear.substring(idx);
    }


    /**
     * Parses a query string passed from the client to the
     * server and builds a <code>HashTable</code> object
     * with key-value pairs.
     */
    public static Properties parseQueryString(String s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }

        Properties p = new Properties();
        StringTokenizer st = new StringTokenizer(s, "&");
        while (st.hasMoreTokens()) {
            String pair = st.nextToken();
            int pos = pair.indexOf('=');
            if (pos == -1) {
                throw new IllegalArgumentException();
            }

            p.put(pair.substring(0, pos), pair.substring(pos + 1));
        }
        return p;
    }

    /**
     * Splits a string into an array of substrings, using a delimiter.  This provides the
     * functionality of the StringTokenizer class in a more convenient function form.
     * The characters in the delim argument are the delimiters for separating tokens.
     * Delimiter characters themselves will not be treated as tokens
     *
     * @param str   the string to be tokenized.
     * @param delim the delimiter(s)
     */
    public static String[] split(String str, String delim) {
        StringTokenizer st = new StringTokenizer(str, delim);
        String[] ret = new String[st.countTokens()];
        int ix = 0;
        while (st.hasMoreTokens()) {
            ret[ix++] = st.nextToken();
        }
        return ret;
    }

    /**
     * This method is essentially the same as split(), but it returns tokens which have had
     * leading and trailing whitespaces trimmed.
     *
     * @param str   the string to be tokenized.
     * @param delim the delimiter(s)
     */
    public static String[] splitAndTrim(String str, String delim) {
        StringTokenizer st = new StringTokenizer(str, delim);
        String[] ret = new String[st.countTokens()];
        int ix = 0;
        while (st.hasMoreTokens()) {
            ret[ix++] = st.nextToken().trim();
        }
        return ret;
    }

    /**
     * unsplits an array of strings.  This is the reverse of splitting a string.
     * This will essentially build a delim separated string from the array of strings.
     *
     * @param strings The array of strings to unsplit.
     * @param delim   The delimiter to separate the strings into.
     * @return The unsplit string or "" if the array was empty.
     */
    public static String unsplit(String[] strings, String delim) {
        if (strings == null || strings.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (strings[i] != null && strings[i].length() > 0) {
                if (i == strings.length - 1) {
                    result.append(strings[i]);
                } else {
                    result.append(strings[i]).append(delim);
                }
            }
        }
        return result.toString();
    }

    /**
     * Same as split that returns Array except returns a List.
     * This can be useful in bind xml where Array subscripting is not supported
     * I originally implemented using Arrays.asList but received access error from the bind xml
     */
    public static List<String> splitIntoList(String string, String delimiter) {
        StringTokenizer tokenizer = new StringTokenizer(string, delimiter);
        List<String> result = new ArrayList<String>(tokenizer.countTokens());
        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken());
        }
        return result;
    }

    /**
     * The reverse of splitIntoList(). Works with any object by calling its
     * toString() method.
     */
    public static String joinListIntoString(List list, String delimiter) {
        return joinListIntoString(new StringBuilder(), list, delimiter).toString();
    }

    public static StringBuilder joinListIntoString(StringBuilder stringBuilder, List list, String delimiter) {
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                stringBuilder.append(delimiter);
            }
            stringBuilder.append(list.get(i).toString());
        }
        return stringBuilder;
    }

    /**
     * Remove the specified character from the passed string and return
     * the new trimmed string.
     */
    public static String strip(String src, String delim) {
        StringTokenizer tok = new StringTokenizer(src, delim);
        StringBuilder des = new StringBuilder(src.length());
        while (tok.hasMoreTokens()) {
            des.append(tok.nextToken());
        }

        return des.toString();
    }

    /**
     * Remove all non standard ASCII text and replace with space or equivalent alphabet
     *
     * @param string
     * @return
     */
    public static String stripNonStandardASCII(String string) {
        StringBuilder des = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); i++) {
            char current = string.charAt(i);
            int asciiValue = (int) current;
            if (isStandardASCII(current)) {
                des.append(current);
            } else if (NON_STANDARD_ASCII_MAPPING.containsKey(asciiValue)) {
                des.append(NON_STANDARD_ASCII_MAPPING.get(asciiValue));
            } else {
                des.append(' ');
            }
        }
        String strippedString = des.toString();
        // trim leading and ending spaces if it's not all spaces
        if (isWhitespace(strippedString)) {
            return " ";
        } else {
            return rightTrim(leftTrim(strippedString, ' '), ' ');
        }
    }

    /**
     * Remove all leading and trailing spaces.
     */
    public static String stripLeadingAndTrailinSpaces(String string) {
        return rightTrim(leftTrim(string, ' '), ' ');
    }

    /**
     * Remove all punctuation text and leading, trailing and adjacent spaces.
     *
     * @param string
     * @return
     */
    public static String stripPunctuation(String string) {
        StringBuilder des = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); i++) {
            if (Character.isLetterOrDigit(string.charAt(i)) || Character.isWhitespace(string.charAt(i))) {
                des.append(string.charAt(i));
            }
        }

        // let's trim out all leading, trailing and adjacent spaces
        StringTokenizer st = new StringTokenizer(des.toString(), " ");
        int totalCount = st.countTokens();
        StringBuilder retString = new StringBuilder();
        int ix = 0;
        while (st.hasMoreTokens()) {
            retString.append(st.nextToken() + (ix == totalCount - 1 ? "" : " "));
            ix++;
        }

        return retString.toString();
    }

    /**
     * Remove all non alpha, non numeric characters
     *
     * @param string
     * @return
     */
    public static String stripNonAlphaAndNonNumeric(String string) {
        StringBuilder des = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); i++) {
            if (Character.isLetterOrDigit(string.charAt(i))) {
                des.append(string.charAt(i));
            }
        }

        return des.toString();
    }

    /**
     * Remove the non-numeric character from the passed string and return
     * the new trimmed string.
     */
    public static String stripNonNumeric(String src) {
        StringBuilder des = new StringBuilder(src.length());
        for (int i = 0; i < src.length(); i++) {
            if (Character.isDigit(src.charAt(i))) {
                des.append(src.charAt(i));
            }
        }

        return des.toString();
    }

    /**
     * Remove the leading zeros from the passed string and return
     * the new trimmed string.
     */
    public static String stripLeadingZeros(String src) {
        int index = 0;
        for (; index < src.length(); index++) {
            if (src.charAt(index) != '0') {
                break;
            }
        }
        return index == 0 ? src : src.substring(index);
    }

    public static String stripNewLine(String addr1) {
        if (addr1 == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();
        int len = addr1.length();
        for (int i = 0; i < len; i++) {
            int ic = addr1.charAt(i);
            if (ic != 10 && ic != 13) {
                buf.append(addr1.charAt(i));
            } else {
                buf.append(' ');
            }
        }

        return buf.toString();
    }

    /*
     * Condenses all white space to a single space.  White space includes
     * tabs, CR & LF characters.  Ex. "  \r Test\tString\r\n" becomes 
     * "Test String".
     */

    public static String condenseWhiteSpace(String addr1) {
        if (addr1 == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();
        int len = addr1.length();
        boolean prevWasWhite = true;
        for (int i = 0; i < len; i++) {
            int ic = addr1.charAt(i);
            if (ic == 9 || ic == 10 || ic == 13 || ic == 32) {
                if (!prevWasWhite) {
                    buf.append(" ");
                }
                prevWasWhite = true;
            } else {
                buf.append(addr1.charAt(i));
                prevWasWhite = false;
            }
        }

        // Trim last space if one was added.
        len = buf.length();
        if (len >= 1 && buf.charAt(len - 1) == 32) {
            buf.deleteCharAt(len - 1);
        }

        return buf.toString();
    }

    /**
     * Replace sub1 with sub2 in src string, case insensitive search
     */
    public static String replace(String src, String sub1, String sub2) {
        int index = src.toUpperCase().indexOf(sub1.toUpperCase());

        if (index < 0) {
            return src;
        }

        StringBuffer buf = new StringBuffer(src);

        return buf.replace(index, index + sub1.length(), sub2).toString();
    }

    /**
     * Determine if the string contains only whitespace.
     */
    public static boolean isWhitespace(String buf) {
        for (int i = 0; i < buf.length(); i++) {
            if (!Character.isWhitespace(buf.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks to see whether a string is empty or null.
     */
    public static boolean isNullOrEmpty(String s) {
        if (null == s) {
            return true;
        }
        if (0 == s.length()) {
            return true;
        }

        return false;
    }

    //----------------------------- Formatting ------------------------------//

    /**
     * Wrapper for default format.
     */
    public static String format(Object o) {
        return format(o, null);
    }

    /**
     * Format the object with default formatter.
     */
    public static String format(Object o, String format) {
        String defaultDecimalFormat = "#####0.00";
        if (Helper.isEmpty(o)) {
            return "";
        }

        if (format != null && o instanceof Number) {
            if (format.equalsIgnoreCase("dollar")) {
                return numberFormat(o, "\u00A4###,##0.00");
            } else if (format.equalsIgnoreCase("dollars")) {
                String str = numberFormat(o, defaultDecimalFormat);
                return str.substring(0, str.length() - 3);
            } else if (format.equalsIgnoreCase("cents")) {
                String str = numberFormat(o, defaultDecimalFormat);
                return str.substring(str.length() - 2, str.length());
            } else {
                return numberFormat(o, format);
            }
        }

        if (format != null && o instanceof String) {
            if (format.equalsIgnoreCase("numbers")) {
                return REReplace("[^0-9]", (String) o, "");
            }
        }

        if (o instanceof Date) {
            return DateUtil.dateFormat(o, format == null ? "MM/dd/yyyy" : format);
        } else if (o instanceof DateModel) {
            return DateUtil.dateFormat(ModelHelper.toDate((DateModel) o), format == null ? "MM/dd/yyyy" : format);
        } else if (o instanceof Double || o instanceof BigDecimal) {
            return numberFormat(o, format == null ? defaultDecimalFormat : format);
        } else {
            return o.toString();
        }
    }

    /**
     * Format a number according to the passed format string
     */
    public static String numberFormat(Object num, String format) {
        if (num == null || num instanceof String && ((String) num).length() <= 0) {
            return "";
        }

        NumberFormat nf = NumberFormat.getInstance();
        ((DecimalFormat) nf).setMaximumFractionDigits(2);
        ((DecimalFormat) nf).setMinimumFractionDigits(2);
        ((DecimalFormat) nf).applyPattern(format);

        return ((DecimalFormat) nf).format(num);
    }

    /**
     * Format a phone number base on a pattern
     *
     * @param phoneNumber - valid digits only (0-9), non-digit char will be ignored.
     * @param newPattern  - only char # will be replace with phone number, if null then default pattern will be used.
     * @return formatted phone number
     */
    public static String phoneNumberFormat(String pNumber, String newPattern) {
        String pattern = "###-###-####";  //default pattern
        StringBuffer sb = new StringBuffer();
        int k = 0;
        int totalDigits = 0;
        String phoneNumber = new String();
        if (pNumber != null) {
            // remove all the non digit number first
            phoneNumber = pNumber.replaceAll("\\D", "");
            totalDigits = phoneNumber.length();
        }
        if (newPattern != null) {
            pattern = newPattern;
        }

        if (phoneNumber.length() == 0) {
            return phoneNumber;
        }

        // format the phone number based on pattern
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '#') {
                if (k < totalDigits) {
                    sb.append(phoneNumber.charAt(k));
                    k++;
                }
            } else {
                sb.append(pattern.charAt(i));
            }
        }
        return sb.toString();
    }

    /**
     * Parse all the phones digits
     *
     * @param pNumber - a phone number string contains both digit and non-digit characters.
     * @return a string contains all phone digits
     */
    public static String parsePhoneDigits(String pNumber) {
        String phoneNumberDigits = new String();
        if (pNumber != null) {
            // remove all the non digit number first
            phoneNumberDigits = pNumber.replaceAll("\\D", "");
        }
        return phoneNumberDigits;
    }

    /**
     * Converts a Throwable to a printable stack trace
     *
     * @param t any Throwable, or null
     */
    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);

        if (t != null) {
            t.printStackTrace(w);
        }

        w.flush();    // couldn't hurt...
        return sw.toString();
    }

    /**
     * Any number is converted into equivalent sentence. Only integer portion is converted.
     */
    public static String spellNumber(Object n) {
        String str = n.toString();
        int num = 0;

        if (isAllDigits(str)) {
            num = Integer.parseInt(str);
        } else {
            num = (int) (Double.parseDouble(str));
        }

        NumberSpeller numberSpeller = new NumberSpeller();
        // 0 is used as default setting if there is no gender in the language.
        numberSpeller.setContextGender(0);
        numberSpeller.setNumber(num); //  or numberSpeller.setNumberAsString("25");
        // setting a number for transformation
        return numberSpeller.getSpellForm(); // getting spelling form
    }

    /**
     * replace single quotes with a pair of single quotes
     */
    public static String preserveSingleQuotes(String s) {
        String retvalue = s;
        if (s.indexOf("'") != -1) {
            StringBuilder hold = new StringBuilder();
            char c;
            for (int i = 0; i < s.length(); i++) {
                if ((c = s.charAt(i)) == '\'') {
                    hold.append("''");
                } else {
                    hold.append(c);
                }
            }
            retvalue = hold.toString();
        }
        return retvalue;
    }

    /**
     * replace double quotes with a pair of double quotes
     */
    public static String preserveDoubleQuotes(String s) {
        if (s == null) {
            return s;
        }
        String retvalue = s;
        if (s.indexOf('"') != -1) {
            StringBuilder hold = new StringBuilder();
            char c;
            for (int i = 0; i < s.length(); i++) {
                if ((c = s.charAt(i)) == '"') {
                    hold.append("\"\"");
                } else {
                    hold.append(c);
                }
            }
            retvalue = hold.toString();
        }
        return retvalue;
    }

    public static String replaceSingleQuotes(String s) {
        if (s == null) {
            return "";
        }
        String retvalue = s;
        if (s.indexOf("'") != -1) {
            StringBuilder hold = new StringBuilder();
            char c;
            for (int i = 0; i < s.length(); i++) {
                if ((c = s.charAt(i)) == '\'') {
                    hold.append("\\'");
                } else {
                    hold.append(c);
                }
            }
            retvalue = hold.toString();
        }
        return retvalue;
    }

    /**
     * replace single quotes with a pair of single quotes
     */
    public static String preserveQuotes(String s) {
        if (s == null) {
            return null;
        }

        String retvalue = s;
        if (s.indexOf("'") != -1 || s.indexOf('"') != -1) {
            StringBuilder hold = new StringBuilder();
            char c;
            for (int i = 0; i < s.length(); i++) {
                if ((c = s.charAt(i)) == '\'') {
                    hold.append("''");
                } else if ((c = s.charAt(i)) == '"') {
                    hold.append("\"\"");
                } else {
                    hold.append(c);
                }
            }
            retvalue = hold.toString();
        }
        return retvalue;
    }

    /**
     * Here is the basic rule:
     * 1.  Trim whitespaces from the beginning and end of the string
     * 2.  Replace each instance of double quotes with a pair of double quotes
     * 3.  Enclose the entire string in double quotes
     *
     * @param s
     * @return an Excel-CSV kosher string
     */
    public static String escapeExcelCSV(String s) {
        if (s == null) {
            return null;
        }
        String returnVal = preserveDoubleQuotes(rightTrim(leftTrim(s, ' '), ' '));
        returnVal = "\"" + returnVal + "\"";
        return returnVal;
    }

    /**
     * Escapes JavaScript strings.
     *
     * @param s
     * @return an JavaScript kosher string
     */
    public static String escapeJavaScript(String s) {
        if (s == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int cInt = (int) c;

            if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '\'') {
                sb.append("\\\'");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else if (cInt > 128) {
                sb.append("&#" + cInt + ";");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    /**
     * Escape the 5 entities defined by XML.
     */
    public static String escapeXml(String s) {
        if (s == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int cInt = (int) c;

            if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '\'') {
                sb.append("&#39;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else if (cInt > 128) {
                sb.append("&#" + cInt + ";");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Escape the 5 entities defined by XML.
     */
    public static String escapeXmlNoNull(String s) {
        if (s == null) {
            return "";
        }
        return escapeXml(s);
    }

    /**
     * Escape the few entities defined by XML for e-mail templates.
     */
    public static String escapeSpecialChars(String s, boolean newLineEscape) {
        if (s == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                sb.append("&#39;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c == '"') {
                sb.append("&quot;");
            } else if (c == '#') {
                sb.append("%23");
            } else if (c == '\n') {
                if (newLineEscape) {
                    sb.append("<p>");
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Escape the few entities defined by XML for e-mail templates.
     */
    public static String escapePdfChars(String s) {
        if (s == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                sb.append("\\(");
            } else if (c == ')') {
                sb.append("\\)");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Returns a capitalized version of a string.
     *
     * @param s input string
     */
    public static String capitalize(String s) {
        if (s.length() == 0) {
            return s;
        }
        char chars[] = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    /**
     * Given a string array, return capitalized version of all the strings
     *
     * @param s input array of Strings
     * @return array of capitalized strings
     */
    public static String[] capitalize(String[] s) {
        String[] capitalizedString = new String[s.length];

        for (int i = 0; i < s.length; i++) {
            capitalizedString[i] = capitalize(s[i]);
        }

        return capitalizedString;
    }

    /**
     * Capitalizes the first letter of all words in the string.
     * A "word" is a token, created by splitting the string by whitespace.
     * Returns all whitespace intact.
     */
    public static String capitalizeAllWords(String s) {
        StringBuilder output = new StringBuilder();
        char chars[] = s.toCharArray();
        boolean previousCharWasWhitespace = false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && !Character.isWhitespace(chars[i])) {
                output.append(Character.toUpperCase(chars[i]));
            } else {
                if (previousCharWasWhitespace) {
                    output.append(Character.toUpperCase(chars[i]));
                    previousCharWasWhitespace = false;
                } else {
                    output.append(chars[i]);
                }
            }
            if (Character.isWhitespace(chars[i])) {
                previousCharWasWhitespace = true;
            }
        }
        return output.toString();
    }

    /**
     * Prepend the string with characters.
     */
    public static String prepend(String str, int len, String pre) {
        if (str == null || str.length() >= len || pre == null || pre.length() != 1) {
            return str;
        }

        StringBuilder value = new StringBuilder(str);
        // pre-fill buffer with given string.
        while (value.length() < len) {
            value.insert(0, pre);
        }

        return value.toString();
    }

    /**
     * Return the system-wide line separator
     */
    public static String newLine() {
        return System.getProperty("line.separator");
    }

    public static boolean isPunctuation(char c) {
        boolean result = false;
        for (int index = 0; index < punctuation.length() && !result; index++) {
            if (c == punctuation.charAt(index)) {
                result = true;
            }
        }

        return result;
    }

    public static boolean isStandardASCII(char c) {
        int asciiValue = (int) c;
        return (asciiValue >= 32 && asciiValue <= 126);
    }

    public static String leftTrim(String string, char ch) {
        int stringLength = string.length();
        for (int i = 0; i < stringLength; i += 1) {
            if (string.charAt(i) != ch) {
                return string.substring(i);
            }
        }
        return "";
    }

    public static String rightTrim(String string, char ch) {
        int stringLength = string.length();
        for (int i = stringLength - 1; i > -1; i -= 1) {
            if (string.charAt(i) != ch) {
                return string.substring(0, i + 1);
            }
        }
        return "";
    }

    public static String isEmpty(String value, String defaultValue) {
        return Helper.isEmpty(value) ? defaultValue : value;
    }

    /**
     * Join two strings with separator, remove duplicate separators.
     * Return null if either string is null.
     * Example: join("folder1/", "folder2", "/") returns "folder1/folder2"
     */
    public static String join(String string1, String string2, String separator) {
        if (string1 == null || string2 == null) {
            return null;
        }
        boolean string2StartsWith = string2.startsWith(separator);
        if (string1.endsWith(separator)) {
            if (string2StartsWith) {
                return string1 + string2.substring(1);
            } else {
                return string1 + string2;
            }
        } else if (!string2StartsWith) {
            return string1 + separator + string2;
        } else {
            return string1 + string2;
        }
    }

    /**
     * Remove spaces from the passed string just before and after the delim
     * where ever it occurs and return the new trimmed string.
     * Example: stripSurroundingSpaces("Wage Account  :  Overtime", ":") returns "Wage Account:Overtime"
     */
    public static String stripSurroundingSpaces(String src, String delim) {
        String[] temp = src.split(delim);
        StringBuilder des = new StringBuilder(src.length());
        for (int index = 0; index < temp.length; index++) {
            if (index > 0) {
                des.append(":");
            }
            des.append(temp[index].trim());
        }

        return des.toString();
    }

    /**
     * Generates a string of random alphabetic characters. Letters are
     * both capital and lowercase. Useful as a random password.
     */
    public static String generateRandomAlphabeticString(int length) {
        return generateRandomStringFromCharArray(ALL_ALPHABETIC_CHARS, length);
    }

    public static String generateRandomNumericString(int length) {
        return generateRandomStringFromCharArray(ALL_NUMERIC_CHARS, length);
    }

    // the "ambiguous characters" are 1, I, l, 0 and O.

    public static String generateRandomAlphanumericString(int length, boolean includeAmbiguousChars) {
        if (includeAmbiguousChars) {
            return generateRandomStringFromCharArray(ALL_ALPHANUMERIC_CHARS, length);
        }
        return generateRandomStringFromCharArray(NON_AMBIGUOUS_ALPHANUMERIC_CHARS, length);
    }

    private static Random m_random = new Random();

    private static String generateRandomStringFromCharArray(char[] charArray, int length) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int nextCharIndex = m_random.nextInt(charArray.length);
            output.append(charArray[nextCharIndex]);
        }
        return output.toString();
    }

    /**
     * Separates a camel cased string into it's individual words, capitalized.
     */
    public static String separateCamelCasedString(String s) {
        StringBuilder buf = new StringBuilder();
        buf.append(s.charAt(0));    //start with the first character already appended

        for (int i = 1; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i))) {
                buf.append(" ");
            }

            buf.append(s.charAt(i));
        }

        return StringUtil.capitalize(buf.toString());
    }

    /**
     * toSQLStringList(["ed","bowen"]) = "'ed','bowen'"
     * Does not convert collection elements to String
     *
     * @param collection String collection
     * @return
     */
    public static String toSQLStringList(Collection collection) {
        List<String> quotedStringList = new ArrayList<String>();
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            quotedStringList.add("'" + iterator.next() + "'");
        }
        return concatenate(quotedStringList, ",");
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

    /**
     * toSQLStringList("DE-6,DE-7") = "'DE-6','DE-7'"
     */
    public static String toSQLStringList(String stringList) {
        return toSQLStringList(splitIntoList(stringList, ","));
    }

    /**
     * filter out not allowed characters from a text input and replace them with spaces.
     */
    public static String filterInputText(String inputText) {
        // not allowed characters for text input: <  >  &  %  \  "  '  `  $  :  |  !  [   ]  ^
        final StringBuilder buf = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(inputText);
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
            if (character == '<' || character == '>' || character == '&' || character == '%' || character == '\\' || character == '\"' || character == '\'' || character == '`' || character == '$' || character == ':' || character == '|' || character == '!' || character == '[' || character == ']' || character == '^') {
                buf.append(" ");
            } else {
                buf.append(character);
            }
            character = iterator.next();
        }
        String result = buf.toString();
        return result;
    }

    /**
     * check if a text input contains any not allowed characters.
     */
    public static boolean isValidInputText(String inputText) {
        // not allowed characters for text input: <  >  &  %  \  "      `  $  :  |  !  [   ]  ^
        if (REMatch(".*[<>&%\\\\\"'`\\$:|!\\[\\]\\^].*", inputText, 0)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * ["Ed"] returns "Ed"
     * ["Ed","Nancy"] returns "Ed and Nancy"
     * ["Ed","Nancy","Bob"] returns "Ed, Nancy, and Bob"
     * works with non-String objects
     * doesn't pre-process items (like trimming)
     */
    public static String joinListInEnglish(List items) {
        int size = items.size();
        if (size < 2) {
            return StringUtil.joinListIntoString(items, "");
        } else if (size == 2) {
            return items.get(0).toString() + " and " + items.get(1).toString();
        } else {
            return joinListIntoString(items.subList(0, size - 1), ", ") + ", and " + items.get(size - 1).toString();
        }
    }

    /**
     * Truncates the string at the specified length unless the string is already shorter than
     * the desired length.
     * <p/>
     * Examples:
     * <blockquote><pre>
     * StringUtil.truncate("hamburger", 3) returns "ham"
     * StringUtil.truncate("hamburger", 11) returns "hamburger"
     * </pre></blockquote>
     */
    public static String truncate(String s, int length) {
        if (s == null) {
            return "";
        }
        if (s.length() <= length) {
            return s;
        }
        return s.substring(0, length);
    }

    /**
     * Truncates the substrings within a string to the desired length if necessary.
     * <p/>
     * Examples:
     * <blockquote><pre>
     * StringUtil.truncateParts("ham:bur:ger", ":", 2) returns "ha:bu:ge"
     * StringUtil.truncateParts("hamburg:er", ":", 3) returns "ham:er"
     * </pre></blockquote>
     */
    public static String truncateParts(String s, String delim, int length) {
        if ((s == null) || (s.length() == 0)) {
            return "";
        }

        // Loop through the tokens building a new string.
        String str;
        StringBuffer result = new StringBuffer("");
        StringTokenizer tokenizer = new StringTokenizer(s, delim, true);
        while (tokenizer.hasMoreTokens()) {
            str = tokenizer.nextToken();
            if (str.equals(delim)) {
                result.append(delim);
            } else {
                result.append(StringUtil.truncate(str, length));
            }
        }

        return result.toString();
    }

    /**
     * Finds the earliest position of any of the characters in the chars
     * string or -1 if none are found.  Example: indexOf("abcd", "bc") = 1,
     * indexOf("abc[0].id", "[.") = 3, indexOf("abd.id[0]", "[.") = 3.
     *
     * @param source    The source string to search.
     * @param chars     The characters to find in the source string.
     * @param fromIndex The location to start searching.
     * @return The position of the earliest character found or -1 if not found.
     */
    public static int indexOf(String source, String chars, int fromIndex) {
        // Bail is parameters aren't good.
        if (source == null || chars == null || fromIndex >= source.length()) {
            return -1;
        }

        // Search the source for any of the chars characters
        for (int i = fromIndex; i < source.length(); i++) {
            for (int j = 0; j < chars.length(); j++) {
                if (source.charAt(i) == chars.charAt(j)) {
                    return i;
                }
            }
        }
        return -1;
    }


    /**
     * Trims the passed in referrer to be 119 characters or less.  If it's
     * more than 119 characters, we peel off everything up to the first slash
     * and then whatever we can get on the right hand side.
     *
     * @param referrer The referrer string to trim down to 119 characters.
     * @return The position of the earliest character found or -1 if not found.
     */
    public static String trimReferrer(String referrer) {
        if (referrer.length() > 119) {
            // If source code > 119 characters, then we want to peal off everything up to the
            // the first slash and then whatever we can get on the right hand side.
            String left = "";
            int ix = referrer.indexOf("//");
            if (ix >= 0) {
                ix += 2;
                int iy = ix + referrer.substring(ix).indexOf("/");
                if (iy < 0) {
                    ix = -1;
                } else {
                    ix = iy + 1;
                }
            }
            if (ix >= 0) {
                if (ix > 115) {
                    ix = 115;
                }
                left = referrer.substring(0, ix) + "...";
            }
            ix = left.length();
            referrer = left + referrer.substring(referrer.length() - (119 - ix));
        }

        return referrer;
    }

    /**
     * SC has EINs in the following formats: xxxxxx-n, xxxxxx, 0xxxxxx
     * Customers somtimes enter them as 0xxxxx-x
     * SC wants to receive the EINs as 0xxxxxx, with the final "n" removed
     */
    public static String formatSCEIN(String ein) {
        String scEIN = strip(ein.trim(), "- ");
        if (scEIN.length() < 7) {
            scEIN = "0" + scEIN;
        } else if (scEIN.length() > 6 && !scEIN.startsWith("0")) {
            scEIN = scEIN.substring(0, 6);
            scEIN = "0" + scEIN;
        } else if (scEIN.length() > 7) {
            scEIN = scEIN.substring(0, 7);
        }
        return scEIN;
    }

    /**
     * Constructs a mini stack-trace that includes only the com.paycycle and com.inuit
     * classes starting at callsBack+1.  This allows you to log where a particular
     * call came from in a compact way.
     *
     * @param callsBack The position in the call stack to start (not including this call).
     * @return String containing all com.paycycle and com.intuit calls in compact form.
     */
    public static String getMiniStackTrace(int callsBack) {
        // Add 1 to calls back to account for this call.
        return getMiniStackTrace(new Throwable(), callsBack + 1);
    }

    /**
     * Constructs a mini stack-trace that includes only the com.paycycle and com.inuit
     * classes starting at callsBack.  This allows you to log where a particular
     * call came from in a compact way.
     *
     * @param callsBack The position in the call stack to start (not including this call).
     * @return String containing all com.paycycle and com.intuit calls in compact form.
     */
    public static String getMiniStackTrace(Throwable ex, int callsBack) {
        String result = "";
        try {
            StackTraceElement[] stackTraceElements = ex.getStackTrace();

            // Find the start and end of what we want to output.
            boolean done = false;
            int startPosition = callsBack;
            int endPosition = callsBack;
            do {
                String name = stackTraceElements[endPosition].getClassName();
                if (name.indexOf("com.paycycle") < 0 && name.indexOf("com.intuit") < 0) {
                    done = true;
                } else {
                    endPosition++;
                }
            } while (!done && endPosition <= stackTraceElements.length);

            // Go backwards building up the string.
            for (int i = endPosition - 1; i >= startPosition; i--) {
                StackTraceElement elem = stackTraceElements[i];
                String name = elem.getClassName();
                if (name.indexOf("com.paycycle") >= 0 || name.indexOf("com.intuit") >= 0) {
                    name = name.substring(name.lastIndexOf('.') + 1) + "." + elem.getMethodName() + "():" + elem.getLineNumber();
                    result = result.length() > 0 ? result + "->" + name : name;
                } else {
                    done = true;
                }
            }
        } catch (Exception e) {
            result = "Unknown";
        }

        return result;
    }

    /**
     * Returns the direct caller of the method in class.method():nn format where class is the short class nema
     * method is the method name and nn is the line number.
     *
     * @param callsBack The number of calls back to get the caller.
     * @return String containing the caller class, method name and line number.
     */
    public static String getCaller(int callsBack, boolean includeLineNumber) {
        try {
            Throwable ex = new Throwable();
            StackTraceElement[] stackTraceElements = ex.getStackTrace();
            StackTraceElement elem = stackTraceElements[callsBack + 2];  // Add 1 for this call and 1 to get caller.
            return elem.getClassName().substring(elem.getClassName().lastIndexOf('.') + 1) + "." + elem.getMethodName() + "()" + (includeLineNumber ? ":" + elem.getLineNumber() : "");
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * Parses a string into a Long.  If there is any exception, null is returned.
     *
     * @param str The string to convert to a Long.
     * @return str converted to a Long or null if the conversion failed.
     */
    public static Long parseLong(String str) {
        if (str == null) {
            return null;
        }

        try {
            return Long.parseLong(str);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

