/*
 * Copyright Statement:
 * CONFIDENTIAL -- Copyright 2000-2003 Intuit Inc. This material contains
 * certain trade secrets and confidential and proprietary information
 * of Intuit Inc. Use, reproduction, disclosure and distribution by
 * any means are prohibited, except pursuant to a written license from
 * Intuit Inc. Use of copyright notice is precautionary and does not
 * imply publication or disclosure.
 */
package com.intuit.payroll.agency.dao.mnemonics;

import java.util.Arrays;
import java.util.List;

public class MnemonicPeriod {
	public String start;
	public String end;
	public String dueOn;
	public String uiString;

    public final static List<String> daysOfWeek = Arrays.asList("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT");

	public MnemonicPeriod() {}

	public String toString () {
		StringBuffer buf = new StringBuffer();
		buf.append("MnemonicPeriod: ");
		buf.append(uiString);
//		buf.append(" ");
//		buf.append(end);
//		buf.append(" ");
		buf.append ("\r\n");
		return buf.toString();
	}

    // Returns Calendar object's DayOfWeek (1-7)
    public int getDueOnDayOfWeek() {
        int index = daysOfWeek.indexOf(dueOn);
        return index == -1 ? -1 : index + 1;
    }
}
