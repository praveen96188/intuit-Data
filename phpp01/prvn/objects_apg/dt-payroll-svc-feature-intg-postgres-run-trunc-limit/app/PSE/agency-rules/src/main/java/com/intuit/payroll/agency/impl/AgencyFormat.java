package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.IAgencyFormat;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/PSE/AgencyRules/src/com/intuit/payroll/agency/impl/AgencyFormat.java $
 * $Revision: #1 $
 * $DateTime: 2012/06/20 23:57:52 $
 * $Author: JChickanosky $
 */
public class AgencyFormat implements IAgencyFormat {
    private String m_format = "";

    public String getFormat() {
        return m_format;
    }

    public void setFormat(String format) {
        this.m_format = format;
    }

    public String getRegularExpression() {
        StringBuilder regularExpressionSB = new StringBuilder();
        if (m_format==null || m_format.length() == 0) {
            return "";
        }
        String lastMatchStr = getReplacementRegExStringForChar(m_format.charAt(0));
        if (m_format==null || m_format.length() == 0) {
            return lastMatchStr;
        }
        int matchCnt = 0;
        for (int formatIndex=1,formatLen=m_format.length();formatIndex<formatLen;formatIndex++) {
            boolean lastCharToProcess = formatIndex == formatLen-1;
            char chr = m_format.charAt(formatIndex);
            String matchStr = getReplacementRegExStringForChar(chr);
            if (lastCharToProcess) {
                regularExpressionSB.append(lastMatchStr);
                if (matchStr.compareTo(lastMatchStr)==0) {
                    regularExpressionSB.append("{"+(matchCnt+1+1)+"}");
                } else {
                    if (matchCnt > 0) {
                        regularExpressionSB.append("{"+(matchCnt+1)+"}");
                    }
                    regularExpressionSB.append(matchStr);
                }
            } else {
                if (matchStr.compareTo(lastMatchStr)==0) {
                    matchCnt++;
                } else {
                    regularExpressionSB.append(lastMatchStr);
                    if (matchCnt > 0) {
                        regularExpressionSB.append("{"+(matchCnt+1)+"}");
                    }
                    matchCnt = 0;
                    lastMatchStr = matchStr;
                }
            }
        }
        return "^" + regularExpressionSB.toString() + "$";
    }

    //?	9 represents any number.
    //?	0 represents the number 0.
    //?	Z represents any letter.
    //?	# represents any number or letter.
    //?	? represents any character, including a punctuation mark.
    private String getReplacementRegExStringForChar(char pChar) {
        switch (pChar) {
            case '9':
                return "\\d";
            case 'Z':
                return "[a-zA-Z]";
            case '#':
                return "[a-zA-Z0-9]";
            case '?':
                return ".";
        }
        return pChar + "";
    }
}
