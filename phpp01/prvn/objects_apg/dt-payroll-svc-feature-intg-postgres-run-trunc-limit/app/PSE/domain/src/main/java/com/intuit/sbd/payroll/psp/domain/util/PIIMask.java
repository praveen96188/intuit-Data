package com.intuit.sbd.payroll.psp.domain.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: mwaqarbaig
 * Date: Mar 9, 2011
 * Time: 11:32:02 AM
 */
public class PIIMask {
    public static String getMaskedString(String pInputString, Boolean pCanViewFullBankAccountNumbers, Boolean pCanViewEEPII) {
        String[] piiRegEx = {
                "<ACCTID>.*\\s*",
                "<AccountNumber>.*<\\/AccountNumber>\\s*",
                "<TaxId>.*<\\/TaxId>\\s*",
                "<SocialSecurityNumber>.*<\\/SocialSecurityNumber>\\s*",
                "<I.SSN>.*\\s*"
        };

        if (!pCanViewFullBankAccountNumbers || !pCanViewEEPII) {
            for (int i = 0; i < piiRegEx.length; i++) {
                Pattern pattern = Pattern.compile(piiRegEx[i], Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(pInputString);
                StringBuffer stringBuffer = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(stringBuffer, computeReplacementString(pInputString.substring(matcher.start(), matcher.end()), i, !pCanViewFullBankAccountNumbers, !pCanViewEEPII));
                }
                matcher.appendTail(stringBuffer);
                pInputString = stringBuffer.toString();
            }
        }
        return pInputString;
    }

    private static String computeReplacementString(String originalString, int index, Boolean pCan_tViewFullBankAccountNumbers, Boolean pCan_tViewEEPII) {
        originalString = originalString.trim();
        String textToMask = originalString.substring(beginningTags[index].length(), originalString.length() - endingTags[index].length() + 1);
        String maskedText = textToMask;
        if (beginningTags[index].equalsIgnoreCase("<ACCTID>") && pCan_tViewFullBankAccountNumbers) {
            maskedText = maskAccountId(textToMask);
        }
        else if ((beginningTags[index].equalsIgnoreCase("<I.SSN>") || beginningTags[index].equalsIgnoreCase("<SocialSecurityNumber>")) && pCan_tViewEEPII) {
            maskedText = maskSSN(textToMask);
        }else if (beginningTags[index].equalsIgnoreCase("<AccountNumber>") &&  pCan_tViewFullBankAccountNumbers) {
            maskedText = maskText(textToMask);
        }

        return beginningTags[index] + maskedText + endingTags[index];
    }

    private static String maskSSN(String pSSNToMask) {
        if (pSSNToMask == null) {
            return "";
        }
        return maskText(pSSNToMask);
    }

    private static String maskAccountId(String pAcctIdToMask) {
        if (pAcctIdToMask == null) {
            return "";
        }
        return maskText(pAcctIdToMask);
    }

    public static String maskText(String pTextToMask, boolean mask) {
        return mask ? maskText(pTextToMask) : pTextToMask;
    }

    public static String maskText(String pTextToMask, int digitsToShow) {
        if (pTextToMask == null) {
            return "";
        }
        int strLen = pTextToMask.length();
        if (strLen <= digitsToShow) {
            return pTextToMask;
        }
        String retString = "";
        for (int i = 0; i < (strLen - digitsToShow); i++) {
            retString += "*";
        }
        retString += pTextToMask.substring(strLen - digitsToShow);
        return retString;
    }

    public static String maskText(String pTextToMask) {
        return maskText(pTextToMask, 4);
    }

    public static String getMaskedDate(SpcfCalendar date) {
        if (Objects.isNull(date)) {
            return StringUtils.EMPTY;
        }
        String dateMaskPattern = "%d/**/%d"; // mm/dd/yyyy - Mask only day
        return String.format(dateMaskPattern, date.getMonth(), date.getYear());
    }

    public static String getMaskedLastNDigits(String pTextToMask) {
        if (StringUtils.isEmpty(pTextToMask)) {
            return StringUtils.EMPTY;
        }

        int strLen = pTextToMask.length();
        int digitsToMask = (strLen > 6) ? 5 : 4;
        if (strLen <= digitsToMask) {
            return pTextToMask;
        }

        String clearText = pTextToMask.substring(0, strLen - digitsToMask);
        return StringUtils.rightPad(clearText, strLen, "*");
    }

    private static String[] beginningTags = {
            "<ACCTID>",
            "<AccountNumber>",
            "<TaxId>",
            "<SocialSecurityNumber>",
            "<I.SSN>"
    };
    private static String[] endingTags = {
            "\n",
            "</AccountNumber>\n",
            "</TaxId>\n",
            "</SocialSecurityNumber>\n",
            "\n"
    };

    public static boolean authenticatedUserCanViewEEPII() {
        AuthUser foundUser = AuthUser.findUser(Application.getCurrentPrincipal().getId());
        return foundUser == null || foundUser.hasOperation(OperationId.ViewEEPII);
    }

    public static boolean authenticatedUserCanViewFullBankAccountNumbers() {
        AuthUser foundUser = AuthUser.findUser(Application.getCurrentPrincipal().getId());
        return foundUser == null || foundUser.hasOperation(OperationId.ViewFullBankAccountNumbers);
    }
}
