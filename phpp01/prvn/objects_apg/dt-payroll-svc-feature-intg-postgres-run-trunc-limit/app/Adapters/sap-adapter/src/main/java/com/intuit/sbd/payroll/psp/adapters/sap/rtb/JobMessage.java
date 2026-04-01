package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

/**
 * Created by anandp233 on 2/23/14.
 */
public class JobMessage {

    private String message;
    private String code;

    public JobMessage(String message, String code) {
        this.message = message;
        this.code = (code == null) ? "" : code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String pMessage) {
        message = pMessage;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String pCode) {
        code = pCode;
    }

    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
        String newLine = System.getProperty("line.separator");
        if (newLine == null) newLine = "\n";

        output.append(" Message: ");
        output.append(message);
        output.append("\t" + code);
        output.append(newLine);

        return output.toString();
    }
}
