package com.intuit.sbd.payroll.psp.util;

import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA. User: dhaddan Date: Oct 30, 2006 Time: 4:09:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class NACHAStringEncoder {
	static Hashtable valueMap = new Hashtable();

	/**
	 * This class simply encodes special characters for consumption by XML
	 * parsers. In addition to encoding xml entities, replace characters outside
	 * of 20-7F
	 *
	 * @param text
	 *            holds the data (must be single byte ascii) to be scanned for
	 *            special case characters.
	 * @return the text including the encoded characters. null is returned if
	 *         the input string is empty.
	 */
	public static String toValidNACHAFormat(String text) {
		StringBuffer sb = new StringBuffer();
		if (text == null) {
			return null;
		}
		int len = text.length();
		char ch = 0;
		for (int i = 0; i < len; i++) {
			ch = text.charAt(i);

			if (ch >= 0x00C0 && ch <= 0x00C5) {
				sb.append('A');
			} else if (ch == 0x00C7) {
				sb.append('C');
			} else if (ch == 0x00C6) {
				sb.append("AE");
			} else if (ch >= 0x00C8 && ch <= 0x00CB) {
				sb.append('E');
			} else if (ch >= 0x00CC && ch <= 0x00CF) {
				sb.append('I');
			} else if (ch == 0x00D0) {
				sb.append('D');
			} else if (ch == 0x00D1) {
				sb.append('N');
			} else if ((ch >= 0x00D2 && ch <= 0x00D6) || (ch == 0x00D8)) {
				sb.append('O');
			} else if (ch == 0x00DD) {
				sb.append('Y');
			} else if (ch >= 0x00D9 && ch <= 0x00DC) {
				sb.append('U');
			} else if (ch == 0x00DF) {
				// German B (0x00DF) is commonly translated to SS in ASCII
				sb.append("SS");
			} else if (ch >= 0x00E0 && ch <= 0x00E5) {
				sb.append('a');
			} else if (ch == 0x00E6) {
				sb.append("ae");
			} else if (ch == 0x00E7) {
				sb.append('c');
			} else if (ch >= 0x00E8 && ch <= 0x00EB) {
				sb.append('e');
			} else if (ch >= 0x00EC && ch <= 0x00EF) {
				sb.append('i');
			} else if (ch == 0x00F0) {
				sb.append('d');
			} else if (ch == 0x00F1) {
				sb.append('n');
			} else if ((ch >= 0x00F2 && ch <= 0x00F6) || (ch == 0x00F8)) {
				sb.append('o');
			} else if (ch >= 0x00F9 && ch <= 0x00FC) {
				sb.append('u');
			} else if (ch == 0x00FD || ch == 0x00FF) {
				sb.append('y');
			} else if (ch == 0x00DE || ch == 0x00FE) {
				sb.append('b');
			}  else if (ch >= 0x0020 && ch <= 0x007F) {
				sb.append(ch);
			} else {
				sb.append('?');
			}
		}
		return sb.toString();
	}
}

