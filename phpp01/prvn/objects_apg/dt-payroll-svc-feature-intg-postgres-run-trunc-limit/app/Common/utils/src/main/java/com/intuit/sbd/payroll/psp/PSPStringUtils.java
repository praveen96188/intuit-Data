package com.intuit.sbd.payroll.psp;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class PSPStringUtils {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(PSPStringUtils.class);

    private static final String REGEX_ONLY_DIGITS = "[^0-9]";
    private static final Pattern pattern = Pattern.compile(REGEX_ONLY_DIGITS);

    /**
     * compares if two strings are equal (ignoring case),
     * it treats empty String and null String as equal
     * @param s1
     * @param s2
     * @return
     */
    public static boolean isEqual(String s1 , String s2){
        if(StringUtils.isEmpty(s1) && StringUtils.isEmpty(s2)){
            return true;
        }
        return StringUtils.equalsIgnoreCase(s1,s2);


    }

    /**
     * evaluates if a given string is all digits
     * @param str
     * @return
     */
    public static boolean isOnlyDigits(@NonNull String str) {

        return pattern.matcher(str).find();
    }

    /**
     * Given a string, this method extracts only digits
     * from that string
     *
     * @param value
     * @return
     */
    public static CharSequence extractDigitsOnly(String value) {
        if (StringUtils.isEmpty(value)) {
            return StringUtils.EMPTY;
        }
        return value.replaceAll(REGEX_ONLY_DIGITS, StringUtils.EMPTY);
    }

    /**
     * This method is used to compare digits in 2 strings
     * @param s1
     * @param s2
     * @return
     */
    public static boolean equalsIgnoreNonDigits(String s1, String s2) {
        return StringUtils.equals(extractDigitsOnly(s1), extractDigitsOnly(s2));
    }
}
