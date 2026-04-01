package com.intuit.sbd.payroll.psp.util;

import java.util.regex.Pattern;

/**
 * reusable validation methods
 *
 * @author Wiktor Kozlik
 */
public final class Validator
{

    public static boolean isMatchingPattern(String pValue, String pPattern)
    {
        return pValue == null || Pattern.matches(pPattern, pValue);
    }

    public static boolean isMatchingPattern(String pValue, String pPattern, int pFlags)
    {
        return pValue == null || Pattern.compile(pPattern, pFlags).matcher(pValue).matches();
    }

    public static boolean isValidLength(String pValue, int pMin, int pMax)
    {
        return pValue == null || pValue.length() >= pMin && pValue.length() <= pMax;
    }



    private static Pattern EMAIL_PATTERN = null;
    private static Pattern PHONE_PATTERN = null;
    private static Pattern EIN_PATTERN = null;

    /**
     * Initialization of EMAIL_PATTERN for regular expression validation
     */
    static
    {
        //RFC 2822 token definitions for valid email
        final String sp = "\\!\\#\\$\\%\\&\\'\\*\\+\\-\\/\\=\\?\\^\\_\\`\\{\\|\\}\\~";
        final String atext = "[a-zA-Z0-9" + sp + "]";
        final String atom = atext + "+"; //one or more atext chars
        final String dotAtom = "\\." + atom;
        final String localPart = atom + "(" + dotAtom + ")*";
        //one atom followed by 0 or more dotAtoms.
        //RFC 1035 tokens for domain names:
        final String letter = "[a-zA-Z]";
        final String letDig = "[a-zA-Z0-9]";
        final String letDigHyp = "[a-zA-Z0-9-]";
        final String rfcLabel = letDig + letDigHyp + "{0,61}" + letDig;
        
        String domainExpression = "[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])*(\\.[a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9])*\\.[a-zA-Z]{2,6}";
        //Combined together, these form the allowed email regexp allowed by RFC 2822:
        final String addrSpec = "^" + localPart + "@" + domainExpression + "$";
        EMAIL_PATTERN = Pattern.compile( addrSpec );

        PHONE_PATTERN = Pattern.compile("^(1\\s*[-\\/\\.]?)?(\\(([2-9]{1}[0-9]{2})\\)|([2-9]{1}[0-9]{2}))\\s*[-\\/\\.]?\\s*(\\d{3})\\s*[-\\/\\.]?\\s*(\\d{4})\\s*(([xX]|[eE][xX][tT])\\.?\\s*(\\d+))*$");

        EIN_PATTERN = Pattern.compile("^[0-9]{9}$");
    }

    public static boolean isValidEIN(String pEIN) {
        boolean isValid = pEIN != null;
        if (isValid) {
            isValid = EIN_PATTERN.matcher(pEIN).matches();
        }
        return isValid;
    }

    public static boolean isValidEmail(String pValue)
    {
        boolean isValid = false;
        if (pValue == null) {
            isValid = true;
        } else if ((pValue.length() >= 6 && pValue.length() <= 100) && EMAIL_PATTERN.matcher(pValue).matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean isValidPhone(String pValue) {
        if (pValue == null)
            return true;

        return (PHONE_PATTERN.matcher(pValue).matches());
    }

    public static boolean isInteger(String value) {
        if (value == null || value.trim().length() == 0)
            return false;

        try {
            Integer.parseInt(value);
            return true;
        }
        catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean isNonNegativeInteger(String value) {
        if (value == null || value.trim().length() == 0)
            return false;

        try {
            return Integer.parseInt(value) >= 0;
        }
        catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean isIntegerInRange(String value, Integer minValue, Integer maxValue) {
        if (value == null || value.trim().length() == 0)
            return false;

        try {
            int val = Integer.parseInt(value);

            if (minValue != null) {
                if (val < minValue.intValue())
                    return false;
            }

            if (maxValue != null) {
                if (val > maxValue.intValue())
                    return false;
            }

            return true;
        }
        catch (NumberFormatException nfe) {
            return false;
        }                
    }

    public static boolean isDouble(String value) {
        if (value == null || value.trim().length() == 0)
            return false;

        try {
            Double.parseDouble(value);
            return true;
        }
        catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean isNonNegativeDouble(String value) {
        if (value == null || value.trim().length() == 0)
            return false;

        try {
            return Double.parseDouble(value) >= 0;
        }
        catch (NumberFormatException nfe) {
            return false;
        }
    }
}
