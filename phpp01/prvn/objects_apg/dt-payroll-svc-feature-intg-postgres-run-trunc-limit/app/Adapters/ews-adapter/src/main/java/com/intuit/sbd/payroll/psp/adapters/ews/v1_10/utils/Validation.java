package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: May 19, 2010
 * Time: 10:13:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class Validation {

    public static Boolean validateValue(String pValue, boolean pNullable, String pPattern) {
        Pattern pattern = Pattern.compile(pPattern);

        if ((!pNullable) && (pValue == null)) {
            return false;
        }

        if (pValue == null) {
            return true;
        }

        Matcher matcher = pattern.matcher(pValue.trim());
        return matcher.matches();
    }

}
