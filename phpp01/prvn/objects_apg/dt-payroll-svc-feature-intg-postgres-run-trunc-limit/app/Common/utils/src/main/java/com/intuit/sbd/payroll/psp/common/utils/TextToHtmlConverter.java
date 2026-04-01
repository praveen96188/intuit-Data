package com.intuit.sbd.payroll.psp.common.utils;

public class TextToHtmlConverter {

    /**
     * this method returns html text for a given text content
     * converts carriage return to single line break
     * converts newline to single line break
     * converts empty char ' ' to fixed space
     * @return
     */
    public static String textToHTML(String text) {
        if(text == null) {
            return null;
        }
        int length = text.length();
        boolean previousWasASpace = false;
        StringBuffer out = new StringBuffer();
        for(int i = 0; i < length; i++) {
            char ch = text.charAt(i);
            switch(ch) {
                case '\r':
                    if(previousWasASpace) {
                        out.append("<br/>");
                    }
                    previousWasASpace = true;
                    break;
                case '\n':
                    previousWasASpace = false;
                    out.append("<br/>");
                    break;
                case ' ':
                    out.append("&nbsp;");
                    out.append("\t");
                    break;
                default:
                    if(previousWasASpace) {
                        out.append("<br>");
                        previousWasASpace = false;
                    }
                    out.append(ch);
                    break;
            }
        }
        return out.toString();
    }
}